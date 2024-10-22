package com.trueedu.project.ui.topbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.ui.common.TouchIcon32
import com.trueedu.project.utils.NetworkImage


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainTopBar(
    googleAccount: GoogleSignInAccount? = null,
    accountNum: String = "74341523-01",
    onUserInfoClick: () -> Unit = {},
    onAccountInfoClick: () -> Unit = {},
    onWatchListClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSettingClick: () -> Unit = {}
) {
    val title = accountNum.ifEmpty {
        "등록 계좌 없음"
    }
    TopAppBar(
        navigationIcon = {
            if (googleAccount == null) {
                TouchIcon32(icon = Icons.Outlined.AccountCircle, onClick = onUserInfoClick)
            } else {
                val imageUrl = googleAccount.photoUrl
                NetworkImage(
                    imageUrl = imageUrl.toString(),
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(CircleShape)
                        .size(32.dp)
                        .clickable { onUserInfoClick() }
                )
            }
        },
        actions = {
            TouchIcon24(icon = Icons.Outlined.StarOutline, onClick = onWatchListClick)
            TouchIcon24(icon = Icons.Outlined.Search, onClick = onSearchClick)
            TouchIcon24(icon = Icons.Outlined.Settings, onClick = onSettingClick)
        },
        title = { TopBarTitle(title, onAccountInfoClick) },
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