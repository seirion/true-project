package com.trueedu.project

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.model.dto.RevokeTokenRequest
import com.trueedu.project.model.dto.TokenRequest
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AuthRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val local: Local,
    private val authRemote: AuthRemote,
): ViewModel() {

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }

    fun init() {
        val accessToken = local.accessToken
        Log.d(TAG,"accessToken: $accessToken")
        if (accessToken.isEmpty()) {
            issueAccessToken()
        }
    }

    private fun issueAccessToken() {
        Log.d(TAG, "appKey: ${local.appKey}")
        Log.d(TAG, "appSecret: ${local.appSecret}")

        if (local.appKey.isEmpty() || local.appSecret.isEmpty()) {
            Log.d(TAG, "appKey appSecret is empty")
            return
        }
        val request = TokenRequest(
            grantType = "client_credentials",
            appKey = local.appKey,
            appSecret = local.appSecret,
        )

        authRemote.refreshToken(request)
            .catch {
                Log.e(TAG, "failed to get AccessToken: $it")
                // service not available
            }
            .onEach {
                local.accessToken = it.accessToken
                Log.d(TAG, "new token: $it")
            }
            .launchIn(viewModelScope)
    }

    private fun revokeToken() {
        if (local.accessToken.isEmpty()) {
            return
        }

        val request = RevokeTokenRequest(
            appKey = local.appKey,
            appSecret = local.appSecret,
            token = local.accessToken,
        )
        authRemote.revokeToken(request)
            .catch {
                Log.e(TAG, "failed to revoke AccessToken: $it")
                // service not available
            }
            .onEach {
                Log.d(TAG, "revoke ok: $it")
            }
            .launchIn(viewModelScope)
    }
}
