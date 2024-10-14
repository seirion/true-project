package com.trueedu.project.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.service.voice.VoiceInteractionSession.ActivityId
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.*
import com.trueedu.project.R
import com.trueedu.project.analytics.TrueAnalytics
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sign

@Singleton
class GoogleAccount @Inject constructor(
    private val trueAnalytics: TrueAnalytics,
) {
    companion object {
        private val TAG = GoogleAccount::class.java.simpleName
        const val RC_SIGN_IN = 9001
    }

    var googleSignInAccount: GoogleSignInAccount? = null
    var loginSignal = MutableSharedFlow<Boolean>(1) // true - login, false - logout

    fun init(activity: Activity) {
        val gso = getGoogleSignInOptions(activity.applicationContext)
        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        googleSignInClient.silentSignIn()
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    setLoggedIn(task.result)
                    trueAnalytics.log("google_login_success", mapOf("silent_login" to true))
                } else {
                    // 자동 로그인 실패, 수동 로그인으로 전환
                    trueAnalytics.log("google_login_fail", mapOf("silent_login" to true))
                }
            }
    }

    private fun setLoggedIn(account: GoogleSignInAccount?) {
        googleSignInAccount = account
        MainScope().launch {
            loginSignal.emit(true)
        }
    }

    fun loggedIn() = googleSignInAccount?.let { !it.isExpired } ?: false

    fun getToken() = googleSignInAccount?.idToken

    fun getEmail() = googleSignInAccount?.email

    fun getProfileImage() = googleSignInAccount?.photoUrl

    fun login(activity: Activity) {
        Log.d(TAG, "login()")
        val gso = getGoogleSignInOptions(activity.applicationContext)
        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun logout(context: Context) {
        Log.d(TAG, "logout()")
        trueAnalytics.log("google_logout")
        val gso = getGoogleSignInOptions(context)
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut()
            .addOnCompleteListener {
                setLoggedIn(null)
                Log.d(TAG, "logout completed: ${loggedIn()}")
            }
    }

    private fun getGoogleSignInOptions(context: Context) =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?, context: Context) {
        if (data != null) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result?.isSuccess == true) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                trueAnalytics.log("google_login_success", mapOf("silent_login" to false))
                setLoggedIn(task.result)
            } else {
                result?.status?.let { status ->
                    Toast.makeText(
                        context,
                        "errorCode:${status.statusCode}, message:${status.statusMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}