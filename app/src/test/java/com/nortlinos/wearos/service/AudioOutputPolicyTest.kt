package com.nortlinos.wearos.service

import android.media.AudioDeviceInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioOutputPolicyTest {
    @Test
    fun bluetoothAndWiredHeadphonesArePrivateOutputs() {
        listOf(
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_USB_HEADSET
        ).forEach { type ->
            assertTrue(AudioOutputPolicy.hasPrivateOutput(listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, type)))
        }
    }

    @Test
    fun speakerAndCallDevicesAreNotPrivateOutputs() {
        assertFalse(
            AudioOutputPolicy.hasPrivateOutput(
                listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDeviceInfo.TYPE_BLUETOOTH_SCO)
            )
        )
        assertTrue(AudioOutputPolicy.hasSpeaker(listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)))
        assertFalse(AudioOutputPolicy.hasSpeaker(emptyList()))
    }

    @Test
    fun promptsOnlyWithoutHeadphonesUntilSpeakerIsAccepted() {
        val headphones = AudioOutputState(hasPrivateOutput = true, hasSpeaker = true)
        val speakerOnly = AudioOutputState(hasPrivateOutput = false, hasSpeaker = true)
        val nothing = AudioOutputState(hasPrivateOutput = false, hasSpeaker = false)

        assertFalse(AudioOutputPolicy.shouldPrompt(headphones, speakerAccepted = false))
        assertTrue(AudioOutputPolicy.shouldPrompt(speakerOnly, speakerAccepted = false))
        assertFalse(AudioOutputPolicy.shouldPrompt(speakerOnly, speakerAccepted = true))
        // Accepting the speaker means nothing on a watch that has none.
        assertTrue(AudioOutputPolicy.shouldPrompt(nothing, speakerAccepted = true))
    }
}
