package com.nortlinos.wearos

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.nortlinos.wearos.data.repository.SessionRepository
import com.nortlinos.wearos.service.ConnectivityMonitor
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class NortlinOSApp : Application(), DefaultLifecycleObserver, ImageLoaderFactory {
    @Inject lateinit var connectivityMonitor: ConnectivityMonitor
    @Inject lateinit var sessionRepository: SessionRepository
    @Inject lateinit var okHttpClient: OkHttpClient
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super<Application>.onCreate()
        connectivityMonitor.start()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        applicationScope.launch {
            connectivityMonitor.networkAvailable.collect { available ->
                if (!available) return@collect
                for (attempt in 0 until CONNECTION_RETRIES) {
                    val result = sessionRepository.reconnectSavedSession()
                    if (result != SessionRepository.ValidationResult.UNREACHABLE) break
                    if (attempt < CONNECTION_RETRIES - 1) delay(CONNECTION_RETRY_DELAY_MS)
                }
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (sessionRepository.session.value != null) {
            connectivityMonitor.enqueueImmediateSync()
        }
        applicationScope.launch { sessionRepository.reconnectSavedSession() }
    }

    /**
     * Covers share the app's OkHttp connection pool, and ignore HTTP cache headers.
     *
     * Audiobookshelf serves covers with no Cache-Control or validators, so a header-respecting
     * cache treats every disk hit as stale and re-downloads the image each time it leaves the
     * memory cache (every cold start, or after scrolling far). Serving from disk instead keeps
     * the radio off while browsing. The trade-off: a cover replaced on the server keeps its old
     * image on the watch until Coil's disk cache evicts it or the app's cache is cleared.
     * Covers are opaque, so RGB_565 halves each decoded bitmap's memory with no visible cost.
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient(okHttpClient)
        .respectCacheHeaders(false)
        .allowRgb565(true)
        .crossfade(false)
        .build()

    private companion object {
        const val CONNECTION_RETRIES = 3
        const val CONNECTION_RETRY_DELAY_MS = 2_000L
    }
}
