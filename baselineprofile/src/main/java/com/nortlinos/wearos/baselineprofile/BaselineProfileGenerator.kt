package com.nortlinos.wearos.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Records the code run on cold start and the most common path through the app: Home, the
 * server library, a list of books and a book's detail screen.
 *
 * Sign the app in to a server (with at least one book) before generating. Signed out, only
 * startup and the sign-in screen are recorded, which still covers the most important part.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = PACKAGE, includeInStartupProfile = true) {
        pressHome()
        startActivityAndWait()
        device.browseLibrary()
    }

    private fun UiDevice.browseLibrary() {
        waitForIdle()
        if (!openByText("Server library")) return
        // First library, then first book: tap the top item of each list.
        if (!tapFirstListItem()) return
        if (!tapFirstListItem()) return
        findObject(By.scrollable(true))?.fling(Direction.DOWN)
        waitForIdle()
        pressBack()
        pressBack()
        pressBack()
    }

    private fun UiDevice.openByText(text: String): Boolean {
        val list = wait(Until.findObject(By.scrollable(true)), TIMEOUT_MS) ?: return false
        val target = findObject(By.text(text)) ?: run {
            list.scroll(Direction.DOWN, 1f)
            wait(Until.findObject(By.text(text)), TIMEOUT_MS)
        } ?: return false
        target.click()
        waitForIdle()
        return true
    }

    private fun UiDevice.tapFirstListItem(): Boolean {
        wait(Until.findObject(By.scrollable(true)), TIMEOUT_MS) ?: return false
        // Lists load from the network; give the first row time to appear, then tap the middle
        // of the screen, where a Wear list centres its first real item below the header.
        Thread.sleep(LOAD_WAIT_MS)
        click(displayWidth / 2, displayHeight * 5 / 8)
        waitForIdle()
        return true
    }

    private companion object {
        const val PACKAGE = "com.nortlinos.wearos"
        const val TIMEOUT_MS = 5_000L
        const val LOAD_WAIT_MS = 2_500L
    }
}
