package com.trueedu.project.ui.topbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.trueedu.project.ui.common.TouchIcon32


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainTopBar(
    onUserInfoClick: () -> Unit = {},
    onSettingClick: () -> Unit = {}
) {
    TopAppBar(
        navigationIcon = {
            TouchIcon32(Icons.Outlined.AccountCircle, onUserInfoClick)
        },
        actions = {
            TouchIcon32(Icons.Outlined.Settings, onSettingClick)
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