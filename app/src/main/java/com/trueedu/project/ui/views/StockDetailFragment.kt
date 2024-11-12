package com.trueedu.project.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.extensions.priceChangeStr
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.ads.NativeAdView
import com.trueedu.project.ui.assets.EditAssetFragment
import com.trueedu.project.ui.common.BackStockTopBar
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.ui.views.setting.AppKeyInputFragment
import com.trueedu.project.ui.views.stock.DailyPriceFragment
import com.trueedu.project.ui.widget.SettingItem
import com.trueedu.project.utils.formatter.cashFormatter
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

    private val vm by viewModels<StockDetailViewModel>()

    @Inject
    lateinit var remoteConfig: RemoteConfig
    @Inject
    lateinit var admobManager: AdmobManager
    @Inject
    lateinit var googleAccount: GoogleAccount

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
                    cashFormatter.format(price, false),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SettingItem("일별 가격", true, ::gotoDailyPrice)

                vm.infoList.value.forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        TrueText(s = it.first, fontSize = 16, color = MaterialTheme.colorScheme.primary)
                        TrueText(s = it.second ?: "", fontSize = 16, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
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
