package com.trueedu.project.ui.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.treuedu.project.R
import com.trueedu.project.Greeting
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.theme.TrueProjectTheme
import com.trueedu.project.ui.topbar.TopBar

class SettingFragment: BottomSheetDialogFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): SettingFragment {
            val fragment = SettingFragment()
            fragment.show(fragmentManager, "setting")
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FillScreenSheetTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            (this as? BottomSheetDialog)?.behavior?.run {
                this.skipCollapsed = true
                this.state = BottomSheetBehavior.STATE_EXPANDED
                this.isDraggable = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setContent {
                TrueProjectTheme {
                    Scaffold(
                        topBar = {},
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.background),
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier.fillMaxSize().padding(innerPadding)
                        ) {
                            SettingItem("appkey 설정", {})
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingItem(
    text: String = "나의 설정",
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp)
            .height(60.dp)
    ) {
        BasicText(
            s = text,
            fontSize = 14,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    DividerHorizontal()
}
