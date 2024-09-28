package com.trueedu.project.ui.ranking

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.trueedu.project.R
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.model.dto.VolumeRankingOutput
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.theme.TrueProjectTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VolumeRankingFragment: BottomSheetDialogFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): VolumeRankingFragment {
            val fragment = VolumeRankingFragment()
            fragment.show(fragmentManager, "setting")
            return fragment
        }
    }

    private val vm by viewModels<VolumeRankingViewModel>()

    @Inject
    lateinit var screen: ScreenControl

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
                TrueProjectTheme(
                    n = screen.theme.intValue,
                    forceDark = screen.forceDark.value
                ) {
                    Scaffold(
                        topBar = { BackTitleTopBar("거래량 상위 30", ::dismissAllowingStateLoss) },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.background),
                    ) { innerPadding ->

                        if (vm.loading.value) {
                            LoadingView()
                        } else if (vm.loadingFail.value) {
                            BasicText(
                                s = "데이터 로딩 실패",
                                fontSize = 18,
                                color = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            Column(modifier = Modifier.padding(innerPadding)) {
                                StockList(vm.list)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockList(
    list: List<VolumeRankingOutput>
) {
    val state = rememberLazyListState()
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        itemsIndexed(list, key = { _, item -> item.code }) { index, item ->
            BasicText(
                s = "${item.rank}. ${item.nameKr} (${item.code})",
                fontSize = 14,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
