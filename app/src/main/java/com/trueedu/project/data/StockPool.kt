package com.trueedu.project.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.repository.FirebaseRealtimeDatabase
import com.trueedu.project.utils.StockInfoDownloader
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
    private val stockInfoDownloader: StockInfoDownloader,
) {
    companion object {
        private val TAG = StockPool::class.java.simpleName
    }

    private var lastUpdatedAt = 0
    private var stocks: Map<String, StockInfo> = emptyMap()

    enum class Status {
        LOADING,
        SUCCESS,
        FAIL,
        UPDATING,
    }
    val status = mutableStateOf(Status.LOADING)

    init {
        loadStockInfo()
    }

    fun loadStockInfo() {
        if (status.value == Status.SUCCESS || status.value == Status.UPDATING) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val (lastUpdatedAt, stocks) = firebaseRealtimeDatabase.loadStocks()

            withContext(Dispatchers.Main) {
                if (lastUpdatedAt == 0) {
                    status.value = Status.FAIL
                } else {
                    status.value = Status.SUCCESS
                    this@StockPool.lastUpdatedAt = lastUpdatedAt
                    this@StockPool.stocks = stocks

                    Log.d(TAG, "stocks(${stocks.size}) loaded")
                }
            }
        }
    }

    // 종목 파일을 다운로드/파싱하여 firebase 에 저장하기
    fun updateStocks() {
        if (!needUpdate()) return

        status.value = Status.UPDATING
        CoroutineScope(Dispatchers.IO).launch {
            stocks = stockInfoDownloader.getStockInfoList()
                .associateBy(StockInfo::code)
            firebaseRealtimeDatabase.writeStockInfo(today(), stocks)

            withContext(Dispatchers.Main) {
                if (stocks.isNotEmpty()) {
                    lastUpdatedAt = today()
                    status.value = Status.SUCCESS
                } else {
                    status.value = Status.FAIL
                }
            }
        }
    }

    // 업데이트가 필요한 지 여부
    fun needUpdate(): Boolean {
        return lastUpdatedAt < today()
    }

    fun search(keyword: String): List<StockInfo> {
        val key = keyword.lowercase()
        return stocks.values.filter {
            // 일단 간단한 패턴 매칭
            it.code.lowercase().contains(key) || it.nameKr.lowercase().contains(keyword)
        }
    }

    private fun today(): Int {
        val currentDate = Date()
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return formatter.format(currentDate).toInt()
    }

    fun get(code: String): StockInfo? {
        return stocks[code]
    }
}
