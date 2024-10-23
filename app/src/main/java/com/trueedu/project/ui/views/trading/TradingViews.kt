package com.trueedu.project.ui.views.trading

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.theme.ChartColor

@Preview(showBackground = true)
@Composable
fun OrderBook() {
    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        val scrollRange = scrollState.maxValue
        scrollState.scrollTo(scrollRange / 2)
    }
    Column(
        modifier = Modifier
            .width(160.dp)
            .fillMaxHeight()
            .verticalScroll(scrollState),
    ) {
        repeat(10) {
            SellItems()
        }
        repeat(10) {
            BuyItems()
        }
    }
}

@Composable
fun Section() {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(160.dp)
            .height(24.dp)
            .border(width = 1.dp, borderColor)
            .padding(vertical = 2.dp),
    ) {
        val textColor = MaterialTheme.colorScheme.primary
        val bgColor = MaterialTheme.colorScheme.background
        NumberText("호가", textColor, bgColor)
        VerticalDivider(thickness = 1.dp, color = borderColor)
        Margin(1)
        NumberText("잔량", textColor, bgColor)
    }
}

@Composable
private fun SellItems() {
    SellBuyItems(
        ChartColor.up,
        ChartColor.up.copy(alpha = 0.1f)
    )
}

@Composable
private fun BuyItems() {
    SellBuyItems(
        ChartColor.down,
        ChartColor.down.copy(alpha = 0.1f)
    )
}

@Composable
private fun SellBuyItems(
    textColor: Color,
    bgColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(48.dp)
            .padding(1.dp),
    ) {
        NumberText("124,000", textColor, bgColor)
        Margin(1)
        NumberText("457", textColor, bgColor)
    }
}

@Composable
private fun RowScope.NumberText(
    s: String,
    textColor: Color,
    bgColor: Color,
) {
    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier.weight(1f)
            .fillMaxHeight()
            .background(color = bgColor)
            .padding(end = 4.dp),
    ) {
        BasicText(
            s = s,
            fontSize = 14,
            color = textColor,
        )
    }
}