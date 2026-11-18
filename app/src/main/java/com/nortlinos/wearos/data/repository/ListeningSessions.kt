package com.nortlinos.wearos.data.repository

import androidx.room.withTransaction
import com.nortlinos.wearos.data.api.ApiClient
import com.nortlinos.wearos.data.api.AudiobookshelfApi
import com.nortlinos.wearos.data.api.LocalSessionDto
import com.nortlinos.wearos.data.api.LocalSessionsRequest
import com.nortlinos.wearos.data.api.MEDIA_PLAYER
import com.nortlinos.wearos.data.local.AppDatabase
import com.nortlinos.wearos.data.local.LibraryDao
import com.nortlinos.wearos.data.local.ListeningSessionDao
import com.nortlinos.wearos.data.local.ListeningSessionEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Records how long the watch actually plays each book and hands it to Audiobookshelf as
 * playback sessions, which is what the server's listening time and stats are built from.
 *
 * Sessions are written locally at the same moments, and with the same timestamp, as the
 * position in [ProgressRepository], so a session's last position is always the watch's position
 * as of that session's `updatedAt`. That matters because the server also applies a session's
 * position to media progress, but only when the session is newer than the server's progress. A
 * session can therefore never move progress anywhere the regular progress sync would not, and
 * one recorded during an unresolved conflict is older than the server's side and never
 * overrides it.
 */
@Singleton
class ListeningSessionRepository @Inject constructor(
    private val database: AppDatabase,
    private val dao: ListeningSessionDao,
    private val libraryDao: LibraryDao,
    private val apiClient: ApiClient
) {
    /**
     * Adds [listenedMs] of real playing time to the item's current session, starting a new one
     * when there is none to continue, and moves the session's position to [positionMs].
     */
    suspend fun record(
        itemId: String,
        originServerUrl: String,
        streamed: Boolean,
        positionMs: Long,
        durationMs: Long,
        listenedMs: Long,
        timestamp: Long
    ) {
        val origin = ServerIdentity.normalize(originServerUrl)
        database.withTransaction {
            val next = ListeningSessionPlanner.next(
                latest = dao.latest(itemId, origin),
                itemId = itemId,
                originServerUrl = origin,
                streamed = streamed,
                positionMs = positionMs,
                durationMs = durationMs,
                listenedMs = listenedMs,
                now = timestamp,
                newId = { UUID.randomUUID().toString() }
            ) ?: return@withTransaction
            dao.upsert(next)
        }
    }

    /**
     * Uploads every session with listening time the server does not have yet. Called after the
     * progress sync (see the class comment for why the order matters). Failures leave the rows
     * pending for the next sync; they never fail the progress sync itself.
     */
    suspend fun push(api: AudiobookshelfApi, originServerUrl: String) {
        val origin = ServerIdentity.normalize(originServerUrl)
        for (batch in 0 until MAX_BATCHES) {
            val pending = dao.pending(origin, BATCH_SIZE)
            if (pending.isEmpty()) break
            val response = runCatching {
                api.syncLocalSessions(
                    LocalSessionsRequest(apiClient.deviceInfo, pending.map { toDto(it, origin) })
                )
            }.getOrNull() ?: return
            if (!response.isSuccessful) return
            // Every answered session is done with, including ones the server refused because
            // the book is gone or no longer accessible: resending those would fail forever.
            val answered = response.body()?.results.orEmpty().mapNotNullTo(HashSet()) { it.id }
            var cleared = 0
            for (session in pending) {
                if (session.id in answered) {
                    cleared += dao.markCleanIfUnchanged(session.id, session.updatedAt)
                }
            }
            // Nothing acknowledged (an unexpected reply); stop instead of resending in a loop.
            if (cleared == 0) break
        }
        val expired = System.currentTimeMillis() - ListeningSessionPlanner.CONTINUE_WINDOW_MS
        dao.deleteSyncedBefore(expired)
        dao.deleteEmptyBefore(expired)
    }

    private suspend fun toDto(session: ListeningSessionEntity, origin: String): LocalSessionDto {
        val item = libraryDao.getItem(session.itemId, origin)
        // Podcast episodes are stored under their own id with the show as parent; the server
        // keys them as (show, episode).
        val podcastId = item?.parentItemId
        return LocalSessionDto(
            id = session.id,
            libraryItemId = podcastId ?: session.itemId,
            episodeId = podcastId?.let { session.itemId },
            mediaType = if (podcastId != null) "podcast" else "book",
            displayTitle = item?.title,
            displayAuthor = item?.author,
            duration = session.durationMs / 1000.0,
            playMethod = if (session.streamed) {
                LocalSessionDto.PLAY_METHOD_DIRECT_PLAY
            } else {
                LocalSessionDto.PLAY_METHOD_LOCAL
            },
            mediaPlayer = MEDIA_PLAYER,
            startTime = session.startPositionMs / 1000.0,
            currentTime = session.positionMs / 1000.0,
            timeListening = session.listenedMs / 1000.0,
            startedAt = session.startedAt,
            updatedAt = session.updatedAt
        )
    }

    private companion object {
        const val BATCH_SIZE = 25
        const val MAX_BATCHES = 8
    }
}

/** Decides whether a recording continues the item's latest session or starts a new one. */
internal object ListeningSessionPlanner {
    /**
     * A pause shorter than this continues the same session, so pausing to cross a road does not
     * split one sitting into many sessions; a longer break starts a new one, as other
     * Audiobookshelf apps do.
     */
    const val CONTINUE_WINDOW_MS = 10 * 60 * 1000L

    fun next(
        latest: ListeningSessionEntity?,
        itemId: String,
        originServerUrl: String,
        streamed: Boolean,
        positionMs: Long,
        durationMs: Long,
        listenedMs: Long,
        now: Long,
        newId: () -> String
    ): ListeningSessionEntity? {
        val listened = listenedMs.coerceAtLeast(0)
        val position = positionMs.coerceAtLeast(0)
        if (latest != null && latest.streamed == streamed &&
            now - latest.updatedAt in 0 until CONTINUE_WINDOW_MS
        ) {
            return latest.copy(
                positionMs = position,
                durationMs = maxOf(durationMs, latest.durationMs),
                listenedMs = latest.listenedMs + listened,
                updatedAt = now,
                dirty = true
            )
        }
        // Moving the position without playing (a seek while paused) is not a listening session.
        if (listened == 0L) return null
        return ListeningSessionEntity(
            id = newId(),
            itemId = itemId,
            originServerUrl = originServerUrl,
            streamed = streamed,
            // Approximate: at speeds other than 1x the start drifts slightly, which only affects
            // how the session is described, not the time it counts.
            startPositionMs = (position - listened).coerceAtLeast(0),
            positionMs = position,
            durationMs = durationMs,
            listenedMs = listened,
            startedAt = now - listened,
            updatedAt = now,
            dirty = true
        )
    }
}
