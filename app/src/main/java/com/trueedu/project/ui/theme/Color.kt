package com.trueedu.project.ui.theme

import androidx.compose.ui.graphics.Color

// light mode only
object ChartColor {
    val up = Color(0xFFFF2D55)
    val down = Color(0xFF007BFF)
    val neutral = Color(0xFF68727C)

    fun color(v: Double): Color {
        return when {
            v == 0.0 -> neutral
            v > 0 -> up
            else -> down
        }
    }
}
