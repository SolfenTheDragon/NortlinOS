package com.nortlinos.wearos.service

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutionException

/** Suspends until the future completes; cancelling the coroutine cancels the future. */
suspend fun <T> ListenableFuture<T>.await(): T {
    if (isDone) return unwrap()
    return suspendCancellableCoroutine { continuation ->
        addListener({
            runCatching { unwrap() }
                .onSuccess { continuation.resumeWith(Result.success(it)) }
                .onFailure { continuation.resumeWith(Result.failure(it)) }
        }, MoreExecutors.directExecutor())
        continuation.invokeOnCancellation { cancel(false) }
    }
}

private fun <T> ListenableFuture<T>.unwrap(): T = try {
    get()
} catch (e: ExecutionException) {
    throw e.cause ?: e
}
