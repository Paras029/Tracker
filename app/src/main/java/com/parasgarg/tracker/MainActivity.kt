package com.parasgarg.tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.parasgarg.tracker.core.designsystem.theme.TrackerTheme
import com.parasgarg.tracker.core.navigation.TrackerNavGraph
import com.parasgarg.tracker.core.strava.StravaAuthManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var stravaAuthManager: StravaAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        intent?.let { handleDeepLink(it) }
        setContent {
            TrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TrackerNavGraph()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data ?: return
        if (data.scheme == "tracker" && data.host == "strava") {
            val code = data.getQueryParameter("code") ?: return
            lifecycleScope.launch {
                stravaAuthManager.exchangeCode(code)
            }
        }
    }
}
