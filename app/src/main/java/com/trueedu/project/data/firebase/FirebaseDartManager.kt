package com.trueedu.project.data.firebase

import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.project.dart.model.DartListResponse
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.log.logD
import com.trueedu.project.utils.yyyyMMddHHmm
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseRealtimeDatabase 에서 dart 데이터 처리
 */
@Singleton
class FirebaseDartManager @Inject constructor(
    googleAccount: GoogleAccount,
): FirebaseDatabaseBase(googleAccount) {
    companion object {
        private const val BASE_PATH = "dart"
        private const val CHILD_PATH = "list"
    }

    /**
     * yyyyMMddHHmm 형식의 Long 타입 반환
     */
    suspend fun lastUpdatedAt(): Long {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            logD("lastUpdatedAt() failed: currentUser null")
        }
        val snapshot = database.getReference("meta").get().await()
        val lastUpdatedAt = snapshot.child("dartLastUpdatedAt").getValue(Long::class.java)
        return lastUpdatedAt ?: 0L
    }

    suspend fun loadDartList(): List<DartListResponse> {
        logD("loadDartList()")
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            logD("loadAssets() failed: currentUser null")
        }
        val ref = database.getReference(BASE_PATH)
        val snapshot = ref.child(CHILD_PATH)
        val list = snapshot.get().await()
            .getValue(object : GenericTypeIndicator<List<DartListResponse>>() {})
        return list ?: emptyList()
    }

    suspend fun writeDartList(list :List<DartListResponse>) {
        logD("writeDartList(): ${list.size}")
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            logD("writeDartList() failed: currentUser null")
        }
        val ref = database.getReference(BASE_PATH)
        val snapshot = ref.child(CHILD_PATH)
        snapshot.setValue(list)

        val metaRef = database.getReference("meta")
        metaRef.child("dartLastUpdatedAt").setValue(
            Date().yyyyMMddHHmm().toLong()
        )
    }
}
