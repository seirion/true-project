package com.trueedu.project.ui.views.setting

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.R
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.UserInfo
import com.trueedu.project.extensions.getClipboardText
import com.trueedu.project.model.local.UserKey
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
class AppKeyInputFragment: BaseFragment() {
    companion object {
        private val TAG = AppKeyInputFragment::class.java.simpleName

        fun show(
            fragmentManager: FragmentManager
        ): AppKeyInputFragment {
            val fragment = AppKeyInputFragment()
            fragment.show(fragmentManager, "appkey")
            return fragment
        }
    }

    @Inject
    lateinit var tokenKeyManager: TokenKeyManager

    private val appKey = mutableStateOf("")
    private val appSecret = mutableStateOf("")
    private val accountNumber = mutableStateOf("")
    private val buttonEnabled = mutableStateOf(false)

    private lateinit var appKeyOrg: String
    private lateinit var appSecretOrg: String
    private lateinit var accountNumberOrg: String

    @Inject
    lateinit var local: Local

    @Inject
    lateinit var userInfo: UserInfo

    override fun init() {
        val userKey = tokenKeyManager.userKey
        appKeyOrg = userKey?.appKey ?: ""
        appSecretOrg = userKey?.appSecret ?: ""
        accountNumberOrg = userKey?.accountNum ?: ""
        appKey.value = userKey?.appKey ?: ""
        appSecret.value = userKey?.appSecret ?: ""
        accountNumber.value = userKey?.accountNum ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenSheetKeyboardDialogTheme)
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("appkey 설정", ::dismissAllowingStateLoss) },
            bottomBar = { BottomBar("저장", buttonEnabled.value, ::onSave) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            val state = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(state)
            ) {
                AppKeySecretInput()
            }
        }
    }

    private fun onAppKeyChanged(key: String) {
        appKey.value = key
        checkUpdate()
    }

    private fun onAppSecretChanged(secret: String) {
        appSecret.value = secret
        checkUpdate()
    }

    private fun onAccountNumberChanged(text: String) {
        accountNumber.value = text.filter(Char::isDigit).take(10)
        checkUpdate()
    }

    private fun pasteAppKey() {
        trueAnalytics.clickButton("${screenName()}__paste_app_key__click")
        val text = getClipboardText(requireContext()) ?: return
        onAppKeyChanged(text)
    }

    private fun pasteAppSecret() {
        trueAnalytics.clickButton("${screenName()}__paste_app_secret__click")
        val text = getClipboardText(requireContext()) ?: return
        onAppSecretChanged(text)
    }

    private fun pasteAccountNumber() {
        trueAnalytics.clickButton("${screenName()}__paste_account_number__click")
        val text = getClipboardText(requireContext()) ?: return
        onAccountNumberChanged(text)
    }

    private fun checkUpdate() {
        buttonEnabled.value = appKey.value.isNotBlank() &&
                appSecret.value.isNotBlank() &&
                accountNumber.value.isNotBlank() &&
                (appKey.value != appKeyOrg || appSecret.value != appSecretOrg || accountNumber.value != accountNumberOrg)
    }

    private fun onSave() {
        trueAnalytics.clickButton("${screenName()}__bottom_btn__click")
        checkToken()
    }

    private fun checkToken() {
        tokenKeyManager.issueAccessToken(
            appKey = appKey.value,
            appSecret = appSecret.value,
            onSuccess = {
                Log.d(TAG, "new token issued")
                Toast.makeText(requireContext(), "토큰 정상 발급 완료", Toast.LENGTH_SHORT).show()
                checkAccount()
            },
            onFailed = {
                Log.d(TAG, "failed: $it")
                Toast.makeText(requireContext(), "토큰 발급 실패", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun checkAccount() {
        userInfo.loadUserStocks(
            accountNum = accountNumber.value,
            onSuccess = {
                Log.d(TAG, "account check ok")
                saveUserKey()
                Toast.makeText(requireContext(), "계좌 번호 확인 완료", Toast.LENGTH_SHORT).show()
                dismissAllowingStateLoss()
            },
            onFail = {
                Log.d(TAG, "failed to get account: $it")
                Toast.makeText(requireContext(), "계좌 번호 오류", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveUserKey() {
        val userKey = UserKey(
            appKey = appKey.value,
            appSecret = appSecret.value,
            accountNum = accountNumber.value,
            htsId = null, // TODO
        )

        tokenKeyManager.addUserKey(userKey)
    }

    @Composable
    private fun AppKeySecretInput() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextInputItem("appkey", appKey.value, ::pasteAppKey, ::onAppKeyChanged)
            TextInputItem("appsecret", appSecret.value, ::pasteAppSecret, ::onAppSecretChanged)
            TextInputItem("계좌번호(숫자)", accountNumber.value, ::pasteAccountNumber, ::onAccountNumberChanged, true)
        }
    }
}


@Composable
private fun TextInputItem(
    label: String,
    value: String,
    onPaste: () -> Unit,
    onValueChange: (String) -> Unit,
    digitOnly: Boolean = false,
) {
    val keyboardOptions = if (digitOnly) {
        KeyboardOptions(keyboardType = KeyboardType.Number)
    } else {
        KeyboardOptions(keyboardType = KeyboardType.Text)
    }
    Margin(4)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            label = {
                BasicText(
                    s = label,
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            modifier = Modifier.weight(1f),
            keyboardOptions = keyboardOptions,
            maxLines = 2,
        )
        Margin(8)
        TouchIcon24(Icons.Filled.ContentPaste, onPaste)
    }
}