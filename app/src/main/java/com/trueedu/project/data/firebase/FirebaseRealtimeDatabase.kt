package com.trueedu.project.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.project.BuildConfig
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.model.dto.firebase.StockInfoKosdaq
import com.trueedu.project.model.dto.firebase.StockInfoKospi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRealtimeDatabase @Inject constructor(
    private val googleAccount: GoogleAccount,
) {
    companion object {
        private val TAG = FirebaseRealtimeDatabase::class.java.simpleName
    }

    // Firebase Realtime Database 인스턴스 가져오기
    private val database = FirebaseDatabase.getInstance()
    private val metaRef = database.getReference("meta") // 마지막 업데이트 시각
    private val configRef = database.getReference("app_config") // 앱 속성 관련
    private val stocksRef = database.getReference("stocks") // 종목 데이터
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

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

    suspend fun lastUpdatedTime(): Long {
        try {
            val snapshot = metaRef.get().await()
            val lastUpdatedAt = snapshot.child("stockLastUpdatedAt").getValue(Long::class.java)
            return lastUpdatedAt ?: 0L
        } catch (e: Exception) {
            return 0L
        }
    }

    suspend fun loadStocks(): Pair<Long, Map<String, StockInfo>> {
        Log.d(TAG, "loadStocks()")
        try {
            val snapshotMeta = metaRef.get().await()
            val snapshot = stocksRef.get().await()
            val lastUpdatedAt = snapshotMeta.child("stockLastUpdatedAt").getValue(Long::class.java)
            val kospi = snapshot.child("kospi").getValue(object : GenericTypeIndicator<Map<String, StockInfoKospi>>() {})
                ?: emptyMap()
            val kosdaq = snapshot.child("kosdaq").getValue(object : GenericTypeIndicator<Map<String, StockInfoKosdaq>>() {})
                ?: emptyMap()

            if (lastUpdatedAt == null) {
                Log.d(TAG, "cannot read values: \"lastUpdatedAt\"")
                return 0L to emptyMap()
            }
            Log.d(TAG, "loading stocks completed - lastUpdatedAt: $lastUpdatedAt")

            return lastUpdatedAt to (kospi + kosdaq)
        } catch (e: Exception) {
            // 오류 처리
            Log.e(TAG, "Failed to get stocks", e)
            return 0L to emptyMap()
        }
    }

    private suspend fun firebaseCurrentUser(): FirebaseUser? {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            return currentUser
        }

        if (!googleAccount.loggedIn()) {
            Log.d(TAG, "cannot write values: currentUser == null")
            return null
        }
        val idToken = googleAccount.getToken()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        return auth.currentUser
    }

    suspend fun loadDelistedStocks(): List<String> {
        Log.d(TAG, "loadDelistedStocks()")
        try {
            val snapshot = stocksRef.get().await()
            return snapshot.child("delisted").getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
        } catch (e: Exception) {
            // 오류 처리
            Log.e(TAG, "Failed to get delisted stocks", e)
            return emptyList()
        }
    }

    /**
     * @param lastUpdatedAt: 'yyyyMMddHHmm'
     */
    suspend fun writeStockInfo(lastUpdatedAt: Long, stocks: Map<String, StockInfo>) {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "cannot write values: \"currentUser\"")
            return
        }

        val kospi = stocks.filter { it.value.kospi() }
        val kosdaq = stocks.filter { it.value.kosdaq() }
        try {
            stocksRef.child("kospi").setValue(kospi)
            stocksRef.child("kosdaq").setValue(kosdaq)
            metaRef.child("stockLastUpdatedAt").setValue(lastUpdatedAt)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update stocks", e)
        }
    }

    fun deleteUser(
        onSuccess: () -> Unit,
        onFail: () -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentUser = firebaseCurrentUser()
            if (currentUser == null) {
                Log.d(TAG, "deleteUser() failed: currentUser null")
            }
            val userId = currentUser?.uid ?: return@launch

            val ref = database.getReference("users")
            ref.child(userId).removeValue()
                .addOnSuccessListener {
                    MainScope().launch {
                        onSuccess()
                    }
                }
                .addOnFailureListener {
                    MainScope().launch {
                        onFail()
                    }
                }
        }
    }

    suspend fun loadUserConfig(): Map<String, String> {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "loadUserConfig() failed: currentUser null")
            return emptyMap()
        }
        val userId = currentUser.uid

        val ref = database.getReference("users")
        val snapshot = ref.child(userId).child("config")
        val m = snapshot.get().await()
            .getValue(object : GenericTypeIndicator<Map<String, String>>() {})
        return m ?: emptyMap()
    }

    suspend fun writeUserConfig(m: Map<String, String>) {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "writeUserConfig() failed: currentUser null")
            return
        }
        val userId = currentUser.uid

        val ref = database.getReference("users")
        val snapshot = ref.child(userId).child("config")
        snapshot.setValue(m)
    }
}
