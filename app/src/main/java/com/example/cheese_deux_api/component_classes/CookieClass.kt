package com.example.cheese_deux_api.component_classes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize


class CookieClass(val cookieIndex: Int, var laneIndex: Int) {

    private var currentCookieOffset = 0f

    fun move(velocityPx: Float) {
        currentCookieOffset += velocityPx
    }

    fun reset() {
        currentCookieOffset = 0f
    }

    fun draw(
        drawScope: DrawScope,
        divHeight: Float,
        imageBitmap: ImageBitmap,
        centreCoords: List<Float>,
        height: Float,
    ): Rect {
        val indexOffset: Float = cookieIndex.times(divHeight)
        val yPosition = currentCookieOffset - indexOffset

        if (currentCookieOffset > height + indexOffset) {
            currentCookieOffset = indexOffset
            laneIndex = (1..60).random()
        }

        drawScope.apply {
            val cookieWidth = 35.dp.toPx()
            when (laneIndex) {
                1 -> {
                    return drawCookie(
                        imageBitmap,
                        cookieWidth,
                        centreCoords,
                        yPosition,
                        laneIndex = 1,
                    )
                }

                2 -> {
                    return drawCookie(
                        imageBitmap,
                        cookieWidth,
                        centreCoords,
                        yPosition,
                        laneIndex = 2,
                    )
                }

                3 -> {
                    return drawCookie(
                        imageBitmap,
                        cookieWidth,
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

    private fun DrawScope.drawCookie(
        imageBitmap: ImageBitmap,
        cookieWidth: Float,
        centreCoords: List<Float>,
        yPosition: Float,
        laneIndex: Int,
    ): Rect {
        val dstOffset = IntOffset(
            x = (centreCoords[laneIndex - 1].minus(cookieWidth.div(2))).toInt(),
            y = yPosition.toInt()
        )
        val dstSize = IntSize(
            width = cookieWidth.toInt(),
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
