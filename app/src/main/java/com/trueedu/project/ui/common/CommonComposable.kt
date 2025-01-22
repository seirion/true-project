package com.trueedu.project.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em

@Composable
fun DividerHorizontal() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp,
    )
}

@Composable
fun TrueText(
    s: String,
    fontSize: Int,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.W400,
    color: Color = MaterialTheme.colorScheme.primary,
    maxLines: Int = 1,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = 1.5.em,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        modifier = modifier,
        text = s,
        fontWeight = fontWeight,
        color = color,
        fontSize = dpToSp(dp = fontSize.dp),
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
        textAlign = textAlign,
        lineHeight = lineHeight,
        style = style,
    )
}

@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }

@Composable
fun Int.toPx(): Float {
    val dp = this.dp
    return with(LocalDensity.current) { dp.toPx() }
}

@Composable
fun Margin(space: Int) {
    Spacer(modifier = Modifier.size(space.dp))
}

@Composable
fun LoadingView() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.Center)
        )
    }
}
