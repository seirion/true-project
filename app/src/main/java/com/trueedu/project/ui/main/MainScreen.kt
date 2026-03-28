package com.trueedu.project.ui.main

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trueedu.project.BuildConfig
import com.trueedu.project.MainActivity
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.log.logD
import com.trueedu.project.data.realtime.WsMessageHandler
import com.trueedu.project.ui.dev.OnOffState
import com.trueedu.project.ui.views.UserInfoViewModel
import com.trueedu.project.ui.views.home.BottomNavItem
import com.trueedu.project.ui.views.home.HomeBottomNavigation
import com.trueedu.project.ui.views.home.HomeDrawer
import com.trueedu.project.ui.navigation.bottomNavItemOrNull
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    activity: MainActivity,
    googleAccount: GoogleAccount,
    trueAnalytics: TrueAnalytics,
    fragmentManager: FragmentManager,
    wsMessageHandler: WsMessageHandler,
    getLastBackgroundTime: () -> Long,
    setLastBackgroundTime: (Long) -> Unit,
    setOpenDrawer: ((() -> Unit)?) -> Unit,
    onSessionExpired: () -> Unit,
    mainNavigation: @Composable (navController: NavHostController, innerPadding: PaddingValues) -> Unit,
    homeDrawerVm: UserInfoViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var currentTab by remember { mutableStateOf<BottomNavItem?>(BottomNavItem.Home) }
    currentTab = navBackStackEntry.bottomNavItemOrNull() ?: currentTab

    val lifecycleOwner = LocalLifecycleOwner.current

    // 30분 세션 만료 체크를 navBackStackEntry lifecycle 변화로 처리
    DisposableEffect(navBackStackEntry) {
        val observer = LifecycleEventObserver { owner, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    val currentTime = SystemClock.elapsedRealtime()
                    val elapsedTime = currentTime - getLastBackgroundTime()
                    logD("elapsedTime: $elapsedTime")
                    if (elapsedTime >= 30 * 60 * 1000) {
                        onSessionExpired()
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    setLastBackgroundTime(SystemClock.elapsedRealtime())
                }
                else -> Unit
            }
        }
        navBackStackEntry?.lifecycle?.addObserver(observer)
        onDispose {
            navBackStackEntry?.lifecycle?.removeObserver(observer)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawerCallback: () -> Unit = remember(drawerState) {
        { scope.launch { drawerState.open() } }
    }
    DisposableEffect(openDrawerCallback) {
        setOpenDrawer(openDrawerCallback)
        onDispose { setOpenDrawer(null) }
    }

    val login by googleAccount.loginSignal.collectAsStateWithLifecycle(initialValue = false)
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = login && currentTab == BottomNavItem.Home,
        drawerContent = {
            HomeDrawer(activity, homeDrawerVm, googleAccount, trueAnalytics, fragmentManager) {
                scope.launch { drawerState.close() }
            }
        },
        content = {
            Scaffold(
                bottomBar = {
                    HomeBottomNavigation(
                        navController = navController,
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                    )
                },
            ) { innerPadding ->
                // 탭 영역 제외하고 화면이 그려지도록
                val padding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
                mainNavigation(navController, padding)
            }
        }
    )

    // 소켓 연결 상태 표시
    if (BuildConfig.DEBUG) {
        OnOffState(wsMessageHandler.on.value)
    }
}
