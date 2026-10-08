package com.nortlinos.wearos.data.repository

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.room.withTransaction
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import com.nortlinos.wearos.data.api.ApiClient
import com.nortlinos.wearos.data.api.AudioBookmarkDto
import com.nortlinos.wearos.data.api.AudiobookshelfApi
import com.nortlinos.wearos.data.api.BookmarkRequest
import com.nortlinos.wearos.data.api.LoginRequest
import com.nortlinos.wearos.data.api.OidcFlow
import com.nortlinos.wearos.data.api.ProgressUpdateRequest
import com.nortlinos.wearos.data.local.AppDatabase
import com.nortlinos.wearos.data.local.ChapterEntity
import com.nortlinos.wearos.data.local.DownloadDao
import com.nortlinos.wearos.data.local.DownloadStatus
import com.nortlinos.wearos.data.local.DownloadedBookData
import com.nortlinos.wearos.data.local.DownloadedItemEntity
import com.nortlinos.wearos.data.local.LibraryDao
import com.nortlinos.wearos.data.local.LibraryEntity
import com.nortlinos.wearos.data.local.LibraryItemEntity
import com.nortlinos.wearos.data.local.ProgressDao
import com.nortlinos.wearos.data.local.ProgressEntity
import com.nortlinos.wearos.data.local.RecentPlaybackItem
import com.nortlinos.wearos.data.local.SessionStore
import com.nortlinos.wearos.data.model.LibraryItem
import com.nortlinos.wearos.data.model.PodcastEpisode
import com.nortlinos.wearos.data.model.Server
import com.nortlinos.wearos.service.AuthorizeOutcome
import com.nortlinos.wearos.service.ConnectivityMonitor
import com.nortlinos.wearos.service.DownloadWorker
import com.nortlinos.wearos.service.OidcAuthorizer
import com.nortlinos.wearos.service.OidcAuthorizers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.nortlinos.wearos.data.local.SettingsStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import java.security.MessageDigest
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Thrown instead of attempting a request when the watch has no active network path at all. Kept
 * distinct from a plain [IOException] so it is unambiguous in logs/tests that no socket was ever
 * opened.
 */
class NoNetworkException : IOException("No network connection")

private class AuthenticationFailureException(code: Int) :
    IOException("Login failed: HTTP $code")

@Singleton
class SessionRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val sessionStore: SessionStore,
    private val authorizers: OidcAuthorizers,
    private val connectivityMonitor: ConnectivityMonitor
) {
    private val _session = MutableStateFlow(sessionStore.get())
    val session = _session.asStateFlow()

    /** Serializes token refreshes, which cannot safely overlap. */
    private val refreshLock = Mutex()
    private val reconnectLock = Mutex()

    suspend fun login(serverUrl: String, username: String, password: String): Result<Server> =
        withContext(Dispatchers.IO) {
            val cleanUrl = serverUrl.trim().trimEnd('/')
            if (cleanUrl.isBlank()) return@withContext Result.failure(IOException("Server URL is required"))
            if (!connectivityMonitor.hasActiveNetwork()) return@withContext Result.failure(NoNetworkException())
            var lastFailure: Throwable = IOException("Unable to connect")
            for (url in candidateUrls(cleanUrl)) {
                try {
                    val response = apiClient.scopedApi(url, null)
                        .login(LoginRequest(username.trim(), password))
                    val user = response.body()?.user
                    if (response.isSuccessful && user != null && user.bearerToken.isNotBlank()) {
                        sessionStore.saveLogin(url, username.trim(), password)
                        return@withContext Result.success(
                            adopt(Server(url, user.bearerToken, user.id, user.username, user.refreshToken))
                        )
                    }
                    // The server answered, so the URL is right; trying the same host over
                    // cleartext would only downgrade the connection without changing the result.
                    return@withContext Result.failure(
                        if (response.code() == 401 || response.code() == 403) {
                            AuthenticationFailureException(response.code())
                        } else {
                            IOException("Login failed: HTTP ${response.code()}")
                        }
                    )
                } catch (error: Exception) {
                    lastFailure = error
                    if (!isWorthRetryingOverCleartext(error)) break
                }
            }
            Result.failure(lastFailure)
        }

    /** What a server advertises on its public `/status` endpoint, plus the URL that answered. */
    data class AuthOptions(
        val serverUrl: String,
        val openIdEnabled: Boolean,
        val openIdButtonText: String
    )

    /**
     * Probes a server for its enabled auth methods so the sign-in screen can offer SSO only where
     * it actually works. Failures are non-fatal: the password form stays usable regardless.
     */
    suspend fun discoverAuthOptions(serverUrl: String): AuthOptions? = withContext(Dispatchers.IO) {
        val cleanUrl = serverUrl.trim().trimEnd('/')
        if (cleanUrl.isBlank() || !connectivityMonitor.hasActiveNetwork()) return@withContext null
        for (url in candidateUrls(cleanUrl)) {
            val response = try {
                apiClient.newAuthSession(url).serverStatus()
            } catch (error: Exception) {
                if (isWorthRetryingOverCleartext(error)) continue else return@withContext null
            }
            // The host answered, so it is the right one; a non-2xx here means this is not an
            // Audiobookshelf server rather than a reason to retry it over cleartext.
            val status = response.takeIf { it.isSuccessful }?.body() ?: return@withContext null
            return@withContext AuthOptions(
                serverUrl = url,
                openIdEnabled = OidcFlow.supportsOpenId(status),
                openIdButtonText = OidcFlow.buttonText(status)
            )
        }
        null
    }

    /**
     * Signs in through the server's identity provider.
     *
     * Transports are tried in order because each can only receive the redirect at its own URI and
     * the server only honours URIs its administrator allow-listed. A `400` from `/auth/openid`
     * means "that redirect URI is not registered", which is a reason to try the next transport
     * rather than to fail the sign-in.
     */
    suspend fun loginWithOpenId(serverUrl: String): Result<Server> = withContext(Dispatchers.IO) {
        val cleanUrl = serverUrl.trim().trimEnd('/')
        if (cleanUrl.isBlank()) return@withContext Result.failure(IOException("Server URL is required"))
        val resolvedUrl = discoverAuthOptions(cleanUrl)?.takeIf { it.openIdEnabled }?.serverUrl
            ?: return@withContext Result.failure(
                IOException("This server does not offer single sign-on")
            )

        var lastFailure = "No sign-in method is available on this watch"
        for (authorizer in authorizers.all()) {
            when (val outcome = attemptOpenId(resolvedUrl, authorizer)) {
                is OpenIdAttempt.Succeeded -> return@withContext Result.success(outcome.server)
                is OpenIdAttempt.TryNext -> lastFailure = outcome.message
                is OpenIdAttempt.Aborted -> return@withContext Result.failure(IOException(outcome.message))
            }
        }
        Result.failure(IOException(lastFailure))
    }

    private sealed interface OpenIdAttempt {
        data class Succeeded(val server: Server) : OpenIdAttempt
        data class TryNext(val message: String) : OpenIdAttempt
        data class Aborted(val message: String) : OpenIdAttempt
    }

    private suspend fun attemptOpenId(serverUrl: String, authorizer: OidcAuthorizer): OpenIdAttempt {
        // Every request in the attempt must go through the same client: the server ties the PKCE
        // exchange to an Express session cookie handed out by /auth/openid.
        val api: AudiobookshelfApi = apiClient.newAuthSession(serverUrl)
        val verifier = OidcFlow.newCodeVerifier()
        val state = OidcFlow.newState()

        val authorizationUrl = try {
            val response = api.openIdAuthorize(
                OidcFlow.authorizeQuery(authorizer.redirectUri, OidcFlow.codeChallenge(verifier), state)
            )
            when {
                response.code() == 400 ->
                    return OpenIdAttempt.TryNext("Server rejected redirect ${authorizer.redirectUri}")
                response.code() !in 300..399 ->
                    return OpenIdAttempt.Aborted("Server could not start sign-in (HTTP ${response.code()})")
                else -> response.raw().header("Location")
                    ?: return OpenIdAttempt.Aborted("Server did not return a sign-in address")
            }
        } catch (error: Exception) {
            return OpenIdAttempt.Aborted(error.message ?: "Could not reach the server")
        }

        val code = when (val outcome = authorizer.authorize(authorizationUrl)) {
            is AuthorizeOutcome.Unavailable -> return OpenIdAttempt.TryNext(outcome.reason)
            is AuthorizeOutcome.Failed -> return OpenIdAttempt.Aborted(outcome.message)
            is AuthorizeOutcome.Success ->
                when (val parsed = OidcFlow.parseRedirect(outcome.redirectUrl, state)) {
                    is OidcFlow.RedirectResult.Failure -> return OpenIdAttempt.Aborted(parsed.message)
                    is OidcFlow.RedirectResult.Success -> parsed.code
                }
        }

        return try {
            val response = api.openIdCallback(state, code, verifier)
            val user = response.body()?.user
            if (response.isSuccessful && user != null && user.bearerToken.isNotBlank()) {
                sessionStore.clearLogin()
                OpenIdAttempt.Succeeded(
                    adopt(Server(serverUrl, user.bearerToken, user.id, user.username, user.refreshToken))
                )
            } else {
                OpenIdAttempt.Aborted("Sign-in could not be completed (HTTP ${response.code()})")
            }
        } catch (error: Exception) {
            OpenIdAttempt.Aborted(error.message ?: "Sign-in could not be completed")
        }
    }

    private fun adopt(server: Server): Server {
        sessionStore.save(server)
        _session.value = server
        lastValidated = server.token to System.currentTimeMillis()
        return server
    }

    enum class ValidationResult { VALID, INVALID, UNREACHABLE }

    @Volatile private var lastValidated: Pair<String, Long>? = null

    suspend fun validateStoredSession(): ValidationResult = withContext(Dispatchers.IO) {
        val server = _session.value ?: return@withContext ValidationResult.INVALID
        if (!connectivityMonitor.hasActiveNetwork()) return@withContext ValidationResult.UNREACHABLE
        runCatching { apiClient.scopedApi(server.url, server.token).getCurrentUser() }.fold(
            onSuccess = {
                when {
                    it.isSuccessful -> {
                        lastValidated = server.token to System.currentTimeMillis()
                        ValidationResult.VALID
                    }
                    it.code() == 401 || it.code() == 403 ->
                        if (recoverFromUnauthorized(server.url, server.token)) {
                            ValidationResult.VALID
                        } else if (_session.value == null) {
                            ValidationResult.INVALID
                        } else {
                            ValidationResult.UNREACHABLE
                        }
                    else -> ValidationResult.UNREACHABLE
                }
            },
            onFailure = { ValidationResult.UNREACHABLE }
        )
    }

    /**
     * Foreground-triggered validation, skipped when this same token was confirmed valid within
     * [SessionValidationThrottle.INTERVAL_MS]. Every app open used to cost a `/api/me` round
     * trip (on top of the progress sync), even when the watch was reopened seconds later.
     */
    suspend fun validateStoredSessionIfStale(): ValidationResult? {
        val token = _session.value?.token ?: return ValidationResult.INVALID
        val last = lastValidated
        val lastAt = if (last?.first == token) last.second else null
        if (!SessionValidationThrottle.shouldValidate(System.currentTimeMillis(), lastAt)) return null
        return validateStoredSession()
    }

    /**
     * Revalidates an existing session or signs back in with the encrypted saved login after a
     * network returns. This lets sessions recover when their access token expired while offline.
     */
    suspend fun reconnectSavedSession(): ValidationResult = reconnectLock.withLock {
        if (!connectivityMonitor.hasActiveNetwork()) return@withLock ValidationResult.UNREACHABLE

        if (_session.value != null) {
            val validation = validateStoredSessionIfStale()
            if (_session.value != null) return@withLock validation ?: ValidationResult.VALID
        }

        val savedLogin = sessionStore.getLogin()
            ?: return@withLock ValidationResult.INVALID
        login(savedLogin.url, savedLogin.username, savedLogin.password).fold(
            onSuccess = { ValidationResult.VALID },
            onFailure = {
                if (it is AuthenticationFailureException) {
                    logout()
                    ValidationResult.INVALID
                } else {
                    ValidationResult.UNREACHABLE
                }
            }
        )
    }

    fun logout() {
        lastValidated = null
        sessionStore.clear()
        _session.value = null
    }

    /**
     * Handles a `401`/`403` for the given session.
     *
     * Audiobookshelf 2.26+ issues access tokens that expire after an hour, so an unauthorized
     * response usually means "refresh", not "signed out" — dropping the session there would log
     * the user out roughly hourly. The stored session is only cleared when no refresh token is
     * held or the server refuses it.
     *
     * @return true when the session was refreshed and the caller may retry.
     */
    suspend fun recoverFromUnauthorized(originServerUrl: String, token: String): Boolean {
        val current = _session.value ?: return false
        if (!ServerIdentity.matches(current.url, originServerUrl) || current.token != token) {
            // Someone else already replaced the session; the caller's token is simply stale.
            return false
        }
        // Refresh tokens are single-use: the server rotates them on every exchange. Two callers
        // racing here would both spend the same token, and the loser's 401 would tear down the
        // session the winner had just renewed, so only one refresh may be in flight at a time.
        return refreshLock.withLock {
            val latest = _session.value ?: return@withLock false
            if (latest.token != token) {
                // Refreshed while this caller waited; it only needs to retry with the new token.
                return@withLock ServerIdentity.matches(latest.url, originServerUrl)
            }
            val refreshToken = latest.refreshToken
            if (refreshToken.isNullOrBlank()) {
                return@withLock reauthenticate(latest)
            }
            val response = runCatching {
                apiClient.newAuthSession(latest.url).refreshSession(refreshToken)
            }.getOrNull()
            val user = response?.body()?.user
            if (response?.isSuccessful == true && user != null && user.bearerToken.isNotBlank()) {
                adopt(
                    latest.copy(
                        token = user.bearerToken,
                        // The server only returns a rotated refresh token to callers that sent the
                        // header; keep the current one if it withheld a replacement.
                        refreshToken = user.refreshToken ?: latest.refreshToken
                    )
                )
                true
            } else {
                if (response != null && response.code() in 400..499) {
                    // A rejected refresh token may still be recoverable with the saved password.
                    reauthenticate(latest)
                } else {
                    false
                }
            }
        }
    }

    private suspend fun reauthenticate(server: Server): Boolean {
        val savedLogin = sessionStore.getLogin()
            ?.takeIf { ServerIdentity.matches(it.url, server.url) }
        if (savedLogin == null) {
            logout()
            return false
        }

        return login(savedLogin.url, savedLogin.username, savedLogin.password).fold(
            onSuccess = { true },
            onFailure = {
                if (it is AuthenticationFailureException) logout()
                false
            }
        )
    }

    fun requireServer(): Server =
        _session.value ?: throw IOException("No Audiobookshelf server is configured")

    private fun candidateUrls(input: String): List<String> =
        if (input.startsWith("http://", true) || input.startsWith("https://", true)) {
            listOf(input)
        } else {
            listOf("https://$input", "http://$input")
        }

    /**
     * Whether an HTTPS failure justifies retrying the same host over cleartext.
     *
     * Self-hosted servers are routinely plain HTTP, so a URL typed without a scheme has to be able
     * to fall back. What must not fall back is a host that answered over TLS and failed to prove
     * its identity, or one whose failure is ambiguous about how far the request got: downgrading
     * either would put credentials and session tokens on the wire in the clear.
     */
    private fun isWorthRetryingOverCleartext(error: Throwable): Boolean = when (error) {
        // The host proved it speaks TLS but could not be trusted. Retrying in the clear would send
        // credentials to exactly the host whose identity could not be verified.
        is SSLPeerUnverifiedException, is SSLHandshakeException -> false
        // The host is not speaking TLS at all, which is the self-hosted HTTP case this exists for.
        is SSLException -> true
        // Never reached, so nothing was sent and nothing can have leaked.
        is ConnectException, is UnknownHostException, is NoRouteToHostException -> true
        // Anything else, a read timeout above all, is ambiguous: the request may already have been
        // sent over TLS, so the safe answer is to report the failure rather than downgrade.
        else -> false
    }

}

@Singleton
class LibraryRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val database: AppDatabase,
    private val libraryDao: LibraryDao,
    private val sessionRepository: SessionRepository,
    private val connectivityMonitor: ConnectivityMonitor,
    private val settingsStore: SettingsStore
) {
    private val precacheScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var precacheJob: Job? = null

    data class Page(
        val items: List<LibraryItemEntity>,
        val page: Int,
        val total: Int,
        val hasMore: Boolean
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun libraries(): Flow<List<LibraryEntity>> = sessionRepository.session.flatMapLatest { server ->
        if (server == null) flowOf(emptyList())
        else libraryDao.observeLibraries(ServerIdentity.normalize(server.url))
    }
    fun items(libraryId: String, originServerUrl: String): Flow<List<LibraryItemEntity>> =
        libraryDao.observeItems(libraryId, ServerIdentity.normalize(originServerUrl))
    fun podcastEpisodes(podcastId: String, originServerUrl: String): Flow<List<LibraryItemEntity>> =
        libraryDao.observePodcastEpisodes(podcastId, ServerIdentity.normalize(originServerUrl))
    fun item(itemId: String, originServerUrl: String): Flow<LibraryItemEntity?> =
        libraryDao.observeItem(itemId, ServerIdentity.normalize(originServerUrl))
    fun chapters(itemId: String, originServerUrl: String): Flow<List<ChapterEntity>> =
        libraryDao.observeChapters(itemId, ServerIdentity.normalize(originServerUrl))
    fun searchLocal(query: String, originServerUrl: String): Flow<List<LibraryItemEntity>> =
        libraryDao.search(query.trim(), ServerIdentity.normalize(originServerUrl))
    suspend fun getItem(itemId: String, originServerUrl: String): LibraryItemEntity? =
        libraryDao.getItem(itemId, ServerIdentity.normalize(originServerUrl))

    suspend fun refreshLibraries(): Result<Unit> = networkResult {
        val server = sessionRepository.requireServer()
        val origin = ServerIdentity.normalize(server.url)
        val response = apiClient.scopedApi(server.url, server.token).getLibraries()
        if (!response.isSuccessful) throw IOException("Libraries: HTTP ${response.code()}")
        libraryDao.upsertLibraries(response.body().requireBody().libraries.map {
            LibraryEntity(it.id, origin, it.name, it.mediaType)
        })
        precacheIfEnabled()
    }

    /**
     * Background warm-up of every library's first pages so lists are already in Room while the
     * user navigates. No-op unless the setting is enabled; a run in progress is not restarted.
     */
    @Synchronized
    fun precacheIfEnabled() {
        if (precacheJob?.isActive == true) return
        precacheJob = precacheScope.launch {
            if (!settingsStore.precacheLibraries.first()) return@launch
            val server = sessionRepository.session.value ?: return@launch
            val libraries = libraryDao.getLibraries(ServerIdentity.normalize(server.url))
            for (library in libraries) {
                for (page in 0 until PRECACHE_MAX_PAGES) {
                    val result = loadPage(
                        library.id, page, PAGE_SIZE, expectedOriginServerUrl = server.url
                    ).getOrNull() ?: return@launch
                    if (!result.hasMore) break
                }
            }
        }
    }

    suspend fun loadPage(
        libraryId: String,
        page: Int,
        limit: Int,
        expectedOriginServerUrl: String? = null
    ): Result<Page> = withContext(Dispatchers.IO) {
        if (!connectivityMonitor.hasActiveNetwork()) return@withContext Result.failure(NoNetworkException())
        runCatching {
            val server = sessionRepository.requireServer()
            if (expectedOriginServerUrl != null &&
                !ServerIdentity.matches(server.url, expectedOriginServerUrl)
            ) {
                throw IOException("The active server changed while loading this library.")
            }
            val origin = ServerIdentity.normalize(server.url)
            val response = apiClient.scopedApi(server.url, server.token)
                .getLibraryItems(libraryId, limit = limit, page = page)
            if (!response.isSuccessful) throw IOException("Library: HTTP ${response.code()}")
            val payload = response.body().requireBody()
            val cached = cacheItems(payload.results, origin)
            Page(
                items = cached,
                page = payload.page,
                total = payload.total,
                hasMore = LibraryPageMath.hasMore(
                    resultCount = cached.size,
                    limit = limit
                )
            )
        }
    }

    suspend fun cachedPage(
        libraryId: String,
        originServerUrl: String,
        page: Int,
        limit: Int
    ): List<LibraryItemEntity> = withContext(Dispatchers.IO) {
        libraryDao.getItemsPage(
            libraryId,
            ServerIdentity.normalize(originServerUrl),
            limit,
            page * limit
        )
    }

    suspend fun cachedItems(
        libraryId: String,
        originServerUrl: String
    ): List<LibraryItemEntity> = withContext(Dispatchers.IO) {
        libraryDao.getAllItems(libraryId, ServerIdentity.normalize(originServerUrl))
    }

    /**
     * Loads every page for the opt-in series view. The ordinary Books view remains paged; callers
     * only pay this cost when they ask to browse series. A failure is surfaced rather than
     * presenting a partial series as complete.
     */
    suspend fun loadAllItems(
        libraryId: String,
        originServerUrl: String,
        onPageLoaded: (loadedCount: Int, totalCount: Int) -> Unit = { _, _ -> }
    ): Result<List<LibraryItemEntity>> =
        withContext(Dispatchers.IO) {
            if (!connectivityMonitor.hasActiveNetwork()) {
                return@withContext Result.failure(NoNetworkException())
            }
            val result = runCatching {
                val items = mutableListOf<LibraryItemEntity>()
                var page = 0
                var hasMore: Boolean
                do {
                    val result = loadPage(
                        libraryId = libraryId,
                        page = page,
                        limit = PAGE_SIZE,
                        expectedOriginServerUrl = originServerUrl
                    ).getOrThrow()
                    items += result.items
                    onPageLoaded(items.size, result.total)
                    hasMore = result.hasMore
                    page++
                } while (hasMore)
                items.distinctBy { it.id }
            }
            result.exceptionOrNull()?.let { if (it is CancellationException) throw it }
            result
        }

    suspend fun searchCached(
        libraryId: String,
        originServerUrl: String,
        query: String,
        limit: Int
    ): List<LibraryItemEntity> = withContext(Dispatchers.IO) {
        libraryDao.searchLibrary(
            libraryId,
            ServerIdentity.normalize(originServerUrl),
            query.trim(),
            limit
        )
    }

    suspend fun searchServer(
        libraryId: String,
        query: String,
        limit: Int
    ): Result<List<LibraryItemEntity>> = withContext(Dispatchers.IO) {
        if (!connectivityMonitor.hasActiveNetwork()) return@withContext Result.failure(NoNetworkException())
        runCatching {
            val server = sessionRepository.requireServer()
            val origin = ServerIdentity.normalize(server.url)
            val response = apiClient.scopedApi(server.url, server.token)
                .searchLibrary(libraryId, query.trim(), limit)
            if (!response.isSuccessful) throw IOException("Search: HTTP ${response.code()}")
            cacheItems(response.body().requireBody().book.map { it.libraryItem }, origin)
        }
    }

    suspend fun refreshLibrary(libraryId: String): Result<Unit> = networkResult {
        val server = sessionRepository.requireServer()
        val origin = ServerIdentity.normalize(server.url)
        val response = apiClient.scopedApi(server.url, server.token)
            .getLibraryItems(libraryId, limit = PAGE_SIZE, page = 0)
        if (!response.isSuccessful) throw IOException("Library: HTTP ${response.code()}")
        cacheItems(response.body().requireBody().results, origin)
    }

    suspend fun refreshItem(itemId: String): Result<Unit> = networkResult {
        val server = sessionRepository.requireServer()
        val origin = ServerIdentity.normalize(server.url)
        if (libraryDao.getItem(itemId, origin)?.parentItemId != null) return@networkResult
        val response = apiClient.scopedApi(server.url, server.token).getLibraryItem(itemId)
        if (!response.isSuccessful) throw IOException("Book: HTTP ${response.code()}")
        cacheItems(listOfNotNull(response.body()), origin)
    }

    suspend fun searchRemote(libraryId: String, query: String): Result<Unit> = networkResult {
        val server = sessionRepository.requireServer()
        val origin = ServerIdentity.normalize(server.url)
        val response = apiClient.scopedApi(server.url, server.token)
            .searchLibrary(libraryId, query)
        if (!response.isSuccessful) throw IOException("Search: HTTP ${response.code()}")
        cacheItems(response.body()?.book.orEmpty().map { it.libraryItem }, origin)
    }

    private suspend fun cacheItems(
        items: List<LibraryItem>,
        originServerUrl: String
    ): List<LibraryItemEntity> = database.withTransaction {
        // One transaction and one batched read per page, instead of a read plus a chapter
        // rewrite per item each committing (and fsyncing) on its own.
        val allIds = items.flatMap { item ->
            listOf(item.id) + item.media.episodeList.map(PodcastEpisode::id)
        }.distinct()
        val cachedById = allIds
            .chunked(SQLITE_VARIABLE_CHUNK)
            .flatMap { libraryDao.getItems(it, originServerUrl) }
            .associateBy { it.id }
        val entities = items.map { item ->
            val cached = cachedById[item.id]
            val incoming = item.toEntity(originServerUrl)
            incoming.copy(
                author = incoming.author ?: cached?.author,
                series = incoming.series ?: cached?.series,
                narrator = incoming.narrator ?: cached?.narrator,
                description = incoming.description ?: cached?.description,
                coverPath = incoming.coverPath ?: cached?.coverPath,
                localCoverPath = cached?.localCoverPath,
                durationMs = incoming.durationMs.takeIf { it > 0 } ?: cached?.durationMs ?: 0
            )
        }
        libraryDao.upsertItems(entities)
        val episodeEntities = items.flatMap { podcast ->
            if (podcast.mediaType != "podcast") return@flatMap emptyList()
            val podcastEntity = entities.firstOrNull { it.id == podcast.id } ?: return@flatMap emptyList()
            podcast.media.episodeList.distinctBy { it.id }.map { episode ->
                val cachedEpisode = cachedById[episode.id]
                LibraryItemEntity(
                    id = episode.id,
                    originServerUrl = originServerUrl,
                    libraryId = podcast.libraryId,
                    mediaType = "podcastEpisode",
                    title = episode.title?.takeIf(String::isNotBlank)
                        ?: cachedEpisode?.title
                        ?: "Untitled episode",
                    author = podcastEntity.author ?: cachedEpisode?.author,
                    series = podcastEntity.title,
                    narrator = null,
                    description = episode.description ?: episode.subtitle ?: cachedEpisode?.description,
                    coverPath = podcastEntity.coverPath ?: cachedEpisode?.coverPath,
                    localCoverPath = cachedEpisode?.localCoverPath,
                    durationMs = ((episode.audioFile?.duration ?: 0.0) * 1000).toLong()
                        .takeIf { it > 0 }
                        ?: cachedEpisode?.durationMs
                        ?: 0,
                    updatedAt = episode.publishedAt ?: cachedEpisode?.updatedAt ?: podcast.updatedAt,
                    parentItemId = podcast.id,
                    remoteSizeBytes = episode.audioFile?.metadata?.size
                        ?.takeIf { it > 0 }
                        ?: episode.enclosure?.sizeBytes
                        ?: cachedEpisode?.remoteSizeBytes
                        ?: 0
                )
            }
        }
        libraryDao.upsertItems(episodeEntities)
        items.forEach { item ->
            val chapters = item.media.chapterList
            if (chapters.isNotEmpty()) {
                libraryDao.deleteChapters(item.id, originServerUrl)
                libraryDao.upsertChapters(
                    chapters.mapIndexed { index, chapter ->
                        ChapterEntity(
                            itemId = item.id,
                            originServerUrl = originServerUrl,
                            chapterId = chapter.id.takeUnless { it == 0 } ?: index,
                            title = chapter.title,
                            startMs = (chapter.start * 1000).toLong(),
                            endMs = (chapter.end * 1000).toLong()
                        )
                    }
                )
            }
        }
        entities
    }

    private fun LibraryItem.toEntity(originServerUrl: String): LibraryItemEntity {
        return LibraryItemEntity(
            id = id,
            originServerUrl = originServerUrl,
            libraryId = libraryId,
            mediaType = mediaType,
            title = media.metadata.title?.takeIf { it.isNotBlank() } ?: "Untitled",
            author = media.metadata.displayAuthor,
            series = media.metadata.displaySeries,
            narrator = media.metadata.displayNarrator,
            description = media.metadata.description,
            coverPath = media.coverPath,
            localCoverPath = null,
            durationMs = ((media.duration ?: 0.0) * 1000).toLong(),
            updatedAt = updatedAt
        )
    }

    private suspend fun networkResult(block: suspend () -> Unit): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!connectivityMonitor.hasActiveNetwork()) return@withContext Result.failure(NoNetworkException())
            runCatching { block() }
        }

    companion object {
        const val PAGE_SIZE = 40
        const val SEARCH_LIMIT = 50
        private const val PRECACHE_MAX_PAGES = 5
        private const val SQLITE_VARIABLE_CHUNK = 500
    }
}

@Singleton
class DownloadRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val downloadDao: DownloadDao,
    private val libraryDao: LibraryDao,
    private val sessionRepository: SessionRepository
) {
    private val externalFilesRoot = context.getExternalFilesDir(null)
    private val root = File(externalFilesRoot ?: context.filesDir, "audiobooks")
    private val workManager = WorkManager.getInstance(context)

    data class StorageStats(
        val booksBytes: Long,
        val totalBytes: Long,
        val availableBytes: Long,
        val error: String? = null
    )

    /**
     * The query joins `library_items`, so every library page refresh or metadata write re-emits
     * an identical list. Each emission costs one track query plus a file stat per track for every
     * downloaded book, so unchanged lists are dropped before that work. Any real change (a book
     * finishing, being removed, or its row being updated) still re-verifies the files.
     */
    fun downloadedBooks(): Flow<List<DownloadedBookData>> =
        downloadDao.observeDownloadedItems().distinctUntilChanged().map { items ->
            withContext(Dispatchers.IO) {
                items.mapNotNull { item -> readyBook(item) }
            }
        }
    fun downloads(): Flow<List<DownloadedItemEntity>> = downloadDao.observeAll()
    fun download(itemId: String, originServerUrl: String): Flow<DownloadedItemEntity?> =
        downloadDao.observeDownload(itemId, ServerIdentity.normalize(originServerUrl))
    /**
     * Aggregate storage is only rendered in Settings, and this flow only runs while that screen is
     * subscribed. The walk is recursive over every stored book, so identical re-emissions from
     * Room's table invalidation are dropped; any real row change still recomputes, keeping the
     * used/free figures accurate while a download progresses.
     */
    fun storageStats(): Flow<StorageStats> = downloadDao.observeAll()
        .distinctUntilChanged()
        .map {
            withContext(Dispatchers.IO) {
                runCatching { readStorageStats() }.getOrElse { error ->
                    StorageStats(0, 0, 0, error.message ?: "Audiobook storage is unavailable")
                }
            }
        }
    suspend fun getBook(itemId: String, originServerUrl: String): DownloadedBookData? =
        libraryDao.getItem(itemId, ServerIdentity.normalize(originServerUrl))
            ?.let { readyBook(it) }

    fun bookDirectory(originServerUrl: String, itemId: String): File =
        File(File(root, storageKey(originServerUrl)), storageKey(itemId))

    fun availableBytes(): Long {
        ensureStorageAvailable()
        return StatFs(root.absolutePath).availableBytes
    }

    suspend fun start(itemId: String, requestedOriginServerUrl: String? = null) {
        val server = sessionRepository.requireServer()
        val originServerUrl = ServerIdentity.normalize(requestedOriginServerUrl ?: server.url)
        if (!ServerIdentity.matches(server.url, originServerUrl)) {
            throw IOException("Sign in to the server this book belongs to.")
        }
        val existing = downloadDao.getDownload(itemId, originServerUrl)
        if (existing?.status == DownloadStatus.DOWNLOADED) return
        val directory = existing?.localDirectory?.let(::File)
            ?: bookDirectory(originServerUrl, itemId)
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Unable to create audiobook storage")
        }
        downloadDao.upsertDownload(
            DownloadedItemEntity(
                itemId = itemId,
                originServerUrl = originServerUrl,
                status = DownloadStatus.QUEUED,
                localDirectory = directory.absolutePath,
                fileSizeBytes = existing?.fileSizeBytes ?: 0,
                downloadedBytes = directory.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            )
        )
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                workDataOf(
                    DownloadWorker.KEY_ITEM_ID to itemId,
                    DownloadWorker.KEY_ORIGIN_SERVER_URL to originServerUrl
                )
            )
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            // A book that finds the single transfer slot taken ends its run and is retried, so
            // the backoff is the queue's polling interval: linear keeps a long series moving
            // instead of pushing later books into exponentially longer waits.
            .setBackoffCriteria(BackoffPolicy.LINEAR, QUEUE_RETRY_SECONDS, TimeUnit.SECONDS)
            .addTag(workerName(itemId, originServerUrl))
            .build()
        workManager.enqueueUniqueWork(
            workerName(itemId, originServerUrl),
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    suspend fun pause(itemId: String, originServerUrl: String) {
        val origin = ServerIdentity.normalize(originServerUrl)
        withContext(Dispatchers.IO) {
            workManager.cancelUniqueWork(workerName(itemId, origin)).result.get()
            downloadDao.getDownload(itemId, origin)?.let {
                downloadDao.upsertDownload(it.copy(status = DownloadStatus.PAUSED))
            }
        }
    }

    suspend fun cancel(itemId: String, originServerUrl: String) {
        val origin = ServerIdentity.normalize(originServerUrl)
        withContext(Dispatchers.IO) {
            workManager.cancelUniqueWork(workerName(itemId, origin)).result.get()
            delete(itemId, origin)
        }
    }

    suspend fun delete(itemId: String, originServerUrl: String) = withContext(Dispatchers.IO) {
        val origin = ServerIdentity.normalize(originServerUrl)
        val directory = downloadDao.getDownload(itemId, origin)?.localDirectory?.let(::File)
        if (directory?.exists() == true && !directory.deleteRecursively()) {
            throw IOException("Unable to remove all audiobook files")
        }
        libraryDao.setLocalCoverPath(itemId, origin, null)
        downloadDao.deleteTracks(itemId, origin)
        downloadDao.deleteDownload(itemId, origin)
    }

    companion object {
        /** WorkManager clamps retry backoff to ten seconds, so this is the shortest useful step. */
        private const val QUEUE_RETRY_SECONDS = 15L
    }

    private fun workerName(itemId: String, originServerUrl: String) =
        "download-${storageKey("$originServerUrl\u0000$itemId")}"

    private fun readStorageStats(): StorageStats {
        ensureStorageAvailable()
        val fileSystem = StatFs(root.absolutePath)
        val booksBytes = root.walkTopDown()
            .filter(File::isFile)
            .sumOf(File::length)
        return StorageStats(
            booksBytes = booksBytes,
            totalBytes = fileSystem.totalBytes,
            availableBytes = fileSystem.availableBytes
        )
    }

    private fun ensureStorageAvailable() {
        if (externalFilesRoot != null &&
            Environment.getExternalStorageState(externalFilesRoot) != Environment.MEDIA_MOUNTED
        ) {
            throw IOException("Audiobook storage is unavailable")
        }
        if (!root.exists() && !root.mkdirs()) {
            throw IOException("Audiobook storage is unavailable")
        }
    }

    private suspend fun assembleBook(item: LibraryItemEntity): DownloadedBookData {
        val origin = item.originServerUrl
        return DownloadedBookData(
            item = item,
            download = downloadDao.getDownload(item.id, origin),
            tracks = downloadDao.getTracks(item.id, origin),
            chapters = libraryDao.getChapters(item.id, origin)
        )
    }

    private suspend fun readyBook(item: LibraryItemEntity): DownloadedBookData? {
        val book = assembleBook(item)
        val ready = book.download?.status == DownloadStatus.DOWNLOADED &&
            book.tracks.isNotEmpty() &&
            book.tracks.all { File(it.localPath).isFile }
        if (!ready && book.download?.status == DownloadStatus.DOWNLOADED) {
            downloadDao.upsertDownload(
                book.download.copy(
                    status = DownloadStatus.FAILED,
                    error = "Stored audio files are unavailable"
                )
            )
        }
        return book.takeIf { ready }
    }

    internal fun storageKey(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}

@Singleton
class ProgressRepository @Inject constructor(
    private val progressDao: ProgressDao
) {
    fun observe(itemId: String, originServerUrl: String): Flow<ProgressEntity?> =
        progressDao.observe(itemId, ServerIdentity.normalize(originServerUrl))
    fun conflicts(): Flow<List<ProgressEntity>> = progressDao.observeConflicts()
    fun recentProgressForServer(
        originServerUrl: String,
        limit: Int = 10
    ): Flow<List<ProgressEntity>> =
        progressDao.observeRecentProgressForServer(
            ServerIdentity.normalize(originServerUrl),
            limit
        )
    fun recentForServer(originServerUrl: String, limit: Int = 10): Flow<List<RecentPlaybackItem>> =
        progressDao.observeRecentForServer(ServerIdentity.normalize(originServerUrl), limit)
    fun recentDownloaded(limit: Int = 10): Flow<List<RecentPlaybackItem>> =
        progressDao.observeRecentDownloaded(limit)
    suspend fun get(itemId: String, originServerUrl: String): ProgressEntity? =
        progressDao.get(itemId, ServerIdentity.normalize(originServerUrl))

    suspend fun record(
        itemId: String,
        originServerUrl: String,
        positionMs: Long,
        durationMs: Long,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val server = ServerIdentity.normalize(originServerUrl)
        val existing = progressDao.get(itemId, server)
        progressDao.upsert(
            ProgressEntity(
                itemId = itemId,
                originServerUrl = server,
                positionMs = positionMs.coerceAtLeast(0),
                durationMs = durationMs.coerceAtLeast(positionMs),
                updatedAt = timestamp,
                dirty = true,
                // The upsert replaces the whole row, so carry any unresolved conflict forward.
                // Clearing it here meant a conflict raised by sync was erased by the next
                // progress tick, leaving the user no chance to choose and letting the stale
                // local position push over newer progress from another device.
                conflictPositionMs = existing?.conflictPositionMs,
                conflictUpdatedAt = existing?.conflictUpdatedAt
            )
        )
    }

    suspend fun keepLocal(itemId: String, originServerUrl: String) {
        progressDao.get(itemId, ServerIdentity.normalize(originServerUrl))?.let {
            progressDao.upsert(
                it.copy(
                    updatedAt = System.currentTimeMillis(),
                    dirty = true,
                    conflictPositionMs = null,
                    conflictUpdatedAt = null
                )
            )
        }
    }

    suspend fun keepServer(itemId: String, originServerUrl: String) {
        progressDao.get(itemId, ServerIdentity.normalize(originServerUrl))?.let {
            val serverPosition = it.conflictPositionMs ?: return
            progressDao.upsert(
                it.copy(
                    positionMs = serverPosition,
                    updatedAt = it.conflictUpdatedAt ?: System.currentTimeMillis(),
                    dirty = false,
                    conflictPositionMs = null,
                    conflictUpdatedAt = null
                )
            )
        }
    }
}

/**
 * Audio bookmarks (a named timestamp within a book) are server-only: unlike progress they have
 * no offline fallback, so every call here requires connectivity and nothing is cached locally.
 */
@Singleton
class BookmarkRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val sessionRepository: SessionRepository,
    private val connectivityMonitor: ConnectivityMonitor
) {
    suspend fun list(itemId: String): Result<List<AudioBookmarkDto>> = withContext(Dispatchers.IO) {
        if (!connectivityMonitor.hasActiveNetwork()) return@withContext Result.failure(NoNetworkException())
        runCatching {
            val server = sessionRepository.requireServer()
            val response = apiClient.scopedApi(server.url, server.token).getCurrentUser()
            if (!response.isSuccessful) throw IOException("Bookmarks: HTTP ${response.code()}")
            response.body().requireBody().bookmarks
                .filter { it.libraryItemId == itemId }
                .sortedBy { it.time }
        }
    }

    suspend fun create(itemId: String, timeSeconds: Int, title: String): Result<AudioBookmarkDto> =
        withContext(Dispatchers.IO) {
            if (!connectivityMonitor.hasActiveNetwork()) return@withContext Result.failure(NoNetworkException())
            runCatching {
                val server = sessionRepository.requireServer()
                val response = apiClient.scopedApi(server.url, server.token)
                    .createBookmark(itemId, BookmarkRequest(timeSeconds, title))
                if (!response.isSuccessful) throw IOException("Bookmark: HTTP ${response.code()}")
                response.body().requireBody()
            }
        }

    suspend fun delete(itemId: String, timeSeconds: Int): Result<Unit> = withContext(Dispatchers.IO) {
        if (!connectivityMonitor.hasActiveNetwork()) return@withContext Result.failure(NoNetworkException())
        runCatching {
            val server = sessionRepository.requireServer()
            val response = apiClient.scopedApi(server.url, server.token)
                .deleteBookmark(itemId, timeSeconds)
            // The bookmark may already be gone (e.g. deleted from another device); that is not a
            // failure worth surfacing since the end state the caller wants is already true.
            if (!response.isSuccessful && response.code() != 404) {
                throw IOException("Bookmark: HTTP ${response.code()}")
            }
        }
    }
}

@Singleton
class ProgressSyncEngine @Inject constructor(
    private val apiClient: ApiClient,
    private val progressDao: ProgressDao,
    private val libraryDao: LibraryDao,
    private val sessionRepository: SessionRepository
) {
    data class Result(val pushed: Int, val pulled: Int, val conflicts: Int)

    suspend fun sync(): Result = withContext(Dispatchers.IO) {
        val activeServer = sessionRepository.requireServer()
        val activeServerUrl = ServerIdentity.normalize(activeServer.url)
        val scopedApi = apiClient.scopedApi(activeServer.url, activeServer.token)
        var pushed = 0
        var pulled = 0
        var conflicts = 0
        val localEntries = progressDao.allForServer(activeServerUrl)
        val currentUserResponse = scopedApi.getCurrentUser()
        if (!currentUserResponse.isSuccessful) {
            if (currentUserResponse.code() == 401 || currentUserResponse.code() == 403) {
                sessionRepository.recoverFromUnauthorized(activeServerUrl, activeServer.token)
            }
            throw HttpException(currentUserResponse)
        }
        val remoteEntries = currentUserResponse.body().requireBody()
            .mediaProgress
            .associateBy { it.libraryItemId to it.episodeId }

        for (local in localEntries) {
            val localItem = libraryDao.getItem(local.itemId, activeServerUrl)
            val podcastId = localItem?.parentItemId
            // `/api/me` already carries podcast-episode progress (keyed by podcast + episode), so
            // no per-episode request is needed: one GET covers every local row.
            val remote = remoteEntries[RemoteProgressKey.of(local.itemId, podcastId)]
            val remoteUpdatedAt = remote?.effectiveUpdatedAt ?: 0L
            val remotePositionMs = ((remote?.currentTime ?: 0.0) * 1000).toLong()

            when (ProgressConflictResolver.resolve(local, remoteUpdatedAt, remote != null)) {
                ProgressSyncAction.CONFLICT -> {
                    progressDao.upsert(
                        local.copy(
                            conflictPositionMs = remotePositionMs,
                            conflictUpdatedAt = remoteUpdatedAt
                        )
                    )
                    conflicts++
                }
                ProgressSyncAction.PUSH -> {
                    val update = ProgressUpdateRequest(
                        currentTime = local.positionMs / 1000.0,
                        duration = local.durationMs / 1000.0
                    )
                    val response = podcastId?.let {
                        scopedApi.updatePodcastEpisodeProgress(it, local.itemId, update)
                    } ?: scopedApi.updateProgress(local.itemId, update)
                    if (!response.isSuccessful) {
                        if (response.code() == 401 || response.code() == 403) {
                            sessionRepository.recoverFromUnauthorized(activeServerUrl, activeServer.token)
                        }
                        throw HttpException(response)
                    }
                    val sessionStillMatches = sessionRepository.session.value?.let {
                        ServerIdentity.matches(it.url, activeServerUrl) && it.token == activeServer.token
                    } == true
                    if (sessionStillMatches &&
                        progressDao.markCleanIfUnchanged(
                            local.itemId,
                            activeServerUrl,
                            local.updatedAt
                        ) == 1
                    ) pushed++
                }
                ProgressSyncAction.PULL -> {
                    checkNotNull(remote)
                    progressDao.upsert(
                        local.copy(
                            positionMs = remotePositionMs,
                            durationMs = (remote.duration * 1000).toLong(),
                            updatedAt = remoteUpdatedAt,
                            dirty = false
                        )
                    )
                    pulled++
                }
                ProgressSyncAction.NONE -> Unit
            }
        }
        // Remote-only rows (books first played elsewhere) land in one batched write. A first
        // sync against a long listening history otherwise commits one transaction per book.
        val localIds = localEntries.mapTo(HashSet()) { it.itemId }
        val remoteOnly = remoteEntries.values.mapNotNull { remote ->
            val itemId = remote.libraryItemId ?: return@mapNotNull null
            val progressItemId = remote.episodeId ?: itemId
            if (progressItemId in localIds) return@mapNotNull null
            if (remote.episodeId != null &&
                libraryDao.getItem(progressItemId, activeServerUrl) == null
            ) return@mapNotNull null
            ProgressEntity(
                itemId = progressItemId,
                originServerUrl = activeServerUrl,
                positionMs = (remote.currentTime * 1000).toLong(),
                durationMs = (remote.duration * 1000).toLong(),
                updatedAt = remote.effectiveUpdatedAt,
                dirty = false
            )
        }
        if (remoteOnly.isNotEmpty()) {
            progressDao.upsertAll(remoteOnly)
            pulled += remoteOnly.size
        }
        Result(pushed, pulled, conflicts)
    }
}

/**
 * Key into `/api/me`'s `mediaProgress`, which Audiobookshelf indexes by `(libraryItemId,
 * episodeId)`. A book is `(itemId, null)`; a podcast episode is stored locally under its own id
 * with the podcast as parent, and appears remotely as `(podcastId, episodeId)`.
 */
internal object RemoteProgressKey {
    fun of(localItemId: String, podcastId: String?): Pair<String, String?> =
        if (podcastId == null) localItemId to null else podcastId to localItemId
}

internal object LibraryPageMath {
    fun hasMore(resultCount: Int, limit: Int): Boolean =
        limit > 0 && resultCount >= limit
}

enum class ProgressSyncAction { PUSH, PULL, CONFLICT, NONE }

object ProgressConflictResolver {
    fun resolve(
        local: ProgressEntity,
        remoteUpdatedAt: Long,
        remoteExists: Boolean
    ): ProgressSyncAction = when {
        // An unresolved conflict holds the push until the user picks a side. Routine playback
        // ticks keep bumping local.updatedAt past the remote timestamp, so without this the next
        // sync pushed over the server's newer position, which is exactly what the conflict exists
        // to prevent. CONFLICT also refreshes the stored remote position to the latest one.
        local.dirty && remoteExists && local.conflictPositionMs != null -> ProgressSyncAction.CONFLICT
        local.dirty && remoteExists && remoteUpdatedAt > local.updatedAt -> ProgressSyncAction.CONFLICT
        local.dirty -> ProgressSyncAction.PUSH
        remoteExists && remoteUpdatedAt > local.updatedAt -> ProgressSyncAction.PULL
        else -> ProgressSyncAction.NONE
    }

}

object ServerIdentity {
    fun normalize(serverUrl: String): String = serverUrl.trim().trimEnd('/')
    fun matches(left: String, right: String): Boolean =
        normalize(left) == normalize(right)
}

private fun <T> T?.requireBody(): T = this ?: throw IOException("Server returned an empty response")

internal object SessionValidationThrottle {
    const val INTERVAL_MS = 15 * 60 * 1000L

    fun shouldValidate(nowMs: Long, lastValidatedAtMs: Long?): Boolean =
        lastValidatedAtMs == null || nowMs - lastValidatedAtMs !in 0 until INTERVAL_MS
}
