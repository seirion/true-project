package com.trueedu.project.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun listBackgroundColor(index: Int): Color {
    return if (index % 2 == 0) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.5f)
    }
}
