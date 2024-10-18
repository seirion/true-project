package com.trueedu.project.ui.views

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.UserInfo
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
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

    override fun init() {
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("사용자 정보", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                BasicText(
                    s = "로그아웃",
                    fontSize = 16,
                    color = MaterialTheme.colorScheme.primary,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    modifier = Modifier.clickable { onLogout() }
                )
            }
        }
    }

    private fun onLogout() {
        googleAccount.logout(requireContext()) {
            dismissAllowingStateLoss()
            Toast.makeText(requireContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
        }
    }
}
