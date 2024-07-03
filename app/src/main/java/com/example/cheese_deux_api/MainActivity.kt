package com.example.cheese_deux_api

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.cheese_deux_api.ui.theme.Cheese_deux_apiTheme
import com.example.cheese_deux_api.component_classes.AudioClass
import com.example.cheese_deux_api.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val backgroundAudio by lazy {
        AudioClass(context = this, audioIndex = R.raw.background_audio)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this

        backgroundAudio.playLoop(volume = 0.25f)

        enableEdgeToEdge()
        setContent {
            Cheese_deux_apiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)){
                        Navigation(context = context)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        backgroundAudio.pauseLoop()
    }

    override fun onRestart() {
        super.onRestart()
        backgroundAudio.resumeLoop()
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundAudio.release()
    }
}