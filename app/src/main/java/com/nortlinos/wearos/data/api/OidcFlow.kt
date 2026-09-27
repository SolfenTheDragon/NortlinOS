package com.nortlinos.wearos.data.api

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Pure logic for Audiobookshelf's OpenID Connect "mobile" login flow.
 *
 * Audiobookshelf deliberately keeps the identity provider at arm's length from the app: the app
 * never talks to the IdP's token endpoint and never learns the OIDC client secret. The shape of
 * the flow is
 *
 *  1. `GET /auth/openid?redirect_uri=…&code_challenge=…&code_challenge_method=S256&response_type=code`
 *     with redirects *disabled*. The server answers `302` and the `Location` header is the IdP's
 *     fully-built authorization URL. The server also drops an Express session cookie which must be
 *     replayed in step 3, so both calls have to share one cookie jar.
 *  2. The user completes sign-in in a real browser. The IdP redirects to the server's own
 *     `/auth/openid/mobile-redirect`, which in turn redirects to the `redirect_uri` we asked for,
 *     carrying `code` and `state`.
 *  3. `GET /auth/openid/callback?state=…&code=…&code_verifier=…` exchanges the code for the same
 *     JSON payload `POST /login` returns.
 *
 * The server validates `redirect_uri` against an admin-managed allow-list, so it must be one of the
 * values in [redirectCandidates] (or the admin has to add ours). PKCE is mandatory for this flow
 * and only `S256` is accepted.
 */
object OidcFlow {
    /**
     * Prefix of the redirect URL the Wear OS companion app intercepts on the paired phone. The
     * app's package name is appended so another app cannot claim our responses.
     */
    const val WEAR_REDIRECT_PREFIX = "https://wear.googleapis.com/3p_auth/"

    /** Custom scheme handled by this app, used when a browser is available on the watch itself. */
    const val APP_SCHEME_REDIRECT = "nortlinos://oauth"

    /** Audiobookshelf's out-of-the-box allow-listed redirect, used by the official mobile app. */
    const val DEFAULT_SCHEME_REDIRECT = "audiobookshelf://oauth"

    private const val VERIFIER_BYTES = 64
    private const val STATE_BYTES = 16

    private val encoder: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()

    fun wearRedirectUri(packageName: String): String = WEAR_REDIRECT_PREFIX + packageName

    /** True when the server advertises OIDC as an active auth method on `GET /status`. */
    fun supportsOpenId(status: ServerStatusDto?): Boolean =
        status?.authMethods?.any { it.equals("openid", ignoreCase = true) } == true

    /** Label for the sign-in affordance; servers may customise it. */
    fun buttonText(status: ServerStatusDto?): String =
        status?.authFormData?.authOpenIDButtonText?.takeIf { it.isNotBlank() } ?: "Sign in with SSO"

    fun newCodeVerifier(random: SecureRandom = SecureRandom()): String =
        encoder.encodeToString(ByteArray(VERIFIER_BYTES).also(random::nextBytes))

    fun newState(random: SecureRandom = SecureRandom()): String =
        encoder.encodeToString(ByteArray(STATE_BYTES).also(random::nextBytes))

    /** PKCE S256 challenge: BASE64URL(SHA-256(verifier)), unpadded. */
    fun codeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        return encoder.encodeToString(digest)
    }

    /**
     * Query parameters for `GET /auth/openid`. Sending [redirectUri] is what puts the server into
     * mobile-flow mode; `client_id` is ignored by the server (it uses its own configured one) but
     * the resulting IdP URL must carry one for the Wear companion to accept the request.
     */
    fun authorizeQuery(redirectUri: String, codeChallenge: String, state: String): Map<String, String> =
        linkedMapOf(
            "response_type" to "code",
            "redirect_uri" to redirectUri,
            "code_challenge" to codeChallenge,
            "code_challenge_method" to "S256",
            "state" to state
        )

    sealed interface RedirectResult {
        data class Success(val code: String) : RedirectResult
        data class Failure(val message: String) : RedirectResult
    }

    /**
     * Validates the redirect the browser (or the phone companion) handed back and extracts the
     * authorization code. A mismatched `state` means the response belongs to a different attempt
     * and must never be exchanged.
     */
    fun parseRedirect(redirectUrl: String?, expectedState: String): RedirectResult {
        if (redirectUrl.isNullOrBlank()) return RedirectResult.Failure("Sign-in was cancelled")
        val params = parseQuery(redirectUrl)
        params["error"]?.let { error ->
            val detail = params["error_description"]?.takeIf { it.isNotBlank() }
            return RedirectResult.Failure(detail ?: "Identity provider rejected the sign-in ($error)")
        }
        val state = params["state"]
        if (state != expectedState) return RedirectResult.Failure("Sign-in response did not match this request")
        val code = params["code"]?.takeIf { it.isNotBlank() }
            ?: return RedirectResult.Failure("Sign-in response did not contain an authorization code")
        return RedirectResult.Success(code)
    }

    /**
     * Minimal query-string parser. [java.net.URI] rejects some of the custom-scheme URLs providers
     * emit, and `android.net.Uri` is unavailable to unit tests, so the query is split by hand.
     */
    internal fun parseQuery(url: String): Map<String, String> {
        val start = url.indexOf('?')
        if (start < 0 || start == url.lastIndex) return emptyMap()
        val query = url.substring(start + 1).substringBefore('#')
        val result = LinkedHashMap<String, String>()
        for (pair in query.split('&')) {
            if (pair.isEmpty()) continue
            val separator = pair.indexOf('=')
            val rawKey = if (separator < 0) pair else pair.substring(0, separator)
            val rawValue = if (separator < 0) "" else pair.substring(separator + 1)
            val key = decode(rawKey)
            if (key.isNotEmpty() && !result.containsKey(key)) result[key] = decode(rawValue)
        }
        return result
    }

    private fun decode(value: String): String =
        runCatching { java.net.URLDecoder.decode(value, "UTF-8") }.getOrDefault(value)
}
