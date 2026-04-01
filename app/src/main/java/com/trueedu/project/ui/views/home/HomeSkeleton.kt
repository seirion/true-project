package com.trueedu.project.ui.views.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─── Shimmer brush ───────────────────────────────────────────────────────────

private fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant,
    )
    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim - 300f, 0f),
            end = Offset(translateAnim, 0f),
        ),
        shape = RoundedCornerShape(4.dp),
    )
}

@Composable
private fun SkeletonBox(width: Dp, height: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .shimmerEffect()
    )
}

@Composable
private fun SkeletonFill(height: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shimmerEffect()
    )
}

// ─── AccountInfo skeleton ────────────────────────────────────────────────────

@Composable
fun AccountInfoSkeleton() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
    ) {
        Column {
            // 총자산 + refresh icon placeholder
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(width = 140.dp, height = 28.dp)
                Spacer(Modifier.width(8.dp))
                SkeletonBox(width = 24.dp, height = 24.dp)
            }
            Spacer(Modifier.height(6.dp))
            // 수익/수익률
            SkeletonBox(width = 110.dp, height = 16.dp)
        }
        // toggle button
        SkeletonBox(width = 80.dp, height = 32.dp)
    }

    // 예수금 헤더
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        repeat(3) {
            SkeletonBox(width = 60.dp, height = 14.dp, modifier = Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(4.dp))
    // 예수금 값
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        repeat(3) {
            SkeletonBox(width = 70.dp, height = 16.dp, modifier = Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(8.dp))
}

// ─── HomeStockItem skeleton ──────────────────────────────────────────────────

@Composable
fun HomeStockItemSkeleton() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            SkeletonBox(width = 100.dp, height = 16.dp)
            Spacer(Modifier.height(4.dp))
            SkeletonBox(width = 80.dp, height = 14.dp)
        }
        Column(horizontalAlignment = Alignment.End) {
            SkeletonBox(width = 80.dp, height = 16.dp)
            Spacer(Modifier.height(4.dp))
            SkeletonBox(width = 100.dp, height = 14.dp)
        }
    }
}

// ─── Full home skeleton ──────────────────────────────────────────────────────

@Composable
fun HomeLoadingSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        item { AccountInfoSkeleton() }
        // 종목 스켈레톤 5개
        items(5) { HomeStockItemSkeleton() }
    }
}
