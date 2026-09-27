package com.nortlinos.wearos.tile

import android.content.Context
import androidx.core.content.edit
import androidx.wear.tiles.TileService

/**
 * What the Now Playing tile shows: the current or most recent book and whether it is playing.
 *
 * The tile renderer runs in the system process and asks for a layout whenever the tile becomes
 * visible, so the answer must be instant and must not touch the network or wake the database.
 * [PlaybackService][com.nortlinos.wearos.service.PlaybackService] therefore writes this small
 * snapshot to preferences at the moments it already handles (play, pause, book change, end),
 * and the tile only reads it.
 */
data class NowPlayingSnapshot(
    val itemId: String,
    val originServerUrl: String,
    val title: String,
    val author: String?,
    val playing: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    /** Item whose cover is saved as [NowPlayingSnapshotStore.coverFile]; null while none is. */
    val coverItemId: String? = null,
    /** When playback last started; changes on every resume. */
    val playingSince: Long = 0
) {
    val hasCover: Boolean get() = coverItemId == itemId

    /**
     * Click ID of the tile's Pause button. Tied to this run of playback because the renderer can
     * report the same last-clicked ID on later requests, and a stale ID must never pause a book
     * the user has since resumed.
     */
    val pauseClickId: String get() = "pause:$itemId:$playingSince"

    /** Short status line such as "Playing · 42%" or "Paused · 3h 10m left". */
    fun status(): String {
        val state = if (playing) "Playing" else "Paused"
        if (durationMs <= 0) return state
        val remainingMinutes = ((durationMs - positionMs).coerceAtLeast(0) + 59_999) / 60_000
        val remaining = when {
            remainingMinutes >= 60 -> "${remainingMinutes / 60}h ${remainingMinutes % 60}m left"
            else -> "${remainingMinutes}m left"
        }
        return "$state \u00b7 $remaining"
    }

    /**
     * Tile refreshes cost a render in the system UI, so only changes the tile actually shows
     * trigger one. Position alone does not: it is written on every pause anyway, which is when
     * the "time left" text next matters.
     */
    fun needsTileRefresh(previous: NowPlayingSnapshot?): Boolean =
        previous == null ||
            previous.itemId != itemId ||
            previous.originServerUrl != originServerUrl ||
            previous.title != title ||
            previous.playing != playing ||
            previous.coverItemId != coverItemId ||
            (!playing && previous.positionMs != positionMs)
}

object NowPlayingSnapshotStore {
    private const val PREFS = "now_playing_tile"
    private const val KEY_ITEM_ID = "item_id"
    private const val KEY_ORIGIN = "origin_server_url"
    private const val KEY_TITLE = "title"
    private const val KEY_AUTHOR = "author"
    private const val KEY_PLAYING = "playing"
    private const val KEY_POSITION = "position_ms"
    private const val KEY_DURATION = "duration_ms"
    private const val KEY_COVER_ITEM_ID = "cover_item_id"
    private const val KEY_PLAYING_SINCE = "playing_since"
    private const val COVER_FILE = "tile_cover.jpg"

    /** A small JPEG of the current book's cover for the tile, written by the playback service. */
    fun coverFile(context: Context): java.io.File = java.io.File(context.filesDir, COVER_FILE)

    /** The saved cover bytes if they belong to [snapshot]'s book, else null. */
    fun readCover(context: Context, snapshot: NowPlayingSnapshot): ByteArray? {
        if (!snapshot.hasCover) return null
        return runCatching { coverFile(context).readBytes() }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    fun read(context: Context): NowPlayingSnapshot? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val itemId = prefs.getString(KEY_ITEM_ID, null) ?: return null
        return NowPlayingSnapshot(
            itemId = itemId,
            originServerUrl = prefs.getString(KEY_ORIGIN, null).orEmpty(),
            title = prefs.getString(KEY_TITLE, null).orEmpty(),
            author = prefs.getString(KEY_AUTHOR, null),
            playing = prefs.getBoolean(KEY_PLAYING, false),
            positionMs = prefs.getLong(KEY_POSITION, 0),
            durationMs = prefs.getLong(KEY_DURATION, 0),
            coverItemId = prefs.getString(KEY_COVER_ITEM_ID, null),
            playingSince = prefs.getLong(KEY_PLAYING_SINCE, 0)
        )
    }

    /** Saves [snapshot] and asks the system to re-render the tile if anything visible changed. */
    fun write(context: Context, snapshot: NowPlayingSnapshot) {
        val previous = read(context)
        if (previous == snapshot) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            putString(KEY_ITEM_ID, snapshot.itemId)
            putString(KEY_ORIGIN, snapshot.originServerUrl)
            putString(KEY_TITLE, snapshot.title)
            putString(KEY_AUTHOR, snapshot.author)
            putBoolean(KEY_PLAYING, snapshot.playing)
            putLong(KEY_POSITION, snapshot.positionMs)
            putLong(KEY_DURATION, snapshot.durationMs)
            putString(KEY_COVER_ITEM_ID, snapshot.coverItemId)
            putLong(KEY_PLAYING_SINCE, snapshot.playingSince)
        }
        if (snapshot.needsTileRefresh(previous)) requestTileUpdate(context)
    }

    private fun requestTileUpdate(context: Context) {
        TileService.getUpdater(context).requestUpdate(NowPlayingTileService::class.java)
    }
}
