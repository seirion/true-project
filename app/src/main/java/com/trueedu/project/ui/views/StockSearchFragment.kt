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
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.views.search.SearchBar
import com.trueedu.project.ui.views.search.SearchList
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StockSearchFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): StockSearchFragment {
            val fragment = StockSearchFragment()
            fragment.show(fragmentManager, "stock-search")
            return fragment
        }
    }

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
                SearchList(vm.searchResult.value)
            }
        }
    }
}
