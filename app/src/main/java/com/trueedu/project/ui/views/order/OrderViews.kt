package com.trueedu.project.ui.views.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.cashFormatter

@Composable
fun OrderBook(
    sells: List<Pair<Double, Double>>,
    buys: List<Pair<Double, Double>>,
    price: Double, // 현재가
    previousClose: Double,
    onClick: (Double) -> Unit,
) {
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
        sells.forEach { (p, q) ->
            SellItems(p, q, price, previousClose) {
                onClick(p)
            }
        }
        buys.forEach { (p, q) ->
            BuyItems(p, q, price, previousClose) {
                onClick(p)
            }
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
private fun SellItems(
    price: Double,
    quantity: Double,
    currentPrice: Double,
    previousClose: Double,
    onClick: () -> Unit,
) {
    val priceString = if (price > 0.0) cashFormatter.format(price) else ""
    val quantityString = if (quantity > 0.0) cashFormatter.format(quantity) else ""
    val selected = price != 0.0 && price == currentPrice
    SellBuyItems(
        priceString,
        quantityString,
        selected,
        ChartColor.color(price - previousClose),
        ChartColor.up.copy(alpha = 0.1f),
        onClick,
    )
}

@Composable
private fun BuyItems(
    price: Double,
    quantity: Double,
    currentPrice: Double,
    previousClose: Double,
    onClick: () -> Unit,
) {
    val priceString = if (price > 0.0) cashFormatter.format(price) else ""
    val quantityString = if (quantity > 0.0) cashFormatter.format(quantity) else ""
    val selected = price != 0.0 && price == currentPrice
    SellBuyItems(
        priceString,
        quantityString,
        selected,
        ChartColor.color(price - previousClose),
        ChartColor.down.copy(alpha = 0.1f),
        onClick,
    )
}

@Composable
private fun SellBuyItems(
    price: String,
    count: String,
    selected: Boolean,
    textColor: Color,
    bgColor: Color,
    onClick: () -> Unit,
) {
    val border = if (selected) 1.dp else 0.dp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(48.dp)
            .border(border, textColor)
            .padding(1.dp)
            .clickable { onClick() },
    ) {
        NumberText(price, textColor, bgColor)
        Margin(1)
        NumberText(count, textColor, bgColor)
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