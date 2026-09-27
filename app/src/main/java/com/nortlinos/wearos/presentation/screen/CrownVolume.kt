package com.nortlinos.wearos.presentation.screen

import android.content.Context
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.wear.compose.foundation.requestFocusOnHierarchyActive

/**
 * Binds the crown (or rotating bezel) to media volume while this composable is focused.
 *
 * Wear OS media guidance treats the crown as the volume control on a player screen, and the Now
 * Playing page has nothing else to scroll. Each step goes through [AudioManager.adjustStreamVolume]
 * with `FLAG_SHOW_UI`, so the system's own volume overlay gives the visual feedback and no extra
 * UI or polling is needed here; a clock-tick haptic confirms each step.
 */
fun Modifier.crownVolume(): Modifier = composed {
    val context = LocalContext.current
    val view = LocalView.current
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val accumulator = remember { CrownVolumeAccumulator() }
    this
        .onRotaryScrollEvent { event ->
            val steps = accumulator.add(event.verticalScrollPixels)
            if (steps != 0) {
                val direction = if (steps > 0) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
                repeat(kotlin.math.abs(steps)) {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        direction,
                        AudioManager.FLAG_SHOW_UI
                    )
                }
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
            true
        }
        .requestFocusOnHierarchyActive()
        .focusable()
}

/**
 * Turns raw rotary pixels into whole volume steps. Mapping every event to a step makes a smooth
 * crown far too sensitive (one flick would jump from silent to maximum), so pixels accumulate
 * until a step's worth has been turned, and reversing direction discards the leftover.
 */
internal class CrownVolumeAccumulator(private val pixelsPerStep: Float = PIXELS_PER_STEP) {
    private var pending = 0f

    fun add(pixels: Float): Int {
        if (pixels == 0f) return 0
        if (pending != 0f && (pending > 0f) != (pixels > 0f)) pending = 0f
        pending += pixels
        val steps = (pending / pixelsPerStep).toInt()
        pending -= steps * pixelsPerStep
        return steps
    }

    companion object {
        const val PIXELS_PER_STEP = 40f
    }
}

@Composable
internal fun isLargeRoundScreen(): Boolean =
    androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= LARGE_SCREEN_WIDTH_DP

/** Official Wear OS breakpoint between small (~192 dp) and large (~227 dp+) displays. */
internal const val LARGE_SCREEN_WIDTH_DP = 225
