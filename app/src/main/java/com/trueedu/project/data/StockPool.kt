package com.trueedu.project.data

import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.repository.FirebaseRealtimeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockPool @Inject constructor(
    private val firebaseRealtimeDatabase: FirebaseRealtimeDatabase,
) {
    private var lastUpdatedAt = 0
    private val stocks = mutableListOf<StockInfo>()

    enum class Status {
        LOADING,
        SUCCESS,
        FAIL,
    }
    val status = mutableStateOf(Status.LOADING)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val (lastUpdatedAt, stocks) = firebaseRealtimeDatabase.loadStocks()

            withContext(Dispatchers.Main) {
                if (lastUpdatedAt == 0) {
                    status.value = Status.FAIL
                } else {
                    status.value = Status.SUCCESS
                    this@StockPool.lastUpdatedAt = lastUpdatedAt
                }
            }
        }
    }

    fun updateStocks() {

    }

    // 업데이트가 필요한 지 여부
    fun needUpdate(): Boolean {
        return lastUpdatedAt < today()
    }

    private fun today(): Int {
        val currentDate = Date()
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return formatter.format(currentDate).toInt()
    }
}
