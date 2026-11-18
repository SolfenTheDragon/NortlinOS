package com.nortlinos.wearos.presentation

import com.nortlinos.wearos.data.local.ButtonAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ButtonCommandsTest {
    private fun resolve(action: ButtonAction, hasBook: Boolean = true, playing: Boolean = false) =
        ButtonCommands.resolve(action, hasBook, playing)

    @Test
    fun unassignedButtonIsLeftToTheSystem() {
        assertNull(resolve(ButtonAction.NONE))
        assertNull(resolve(ButtonAction.NONE, hasBook = false))
    }

    @Test
    fun playPausePausesInPlaceButStartsThroughThePlayer() {
        assertEquals(ButtonCommand.Pause, resolve(ButtonAction.PLAY_PAUSE, playing = true))
        // Starting must reach the player screen so the headphone check prompt is visible.
        assertEquals(ButtonCommand.OpenPlayer(play = true), resolve(ButtonAction.PLAY_PAUSE))
    }

    @Test
    fun skipsAreThirtySeconds() {
        assertEquals(ButtonCommand.SeekBy(-30_000), resolve(ButtonAction.SKIP_BACK))
        assertEquals(ButtonCommand.SeekBy(30_000), resolve(ButtonAction.SKIP_FORWARD))
    }

    @Test
    fun otherActionsMapDirectly() {
        assertEquals(ButtonCommand.NextChapter, resolve(ButtonAction.NEXT_CHAPTER))
        assertEquals(ButtonCommand.PreviousChapter, resolve(ButtonAction.PREVIOUS_CHAPTER))
        assertEquals(ButtonCommand.Bookmark, resolve(ButtonAction.ADD_BOOKMARK))
        assertEquals(ButtonCommand.OpenPlayer(play = false), resolve(ButtonAction.OPEN_PLAYER))
    }

    @Test
    fun everyAssignedActionNeedsALoadedBook() {
        ButtonAction.entries.filter { it != ButtonAction.NONE }.forEach {
            assertEquals(it.name, ButtonCommand.NothingLoaded, resolve(it, hasBook = false))
        }
    }

    @Test
    fun storedValuesFallBackToTheSystemDefault() {
        assertEquals(ButtonAction.SKIP_BACK, ButtonAction.fromStored("SKIP_BACK"))
        assertEquals(ButtonAction.NONE, ButtonAction.fromStored(null))
        assertEquals(ButtonAction.NONE, ButtonAction.fromStored("SOMETHING_NEWER"))
    }
}
