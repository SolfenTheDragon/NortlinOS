package com.nortlinos.wearos.service

import com.nortlinos.wearos.tile.NowPlayingSnapshot
import com.nortlinos.wearos.tile.NowPlayingSnapshotStore
import com.nortlinos.wearos.tile.TileCover
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.common.util.BitmapLoader
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.nortlinos.wearos.data.api.ApiClient
import com.nortlinos.wearos.data.local.DownloadedBookData
import com.nortlinos.wearos.data.model.PlaybackSession
import com.nortlinos.wearos.data.repository.ProgressRepository
import com.nortlinos.wearos.data.repository.SessionRepository
import com.nortlinos.wearos.data.repository.ServerIdentity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import okhttp3.OkHttpClient

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    @Inject lateinit var progressRepository: ProgressRepository
    @Inject lateinit var sessionRepository: SessionRepository
    @Inject lateinit var connectivityMonitor: ConnectivityMonitor
    @Inject lateinit var sleepTimerManager: SleepTimerManager
    @Inject lateinit var okHttpClient: OkHttpClient

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var progressJob: Job? = null
    private var session: MediaSession? = null
    // One at a time, in order, so a quick play-then-pause can never leave "Playing" on the tile.
    private val tileWrites = Dispatchers.IO.limitedParallelism(1)
    @UnstableApi
    private var bitmapLoader: BitmapLoader? = null
    private var tileCoverItemId: String? = null
    private var tileCoverJob: Job? = null
    private var playingSince = 0L

    // Configures ExoPlayer's authenticated resolving data source, audiobook buffering, and audio
    // offload, all of which are unstable Media3 APIs.
    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        // Streams through the app-wide OkHttpClient so audio shares its connection pool with the
        // API calls that precede it (the play-session request goes to the same host). A separate
        // HTTP stack would pay a second TCP + TLS handshake, and keep the radio up longer, every
        // time playback starts or rebuffers.
        val http = OkHttpDataSource.Factory(okHttpClient)
        val dataSource = ResolvingDataSource.Factory(
            DefaultDataSource.Factory(this, http)
        ) { dataSpec ->
            val activeServer = sessionRepository.session.value
            val activeBaseUrl = activeServer?.url?.let(ServerIdentity::normalize)
            if (activeServer != null && activeBaseUrl != null &&
                dataSpec.uri.toString().startsWith("$activeBaseUrl/")
            ) {
                val authHeader = ApiClient.authHeader(activeServer.token)
                    ?: return@Factory dataSpec
                dataSpec.buildUpon()
                    .setHttpRequestHeaders(dataSpec.httpRequestHeaders + authHeader)
                    .build()
            } else {
                dataSpec
            }
        }
        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSource))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setLoadControl(audiobookLoadControl())
            // Only a CPU wake lock. WAKE_MODE_NETWORK would also hold a WifiLock for the whole
            // session; that is for low-latency screen-on streaming, not buffered audio, and would
            // keep the radio in a high-power state for hours. LOCAL is correct for local files
            // and for buffered network streams alike.
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setAudioOffloadPreferences(AUDIO_OFFLOAD_PREFERENCES)
            .build()
        session = MediaSession.Builder(this, player)
            // Cover art for the system media controls. It goes through the same authenticated
            // data source as the audio (covers are auth-protected), and the cache keeps the last
            // bitmap so metadata updates within a book never re-fetch or re-decode it.
            .setBitmapLoader(
                CacheBitmapLoader(
                    DataSourceBitmapLoader(DataSourceBitmapLoader.DEFAULT_EXECUTOR_SERVICE.get(), dataSource)
                ).also { bitmapLoader = it }
            )
            .setCallback(object : MediaSession.Callback {
                override fun onAddMediaItems(
                    mediaSession: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    mediaItems: MutableList<MediaItem>
                ): ListenableFuture<MutableList<MediaItem>> {
                    val resolved = mediaItems.mapTo(mutableListOf()) { mediaItem ->
                        mediaItem.buildUpon()
                            .setUri(mediaItem.requestMetadata.mediaUri)
                            .build()
                    }
                    return Futures.immediateFuture(resolved)
                }
            })
            .build()
        scope.launch {
            sleepTimerManager.expired.collect {
                session?.player?.pause()
            }
        }
        player.addListener(progressListener)
    }

    /**
     * Persists the position at the moments a 10-second playing-only tick cannot cover.
     *
     * The ticker only runs while `isPlaying` is true, and the final flush lives in [onDestroy],
     * which a killed (rather than destroyed) service never reaches. That left pauses, seeks while
     * paused, sleep-timer expiry, book switches and reaching the end able to lose up to ten
     * seconds — or the whole listening position — until the next periodic sync.
     */
    private val progressListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            // The ticker exists only while playing, so a paused service never wakes the CPU.
            // Pausing is the natural "I'm done for now" moment, so push it to the server too.
            if (isPlaying) playingSince = System.currentTimeMillis()
            publishTileSnapshot()
            if (isPlaying) {
                startProgressTicker()
            } else {
                progressJob?.cancel()
                progressJob = null
                flushProgress(syncNow = true)
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            if (reason == Player.DISCONTINUITY_REASON_SEEK) flushProgress()
        }

        @UnstableApi
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // Only automatic track advances within a book. A playlist change is the player being
            // loaded with a new book, where the position is still being established.
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) flushProgress()
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) {
                publishTileSnapshot()
                saveTileCover()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                flushProgress(syncNow = true)
                publishTileSnapshot()
            }
        }
    }

    /**
     * Hands the tile its book and play state; the store skips writes that change nothing. The
     * player is read here on the main thread, and the preferences file is touched on IO so a
     * play/pause never waits on disk.
     */
    private fun publishTileSnapshot() {
        val snapshot = tileSnapshot() ?: return
        scope.launch(tileWrites) { NowPlayingSnapshotStore.write(this@PlaybackService, snapshot) }
    }

    /**
     * Saves a small copy of the new book's cover for the tile. It reuses the session's bitmap
     * loader, whose cache already holds (or is fetching) the same image for the media
     * notification, so the tile costs no extra download. The tile only shows the file once the
     * snapshot names this book, so a half-written or stale file is never displayed as the cover.
     */
    @UnstableApi
    private fun saveTileCover() {
        val player = session?.player ?: return
        val itemId = currentProgress()?.itemId ?: return
        if (itemId == tileCoverItemId) return
        tileCoverItemId = null
        tileCoverJob?.cancel()
        val artwork = player.currentMediaItem?.mediaMetadata?.artworkUri ?: return
        val loader = bitmapLoader ?: return
        tileCoverJob = scope.launch {
            val bitmap = runCatching { loader.loadBitmap(artwork).await() }.getOrNull() ?: return@launch
            val saved = kotlinx.coroutines.withContext(tileWrites) {
                runCatching { TileCover.write(NowPlayingSnapshotStore.coverFile(this@PlaybackService), bitmap) }
                    .isSuccess
            }
            if (saved && currentProgress()?.itemId == itemId) {
                tileCoverItemId = itemId
                publishTileSnapshot()
            }
        }
    }

    private fun tileSnapshot(playing: Boolean? = null): NowPlayingSnapshot? {
        val player = session?.player ?: return null
        val progress = currentProgress() ?: return null
        val metadata = player.currentMediaItem?.mediaMetadata ?: return null
        return NowPlayingSnapshot(
            itemId = progress.itemId,
            originServerUrl = progress.originServerUrl,
            title = metadata.title?.toString().orEmpty(),
            author = metadata.artist?.toString(),
            playing = playing ?: player.isPlaying,
            positionMs = progress.positionMs,
            durationMs = progress.durationMs,
            coverItemId = tileCoverItemId,
            playingSince = playingSince
        )
    }

    /** Snapshots the player on the main thread, then writes off it. */
    private fun flushProgress(syncNow: Boolean = false) {
        val progress = currentProgress() ?: return
        scope.launch {
            kotlinx.coroutines.withContext(Dispatchers.IO) {
                persistProgress(progress, syncNow)
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Without this the service lingers after the app is swiped away and is eventually killed
        // rather than destroyed, so onDestroy's final flush never runs. Stop only when there is no
        // playback intent: isPlaying alone is false while a stream is still buffering, and
        // stopping there would kill the background playback the user just asked for.
        val player = session?.player
        if (player == null || (!player.playWhenReady && !player.isPlaying)) stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        tileCoverJob?.cancel()
        val snapshot = tileSnapshot(playing = false)
        val progress = currentProgress()
        runBlocking(Dispatchers.IO) {
            // Queued behind any pending tile write so the final "paused" state lands last.
            snapshot?.let {
                kotlinx.coroutines.withContext(tileWrites) {
                    NowPlayingSnapshotStore.write(this@PlaybackService, it)
                }
            }
            progress?.let { persistProgress(it, syncNow = true) }
        }
        progressJob?.cancel()
        sleepTimerManager.cancel()
        scope.cancel()
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }

    /**
     * Streaming audiobooks buffer far ahead so the network radio can fetch one large burst and
     * then power down for minutes at a time. Radio "tail" energy dominates the cost of many small
     * fetches, so a long buffer is materially cheaper than ExoPlayer's video-tuned 50-second
     * default. Speech-bitrate audio keeps the memory cost of a 3-minute buffer small.
     */
    @UnstableApi
    private fun audiobookLoadControl(): LoadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            /* minBufferMs = */ MIN_BUFFER_MS,
            /* maxBufferMs = */ MAX_BUFFER_MS,
            /* bufferForPlaybackMs = */ BUFFER_FOR_PLAYBACK_MS,
            /* bufferForPlaybackAfterRebufferMs = */ BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
        )
        .setPrioritizeTimeOverSizeThresholds(true)
        .setBackBuffer(BACK_BUFFER_MS, /* retainBackBufferFromKeyframe = */ false)
        .build()

    /**
     * Writes the position every ~10 s of playback. Under audio offload the CPU sleeps between
     * DSP buffer refills and coroutine delays stall with it, so the real cadence is "every ~10 s
     * the process is awake"; the pause/seek/track-end/destroy flushes above cover the gaps.
     */
    private fun startProgressTicker() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (true) {
                delay(PROGRESS_TICK_MS)
                if (session?.player?.isPlaying == true) persistCurrentProgress()
            }
        }
    }

    private suspend fun persistCurrentProgress() {
        val progress = currentProgress() ?: return
        kotlinx.coroutines.withContext(Dispatchers.IO) { persistProgress(progress) }
    }

    private fun currentProgress(): CurrentProgress? {
        val player = session?.player ?: return null
        val extras = player.currentMediaItem?.mediaMetadata?.extras ?: return null
        val itemId = extras.getString(EXTRA_ITEM_ID) ?: return null
        val originServerUrl = extras.getString(EXTRA_ORIGIN_SERVER_URL) ?: return null
        return CurrentProgress(
            itemId = itemId,
            originServerUrl = originServerUrl,
            positionMs = extras.getLong(EXTRA_TRACK_OFFSET_MS) +
                player.currentPosition.coerceAtLeast(0),
            durationMs = extras.getLong(EXTRA_BOOK_DURATION_MS)
        )
    }

    /**
     * Writes the local progress snapshot. Routine 10-second ticks stay local-only and rely on
     * the existing periodic/connectivity-triggered sync paths; only [onDestroy]'s final flush
     * (`syncNow = true`) requests an immediate sync so the last known position reaches the server
     * promptly when playback stops. Without this distinction every 10-second tick during playback
     * would wake the radio for a network sync, draining battery far faster than necessary.
     */
    private suspend fun persistProgress(progress: CurrentProgress, syncNow: Boolean = false) {
        progressRepository.record(
            itemId = progress.itemId,
            originServerUrl = progress.originServerUrl,
            positionMs = progress.positionMs,
            durationMs = progress.durationMs
        )
        if (syncNow) connectivityMonitor.enqueueImmediateSync()
    }

    private data class CurrentProgress(
        val itemId: String,
        val originServerUrl: String,
        val positionMs: Long,
        val durationMs: Long
    )

    companion object {
        const val EXTRA_ITEM_ID = "item_id"
        const val EXTRA_ORIGIN_SERVER_URL = "origin_server_url"
        const val EXTRA_TRACK_OFFSET_MS = "track_offset_ms"
        const val EXTRA_BOOK_DURATION_MS = "book_duration_ms"

        private const val PROGRESS_TICK_MS = 10_000L
        private const val MIN_BUFFER_MS = 60_000
        private const val MAX_BUFFER_MS = 180_000
        private const val BUFFER_FOR_PLAYBACK_MS = 2_500
        private const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 5_000

        /**
         * Kept comfortably above the player's 30-second rewind so the most common backward seek
         * resolves from memory. ExoPlayer's default back buffer is 0 ms, which makes every rewind
         * re-seek the source — a fresh network request while streaming.
         */
        private const val BACK_BUFFER_MS = 50_000

        /**
         * Audio offload lets the DSP decode the stream so the CPU can sleep between buffers, which
         * is the single largest battery saving available for long spoken-word playback.
         * `AUDIO_OFFLOAD_MODE_ENABLED` is a preference, not a requirement: Media3 falls back to
         * normal decoding when the device or codec cannot offload. `AUDIO_OFFLOAD_MODE_REQUIRED`
         * must not be used here — it selects no tracks at all when offload is unavailable.
         *
         * Gapless support is required because a book's parts are split mid-narration, so an
         * audible gap at each track boundary would be a real regression.
         *
         * Speed-change support is required: the player offers non-1.0x playback speed presets, and
         * without this flag offload could engage on hardware that cannot vary speed while
         * offloaded, silently ignoring the speed control.
         */
        @UnstableApi
        private val AUDIO_OFFLOAD_PREFERENCES =
            TrackSelectionParameters.AudioOffloadPreferences.Builder()
                .setAudioOffloadMode(
                    TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED
                )
                .setIsGaplessSupportRequired(true)
                .setIsSpeedChangeSupportRequired(true)
                .build()

        fun localMediaItems(book: DownloadedBookData): List<MediaItem> =
            book.tracks.sortedBy { it.trackIndex }.map { track ->
                item(
                    uri = "file://${track.localPath}",
                    mediaId = track.trackIndex.toString(),
                    itemId = book.item.id,
                    originServerUrl = book.download?.originServerUrl.orEmpty(),
                    title = book.item.title,
                    author = book.item.author,
                    offsetMs = track.startOffsetMs,
                    durationMs = book.item.durationMs,
                    artworkUri = book.item.localCoverPath
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { Uri.fromFile(java.io.File(it)) }
                )
            }

        fun remoteMediaItems(
            baseUrl: String,
            itemId: String,
            title: String,
            author: String?,
            session: PlaybackSession,
            artworkUrl: String? = null
        ): List<MediaItem> = session.audioTracks.sortedBy { it.index }.map { track ->
            item(
                uri = ApiClient.resolveUrl(baseUrl, track.contentUrl),
                mediaId = track.index.toString(),
                itemId = itemId,
                originServerUrl = ServerIdentity.normalize(baseUrl),
                title = title,
                author = author,
                offsetMs = (track.startOffset * 1000).toLong(),
                durationMs = (session.duration * 1000).toLong(),
                artworkUri = artworkUrl?.let(Uri::parse)
            )
        }

        private fun item(
            uri: String,
            mediaId: String,
            itemId: String,
            originServerUrl: String,
            title: String,
            author: String?,
            offsetMs: Long,
            durationMs: Long,
            artworkUri: Uri? = null
        ): MediaItem {
            val extras = Bundle().apply {
                putString(EXTRA_ITEM_ID, itemId)
                putString(EXTRA_ORIGIN_SERVER_URL, originServerUrl)
                putLong(EXTRA_TRACK_OFFSET_MS, offsetMs)
                putLong(EXTRA_BOOK_DURATION_MS, durationMs)
            }
            return MediaItem.Builder()
                .setUri(uri)
                .setRequestMetadata(
                    MediaItem.RequestMetadata.Builder()
                        .setMediaUri(Uri.parse(uri))
                        .build()
                )
                .setMediaId(mediaId)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(author)
                        .setArtworkUri(artworkUri)
                        .setExtras(extras)
                        .build()
                )
                .build()
        }
    }
}
