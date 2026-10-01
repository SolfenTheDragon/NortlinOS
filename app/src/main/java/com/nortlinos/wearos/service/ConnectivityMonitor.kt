package com.nortlinos.wearos.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import com.nortlinos.wearos.data.local.ProgressDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class ConnectivityMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val progressDao: ProgressDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    private val _online = MutableStateFlow(isValidatedNetworkAvailable())
    val online = _online.asStateFlow()
    private val _networkAvailable = MutableStateFlow(hasActiveNetwork())
    val networkAvailable = _networkAvailable.asStateFlow()
    private val workManager = WorkManager.getInstance(context)
    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * The recurring background sync is deferrable, so it also waits for a healthy battery. The
     * immediate sync deliberately does not: it is user-visible catch-up work triggered by
     * regaining connectivity, entering the foreground, or stopping playback.
     */
    private val periodicConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _networkAvailable.value = true
            _online.value = isValidatedNetworkAvailable()
            // A watch hands its default network between the phone's Bluetooth proxy, Wi-Fi and
            // LTE many times a day, and registering the callback reports the current network too.
            // Only local progress still waiting to be pushed needs the radio right now; pulling
            // other devices' progress is covered by the foreground and periodic syncs, and an
            // unconditional REPLACE here would also cancel the foreground sync started at launch.
            scope.launch {
                if (progressDao.hasDirty()) enqueueImmediateSync()
            }
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            _networkAvailable.value = hasActiveNetwork()
            _online.value =
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

        override fun onLost(network: Network) {
            _networkAvailable.value = hasActiveNetwork()
            _online.value = isValidatedNetworkAvailable()
        }
    }

    fun start() {
        connectivityManager.registerDefaultNetworkCallback(callback)
        val periodic = PeriodicWorkRequestBuilder<ProgressSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(periodicConstraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            ProgressSyncWorker.PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodic
        )
    }

    fun enqueueImmediateSync() {
        val request = OneTimeWorkRequestBuilder<ProgressSyncWorker>()
            .setConstraints(constraints)
            .build()
        // REPLACE, not KEEP or APPEND_OR_REPLACE. The worker syncs every dirty row, so it is
        // idempotent and the newest request supersedes any pending one. KEEP silently dropped the
        // final flush from onDestroy whenever a sync happened to be running, while APPEND_OR_REPLACE
        // would queue one full sync per pause/connectivity event and burn radio time re-running work
        // the latest request already covers.
        workManager.enqueueUniqueWork(
            ProgressSyncWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun isValidatedNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Whether the watch currently has any active network path at all — Wi-Fi, or the
     * Bluetooth/companion-proxy link through a paired phone — regardless of whether that path can
     * reach the public internet.
     *
     * This is deliberately coarser than [online]: Audiobookshelf is commonly self-hosted on a
     * local network with no route to the internet, so a server on that network can be perfectly
     * reachable even though the OS does not consider it "validated". Callers should use this only
     * to skip network calls that are certain to fail because there is no network interface up at
     * all (radio off, no Wi-Fi, no paired phone) — not as a general substitute for [online].
     */
    fun hasActiveNetwork(): Boolean = connectivityManager.activeNetwork != null
}
