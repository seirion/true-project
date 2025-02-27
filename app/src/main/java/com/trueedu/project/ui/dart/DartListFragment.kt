package com.trueedu.project.ui.dart

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.dart.model.DartListItem
import com.trueedu.project.data.DartManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DartListFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): DartListFragment {
            val fragment = DartListFragment()
            fragment.show(fragmentManager, "dart-list")
            return fragment
        }
    }

    @Inject
    lateinit var stockPool: StockPool

    @Inject
    lateinit var spacManager: SpacManager

    @Inject
    lateinit var dartManager: DartManager

    private val loading = mutableStateOf(false)
    private val num = mutableStateOf(0)

    override fun init() {
        super.init()

        lifecycleScope.launch {
            launch {
                snapshotFlow { spacManager.spacList.value }
                    .filterNot { it.isEmpty() }
                    .collectLatest {
                        val codes = it.map { it.code }
                        dartManager.loadList(codes)
                    }
            }
            launch {
                dartManager.updateSignal
                    .collectLatest {
                        num.value = dartManager.getSize()
                    }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                BackTitleTopBar("오늘의 스팩 공시 ${num.value}", ::dismissAllowingStateLoss)
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->

            if (loading.value) {
                LoadingView()
                return@Scaffold
            }

            num.value
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.padding(innerPadding)
            ) {
                val items = dartManager.getListMap().map {
                    it.key to it.value
                }
                stickyHeader { Header() }
                itemsIndexed(items, key = { _, item -> item.first }) { _, (code, list) ->
                    val stock = stockPool.get(code) ?: return@itemsIndexed
                    NameView(stock.nameKr, stock.code)
                    list.forEach {
                        DartListItemView(it, ::onItemClick)
                    }
                    Margin(4)
                    DividerHorizontal()
                }
            }
        }
    }

    private fun onItemClick(receiptNum: String) {
        val url = Uri.parse("https://dart.fss.or.kr/dsaf001/main.do?rcpNo=${receiptNum}")
        requireActivity().startActivity(Intent(Intent.ACTION_VIEW, url))
    }

    @Composable
    private fun Header() {
    }
}

@Composable
private fun NameView(nameKr: String, code: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = 8.dp)
    ) {
        TrueText(
            s = nameKr,
            fontSize = 14,
            color = MaterialTheme.colorScheme.primary,
        )
        Margin(4)
        TrueText(
            s = "(${code})",
            fontSize = 12,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun DartListItemView(
    item: DartListItem,
    onClick: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clickable { onClick(item.receiptNum) }
            .padding(start = 24.dp, end = 8.dp)
    ) {
        TrueText(
            s = item.reportName,
            fontSize = 14,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
