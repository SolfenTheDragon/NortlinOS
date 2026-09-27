package com.nortlinos.wearos.service

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Admission control shared by every running [DownloadWorker] in the process.
 *
 * Any number of books can be requested at once - picking a whole series queues dozens - but only
 * [MAX_CONCURRENT_DOWNLOADS] transfer at a time. Serialising matters on a watch for two reasons:
 * parallel transfers split the same narrow radio link, so they finish no sooner while holding the
 * radio awake for longer; and each worker sizes its download against the free space it observes,
 * which only stays truthful while one worker is writing.
 *
 * A worker that cannot get a slot waits only briefly and then gives up its run, so waiting books
 * are never promoted to foreground services they do not need. WorkManager retries them, and they
 * take a slot once one frees up.
 */
@Singleton
class DownloadQueue @Inject constructor() {
    private val slots = Semaphore(MAX_CONCURRENT_DOWNLOADS)

    /** Books transferring right now, for callers that surface queue depth. */
    val activeCount: Int get() = MAX_CONCURRENT_DOWNLOADS - slots.availablePermits

    /**
     * Runs [block] once a transfer slot is free and returns its result, or returns `null` if no
     * slot came free within [waitMillis]. [onWait] runs first if the caller has to queue at all.
     * The slot is always returned, including when a transfer throws or the worker is stopped.
     */
    suspend fun <T> withSlot(
        waitMillis: Long = DEFAULT_WAIT_MILLIS,
        onWait: suspend () -> Unit = {},
        block: suspend () -> T
    ): T? {
        if (!slots.tryAcquire()) {
            onWait()
            // Semaphore.acquire() hands back nothing when cancelled, so timing out here cannot
            // leak the permit it was waiting for.
            withTimeoutOrNull(waitMillis) { slots.acquire() } ?: return null
        }
        return try {
            block()
        } finally {
            slots.release()
        }
    }

    companion object {
        const val MAX_CONCURRENT_DOWNLOADS = 1

        /**
         * Long enough to absorb the handover between one book finishing and the next starting,
         * short enough to stay well inside the ten minutes WorkManager allows a worker that has
         * not gone foreground.
         */
        const val DEFAULT_WAIT_MILLIS = 90_000L
    }
}
