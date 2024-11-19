package com.trueedu.project.data

import android.util.Log
import com.trueedu.project.model.dto.account.AccountResponse
import com.trueedu.project.model.event.TokenIssued
import com.trueedu.project.repository.remote.AccountRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAssets @Inject constructor(
    private val tokenKeyManager: TokenKeyManager,
    private val accountRemote: AccountRemote,
) {
    companion object {
        private val TAG = UserAssets::class.java.simpleName
    }

    var job: Job? = null
    val assets = MutableSharedFlow<AccountResponse>(1)

    // 앱이 foreground 상태가 될 때
    fun start() {
        Log.d(TAG, "start")
        loadUserStocks()

        job = MainScope().launch {
            tokenKeyManager.observeTokenKeyEvent()
                .collect {
                    if (it is TokenIssued) {
                        loadUserStocks()
                    }
                }
        }
    }

    // 앱이 background 상태가 될 때
    fun stop() {
        Log.d(TAG, "stop")
        job?.cancel()
        job = null
    }

    fun loadUserStocks(
        onSuccess: () -> Unit = {},
        onFail: (Throwable) -> Unit = {},
    ) {
        val accountNum = tokenKeyManager.userKey.value?.accountNum
        if (accountNum.isNullOrEmpty()) return
        accountRemote.getUserStocks(accountNum)
            .flowOn(Dispatchers.Main)
            .catch {
                onFail(it)
            }
            .onEach {
                assets.emit(it)
                onSuccess()
            }
            .launchIn(MainScope())
    }
}
