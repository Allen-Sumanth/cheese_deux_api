package com.example.cheesechase.screens

import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.cheese_deux_api.component_classes.AudioClass
import com.example.cheese_deux_api.component_classes.AudioType
import com.example.cheesechase.Constants
import com.example.cheesechase.GameStatus
import com.example.cheesechase.GameViewModel
import com.example.cheesechase.R
import com.example.cheesechase.ui.theme.ButtonFont
import com.example.cheesechase.ui.theme.DisabledReviveButton
import com.example.cheesechase.ui.theme.GameOverBackground
import com.example.cheesechase.ui.theme.GameOverText
import com.example.cheesechase.ui.theme.GamePageBackground
import com.example.cheesechase.ui.theme.HomePageButtonBackground
import com.example.cheesechase.ui.theme.ScoreCardBackground
import com.example.cheesechase.ui.theme.TitleColour
import com.example.cheesechase.ui.theme.TrackColor
import com.example.cheesechase.ui.theme.anonymousProBold

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GamePage(
    viewModel: GameViewModel,
    navController: NavController,
    audioMap: Map<AudioType, AudioClass>,
    context: Context,
) {
    BackHandler {//handles back button press
        viewModel.pauseGame()
        navController.navigate("homepage")
    }

    //region bitmaps
    val catDrawableBitmap = ImageBitmap.imageResource(id = R.drawable.cat_icon)
    val mouseDrawableBitmap = ImageBitmap.imageResource(id = R.drawable.mouse_icon)
    val mouseHitDrawableBitmap = ImageBitmap.imageResource(id = R.drawable.mouse_icon_firsthit)
    val mouseInvulnerableBitmap = ImageBitmap.imageResource(id = R.drawable.invulnerable_mouse_icon)
    val obstacleDrawableBitmap = ImageBitmap.imageResource(id = R.drawable.obstacle)
    val invulnerabilityCookieBitmap =
        ImageBitmap.imageResource(id = R.drawable.invulnerability_cookie)
    val speedUpBitmap = ImageBitmap.imageResource(id = R.drawable.speedup_icon)
    val cheeseBitmap = ImageBitmap.imageResource(id = R.drawable.cheese_icon)
    //endregion

    //region positioning cat and mouse
    var centreCoords by remember {
        mutableStateOf(listOf(0f, 500f, 0f))
    }//to position mouse and cat
    val mouseXCoords by animateIntAsState(//to move mouse when tapped
        targetValue = centreCoords[viewModel.state.currentTrack].minus(
            85
        ).toInt(),
        label = "mouse x coordinates",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val catXCoords by animateIntAsState(//to make cat follow mouse
        targetValue = centreCoords[viewModel.state.currentTrack].minus(
            100
        ).toInt(),
        label = "cat x coordinates",
        animationSpec = tween(durationMillis = 900, delayMillis = 100)
    )
    val catYOffset by animateIntAsState(
        targetValue = if (viewModel.state.firstHit) {
            Constants.CAT_VISIBLE
        } else {
            Constants.CAT_HIDDEN

        },
        label = "cat y offset",
        animationSpec = tween(durationMillis = 800)
    )
    //endregion

    //calculating current velocity
    val currentVelocity by remember {
        derivedStateOf {
            (viewModel.gameTracker.times(0.005f) + Constants.INIT_VELOCITY + viewModel.speedupVelocity).dp
        }
    }

    // region Content Box - contains everything in the screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = GamePageBackground),
        contentAlignment = Alignment.TopCenter,
    ) {
        /**
        Drawing :
        1. Tracks
        2. Markers
        3. Cat
        4. Mouse
         * */
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { (offsetX, offsetY) ->
                        viewModel.moveMouseLaneFromPointer(
                            xInp = offsetX,
                            yInp = offsetY,
                            width = size.width.toFloat(),
                            height = size.height.toFloat()
                        )
                    }
                },
        ) {
            val width = size.width
            val height = size.height

            val centre1 = width.times(0.1875f)
            val centre2 = width.times(0.5f)
            val centre3 = width.times(0.8125f)
            centreCoords = listOf(centre1, centre2, centre3)

            drawLine(//draws the track 1
                color = TrackColor,
                start = Offset(x = centre1, y = 0f),
                end = Offset(x = centre1, y = height),
                strokeWidth = width.times(0.25f)
            )
            drawLine(//draws the track 2
                color = TrackColor,
                start = Offset(x = centre2, y = 0f),
                end = Offset(x = centre2, y = height),
                strokeWidth = width.times(0.25f)
            )
            drawLine(//draws the track 3
                color = TrackColor,
                start = Offset(x = centre3, y = 0f),
                end = Offset(x = centre3, y = height),
                strokeWidth = width.times(0.25f)
            )

            drawMarkers(//track markers(yellow)
                viewModel = viewModel,
                velocity = currentVelocity,
                centreCoords = centreCoords
            )

            drawObstacles(
                viewModel = viewModel,
                imageBitmap = obstacleDrawableBitmap,
                centreCoords = centreCoords,
                velocity = currentVelocity
            )

            drawInvulnerabilityCookies(
                viewModel = viewModel,
                imageBitmap = invulnerabilityCookieBitmap,
                centreCoords = centreCoords,
                velocity = currentVelocity
            )

            drawCheese(
                viewModel = viewModel,
                imageBitmap = cheeseBitmap,
                centreCoords = centreCoords,
                velocity = currentVelocity
            )

            drawSpeedUps(
                viewModel = viewModel,
                imageBitmap = speedUpBitmap,
                centreCoords = centreCoords,
                velocity = currentVelocity
            )

            drawCat(
                catDrawableBitmap = catDrawableBitmap,
                catXCoords = catXCoords,
                viewModel = viewModel,
                catYOffset = catYOffset
            )

            drawMouse(
                mouseDrawableBitmap = mouseDrawableBitmap,
                mouseHitDrawableBitmap = mouseHitDrawableBitmap,
                mouseInvulnerableBitmap = mouseInvulnerableBitmap,
                mouseXCoords = mouseXCoords,
                viewModel = viewModel,
                audioMap = audioMap,
                context = context
            )
        }

        /**
        Score Card, menu, cheese shooter
         * */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card( //Score Card
                    shape = RoundedCornerShape(100),
                    colors = CardDefaults.cardColors(
                        containerColor = ScoreCardBackground
                    ),
                    border = BorderStroke(width = 8.dp, color = Color.Black),
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth(0.9f)
                ) {
                    Text(
                        text = "score : ${viewModel.gameScore}",
                        fontFamily = anonymousProBold,
                        fontSize = 35.sp,
                        modifier = Modifier
                            .padding(horizontal = 0.dp, vertical = 5.dp)
                            .align(alignment = Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
                Button(
                    onClick = {
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScoreCardBackground
                    ),
                    border = BorderStroke(width = 6.dp, color = TitleColour),
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .fillMaxWidth(0.35f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = painterResource(R.drawable.cheese_icon),
                            contentDescription = "Cheese Icon",
                            modifier = Modifier
                                .size(30.dp)
                        )
                        Text(
                            text = "${viewModel.hackerPlusState.cheeseCount}",
                            fontFamily = anonymousProBold,
                            fontSize = 30.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(start = 5.dp)
                        )
                    }
                }
            }
            Row(//bottom menu
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(//reset game button
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.resetGame()
                            audioMap[AudioType.BUTTON]?.play(1f)
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
                    Image(
                        painter = painterResource(id = R.drawable.reset_icon),
                        contentDescription = "pause symbol",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Box(//pause/play game button
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Button(
                        onClick = {
                            if (viewModel.state.gameStatus == GameStatus.PLAYING) {
                                viewModel.pauseGame()
                            } else {
                                viewModel.startGame()
                            }
                            audioMap[AudioType.BUTTON]?.play(1f)
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
                    this@Row.AnimatedVisibility(
                        visible = viewModel.state.gameStatus == GameStatus.PLAYING,
                        enter = slideInHorizontally() + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.pause_symbol),
                            contentDescription = "pause symbol",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    this@Row.AnimatedVisibility(
                        visible = viewModel.state.gameStatus == GameStatus.PAUSED || viewModel.state.gameStatus == GameStatus.STOPPED,
                        enter = slideInHorizontally() + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "play button",
                            tint = ButtonFont,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                }
                Box(//cheese shooter
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Button(
                        onClick = {
                            if (viewModel.hackerPlusState.cheeseCount > 0) {
                                viewModel.shootCheese()
                                audioMap[AudioType.CHEESE_SHOOT]?.play(1f)
                            }
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
                    Image(
                        painter = painterResource(R.drawable.cheese_icon),
                        contentDescription = "Cheese Icon",
                        modifier = Modifier
                            .size(35.dp)
                    )
                }
            }
        }

        /**
         * Game over card
         */
        if (viewModel.openGameOverDialog) {
            Dialog(onDismissRequest = {
                viewModel.openGameOverDialog = false
                viewModel.resetGame()
            }) {
                Card(
                    shape = RoundedCornerShape(40.dp),
                    modifier = Modifier
                        .size(300.dp)
                        .padding(10.dp, 5.dp, 10.dp, 10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GameOverBackground
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
                            text = "GAME OVER",
                            fontFamily = anonymousProBold,
                            fontSize = 45.sp,
                            color = GameOverText,
                            maxLines = 1,
                            overflow = TextOverflow.Visible
                        )
                        Card( //Score Card
                            shape = RoundedCornerShape(100),
                            colors = CardDefaults.cardColors(
                                containerColor = ScoreCardBackground
                            ),
                            border = BorderStroke(width = 6.dp, color = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Text(
                                text = "score : ${viewModel.gameScore}",
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        ) {
                            Box(
//reset game button
                                contentAlignment = Alignment.Center,
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.openGameOverDialog = false
                                        viewModel.resetGame()
                                        audioMap[AudioType.BUTTON]?.play(1f)
                                    },
                                    shape = CircleShape,
                                    modifier = Modifier.size(65.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HomePageButtonBackground
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                                    border = BorderStroke(width = 7.dp, color = Color.Black)
                                ) {
                                }
                                Image(
                                    painter = painterResource(id = R.drawable.reset_icon),
                                    contentDescription = "pause symbol",
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Box(
//revive button
                                contentAlignment = Alignment.Center,
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.openGameOverDialog = false
                                        viewModel.cheeseRevive()
                                        audioMap[AudioType.CHEESE_REVIVE]?.play(0.7f)
                                    },
                                    shape = CircleShape,
                                    modifier = Modifier.size(85.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HomePageButtonBackground,
                                        disabledContainerColor = DisabledReviveButton
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                                    border = BorderStroke(width = 7.dp, color = Color.Black),
                                    enabled = viewModel.hackerPlusState.cheeseCount > 0,
                                ) {
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        painter = painterResource(R.drawable.cheese_icon),
                                        contentDescription = "Cheese Icon",
                                        modifier = Modifier
                                            .size(25.dp)
                                    )
                                    Text(
                                        text = "Revive",
                                        color = GameOverText,
                                        fontSize = 15.sp,
                                        fontFamily = anonymousProBold,
                                        style = if (viewModel.hackerPlusState.cheeseCount == 0) TextStyle(
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        else LocalTextStyle.current
                                    )
                                }
                            }
                            Box(
//go to homepage button
                                contentAlignment = Alignment.Center,
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.resetGame()
                                        navController.navigate("homepage")
                                        viewModel.openGameOverDialog = false
                                        audioMap[AudioType.BUTTON]?.play(1f)
                                    },
                                    shape = CircleShape,
                                    modifier = Modifier.size(65.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HomePageButtonBackground
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                                    border = BorderStroke(width = 7.dp, color = Color.Black)
                                ) {
                                }
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home Page",
                                    tint = ButtonFont,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    //endregion
}


private fun DrawScope.drawCat(
    catDrawableBitmap: ImageBitmap,
    catXCoords: Int,
    viewModel: GameViewModel,
    catYOffset: Int,
) {
    val yOffset = if (viewModel.state.firstHit) {
        size.height.minus(catYOffset.dp.toPx().toInt())
    } else {
        size.height.minus(catYOffset.dp.toPx().toInt())
    }

    clipRect(//restricts drawing to within screen
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height,
        ClipOp.Intersect
    ) {
        drawImage(
            image = catDrawableBitmap,
            dstSize = IntSize(width = 200, height = 180),
            dstOffset = IntOffset(
                x = catXCoords,
                y = yOffset.toInt()
            )
        )
    }

}

@RequiresApi(Build.VERSION_CODES.O)
private fun DrawScope.drawMouse(
    mouseDrawableBitmap: ImageBitmap,
    mouseHitDrawableBitmap: ImageBitmap,
    mouseInvulnerableBitmap: ImageBitmap,
    mouseXCoords: Int,
    viewModel: GameViewModel,
    audioMap: Map<AudioType, AudioClass>,
    context: Context
) {
    val image = if (!viewModel.hackerState.invulnerability) {
        if (viewModel.state.firstHit) {
            mouseHitDrawableBitmap
        } else {
            mouseDrawableBitmap
        }
    } else {
        mouseInvulnerableBitmap
    }
    val mouseSize = IntSize(width = 170, height = 160)
    val mouseOffset = IntOffset(
        x = mouseXCoords,
        y = size.height.minus(Constants.MOUSE_HEIGHT.dp.toPx().toInt()).toInt()
    )

    drawImage(
        image = image,
        dstSize = mouseSize,
        dstOffset = mouseOffset
    )

    //check for collision and enabling powerUps
    val mouseRect = Rect(mouseOffset.toOffset(), mouseSize.toSize())
    viewModel.observeCollision(
        mouseRect = mouseRect,
        gameOverAudio = audioMap[AudioType.GAME_OVER],
        firstHitAudio = audioMap[AudioType.FIRST_HIT],
        context = context
        )
    viewModel.observeInvulnerabilityPowerup(
        mouseRect = mouseRect,
        cookieAudio = audioMap[AudioType.INVULNERABILITY]
    )
    viewModel.observeSpeedUpPowerup(
        mouseRect = mouseRect,
        speedUpAudio = audioMap[AudioType.SPEEDUP]
    )
    viewModel.observeCheesePowerup(
        mouseRect = mouseRect,
        collectAudio = audioMap[AudioType.CHEESE_COLLECT]
    )
}

private fun DrawScope.drawMarkers(
    viewModel: GameViewModel,
    velocity: Dp,
    centreCoords: List<Float>,
) {
    val markerCount = 10
    val markingHeight: Float =
        size.height.div(markerCount) //length of the road markings (incl. space)

    clipRect(//restricts drawing to within screen
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height,
        ClipOp.Intersect
    ) {
        viewModel.moveMarkers(
            velocityPx = velocity.toPx(),
            height = size.height
        )
        (-markerCount..markerCount).forEach { markerIndex ->
            viewModel.drawMarkers(
                centreCoords = centreCoords,
                drawScope = this,
                markingHeight = markingHeight,
                markerIndex = markerIndex,

                )
        }
    }
}

private fun DrawScope.drawObstacles(
    viewModel: GameViewModel,
    imageBitmap: ImageBitmap,
    centreCoords: List<Float>,
    velocity: Dp,
) {
    val divHeight =
        size.height.div(Constants.OBSTACLE_COUNT) //divided height of each obstacle (incl. space)

    clipRect(
//restricts drawing to within screen
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height,
    ) {
        viewModel.drawObstacles(
            centreCoords = centreCoords,
            drawScope = this,
            divHeight = divHeight,
            imageBitmap = imageBitmap,
            height = size.height,
            velocityPx = velocity.toPx()
        )
        viewModel.increaseGameScore(velocity.toPx()) //increases score each time an obstacle passes the mouse
    }
}

private fun DrawScope.drawInvulnerabilityCookies(
    viewModel: GameViewModel,
    imageBitmap: ImageBitmap,
    centreCoords: List<Float>,
    velocity: Dp,
) {
    val divHeight =
        size.height.div(Constants.COOKIE_COUNT) //divided height of each obstacle (incl. space)

    clipRect(
//restricts drawing to within screen
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height,
    ) {
        viewModel.drawCookies(
            centreCoords = centreCoords,
            drawScope = this,
            divHeight = divHeight,
            imageBitmap = imageBitmap,
            height = size.height,
            velocityPx = velocity.toPx()
        )
    }
}

private fun DrawScope.drawSpeedUps(
    viewModel: GameViewModel,
    imageBitmap: ImageBitmap,
    centreCoords: List<Float>,
    velocity: Dp,
) {
    val divHeight =
        size.height.div(Constants.SPEED_UP_COUNT) //divided height of each obstacle (incl. space)

    clipRect(
//restricts drawing to within screen
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height,
    ) {
        viewModel.drawSpeedUps(
            centreCoords = centreCoords,
            drawScope = this,
            divHeight = divHeight,
            imageBitmap = imageBitmap,
            height = size.height,
            velocityPx = velocity.toPx()
        )
    }
}

private fun DrawScope.drawCheese(
    viewModel: GameViewModel,
    imageBitmap: ImageBitmap,
    centreCoords: List<Float>,
    velocity: Dp,
) {
    val divHeight =
        size.height.div(Constants.CHEESE_COUNT) //divided height of each obstacle (incl. space)

    clipRect(
//restricts drawing to within screen
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height,
    ) {
        viewModel.drawCheese(
            centreCoords = centreCoords,
            drawScope = this,
            divHeight = divHeight,
            imageBitmap = imageBitmap,
            height = size.height,
            velocityPx = velocity.toPx()
        )
    }
}
