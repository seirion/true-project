package com.trueedu.project.ui.view.setting

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.treuedu.project.R
import com.trueedu.project.model.dto.TokenRequest
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AuthRemote
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.Margin
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
                TrueProjectTheme {
                    Scaffold(
                        topBar = { TopBar(::dismissAllowingStateLoss) },
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

    private fun checkUpdate() {
        buttonEnabled.value = appKey.value.isNotBlank() && appSecret.value.isNotBlank() &&
                (appKey.value != appKeyOrg || appSecret.value != appSecretOrg)
    }

    private fun onSave() {
        local.appKey = appKey.value
        local.appKey = appKey.value

        val request = TokenRequest(
            grantType = "client_credentials",
            appKey = local.appKey,
            appSecret = local.appSecret,
        )
        authRemote.refreshToken(request)
            .catch {
                Log.e(TAG, "failed to get AccessToken: $it")
                // service not available
            }
            .onEach {
                local.accessToken = it.accessToken
                Log.d(TAG, "new token: $it")
                Toast.makeText(requireContext(), "토큰 정상 발급 완료", Toast.LENGTH_SHORT).show()
                dismissAllowingStateLoss()
            }
            .launchIn(MainScope())
    }

    @Composable
    private fun AppKeySecretInput(
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
                    label = { BasicText("appkey", 14) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Margin(8)
                TouchIcon(Icons.Filled.ContentPaste) {}
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
                    label = { BasicText("appsecret", 14) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Margin(8)
                TouchIcon(Icons.Filled.ContentPaste) {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TopBar(
    onBack: () -> Unit = {},
) {
    TopAppBar(
        navigationIcon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onBack() },
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    imageVector = Icons.Filled.ChevronLeft,
                    tint = MaterialTheme.colorScheme.tertiary,
                    contentDescription = "icon"
                )
            }
        },
        title = { BasicText("appkey 설정", 20) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
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


@Composable
fun TouchIcon(
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() },
    ) {
        Icon(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            imageVector = icon,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "icon"
        )
    }
}
