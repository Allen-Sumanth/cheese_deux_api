package com.example.cheese_deux_api.component_classes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//obstacleIndex starts from 1 to hide all obstacles in the beginning
class ObstacleClass(
    val obstacleIndex: Int,
    var laneIndex: Int = 1,
    var isHindrance: Boolean,
    val obstacleCourseApi: ObstacleCourseApi,
) {
    private var currentObstacleOffset = 0f

    fun move(velocityPx: Float) {
        currentObstacleOffset += velocityPx
    }

    fun reset() {
        currentObstacleOffset = 0f
    }

    fun draw(
        drawScope: DrawScope,
        divHeight: Float,
        obstacleImageBitmap: ImageBitmap,
        hindranceImageBitmap: ImageBitmap,
        centreCoords: List<Float>,
        height: Float,
        scope: CoroutineScope
    ): Rect {
        val indexOffset: Float = (obstacleIndex + 1).times(divHeight)
        val yPosition = currentObstacleOffset - indexOffset

        if (currentObstacleOffset > height + indexOffset) {
            obtainLaneIndexFromApi(scope)
            currentObstacleOffset = -(divHeight.times(4) - indexOffset) //4 more need to come into view, as we're using 8 obstacles
        }

        drawScope.apply {
            if (isHindrance) {
                val hindranceWidth = size.width.times(0.25f).times(0.25f)
                return if (laneIndex in (1..3)) {
                    drawHindrance(
                        hindranceImageBitmap,
                        hindranceWidth,
                        centreCoords,
                        yPosition
                    )
                } else Rect.Zero
            } else {
                val obstacleWidth = size.width.times(0.25f.times(0.9f))
                return if (laneIndex in (1..3)) {
                    drawObstacle(
                        obstacleImageBitmap,
                        obstacleWidth,
                        centreCoords,
                        yPosition
                    )
                } else Rect.Zero
            }
        }
    }

    private fun obtainLaneIndexFromApi(scope: CoroutineScope) {
        scope.launch {
            val lane = obstacleCourseApi.getLaneIndex(obstacleIndex)
            isHindrance = lane.first
            laneIndex = lane.second
        }
    }

    private fun DrawScope.drawObstacle(
        obstacleImageBitmap: ImageBitmap,
        obstacleWidth: Float,
        centreCoords: List<Float>,
        yPosition: Float,
    ): Rect {
        val dstOffset = IntOffset(
            x = (centreCoords[laneIndex - 1].minus(obstacleWidth.div(2))).toInt(),
            y = yPosition.toInt()
        )
        val dstSize = IntSize(
            width = obstacleWidth.toInt(),
            height = obstacleWidth.times(0.9f).toInt()
        )
        drawImage(
            image = obstacleImageBitmap,
            dstSize = dstSize,
            dstOffset = dstOffset
        )
        return Rect(
            offset = dstOffset.toOffset(),
            size = dstSize.toSize()
        )
    }

    private fun DrawScope.drawHindrance(
        hindranceImageBitmap: ImageBitmap,
        hindranceWidth: Float,
        centreCoords: List<Float>,
        yPosition: Float,
    ): Rect {
        val dstOffset = IntOffset(
            x = (centreCoords[laneIndex - 1].minus(hindranceWidth.div(2))).toInt(),
            y = yPosition.toInt()
        )
        val dstSize = IntSize(
            width = hindranceWidth.toInt(),
            height = hindranceWidth.times(1.5f).toInt()
        )
        drawImage(
            image = hindranceImageBitmap,
            dstSize = dstSize,
            dstOffset = dstOffset
        )
        return Rect(
            offset = dstOffset.toOffset(),
            size = dstSize.toSize()
        )
    }
}