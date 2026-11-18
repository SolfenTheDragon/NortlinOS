package com.nortlinos.wearos.presentation

import android.content.Context
import androidx.wear.input.WearableButtons
import com.nortlinos.wearos.data.local.ButtonAction
import com.nortlinos.wearos.data.local.STEM_KEY_CODES

/** A multifunction button present on this watch, with the system's description of where it is. */
data class HardwareButton(val keyCode: Int, val label: String)

/**
 * Which multifunction buttons this watch has. Many watches have none beyond the power button, in
 * which case the hardware button settings are not shown at all.
 */
object HardwareButtons {
    fun available(context: Context): List<HardwareButton> {
        // The count includes the power button, which apps cannot use.
        if (WearableButtons.getButtonCount(context) <= 1) return emptyList()
        return STEM_KEY_CODES.mapIndexedNotNull { index, keyCode ->
            WearableButtons.getButtonInfo(context, keyCode) ?: return@mapIndexedNotNull null
            HardwareButton(
                keyCode = keyCode,
                // The system's own placement description, e.g. "Top right", so the setting
                // names the button the way the watch does.
                label = WearableButtons.getButtonLabel(context, keyCode)
                    ?.toString()
                    ?.takeIf { it.isNotBlank() }
                    ?: "Button ${index + 1}"
            )
        }
    }
}

/** What a button press should do, given the configured action and what is loaded. */
sealed interface ButtonCommand {
    data object Pause : ButtonCommand
    data class SeekBy(val deltaMs: Long) : ButtonCommand
    data object NextChapter : ButtonCommand
    data object PreviousChapter : ButtonCommand
    data object Bookmark : ButtonCommand
    /** Opens the player on the loaded book; with [play], also starts it there. */
    data class OpenPlayer(val play: Boolean) : ButtonCommand
    /** The action needs a loaded book and there is none. */
    data object NothingLoaded : ButtonCommand
}

object ButtonCommands {
    const val SKIP_MS = 30_000L

    fun resolve(
        action: ButtonAction,
        hasBook: Boolean,
        playing: Boolean
    ): ButtonCommand? {
        if (action == ButtonAction.NONE) return null
        if (!hasBook) return ButtonCommand.NothingLoaded
        return when (action) {
            ButtonAction.NONE -> null
            // Starting goes through the player screen, like the tile's Resume, so the
            // headphone check can show its prompt; pausing needs no screen.
            ButtonAction.PLAY_PAUSE ->
                if (playing) ButtonCommand.Pause else ButtonCommand.OpenPlayer(play = true)
            ButtonAction.SKIP_BACK -> ButtonCommand.SeekBy(-SKIP_MS)
            ButtonAction.SKIP_FORWARD -> ButtonCommand.SeekBy(SKIP_MS)
            ButtonAction.PREVIOUS_CHAPTER -> ButtonCommand.PreviousChapter
            ButtonAction.NEXT_CHAPTER -> ButtonCommand.NextChapter
            ButtonAction.ADD_BOOKMARK -> ButtonCommand.Bookmark
            ButtonAction.OPEN_PLAYER -> ButtonCommand.OpenPlayer(play = false)
        }
    }
}
