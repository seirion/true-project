package com.trueedu.project.ui.topbar

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.TouchIcon32


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainTopBar(
    accountNum: String = "74341523-01",
    onUserInfoClick: () -> Unit = {},
    onSettingClick: () -> Unit = {}
) {
    val title = accountNum.ifEmpty {
        "등록 계좌 없음"
    }
    TopAppBar(
        navigationIcon = {
            TouchIcon32(Icons.Outlined.AccountCircle, onUserInfoClick)
        },
        actions = {
            TouchIcon32(Icons.Outlined.Settings, onSettingClick)
        },
        title = { TopBarTitle(title, onUserInfoClick) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun TopBarTitle(text: String, onClick: () -> Unit) {
    BasicText(
        s = text,
        fontSize = 16,
        color = MaterialTheme.colorScheme.primary,
        style = TextStyle(textDecoration = TextDecoration.Underline),
        modifier = Modifier.clickable { onClick() }
    )
}