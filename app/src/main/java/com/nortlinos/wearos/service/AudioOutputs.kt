package com.nortlinos.wearos.service

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Knows which audio outputs the watch can currently play through, and how to send the user to
 * the system screen that connects headphones.
 *
 * Listening on a watch speaker is rarely what the user wants (it is quiet and public), so the
 * player asks before starting playback without headphones instead of silently using the speaker
 * or silently refusing to play. Nothing here polls: [headsetConnected] only registers an
 * [AudioDeviceCallback] while someone is collecting it.
 */
@Singleton
class AudioOutputs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(AudioManager::class.java)

    fun current(): AudioOutputState {
        val types = outputTypes()
        return AudioOutputState(
            hasPrivateOutput = AudioOutputPolicy.hasPrivateOutput(types),
            hasSpeaker = context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT) &&
                AudioOutputPolicy.hasSpeaker(types)
        )
    }

    /** Emits whether headphones are connected now, then again whenever that changes. */
    fun headsetConnected(): Flow<Boolean> = callbackFlow {
        val callback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
                trySend(AudioOutputPolicy.hasPrivateOutput(outputTypes()))
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
                trySend(AudioOutputPolicy.hasPrivateOutput(outputTypes()))
            }
        }
        trySend(AudioOutputPolicy.hasPrivateOutput(outputTypes()))
        audioManager.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { audioManager.unregisterAudioDeviceCallback(callback) }
    }.distinctUntilChanged()

    /**
     * Opens the system output switcher (Wear OS 5+), falling back to the Bluetooth settings
     * screen filtered to audio devices. Returns false when neither screen exists.
     */
    fun launchOutputSwitcher(activityContext: Context): Boolean {
        val candidates = listOf(
            Intent(ACTION_MEDIA_OUTPUT)
                .putExtra(EXTRA_PACKAGE_NAME, context.packageName),
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                .putExtra(EXTRA_CONNECTION_ONLY, true)
                .putExtra(EXTRA_CLOSE_ON_CONNECT, true)
                .putExtra(EXTRA_FILTER_TYPE, FILTER_TYPE_AUDIO)
        )
        return candidates.any { intent ->
            try {
                activityContext.startActivity(intent)
                true
            } catch (_: ActivityNotFoundException) {
                false
            } catch (_: SecurityException) {
                false
            }
        }
    }

    private fun outputTypes(): List<Int> =
        audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map { it.type }

    private companion object {
        const val ACTION_MEDIA_OUTPUT = "com.android.settings.panel.action.MEDIA_OUTPUT"
        const val EXTRA_PACKAGE_NAME = "com.android.settings.panel.extra.PACKAGE_NAME"
        const val EXTRA_CONNECTION_ONLY = "EXTRA_CONNECTION_ONLY"
        const val EXTRA_CLOSE_ON_CONNECT = "EXTRA_CLOSE_ON_CONNECT"
        const val EXTRA_FILTER_TYPE = "android.bluetooth.devicepicker.extra.FILTER_TYPE"
        const val FILTER_TYPE_AUDIO = 1
    }
}

data class AudioOutputState(val hasPrivateOutput: Boolean, val hasSpeaker: Boolean)

/** Pure device-type rules behind [AudioOutputs], kept separate so they can be unit tested. */
object AudioOutputPolicy {
    private val PRIVATE_OUTPUT_TYPES = setOf(
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_BLE_HEADSET,
        AudioDeviceInfo.TYPE_BLE_SPEAKER,
        AudioDeviceInfo.TYPE_BLE_BROADCAST,
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_HEARING_AID
    )

    fun hasPrivateOutput(types: Collection<Int>): Boolean = types.any { it in PRIVATE_OUTPUT_TYPES }

    fun hasSpeaker(types: Collection<Int>): Boolean = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER in types

    /**
     * Whether starting playback should first ask about headphones. Once the user has chosen the
     * speaker it is not asked again for the rest of the app session.
     */
    fun shouldPrompt(state: AudioOutputState, speakerAccepted: Boolean): Boolean =
        !state.hasPrivateOutput && !(speakerAccepted && state.hasSpeaker)
}
