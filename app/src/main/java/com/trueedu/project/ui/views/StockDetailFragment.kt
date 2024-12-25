package com.trueedu.project.ui.views

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.trueedu.project.MainViewModel
import com.trueedu.project.R
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.extensions.priceChangeStr
import com.trueedu.project.model.dto.firebase.SpacStatus
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.ads.NativeAdView
import com.trueedu.project.ui.assets.EditAssetFragment
import com.trueedu.project.ui.common.BackStockTopBar
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.ui.views.order.OrderFragment
import com.trueedu.project.ui.views.setting.AppKeyInputFragment
import com.trueedu.project.ui.views.spac.SpacDataView
import com.trueedu.project.ui.views.spac.SpacValueSection
import com.trueedu.project.ui.views.spac.SpacValueView
import com.trueedu.project.ui.views.stock.DailyPriceFragment
import com.trueedu.project.ui.widget.SettingItem
import com.trueedu.project.utils.formatter.intFormatter
import com.trueedu.project.utils.formatter.rateFormatter
import com.trueedu.project.utils.formatter.safeDouble
import com.trueedu.project.utils.redemptionProfitRate
import com.trueedu.project.utils.stringToLocalDate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StockDetailFragment: BaseFragment() {
    companion object {
        fun show(
            stockInfo: StockInfo,
            fragmentManager: FragmentManager
        ): StockDetailFragment {
            val fragment = StockDetailFragment()
            fragment.stockInfo = stockInfo
            fragment.show(fragmentManager, "stock-detail")
            return fragment
        }
    }

    lateinit var stockInfo: StockInfo

    private val vmMain by activityViewModels<MainViewModel>()
    private val vm by viewModels<StockDetailViewModel>()

    @Inject
    lateinit var remoteConfig: RemoteConfig
    @Inject
    lateinit var admobManager: AdmobManager
    @Inject
    lateinit var googleAccount: GoogleAccount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenSheetKeyboardDialogTheme)
    }

    override fun onStart() {
        super.onStart()
        if (!::stockInfo.isInitialized) {
            dismissAllowingStateLoss()
        }
    }

    override fun init() {
        vm.init(stockInfo)
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.destroy()
    }

    @Composable
    override fun BodyScreen() {
        if (!::stockInfo.isInitialized) dismissAllowingStateLoss()
        Scaffold(
            topBar = {
                val realTimeTrade = vm.priceManager.dataMap[stockInfo.code]

                val price = realTimeTrade?.price
                    ?: vm.basePrice.value?.output?.price?.toDouble()
                    ?: 0.0
                val (priceChangeStr, textColor) = when {
                    realTimeTrade != null -> priceChangeStr(realTimeTrade)
                    vm.basePrice.value != null -> priceChangeStr(vm.basePrice.value!!)
                    else -> "" to ChartColor.up
                }
                val actions: @Composable RowScope.() -> Unit =
                    if (stockInfo.spac()) {
                        @Composable
                        {
                            TouchIcon24(Icons.Outlined.Edit, onClick = ::editAssets)
                        }
                    } else {
                        {}
                    }
                BackStockTopBar(
                    stockInfo.nameKr,
                    intFormatter.format(price, false),
                    priceChangeStr,
                    textColor,
                    stockInfo.halt(),
                    stockInfo.designated(),
                    ::dismissAllowingStateLoss,
                    actions = actions
                )
            },
            bottomBar = {
                if (remoteConfig.adVisible.value && admobManager.nativeAd.value != null) {
                    NativeAdView(admobManager.nativeAd.value!!)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                if (vm.hasAppKey()) {
                    SettingItem("주문 하기", true, ::gotoOrder)
                    SettingItem("일별 가격", true, ::gotoDailyPrice)
                }

                vm.spacStatus.value?.let {
                    SpacDetailView(vm.currentPrice(), stockInfo, it)
                }

                vm.infoList.value.forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        TrueText(s = it.first, fontSize = 16, color = MaterialTheme.colorScheme.primary)
                        TrueText(
                            s = it.second ?: "",
                            fontSize = 16,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 2,
                            textAlign = TextAlign.End,
                        )
                    }
                }

                val holding = vmMain.getUserStock(stockInfo.code)
                val holdingQuantity = holding?.holdingQuantity.safeDouble()
                if (holdingQuantity > 0.0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        TrueText(s = "계좌 보유", fontSize = 16, fontWeight = FontWeight.W700, color = MaterialTheme.colorScheme.primary)
                        val cost = holding!!.purchaseAveragePrice.safeDouble()
                        val costString = intFormatter.format(cost)
                        val quantityString = intFormatter.format(holdingQuantity)
                        TrueText(
                            s = "$costString • ${quantityString}주",
                            fontSize = 16,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 2,
                            textAlign = TextAlign.End,
                        )
                    }
                }

            }
        }
    }

    private fun gotoOrder() {
        trueAnalytics.clickButton("stock_detail__order__click")
        if (vm.hasAppKey()) {
            OrderFragment.show(stockInfo.code, childFragmentManager)
        } else {
            AppKeyInputFragment.show(false, childFragmentManager)
        }
    }

    private fun gotoDailyPrice() {
        trueAnalytics.clickButton("stock_detail__daily_price__click")
        if (vm.hasAppKey()) {
            DailyPriceFragment.show(stockInfo.code, childFragmentManager)
        } else {
            AppKeyInputFragment.show(false, childFragmentManager)
        }
    }

    private fun editAssets() {
        trueAnalytics.clickButton("stock_detail__edit_assets__click")
        if (googleAccount.loggedIn()) {
            EditAssetFragment.show(stockInfo.code, childFragmentManager)
        } else {
            googleAccount.login(requireActivity()) {
                // 로그인 성공 후 다시 시도
                EditAssetFragment.show(stockInfo.code, childFragmentManager)
            }
        }
    }
}

@Composable
fun ColumnScope.SpacDetailView(
    currentPrice: Double,
    stock: StockInfo,
    spac: SpacStatus
) {
    val redemptionPrice = spac.redemptionPrice?.toString() ?: "0"
    val inputString = remember { mutableStateOf(TextFieldValue(redemptionPrice)) }

    SpacValueSection()
    SpacValueView(
        basePrice = currentPrice,
        input = inputString,
    )

    val listingDateStr = stock.listingDate() ?: return
    val targetDate = stringToLocalDate(listingDateStr)
        .plusYears(3)

    val targetPrice = inputString.value.text.let {
        if (it.isEmpty()) 0
        else it.toInt()
    }
    val (profitRate, annualizedProfit) = redemptionProfitRate(
        currentPrice, targetPrice, targetDate
    )
    if (profitRate == null || annualizedProfit == null) {

    } else {
        SpacDataView(
            "$targetDate 청산 시",
            rateFormatter.format(profitRate, true),
            ChartColor.color(profitRate)
        )
        SpacDataView(
            "1년 환산 수익률",
            rateFormatter.format(annualizedProfit, true),
            ChartColor.color(annualizedProfit)
        )
    }
    DividerHorizontal()
}
