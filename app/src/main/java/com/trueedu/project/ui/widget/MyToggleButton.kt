package com.trueedu.project.ui.widget

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.BasicText

@Preview(showBackground = true)
@Composable
fun MyToggleButton(
    defaultValue: Int = 0,
    textKeys: List<String> = listOf("On", "Off"),
    toggleClick: (Int) -> Unit = {},
) {
    val width = 74.dp
    val height = 30.dp
    val gap = 3.dp

    var state by remember { mutableIntStateOf(defaultValue) }
    val unitSize = (width - gap * 2) / 2
    val innerHeight = height - gap * 2

    val animatePosition = animateDpAsState(
        targetValue = (3.dp + unitSize * state),
        label = ""
    )


    val trackColor = MaterialTheme.colorScheme.outline
    val labelModifier = Modifier.size(unitSize, innerHeight)

    Box(
        modifier = Modifier
            .background(
                color = trackColor,
                shape = RoundedCornerShape(height)
            )
    ) {
        // 토글 버튼 이동 영역
        Box(
            modifier = Modifier.size(width, height)
        ) {
            Box(
                modifier = labelModifier
                    .offset(x = animatePosition.value, y = gap)
                    .background(
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        shape = RoundedCornerShape(height * 2)
                    )
            )
        }
        Row(
            modifier = Modifier
                .size(width, height)
                .padding(gap),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            textKeys.forEachIndexed { index, s ->
                Box(modifier = labelModifier
                    .clickable {
                    val candidate = (state + 1) % textKeys.size
                    state = candidate
                    toggleClick(candidate)
                }) {
                    val isCurrent = state == index
                    BasicText(
                        s = s,
                        fontSize = 14,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 3.dp)
                        ,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        fontWeight = if (isCurrent) FontWeight.W500 else FontWeight.W400,
                    )
                }
            }
        }
    }
}
