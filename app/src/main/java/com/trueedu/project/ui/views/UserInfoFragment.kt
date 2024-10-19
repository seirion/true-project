package com.trueedu.project.ui.views

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.model.local.UserKey
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.views.setting.AppKeyInputFragment
import com.trueedu.project.ui.views.user.AccountNumView
import com.trueedu.project.ui.views.user.AccountView
import com.trueedu.project.ui.views.user.AddIcon
import com.trueedu.project.utils.toAccountNumFormat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserInfoFragment: BaseFragment() {
    companion object {
        private val TAG = UserInfoFragment::class.java.simpleName

        fun show(
            fragmentManager: FragmentManager
        ): UserInfoFragment {
            val fragment = UserInfoFragment()
            fragment.show(fragmentManager, "user-info")
            return fragment
        }
    }

    @Inject
    lateinit var local: Local
    @Inject
    lateinit var googleAccount: GoogleAccount

    private val userKeys = mutableStateOf<List<UserKey>>(emptyList())

    private val selected = mutableIntStateOf(0)

    override fun init() {
        // 마지막 item 이 현재 선택된 item 이므로 역순으로 보여줌
        userKeys.value = local.getUserKeys().reversed()
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    "사용자 정보",
                    ::dismissAllowingStateLoss,
                    Icons.AutoMirrored.Filled.Logout,
                    ::onLogout,
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
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
                    BasicText(
                        s = "계좌 목록",
                        fontSize = 16,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp)
                            .padding(horizontal = 16.dp)
                    )
                }

                itemsIndexed(userKeys.value, key = { _, item -> item.accountNum!! }) { index, item ->
                    AccountNumView(
                        item.accountNum.toAccountNumFormat(),
                        selected = selected.intValue == index,
                    ) {
                    }
                }

                item { AddIcon(::onAddUserKey) }
            }
        }
    }

    private fun onLogout() {
        googleAccount.logout(requireContext()) {
            dismissAllowingStateLoss()
            Toast.makeText(requireContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onAddUserKey() {
        AppKeyInputFragment.show(parentFragmentManager) {

        }
    }
}
