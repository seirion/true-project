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
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.broadcast.DownloadCompleteReceiver
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.data.StockPool
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AuthRemote
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.home.HomeDrawer
import com.trueedu.project.ui.ranking.VolumeRankingFragment
import com.trueedu.project.ui.views.UserInfoFragment
import com.trueedu.project.ui.views.WatchListFragment
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
    lateinit var stockPool: StockPool
    @Inject
    lateinit var authRemote: AuthRemote
    @Inject
    lateinit var googleAccount: GoogleAccount
    @Inject
    lateinit var trueAnalytics: TrueAnalytics
    @Inject
    lateinit var downloadCompleteReceiver: DownloadCompleteReceiver
    @Inject
    lateinit var remoteConfig: RemoteConfig
    @Inject
    lateinit var admobManager: AdmobManager

    private val vm by viewModels<MainViewModel>()

    private lateinit var homeDrawer: HomeDrawer

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
        admobManager.init()

        enableEdgeToEdge()

        homeDrawer = HomeDrawer(
            vm = vm,
            screen = screen,
            stockPool = stockPool,
            admobManager = admobManager,
            remoteConfig = remoteConfig,
            trueAnalytics = trueAnalytics,
            fragmentManager = supportFragmentManager,
            gotoPlayStore = ::gotoPlayStore,
            onUserInfo = ::onUserInfo,
            onWatchList = ::onWatchList,
        )
        setContent {
            homeDrawer.Draw()
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
            UserInfoFragment.show(supportFragmentManager)
        }
    }

    private fun onWatchList() {
        trueAnalytics.clickButton("home__watch_list__click")
        doAfterLogin {
            WatchListFragment.show(supportFragmentManager)
        }
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

    private fun doAfterLogin(action: () -> Unit) {
        if (googleAccount.loggedIn()) {
            action()
        } else {
            googleAccount.login(this, action)
        }
    }
}
