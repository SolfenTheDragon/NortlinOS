package com.nortlinos.wearos.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nortlinos.wearos.R
import com.nortlinos.wearos.data.api.ApiClient
import com.nortlinos.wearos.data.local.DownloadDao
import com.nortlinos.wearos.data.local.DownloadStatus
import com.nortlinos.wearos.data.local.DownloadTrackEntity
import com.nortlinos.wearos.data.local.DownloadedItemEntity
import com.nortlinos.wearos.data.repository.DownloadRepository
import com.nortlinos.wearos.data.repository.LibraryRepository
import com.nortlinos.wearos.data.repository.SessionRepository
import com.nortlinos.wearos.data.model.Server
import com.nortlinos.wearos.data.repository.ServerIdentity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.FileOutputStream

class DownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val http: OkHttpClient by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            DownloadEntryPoint::class.java
        ).okHttpClient()
    }
    private var channelCreated = false

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val itemId = inputData.getString(KEY_ITEM_ID) ?: return@withContext Result.failure()
        val originServerUrl = inputData.getString(KEY_ORIGIN_SERVER_URL)
            ?: return@withContext Result.failure()
        val dependencies = EntryPointAccessors.fromApplication(
            applicationContext,
            DownloadEntryPoint::class.java
        )
        val dao = dependencies.downloadDao()
        val repository = dependencies.downloadRepository()
        val existing = dao.getDownload(itemId, originServerUrl)
            ?: return@withContext Result.failure()
        val notificationId = "$originServerUrl\u0000$itemId".hashCode()

        try {
            val server = dependencies.sessionRepository().requireServer()
            if (!ServerIdentity.matches(existing.originServerUrl, server.url)) {
                throw IOException("Download belongs to ${existing.originServerUrl}")
            }
            val queued = dao.getDownload(itemId, originServerUrl)?.status == DownloadStatus.QUEUED
            val started = dependencies.downloadQueue().withSlot(
                onWait = {
                    if (!queued) {
                        dao.upsertDownload(
                            existing.copy(status = DownloadStatus.QUEUED, error = null)
                        )
                    }
                }
            ) {
                // Foreground only once this book is actually transferring: a queued worker that
                // held a data-sync service would spend the platform's daily budget on waiting.
                setForeground(notification(notificationId, itemId, 0))
                transfer(
                    dependencies = dependencies,
                    dao = dao,
                    repository = repository,
                    existing = existing,
                    itemId = itemId,
                    originServerUrl = originServerUrl,
                    server = server,
                    notificationId = notificationId
                )
                true
            }
            // No slot came free in time; stay queued and let WorkManager bring this book back.
            if (started == null) Result.retry() else Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (error: InterruptedException) {
            dao.upsertDownload(existing.copy(status = DownloadStatus.PAUSED))
            Result.failure()
        } catch (error: Exception) {
            dao.upsertDownload(existing.copy(status = DownloadStatus.FAILED, error = error.message))
            Result.failure(workDataOf(KEY_ERROR to (error.message ?: "Download failed")))
        }
    }

    private suspend fun transfer(
        dependencies: DownloadEntryPoint,
        dao: DownloadDao,
        repository: DownloadRepository,
        existing: DownloadedItemEntity,
        itemId: String,
        originServerUrl: String,
        server: Server,
        notificationId: Int
    ) {
        setForeground(notification(notificationId, itemId, 0))
        val item = dependencies.libraryRepository().getItem(itemId, originServerUrl)
            ?: throw IOException("Book metadata was not cached")
        val api = dependencies.apiClient().scopedApi(server.url, server.token)
        val session = item.parentItemId?.let { podcastId ->
            api.startPodcastPlaybackSession(podcastId, itemId)
        } ?: api.startPlaybackSession(itemId)
        if (!session.isSuccessful) throw IOException("Playback session: HTTP ${session.code()}")
        val tracks = session.body()?.audioTracks.orEmpty().sortedBy { it.index }
        if (tracks.isEmpty()) throw IOException("No audio tracks are available")

        val directory = File(existing.localDirectory).apply { mkdirs() }
        val remainingTrackBytes = tracks.sumOf { track ->
            val destination = File(directory, "track_${track.index}.${extension(track.mimeType)}")
            (track.metadata?.size ?: 0L).minus(destination.length()).coerceAtLeast(0)
        }
        if (remainingTrackBytes > repository.availableBytes()) {
            throw IOException(
                "Not enough storage: $remainingTrackBytes bytes are still required"
            )
        }
        dao.upsertDownload(existing.copy(status = DownloadStatus.DOWNLOADING, error = null))
        if (item.coverPath != null) {
            val coverFile = File(directory, "cover.jpg")
            runCatching {
                downloadTrack(
                    ApiClient.sizedCoverUrl(
                        ApiClient.resolveUrl(
                            server.url,
                            ApiClient.coverEndpoint(item.parentItemId ?: itemId)
                        ),
                        OFFLINE_COVER_WIDTH_PX
                    ),
                    server.token,
                    coverFile,
                    repository.availableBytes(),
                    expectedBytes = 0
                )
            }.onSuccess {
                dependencies.libraryDao().setLocalCoverPath(
                    itemId,
                    originServerUrl,
                    coverFile.absolutePath
                )
            }.onFailure {
                coverFile.delete()
            }
        }
        val completedTracks = mutableListOf<DownloadTrackEntity>()
        var downloadedTotal = 0L

        tracks.forEachIndexed { position, track ->
            if (isStopped) throw InterruptedException("Download paused")
            val file = File(directory, "track_${track.index}.${extension(track.mimeType)}")
            downloadTrack(
                ApiClient.resolveUrl(server.url, track.contentUrl),
                server.token,
                file,
                repository.availableBytes(),
                expectedBytes = track.metadata?.size ?: 0
            )
            downloadedTotal += file.length()
            completedTracks += DownloadTrackEntity(
                itemId = itemId,
                originServerUrl = originServerUrl,
                trackIndex = track.index,
                localPath = file.absolutePath,
                contentUrl = track.contentUrl,
                mimeType = track.mimeType,
                startOffsetMs = (track.startOffset * 1000).toLong(),
                durationMs = (track.duration * 1000).toLong(),
                sizeBytes = file.length()
            )
            val percent = ((position + 1) * 100 / tracks.size)
            dao.upsertDownload(
                existing.copy(
                    status = DownloadStatus.DOWNLOADING,
                    localDirectory = directory.absolutePath,
                    downloadedBytes = downloadedTotal
                )
            )
            setProgress(workDataOf(KEY_PROGRESS to percent))
            setForeground(notification(notificationId, item.title, percent))
        }

        val retainedFiles = completedTracks.mapTo(mutableSetOf()) { it.localPath }
        retainedFiles += File(directory, "cover.jpg").absolutePath
        directory.listFiles()
            .orEmpty()
            .filter { it.isFile && it.absolutePath !in retainedFiles }
            .forEach {
                if (!it.delete()) throw IOException("Unable to remove obsolete audio file")
            }
        dao.deleteTracks(itemId, originServerUrl)
        dao.upsertTracks(completedTracks)
        dao.upsertDownload(
            existing.copy(
                status = DownloadStatus.DOWNLOADED,
                localDirectory = directory.absolutePath,
                fileSizeBytes = downloadedTotal,
                downloadedBytes = downloadedTotal,
                error = null
            )
        )
    }

    private fun downloadTrack(
        url: String,
        token: String,
        file: File,
        availableBytes: Long,
        expectedBytes: Long
    ) {
        if (expectedBytes > 0 && file.length() == expectedBytes) return
        if (expectedBytes > 0 && file.length() > expectedBytes && !file.delete()) {
            throw IOException("Unable to reset invalid partial download")
        }
        val offset = file.takeIf(File::exists)?.length() ?: 0L
        val request = Request.Builder().url(url)
            .apply {
                ApiClient.authHeader(token)?.let { (name, value) -> header(name, value) }
                if (offset > 0) header("Range", "bytes=$offset-")
            }
            .build()
        http.newCall(request).execute().use { response ->
            if (response.code == 416 && offset > 0) {
                val remoteBytes = response.header("Content-Range")
                    ?.substringAfterLast('/')
                    ?.toLongOrNull()
                if (remoteBytes == offset && (expectedBytes == 0L || expectedBytes == offset)) return
                if (file.exists() && !file.delete()) {
                    throw IOException("Unable to reset rejected partial download")
                }
                throw IOException("Server rejected the saved partial download; retry to restart it")
            }
            if (!response.isSuccessful) throw IOException("Download failed: HTTP ${response.code}")
            val append = offset > 0 && response.code == 206
            val required = response.body?.contentLength()?.coerceAtLeast(0) ?: 0
            if (required > availableBytes) throw IOException("Not enough storage: need $required bytes")
            response.body?.byteStream()?.use { input ->
                FileOutputStream(file, append).use { output ->
                    // Chunked so a pause/cancel stops the transfer (and the radio) within one
                    // buffer instead of after the whole track; partial bytes resume via Range.
                    val buffer = ByteArray(COPY_BUFFER_BYTES)
                    while (true) {
                        if (isStopped) throw InterruptedException("Download paused")
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                    }
                }
            } ?: throw IOException("Empty audio response")
            if (expectedBytes > 0 && file.length() != expectedBytes) {
                throw IOException(
                    "Incomplete download: expected $expectedBytes bytes, received ${file.length()}"
                )
            }
        }
    }

    private fun notification(notificationId: Int, title: String, progress: Int): ForegroundInfo =
        foregroundInfo(notificationId, "Downloading $title", progress)

    private fun foregroundInfo(
        notificationId: Int,
        contentTitle: String,
        progress: Int?
    ): ForegroundInfo {
        if (!channelCreated) {
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Audiobook downloads",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
            channelCreated = true
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(contentTitle)
            .setProgress(100, progress ?: 0, progress == null || progress == 0)
            .setOngoing(true)
            .build()
        return ForegroundInfo(
            notificationId,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun extension(mime: String): String = when {
        "mp4" in mime || "m4a" in mime -> "m4b"
        "ogg" in mime -> "ogg"
        "flac" in mime -> "flac"
        else -> "mp3"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DownloadEntryPoint {
        fun apiClient(): ApiClient
        fun okHttpClient(): OkHttpClient
        fun downloadDao(): DownloadDao
        fun libraryDao(): com.nortlinos.wearos.data.local.LibraryDao
        fun downloadRepository(): DownloadRepository
        fun downloadQueue(): DownloadQueue
        fun libraryRepository(): LibraryRepository
        fun sessionRepository(): SessionRepository
    }

    companion object {
        const val KEY_ITEM_ID = "item_id"
        const val KEY_ORIGIN_SERVER_URL = "origin_server_url"
        const val KEY_PROGRESS = "progress"
        const val KEY_ERROR = "error"
        private const val COPY_BUFFER_BYTES = 64 * 1024
        private const val OFFLINE_COVER_WIDTH_PX = 160
        private const val CHANNEL_ID = "downloads"
    }
}
