package com.example.cheese_deux_api.navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cheese_deux_api.R
import com.example.cheesechase.GameViewModel
import com.example.cheese_deux_api.component_classes.AudioClass
import com.example.cheese_deux_api.component_classes.AudioType
import com.example.cheesechase.navigation.Screens
import com.example.cheesechase.screens.GamePage
import com.example.cheesechase.screens.HomePage

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(context: Context) {
    val viewModel = hiltViewModel<GameViewModel>()
    val navController = rememberNavController()

    //region audio initialisation
    val buttonAudio = AudioClass(context = context, audioIndex = R.raw.click_audio)
    val enterAudio = AudioClass(context = context, audioIndex = R.raw.enter_gamepage)
    val homePageAudioMap = mapOf(
        AudioType.BUTTON to buttonAudio,
        AudioType.ENTER to enterAudio,
    )

    val cheeseCollectAudio = AudioClass(context = context, audioIndex = R.raw.cheese_collect_final)
    val cheeseShootAudio = AudioClass(context = context, audioIndex = R.raw.cheese_shoot)
    val cheeseReviveAudio = AudioClass(context = context, audioIndex = R.raw.cheese_revive)
    val invulnerabilityAudio = AudioClass(context = context, audioIndex = R.raw.invulnerability)
    val speedUpAudio = AudioClass(context = context, audioIndex = R.raw.speedup)
    val gameOverAudio = AudioClass(context = context, audioIndex = R.raw.game_over)
    val clickAudio = AudioClass(context = context, audioIndex = R.raw.click_audio)
    val firstHitAudio = AudioClass(context = context, audioIndex = R.raw.first_hit)
    val gamePageAudioMap = mapOf(
        AudioType.CHEESE_COLLECT to cheeseCollectAudio,
        AudioType.CHEESE_SHOOT to cheeseShootAudio,
        AudioType.CHEESE_REVIVE to cheeseReviveAudio,
        AudioType.INVULNERABILITY to invulnerabilityAudio,
        AudioType.SPEEDUP to speedUpAudio,
        AudioType.GAME_OVER to gameOverAudio,
        AudioType.BUTTON to clickAudio,
        AudioType.FIRST_HIT to firstHitAudio
    )
    //endregion

    NavHost(navController = navController, startDestination = Screens.HomePage.route) {

        composable(route = Screens.HomePage.route) {
            HomePage(navController = navController, viewModel = viewModel, audioMap = homePageAudioMap)
        }

        composable(route = Screens.GamePage.route) {
            GamePage(
                navController = navController,
                viewModel = viewModel,
                audioMap = gamePageAudioMap,
                context = context
            )
        }
    }
}