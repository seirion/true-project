package com.trueedu.project.data.firebase

import android.util.Log
import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.project.data.GoogleAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseWatchManager @Inject constructor(
    googleAccount: GoogleAccount,
): FirebaseDatabaseBase(googleAccount) {
    companion object {
        private val TAG = FirebaseWatchManager::class.java.simpleName
    }

    suspend fun loadWatchList(): List<List<String>> {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "loadWatchList() failed: currentUser null")
        }
        val userId = currentUser?.uid ?: return emptyList()

        val ref = database.getReference("users") // 종목 데이터
        val snapshot = ref.child(userId).child("watch")
        val list = snapshot.get().await()
            .getValue(object : GenericTypeIndicator<List<List<String>>>() {})
        return list ?: emptyList()
    }

    fun writeWatchList(list :List<List<String>>) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentUser = firebaseCurrentUser()
            if (currentUser == null) {
                Log.d(TAG, "writeWatchList() failed: currentUser null")
            }
            val userId = currentUser?.uid ?: return@launch

            val ref = database.getReference("users") // 종목 데이터
            val snapshot = ref.child(userId).child("watch")
            snapshot.setValue(list)
        }
    }
}