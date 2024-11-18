package com.trueedu.project.data.firebase

import android.util.Log
import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.model.dto.firebase.SpacStatus
import com.trueedu.project.utils.yyyyMMdd
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseRealtimeDatabase 에서 spac/status 데이터 처리
 */
@Singleton
class SpacStatusManager @Inject constructor(
    googleAccount: GoogleAccount,
): FirebaseDatabaseBase(googleAccount) {
    companion object {
        private val TAG = SpacStatusManager::class.java.simpleName
        private const val SNAPSHOT_KEY = "spac"
    }

    /**
     * @return yyyyMMdd 포맷의 스트링
     */
    suspend fun serverLastUpdated(): String? {
        Log.d(TAG, "serverLastUpdated()")
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "load() failed: currentUser null")
            return null
        }
        val ref = database.getReference(SNAPSHOT_KEY) // spac 데이터
        val snapshot = ref.child("last_updated")
        val lastUpdated = snapshot.get().await()
            .getValue(String::class.java)
        return lastUpdated
    }

    suspend fun load(): List<SpacStatus> {
        Log.d(TAG, "load()")
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "load() failed: currentUser null")
        }
        val ref = database.getReference(SNAPSHOT_KEY) // spac 데이터
        val snapshot = ref.child("status")
        val list = snapshot.get().await()
            .getValue(object : GenericTypeIndicator<List<SpacStatus>>() {})
        return list ?: emptyList()
    }

    /**
     * admin only
     */
    suspend fun write(list: List<SpacStatus>, onSuccess: () -> Unit, onFail: () -> Unit) {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "write() failed: currentUser null")
        }
        val ref = database.getReference(SNAPSHOT_KEY) // 종목 데이터
        ref.child("last_updated").setValue(LocalDate.now().yyyyMMdd())

        val snapshot = ref.child("status")
        snapshot.setValue(list)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFail()
            }
    }
}