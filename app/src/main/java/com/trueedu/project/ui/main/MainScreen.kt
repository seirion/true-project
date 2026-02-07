package com.trueedu.project.ui.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.trueedu.project.BuildConfig
import com.trueedu.project.MainActivity
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.log.logD
import com.trueedu.project.data.realtime.WsMessageHandler
import com.trueedu.project.ui.dev.OnOffState
import com.trueedu.project.ui.views.UserInfoViewModel
import com.trueedu.project.ui.views.home.BottomNavItem
import com.trueedu.project.ui.views.home.BottomNavScreen
import com.trueedu.project.ui.views.home.HomeBottomNavigation
import com.trueedu.project.ui.views.home.HomeDrawer
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    activity: MainActivity,
    googleAccount: GoogleAccount,
    homeDrawerVm: UserInfoViewModel,
    trueAnalytics: TrueAnalytics,
    fragmentManager: FragmentManager,
    wsMessageHandler: WsMessageHandler,
    screenOf: (String?) -> BottomNavScreen?,
    getLastBackgroundTime: () -> Long,
    setLastBackgroundTime: (Long) -> Unit,
    setOpenDrawer: ((() -> Unit)?) -> Unit,
    onSessionExpired: () -> Unit,
    mainNavigation: @Composable (navController: NavHostController, innerPadding: PaddingValues) -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val lifecycleObserver = remember {
        LifecycleEventObserver { owner, event ->
            if (owner !is NavBackStackEntry) return@LifecycleEventObserver
            val screen = screenOf(owner.destination.route) ?: return@LifecycleEventObserver

            when (event) {
                Lifecycle.Event.ON_START -> {
                    val currentTime = System.currentTimeMillis()
                    val elapsedTime = currentTime - getLastBackgroundTime()

                    logD("elapsedTime: $elapsedTime")
                    if (elapsedTime >= 30 * 60 * 1000) { // 30 minutes
                        onSessionExpired()
                    } else {
                        screen.onStart()
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    screen.onStop()
                    setLastBackgroundTime(System.currentTimeMillis())
                }
                else -> Unit
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
    val openDrawerCallback = remember(drawerState, scope) {
        { scope.launch { drawerState.open() }; Unit }
    }
    LaunchedEffect(openDrawerCallback) {
        setOpenDrawer(openDrawerCallback)
    }

    val login = googleAccount.loginSignal.collectAsState(false)
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = login.value && navBackStackEntry?.destination?.route == BottomNavItem.Home.screenRoute,
        drawerContent = {
            HomeDrawer(activity, homeDrawerVm, googleAccount, trueAnalytics, fragmentManager) {
                scope.launch { drawerState.close() }
            }
        },
        content = {
            Scaffold(
                bottomBar = { HomeBottomNavigation(navController = navController) },
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

