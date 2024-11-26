package com.trueedu.project.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.data.firebase.FirebaseRealtimeDatabase
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
                        val temp = firebaseRealtimeDatabase.loadWatchList()
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
        return list.value.getOrElse(index) { emptyList() }
    }

    fun add(index: Int, code: String) {
        if (list.value.isEmpty()) return

        require(index in list.value.indices)

        if (list.value[index].contains(code)) {
            Log.d(TAG, "trying to insert already existing code: $code")
            return
        }

        val temp = list.value
            .mapIndexed { i, list ->
                if (i == index) {
                    list + code
                } else {
                    list
                }
            }
        list.value = temp
        firebaseRealtimeDatabase.writeWatchList(temp)
    }

    fun remove(index: Int, code: String) {
        require(index in list.value.indices)

        if (!list.value[index].contains(code)) {
            Log.d(TAG, "trying to remove not existing code: $code")
            return
        }

        val temp = list.value
            .mapIndexed { i, list ->
                if (i == index) {
                    list.filter { it != code }
                } else {
                    list
                }
            }
        list.value = temp
        firebaseRealtimeDatabase.writeWatchList(temp)
    }

    // 편집한 관심종목 목록을 갱신
    fun replace(index: Int, codes: List<String>) {
        val temp = list.value
            .mapIndexed { i, list ->
                if (i == index) {
                    codes
                } else {
                    list
                }
            }
        list.value = temp
        firebaseRealtimeDatabase.writeWatchList(temp)
    }

    fun contains(index: Int, code: String): Boolean {
        return list.value.getOrNull(index)?.contains(code) == true
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
