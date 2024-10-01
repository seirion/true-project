package com.trueedu.project.ui.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.data.UserInfo
import com.trueedu.project.extensions.getClipboardText
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.BottomBar
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TouchIcon24
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserInfoFragment: BaseFragment() {
    companion object {
        private val TAG = UserInfoFragment::class.java.simpleName

        private const val ACCOUNT_NUM_LENGTH = 10

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
    lateinit var userInfo: UserInfo

    // 계좌 번호
    private val accountNumber = mutableStateOf("")

    private val buttonEnabled = mutableStateOf(false)

    override fun init() {
        accountNumber.value = local.currentAccountNumber
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("사용자 정보", ::dismissAllowingStateLoss) },
            bottomBar = { BottomBar("확인", buttonEnabled.value, ::onConfirm) },
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
                AccountNumInput()
            }
        }
    }

    private fun onAccountNumberChanged(text: String) {
        accountNumber.value = text.filter(Char::isDigit) // 숫자만
            .take(ACCOUNT_NUM_LENGTH)
        buttonEnabled.value = accountNumber.value.length == ACCOUNT_NUM_LENGTH
    }

    private fun pasteAccountNum() {
        val text = getClipboardText(requireContext()) ?: return
        onAccountNumberChanged(text)
    }

    private fun onConfirm() {
        trueAnalytics.clickButton("${screenName()}__bottom_btn__click")

        userInfo.loadAccount(
            accountNum = accountNumber.value,
            onSuccess = {
                Log.d(TAG, "account ok")
                local.currentAccountNumber = accountNumber.value
            },
            onFail = {
                Log.d(TAG, "failed to get account: $it")
            }
        )
    }

    // 계좌번호 입력하기
    @Preview(showBackground = true)
    @Composable
    private fun AccountNumInput() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = accountNumber.value,
                onValueChange = {
                    onAccountNumberChanged(it)
                },
                label = {
                    BasicText(
                        s = "계좌번호(숫자만)",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                maxLines = 1,
            )
            Margin(8)
            TouchIcon24(Icons.Filled.ContentPaste, ::pasteAccountNum)
        }
    }
}
