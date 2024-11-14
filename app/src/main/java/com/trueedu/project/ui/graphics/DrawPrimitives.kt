package com.trueedu.project.ui.graphics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill


fun DrawScope.drawBar(color: Color, offset: Offset, size: Size) {
    try {
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(offset = offset, size = size),
                )
            )
        }
        drawPath(path, color = color, style = Fill)
    } catch (error: Throwable) {

    }
}

fun DrawScope.drawLine(start: Offset, end: Offset, lineWidth: Float, color: Color) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = lineWidth,
    )
}
