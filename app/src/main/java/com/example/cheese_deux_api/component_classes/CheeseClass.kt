package com.example.cheese_deux_api.component_classes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize

class CheeseClass(val cheeseIndex: Int, var laneIndex: Int) {

    private var currentCheeseOffset = 0f

    fun move(velocityPx: Float) {
        currentCheeseOffset += velocityPx
    }

    fun reset() {
        currentCheeseOffset = 0f
    }

    fun draw(
        drawScope: DrawScope,
        divHeight: Float,
        imageBitmap: ImageBitmap,
        centreCoords: List<Float>,
        height: Float,
    ): Rect {
        val indexOffset: Float = cheeseIndex.times(divHeight)
        val yPosition = currentCheeseOffset - indexOffset

        if (currentCheeseOffset > height + indexOffset) {
            currentCheeseOffset = indexOffset
            laneIndex = (1..60).random()
        }

        drawScope.apply {
            val cheeseWidth = 35.dp.toPx()
            when (laneIndex) {
                1 -> {
                    return drawCheese(
                        imageBitmap,
                        cheeseWidth,
                        centreCoords,
                        yPosition,
                        laneIndex = 1,
                    )
                }

                2 -> {
                    return drawCheese(
                        imageBitmap,
                        cheeseWidth,
                        centreCoords,
                        yPosition,
                        laneIndex = 2,
                    )
                }

                3 -> {
                    return drawCheese(
                        imageBitmap,
                        cheeseWidth,
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

    private fun DrawScope.drawCheese(
        imageBitmap: ImageBitmap,
        cheeseWidth: Float,
        centreCoords: List<Float>,
        yPosition: Float,
        laneIndex: Int,
    ): Rect {
        val dstOffset = IntOffset(
            x = (centreCoords[laneIndex - 1].minus(cheeseWidth.div(2))).toInt(),
            y = yPosition.toInt()
        )
        val dstSize = IntSize(
            width = cheeseWidth.toInt(),
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