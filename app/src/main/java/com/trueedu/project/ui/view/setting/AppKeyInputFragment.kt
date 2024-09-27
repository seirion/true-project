package com.trueedu.project.ui.view.setting

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.treuedu.project.R
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.extensions.getClipboardText
import com.trueedu.project.model.dto.TokenRequest
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AuthRemote
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.ui.theme.TrueProjectTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class AppKeyInputFragment: BottomSheetDialogFragment() {
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
    lateinit var screen: ScreenControl

    private val appKey = mutableStateOf("")
    private val appSecret = mutableStateOf("")
    private val buttonEnabled = mutableStateOf(false)

    private lateinit var appKeyOrg: String
    private lateinit var appSecretOrg: String

    @Inject
    lateinit var local: Local

    @Inject
    lateinit var authRemote: AuthRemote

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FillScreenSheetTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            (this as? BottomSheetDialog)?.behavior?.run {
                this.skipCollapsed = true
                this.state = BottomSheetBehavior.STATE_EXPANDED
                this.isDraggable = true
            }
        }
    }

    private fun init() {
        appKeyOrg = local.appKey
        appSecretOrg = local.appSecret
        appKey.value = local.appKey
        appSecret.value = local.appSecret
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        init()
        return ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setContent {
                TrueProjectTheme(
                    n = screen.theme.intValue,
                    forceDark = screen.forceDark.value
                ) {
                    Scaffold(
                        topBar = { BackTitleTopBar("appkey 설정", ::dismissAllowingStateLoss) },
                        bottomBar = { BottomBar(buttonEnabled.value, ::onSave) },
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
        val text = getClipboardText(requireContext()) ?: return
        onAppKeyChanged(text)
    }

    private fun pasteAppSecret() {
        val text = getClipboardText(requireContext()) ?: return
        onAppSecretChanged(text)
    }

    private fun checkUpdate() {
        buttonEnabled.value = appKey.value.isNotBlank() && appSecret.value.isNotBlank() &&
                (appKey.value != appKeyOrg || appSecret.value != appSecretOrg)
    }

    private fun onSave() {
        val request = TokenRequest(
            grantType = "client_credentials",
            appKey = appKey.value,
            appSecret = appSecret.value,
        )
        authRemote.refreshToken(request)
            .catch {
                Log.e(TAG, "failed to get AccessToken: $it")
                // service not available
            }
            .onEach {
                local.accessToken = it.accessToken
                local.appKey = appKey.value
                local.appSecret = appSecret.value
                Log.d(TAG, "new token: $it")
                Toast.makeText(requireContext(), "토큰 정상 발급 완료", Toast.LENGTH_SHORT).show()
                dismissAllowingStateLoss()
            }
            .launchIn(MainScope())
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

@Preview(showBackground = true)
@Composable
private fun BottomBar(
    buttonEnabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 16.dp)
            .height(56.dp)
            .padding(horizontal = 24.dp),
        enabled = buttonEnabled,
    ) {
        val buttonColor = if (buttonEnabled) {
            MaterialTheme.colorScheme.inversePrimary
        } else {
            MaterialTheme.colorScheme.inverseSurface
        }
        BasicText(s = "저장", fontSize = 20, color = buttonColor)
    }
}
