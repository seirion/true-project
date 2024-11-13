package com.trueedu.project.data

import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.repository.FirebaseRealtimeDatabase
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfig @Inject constructor(
    private val firebaseRealtimeDatabase: FirebaseRealtimeDatabase
) {
    companion object {
        private const val KEY_AD_VISIBLE = "adVisible"
    }
    val adVisible = mutableStateOf(false)

    init {
        MainScope().launch {
            val m = firebaseRealtimeDatabase.loadUserConfig()
            adVisible.value = m.getOrDefault(KEY_AD_VISIBLE, "true").toBoolean()
        }
    }

    fun setAdVisible(visible: Boolean) {
        if (adVisible.value != visible) {
            adVisible.value = visible
            MainScope().launch {
                val m = firebaseRealtimeDatabase.loadUserConfig()
                firebaseRealtimeDatabase.writeUserConfig(m + mapOf(KEY_AD_VISIBLE to visible.toString()))
            }
        }
    }
}
