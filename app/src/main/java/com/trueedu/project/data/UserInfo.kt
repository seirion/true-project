package com.trueedu.project.data

import android.util.Log
import com.trueedu.project.model.dto.account.AccountResponse
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AccountRemote
import com.trueedu.project.repository.remote.service.WebSocketService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInfo @Inject constructor(
    private val local: Local,
    private val tokenControl: TokenControl,
    private val accountRemote: AccountRemote,
    private val webSocketService: WebSocketService,
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
            startWebSocket()
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

    private fun startWebSocket() {
        Log.d(TAG, "startWebSocket()")

        webSocketService.connect(object: WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "onOpen()")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Log.d(TAG, "onMessage: $text")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }
}
