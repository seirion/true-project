package com.trueedu.project.ui.views.search

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.R
import com.trueedu.project.data.WatchList
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.views.StockDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StockSearchFragment: BaseFragment() {
    companion object {
        fun show(
            targetPage: Int? = null,
            fragmentManager: FragmentManager
        ): StockSearchFragment {
            return StockSearchFragment().also {
                it.targetPage = targetPage
                it.show(fragmentManager, "stock-search")
            }
        }
    }

    @Inject
    lateinit var watchList: WatchList

    // 관심종목 추가 시 사용되는 페이지 번호
    var targetPage: Int? = null
    private val vm by viewModels<StockSearchViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenSheetKeyboardDialogTheme)
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("종목 검색", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SearchBar(searchText = vm.searchInput) {}
                SearchList(
                    vm.searchResult.value,
                    ::inWatchList,
                    ::toggleWatchList,
                    ::gotoStockDetail
                )
            }

            if (vm.selectedStock.value != null) {
                Dialog(
                    onDismissRequest = { vm.selectedStock.value = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false) // Important for custom positioning
                ) {
                    PopupBody(vm.selectedStock.value!!)
                }
            }
        }
    }

    fun inWatchList(code: String): Boolean {
        return if (targetPage == null) {
            watchList.contains(code)
        } else {
            watchList.contains(targetPage!!, code)
        }
    }

    fun toggleWatchList(code: String) {
        if (targetPage == null) {
            vm.selectedStock.value = vm.stockPool.get(code)
            return
        }

        val currentOn = inWatchList(code)
        if (currentOn) {
            watchList.remove(targetPage!!, code)
        } else {
            watchList.add(targetPage!!, code)
        }
        trueAnalytics.clickButton(
            "${screenName()}__toggle_watch__click",
            mapOf(
                "code" to code,
                "previous" to currentOn,
                "status" to currentOn,
            )
        )
    }

    private fun gotoStockDetail(stockInfo: StockInfo) {
        trueAnalytics.clickButton("${screenName()}__item__click")
        StockDetailFragment.show(stockInfo, parentFragmentManager)
    }

    @Composable
    private fun PopupBody(
        item: StockInfo,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(
                    MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            TrueText(
                s = item.nameKr,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
            )
            Margin(8)
            DividerHorizontal()
            Column(modifier = Modifier.fillMaxWidth()) {
                repeat(10) { // FIXME
                    SearchPopupItem(it, watchList.contains(it, item.code)) {
                        val currentOn = watchList.contains(it, item.code)
                        if (currentOn) {
                            watchList.remove(it, item.code)
                        } else {
                            watchList.add(it, item.code)
                        }
                    }
                }
            }
            Margin(8)
        }
    }
}
