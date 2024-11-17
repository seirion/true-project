package com.trueedu.project.admin.spac

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.intFormatter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SpacAdminFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): SpacAdminFragment {
            return SpacAdminFragment().also {
                it.show(fragmentManager, "spac_admin")
            }
        }
    }

    @Inject
    lateinit var stockPool: StockPool

    private val namePrices = mutableStateOf<List<Pair<String, Int>>>(emptyList())

    override fun init() {
        namePrices.value = spacRedemptionPrices
            .map { it.split(" ") }
            .map { it.first().trim() to it.last().toInt() }
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = "스팩 청산 가격",
                    onBack = ::dismissAllowingStateLoss,
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (stockPool.status.value != StockPool.Status.SUCCESS) {
                    item { LoadingView() }
                    return@LazyColumn
                }
                itemsIndexed(namePrices.value, key = { _, item -> item.first }) { i, item ->
                    val nameKr = item.first
                    val stock = stockPool.search { it.nameKr == nameKr }.firstOrNull()
                    val code = stock?.code ?: ""
                    val currentPrice = stock?.prevPrice()?.toDouble() ?: 0.0
                    val beforeTax = beforeTax(item.second) // 세전으로 표시
                    SpacItemInternal(nameKr, code, currentPrice, beforeTax)
                }
            }
        }
    }

    private fun beforeTax(p: Int): Int {
        val base = if (p > 8_000) 10_000 else 2_000
        return ((p - base) / 0.845).toInt() + base
    }
}

@Composable
private fun SpacItemInternal(
    nameKr: String,
    code: String,
    currentPrice: Double,
    spacRedemptionPrices: Int,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp)
    ) {
        Column {
            TrueText(
                s = nameKr,
                fontSize = 14,
                color = MaterialTheme.colorScheme.primary,
            )
            TrueText(
                s = code,
                fontSize = 12,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        val color = ChartColor.color(spacRedemptionPrices - currentPrice)
        Column(horizontalAlignment = Alignment.End) {
            TrueText(
                s = intFormatter.format(currentPrice),
                fontSize = 13,
                color = MaterialTheme.colorScheme.primary,
            )
            TrueText(
                s = intFormatter.format(spacRedemptionPrices),
                fontSize = 14,
                color = color,
            )
        }
    }
}