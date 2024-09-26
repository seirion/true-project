package com.trueedu.project.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp

@Composable
fun DividerHorizontal() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline,
        thickness = 1.dp,
    )
}

@Composable
fun BasicText(
    s: String,
    fontSize: Int,
    modifier: Modifier = Modifier.wrapContentSize(),
    fontWeight: FontWeight = FontWeight.W400,
    color: Color = Color.Black,
    maxLines: Int = 1,
    textAlign: TextAlign? = null,
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
