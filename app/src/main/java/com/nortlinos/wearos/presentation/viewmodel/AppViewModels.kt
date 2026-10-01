package com.nortlinos.wearos.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.nortlinos.wearos.data.api.ApiClient
import com.nortlinos.wearos.data.local.ChapterEntity
import com.nortlinos.wearos.data.local.DownloadStatus
import com.nortlinos.wearos.data.local.DownloadedItemEntity
import com.nortlinos.wearos.data.local.DownloadedBookData
import com.nortlinos.wearos.data.local.LibraryEntity
import com.nortlinos.wearos.data.local.LibraryItemEntity
import com.nortlinos.wearos.data.local.ProgressDisplayMode
import com.nortlinos.wearos.data.local.SettingsStore
import com.nortlinos.wearos.data.api.AudioBookmarkDto
import com.nortlinos.wearos.data.repository.BookmarkRepository
import com.nortlinos.wearos.data.repository.DownloadRepository
import com.nortlinos.wearos.data.repository.LibraryRepository
import com.nortlinos.wearos.data.repository.ProgressRepository
import com.nortlinos.wearos.data.repository.SessionRepository
import com.nortlinos.wearos.data.repository.ServerIdentity
import com.nortlinos.wearos.service.AudioOutputPolicy
import com.nortlinos.wearos.service.AudioOutputs
import com.nortlinos.wearos.service.PlaybackService
import com.nortlinos.wearos.service.ConnectivityMonitor
import com.nortlinos.wearos.service.SleepTimerManager
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class LoginUiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    /** True once the entered server has confirmed it accepts single sign-on. */
    val openIdAvailable: Boolean = false,
    /** Server-supplied label for the single sign-on button. */
    val openIdLabel: String = "Sign in with SSO",
    /** Set while the user is completing sign-in on their phone or in a browser. */
    val openIdInProgress: Boolean = false
)

data class LibraryBrowseState(
    val libraryId: String = "",
    val originServerUrl: String = "",
    val items: List<LibraryItemEntity> = emptyList(),
    val query: String = "",
    val page: Int = -1,
    val total: Int = 0,
    val hasMore: Boolean = true,
    val loading: Boolean = false,
    val error: String? = null
)

data class LibrarySeriesState(
    val libraryId: String = "",
    val originServerUrl: String = "",
    val series: List<LibrarySeries> = emptyList(),
    val loadedItemCount: Int = 0,
    val totalItemCount: Int = 0,
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val error: String? = null
)

data class SeriesDownloadUiState(
    val libraryId: String? = null,
    val originServerUrl: String? = null,
    val seriesName: String? = null,
    val total: Int = 0,
    val queued: Int = 0,
    val skipped: Int = 0,
    val failed: Int = 0,
    val inProgress: Boolean = false,
    val error: String? = null
)

data class PodcastSeriesDownloadUiState(
    val podcastId: String? = null,
    val originServerUrl: String? = null,
    val seriesName: String? = null,
    val total: Int = 0,
    val queued: Int = 0,
    val deleted: Int = 0,
    val skipped: Int = 0,
    val failed: Int = 0,
    val requiredBytes: Long = 0,
    val availableBytes: Long = 0,
    val unknownSizeCount: Int = 0,
    val inProgress: Boolean = false,
    val deleting: Boolean = false,
    val requiresSpaceConfirmation: Boolean = false,
    val pendingItemIds: List<String> = emptyList(),
    val error: String? = null
)

internal data class PodcastSeriesDownloadPlan(
    val itemIdsToQueue: List<String>,
    val skippedCount: Int,
    val requiredBytes: Long,
    val unknownSizeCount: Int
)

internal object PodcastSeriesDownloadPlanner {
    fun create(
        itemIds: List<String>,
        sizes: Map<String, Long>,
        downloads: Map<String, DownloadedItemEntity>
    ): PodcastSeriesDownloadPlan {
        val selection = LibrarySeriesDownloadSelection.create(
            itemIds,
            downloads.mapValues { it.value.status }
        )
        var required = 0L
        var unknownCount = 0
        selection.itemIdsToQueue.forEach { id ->
            val size = sizes[id]?.takeIf { it > 0 }
            if (size == null) {
                unknownCount++
            } else {
                val alreadyDownloaded = downloads[id]?.downloadedBytes?.coerceAtLeast(0) ?: 0
                required += (size - alreadyDownloaded).coerceAtLeast(0)
            }
        }
        return PodcastSeriesDownloadPlan(
            itemIdsToQueue = selection.itemIdsToQueue,
            skippedCount = selection.skippedCount,
            requiredBytes = required,
            unknownSizeCount = unknownCount
        )
    }
}

data class RecentPlaybackUiState(
    val books: List<com.nortlinos.wearos.data.local.RecentPlaybackItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()

    private var probeJob: Job? = null

    fun serverUrl(value: String) {
        _state.value = _state.value.copy(
            serverUrl = value,
            error = null,
            openIdAvailable = false
        )
        probeAuthOptions(value)
    }

    /**
     * Asks the server which sign-in methods it offers so single sign-on is only ever shown when it
     * will actually work. This is best effort — a server that cannot be reached simply leaves the
     * password form as the only option.
     */
    private fun probeAuthOptions(serverUrl: String) {
        probeJob?.cancel()
        if (serverUrl.isBlank()) return
        probeJob = viewModelScope.launch {
            val options = sessionRepository.discoverAuthOptions(serverUrl)
            if (_state.value.serverUrl != serverUrl) return@launch
            _state.value = _state.value.copy(
                openIdAvailable = options?.openIdEnabled == true,
                openIdLabel = options?.openIdButtonText ?: _state.value.openIdLabel
            )
        }
    }

    /**
     * Runs the OpenID Connect flow. The sign-in page opens on the paired phone when one is
     * available, otherwise in a browser on the watch.
     */
    fun loginWithOpenId() {
        val current = _state.value
        if (current.serverUrl.isBlank()) {
            _state.value = current.copy(error = "Enter your server address first")
            return
        }
        if (current.openIdInProgress) return
        _state.value = current.copy(openIdInProgress = true, error = null)
        viewModelScope.launch {
            sessionRepository.loginWithOpenId(current.serverUrl)
                .onSuccess {
                    libraryRepository.refreshLibraries()
                    _state.value = _state.value.copy(openIdInProgress = false, password = "")
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        openIdInProgress = false,
                        error = it.message ?: "Single sign-on failed"
                    )
                }
        }
    }

    fun username(value: String) { _state.value = _state.value.copy(username = value, error = null) }
    fun password(value: String) { _state.value = _state.value.copy(password = value, error = null) }

    fun login() {
        val current = _state.value
        if (current.serverUrl.isBlank() || current.username.isBlank() || current.password.isBlank()) {
            _state.value = current.copy(error = "Enter server, username and password")
            return
        }
        _state.value = current.copy(loading = true, error = null)
        viewModelScope.launch {
            sessionRepository.login(current.serverUrl, current.username, current.password)
                .onSuccess {
                    libraryRepository.refreshLibraries()
                    _state.value = _state.value.copy(loading = false, password = "")
                }
                .onFailure {
                    _state.value = _state.value.copy(loading = false, error = it.message ?: "Login failed")
                }
        }
    }
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    progressRepository: ProgressRepository,
    connectivityMonitor: ConnectivityMonitor,
    val sessionRepository: SessionRepository
) : ViewModel() {
    val online = connectivityMonitor.online
    val activeSession = sessionRepository.session
    val recentPlayback = combine(online, activeSession) { isOnline, server ->
        isOnline to server
    }.flatMapLatest { (isOnline, server) ->
        if (isOnline && server != null) progressRepository.recentForServer(server.url)
        else progressRepository.recentDownloaded()
    }.map { RecentPlaybackUiState(books = it) }
        .catch { error ->
            emit(
                RecentPlaybackUiState(
                    error = error.message ?: "Unable to load recent playback"
                )
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            RecentPlaybackUiState()
        )
    val libraries = libraryRepository.libraries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val downloaded = downloadRepository.downloadedBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val downloadStatuses = downloadRepository.downloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val error = MutableStateFlow<String?>(null)
    val refreshingLibraries = MutableStateFlow(false)
    val refreshingLibraryId = MutableStateFlow<String?>(null)
    val refreshingItemId = MutableStateFlow<String?>(null)
    private val _libraryBrowse = MutableStateFlow(LibraryBrowseState())
    val libraryBrowse: StateFlow<LibraryBrowseState> = _libraryBrowse.asStateFlow()
    private val _librarySeries = MutableStateFlow(LibrarySeriesState())
    val librarySeries: StateFlow<LibrarySeriesState> = _librarySeries.asStateFlow()
    private val _seriesDownload = MutableStateFlow(SeriesDownloadUiState())
    val seriesDownload: StateFlow<SeriesDownloadUiState> = _seriesDownload.asStateFlow()
    private val _podcastSeriesDownload = MutableStateFlow(PodcastSeriesDownloadUiState())
    val podcastSeriesDownload: StateFlow<PodcastSeriesDownloadUiState> =
        _podcastSeriesDownload.asStateFlow()
    private var browseJob: Job? = null
    private var searchJob: Job? = null
    private var seriesJob: Job? = null
    private var seriesDownloadJob: Job? = null
    private var podcastSeriesDownloadJob: Job? = null

    init {
        if (sessionRepository.session.value != null) refreshLibraries()
        viewModelScope.launch {
            // Every progress write republishes this list, so during playback it fired every ten
            // seconds. Only the *set* of recently played books matters here, and an item that
            // could not be fetched must not be retried on every tick forever.
            val attempted = mutableSetOf<Pair<String, String>>()
            combine(online, activeSession) { isOnline, server ->
                if (isOnline) server else null
            }.flatMapLatest { server ->
                server?.let { progressRepository.recentProgressForServer(it.url) }
                    ?: flowOf(emptyList())
            }.map { recent ->
                recent.map { it.itemId to it.originServerUrl }
            }.distinctUntilChanged().collect { keys ->
                keys.forEach { key ->
                    if (key in attempted) return@forEach
                    if (libraryRepository.getItem(key.first, key.second) == null) {
                        attempted += key
                        libraryRepository.refreshItem(key.first)
                    }
                }
            }
        }
    }

    fun items(libraryId: String, originServerUrl: String): Flow<List<LibraryItemEntity>> =
        libraryRepository.items(libraryId, originServerUrl)
    fun item(itemId: String, originServerUrl: String): Flow<LibraryItemEntity?> =
        libraryRepository.item(itemId, originServerUrl)
    fun podcastEpisodes(podcastId: String, originServerUrl: String): Flow<List<LibraryItemEntity>> =
        libraryRepository.podcastEpisodes(podcastId, originServerUrl)
    fun download(itemId: String, originServerUrl: String) =
        downloadRepository.download(itemId, originServerUrl)

    fun loadSeries(libraryId: String, originServerUrl: String) {
        val origin = ServerIdentity.normalize(originServerUrl)
        val current = _librarySeries.value
        if (current.libraryId == libraryId && current.originServerUrl == origin &&
            (current.loaded || current.loading)
        ) return
        seriesJob?.cancel()
        _librarySeries.value = LibrarySeriesState(
            libraryId = libraryId,
            originServerUrl = origin,
            loading = true
        )
        seriesJob = viewModelScope.launch {
            val cached = try {
                libraryRepository.cachedItems(libraryId, origin)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                updateSeries(libraryId, origin) {
                    it.copy(loading = false, error = error.message ?: "Unable to read cached books")
                }
                return@launch
            }
            updateSeries(libraryId, origin) {
                it.copy(
                    series = LibrarySeriesGrouping.group(cached),
                    loadedItemCount = cached.size
                )
            }
            libraryRepository.loadAllItems(libraryId, origin) { loaded, total ->
                updateSeries(libraryId, origin) {
                    it.copy(loadedItemCount = loaded, totalItemCount = total)
                }
            }.fold(
                onSuccess = { items ->
                    updateSeries(libraryId, origin) {
                        it.copy(
                            series = LibrarySeriesGrouping.group(items),
                            loading = false,
                            loaded = true,
                            error = null
                        )
                    }
                },
                onFailure = { failure ->
                    updateSeries(libraryId, origin) {
                        it.copy(
                            loading = false,
                            error = failure.message ?: "Unable to load all books for series"
                        )
                    }
                }
            )
        }
    }

    fun cancelSeriesLoad(libraryId: String, originServerUrl: String) {
        val origin = ServerIdentity.normalize(originServerUrl)
        val current = _librarySeries.value
        if (current.libraryId != libraryId || current.originServerUrl != origin || !current.loading) {
            return
        }
        seriesJob?.cancel()
        _librarySeries.value = current.copy(loading = false, error = null)
    }

    private fun updateSeries(
        libraryId: String,
        originServerUrl: String,
        transform: (LibrarySeriesState) -> LibrarySeriesState
    ) {
        val current = _librarySeries.value
        if (current.libraryId == libraryId && current.originServerUrl == originServerUrl) {
            _librarySeries.value = transform(current)
        }
    }

    fun downloadSeries(
        libraryId: String,
        seriesName: String,
        itemIds: List<String>,
        originServerUrl: String
    ) {
        if (seriesDownloadJob?.isActive == true) return
        val origin = ServerIdentity.normalize(originServerUrl)
        val activeOrigin = sessionRepository.session.value?.url
            ?.let(ServerIdentity::normalize)
        if (activeOrigin != origin) {
            _seriesDownload.value = SeriesDownloadUiState(
                libraryId = libraryId,
                originServerUrl = origin,
                seriesName = seriesName,
                error = "Sign in to the server this series belongs to."
            )
            return
        }
        val statuses = downloadStatuses.value
            .filter { ServerIdentity.matches(it.originServerUrl, origin) }
            .associate { it.itemId to it.status }
        val selection = LibrarySeriesDownloadSelection.create(itemIds, statuses)
        val pending = selection.itemIdsToQueue
        _seriesDownload.value = SeriesDownloadUiState(
            libraryId = libraryId,
            originServerUrl = origin,
            seriesName = seriesName,
            total = itemIds.distinct().size,
            skipped = selection.skippedCount,
            inProgress = pending.isNotEmpty()
        )
        seriesDownloadJob = viewModelScope.launch {
            var queued = 0
            var failed = 0
            val errors = mutableListOf<String>()
            pending.forEach { itemId ->
                try {
                    downloadRepository.start(itemId, origin)
                    queued++
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    failed++
                    errors += error.message ?: "Download could not be queued"
                }
                _seriesDownload.value = _seriesDownload.value.copy(
                    queued = queued,
                    failed = failed,
                    error = errors.firstOrNull()
                )
            }
            _seriesDownload.value = _seriesDownload.value.copy(
                queued = queued,
                failed = failed,
                inProgress = false,
                error = errors.distinct().joinToString("; ").ifBlank { null }
            )
        }
    }

    fun downloadPodcastSeries(
        podcastId: String,
        seriesName: String,
        episodes: List<LibraryItemEntity>,
        originServerUrl: String
    ) {
        if (podcastSeriesDownloadJob?.isActive == true ||
            _podcastSeriesDownload.value.requiresSpaceConfirmation
        ) return
        val origin = ServerIdentity.normalize(originServerUrl)
        val activeOrigin = sessionRepository.session.value?.url?.let(ServerIdentity::normalize)
        if (activeOrigin != origin) {
            _podcastSeriesDownload.value = PodcastSeriesDownloadUiState(
                podcastId = podcastId,
                originServerUrl = origin,
                seriesName = seriesName,
                error = "Sign in to the server this podcast belongs to."
            )
            return
        }
        val downloads = downloadStatuses.value
            .filter { ServerIdentity.matches(it.originServerUrl, origin) }
            .associateBy { it.itemId }
        val plan = PodcastSeriesDownloadPlanner.create(
            itemIds = episodes.map { it.id },
            sizes = episodes.associate { it.id to it.remoteSizeBytes },
            downloads = downloads
        )
        val initial = PodcastSeriesDownloadUiState(
            podcastId = podcastId,
            originServerUrl = origin,
            seriesName = seriesName,
            total = episodes.size,
            skipped = plan.skippedCount,
            requiredBytes = plan.requiredBytes,
            unknownSizeCount = plan.unknownSizeCount,
            inProgress = plan.itemIdsToQueue.isNotEmpty(),
            pendingItemIds = plan.itemIdsToQueue
        )
        if (plan.itemIdsToQueue.isEmpty()) {
            _podcastSeriesDownload.value = initial
            return
        }
        podcastSeriesDownloadJob = viewModelScope.launch {
            try {
                val available = downloadRepository.availableBytes()
                val needsWarning = plan.requiredBytes > available || plan.unknownSizeCount > 0
                _podcastSeriesDownload.value = initial.copy(
                    availableBytes = available,
                    inProgress = false,
                    requiresSpaceConfirmation = needsWarning
                )
                if (!needsWarning) queuePodcastEpisodes(initial)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _podcastSeriesDownload.value = initial.copy(
                    inProgress = false,
                    error = error.message ?: "Unable to check available storage"
                )
            }
        }
    }

    fun confirmPodcastSeriesDownload() {
        val current = _podcastSeriesDownload.value
        if (!current.requiresSpaceConfirmation || podcastSeriesDownloadJob?.isActive == true) return
        podcastSeriesDownloadJob = viewModelScope.launch { queuePodcastEpisodes(current) }
    }

    fun dismissPodcastSeriesSpaceWarning() {
        _podcastSeriesDownload.value = _podcastSeriesDownload.value.copy(
            requiresSpaceConfirmation = false,
            pendingItemIds = emptyList()
        )
    }

    private suspend fun queuePodcastEpisodes(state: PodcastSeriesDownloadUiState) {
        _podcastSeriesDownload.value = state.copy(
            requiresSpaceConfirmation = false,
            inProgress = true,
            queued = 0,
            failed = 0,
            error = null
        )
        var queued = 0
        var failed = 0
        val errors = mutableListOf<String>()
        state.pendingItemIds.forEach { itemId ->
            try {
                downloadRepository.start(itemId, state.originServerUrl)
                queued++
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                failed++
                errors += error.message ?: "Episode download could not be queued"
            }
            _podcastSeriesDownload.value = _podcastSeriesDownload.value.copy(
                queued = queued,
                failed = failed,
                error = errors.firstOrNull()
            )
        }
        _podcastSeriesDownload.value = _podcastSeriesDownload.value.copy(
            inProgress = false,
            error = errors.distinct().joinToString("; ").ifBlank { null }
        )
    }

    fun deletePodcastSeriesDownloads(
        podcastId: String,
        seriesName: String,
        episodeIds: List<String>,
        originServerUrl: String
    ) {
        if (podcastSeriesDownloadJob?.isActive == true) return
        val origin = ServerIdentity.normalize(originServerUrl)
        val downloads = downloadStatuses.value
            .filter {
                it.itemId in episodeIds && ServerIdentity.matches(it.originServerUrl, origin)
            }
        _podcastSeriesDownload.value = PodcastSeriesDownloadUiState(
            podcastId = podcastId,
            originServerUrl = origin,
            seriesName = seriesName,
            total = downloads.size,
            inProgress = downloads.isNotEmpty(),
            deleting = true
        )
        podcastSeriesDownloadJob = viewModelScope.launch {
            var deleted = 0
            var failed = 0
            val errors = mutableListOf<String>()
            downloads.forEach { download ->
                try {
                    downloadRepository.cancel(download.itemId, origin)
                    deleted++
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    failed++
                    errors += error.message ?: "Episode download could not be deleted"
                }
                _podcastSeriesDownload.value = _podcastSeriesDownload.value.copy(
                    deleted = deleted,
                    failed = failed,
                    error = errors.firstOrNull()
                )
            }
            _podcastSeriesDownload.value = _podcastSeriesDownload.value.copy(
                inProgress = false,
                error = errors.distinct().joinToString("; ").ifBlank { null }
            )
        }
    }

    fun search(query: String): Flow<List<LibraryItemEntity>> =
        sessionRepository.session.value?.url?.let { origin ->
            if (query.isBlank()) flowOf(emptyList())
            else libraryRepository.searchLocal(query, origin)
        } ?: flowOf(emptyList())

    fun refreshLibraries() {
        if (refreshingLibraries.value || sessionRepository.session.value == null) return
        viewModelScope.launch {
            refreshingLibraries.value = true
            libraryRepository.refreshLibraries()
                .onSuccess { error.value = null }
                .onFailure { error.value = it.message ?: "Unable to load server libraries" }
            refreshingLibraries.value = false
        }
    }

    fun openLibrary(libraryId: String, originServerUrl: String) {
        val origin = ServerIdentity.normalize(originServerUrl)
        val current = _libraryBrowse.value
        if (current.libraryId == libraryId && current.originServerUrl == origin &&
            current.page >= 0 && current.query.isBlank()
        ) return
        searchJob?.cancel()
        browseJob?.cancel()
        _libraryBrowse.value = LibraryBrowseState(
            libraryId = libraryId,
            originServerUrl = origin,
            loading = true
        )
        browseJob = viewModelScope.launch {
            val cached = libraryRepository.cachedPage(
                libraryId,
                origin,
                page = 0,
                limit = LibraryRepository.PAGE_SIZE
            )
            updateBrowse(libraryId, origin) {
                it.copy(items = cached, hasMore = cached.size == LibraryRepository.PAGE_SIZE)
            }
            libraryRepository.loadPage(
                libraryId,
                page = 0,
                limit = LibraryRepository.PAGE_SIZE
            ).fold(
                onSuccess = { page ->
                    updateBrowse(libraryId, origin) {
                        it.copy(
                            items = page.items,
                            page = page.page,
                            total = page.total,
                            hasMore = page.hasMore,
                            loading = false,
                            error = null
                        )
                    }
                },
                onFailure = { failure ->
                    updateBrowse(libraryId, origin) {
                        it.copy(
                            page = if (cached.isEmpty()) -1 else 0,
                            total = cached.size,
                            hasMore = cached.size == LibraryRepository.PAGE_SIZE,
                            loading = false,
                            error = failure.message
                        )
                    }
                }
            )
        }
    }

    fun loadNextLibraryPage() {
        val current = _libraryBrowse.value
        if (current.loading || !current.hasMore || current.query.isNotBlank()) return
        val nextPage = current.page + 1
        _libraryBrowse.value = current.copy(loading = true, error = null)
        browseJob = viewModelScope.launch {
            libraryRepository.loadPage(
                current.libraryId,
                nextPage,
                LibraryRepository.PAGE_SIZE
            ).fold(
                onSuccess = { page ->
                    updateBrowse(current.libraryId, current.originServerUrl) { latest ->
                        latest.copy(
                            items = (latest.items + page.items).distinctBy { it.id },
                            page = page.page,
                            total = maxOf(latest.total, page.total),
                            hasMore = page.hasMore,
                            loading = false,
                            error = null
                        )
                    }
                },
                onFailure = { failure ->
                    val cached = libraryRepository.cachedPage(
                        current.libraryId,
                        current.originServerUrl,
                        nextPage,
                        LibraryRepository.PAGE_SIZE
                    )
                    updateBrowse(current.libraryId, current.originServerUrl) { latest ->
                        latest.copy(
                            items = (latest.items + cached).distinctBy { it.id },
                            page = if (cached.isEmpty()) latest.page else nextPage,
                            hasMore = cached.size == LibraryRepository.PAGE_SIZE,
                            loading = false,
                            error = failure.message
                        )
                    }
                }
            )
        }
    }

    fun retryLibraryPage() {
        val current = _libraryBrowse.value
        if (current.loading || current.query.isNotBlank()) return
        _libraryBrowse.value = current.copy(hasMore = true, error = null)
        loadNextLibraryPage()
    }

    fun searchLibrary(libraryId: String, originServerUrl: String, query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            _libraryBrowse.value = LibraryBrowseState()
            openLibrary(libraryId, originServerUrl)
            return
        }
        browseJob?.cancel()
        searchJob?.cancel()
        val origin = ServerIdentity.normalize(originServerUrl)
        _libraryBrowse.value = LibraryBrowseState(
            libraryId = libraryId,
            originServerUrl = origin,
            query = cleanQuery,
            loading = true,
            hasMore = false
        )
        searchJob = viewModelScope.launch {
            delay(350)
            val cached = libraryRepository.searchCached(
                libraryId,
                origin,
                cleanQuery,
                LibraryRepository.SEARCH_LIMIT
            )
            updateBrowse(libraryId, origin, cleanQuery) { it.copy(items = cached) }
            libraryRepository.searchServer(
                libraryId,
                cleanQuery,
                LibraryRepository.SEARCH_LIMIT
            ).fold(
                onSuccess = { remote ->
                    updateBrowse(libraryId, origin, cleanQuery) {
                        it.copy(
                            items = remote,
                            total = remote.size,
                            loading = false,
                            error = null
                        )
                    }
                },
                onFailure = { failure ->
                    updateBrowse(libraryId, origin, cleanQuery) {
                        it.copy(
                            total = cached.size,
                            loading = false,
                            error = failure.message
                        )
                    }
                }
            )
        }
    }

    private fun updateBrowse(
        libraryId: String,
        originServerUrl: String,
        query: String = _libraryBrowse.value.query,
        transform: (LibraryBrowseState) -> LibraryBrowseState
    ) {
        val current = _libraryBrowse.value
        if (current.libraryId == libraryId &&
            current.originServerUrl == originServerUrl &&
            current.query == query
        ) {
            _libraryBrowse.value = transform(current)
        }
    }

    fun refreshItem(itemId: String, originServerUrl: String) {
        viewModelScope.launch {
            val activeOrigin = sessionRepository.session.value?.url
                ?.let(ServerIdentity::normalize)
                .orEmpty()
            if (!ServerIdentity.matches(originServerUrl, activeOrigin)) return@launch
            refreshingItemId.value = itemId
            libraryRepository.refreshItem(itemId)
                .onSuccess { error.value = null }
                .onFailure { error.value = it.message }
            refreshingItemId.value = null
        }
    }

    fun startDownload(itemId: String) = launchDownloadAction { downloadRepository.start(itemId) }
    fun pauseDownload(itemId: String, originServerUrl: String) =
        launchDownloadAction { downloadRepository.pause(itemId, originServerUrl) }
    fun cancelDownload(itemId: String, originServerUrl: String) =
        launchDownloadAction { downloadRepository.cancel(itemId, originServerUrl) }
    fun deleteDownload(itemId: String, originServerUrl: String) =
        launchDownloadAction { downloadRepository.delete(itemId, originServerUrl) }

    private fun launchDownloadAction(action: suspend () -> Unit) = viewModelScope.launch {
        runCatching { action() }
            .onSuccess { error.value = null }
            .onFailure { error.value = it.message ?: "Download action failed" }
    }
}

data class PlayerUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val itemId: String? = null,
    val title: String = "",
    val author: String? = null,
    val playing: Boolean = false,
    val chapterTitle: String? = null,
    val currentChapterTitle: String? = null,
    val chapterNumber: Int = 0,
    val chapterCount: Int = 0,
    val hasNextChapter: Boolean = false,
    val hasPreviousChapter: Boolean = false,
    val conflictPositionMs: Long? = null,
    val originServerUrl: String? = null,
    val coverPath: String? = null,
    /** Bookmarks are a book-timeline feature; podcast episodes don't have a documented mapping. */
    val isPodcastEpisode: Boolean = false,
    /** The book's chapters, for the chapter list. The same instance until the book changes. */
    val chapters: List<ChapterOption> = emptyList(),
    /** Chapter starts as fractions of the book (first excluded), for the progress ring's gaps. */
    val chapterMarks: List<Float> = emptyList()
)

data class ChapterOption(val title: String, val startMs: Long, val durationMs: Long)

/**
 * The player's position, kept out of [PlayerUiState] because it changes every second while the
 * player is visible. Only the progress bar, timestamp, and conflict dialog read it, so those ticks
 * no longer recompose the rest of the player or re-emit to Home's Now Playing row.
 */
data class PlaybackPosition(
    val bookPositionMs: Long = 0,
    val bookDurationMs: Long = 0,
    val displayPositionMs: Long = 0,
    val displayDurationMs: Long = 0
) {
    val bookProgress: Float
        get() = if (bookDurationMs > 0) {
            (bookPositionMs.toFloat() / bookDurationMs).coerceIn(0f, 1f)
        } else {
            0f
        }

    val displayProgress: Float
        get() = if (displayDurationMs > 0) {
            (displayPositionMs.toFloat() / displayDurationMs).coerceIn(0f, 1f)
        } else {
            0f
        }
}

data class NowPlayingUiState(
    val itemId: String? = null,
    val title: String = "",
    val playing: Boolean = false,
    val originServerUrl: String? = null
)

/**
 * Gate for the player's one-second position ticker.
 *
 * Each tick costs a binder round trip to the playback service plus a recomposition, so it must
 * only run while the position is genuinely on screen. Playback itself is owned by the service and
 * is unaffected by this gate.
 */
internal object PlaybackTicker {
    fun shouldTick(playing: Boolean, ambient: Boolean, playerScreenActive: Boolean): Boolean =
        playing && !ambient && playerScreenActive
}

/**
 * The fixed playback-speed presets offered in the player's options menu, plus the pure
 * string-formatting used to label them.
 */
internal object PlaybackSpeedFormat {
    val PRESETS = listOf(1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 2.0f, 3.0f)

    /** Trims a trailing ".0" so whole-number speeds read as "2x" rather than "2.0x". */
    fun label(speed: Float): String {
        val rounded = (speed * 10).toInt() / 10f
        return if (rounded == rounded.toInt().toFloat()) {
            rounded.toInt().toString()
        } else {
            rounded.toString()
        }
    }

    /**
     * Falls back to 1.0x for anything outside [PRESETS]. Guards against a stored preference from
     * an older/newer build whose preset list differed, or any other corrupted value, ever reaching
     * the player as an unsupported speed.
     */
    fun sanitize(speed: Float): Float = if (speed in PRESETS) speed else 1.0f
}

/** Formats the auto-generated title given to a bookmark created from the current position. */
internal object BookmarkTitleFormat {
    fun forPosition(positionMs: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(positionMs.coerceAtLeast(0))
        val label = if (totalSeconds >= 3600) {
            "%d:%02d:%02d".format(totalSeconds / 3600, totalSeconds / 60 % 60, totalSeconds % 60)
        } else {
            "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
        }
        return "Bookmark at $label"
    }
}

/**
 * Maps chapter skips onto absolute book positions.
 *
 * Chapters are book-level metadata while the player holds one media item per audio file, so every
 * result here is a whole-book position that the caller must translate into a track index and
 * offset before seeking.
 */
internal object ChapterNavigator {
    /**
     * How far into a chapter "previous" starts meaning "restart this chapter" instead of "go back
     * one". This matches the behaviour of essentially every media player, and on an audiobook it
     * is what makes the button usable for re-hearing a passage.
     */
    const val RESTART_THRESHOLD_MS = 3_000L

    /** Index of the chapter containing [positionMs], or -1 if the position precedes them all. */
    /**
     * Where each chapter after the first begins, as a fraction of [durationMs]. Starts outside
     * (0, 1) are dropped, so a bad chapter table can never draw a gap off the ring.
     */
    fun marks(chapters: List<ChapterEntity>, durationMs: Long): List<Float> {
        if (durationMs <= 0) return emptyList()
        return chapters.map { it.startMs.toFloat() / durationMs }
            .filter { it > 0f && it < 1f }
            .distinct()
            .sorted()
    }

    fun indexAt(chapters: List<ChapterEntity>, positionMs: Long): Int =
        chapters.indexOfLast { positionMs >= it.startMs }

    /**
     * Start of the next chapter, or null when there is none. Returning null (rather than the end
     * of the book) keeps the last chapter from silently ending playback.
     */
    fun nextStart(chapters: List<ChapterEntity>, positionMs: Long): Long? {
        if (chapters.isEmpty()) return null
        return chapters.firstOrNull { it.startMs > positionMs }?.startMs
    }

    /**
     * Start of the current chapter once [RESTART_THRESHOLD_MS] into it, otherwise the start of the
     * previous one. Null only when there is nothing to go back to.
     */
    fun previousStart(chapters: List<ChapterEntity>, positionMs: Long): Long? {
        if (chapters.isEmpty()) return null
        val index = indexAt(chapters, positionMs)
        // Before the first chapter starts there is nowhere earlier to go, except the very start.
        if (index < 0) return if (positionMs > 0) 0 else null
        val current = chapters[index]
        if (positionMs - current.startMs >= RESTART_THRESHOLD_MS) return current.startMs
        return chapters.getOrNull(index - 1)?.startMs
            ?: current.startMs.takeIf { positionMs > it }
    }
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiClient: ApiClient,
    private val sessionRepository: SessionRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val progressRepository: ProgressRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val sleepTimerManager: SleepTimerManager,
    private val connectivityMonitor: ConnectivityMonitor,
    private val settingsStore: SettingsStore,
    private val audioOutputs: AudioOutputs
) : ViewModel() {
    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()
    private val _position = MutableStateFlow(PlaybackPosition())
    val position: StateFlow<PlaybackPosition> = _position.asStateFlow()
    private val _controlsPageRequests = MutableStateFlow(0)
    /** Increments when something (the tile) asks the player to show its main controls page. */
    val controlsPageRequests: StateFlow<Int> = _controlsPageRequests.asStateFlow()
    val nowPlaying = state
        .map {
            NowPlayingUiState(
                itemId = it.itemId,
                title = it.title,
                playing = it.playing,
                originServerUrl = it.originServerUrl
            )
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, NowPlayingUiState())
    val progressMode = settingsStore.progressDisplayMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ProgressDisplayMode.BOOK)
    val playbackSpeed = settingsStore.playbackSpeed
        .map(PlaybackSpeedFormat::sanitize)
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1.0f)
    val online = connectivityMonitor.online
    val activeSession = sessionRepository.session
    val sleepTimer = sleepTimerManager.state

    private var controller: MediaController? = null
    private var ticker: Job? = null
    private var progressObserver: Job? = null
    private val ambient = MutableStateFlow(false)
    private val playerScreenActive = MutableStateFlow(false)
    private var chapters: List<ChapterEntity> = emptyList()
        set(value) {
            field = value
            chapterOptions = value.map { ChapterOption(it.title, it.startMs, it.endMs - it.startMs) }
            chapterMarksFor = -1L
        }
    private var chapterOptions: List<ChapterOption> = emptyList()
    // Marks depend on the book duration, which is only known from the loaded media item.
    private var chapterMarks: List<Float> = emptyList()
    private var chapterMarksFor = -1L
    private var currentOriginServerUrl: String? = null
    private val _outputPrompt = MutableStateFlow<OutputPrompt?>(null)
    /** Non-null while the player is asking how to listen because no headphones are connected. */
    val outputPrompt: StateFlow<OutputPrompt?> = _outputPrompt.asStateFlow()
    private val _bookmarks = MutableStateFlow<List<AudioBookmarkDto>>(emptyList())
    /** Bookmarks for the currently loaded book, newest fetch wins; cleared when the book changes. */
    val bookmarks: StateFlow<List<AudioBookmarkDto>> = _bookmarks.asStateFlow()
    private val _bookmarkError = MutableStateFlow<String?>(null)
    val bookmarkError: StateFlow<String?> = _bookmarkError.asStateFlow()
    private var bookmarksJob: Job? = null
    private var speakerAccepted = false
    private var headsetWaiter: Job? = null
    private var pendingConnects: MutableList<(MediaController) -> Unit>? = null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(playing = isPlaying)
        }
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) _state.value = _state.value.copy(loading = false)
        }
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = updatePosition()
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) = updatePosition()
    }

    init {
        connect(::restoreActivePlayback)
    }

    /** Loads a book at its saved position and, unless [autoPlay] is false, starts it. */
    fun play(itemId: String, originServerUrl: String, autoPlay: Boolean = true) {
        val normalizedOrigin = ServerIdentity.normalize(originServerUrl)
        if (isLoaded(itemId, originServerUrl)) return
        _state.value = PlayerUiState(loading = true, itemId = itemId)
        _position.value = PlaybackPosition()
        _bookmarks.value = emptyList()
        _bookmarkError.value = null
        bookmarksJob?.cancel()
        progressObserver?.cancel()
        viewModelScope.launch {
            chapters = libraryRepository.chapters(itemId, normalizedOrigin).first()
            val item = libraryRepository.getItem(itemId, normalizedOrigin)
            val local = downloadRepository.getBook(itemId, normalizedOrigin)
            if (local == null && sessionRepository.session.value == null) {
                fail("This book is not downloaded and the server is unavailable")
                return@launch
            }
            currentOriginServerUrl = normalizedOrigin
            val isPodcastEpisode = (local?.item?.parentItemId ?: item?.parentItemId) != null
            _state.value = _state.value.copy(
                originServerUrl = originServerUrl,
                isPodcastEpisode = isPodcastEpisode
            )
            if (!isPodcastEpisode) loadBookmarks(itemId)
            observeProgress(itemId, normalizedOrigin)
            connect { mediaController ->
                viewModelScope.launch playbackLoad@{
                    val mediaItems: List<MediaItem>
                    val title: String
                    val author: String?
                    if (local != null) {
                        title = local.item.title
                        author = local.item.author
                        mediaItems = PlaybackService.localMediaItems(local)
                    } else {
                        val cached = item ?: run {
                            fail("Book metadata is unavailable")
                            return@playbackLoad
                        }
                        val server = sessionRepository.requireServer()
                        if (!ServerIdentity.matches(server.url, normalizedOrigin)) {
                            fail("Sign in to ${normalizedOrigin} to stream this book")
                            return@playbackLoad
                        }
                        if (!connectivityMonitor.hasActiveNetwork()) {
                            fail("This book is not downloaded and the watch is offline")
                            return@playbackLoad
                        }
                        val api = apiClient.scopedApi(server.url, server.token)
                        val response = runCatching {
                            cached.parentItemId?.let { podcastId ->
                                api.startPodcastPlaybackSession(podcastId, itemId)
                            } ?: api.startPlaybackSession(itemId)
                        }.getOrNull()
                        val playback = response?.takeIf { it.isSuccessful }?.body()
                        if (playback == null) {
                            fail("Unable to start streaming")
                            return@playbackLoad
                        }
                        title = cached.title
                        author = cached.author
                        mediaItems = PlaybackService.remoteMediaItems(
                            server.url, itemId, title, author, playback,
                            artworkUrl = ApiClient.coverSource(null, cached.coverPath, itemId)
                                ?.let { path ->
                                    ApiClient.sizedCoverUrl(
                                        ApiClient.resolveUrl(server.url, path),
                                        SESSION_ARTWORK_WIDTH_PX
                                    )
                                }
                        )
                    }
                    if (mediaItems.isEmpty()) {
                        fail("No audio tracks are available")
                        return@playbackLoad
                    }
                    val saved = progressRepository.get(itemId, normalizedOrigin)?.positionMs ?: 0
                    val start = startPosition(mediaItems, saved)
                    _state.value = _state.value.copy(
                        title = title,
                        author = author,
                        coverPath = ApiClient.coverSource(
                            local?.item?.localCoverPath ?: item?.localCoverPath,
                            local?.item?.coverPath ?: item?.coverPath,
                            itemId
                        )
                    )
                    mediaController.setMediaItems(mediaItems, start.first, start.second)
                    mediaController.setPlaybackParameters(PlaybackParameters(playbackSpeed.value))
                    mediaController.prepare()
                    if (autoPlay) startPlayback(mediaController)
                }
            }
        }
    }

    /**
     * Opens [itemId] in the player, keeping whatever the playback service already has loaded if
     * it is that book, so opening from the tile never interrupts or restarts a running listen.
     * With [autoPlay] it also starts (or un-pauses) the book.
     */
    fun resume(itemId: String, originServerUrl: String, autoPlay: Boolean = true) {
        connect { player ->
            val extras = player.currentMediaItem?.mediaMetadata?.extras
            val alreadyLoaded = extras?.getString(PlaybackService.EXTRA_ITEM_ID) == itemId &&
                ServerIdentity.matches(
                    extras.getString(PlaybackService.EXTRA_ORIGIN_SERVER_URL).orEmpty(),
                    originServerUrl
                )
            if (!alreadyLoaded) {
                play(itemId, originServerUrl, autoPlay)
            } else if (autoPlay && !player.isPlaying) {
                startPlayback(player)
            }
        }
    }

    private fun isLoaded(itemId: String, originServerUrl: String): Boolean =
        _state.value.itemId == itemId &&
            _state.value.originServerUrl?.let(ServerIdentity::normalize) ==
            ServerIdentity.normalize(originServerUrl) &&
            controller?.mediaItemCount.orZero() > 0

    fun toggle() {
        controller?.let {
            if (it.isPlaying) {
                headsetWaiter?.cancel()
                it.pause()
            } else {
                startPlayback(it)
            }
        }
    }

    /**
     * Starts playback unless no headphones are connected, in which case the player asks first.
     * The book stays prepared at its resume position, so answering the prompt starts instantly.
     */
    private fun startPlayback(player: MediaController) {
        val outputs = audioOutputs.current()
        if (AudioOutputPolicy.shouldPrompt(outputs, speakerAccepted)) {
            _outputPrompt.value = OutputPrompt(speakerAvailable = outputs.hasSpeaker)
            return
        }
        _outputPrompt.value = null
        player.play()
    }

    /**
     * Opens the system screen for connecting headphones and resumes by itself once they connect.
     * The wait is bounded so a headset paired much later does not start a book unexpectedly.
     */
    fun connectHeadphones(activityContext: Context) {
        _outputPrompt.value = null
        audioOutputs.launchOutputSwitcher(activityContext)
        headsetWaiter?.cancel()
        headsetWaiter = viewModelScope.launch {
            val connected = withTimeoutOrNull(HEADSET_WAIT_MS) {
                audioOutputs.headsetConnected().first { it }
            }
            if (connected == true) controller?.play()
        }
    }

    fun useWatchSpeaker() {
        speakerAccepted = true
        _outputPrompt.value = null
        controller?.play()
    }

    fun dismissOutputPrompt() {
        _outputPrompt.value = null
    }

    fun seekBy(deltaMs: Long) {
        controller?.let { it.seekTo((it.currentPosition + deltaMs).coerceAtLeast(0)) }
    }

    fun nextChapter() {
        val position = bookPosition() ?: return
        ChapterNavigator.nextStart(chapters, position)?.let(::seekToBookPosition)
    }

    fun previousChapter() {
        val position = bookPosition() ?: return
        ChapterNavigator.previousStart(chapters, position)?.let(::seekToBookPosition)
    }

    fun seekToChapter(index: Int) {
        chapters.getOrNull(index)?.let { seekToBookPosition(it.startMs) }
    }

    private fun loadBookmarks(itemId: String) {
        bookmarksJob?.cancel()
        bookmarksJob = viewModelScope.launch {
            bookmarkRepository.list(itemId).fold(
                onSuccess = { _bookmarks.value = it },
                onFailure = { /* Leave any previous list; a load failure isn't worth surfacing. */ }
            )
        }
    }

    /** Bookmarks the current position with an auto-generated title; re-lists on success. */
    fun addBookmark() {
        val itemId = state.value.itemId ?: return
        if (state.value.isPodcastEpisode) return
        val positionMs = bookPosition() ?: return
        val timeSeconds = (positionMs / 1000L).toInt()
        viewModelScope.launch {
            bookmarkRepository.create(itemId, timeSeconds, BookmarkTitleFormat.forPosition(positionMs)).fold(
                onSuccess = { loadBookmarks(itemId) },
                onFailure = { _bookmarkError.value = "Couldn't save bookmark" }
            )
        }
    }

    fun deleteBookmark(bookmark: AudioBookmarkDto) {
        val itemId = state.value.itemId ?: return
        viewModelScope.launch {
            bookmarkRepository.delete(itemId, bookmark.time).fold(
                onSuccess = { loadBookmarks(itemId) },
                onFailure = { _bookmarkError.value = "Couldn't remove bookmark" }
            )
        }
    }

    fun seekToBookmark(bookmark: AudioBookmarkDto) {
        seekToBookPosition(bookmark.time * 1000L)
    }

    fun dismissBookmarkError() {
        _bookmarkError.value = null
    }

    /** Current whole-book position, spanning the per-file media items. */
    private fun bookPosition(): Long? {
        val player = controller ?: return null
        val extras = player.currentMediaItem?.mediaMetadata?.extras ?: return null
        return extras.getLong(PlaybackService.EXTRA_TRACK_OFFSET_MS) +
            player.currentPosition.coerceAtLeast(0)
    }

    /**
     * Seeks to an absolute book position. A book is usually split across several audio files, so
     * the target has to be resolved to a media item before seeking — `seekTo` on its own is
     * relative to the current file and would land in the wrong place.
     */
    private fun seekToBookPosition(positionMs: Long) {
        val player = controller ?: return
        val items = (0 until player.mediaItemCount).map { player.getMediaItemAt(it) }
        val target = startPosition(items, positionMs.coerceAtLeast(0))
        player.seekTo(target.first, target.second)
        updatePosition()
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerManager.start(minutes * 60_000L)
    }

    fun cancelSleepTimer() {
        sleepTimerManager.cancel()
    }

    /**
     * Persists the chosen speed and applies it to the live player immediately, if one is
     * connected. The next [play] call also re-applies the persisted value, so the choice survives
     * switching books and app restarts.
     */
    fun setPlaybackSpeed(speed: Float) {
        val sanitized = PlaybackSpeedFormat.sanitize(speed)
        viewModelScope.launch { settingsStore.setPlaybackSpeed(sanitized) }
        controller?.setPlaybackParameters(PlaybackParameters(sanitized))
    }

    fun showControlsPage() {
        _controlsPageRequests.value++
    }

    fun setAmbientMode(ambientMode: Boolean) {
        ambient.value = ambientMode
    }

    /**
     * Tracks whether the player screen is actually on-screen. The one-second position ticker is
     * only useful while a visible timestamp/progress bar is being rendered, so this keeps it from
     * running during hours of background or screen-off playback.
     */
    fun setPlayerScreenActive(active: Boolean) {
        playerScreenActive.value = active
    }

    fun keepLocalProgress() = state.value.itemId?.let { itemId ->
        val originServerUrl = currentOriginServerUrl ?: return@let
        viewModelScope.launch { progressRepository.keepLocal(itemId, originServerUrl) }
    }

    fun keepServerProgress() = state.value.itemId?.let { itemId ->
        val originServerUrl = currentOriginServerUrl ?: return@let
        viewModelScope.launch {
            progressRepository.keepServer(itemId, originServerUrl)
            progressRepository.get(itemId, originServerUrl)?.positionMs?.let { position ->
                val items = (0 until (controller?.mediaItemCount ?: 0)).mapNotNull {
                    controller?.getMediaItemAt(it)
                }
                val start = startPosition(items, position)
                controller?.seekTo(start.first, start.second)
            }
        }
    }

    /**
     * Runs [block] with the connected controller. Requests made while the first connection is
     * still in flight queue behind it (in order) instead of building a second controller.
     */
    private fun connect(block: (MediaController) -> Unit) {
        controller?.let {
            block(it)
            return
        }
        pendingConnects?.let {
            it += block
            return
        }
        pendingConnects = mutableListOf(block)
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            val blocks = pendingConnects.orEmpty()
            pendingConnects = null
            runCatching { future.get() }.onSuccess {
                controller = it
                it.addListener(listener)
                startTicker()
                blocks.forEach { queued -> queued(it) }
            }.onFailure { error -> fail(error.message ?: "Playback service unavailable") }
        }, MoreExecutors.directExecutor())
    }

    /**
     * Polls the player position once per second, but only while that position is actually visible:
     * playing, not in ambient always-on mode, and with the player screen on-screen.
     *
     * Every tick is a binder round trip to [PlaybackService] plus a state emission and
     * recomposition. Previously this loop ran unconditionally for the life of the ViewModel, so a
     * multi-hour background listen paid thousands of pointless IPC wakeups.
     *
     * The combined condition is deliberately *not* de-duplicated: each transition of any input
     * re-enters [collectLatest] and refreshes the position once, so returning to the screen or
     * leaving ambient mode shows a correct value even when the tick condition itself stays false
     * (for example while paused). Seeks are covered separately by `onPositionDiscontinuity`.
     */
    private fun startTicker() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            combine(
                state.map { it.playing }.distinctUntilChanged(),
                ambient,
                playerScreenActive
            ) { playing, isAmbient, screenActive ->
                PlaybackTicker.shouldTick(playing, isAmbient, screenActive)
            }
                .collectLatest { shouldTick ->
                    updatePosition()
                    while (shouldTick) {
                        delay(1_000)
                        updatePosition()
                    }
                }
        }
    }

    private fun updatePosition() {
        val player = controller ?: return
        val extras = player.currentMediaItem?.mediaMetadata?.extras ?: return
        val bookPosition = extras.getLong(PlaybackService.EXTRA_TRACK_OFFSET_MS) +
            player.currentPosition.coerceAtLeast(0)
        val bookDuration = extras.getLong(PlaybackService.EXTRA_BOOK_DURATION_MS)
        val chapter = chapters.lastOrNull { bookPosition >= it.startMs && bookPosition < it.endMs }
        val chapterMode = progressMode.value == ProgressDisplayMode.CHAPTER && chapter != null
        val chapterIndex = ChapterNavigator.indexAt(chapters, bookPosition)
        if (chapterMarksFor != bookDuration) {
            chapterMarks = ChapterNavigator.marks(chapters, bookDuration)
            chapterMarksFor = bookDuration
        }
        _position.value = PlaybackPosition(
            bookPositionMs = bookPosition,
            bookDurationMs = bookDuration,
            displayPositionMs = if (chapterMode) bookPosition - chapter.startMs else bookPosition,
            displayDurationMs = if (chapterMode) chapter.endMs - chapter.startMs else bookDuration
        )
        // Unchanged fields make this an equal value, which StateFlow does not re-emit.
        _state.value = _state.value.copy(
            playing = player.isPlaying,
            chapterTitle = if (chapterMode) chapter.title else null,
            // Chapter navigation is independent of the elapsed/total display preference, so these
            // are populated even when the timestamp is showing whole-book progress.
            currentChapterTitle = chapters.getOrNull(chapterIndex)?.title,
            chapterNumber = chapterIndex + 1,
            chapterCount = chapters.size,
            hasNextChapter = ChapterNavigator.nextStart(chapters, bookPosition) != null,
            hasPreviousChapter = ChapterNavigator.previousStart(chapters, bookPosition) != null,
            chapters = chapterOptions,
            chapterMarks = chapterMarks
        )
    }

    /** Surfaces a sync conflict for the loaded book, including after the UI is recreated. */
    private fun observeProgress(itemId: String, originServerUrl: String) {
        progressObserver?.cancel()
        progressObserver = viewModelScope.launch {
            progressRepository.observe(itemId, ServerIdentity.normalize(originServerUrl)).collect {
                _state.value = _state.value.copy(conflictPositionMs = it?.conflictPositionMs)
            }
        }
    }

    private fun restoreActivePlayback(player: MediaController) {
        val mediaItem = player.currentMediaItem ?: return
        val extras = mediaItem.mediaMetadata.extras ?: return
        val itemId = extras.getString(PlaybackService.EXTRA_ITEM_ID) ?: return
        val originServerUrl = extras.getString(PlaybackService.EXTRA_ORIGIN_SERVER_URL) ?: return
        currentOriginServerUrl = originServerUrl
        _state.value = _state.value.copy(
            itemId = itemId,
            title = mediaItem.mediaMetadata.title?.toString().orEmpty(),
            author = mediaItem.mediaMetadata.artist?.toString(),
            originServerUrl = originServerUrl
        )
        observeProgress(itemId, originServerUrl)
        viewModelScope.launch {
            val normalizedOrigin = ServerIdentity.normalize(originServerUrl)
            val isPodcastEpisode = (
                downloadRepository.getBook(itemId, normalizedOrigin)?.item?.parentItemId
                    ?: libraryRepository.getItem(itemId, normalizedOrigin)?.parentItemId
                ) != null
            _state.value = _state.value.copy(isPodcastEpisode = isPodcastEpisode)
            if (!isPodcastEpisode) loadBookmarks(itemId)
        }
        viewModelScope.launch {
            // Fetched fresh rather than read from the (eagerly-started) playbackSpeed StateFlow:
            // this runs at ViewModel construction, before that flow's first DataStore read is
            // guaranteed to have landed. A reconnecting MediaController may belong to a freshly
            // recreated PlaybackService whose ExoPlayer defaults to 1.0x, so this has to reapply
            // the real persisted value rather than risk overwriting a correct speed with a stale
            // default.
            player.setPlaybackParameters(
                PlaybackParameters(PlaybackSpeedFormat.sanitize(settingsStore.playbackSpeed.first()))
            )
            chapters = libraryRepository.chapters(itemId, originServerUrl).first()
            val item = libraryRepository.getItem(itemId, originServerUrl)
            val local = downloadRepository.getBook(itemId, originServerUrl)
            _state.value = _state.value.copy(
                coverPath = ApiClient.coverSource(
                    local?.item?.localCoverPath ?: item?.localCoverPath,
                    local?.item?.coverPath ?: item?.coverPath,
                    itemId
                )
            )
            updatePosition()
        }
        updatePosition()
    }

    private fun startPosition(items: List<MediaItem>, savedMs: Long): Pair<Int, Long> {
        items.forEachIndexed { index, mediaItem ->
            val start = mediaItem.mediaMetadata.extras?.getLong(PlaybackService.EXTRA_TRACK_OFFSET_MS) ?: 0
            val next = items.getOrNull(index + 1)?.mediaMetadata?.extras
                ?.getLong(PlaybackService.EXTRA_TRACK_OFFSET_MS) ?: Long.MAX_VALUE
            if (savedMs in start until next) return index to (savedMs - start)
        }
        return 0 to 0
    }

    private fun fail(message: String) {
        _state.value = _state.value.copy(loading = false, error = message)
    }

    override fun onCleared() {
        headsetWaiter?.cancel()
        ticker?.cancel()
        progressObserver?.cancel()
        controller?.removeListener(listener)
        controller?.release()
        super.onCleared()
    }

    private fun Int?.orZero() = this ?: 0

    private companion object {
        const val HEADSET_WAIT_MS = 2 * 60_000L
        /** Same size the app requests elsewhere, so the server's resized cover is reused. */
        const val SESSION_ARTWORK_WIDTH_PX = 160
    }
}

data class OutputPrompt(val speakerAvailable: Boolean)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsStore: SettingsStore,
    private val sessionRepository: SessionRepository,
    downloadRepository: DownloadRepository
) : ViewModel() {
    val mode = settingsStore.progressDisplayMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ProgressDisplayMode.BOOK)
    val storageStats = downloadRepository.storageStats()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DownloadRepository.StorageStats(0, 0, 0)
        )
    val downloadedBooks = downloadRepository.downloadedBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setMode(mode: ProgressDisplayMode) =
        viewModelScope.launch { settingsStore.setProgressDisplayMode(mode) }

    val seriesView = settingsStore.librarySeriesView
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val homeSearchVisible = settingsStore.homeSearchVisible
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val homePodcastsVisible = settingsStore.homePodcastsVisible
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setSeriesView(enabled: Boolean) =
        viewModelScope.launch { settingsStore.setLibrarySeriesView(enabled) }

    fun setHomeSearchVisible(visible: Boolean) =
        viewModelScope.launch { settingsStore.setHomeSearchVisible(visible) }

    fun setHomePodcastsVisible(visible: Boolean) =
        viewModelScope.launch { settingsStore.setHomePodcastsVisible(visible) }

    fun logout() = sessionRepository.logout()
}
