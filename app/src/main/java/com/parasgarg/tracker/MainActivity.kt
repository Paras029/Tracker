package com.parasgarg.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.parasgarg.tracker.core.designsystem.theme.TrackerTheme
import com.parasgarg.tracker.core.navigation.TrackerNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TrackerNavGraph()
                }
            }
        }
    }
}
