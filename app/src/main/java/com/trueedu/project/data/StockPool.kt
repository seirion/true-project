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

        /**
         * 마스터 파일이 업로드 되는 시각 (HHmm)
         */
        private val uploadTime = listOf(
            600,
            655,
            735,
            755,
            845,
            946,
            1055,
            1710,
            1730,
            1755,
            1810,
            1830,
            1855,
        )
    }

    private var lastUpdatedAt = 0L
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
                if (lastUpdatedAt == 0L) {
                    status.value = Status.FAIL
                } else {
                    status.value = Status.SUCCESS
                    this@StockPool.lastUpdatedAt = lastUpdatedAt
                    this@StockPool.stocks = stocks

                    Log.d(TAG, "stocks(${stocks.size}) loaded")
                }

                // 마스터 파일 다운로드가 필요한 지 확인 후 추가 작업
                downloadMasterFiles()
            }
        }
    }

    // 종목 파일을 다운로드/파싱하여 firebase 에 저장하기
    fun downloadMasterFiles() {
        if (!needToDownloadMasterFiles()) return

        status.value = Status.UPDATING
        CoroutineScope(Dispatchers.IO).launch {
            stocks = stockInfoDownloader.getStockInfoList()
                .associateBy(StockInfo::code)

            val yyyyMMddHHmm = currentTimeToyyyyMMddHHmm()
            firebaseRealtimeDatabase.writeStockInfo(yyyyMMddHHmm, stocks)

            withContext(Dispatchers.Main) {
                if (stocks.isNotEmpty()) {
                    lastUpdatedAt = yyyyMMddHHmm
                    status.value = Status.SUCCESS
                } else {
                    status.value = Status.FAIL
                }
            }
        }
    }

    // 마스터 파일 갱신이 필요한 지 여부
    fun needToDownloadMasterFiles(): Boolean {
        val yyyyMMdd = currentTimeToyyyyMMdd() * 10000
        val updateTimes = uploadTime.map {
            yyyyMMdd + it
        }
        val now = currentTimeToyyyyMMddHHmm()
        return updateTimes.any { it in (lastUpdatedAt + 1)..now }
    }

    fun search(keyword: String): List<StockInfo> {
        val key = keyword.lowercase()
        return stocks.values.filter {
            // 일단 간단한 패턴 매칭
            it.code.lowercase().contains(key) || it.nameKr.lowercase().contains(keyword)
        }
    }

    fun search(predicate: (StockInfo) -> Boolean): List<StockInfo> {
        return stocks.values.filter(predicate)
    }

    private fun currentTimeToyyyyMMdd(): Long {
        val currentDate = Date()
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return formatter.format(currentDate).toLong()
    }

    /**
     * 분 단위 resolution
     */
    private fun currentTimeToyyyyMMddHHmm(): Long {
        val currentDate = Date()
        val formatter = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
        return formatter.format(currentDate).toLong()
    }

    fun get(code: String): StockInfo? {
        return stocks[code]
    }
}
