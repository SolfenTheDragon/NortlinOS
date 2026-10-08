package com.nortlinos.wearos.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.material3.AppScaffold
import com.nortlinos.wearos.data.local.AccentTheme
import com.nortlinos.wearos.data.local.SettingsStore
import com.nortlinos.wearos.presentation.navigation.AppNavHost
import com.nortlinos.wearos.presentation.theme.NortlinOSTheme
import com.nortlinos.wearos.service.OidcRedirectBus
import com.nortlinos.wearos.tile.TileLaunch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isAmbient by mutableStateOf(false)
    private var tileLaunch by mutableStateOf<TileLaunch?>(null)

    @Inject lateinit var oidcRedirects: OidcRedirectBus
    @Inject lateinit var settingsStore: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deliverOidcRedirect(intent)
        if (savedInstanceState == null) tileLaunch = TileLaunch.from(intent)
        lifecycle.addObserver(
            AmbientLifecycleObserver(
                this,
                object : AmbientLifecycleObserver.AmbientLifecycleCallback {
                    override fun onEnterAmbient(
                        ambientDetails: AmbientLifecycleObserver.AmbientDetails
                    ) {
                        isAmbient = true
                    }

                    override fun onExitAmbient() {
                        isAmbient = false
                    }
                }
            )
        )
        setContent {
            val accent by settingsStore.accentTheme.collectAsState(AccentTheme.AMBER)
            NortlinOSTheme(accent) {
                AppScaffold {
                    AppNavHost(
                        isAmbient = isAmbient,
                        tileLaunch = tileLaunch,
                        onTileLaunchHandled = { tileLaunch = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deliverOidcRedirect(intent)
        TileLaunch.from(intent)?.let { tileLaunch = it }
    }

    /**
     * Forwards a custom-scheme sign-in redirect to whichever attempt is waiting for it. The
     * activity is `singleTask`, so the browser delivers it here rather than to a new instance.
     */
    private fun deliverOidcRedirect(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val data = intent.data ?: return
        if (data.scheme == "nortlinos" || data.scheme == "audiobookshelf") {
            oidcRedirects.publish(data.toString())
        }
    }
}
