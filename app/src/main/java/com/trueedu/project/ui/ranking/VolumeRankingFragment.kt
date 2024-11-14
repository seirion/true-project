package com.trueedu.project.ui.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.model.dto.rank.VolumeRankingOutput
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.common.listBackgroundColor
import com.trueedu.project.utils.formatter.intFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VolumeRankingFragment: BaseFragment() {
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

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("거래량 상위 30", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->

            if (vm.loading.value) {
                LoadingView()
            } else if (vm.loadingFail.value) {
                TrueText(
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
    ) {
        itemsIndexed(list, key = { _, item -> item.code }) { index, item ->
            val bgColor = listBackgroundColor(index)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .background(color = bgColor)
                    .padding(vertical = 4.dp)
                    .padding(horizontal = 8.dp)
            ) {
                TrueText(
                    s = "${item.rank}. ${item.nameKr}",
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                val volumeString = intFormatter.format(item.volume.toDouble())
                TrueText(
                    s = volumeString,
                    fontSize = 13,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}
