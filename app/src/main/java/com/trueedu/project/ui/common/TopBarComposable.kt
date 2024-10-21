package com.trueedu.project.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun BackTitleTopBar(
    title: String = "타이틀",
    onBack: () -> Unit = {},
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
    actionIcon2: ImageVector? = null,
    onAction2: (() -> Unit)? = null,
) {
    val iconList = listOfNotNull(
        actionIcon2, actionIcon
    )
    val actionList = listOf(onAction2, onAction)
    val actions: @Composable (RowScope.() -> Unit) =
        {
            Row {
                iconList.zip(actionList).forEach { (icon, action) ->
                    TouchIcon24(icon = icon) { action?.invoke() }
                }
            }
        }
    TopAppBar(
        navigationIcon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onBack() },
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    imageVector = Icons.Filled.ChevronLeft,
                    tint = MaterialTheme.colorScheme.tertiary,
                    contentDescription = "icon"
                )
            }
        },
        title = {
            BasicText(
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

