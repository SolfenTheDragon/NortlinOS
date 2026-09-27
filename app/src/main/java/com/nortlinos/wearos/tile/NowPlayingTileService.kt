package com.nortlinos.wearos.tile

import android.content.ComponentName
import com.nortlinos.wearos.R
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.LayoutElementBuilders.TEXT_ALIGN_CENTER
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.layout.inlineImageResource
import androidx.wear.protolayout.material3.CardDefaults.filledTonalCardColors
import androidx.wear.protolayout.material3.CardDefaults.imageBackgroundCardColors
import androidx.wear.protolayout.material3.backgroundImage
import androidx.wear.protolayout.material3.ColorScheme
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.material3.titleCard
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.argb
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.Material3TileService
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.tile
import androidx.wear.tiles.timeline
import androidx.wear.tiles.timelineEntry
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.nortlinos.wearos.presentation.MainActivity
import com.nortlinos.wearos.service.PlaybackService
import com.nortlinos.wearos.service.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * "Now playing" tile: the current or last book over its cover, how much is left, and one tap to
 * pause or resume.
 *
 * Pause is handled here, in place: the tile connects to the running playback service, pauses it
 * and redraws, without opening the app. Resume opens the app instead, because starting playback
 * from the background is restricted on Android 12+ and it must go through the headphone prompt.
 *
 * The tile has no freshness interval, so the system never wakes the app on a timer to refresh
 * it; [NowPlayingSnapshotStore] requests an update only when playback state actually changes.
 */
class NowPlayingTileService : Material3TileService(
    allowDynamicTheme = false,
    defaultColorScheme = TileColors
) {
    override suspend fun MaterialScope.tileResponse(requestParams: TileRequest): Tile {
        var snapshot = NowPlayingSnapshotStore.read(this@NowPlayingTileService)
        if (snapshot != null && snapshot.playing &&
            requestParams.currentState.lastClickableId == snapshot.pauseClickId
        ) {
            // Draw paused straight away; the service's own update follows once it has paused.
            if (pausePlayback()) snapshot = snapshot.copy(playing = false)
        }
        val cover = snapshot?.let { NowPlayingSnapshotStore.readCover(this@NowPlayingTileService, it) }
        return tile(
            timeline(timelineEntry(layout(snapshot, cover))),
            // Must be non-empty: on older tile renderers the library only finds the cover it
            // registered automatically when this version is set.
            resourcesVersion = TILE_RESOURCES_VERSION
        )
    }

    /** Pauses the playback service if it is running; false if it could not be reached. */
    private suspend fun pausePlayback(): Boolean {
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        return try {
            withTimeoutOrNull(CONTROLLER_TIMEOUT_MS) {
                val controller = future.await()
                controller.pause()
                true
            } ?: false
        } catch (_: Exception) {
            false
        } finally {
            MediaController.releaseFuture(future)
        }
    }

    private fun MaterialScope.layout(snapshot: NowPlayingSnapshot?, cover: ByteArray?) = primaryLayout(
        titleSlot = { text(getString(R.string.app_name).layoutString) },
        mainSlot = {
            if (snapshot == null) {
                text(
                    "Start a book to resume it from here".layoutString,
                    typography = Typography.BODY_MEDIUM,
                    maxLines = 3,
                    alignment = TEXT_ALIGN_CENTER
                )
            } else {
                titleCard(
                    onClick = clickable(launch(snapshot, resume = false), id = "open_player"),
                    colors = if (cover != null) imageBackgroundCardColors() else filledTonalCardColors(),
                    title = { text(snapshot.title.layoutString, maxLines = 2) },
                    content = { text(snapshot.status().layoutString, maxLines = 1) },
                    backgroundContent = cover?.let { bytes ->
                        {
                            backgroundImage(
                                imageResource(
                                    inlineImage = inlineImageResource(bytes, TileCover.SIZE_PX, TileCover.SIZE_PX)
                                ),
                                // Per book, so the renderer re-uses its copy until the book changes.
                                protoLayoutResourceId = "cover_${snapshot.itemId.hashCode()}"
                            )
                        }
                    }
                )
            }
        },
        bottomSlot = {
            val onClick = when {
                snapshot == null -> clickable(launch(null, resume = false), id = "open")
                snapshot.playing -> clickable(id = snapshot.pauseClickId)
                else -> clickable(launch(snapshot, resume = true), id = "resume")
            }
            val label = when {
                snapshot == null -> "Open"
                snapshot.playing -> "Pause"
                else -> "Resume"
            }
            textEdgeButton(onClick = onClick) { text(label.layoutString) }
        }
    )

    private fun launch(snapshot: NowPlayingSnapshot?, resume: Boolean): ActionBuilders.LaunchAction {
        val component = ComponentName(this, MainActivity::class.java)
        if (snapshot == null) return ActionBuilders.launchAction(component)
        return ActionBuilders.launchAction(
            component,
            mapOf(
                TileLaunch.EXTRA_ITEM_ID to ActionBuilders.stringExtra(snapshot.itemId),
                TileLaunch.EXTRA_ORIGIN to ActionBuilders.stringExtra(snapshot.originServerUrl),
                TileLaunch.EXTRA_RESUME to ActionBuilders.booleanExtra(resume)
            )
        )
    }
}

private const val CONTROLLER_TIMEOUT_MS = 3_000L
private const val TILE_RESOURCES_VERSION = "1"

/** The app's amber-on-black palette, so the tile looks like the app it opens. */
private val TileColors = ColorScheme(
    primary = 0xFFF4A340.argb,
    onPrimary = 0xFF000000.argb,
    primaryContainer = 0xFF5A3A0E.argb,
    onPrimaryContainer = 0xFFFFDDB5.argb,
    surfaceContainer = 0xFF211F1C.argb,
    surfaceContainerHigh = 0xFF2E2B27.argb,
    onSurface = 0xFFFFFFFF.argb,
    onSurfaceVariant = 0xFFD4C8BA.argb,
    background = 0xFF000000.argb,
    onBackground = 0xFFFFFFFF.argb
)

/** A request from the tile to open, and optionally resume, a book in the player. */
data class TileLaunch(val itemId: String, val originServerUrl: String, val resume: Boolean) {
    companion object {
        const val EXTRA_ITEM_ID = "tile_item_id"
        const val EXTRA_ORIGIN = "tile_origin_server_url"
        const val EXTRA_RESUME = "tile_resume"

        fun from(intent: android.content.Intent?): TileLaunch? {
            val itemId = intent?.getStringExtra(EXTRA_ITEM_ID) ?: return null
            return TileLaunch(
                itemId = itemId,
                originServerUrl = intent.getStringExtra(EXTRA_ORIGIN).orEmpty(),
                resume = intent.getBooleanExtra(EXTRA_RESUME, false)
            )
        }
    }
}
