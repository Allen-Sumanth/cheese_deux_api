package com.example.cheese_deux_api

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.collection.intIntMapOf
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
import com.example.cheese_deux_api.cheese_deux.CheeseDeuxApi
import com.example.cheese_deux_api.cheese_deux.ImageFetcher
import com.example.cheese_deux_api.cheese_deux.ObstacleLimit
import com.example.cheese_deux_api.component_classes.AudioClass
import com.example.cheese_deux_api.component_classes.CheeseClass
import com.example.cheese_deux_api.component_classes.FirstHitVibration
import com.example.cheese_deux_api.component_classes.HindranceClass
import com.example.cheese_deux_api.component_classes.HindranceType
import com.example.cheese_deux_api.component_classes.ObstacleClass
import com.example.cheese_deux_api.component_classes.ObstacleCourseApi
import com.example.cheese_deux_api.data.DataStorage
import com.example.cheese_deux_api.gyroscope.MeasurableSensor
import com.example.cheese_deux_api.theme.MarkerColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
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
    cheeseDeuxApi: CheeseDeuxApi,
    private val imageFetcher: ImageFetcher,
    private val obstacleCourseApi: ObstacleCourseApi,
) : ViewModel() {

    //region declaring states
    var state by mutableStateOf(GameState())//game state
    var hackerState by mutableStateOf(HackerState())//hacker state
    var hackerPlusState by mutableStateOf(HackerPlusState())//hacker plus state

    var gameTracker by mutableFloatStateOf(0f)//10ms delay keeping pace of game
    private var currentMarkerOffset by mutableFloatStateOf(0f) // for drawing markers
    var gameScore by mutableIntStateOf(0)
    var openGameOverDialog by mutableStateOf(false)//prompt to open game over dialog
    var openHighScoreDialog by mutableStateOf(false)//prompt to open high score dialog
    var openInfoDialog by mutableStateOf(false)//prompt to open info dialog

    private var obstaclePosRecorder by mutableStateOf(MutableList(8) { Rect.Zero })//recording position of each obstacle for collision detection
    var speedupVelocity by mutableFloatStateOf(0f)//adds velocity when speedup is activated
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

    //region obtaining bitmaps from the api (init block used)
    private val catApiEndpoint = "https://chasedeux.vercel.app/image?character=tom"
    private val mouseApiEndpoint = "https://chasedeux.vercel.app/image?character=jerry"
    private val obstacleApiEndpoint = "https://chasedeux.vercel.app/image?character=obstacle"

    var catBitmap: Bitmap? by mutableStateOf(null)
    var mouseBitmap: Bitmap? by mutableStateOf(null)
    var obstacleBitmap: Bitmap? by mutableStateOf(null)

    init {
        viewModelScope.launch(context = kotlinx.coroutines.Dispatchers.IO) {
            catBitmap = imageFetcher.getBitmap(
                imageEndpoint = catApiEndpoint,
                defaultImageRes = R.drawable.cat_icon
            )
            mouseBitmap = imageFetcher.getBitmap(
                imageEndpoint = mouseApiEndpoint,
                defaultImageRes = R.drawable.mouse_icon
            )
            obstacleBitmap = imageFetcher.getBitmap(
                imageEndpoint = obstacleApiEndpoint,
                defaultImageRes = R.drawable.obstacle
            )
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
            hitCount = 0,
            latestHitScore = 0,
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
            color = MarkerColor, start = Offset(
                x = centreCoords[laneIndex - 1], y = yPosition
            ), end = Offset(
                x = centreCoords[laneIndex - 1], y = yPosition.plus(60.dp.toPx())
            ), strokeWidth = 5.dp.toPx(), alpha = 0.5f
        )
    }
    //endregion

    //region all obstacle related functions (increasing game score here)

    //initializing list of obstacles and hindrances with dummy values
    private var obstacleList = MutableList(8) {
        ObstacleClass(
            obstacleIndex = it,
            isHindrance = false,
            obstacleCourseApi = obstacleCourseApi
        )
    }
    private var nextIterationObstacleList = MutableList(8) {
        ObstacleClass(
            obstacleIndex = it,
            isHindrance = false,
            obstacleCourseApi = obstacleCourseApi
        )
    }

    init {// obtaining the lanes for the first time
        viewModelScope.launch(context = kotlinx.coroutines.Dispatchers.IO) {
            //each element of laneList is a pair of (Bool - isHindrance, Int - lane)
            val laneListPair = obstacleCourseApi.initialiseObstacleList()
            obstacleList.forEachIndexed { index, obstacleClass ->
                obstacleClass.isHindrance = laneListPair.first[index].first
                obstacleClass.laneIndex = laneListPair.first[index].second
            }
            nextIterationObstacleList.forEachIndexed { index, obstacleClass ->
                obstacleClass.isHindrance = laneListPair.second[index].first
                obstacleClass.laneIndex = laneListPair.second[index].second
            }
        }
    }

    fun drawObstaclesAndHindrances(
        centreCoords: List<Float>,
        drawScope: DrawScope,
        divHeight: Float,
        obstacleImageBitmap: ImageBitmap,
        hindranceImageBitmap: ImageBitmap,
        height: Float,
        velocityPx: Float,
    ) {
        obstacleList.forEachIndexed { index, obstacleClass ->
            obstaclePosRecorder[index] = obstacleClass.draw(
                drawScope = drawScope,
                divHeight = divHeight,
                obstacleImageBitmap = obstacleImageBitmap,
                hindranceImageBitmap = hindranceImageBitmap,
                centreCoords = centreCoords,
                height = height,
                scope = viewModelScope
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
                obstacleRect.bottom in (1600f - velocityPx / 2..1600f + velocityPx / 2) //1600f is mouse starting offset (approx)
            } && state.gameStatus == GameStatus.PLAYING) {
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

    //region observing collision - called in mouse drawing from game screen (init block used)
    var obstacleLimit by mutableStateOf(ObstacleLimit(obstacleLimit = 2)) //default value is 2

    //API call to get obstacleLimit
    init {
        viewModelScope.launch {
            val obstacleLimitResponse = try {
                cheeseDeuxApi.getObstacleLimit()
            } catch (t: Throwable) {
                null
            }

            if (obstacleLimitResponse != null && obstacleLimitResponse.isSuccessful) {
                obstacleLimit = obstacleLimitResponse.body()!!
            }
        }

        ObstacleCourseApi(cheeseDeuxApi)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun observeCollision(
        mouseRect: Rect,
        gameOverAudio: AudioClass?,
        collisionAudio: AudioClass?,
        cookieAudio: AudioClass?,
        speedUpAudio: AudioClass?,
        context: Context,
    ) {//also reset cat 10 blocks after collision
        if (obstaclePosRecorder.any { obstacleRect ->
                obstacleRect.overlaps(mouseRect)
            }) {
            val index = obstaclePosRecorder.indexOfLast { rect ->
                rect.overlaps(mouseRect)
            }

            if (!obstacleList[index].isHindrance) {
                if (gameScore > state.latestHitScore && !hackerState.invulnerability && state.hitCount < obstacleLimit.obstacleLimit - 1) {//if invulnerable, collision doesn't matter
                    collisionNormal()
                    collisionAudio?.play(1f)
                    FirstHitVibration().vibrate(context = context, duration = 250)
                } else if (gameScore > state.latestHitScore && !hackerState.invulnerability && state.hitCount >= obstacleLimit.obstacleLimit - 1) {
                    collisionFinalHit()
                    gameOverAudio?.play(1f)
                }
            } else {
                val hindranceType = obstacleCourseApi.hindranceTypeList[index].type
                val hindranceAmount = obstacleCourseApi.hindranceTypeList[index].amount

                when (hindranceType) {
                    HindranceType.NONE -> {}
                    HindranceType.AUTOJUMP -> {
                        autoJump(hindranceAmount, cookieAudio)
                    }

                    HindranceType.SPEEDUP -> {
                        viewModelScope.launch(context = kotlinx.coroutines.Dispatchers.Default) {
                            val delayTime = hindranceAmount * 1000
                            hackerState = hackerState.copy(
                                speedUp = true,
                                speedUpActivationScore = gameScore
                            )
                            speedUpAudio?.play(volume = 0.5f)
                            speedupVelocity = Constants.SPEEDUP_VELOCITY.toFloat()

                            delay(delayTime.toLong())

                            hackerState = hackerState.copy(
                                speedUp = false,
                                speedUpActivationScore = 0
                            )
                            speedupVelocity = 0f
                        }
                    }

                    HindranceType.CAT_CLOSER -> {
                        if (!hackerState.invulnerability) {
                            state = state.copy(
                                latestHitScore = gameScore,
                                hitCount = state.hitCount + hindranceAmount
                            )
                            if (state.hitCount >= obstacleLimit.obstacleLimit - 1) {
                                collisionFinalHit()
                                gameOverAudio?.play(1f)
                            } else {
                                collisionAudio?.play(1f)
                                FirstHitVibration().vibrate(context = context, duration = 250)
                            }
                        }
                    }
                }
            }
        }

        if ((state.hitCount < obstacleLimit.obstacleLimit && gameScore > state.latestHitScore + 10) || (state.hitCount < obstacleLimit.obstacleLimit && hackerState.invulnerability)) {
            state = state.copy(
                latestHitScore = 0,
                hitCount = 0
            )
        }
    }

    private fun autoJump(
        hindranceAmount: Int,
        cookieAudio: AudioClass?,
    ) {
        viewModelScope.launch(context = Dispatchers.Default) {
            val delayTime = hindranceAmount * 1000
            hackerState = hackerState.copy(
                invulnerability = true,
                invulnerabilityActivationScore = gameScore
            )
            cookieAudio?.play(volume = 1f)
            delay(delayTime.toLong())
            hackerState = hackerState.copy(
                invulnerability = false,
                invulnerabilityActivationScore = 0
            )
        }
    }

    private fun collisionNormal() {
        state = state.copy(
            latestHitScore = gameScore,
            hitCount = state.hitCount + 1
        )
    }

    private fun collisionFinalHit() {
        pauseGame()
        openGameOverDialog = true
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
            cheesePosRecorder[cheeseIndex] = cheeseClass.draw(
                drawScope = drawScope,
                divHeight = divHeight,
                imageBitmap = imageBitmap,
                centreCoords = centreCoords,
                height = height,
            )

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

    fun cheeseRevive(cookieAudio: AudioClass?) {
        hackerPlusState = hackerPlusState.copy(
            cheeseCount = hackerPlusState.cheeseCount - 1
        )
        autoJump(hindranceAmount = 5, cookieAudio = cookieAudio)
        state = state.copy(
            currentTrack = if (state.currentTrack == 0) 2 else state.currentTrack - 1,
            hitCount = 0,
            latestHitScore = 0,
        )
    }

//endregion

    //region handling gyro (init block used)
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