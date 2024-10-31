package com.trueedu.project.ui.views.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.BasicText

@Preview(showBackground = true)
@Composable
fun Badge(
    s: String = "정",
    bgColor: Color = Color.Red,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(12.dp)
            .background(color = bgColor)
    ) {
        BasicText(
            s = s,
            fontSize = 10,
            color = Color.White,
        )
    }
}