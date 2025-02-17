package com.trueedu.project.ui.views.watch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText


@Preview(showBackground = true)
@Composable
fun StockDetailWatchingPopup(
    nameKr: String = "삼성전자",
    pageCount: Int = 10,
    watchingList: List<Boolean> = List(pageCount) { false },
    toggle: (Int) -> Unit = { _ -> }, // page
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .background(
                MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        TrueText(
            s = nameKr,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
        )
        Margin(8)
        DividerHorizontal()
        Column(modifier = Modifier.fillMaxWidth()) {
            repeat(pageCount) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                        .clickable {
                            toggle(it)
                        }
                ) {
                    TrueText(
                        s = "관심 그룹 $it",
                        fontSize = 16,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )

                    val isWatching = watchingList[it]
                    val icon = if (isWatching) Icons.Filled.Star else Icons.Outlined.StarOutline
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = icon,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "watching-icon"
                    )
                }
                DividerHorizontal()
            }
        }
        Margin(8)
    }
}
