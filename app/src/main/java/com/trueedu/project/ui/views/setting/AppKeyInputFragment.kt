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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.R
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.UserAssets
import com.trueedu.project.extensions.getClipboardText
import com.trueedu.project.model.event.TokenIssueFail
import com.trueedu.project.model.event.TokenIssued
import com.trueedu.project.model.local.UserKey
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.common.BottomBar
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TouchIcon24
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppKeyInputFragment: BaseFragment() {
    companion object {
        private val TAG = AppKeyInputFragment::class.java.simpleName

        fun show(
            isNewKey: Boolean,
            fragmentManager: FragmentManager
        ): AppKeyInputFragment {
            val fragment = AppKeyInputFragment()
            fragment.isNewKey = isNewKey
            fragment.show(fragmentManager, "appkey")
            return fragment
        }
    }

    @Inject
    lateinit var tokenKeyManager: TokenKeyManager

    var isNewKey = false

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
    lateinit var userAssets: UserAssets

    override fun init() {
        val userKey = if (isNewKey) null else tokenKeyManager.userKey.value
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
        saveUserKey()

        // 토큰이 정상적으로 발급되면 창을 닫는다
        lifecycleScope.launch {
            tokenKeyManager.observeTokenKeyEvent()
                .onEach {
                    if (it is TokenIssued) {
                        Log.d(TAG, "new token issued")
                        Toast.makeText(requireContext(), "토큰 정상 발급 완료", Toast.LENGTH_SHORT)
                            .show()
                        dismissAllowingStateLoss()
                    } else if (it is TokenIssueFail) {
                        Log.d(TAG, "failed: $it")
                        Toast.makeText(requireContext(), "토큰 발급 실패", Toast.LENGTH_SHORT).show()
                        this.cancel()
                    }
                }
                .collect {
                }
        }
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

    @Preview(showBackground = true)
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

            TrueText(
                s = notice,
                fontSize = 10,
                color = MaterialTheme.colorScheme.tertiary,
                maxLines = Int.MAX_VALUE,
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 56.dp)
            )
        }
    }
}

private val notice = """
    앱 키와 시크릿은 서버에 저장되지 않습니다.
    타인에게 노출 되지 않도록 주의해 주세요.
""".trimIndent()


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
                TrueText(
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
        TouchIcon24(icon = Icons.Filled.ContentPaste, onClick = onPaste)
    }
}
