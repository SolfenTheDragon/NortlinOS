package com.nortlinos.wearos.service

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

data class SleepTimerState(
    val endsAtEpochMs: Long? = null,
    val remainingMs: Long = 0,
    val durationMs: Long = 0
) {
    val active: Boolean get() = endsAtEpochMs != null && remainingMs > 0
}

@Singleton
class SleepTimerManager @Inject constructor(
    private val expiryAlarm: ExpiryAlarm,
    private val timeSource: TimeSource
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(SleepTimerState())
    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var timerJob: Job? = null

    /** Guards every mutation of the timer: `start`/`cancel` run on the main thread while expiry
     *  arrives from the countdown coroutine or the alarm receiver. */
    private val lock = Any()

    /** Identifies the current run so a late alarm from a cancelled timer cannot fire. */
    private val generation = AtomicLong(0)

    val state = _state.asStateFlow()
    val expired = _expired.asSharedFlow()

    fun start(durationMs: Long) = synchronized(lock) {
        require(durationMs > 0) { "Sleep timer duration must be positive" }
        timerJob?.cancel()
        val run = generation.incrementAndGet()
        val deadline = timeSource.nowMs() + durationMs
        _state.value = SleepTimerState(
            endsAtEpochMs = deadline,
            remainingMs = durationMs,
            durationMs = durationMs
        )

        // The alarm is the authoritative expiry; the coroutine below only refreshes the countdown
        // shown on the tools page and acts as the in-process fallback when the alarm is disabled.
        expiryAlarm.schedule(deadline) { expireIfDue(deadline, run) }

        timerJob = scope.launch {
            while (isActive) {
                val remaining = SleepTimerMath.remainingMillis(
                    deadline,
                    timeSource.nowMs()
                )
                if (remaining <= 0) {
                    expireIfDue(deadline, run)
                    return@launch
                }
                if (!publishTick(deadline, durationMs, remaining, run)) return@launch
                delay(SleepTimerMath.millisUntilNextDisplayChange(remaining))
            }
        }
    }

    /**
     * Publishes a countdown tick, unless this run is already over.
     *
     * Cancellation only takes effect at the next suspension point, so a tick computed just before
     * the alarm expired the timer would otherwise land afterwards and republish a live countdown
     * for a timer that has already stopped playback. The generation check under the lock discards
     * those late ticks.
     *
     * @return false when the run has ended and the countdown should stop.
     */
    private fun publishTick(
        deadline: Long,
        durationMs: Long,
        remainingMs: Long,
        run: Long
    ): Boolean = synchronized(lock) {
        if (generation.get() != run) return@synchronized false
        _state.value = SleepTimerState(
            endsAtEpochMs = deadline,
            remainingMs = remainingMs,
            durationMs = durationMs
        )
        true
    }

    fun cancel() = synchronized(lock) {
        generation.incrementAndGet()
        timerJob?.cancel()
        timerJob = null
        expiryAlarm.cancel()
        _state.value = SleepTimerState()
    }

    /**
     * Publishes expiry at most once per run. The alarm and the countdown coroutine race to get
     * here, and a timer that was cancelled or restarted in the meantime must stay silent.
     */
    private fun expireIfDue(deadline: Long, run: Long) = synchronized(lock) {
        // The lock matters as much as the generation check: without it a stale expiry could pass
        // the check and then cancel the job, alarm and state belonging to a timer that `start`
        // had already replaced.
        if (generation.get() != run) return@synchronized
        if (SleepTimerMath.remainingMillis(deadline, timeSource.nowMs()) > 0) return@synchronized
        generation.incrementAndGet()
        timerJob?.cancel()
        timerJob = null
        expiryAlarm.cancel()
        _state.value = SleepTimerState()
        _expired.tryEmit(Unit)
    }
}

internal object SleepTimerMath {
    private const val MINUTE_MS = 60_000L

    fun remainingMillis(deadlineMs: Long, nowMs: Long): Long =
        (deadlineMs - nowMs).coerceAtLeast(0)

    /** Whole minutes shown for [remainingMs], rounded up so "1 minute" never reads as zero. */
    fun displayedMinutes(remainingMs: Long): Long =
        ((remainingMs + MINUTE_MS - 1) / MINUTE_MS).coerceAtLeast(1)

    /**
     * Delay until the *displayed* remaining time would change.
     *
     * The timer is shown in whole minutes, so waking once per second spent 59 of every 60 wakeups
     * recomputing an identical string — and each emission recomposed the player screen even while
     * the watch was idle with the display off. Sleeping to the next minute boundary instead keeps
     * the countdown exact while cutting wakeups roughly sixtyfold. The final wake lands exactly on
     * the deadline so expiry is still on time.
     */
    fun millisUntilNextDisplayChange(remainingMs: Long): Long {
        if (remainingMs <= 0) return 0
        val previousBoundary = (displayedMinutes(remainingMs) - 1) * MINUTE_MS
        return (remainingMs - previousBoundary).coerceIn(1, MINUTE_MS)
    }
}
