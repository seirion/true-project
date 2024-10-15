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
    private val googleAccount: GoogleAccount,
    private val firebaseRealtimeDatabase: FirebaseRealtimeDatabase,
) {
    companion object {
        // 관심 그룹 개수
        const val MAX_GROUP_SIZE = 10
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
                            fillDefaultList(temp)
                        }
                    } else { // logout
                        withContext(Dispatchers.Main) {
                            fillDefaultList(null)
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
        firebaseRealtimeDatabase.writeWatchList(googleAccount.getId()!!, temp)
    }

    fun contains(index: Int, code: String): Boolean {
        return list.value[index].contains(code)
    }

    // 관심 종목이 하나라도 있는 지 여부
    fun hasWatchingStock(): Boolean {
        return list.value.any { it.isNotEmpty() }
    }

    /**
     * 크기가 MAX_GROUP_SIZE 인 리스트를 만들어서 list 에 넣는다.
     */
    private fun fillDefaultList(loadedData: List<List<String>>?) {
        list.value = MutableList(MAX_GROUP_SIZE) {
            loadedData?.getOrNull(it) ?: listOf()
        }
    }
}
