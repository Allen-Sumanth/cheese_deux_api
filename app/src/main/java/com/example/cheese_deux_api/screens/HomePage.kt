package com.example.cheese_deux_api.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.cheese_deux_api.R
import com.example.cheese_deux_api.GameViewModel
import com.example.cheese_deux_api.component_classes.AudioClass
import com.example.cheese_deux_api.component_classes.AudioType
import com.example.cheese_deux_api.navigation.Screens
import com.example.cheese_deux_api.theme.ButtonFont
import com.example.cheese_deux_api.theme.DialogBackground
import com.example.cheese_deux_api.theme.GameOverText
import com.example.cheese_deux_api.theme.HomePageBackground
import com.example.cheese_deux_api.theme.HomePageButtonBackground
import com.example.cheese_deux_api.theme.ScoreCardBackground
import com.example.cheese_deux_api.theme.TitleColour
import com.example.cheese_deux_api.theme.anonymousProBold
import com.example.cheese_deux_api.theme.jollyLodger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomePage(
    navController: NavController,
    viewModel: GameViewModel,
    audioMap: Map<AudioType, AudioClass>,
    context: Context
) {
    DoubleBackPressToExit(context)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomePageBackground)
            .padding(25.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                //            modifier = Modifier.height(250.dp),
                text = "CHEESE",
                fontFamily = jollyLodger,
                fontSize = 100.sp,
                color = TitleColour,
                textAlign = TextAlign.Center,
            )
            Text(
                //            modifier = Modifier.height(250.dp),
                text = "CHASE",
                fontFamily = jollyLodger,
                fontSize = 100.sp,
                color = TitleColour,
                textAlign = TextAlign.Center,
            )
        }

        val cheeseTransition = rememberInfiniteTransition(label = "")
        val cheeseHeight = cheeseTransition.animateFloat(
            initialValue = -20f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cheese height"
        )

        //cheese image
        Image(
            painter = painterResource(R.drawable.cheese_icon),
            contentDescription = "Cheese Icon",
            modifier = Modifier
                .rotate(-14f)
                .size(190.dp)
                .offset(y = cheeseHeight.value.dp)
        )

        //buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //info button
            Box(contentAlignment = Alignment.Center) {
                Button(
                    onClick = {
                        viewModel.openInfoDialog = true
                        audioMap[AudioType.BUTTON]?.play(volume = 0.5f)
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomePageButtonBackground
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    border = BorderStroke(width = 5.dp, color = TitleColour)
                ) {
                }
                Text(text = "?", fontSize = 35.sp, color = ButtonFont, fontWeight = FontWeight.W900)
            }

            Button( //play button
                onClick = {
                    audioMap[AudioType.ENTER]?.play(volume = 1f)

                    navController.navigate(Screens.GamePage.route)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = HomePageButtonBackground
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                border = BorderStroke(width = 5.dp, color = TitleColour)
            ) {
                Text(
                    text = "play",
                    fontSize = 50.sp,
                    fontFamily = jollyLodger,
                    color = ButtonFont,
                    modifier = Modifier.padding(horizontal = 25.dp)
                )
            }
            //holds options
            Box {
                //Highscores button
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            viewModel.openHighScoreDialog = true
                            audioMap[AudioType.BUTTON]?.play(volume = 0.5f)
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HomePageButtonBackground
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                        border = BorderStroke(width = 5.dp, color = TitleColour)
                    ) {

                    }
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Options",
                        tint = ButtonFont,
                        modifier = Modifier.size(30.dp)
                    )
                }

            }
        }

        //HighScore Dialog
        if (viewModel.openHighScoreDialog) {
            HighScoreDialog(
                viewModel = viewModel,
                audioMap = audioMap
            )
        }

        //Game Intro Dialog
        if (viewModel.openInfoDialog) {
            GameIntro(onDismissRequest = {
                viewModel.openInfoDialog = false
                audioMap[AudioType.BUTTON]?.play(1f)
            })
        }
    }
}

@Composable
fun DoubleBackPressToExit(context: Context, enabled: Boolean = true) {
    val scope = rememberCoroutineScope()
    val isBackPressed = remember { mutableStateOf(false) }

    BackHandler(enabled && !isBackPressed.value) {
        isBackPressed.value = true
        Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        scope.launch {
            delay(2000L)
            isBackPressed.value = false
        }
    }
}

@Composable
fun HighScoreDialog(viewModel: GameViewModel, audioMap: Map<AudioType, AudioClass>) {
    val currentHighScore = viewModel.retrieveHighScore()
    Dialog(onDismissRequest = {
        viewModel.openHighScoreDialog = false
        audioMap[AudioType.BUTTON]?.play(1f)
    }) {
        Card(
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier
                .size(300.dp, 200.dp)
                .padding(10.dp, 5.dp, 10.dp, 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = DialogBackground
            ),
            border = BorderStroke(width = 10.dp, color = Color.Black),
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "HIGH SCORE",
                    fontFamily = anonymousProBold,
                    fontSize = 35.sp,
                    color = GameOverText,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {
                    //Score Card
                    Card(
                        shape = RoundedCornerShape(100),
                        colors = CardDefaults.cardColors(
                            containerColor = ScoreCardBackground
                        ),
                        border = BorderStroke(width = 6.dp, color = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(60.dp)
                    ) {
                        Text(
                            text = "$currentHighScore",
                            fontFamily = anonymousProBold,
                            fontSize = if (viewModel.gameScore > 100) 35.sp else 40.sp,
                            modifier = Modifier
                                .padding(horizontal = 0.dp, vertical = 5.dp)
                                .align(alignment = Alignment.CenterHorizontally),
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Visible
                        )
                    }
                    //reset game button
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Button(
                            onClick = {
                                viewModel.resetHighScores()
                                audioMap[AudioType.BUTTON]?.play(1f)
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(65.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HomePageButtonBackground
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                            border = BorderStroke(width = 6.dp, color = Color.Black)
                        ) {
                        }
                        Image(
                            painter = painterResource(id = R.drawable.reset_icon),
                            contentDescription = "pause symbol",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun GameIntro(onDismissRequest: () -> Unit) {
    val textData = listOf(
        "You're Jerry the mouse. The Objective of the game is to escape Tom, the cat which is hot on your trails.\n\n",
        "How is game score calculated: game score increases by 1 everytime you cross a block\n\n",
        "Tap on each track, or tilt the phone to move Jerry.\n\n",
        "Once you hit an obstacle, Tom starts getting closer to you. If you hit an obstacle again while being chased, you will be caught and the game ends.\n\n",
        "If you evade Tom for 10 blocks, Tom will go back to chasing from a distance.\n\n",
        "Obtain powerup cookies, which make you invulnerable to block obstacles for the next 10 blocks.\n\n",
        "Tread on the Speedup tiles carefully!! it speeds up your gameplay for the next 10 blocks.\n\n",
        "Collect Cheese along your path - it can be used to shoot obstacles using the Cheese shooter button on the bottom right.\n\n",
        "Cheese can also be used to revive Jerry if caught - revive option consumes 1 cheese, and hence you must have at least 1 cheese to use it.\n\n"
    )
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier
                .padding(10.dp, 5.dp, 10.dp, 10.dp)
                .height(500.dp),
            border = BorderStroke(width = 10.dp, color = Color.Black),

            ) {
            Column(
                modifier = Modifier
                    .background(DialogBackground)
                    .height(500.dp)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ABOUT",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 5.dp),
                    color = Color.LightGray,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = anonymousProBold
                )
                Text(buildAnnotatedString {
                    textData.forEach {
                        withStyle(style = SpanStyle(color = Color.LightGray)) {
                            append("-")
                            append("\t\t")
                            append(it)
                        }
                    }
                })
            }
        }
    }
}
