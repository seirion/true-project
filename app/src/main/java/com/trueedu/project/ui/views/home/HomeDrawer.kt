package com.trueedu.project.ui.views.home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.views.UserInfoViewModel
import com.trueedu.project.ui.views.setting.AppKeyInputFragment
import com.trueedu.project.ui.views.user.AccountNumView
import com.trueedu.project.ui.views.user.AccountView
import com.trueedu.project.ui.views.user.AddIcon
import com.trueedu.project.utils.toAccountNumFormat

@Composable
fun HomeDrawer(
    context: Context,
    vm: UserInfoViewModel,
    googleAccount: GoogleAccount,
    trueAnalytics: TrueAnalytics,
    fragmentManager: FragmentManager,
    close: () -> Unit,
) {
    val onLogout = {
        trueAnalytics.clickButton("home_drawer_logout__click")
        googleAccount.logout(context) {
            Toast.makeText(context, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
            close()
        }
    }

    val onAddUserKey: () -> Unit = {
        trueAnalytics.clickButton("home_drawer_add_user_key__click")
        AppKeyInputFragment.show(true, fragmentManager)
        close()
    }

    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = "사용자 정보",
                onBack = close,
                actionIcon = Icons.AutoMirrored.Filled.Logout,
                onAction = onLogout,
            )
        },
        modifier = Modifier.fillMaxHeight()
            .padding(end = 60.dp)
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) { innerPadding ->
        val state = rememberLazyListState()
        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 16.dp)
        ) {
            item {
                val imageUrl = googleAccount.googleSignInAccount?.photoUrl
                AccountView(
                    imageUrl = imageUrl?.toString() ?: "",
                    email = googleAccount.getEmail() ?: "",
                )
            }
            item {
                TrueText(
                    s = "계좌 목록",
                    fontSize = 16,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp)
                        .padding(horizontal = 16.dp)
                )
            }

            itemsIndexed(vm.userKeys.value, key = { _, item -> item.accountNum!! }) { index, item ->
                AccountNumView(
                    item.accountNum.toAccountNumFormat(),
                    selected = vm.selected.intValue == index,
                    onDelete = {
                        trueAnalytics.clickButton("home_drawer_delete__click")
                        vm.delete(item.accountNum!!)
                        Toast.makeText(context, "삭제했습니다", Toast.LENGTH_SHORT).show()
                    },
                    onClick = {
                        vm.onSelected(index)
                        close()
                    }
                )
            }

            item { AddIcon(onAddUserKey) }
        }
    }
}