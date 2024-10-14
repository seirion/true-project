package com.trueedu.project

import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.broadcast.DownloadCompleteReceiver
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AuthRemote
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.ranking.VolumeRankingFragment
import com.trueedu.project.ui.theme.TrueProjectTheme
import com.trueedu.project.ui.topbar.MainTopBar
import com.trueedu.project.ui.views.SettingFragment
import com.trueedu.project.ui.views.StockSearchFragment
import com.trueedu.project.ui.views.home.AccountInfo
import com.trueedu.project.ui.views.home.EmptyHome
import com.trueedu.project.ui.views.home.ForceUpdateView
import com.trueedu.project.ui.views.home.StockItem
import com.trueedu.project.ui.views.setting.AppKeyInputFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    @Inject
    lateinit var local: Local
    @Inject
    lateinit var screen: ScreenControl
    @Inject
    lateinit var authRemote: AuthRemote
    @Inject
    lateinit var googleAccount: GoogleAccount
    @Inject
    lateinit var trueAnalytics: TrueAnalytics
    @Inject
    lateinit var downloadCompleteReceiver: DownloadCompleteReceiver

    private val vm by viewModels<MainViewModel>()

    override fun onStart() {
        super.onStart()
        if (screen.keepScreenOn.value) {
            keepScreenOnOff(true)
        }
    }

    private fun keepScreenOnOff(on: Boolean) {
        if (on) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onStop() {
        super.onStop()
        if (screen.keepScreenOn.value) {
            keepScreenOnOff(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleAccount.init(this)
        trueAnalytics.enterView("main__enter")

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadCompleteReceiver, filter, RECEIVER_EXPORTED)

        observingScreenSettings()
        vm.init()
        enableEdgeToEdge()
        setContent {
            TrueProjectTheme(
                n = screen.theme.intValue,
                forceDark = screen.forceDark.value
            ) {
                if (vm.forceUpdateVisible.value) {
                    ForceUpdateView(::gotoPlayStore)
                    return@TrueProjectTheme
                }
                Scaffold(
                    topBar = {
                        MainTopBar(
                            vm.googleSignInAccount.value,
                            vm.accountNum.value,
                            ::onUserInfo,
                            ::onSearch,
                            ::onSetting
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->

                    val state = rememberLazyListState()
                    LazyColumn(
                        state = state,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        vm.account.value?.output2?.firstOrNull()?.let {
                            item { AccountInfo(it, vm.marketPriceMode.value, vm::onChangeMarketPriceMode) }
                        } ?: item { EmptyHome() }

                        vm.account.value?.output1?.let {
                            itemsIndexed(it, { _, item -> item.code} ) { index, item ->
                                StockItem(item, vm.marketPriceMode.value)
                            }
                        }

                        /*
                        HomeItem(
                            name = "거래량 순위",
                            ::gotoVolumeRanking
                        )
                         */
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadCompleteReceiver)
    }

    private fun onUserInfo() {
        trueAnalytics.clickButton("home__user_info__click")
        if (vm.googleSignInAccount.value == null) {
            googleAccount.login(this)
        } else {
            AppKeyInputFragment.show(supportFragmentManager)
        }
    }

    private fun onSearch() {
        trueAnalytics.clickButton("home__stock_search__click")
        StockSearchFragment.show(supportFragmentManager)
    }

    private fun onSetting() {
        trueAnalytics.clickButton("home__setting__click")
        SettingFragment.show(supportFragmentManager)
    }

    private fun gotoPlayStore() {
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                data =
                    Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
            }
        )
    }

    // 테스트용
    private fun gotoVolumeRanking() {
        VolumeRankingFragment.show(supportFragmentManager)
    }

    private fun observingScreenSettings() {
        lifecycleScope.launch {
            snapshotFlow { screen.keepScreenOn.value }
                .collectLatest {
                    keepScreenOnOff(it)
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult(): $requestCode $resultCode")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GoogleAccount.RC_SIGN_IN) {
            googleAccount.handleActivityResult(requestCode, resultCode, data, this)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeItem(
    name: String = "종목 보기",
    onClick: () -> Unit = {}
) {
    BasicText(
        s = name,
        fontSize = 18,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    )
    HorizontalDivider()
}
