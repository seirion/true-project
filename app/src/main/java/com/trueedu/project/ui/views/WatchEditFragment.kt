package com.trueedu.project.ui.views

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.WatchList
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.TrueText
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
    private var dirty = false

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
                    dirty = true
                    true
                },
                onEditMoveCallback = { drop ->
                    if (drop && dirty) save()
                },
                modifier = Modifier.padding(innerPadding),
                keyValue = { item, _ -> item.code },
            ) { stock, _ ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f),
                    ) {
                        TrueText(
                            s = stock.nameKr,
                            fontSize = 14,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                        )
                        TrueText(
                            s = "(${stock.code})",
                            fontSize = 13,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    HandleIcon()
                }
            }
        }
    }

    private fun save() {
        val list = items.value.map { it.code }
        watchList.replace(page, list)
        dirty = false
    }
}

@Composable
private fun HandleIcon() {
    Icon(
        imageVector = Icons.Filled.Reorder,
        contentDescription = "reorder",
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.secondary,
    )
}
