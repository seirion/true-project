package com.trueedu.project

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.MobileAds
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.realtime.RealOrderManager
import com.trueedu.project.data.realtime.RealPriceManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.UserAssets
import com.trueedu.project.data.realtime.WsMessageHandler
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.ads.AdmobManager
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.internal.Contexts
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class App : Application(), LifecycleEventObserver {
    companion object {
        private var foreground = false
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface InjectModule {
        fun getLocal(): Local
        fun getUserAssets(): UserAssets
        fun getWsMessage(): WsMessageHandler
        fun getRealPriceManager(): RealPriceManager
        fun getRealOrderManager(): RealOrderManager
        fun getStockPool(): StockPool
        fun getTrueAnalytics(): TrueAnalytics
        fun getAdmobManager(): AdmobManager
    }

    override fun onCreate() {
        super.onCreate()
        // init here
        val local = entryPointInjector(InjectModule::class.java).getLocal()
        local.migrate()
        MobileAds.initialize(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val userInfo = entryPointInjector(InjectModule::class.java).getUserAssets()
        val wsMessage = entryPointInjector(InjectModule::class.java).getWsMessage()
        val realPriceManager = entryPointInjector(InjectModule::class.java).getRealPriceManager()
        val realOrderManager = entryPointInjector(InjectModule::class.java).getRealOrderManager()
        val stockPool = entryPointInjector(InjectModule::class.java).getStockPool()
        val trueAnalytics = entryPointInjector(InjectModule::class.java).getTrueAnalytics()
        val admobManager = entryPointInjector(InjectModule::class.java).getAdmobManager()

        when (event) {
            Lifecycle.Event.ON_CREATE -> {
            }

            Lifecycle.Event.ON_START -> {
                foreground = true
                userInfo.start()
                wsMessage.start()
                realPriceManager.start()
                realOrderManager.start()
                stockPool.loadStockInfo()
                admobManager.start()
            }

            Lifecycle.Event.ON_STOP -> {
                foreground = false
                userInfo.stop()
                wsMessage.stop()
                realPriceManager.stop()
                realOrderManager.stop()
                admobManager.stop()
            }

            Lifecycle.Event.ON_DESTROY -> {
                trueAnalytics.shutdown()
            }

            else -> {}
        }
    }
}

fun <T> Context.entryPointInjector(clazz: Class<T>): T {
    return EntryPoints.get(Contexts.getApplication(applicationContext), clazz)
}

