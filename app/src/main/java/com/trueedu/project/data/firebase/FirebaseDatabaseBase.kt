package com.trueedu.project.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.trueedu.project.data.GoogleAccount
import kotlinx.coroutines.tasks.await

abstract class FirebaseDatabaseBase(
    protected val googleAccount: GoogleAccount,
) {
    protected val database = FirebaseDatabase.getInstance()

    protected suspend fun firebaseCurrentUser(): FirebaseUser? {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            return currentUser
        }

        if (!googleAccount.loggedIn()) {
            Log.d("Firebase", "cannot write values: currentUser == null")
            return null
        }
        val idToken = googleAccount.getToken()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        return auth.currentUser
    }
}
