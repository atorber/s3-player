package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.ui.AetherViewModel
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AetherViewModel by viewModels()

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Notification permission result handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission for Android 13+ (API 33+) to show foreground media controls
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Like mainstream music players (NetEase/Spotify/Apple Music):
        // Back press on root screen moves app to background instead of stopping playback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val nav = viewModel.navState.value
                when {
                    nav.isExpandedPlayerVisible -> {
                        viewModel.setExpandedPlayerVisible(false)
                    }
                    nav.isTelemetryInspectorVisible -> {
                        viewModel.setTelemetryInspectorVisible(false)
                    }
                    nav.isMountBucketDialogVisible -> {
                        viewModel.setMountBucketDialogVisible(false)
                    }
                    viewModel.isUploadDialogVisible.value -> {
                        viewModel.setUploadDialogVisible(false)
                    }
                    viewModel.isNewFolderDialogVisible.value -> {
                        viewModel.setNewFolderDialogVisible(false)
                    }
                    nav.activeTab != 0 -> {
                        viewModel.setActiveTab(0)
                    }
                    else -> {
                        // Move task to background so playback continues seamlessly
                        moveTaskToBack(true)
                    }
                }
            }
        })

        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.audioEngine.persistQueueState()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.audioEngine.persistQueueState()
    }
}
