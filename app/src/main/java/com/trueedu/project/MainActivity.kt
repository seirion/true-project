package com.trueedu.project

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.broadcast.DownloadCompleteReceiver
import com.trueedu.project.data.DartManager
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.realtime.WsMessageHandler
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.model.dto.firebase.AppNotice
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AuthRemote
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.common.ButtonAction
import com.trueedu.project.ui.common.PopupFragment
import com.trueedu.project.ui.common.PopupType
import com.trueedu.project.ui.dev.OnOffState
import com.trueedu.project.ui.main.MainNavigation
import com.trueedu.project.ui.theme.TrueProjectTheme
import com.trueedu.project.ui.views.UserInfoViewModel
import com.trueedu.project.ui.views.home.AppNoticePopup
import com.trueedu.project.ui.views.home.BottomNavItem
import com.trueedu.project.ui.views.home.BottomNavScreen
import com.trueedu.project.ui.views.home.ForceUpdateView
import com.trueedu.project.ui.views.home.HomeBottomNavigation
import com.trueedu.project.ui.views.home.HomeDrawer
import com.trueedu.project.ui.views.home.HomeScreen
import com.trueedu.project.ui.views.menu.MenuScreen
import com.trueedu.project.ui.views.spac.SpacScreen
import com.trueedu.project.ui.views.spac.SpacViewModel
import com.trueedu.project.ui.views.watch.WatchListViewModel
import com.trueedu.project.ui.views.watch.WatchScreen
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
    lateinit var tokenKeyManager: TokenKeyManager
    @Inject
    lateinit var trueAnalytics: TrueAnalytics
    @Inject
    lateinit var downloadCompleteReceiver: DownloadCompleteReceiver
    @Inject
    lateinit var spacManager: SpacManager
    @Inject
    lateinit var dartManager: DartManager
    @Inject
    lateinit var remoteConfig: RemoteConfig
    @Inject
    lateinit var admobManager: AdmobManager

    @Inject
    lateinit var wsMessageHandler: WsMessageHandler

    private val vm by viewModels<MainViewModel>()
    private val watchVm by viewModels<WatchListViewModel>()
    private val spacVm by viewModels<SpacViewModel>()
    private val homeDrawerVm by viewModels<UserInfoViewModel>()

    private lateinit var homeScreen: HomeScreen
    private lateinit var watchScreen: WatchScreen
    private lateinit var spacScreen: SpacScreen
    private lateinit var menuScreen: MenuScreen

    private var openDrawer: (() -> Unit)? = null

    // 앱이 백그라운드로 진입할 때의 시각 저장
    private var lastBackgroundTime = System.currentTimeMillis()

    override fun onStart() {
        super.onStart()
        if (screen.keepScreenOn.value) {
            keepScreenOnOff(true)
        }

        if (local.disclaimerVisible) {
            PopupFragment.show(
                title = "투자 유의 사항",
                desc = """
                    본 앱에서 제공하는 정보는 투자 참고용으로만 사용되며, 투자 권유 또는 자문을 목적으로 하지 않습니다. 투자 결정은 사용자 본인의 판단에 따라 신중하게 이루어져야 하며, 투자 결과에 대한 책임은 사용자 본인에게 있습니다.

                    The information provided in this app is for informational purposes only and is not intended as investment advice or a recommendation to buy or sell any securities. Investment decisions should be made based on your own judgment and research. You are solely responsible for your investment results.
                """.trimIndent(),
                popupType = PopupType.OK,
                buttonActions = listOf(
                    ButtonAction("확인") { local.disclaimerVisible = false }
                ),
                cancellable = true,
                supportFragmentManager,
            )
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

        homeScreen = HomeScreen(
            activity = this,
            vm = vm,
            stockPool = stockPool,
            admobManager = admobManager,
            remoteConfig = remoteConfig,
            trueAnalytics = trueAnalytics,
            fragmentManager = supportFragmentManager,
            onUserInfo = ::onUserInfo,
        )
        watchScreen = WatchScreen(
            activity = this,
            vm = watchVm,
            admobManager = admobManager,
            remoteConfig = remoteConfig,
            trueAnalytics = trueAnalytics,
            fragmentManager = supportFragmentManager,
        )
        spacScreen = SpacScreen(
            mainVm = vm,
            vm = spacVm,
            spacManager = spacManager,
            trueAnalytics = trueAnalytics,
            remoteConfig = remoteConfig,
            admobManager = admobManager,
            fragmentManager = supportFragmentManager,
        )
        menuScreen = MenuScreen(
            screen = screen,
            trueAnalytics = trueAnalytics,
            tokenKeyManager = tokenKeyManager,
            dartManager = dartManager,
            fragmentManager = supportFragmentManager,
        )

        // Register the onBackPressed callback
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Exit the app when the back button is pressed.
                finishAffinity()
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setContent {
            DisposableEffect(screen.forceDark.value) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { screen.forceDark.value },
                )
                onDispose {}
            }

            TrueProjectTheme(
                n = screen.theme.intValue,
                forceDark = screen.forceDark.value
            ) {
                if (vm.forceUpdateVisible.value) {
                    ForceUpdateView(::gotoPlayStore)
                } else if (vm.appNotice.value.available() && local.appNoticeId < vm.appNotice.value.id) {
                    AppNoticePopup(vm.appNotice.value, supportFragmentManager) {
                        trueAnalytics.clickButton("main__notice_close__click")
                        if (vm.appNotice.value.cancellable) {
                            local.appNoticeId = vm.appNotice.value.id
                            vm.appNotice.value = AppNotice()
                        } else {
                            Runtime.getRuntime().exit(0)
                        }
                    }
                } else {
                    MainScreen()
                }
            }
        }
    }

    private fun screenOf(route: String?): BottomNavScreen? {
        return when (route) {
            BottomNavItem.Home.screenRoute -> homeScreen
            BottomNavItem.Watch.screenRoute -> watchScreen
            BottomNavItem.Spac.screenRoute -> spacScreen
            BottomNavItem.Menu.screenRoute -> menuScreen
            else -> null
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

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun MainScreen() {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()

        val lifecycleObserver = remember {
            LifecycleEventObserver { owner, event ->
                if (owner !is NavBackStackEntry) return@LifecycleEventObserver
                val screen = screenOf(owner.destination.route) ?: return@LifecycleEventObserver

                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                    }
                    Lifecycle.Event.ON_START -> {
                        val currentTime = System.currentTimeMillis()
                        val elapsedTime = currentTime - lastBackgroundTime

                        Log.d(TAG, "elapsedTime: $elapsedTime")
                        if (elapsedTime >= 30 * 60 * 1000) { // 30 minutes
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            Runtime.getRuntime().exit(0)
                        } else {
                            screen.onStart()
                        }
                    }
                    Lifecycle.Event.ON_RESUME -> {
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                    }
                    Lifecycle.Event.ON_STOP -> {
                        screen.onStop()
                        lastBackgroundTime = System.currentTimeMillis()
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                    }
                    else -> {}
                }
            }
        }

        // Lifecycle observer 등록 및 해제
        DisposableEffect(navBackStackEntry) {
            navBackStackEntry?.lifecycle?.addObserver(lifecycleObserver)
            onDispose {
                navBackStackEntry?.lifecycle?.removeObserver(lifecycleObserver)
            }
        }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        if (openDrawer == null) {
            openDrawer = {
                scope.launch { drawerState.open() }
            }
        }
        val login = googleAccount.loginSignal.collectAsState(false)
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = login.value && navBackStackEntry?.destination?.route == BottomNavItem.Home.screenRoute,
            drawerContent = {
                HomeDrawer(this, homeDrawerVm, googleAccount, trueAnalytics, supportFragmentManager) {
                    scope.launch { drawerState.close() }
                }
            },
            content = {
                Scaffold(
                    bottomBar = { HomeBottomNavigation(navController = navController) },
                ) { innerPadding ->
                    // 탭 영역 제외하고 화면이 그려지도록
                    val padding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
                    MainNavigation(
                        navController = navController,
                        innerPadding = padding,
                        homeScreen = homeScreen,
                        watchScreen = watchScreen,
                        spacScreen = spacScreen,
                        menuScreen = menuScreen,
                    )
                }
            }
        )
        // 소켓 연결 상태 표시
        if (BuildConfig.DEBUG) {
            OnOffState(wsMessageHandler.on.value)
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
            openDrawer?.invoke()
        }
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
