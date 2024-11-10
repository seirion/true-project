package com.trueedu.project.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.model.dto.firebase.UserAsset
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseRealtimeDatabase 에서 user/assets 데이터만 처리
 */
@Singleton
class FirebaseAssetsManager @Inject constructor(
    private val googleAccount: GoogleAccount,
) {
    companion object {
        private val TAG = FirebaseAssetsManager::class.java.simpleName
    }

    private val database = FirebaseDatabase.getInstance()

    suspend fun loadAssets(): List<UserAsset> {
        Log.d(TAG, "loadAssets()")
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "loadAssets() failed: currentUser null")
        }
        val userId = currentUser?.uid ?: return emptyList()
        val ref = database.getReference("users") // 종목 데이터
        val snapshot = ref.child(userId).child("assets")
        val list = snapshot.get().await()
            .getValue(object : GenericTypeIndicator<List<UserAsset>>() {})
        return list ?: emptyList()
    }

    suspend fun writeAssets(list :List<UserAsset>) {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            Log.d(TAG, "loadWatchList() failed: currentUser null")
        }
        val userId = currentUser?.uid ?: return

        val ref = database.getReference("users") // 종목 데이터
        val snapshot = ref.child(userId).child("assets")
        snapshot.setValue(list)
    }

    fun addAssets(asset: UserAsset) {

    }

    // FIXME: 중복 코드 정리하기
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
}
