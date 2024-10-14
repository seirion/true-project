package com.trueedu.project.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter

@Composable
fun NetworkImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Image(
        painter = rememberImagePainter(
            data = imageUrl,
            builder = {
                crossfade(true) // 페이드 인 효과 추가
            }
        ),
        contentDescription = null, // 이미지 설명
        modifier = modifier.size(128.dp) // 이미지 크기
    )
}
