package com.trueedu.project.ui.views

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.R
import com.trueedu.project.data.WatchList
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.views.search.SearchBar
import com.trueedu.project.ui.views.search.SearchList
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StockSearchFragment: BaseFragment() {
    companion object {
        fun show(
            targetPage: Int? = null,
            fragmentManager: FragmentManager
        ): StockSearchFragment {
            val fragment = StockSearchFragment()
            fragment.targetPage = targetPage ?: 0
            fragment.show(fragmentManager, "stock-search")
            return fragment
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
        }
    }

    fun inWatchList(code: String): Boolean {
        return watchList.contains(targetPage!!, code)
    }

    fun toggleWatchList(code: String) {
        if (inWatchList(code)) {
            watchList.remove(targetPage!!, code)
        } else {
            watchList.add(targetPage!!, code)
        }
    }

    private fun gotoStockDetail(stockInfo: StockInfo) {
        StockDetailFragment.show(stockInfo, parentFragmentManager)
    }
}
