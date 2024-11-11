package com.trueedu.project.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.ui.views.common.DesignatedBadge
import com.trueedu.project.ui.views.common.HaltBadge

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun BackTitleTopBar(
    title: String = "타이틀",
    onBack: (() -> Unit)? = {},
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
    actionIcon2: ImageVector? = null,
    onAction2: (() -> Unit)? = null,
) {
    val iconList = listOfNotNull(
        actionIcon2, actionIcon
    )
    val actionList = listOfNotNull(onAction2, onAction)
    val actions: @Composable (RowScope.() -> Unit) =
        {
            Row {
                iconList.zip(actionList).forEach { (icon, action) ->
                    TouchIcon24(icon = icon) { action.invoke() }
                }
            }
        }

    TopAppBar(
        navigationIcon = {
            if (onBack != null) {
                TouchIcon32(
                    icon = Icons.Filled.ChevronLeft,
                    onClick = onBack,
                )
            }
        },
        title = {
            TrueText(
                s = title,
                fontSize = 20,
                color = MaterialTheme.colorScheme.primary
            )
        },
        actions = actions,
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun BackStockTopBar(
    nameKr: String = "삼성전자",
    price: String = "13,000",
    priceChange: String = "+1,150(+1.15%)",
    textColor: Color = ChartColor.up,
    halt: Boolean = false,
    designated: Boolean = false,
    onBack: () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {},
) {
    TopAppBar(
        title = {
            Column {
                Row {
                    TrueText(
                        s = nameKr,
                        fontSize = 16,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (halt) {
                        Margin(2)
                        HaltBadge()
                    }
                    if (designated) {
                        Margin(2)
                        DesignatedBadge()
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TrueText(s = price, fontSize = 14, fontWeight = FontWeight.W600, color = textColor)
                    Margin(6)
                    TrueText(s = priceChange, fontSize = 14, color = textColor)
                }
            }
        },
        navigationIcon = {
            TouchIcon32(
                icon = Icons.Filled.ChevronLeft,
                onClick = onBack,
            )
        },
        actions = actions,
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
