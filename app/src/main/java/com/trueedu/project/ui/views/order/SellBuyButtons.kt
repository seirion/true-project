package com.trueedu.project.ui.views.order

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.theme.ChartColor

@Preview(showBackground = true)
@Composable
fun SellBuyButtons() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(52.dp)
            .padding(horizontal = 16.dp)
    ) {
        val up = ChartColor.up
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f)
                .fillMaxHeight(),
            colors = ButtonColors(
                containerColor = up,
                contentColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent,
            ),
        ) {
            BasicText(
                s = "매수",
                fontSize = 18,
                color = MaterialTheme.colorScheme.background,
            )
        }
        Margin(8)
        val down = ChartColor.down
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f)
                .fillMaxHeight(),
            colors = ButtonColors(
                containerColor = down,
                contentColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent,
            ),
        ) {
            BasicText(
                s = "매도",
                fontSize = 18,
                color = MaterialTheme.colorScheme.background,
            )
        }
    }
}
