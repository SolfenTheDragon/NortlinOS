package com.nortlinos.wearos.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

val LocalRequestDownloadNotificationPermission =
    staticCompositionLocalOf<(action: () -> Unit) -> Unit> { { action -> action() } }

@Composable
fun ProvideDownloadNotificationPermission(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var requestInFlight by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        requestInFlight = false
    }
    val requestPermission: (() -> Unit) -> Unit = remember(context, launcher) {
        { action ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                action()
            } else {
                if (!requestInFlight) {
                    requestInFlight = true
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                action()
            }
        }
    }
    CompositionLocalProvider(
        LocalRequestDownloadNotificationPermission provides requestPermission,
        content = content
    )
}
