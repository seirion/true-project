package com.trueedu.project.ui.views.schedule

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.R
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.views.order.SellBuyButtons
import com.trueedu.project.ui.views.search.SearchBar
import com.trueedu.project.ui.widget.InputSet
import com.trueedu.project.utils.formatter.safeLong
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleAddFragment: BaseFragment() {
    companion object {
        private val TAG = ScheduleAddFragment::class.java.simpleName

        fun show(
            fragmentManager: FragmentManager,
            onCompleted: (OrderSchedule) -> Unit,
        ): ScheduleAddFragment {
            return ScheduleAddFragment().also {
                it.onCompleted = onCompleted
                it.show(fragmentManager, "schedule_add")
            }
        }
    }

    @Inject
    lateinit var stockPool: StockPool

    var onCompleted: (OrderSchedule) -> Unit = {}

    private val code = mutableStateOf("")

    private val searchMode = mutableStateOf(true)

    private val searchInput = mutableStateOf("")

    private val searchResult = mutableStateOf<List<StockInfo>>(emptyList())

    private val priceInput = mutableStateOf(TextFieldValue(""))

    private val quantityInput = mutableStateOf(TextFieldValue("1"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenSheetKeyboardDialogTheme)
    }

    @OptIn(FlowPreview::class)
    override fun init() {
        super.init()

        lifecycleScope.launch {
            snapshotFlow { searchInput.value }
                .debounce(300)
                .collectLatest {
                    if (it.isEmpty()) {
                        searchResult.value = emptyList()
                    } else {
                        val result = stockPool.search(it)
                        searchResult.value = result
                    }
                }
        }
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = "예약 매매 추가",
                    onBack = ::dismissAllowingStateLoss,
                )
            },
            bottomBar = {
                if (!searchMode.value) {
                    SellBuyButtons(::onBuy, ::onSell)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            if (searchMode.value) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    SearchView(searchInput, searchResult.value) {
                        code.value = it
                        if (priceInput.value.text.isEmpty()) {
                            stockPool.get(it)?.prevPrice()?.let { price ->
                                priceInput.value = TextFieldValue(
                                    AnnotatedString(price.safeLong().toString())
                                )
                            }
                        }
                        searchMode.value = false
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TouchIcon24(icon = Icons.Outlined.Search, onClick = ::onSearch)
                        if (code.value.isNotEmpty()) {
                            Margin(8)
                            val nameKr = stockPool.get(code.value)?.nameKr ?: code.value
                            TrueText(
                                s = nameKr,
                                fontSize = 14,
                            )
                        }
                    }
                    Margin(12)
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.width(240.dp)
                    ) {
                        InputSet("가격", priceInput, ::increasePrice, ::decreasePrice)
                        Margin(24)
                        InputSet("수량", quantityInput, ::increaseQuantity, ::decreaseQuantity)
                    }
                }
            }
        }
    }

    private fun onSearch() {
        searchMode.value = true
    }

    private fun increasePrice() {
        priceInput.value = priceInput.value.copy(
            text = com.trueedu.project.utils.increasePrice(priceInput.value.text)
        )
    }

    private fun decreasePrice() {
        priceInput.value = priceInput.value.copy(
            text = com.trueedu.project.utils.decreasePrice(priceInput.value.text)
        )
    }

    private fun increaseQuantity() {
        quantityInput.value = quantityInput.value.copy(
            text = com.trueedu.project.utils.increaseQuantity(quantityInput.value.text)
        )
    }

    private fun decreaseQuantity() {
        quantityInput.value = quantityInput.value.copy(
            text = com.trueedu.project.utils.decreaseQuantity(quantityInput.value.text)
        )
    }

    private fun onBuy() {
        trueAnalytics.clickButton("${screenName()}__buy__click")
        buySell(true)
    }

    private fun onSell() {
        trueAnalytics.clickButton("${screenName()}__sell__click")
        buySell(false)
    }
    private fun buySell(isBuy: Boolean) {
        if (code.value.isEmpty() || priceInput.value.text.isBlank() || quantityInput.value.text.isBlank()) {
            return
        }

        val a = OrderSchedule(
            code = code.value,
            isBuy = isBuy,
            price = priceInput.value.text.toInt(),
            quantity = quantityInput.value.text.toInt(),
        )

        onCompleted(a)
        dismissAllowingStateLoss()
    }
}

@Composable
private fun SearchView(
    searchInput: MutableState<String>,
    searchResult: List<StockInfo>,
    onItemClick: (String) -> Unit,
) {
    SearchBar(
        searchText = searchInput,
        modifier = Modifier.padding(12.dp)
    ) {}
    val state = rememberLazyListState()
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(searchResult, key = { _, item -> item.code }) { _, item ->
            SearchItem(item) {
                onItemClick(item.code)
            }
        }
    }
}

@Composable
private fun SearchItem(item: StockInfo, onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .height(42.dp)
            .padding(horizontal = 16.dp)
    ) {
        val s = "${item.nameKr} (${item.code})"
        TrueText(
            s = s,
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}
