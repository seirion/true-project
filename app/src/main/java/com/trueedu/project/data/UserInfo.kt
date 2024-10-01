package com.trueedu.project.data

import android.util.Log
import com.trueedu.project.repository.local.Local
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInfo @Inject constructor(
    private val local: Local,
    private val tokenControl: TokenControl,
) {
    companion object {
        private val TAG = UserInfo::class.java.simpleName
    }

    // 앱이 foreground 상태가 될 때
    fun start() {
        Log.d(TAG, "start")
        init()
    }

    // 앱이 background 상태가 될 때
    fun stop() {
        Log.d(TAG, "stop")
    }

    fun init() {
        val accessToken = local.accessToken
        Log.d(TAG,"accessToken: $accessToken")
        tokenControl.issueAccessToken()
    }
}
