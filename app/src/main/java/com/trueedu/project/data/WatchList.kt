package com.trueedu.project.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.repository.FirebaseRealtimeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchList @Inject constructor(
    googleAccount: GoogleAccount,
    private val firebaseRealtimeDatabase: FirebaseRealtimeDatabase,
) {
    companion object {
        private val TAG = WatchList::class.java.simpleName
    }

    val list = mutableStateOf<List<List<String>>>(emptyList())

    init {
        CoroutineScope(Dispatchers.IO).launch {
            googleAccount.loginSignal
                .collect { login ->
                    if (login) {
                        val id = googleAccount.getId()
                        if (id.isNullOrBlank()) {
                            Log.d(TAG, "id is not available: ($id)")
                        }
                        val temp = firebaseRealtimeDatabase.loadWatchList(id!!)
                        Log.d(TAG, "loadWatchList: ${temp.size}")
                        withContext(Dispatchers.Main) {
                            if (temp.isEmpty()) {
                                fillDefaultList()
                            } else {
                                list.value = temp
                            }
                        }
                    } else { // logout
                        withContext(Dispatchers.Main) {
                            fillDefaultList()
                        }
                    }
                }
        }
    }

    fun get(index: Int): List<String> {
        return list.value[index]
    }

    fun add(index: Int, code: String) {
        require(index in list.value.indices)

        val temp = list.value
            .mapIndexed { i, list ->
                if (i == index) {
                    list + code
                } else {
                    list
                }
            }
        list.value = temp
    }

    fun contains(index: Int, code: String): Boolean {
        return list.value[index].contains(code)
    }

    // 관심 종목이 하나라도 있는 지 여부
    fun hasWatchingStock(): Boolean {
        return list.value.any { it.isNotEmpty() }
    }

    private fun fillDefaultList() {
        val temp = MutableList(10) { listOf<String>() }
        list.value = temp
    }
}
