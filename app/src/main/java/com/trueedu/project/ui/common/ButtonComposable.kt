package com.trueedu.project.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun TouchIcon24(icon: ImageVector, onClick: () -> Unit) {
    TouchIconWithSize(24.dp, icon, onClick)
}

@Composable
fun TouchIcon32(icon: ImageVector, onClick: () -> Unit) {
    TouchIconWithSize(32.dp, icon, onClick)
}

@Composable
private fun TouchIconWithSize(
    size: Dp,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val padding = 8.dp
    Box(
        modifier = Modifier
            .size(size + padding * 2)
            .clip(CircleShape)
            .clickable { onClick() },
    ) {
        Icon(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            imageVector = icon,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "icon"
        )
    }
}
