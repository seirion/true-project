package com.trueedu.project.data.firebase

import android.util.Log
import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.model.dto.firebase.SpacSchedule
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

    // caching
    private var spacList: List<SpacStatus> = emptyList()

    private var spacScheduleList: Map<String, SpacSchedule> = emptyMap()

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
        if (spacList.isNotEmpty()) {
            return spacList
        }

        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "load() failed: currentUser null")
            return emptyList()
        }
        val ref = database.getReference(SNAPSHOT_KEY) // spac 데이터
        val snapshot = ref.child("status")
        val list = snapshot.get().await()
            .getValue(object : GenericTypeIndicator<List<SpacStatus>>() {})
        if (list != null) spacList = list
        return list ?: emptyList()
    }

    /**
     * admin only
     */
    suspend fun write(list: List<SpacStatus>, onSuccess: () -> Unit, onFail: () -> Unit) {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "write() failed: currentUser null")
            onFail()
            return
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

    suspend fun loadSpacSchedule(): Map<String, SpacSchedule> {
        Log.d(TAG, "loadSpacSchedule()")
        if (spacScheduleList.isNotEmpty()) {
            return spacScheduleList
        }

        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "load() failed: currentUser null")
            return emptyMap()
        }
        val ref = database.getReference(SNAPSHOT_KEY) // spac 데이터
        val snapshot = ref.child("schedule")
        val list = snapshot.get().await()
            .getValue(object : GenericTypeIndicator<Map<String, SpacSchedule>>() {})
        if (list != null) spacScheduleList = list
        return list ?: emptyMap()
    }
}
