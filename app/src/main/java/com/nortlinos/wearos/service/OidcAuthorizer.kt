package com.nortlinos.wearos.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.wear.phone.interactions.authentication.OAuthRequest
import androidx.wear.phone.interactions.authentication.OAuthResponse
import androidx.wear.phone.interactions.authentication.RemoteAuthClient
import com.nortlinos.wearos.data.api.OidcFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** Outcome of showing the identity provider's sign-in UI to the user. */
sealed interface AuthorizeOutcome {
    /** The browser came back to our redirect URI; [redirectUrl] still needs `state` validation. */
    data class Success(val redirectUrl: String) : AuthorizeOutcome

    /** This transport cannot run here (no phone paired, no browser installed, …). Try the next. */
    data class Unavailable(val reason: String) : AuthorizeOutcome

    /** The attempt genuinely failed; retrying with a different transport will not help. */
    data class Failed(val message: String) : AuthorizeOutcome
}

/**
 * One way of getting the user through an identity provider's sign-in page and back again.
 *
 * Each transport owns a [redirectUri] because the two differ: the phone companion only intercepts
 * its own `wear.googleapis.com` URL, while a browser on the watch can only return through a custom
 * scheme this app has registered. The server validates that URI against an admin-managed allow
 * list, so the sign-in flow tries transports in order and moves on when the server rejects one.
 */
interface OidcAuthorizer {
    val redirectUri: String
    suspend fun authorize(authorizationUrl: String): AuthorizeOutcome
}

/**
 * Hands sign-in to the paired phone through the Wear OS companion app.
 *
 * This is the only transport that works on watches with no browser, which is most of them.
 */
class PhoneOidcAuthorizer(
    private val context: Context,
    private val packageName: String
) : OidcAuthorizer {
    override val redirectUri: String = OidcFlow.wearRedirectUri(packageName)

    override suspend fun authorize(authorizationUrl: String): AuthorizeOutcome =
        withContext(Dispatchers.Main) {
            val request = buildRequest(authorizationUrl)
                ?: return@withContext AuthorizeOutcome.Unavailable(
                    "Phone sign-in is not supported on this watch"
                )
            val client = runCatching { RemoteAuthClient.create(context) }.getOrNull()
                ?: return@withContext AuthorizeOutcome.Unavailable("Phone sign-in is unavailable")
            try {
                suspendCancellableCoroutine<AuthorizeOutcome> { continuation ->
                    val executor = Executor { it.run() }
                    client.sendAuthorizationRequest(
                        request,
                        executor,
                        object : RemoteAuthClient.Callback() {
                            override fun onAuthorizationResponse(
                                request: OAuthRequest,
                                response: OAuthResponse
                            ) {
                                if (!continuation.isActive) return
                                val url = response.responseUrl?.toString()
                                continuation.resume(
                                    if (url.isNullOrBlank()) {
                                        AuthorizeOutcome.Failed("Phone sign-in returned no response")
                                    } else {
                                        AuthorizeOutcome.Success(url)
                                    }
                                )
                            }

                            override fun onAuthorizationError(request: OAuthRequest, errorCode: Int) {
                                if (!continuation.isActive) return
                                continuation.resume(
                                    when (errorCode) {
                                        RemoteAuthClient.ERROR_UNSUPPORTED -> AuthorizeOutcome.Unavailable(
                                            "This watch cannot sign in through the phone"
                                        )
                                        RemoteAuthClient.ERROR_PHONE_UNAVAILABLE -> AuthorizeOutcome.Unavailable(
                                            "Phone not reachable"
                                        )
                                        else -> AuthorizeOutcome.Failed("Phone sign-in failed")
                                    }
                                )
                            }
                        }
                    )
                }
            } finally {
                runCatching { client.close() }
            }
        }

    /**
     * Wraps the server-built authorization URL without letting [OAuthRequest.Builder] touch it.
     *
     * The builder insists on rewriting `redirect_uri` to its own `wear.googleapis.com` value, but
     * in this flow the identity provider must redirect to the *Audiobookshelf server*, which then
     * forwards to the companion URL. Passing the URL through verbatim is therefore required.
     * [WearOAuthRequests] reaches the constructor the builder normally hides; if a future version
     * of the library closes that door the failure is contained here and the browser transport is
     * used instead.
     */
    private fun buildRequest(authorizationUrl: String): OAuthRequest? = runCatching {
        WearOAuthRequests.create(packageName, Uri.parse(authorizationUrl))
    }.getOrNull()
}

/**
 * Opens the identity provider in a browser on the watch itself and waits for the custom-scheme
 * redirect to be delivered back through [OidcRedirectBus].
 */
class WatchBrowserOidcAuthorizer(
    private val context: Context,
    override val redirectUri: String,
    private val redirects: OidcRedirectBus
) : OidcAuthorizer {

    override suspend fun authorize(authorizationUrl: String): AuthorizeOutcome {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(context.packageManager) == null) {
            return AuthorizeOutcome.Unavailable("No browser installed on this watch")
        }
        // Registered before launching so a fast redirect cannot arrive before anyone is waiting.
        val pending = redirects.expect()
        return try {
            context.startActivity(intent)
            val redirect = withTimeoutOrNull(AUTHORIZE_TIMEOUT_MS) { pending.await() }
            if (redirect == null) {
                AuthorizeOutcome.Failed("Sign-in timed out")
            } else {
                AuthorizeOutcome.Success(redirect)
            }
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            // The sign-in was abandoned, not refused by this transport: falling through to the
            // next one would launch another browser for a request nobody is waiting on.
            throw cancellation
        } catch (error: Exception) {
            AuthorizeOutcome.Unavailable(error.message ?: "Could not open a browser")
        } finally {
            redirects.stopExpecting(pending)
        }
    }

    private companion object {
        const val AUTHORIZE_TIMEOUT_MS = 5 * 60 * 1000L
    }
}

/**
 * Carries custom-scheme redirects from the activity that receives them to the sign-in attempt
 * waiting for one. At most one attempt can be outstanding, so a redirect belonging to an abandoned
 * attempt is dropped rather than satisfying a later one.
 */
@Singleton
class OidcRedirectBus @Inject constructor() {
    private val lock = Any()
    private var pending: CompletableDeferred<String>? = null

    fun publish(redirectUrl: String) {
        val waiter = synchronized(lock) { pending.also { pending = null } }
        waiter?.complete(redirectUrl)
    }

    fun expect(): CompletableDeferred<String> {
        val deferred = CompletableDeferred<String>()
        synchronized(lock) {
            pending?.cancel()
            pending = deferred
        }
        return deferred
    }

    fun stopExpecting(deferred: CompletableDeferred<String>) {
        synchronized(lock) { if (pending === deferred) pending = null }
        deferred.cancel()
    }
}

/** Builds the ordered list of transports to try for a sign-in attempt. */
@Singleton
class OidcAuthorizers @Inject constructor(
    @ApplicationContext private val context: Context,
    private val redirects: OidcRedirectBus
) {
    fun all(): List<OidcAuthorizer> = listOf(
        PhoneOidcAuthorizer(context, context.packageName),
        WatchBrowserOidcAuthorizer(context, OidcFlow.APP_SCHEME_REDIRECT, redirects),
        // Audiobookshelf allow-lists this URI out of the box, so it works without the server
        // admin registering anything for this app.
        WatchBrowserOidcAuthorizer(context, OidcFlow.DEFAULT_SCHEME_REDIRECT, redirects)
    )
}
