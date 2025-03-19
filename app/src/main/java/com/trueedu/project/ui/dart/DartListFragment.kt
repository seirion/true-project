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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.BuildConfig
import com.trueedu.project.dart.model.DartListItem
import com.trueedu.project.data.DartManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.CustomTopBar
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TouchIcon32
import com.trueedu.project.ui.common.TouchIconWithSizeRotating
import com.trueedu.project.ui.common.TrueText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

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
    private val num = mutableStateOf(0) // 화면 갱신 용

    override fun init() {
        super.init()

        lifecycleScope.launch {
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
        val items = dartManager.getListMap().map {
            it.key to it.value
        }

        Scaffold(
            topBar = {
                val forceRefresh = if (BuildConfig.DEBUG) {
                    ::forceRefresh
                } else {
                    null
                }
                DartTopBar(items.size, ::dismissAllowingStateLoss, forceRefresh)
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

    private fun forceRefresh() {
        trueAnalytics.clickButton("${screenName()}__force_refresh__click")
        dartManager.forceLoad()
    }

    private fun onItemClick(receiptNum: String) {
        val url = "https://dart.fss.or.kr/dsaf001/main.do?rcpNo=${receiptNum}".toUri()
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
            fontWeight = FontWeight.W700,
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

@Preview(showBackground = true)
@Composable
private fun DartTopBar(
    num: Int = 0,
    onBack: () -> Unit = {},
    onRefresh: (() -> Unit)? = {},
) {
    CustomTopBar(
        navigationIcon = {
            TouchIcon32(
                icon = Icons.Filled.ChevronLeft,
                onClick = onBack,
            )
        },
        titleView = {
            TrueText(
                s = "오늘의 스팩 공시 $num",
                fontSize = 20,
                color = MaterialTheme.colorScheme.primary
            )
        },
        actionsView = {
            if (onRefresh != null) {
                TouchIconWithSizeRotating(
                    size = 24.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    icon = Icons.Outlined.Sync,
                    onClick = onRefresh,
                )
            }
        },
    )
}
