package com.trueedu.project.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.data.firebase.FirebaseRealtimeDatabase
import com.trueedu.project.data.model.UserRemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfig @Inject constructor(
    private val firebaseRealtimeDatabase: FirebaseRealtimeDatabase
) {
    companion object {
        private const val TAG = "RemoteConfig"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val configCache = mutableMapOf<String, String>()

    val adVisible = mutableStateOf(false)

    init {
        scope.launch {
            try {
                val config = firebaseRealtimeDatabase.loadUserConfig()
                adVisible.value = config.adVisible
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load config", e)
                // 기본값 유지
            }
        }
    }

    fun setAdVisible(visible: Boolean) {
        if (adVisible.value != visible) {
            val previousValue = adVisible.value
            adVisible.value = visible

            scope.launch {
                try {
                    val config = UserRemoteConfig(adVisible = visible)
                    firebaseRealtimeDatabase.writeUserConfig(config)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save config", e)
                    // 실패 시 상태 복원
                    adVisible.value = previousValue
                }
            }
        }
    }

    fun onCleared() {
        scope.cancel()
    }
}
