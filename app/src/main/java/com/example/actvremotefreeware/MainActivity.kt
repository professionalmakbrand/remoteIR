package com.example.actvremotefreeware

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.actvremotefreeware.theme.ACTVRemoteFreewareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcutRemoteId = intent?.getStringExtra("shortcut_remote_id")
        val shortcutRemoteType = intent?.getStringExtra("shortcut_remote_type")

        enableEdgeToEdge()
        setContent {
            ACTVRemoteFreewareTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        initialRemoteId = shortcutRemoteId,
                        initialRemoteType = shortcutRemoteType
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
