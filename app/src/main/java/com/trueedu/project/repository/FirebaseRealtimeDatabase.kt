package com.trueedu.project.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.trueedu.project.BuildConfig
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.repository.local.Local
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRealtimeDatabase @Inject constructor(
    private val local: Local,
) {
    companion object {
        private val TAG = FirebaseRealtimeDatabase::class.java.simpleName
    }

    // Firebase Realtime Database 인스턴스 가져오기
    private val database = FirebaseDatabase.getInstance()
    private val configRef = database.getReference("app_config") // 앱 속성 관련
    private val stocksRef = database.getReference("stocks") // 종목 데이터
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        stocksRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    suspend fun needForceUpdate(): Boolean {
        try {
            if (userId == null) {
            }
            val currentVersion = BuildConfig.VERSION_NAME
            val snapshot = configRef.get().await()
            val minVersion = snapshot.child("minVersion").getValue(String::class.java)

            return minVersion != null && compareVersions(currentVersion, minVersion) < 0
        } catch (e: Exception) {
            // 오류 처리
            Log.e(TAG, "Error checking for update", e)
            return false
        }
    }

    // 버전 비교 함수 (숫자로 변환하여 비교)
    private fun compareVersions(version1: String, version2: String): Int {
        val v1Parts = version1.split(".")
        val v2Parts = version2.split(".")

        val maxLength = maxOf(v1Parts.size, v2Parts.size)
        repeat(maxLength) {
            val v1Part = v1Parts.getOrNull(it)?.toIntOrNull() ?: 0
            val v2Part = v2Parts.getOrNull(it)?.toIntOrNull() ?: 0

            if (v1Part != v2Part) {
                return v1Part - v2Part
            }
        }

        return 0
    }

    suspend fun loadStocks(): Pair<Int, Map<String, StockInfo>> {
        try {
            val snapshot = stocksRef.get().await()
            val lastUpdatedAt = snapshot.child("lastUpdatedAt").getValue(Int::class.java)
            val stocks = snapshot.child("list").getValue(object : GenericTypeIndicator<Map<String, StockInfo>>() {})
                ?: emptyMap()

            if (lastUpdatedAt == null) {
                Log.d(TAG, "cannot read values: \"lastUpdatedAt\"")
                return 0 to emptyMap()
            }
            Log.d(TAG, "lastUpdatedAt: $lastUpdatedAt")

            return lastUpdatedAt to stocks
        } catch (e: Exception) {
            // 오류 처리
            Log.e(TAG, "Failed to get stocks", e)
            return 0 to emptyMap()
        }
    }

    fun uploadStockInfo(lastUpdatedAt: Int, stocks: Map<String, StockInfo>) {
        try {
            stocksRef.child("list").setValue(stocks)
                .addOnSuccessListener {

                }
                .addOnFailureListener {

                }
            stocksRef.child("lastUpdatedAt").setValue(lastUpdatedAt)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update stocks", e)
        }
    }
}
