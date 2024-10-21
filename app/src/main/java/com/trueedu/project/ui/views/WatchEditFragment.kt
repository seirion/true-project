package com.trueedu.project.ui.views

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.WatchList
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.widget.dragDropColumn
import com.trueedu.project.ui.widget.swap
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WatchEditFragment: BaseFragment() {
    companion object {
        private val TAG = WatchEditFragment::class.java.simpleName

        fun show(
            page: Int,
            fragmentManager: FragmentManager
        ): WatchEditFragment {
            val fragment = WatchEditFragment()
            fragment.page = page
            fragment.show(fragmentManager, "watch-list-edit")
            return fragment
        }
    }

    private var page: Int = -1
    private val items = mutableStateOf<List<StockInfo>>(emptyList())

    @Inject
    lateinit var stockPool: StockPool
    @Inject
    lateinit var watchList: WatchList

    override fun init() {
        items.value = watchList.get(page)
            .mapNotNull { stockPool.get(it) }
        if (items.value.isEmpty()) dismissAllowingStateLoss()
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = "관심 종목 편집",
                    onBack = ::dismissAllowingStateLoss,
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            dragDropColumn(
                items = items.value,
                onSwap = { from, to ->
                    Log.d(TAG, "swap $from $to")
                    val list = items.value.toMutableList()
                    swap(list, from, to)
                    items.value = list
                    save()
                    true
                },
                modifier = Modifier.padding(innerPadding),
                keyValue = { item, position -> item.code },
            ) { stock, _ ->
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    BasicText(
                        s = stock.nameKr,
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                    BasicText(
                        s = "(${stock.code})",
                        fontSize = 13,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }

    private fun save() {
        val list = items.value.map { it.code }
        watchList.replace(page, list)
    }
}
