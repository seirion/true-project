package com.trueedu.project.ui.view.setting

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.data.TokenControl
import com.trueedu.project.extensions.getClipboardText
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AuthRemote
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
    lateinit var tokenControl: TokenControl

    private val appKey = mutableStateOf("")
    private val appSecret = mutableStateOf("")
    private val buttonEnabled = mutableStateOf(false)

    private lateinit var appKeyOrg: String
    private lateinit var appSecretOrg: String

    @Inject
    lateinit var local: Local

    @Inject
    lateinit var authRemote: AuthRemote

    override fun init() {
        appKeyOrg = local.appKey
        appSecretOrg = local.appSecret
        appKey.value = local.appKey
        appSecret.value = local.appSecret
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AppKeySecretInput(
                    pasteAppKey = ::pasteAppKey,
                    pasteAppSecret = ::pasteAppSecret,
                    onAppKeyChanged = ::onAppKeyChanged,
                    onAppSecretChanged = ::onAppSecretChanged
                )
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

    private fun checkUpdate() {
        buttonEnabled.value = appKey.value.isNotBlank() && appSecret.value.isNotBlank() &&
                (appKey.value != appKeyOrg || appSecret.value != appSecretOrg)
    }

    private fun onSave() {
        trueAnalytics.clickButton("${screenName()}__bottom_btn__click")

        tokenControl.issueAccessToken(
            appKey = appKey.value,
            appSecret = appSecret.value,
            onSuccess = {
                Log.d(TAG, "new token issued")
                Toast.makeText(requireContext(), "토큰 정상 발급 완료", Toast.LENGTH_SHORT).show()
                dismissAllowingStateLoss()
            },
            onFailed = {
                Log.d(TAG, "failed: $it")
                Toast.makeText(requireContext(), "토큰 발급 실패", Toast.LENGTH_SHORT).show()
            }
        )
    }

    @Composable
    private fun AppKeySecretInput(
        pasteAppKey: () -> Unit,
        pasteAppSecret: () -> Unit,
        onAppKeyChanged: (String) -> Unit,
        onAppSecretChanged: (String) -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = appKey.value,
                    onValueChange = {
                        onAppKeyChanged(it)
                    },
                    label = {
                        BasicText(
                            s = "appkey",
                            fontSize = 14,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Margin(8)
                TouchIcon24(Icons.Filled.ContentPaste, pasteAppKey)
            }
            Margin(4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = appSecret.value,
                    onValueChange = {
                        onAppSecretChanged(it)
                    },
                    label = {
                        BasicText(
                            s = "appsecret",
                            fontSize = 14,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Margin(8)
                TouchIcon24(Icons.Filled.ContentPaste, pasteAppSecret)
            }
        }
    }
}
