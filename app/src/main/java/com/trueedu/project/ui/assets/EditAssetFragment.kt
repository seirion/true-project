package com.trueedu.project.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BottomBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditAssetFragment: BaseFragment() {
    companion object {
        fun show(
            code: String,
            fragmentManager: FragmentManager
        ): EditAssetFragment {
            return EditAssetFragment().also {
                it.code = code
                it.show(fragmentManager, "edit-asset")
            }
        }
    }

    lateinit var code: String

    private val buttonEnabled = mutableStateOf(false)

    @Composable
    override fun BodyScreen() {
        if (!::code.isInitialized) dismissAllowingStateLoss()
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = "보유 종목 추가",
                    onBack = ::dismissAllowingStateLoss,
                )
            },
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
            }
        }
    }

    private fun onSave() {

    }
}