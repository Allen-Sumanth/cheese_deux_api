package com.example.cheesechase

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cheese_deux_api.component_classes.AudioClass
import com.example.cheese_deux_api.component_classes.CheeseClass
import com.example.cheese_deux_api.component_classes.CookieClass
import com.example.cheese_deux_api.component_classes.FirstHitVibration
import com.example.cheese_deux_api.component_classes.ObstacleClass
import com.example.cheese_deux_api.component_classes.SpeedUpClass
import com.example.cheese_deux_api.data.DataStorage
import com.example.cheesechase.gyroscope.MeasurableSensor
import com.example.cheesechase.ui.theme.GamePageBackground
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@SuppressLint("MutableCollectionMutableState")
@HiltViewModel
class GameViewModel @Inject constructor(
    gyroSensor: MeasurableSensor,
    private val dataStore: DataStorage,
) : ViewModel() {

    //region declaring states
    var state by mutableStateOf(GameState())//game state
    var hackerState by mutableStateOf(HackerState())//hacker state
    var hackerPlusState by mutableStateOf(HackerPlusState())//hacker plus state

    var gameTracker by mutableFloatStateOf(0f)//10ms delay keeping pace of game
    private var currentMarkerOffset by mutableFloatStateOf(0f) // for drawing markers
    private var obstaclePosRecorder by mutableStateOf(MutableList(4) { Rect.Zero })//recording position of each obstacle for collision detection
    var gameScore by mutableIntStateOf(0)
    var openGameOverDialog by mutableStateOf(false)//prompt to open game over dialog
    var openHighScoreDialog by mutableStateOf(false)//prompt to open high score dialog
    var openInfoDialog by mutableStateOf(false)//prompt to open info dialog

    var speedupVelocity by mutableFloatStateOf(0f)//adds velocity when speedup is activated
    private var cookiePosRecorder by mutableStateOf(MutableList(8) { Rect.Zero })//recording position of each cookie for collision detection
    private var speedUpPosRecorder by mutableStateOf(MutableList(8) { Rect.Zero })//recording position of each speed up for collision detection
    private var cheesePosRecorder by mutableStateOf(MutableList(8) { Rect.Zero })//recording position of each cheese for collision detection
    //endregion

    //region HighScore
    private fun checkAndSaveHighScore() {
        runBlocking {
            if (gameScore > dataStore.highScoreFlow.first()) {
                dataStore.saveNewScore(gameScore)
            }
        }
    }

    fun retrieveHighScore(): Int {
        var highScore: Int
        runBlocking {
            highScore = dataStore.highScoreFlow.first()
        }
        return highScore
    }

    fun resetHighScores() {
        runBlocking {
            dataStore.saveNewScore(0)
        }
    }
    //endregion

    //region starting, pausing and ending the game - overall control functions
    fun startGame() {
        state = state.copy(
            gameStatus = GameStatus.PLAYING
        )
    }

    fun pauseGame() {
        state = state.copy(
            gameStatus = GameStatus.PAUSED
        )
    }

    fun resetGame() {
        state = state.copy(
            gameStatus = GameStatus.STOPPED,
            currentTrack = 1,
            firstHit = false,
            firstHitScore = 0,
        )
        hackerState = hackerState.copy(
            invulnerability = false,
            invulnerabilityActivationScore = 0,
            speedUp = false,
            speedUpActivationScore = 0,
        )
        hackerPlusState = hackerPlusState.copy(
            cheeseCount = 0,
            latestCheeseScore = 0,
        )
        resetObstacles()
        if (hackerState.isHackerState) {
            resetSpeedUps()
            resetCookies()
            resetCheese()
        }
        speedupVelocity = 0f
        gameTracker = 0f
        currentMarkerOffset = 0f

        checkAndSaveHighScore()
        gameScore = 0
    }
    //endregion

    //region all marker related functions - movement controlled here
    fun moveMarkers(velocityPx: Float, height: Float) {
        if (currentMarkerOffset > height) {
            currentMarkerOffset = 0f
        }
        if (state.gameStatus == GameStatus.PLAYING) {
            currentMarkerOffset += velocityPx
            updateGameTracker()
        }
    }

    private fun updateGameTracker() {
        viewModelScope.launch {
            delay(10)
            gameTracker += 0.1f
        }
    }

    fun drawMarkers(
        centreCoords: List<Float>,
        drawScope: DrawScope,
        markerIndex: Int,
        markingHeight: Float,
    ) {
        drawScope.apply {
            val indexOffset = markerIndex.times(markingHeight) //initial offset of the marking
            val yPosition = (indexOffset) + (currentMarkerOffset)

            if (currentMarkerOffset < size.height) {
                drawMarker(centreCoords, yPosition, laneIndex = 1)
                drawMarker(centreCoords, yPosition, laneIndex = 2)
                drawMarker(centreCoords, yPosition, laneIndex = 3)
            }

        }
    }

    private fun DrawScope.drawMarker(
        centreCoords: List<Float>,
        yPosition: Float,
        laneIndex: Int,
    ) {
        drawLine(
            color = GamePageBackground, start = Offset(
                x = centreCoords[laneIndex - 1], y = yPosition
            ), end = Offset(
                x = centreCoords[laneIndex - 1], y = yPosition.plus(60.dp.toPx())
            ), strokeWidth = 5.dp.toPx(), alpha = 0.5f
        )
    }
    //endregion

    //region all obstacle related functions (increasing game score here)
    private val obstacleList = (0..<Constants.OBSTACLE_COUNT).map {
        ObstacleClass(obstacleIndex = it, laneIndex = (1..3).random())
    }

    fun drawObstacles(
        centreCoords: List<Float>,
        drawScope: DrawScope,
        divHeight: Float,
        imageBitmap: ImageBitmap,
        height: Float,
        velocityPx: Float,
    ) {
        obstacleList.forEachIndexed { obstacleIndex, obstacleClass ->
            obstaclePosRecorder[obstacleIndex] = obstacleClass.draw(
                drawScope = drawScope,
                divHeight = divHeight,
                imageBitmap = imageBitmap,
                centreCoords = centreCoords,
                height = height,
            )

            if (state.gameStatus == GameStatus.PLAYING) {
                obstacleClass.move(velocityPx = velocityPx)
            }
        }
    }

    private fun resetObstacles() {
        obstacleList.forEach {
            it.reset()
        }
    }


    fun increaseGameScore(velocityPx: Float) {
        if (obstaclePosRecorder.any { obstacleRect ->
                obstacleRect.top in (1830f - velocityPx / 2..1830f + velocityPx / 2) //1830f is mouse ending offset (approx)
            }) {
            gameScore++
        }
    }
    //endregion

    //region handling mouse movement
    fun moveMouseLaneFromPointer(xInp: Float, yInp: Float, width: Float, height: Float) {
        val lane1Range = 0f..width.times(0.3125f)
        val lane2Range = width.times(0.3125f)..width.times(0.6875f)
        val lane3Range = width.times(0.6875f)..width

        if (state.gameStatus == GameStatus.PLAYING && yInp < height.times(0.9f)) {
            when (xInp) {
                in lane1Range -> {
                    state = state.copy(
                        currentTrack = 0
                    )
                }

                in lane2Range -> {
                    state = state.copy(
                        currentTrack = 1
                    )
                }

                in lane3Range -> {
                    state = state.copy(
                        currentTrack = 2
                    )
                }
            }
        }
    }

    private fun mouseMouseLaneFromGyro(yAngularVelocity: Float) {
        if (yAngularVelocity > 1.5) {
            state = state.copy(
                currentTrack = when (state.currentTrack) {
                    0 -> 1
                    1 -> 2
                    2 -> 2
                    else -> 1
                }
            )
        } else if (yAngularVelocity < -1.5) {
            state = state.copy(
                currentTrack = when (state.currentTrack) {
                    0 -> 0
                    1 -> 0
                    2 -> 1
                    else -> 1
                }
            )
        }
    }
    //endregion

    //region observing collision - called in mouse drawing from game screen
    @RequiresApi(Build.VERSION_CODES.O)
    fun observeCollision(
        mouseRect: Rect,
        gameOverAudio: AudioClass?,
        firstHitAudio: AudioClass?,
        context: Context,
    ) {//also reset cat 10 blocks after collision
        if (obstaclePosRecorder.any { obstacleRect ->
                obstacleRect.overlaps(mouseRect)
            }) {
            if (!state.firstHit && !hackerState.invulnerability) {//if invulnerable, collision doesn't matter
                collisionFirstHit()
                firstHitAudio?.play(1f)
                FirstHitVibration().vibrate(context = context, duration = 250)
            } else if (state.firstHit && gameScore > state.firstHitScore && !hackerState.invulnerability) {
                collisionSecondHit()
                gameOverAudio?.play(1f)
            }
        }

        if ((state.firstHit && gameScore > state.firstHitScore + 10) || (state.firstHit && hackerState.invulnerability)) {
            state = state.copy(
                firstHit = false,
                firstHitScore = 0
            )
        }
    }

    private fun collisionFirstHit() {
        state = state.copy(
            firstHit = true,
            firstHitScore = gameScore
        )

    }

    private fun collisionSecondHit() {
        pauseGame()
        openGameOverDialog = true
    }
    //endregion

    //region hacker mode - invulnerability cookies
    private val cookieList = (1..Constants.COOKIE_COUNT step 2).map {
        CookieClass(
            cookieIndex = it,
            laneIndex = (1..60).random()
        )//randomising to 1 in 20 probability
    }

    fun drawCookies(
        centreCoords: List<Float>,
        drawScope: DrawScope,
        divHeight: Float,
        imageBitmap: ImageBitmap,
        height: Float,
        velocityPx: Float,
    ) {
        cookieList.forEachIndexed { cookieIndex, cookieClass ->
            cookiePosRecorder[cookieIndex] = cookieClass.draw(
                drawScope = drawScope,
                divHeight = divHeight,
                imageBitmap = imageBitmap,
                centreCoords = centreCoords,
                height = height,
            )

            if (state.gameStatus == GameStatus.PLAYING) {
                cookieClass.move(velocityPx = velocityPx)
            }
        }
    }

    fun observeInvulnerabilityPowerup(mouseRect: Rect, cookieAudio: AudioClass?) {
        if (cookiePosRecorder.any { cookieRect ->
                cookieRect.overlaps(mouseRect)
            }) {
            hackerState = hackerState.copy(
                invulnerability = true,
                invulnerabilityActivationScore = gameScore
            )
            cookieAudio?.play(volume = 1f)
        }
        if (hackerState.invulnerability && gameScore > hackerState.invulnerabilityActivationScore + 10) {
            hackerState = hackerState.copy(
                invulnerability = false,
                invulnerabilityActivationScore = 0
            )
        }
    }

    private fun resetCookies() {
        cookieList.forEach {
            it.reset()
        }
    }
    //endregion

    //region hacker mode - speed ups
    private val speedUpList = (1..Constants.SPEED_UP_COUNT step 2).map {
        SpeedUpClass(
            speedUpIndex = it,
            laneIndex = (1..60).random()
        )//randomising to 1 in 20 probability
    }

    fun drawSpeedUps(
        centreCoords: List<Float>,
        drawScope: DrawScope,
        divHeight: Float,
        imageBitmap: ImageBitmap,
        height: Float,
        velocityPx: Float,
    ) {
        speedUpList.forEachIndexed { speedUpIndex, speedUpClass ->
            if (cookiePosRecorder[speedUpIndex] == Rect.Zero) {
                speedUpPosRecorder[speedUpIndex] = speedUpClass.draw(
                    drawScope = drawScope,
                    divHeight = divHeight,
                    imageBitmap = imageBitmap,
                    centreCoords = centreCoords,
                    height = height,
                )
            }

            if (state.gameStatus == GameStatus.PLAYING) {
                speedUpClass.move(velocityPx = velocityPx)
            }
        }
    }

    fun observeSpeedUpPowerup(mouseRect: Rect, speedUpAudio: AudioClass?) {
        if (speedUpPosRecorder.any { speedUpRect ->
                speedUpRect.overlaps(mouseRect)
            }) {
            hackerState = hackerState.copy(
                speedUp = true,
                speedUpActivationScore = gameScore
            )
            speedUpAudio?.play(volume = 0.5f)
            speedupVelocity = Constants.SPEEDUP_VELOCITY.toFloat()
        }
        if (hackerState.speedUp && gameScore > hackerState.speedUpActivationScore + 10) {
            hackerState = hackerState.copy(
                speedUp = false,
                speedUpActivationScore = 0
            )
            speedupVelocity = 0f
        }
    }

    private fun resetSpeedUps() {
        speedUpList.forEach {
            it.reset()
        }
    }
    //endregion

    //region hacker++ mode - cheese
    private val cheeseList = (1..Constants.CHEESE_COUNT step 2).map {
        CheeseClass(
            cheeseIndex = it,
            laneIndex = (1..60).random()
        )//randomising to 1 in 20 probability
    }

    fun drawCheese(
        centreCoords: List<Float>,
        drawScope: DrawScope,
        divHeight: Float,
        imageBitmap: ImageBitmap,
        height: Float,
        velocityPx: Float,
    ) {
        cheeseList.forEachIndexed { cheeseIndex, cheeseClass ->
            if (cookiePosRecorder[cheeseIndex] == Rect.Zero && speedUpPosRecorder[cheeseIndex] == Rect.Zero) {
                cheesePosRecorder[cheeseIndex] = cheeseClass.draw(
                    drawScope = drawScope,
                    divHeight = divHeight,
                    imageBitmap = imageBitmap,
                    centreCoords = centreCoords,
                    height = height,
                )
            }

            if (state.gameStatus == GameStatus.PLAYING) {
                cheeseClass.move(velocityPx = velocityPx)
            }
        }
    }

    fun observeCheesePowerup(mouseRect: Rect, collectAudio: AudioClass?) {
        if (cheesePosRecorder.any { cheeseRect ->
                cheeseRect.overlaps(mouseRect)
            } && gameScore > hackerPlusState.latestCheeseScore) {//second condition to ensure the cheese doesn't get added twice
            hackerPlusState = hackerPlusState.copy(
                cheeseCount = hackerPlusState.cheeseCount + 1,
                latestCheeseScore = gameScore
            )
            collectAudio?.play(volume = 0.5f)
        }

    }

    private fun resetCheese() {
        cheeseList.forEach {
            it.reset()
        }
    }

    fun shootCheese() {
        val targetObstacleList: List<ObstacleClass> =
            obstacleList.filter { obstacleClass -> //The obstacles that qualify
                obstacleClass.laneIndex == state.currentTrack + 1 && //Must be in same lane
                        obstaclePosRecorder[obstacleClass.obstacleIndex].top > 0 && //Must be visible
                        obstaclePosRecorder[obstacleClass.obstacleIndex].bottom < 1800f // Must be above our mouse
            }

        if (targetObstacleList.isNotEmpty()) {
            var targetObstacle = targetObstacleList.first()
            targetObstacleList.forEach { obstacleClass ->
                if (obstaclePosRecorder[obstacleClass.obstacleIndex].bottom > obstaclePosRecorder[targetObstacle.obstacleIndex].bottom) {
                    targetObstacle = obstacleClass
                }
            }

            obstacleList[targetObstacle.obstacleIndex].laneIndex =
                4 //change the lane index to 4 to mark it as shot down
            hackerPlusState = hackerPlusState.copy(
                cheeseCount = hackerPlusState.cheeseCount - 1
            )
        }

    }

    fun cheeseRevive() {
        hackerPlusState = hackerPlusState.copy(
            cheeseCount = hackerPlusState.cheeseCount - 1
        )
        hackerState = hackerState.copy(
            invulnerability = true,
            invulnerabilityActivationScore = gameScore
        )
        state = state.copy(
            currentTrack = if (state.currentTrack == 0) 2 else state.currentTrack - 1,
            firstHit = false,
            firstHitScore = 0,
        )
    }

    //endregion

    //region handling gyro
    init {
        gyroSensor.startListening()
        gyroSensor.setOnSensorValuesChangedListener { values ->
            val yAngularVelocity = values[1]
            if (state.gameStatus == GameStatus.PLAYING) {
                mouseMouseLaneFromGyro(yAngularVelocity)
            }
        }
    }
    //endregion

}