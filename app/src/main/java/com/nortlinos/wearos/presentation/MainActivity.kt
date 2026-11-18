package com.nortlinos.wearos.presentation

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.material3.AppScaffold
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nortlinos.wearos.data.local.AccentTheme
import com.nortlinos.wearos.data.local.ButtonAction
import com.nortlinos.wearos.data.local.SettingsStore
import com.nortlinos.wearos.presentation.ProvideDownloadNotificationPermission
import com.nortlinos.wearos.presentation.navigation.AppNavHost
import com.nortlinos.wearos.presentation.theme.NortlinOSTheme
import com.nortlinos.wearos.service.OidcRedirectBus
import com.nortlinos.wearos.presentation.viewmodel.PlayerViewModel
import com.nortlinos.wearos.tile.TileLaunch
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isAmbient by mutableStateOf(false)
    private var tileLaunch by mutableStateOf<TileLaunch?>(null)

    @Inject lateinit var oidcRedirects: OidcRedirectBus
    @Inject lateinit var settingsStore: SettingsStore

    /**
     * The same instance the navigation host uses: both are scoped to this activity, so a button
     * press acts on exactly the book the player screen shows.
     */
    private val playerViewModel: PlayerViewModel by viewModels()

    /** Button assignments, kept current while the activity is started; read on the main thread. */
    private var buttonActions: Map<Int, ButtonAction> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deliverOidcRedirect(intent)
        if (savedInstanceState == null) tileLaunch = TileLaunch.from(intent)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsStore.buttonActions.collect { buttonActions = it }
            }
        }
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
            val customAccent by settingsStore.customAccentColor.collectAsState(null)
            NortlinOSTheme(accent, customAccent) {
                ProvideDownloadNotificationPermission {
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
    }

    /**
     * Multifunction buttons only reach an app while it is in the foreground (Wear OS keeps the
     * power button for itself). An unassigned button falls through to the system untouched.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val action = buttonActions[keyCode] ?: return super.onKeyDown(keyCode, event)
        // Holding a button repeats key-downs; act once per press but keep swallowing the repeats
        // so the system's own long-press action does not fire on top of ours.
        if (event.repeatCount == 0) runButtonAction(action)
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean =
        if (keyCode in buttonActions) true else super.onKeyUp(keyCode, event)

    private fun runButtonAction(action: ButtonAction) {
        val now = playerViewModel.nowPlaying.value
        val command = ButtonCommands.resolve(action, hasBook = now.itemId != null, playing = now.playing)
            ?: return
        val handled = when (command) {
            ButtonCommand.NothingLoaded -> false
            ButtonCommand.Pause -> { playerViewModel.toggle(); true }
            is ButtonCommand.SeekBy -> { playerViewModel.seekBy(command.deltaMs); true }
            ButtonCommand.NextChapter -> { playerViewModel.nextChapter(); true }
            ButtonCommand.PreviousChapter -> { playerViewModel.previousChapter(); true }
            ButtonCommand.Bookmark -> { playerViewModel.addBookmark(); true }
            is ButtonCommand.OpenPlayer -> {
                // The same path as a tile tap: navigates to the player and, when starting,
                // runs the headphone check there where its prompt can be seen.
                tileLaunch = TileLaunch(
                    itemId = checkNotNull(now.itemId),
                    originServerUrl = now.originServerUrl.orEmpty(),
                    resume = command.play
                )
                true
            }
        }
        // A press has no on-screen result when the player is not showing, so the wrist confirms
        // it, and a different buzz says there was nothing to act on.
        window.decorView.performHapticFeedback(
            if (handled) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT
        )
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
