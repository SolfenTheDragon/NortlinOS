package com.nortlinos.wearos.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wakes the device when the sleep timer is due.
 *
 * The countdown itself runs on a coroutine, but `delay` is driven by a monotonic clock that stops
 * advancing while the CPU is suspended. Audio offload deliberately lets the CPU suspend during
 * playback — Media3 drops its wake lock while `isSleepingForOffload()` — so a purely coroutine
 * timer can overshoot its deadline by minutes of wall time and keep playing after the listener has
 * fallen asleep. A wakeup alarm is the only scheduler that survives that suspend.
 */
interface ExpiryAlarm {    fun schedule(deadlineEpochMs: Long, onFire: () -> Unit)

    fun cancel()

    /** No-op used by unit tests, which drive the countdown coroutine directly. */
    object Disabled : ExpiryAlarm {
        override fun schedule(deadlineEpochMs: Long, onFire: () -> Unit) = Unit

        override fun cancel() = Unit
    }
}

/**
 * Wall-clock source for the sleep timer. The deadline is an absolute epoch value so it stays
 * correct across CPU suspend; injecting the clock lets tests reach a deadline without waiting.
 */
fun interface TimeSource {
    fun nowMs(): Long

    companion object {
        val System = TimeSource { java.lang.System.currentTimeMillis() }
    }
}

@Singleton
class SystemExpiryAlarm @Inject constructor(
    @ApplicationContext private val context: Context
) : ExpiryAlarm {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private var onFire: (() -> Unit)? = null
    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onFire?.invoke()
        }
    }

    private val pendingIntent: PendingIntent
        get() = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(ACTION_SLEEP_TIMER_EXPIRED).setPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    @Synchronized
    override fun schedule(deadlineEpochMs: Long, onFire: () -> Unit) {
        val manager = alarmManager ?: return
        this.onFire = onFire
        if (!registered) {
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(ACTION_SLEEP_TIMER_EXPIRED),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            registered = true
        }
        // setAndAllowWhileIdle needs no special permission and still fires during Doze. RTC_WAKEUP
        // matches the wall-clock deadline the timer stores and wakes the CPU to deliver it.
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadlineEpochMs, pendingIntent)
    }

    @Synchronized
    override fun cancel() {
        alarmManager?.cancel(pendingIntent)
        onFire = null
    }

    private companion object {
        const val ACTION_SLEEP_TIMER_EXPIRED = "com.nortlinos.wearos.SLEEP_TIMER_EXPIRED"
        const val REQUEST_CODE = 4021
    }
}
