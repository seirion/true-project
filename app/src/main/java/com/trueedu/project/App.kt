package com.trueedu.project

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.repository.local.Local
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
        fun getTrueAnalytics(): TrueAnalytics
    }

    override fun onCreate() {
        super.onCreate()
        // init here
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val trueAnalytics = entryPointInjector(InjectModule::class.java).getTrueAnalytics()

        when (event) {
            Lifecycle.Event.ON_CREATE -> {
            }

            Lifecycle.Event.ON_START -> {
                foreground = true
            }

            Lifecycle.Event.ON_STOP -> {
                foreground = false
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

