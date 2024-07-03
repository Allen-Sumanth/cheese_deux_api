package com.example.cheese_deux_api.component_classes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize

//obstacleIndex starts from 1 to hide all obstacles in the beginning
class ObstacleClass(val obstacleIndex: Int, var laneIndex: Int) {

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
        imageBitmap: ImageBitmap,
        centreCoords: List<Float>,
        height: Float,
        ): Rect {
        val indexOffset: Float = (obstacleIndex+1).times(divHeight)
        val yPosition = currentObstacleOffset - indexOffset

        if (currentObstacleOffset > height + indexOffset) {
            currentObstacleOffset = indexOffset
            laneIndex = (1..3).random()
        }

        drawScope.apply {
            val obstacleWidth = size.width.times(0.25f.times(0.9f))
            when (laneIndex) {
                1 -> {
                    return drawObstacle(
                        imageBitmap,
                        obstacleWidth,
                        centreCoords,
                        yPosition,
                        laneIndex = 1,
                    )
                }

                2 -> {
                    return drawObstacle(
                        imageBitmap,
                        obstacleWidth,
                        centreCoords,
                        yPosition,
                        laneIndex = 2,
                    )
                }

                3 -> {
                    return drawObstacle(
                        imageBitmap,
                        obstacleWidth,
                        centreCoords,
                        yPosition,
                        laneIndex = 3,
                    )
                }
                else -> {
                    return Rect.Zero
                }
            }
        }
    }

    private fun DrawScope.drawObstacle(
        imageBitmap: ImageBitmap,
        obstacleWidth: Float,
        centreCoords: List<Float>,
        yPosition: Float,
        laneIndex: Int,
    ): Rect {
        val dstOffset = IntOffset(
            x = (centreCoords[laneIndex - 1].minus(obstacleWidth.div(2))).toInt(),
            y = yPosition.toInt()
        )
        val dstSize = IntSize(
            width = obstacleWidth.toInt(),
            height = 35.dp.toPx().toInt()
        )
        drawImage(
            image = imageBitmap,
            dstSize = dstSize,
            dstOffset = dstOffset
        )
        return Rect(
            offset = dstOffset.toOffset(),
            size = dstSize.toSize()
        )
    }
}
