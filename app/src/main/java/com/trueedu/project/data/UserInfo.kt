package com.trueedu.project.data

import android.util.Log
import com.trueedu.project.model.dto.account.AccountResponse
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AccountRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInfo @Inject constructor(
    private val local: Local,
    private val tokenControl: TokenControl,
    private val accountRemote: AccountRemote,
) {
    companion object {
        private val TAG = UserInfo::class.java.simpleName
    }

    val account = MutableSharedFlow<AccountResponse>(1)

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
        tokenControl.issueAccessToken {
            loadAccount(local.currentAccountNumber)
        }

        tokenControl.issueWebSocketKey {
            Log.d(TAG,"webSocketKey: ${local.webSocketKey}")
        }
    }

    fun loadAccount(
        accountNum: String? = null,
        onSuccess: () -> Unit = {},
        onFail: (Throwable) -> Unit = {},
    ) {
        if (accountNum.isNullOrEmpty()) return
        accountRemote.getAccount(accountNum)
            .catch {
                withContext(Dispatchers.Main) {
                    onFail(it)
                }
            }
            .onEach {
                account.emit(it)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
            .launchIn(MainScope())
    }
}
