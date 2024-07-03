package com.example.cheese_deux_api.component_classes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize


class SpeedUpClass(val speedUpIndex: Int, var laneIndex: Int) {

    private var currentSpeedUpOffset = 0f

    fun move(velocityPx: Float) {
        currentSpeedUpOffset += velocityPx
    }

    fun reset() {
        currentSpeedUpOffset = 0f
    }

    fun draw(
        drawScope: DrawScope,
        divHeight: Float,
        imageBitmap: ImageBitmap,
        centreCoords: List<Float>,
        height: Float,
    ): Rect {
        val indexOffset: Float = speedUpIndex.times(divHeight)
        val yPosition = currentSpeedUpOffset - indexOffset

        if (currentSpeedUpOffset > height + indexOffset) {
            currentSpeedUpOffset = indexOffset
            laneIndex = (1..60).random()
        }

        drawScope.apply {
            val speedUpWidth = 35.dp.toPx()
            when (laneIndex) {
                1 -> {
                    return drawSpeedUp(
                        imageBitmap,
                        speedUpWidth,
                        centreCoords,
                        yPosition,
                        laneIndex = 1,
                    )
                }

                2 -> {
                    return drawSpeedUp(
                        imageBitmap,
                        speedUpWidth,
                        centreCoords,
                        yPosition,
                        laneIndex = 2,
                    )
                }

                3 -> {
                    return drawSpeedUp(
                        imageBitmap,
                        speedUpWidth,
                        centreCoords,
                        yPosition,
                        laneIndex = 3,
                    )
                }
                in 4..60 -> {
                    return Rect.Zero
                }
            }
        }
        return Rect(offset = IntOffset.Zero.toOffset(), size = IntSize.Zero.toSize())
    }

    private fun DrawScope.drawSpeedUp(
        imageBitmap: ImageBitmap,
        speedUpWidth: Float,
        centreCoords: List<Float>,
        yPosition: Float,
        laneIndex: Int,
    ): Rect {
        val dstOffset = IntOffset(
            x = (centreCoords[laneIndex - 1].minus(speedUpWidth.div(2))).toInt(),
            y = yPosition.toInt()
        )
        val dstSize = IntSize(
            width = speedUpWidth.toInt(),
            height = 24.dp.toPx().toInt()
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
