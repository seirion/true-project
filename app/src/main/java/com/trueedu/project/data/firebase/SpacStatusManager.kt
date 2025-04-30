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
        private const val META_KEY = "meta"
        private const val SNAPSHOT_KEY = "spac"
    }

    // caching
    private var spacList: List<SpacStatus> = emptyList()

    private var spacScheduleList: Map<String, SpacSchedule> = emptyMap()

    /**
     * @return yyyyMMdd 포맷의 스트링
     */
    suspend fun serverLastUpdated(): Long? {
        Log.d(TAG, "serverLastUpdated()")
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "load() failed: currentUser null")
            return null
        }
        val ref = database.getReference(META_KEY) // spac 데이터
        val snapshot = ref.child("spacLastUpdatedAt")
        val lastUpdated = snapshot.get().await()
            .getValue(Long::class.java)
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
            ?.filter { it?.code != null }

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
        val meta = database.getReference(META_KEY) // 종목 데이터
        val ref = database.getReference(SNAPSHOT_KEY) // 종목 데이터
        meta.child("spacLastUpdatedAt").setValue(
            LocalDate.now().yyyyMMdd().toLong()
        )

        val snapshot = ref.child("status")
        snapshot.setValue(list)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFail()
            }
    }

    suspend fun loadSpacSchedule(force: Boolean = false): Map<String, SpacSchedule> {
        Log.d(TAG, "loadSpacSchedule()")
        if (spacScheduleList.isNotEmpty() && !force) {
            return spacScheduleList
        }

        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "load() failed: currentUser null")
            return spacScheduleList
        }
        val ref = database.getReference(SNAPSHOT_KEY) // spac 데이터
        val snapshot = ref.child("schedule")
        val m = snapshot.get().await()
            .getValue(object : GenericTypeIndicator<Map<String, SpacSchedule>>() {})
        if (m != null) spacScheduleList = m
        return spacScheduleList
    }

    suspend fun writeSpacSchedule(
        list: List<Pair<String, SpacSchedule>>,
        onSuccess: () -> Unit,
        onFail: () -> Unit,
    ) {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "write() failed: currentUser null")
            onFail()
            return
        }
        val ref = database.getReference(SNAPSHOT_KEY)
        val snapshot = ref.child("schedule")
        val m = list.associate { (date, schedule) -> date to schedule }

        snapshot.setValue(m)
            .addOnSuccessListener {
                spacScheduleList = m
                onSuccess()
            }
            .addOnFailureListener {
                onFail()
            }
    }
}
