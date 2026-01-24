package com.mediationmixer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.mediationmixer.app.navigation.MeditationMixerNavHost
import com.meditationmixer.core.ui.theme.MeditationMixerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MeditationMixerTheme {
                MeditationMixerNavHost(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
