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
    val adVisible = mutableStateOf(false)

    init {
        MainScope().launch {
            val m = firebaseRealtimeDatabase.loadUserConfig()
            adVisible.value = m.getOrDefault("adVisible", "true").toBoolean()
        }
    }
}