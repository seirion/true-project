package com.trueedu.project.ui.topbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TopBar(
    onClick: () -> Unit = {}
) {
    TopAppBar(
        actions = {
            Box(
                modifier = Modifier.size(48.dp)
                    .clip(CircleShape)
                    .clickable { onClick() },
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize()
                        .padding(8.dp),
                    imageVector = Icons.Outlined.Settings,
                    tint = MaterialTheme.colorScheme.outline,
                    contentDescription = "icon"
                )
            }
        },
        title = { TopBarTitle("True Project") },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun TopBarTitle(text: String) {
    Text(text = text)
}