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
            .flowOn(Dispatchers.IO)
            .catch {
                onFail(it)
            }
            .onEach {
                if (it.fk100.isNotEmpty() && it.nk100.isNotEmpty() && it.output1.size >= 50) {
                    loadNext(it, accountNum, it.fk100, it.nk100, onSuccess, onFail)
                } else {
                    assets.emit(it)
                    onSuccess()
                }
            }
            .flowOn(Dispatchers.Main)
            .launchIn(MainScope())
    }

    // 자산 연속 조회
    private fun loadNext(
        prevResult: AccountResponse,
        accountNum: String,
        fk100: String,
        nk100: String,
        onSuccess: () -> Unit = {},
        onFail: (Throwable) -> Unit = {},
    ) {
        accountRemote.getUserStocks(accountNum, fk100, nk100)
            .flowOn(Dispatchers.IO)
            .catch {
                onFail(it)
            }
            .onEach {
                val output1 = prevResult.output1 + it.output1
                val result = it.copy(output1 = output1)
                // 원래 header tr_cont 를 체크해야 하지만 편의상 output1 크기로 체크하기
                if (it.fk100.isNotEmpty() && it.nk100.isNotEmpty() && it.output1.size >= 50) {
                    loadNext(result, accountNum, it.fk100, it.nk100, onSuccess, onFail)
                } else {
                    assets.emit(result)
                    onSuccess()
                }
            }
            .flowOn(Dispatchers.Main)
            .launchIn(MainScope())
    }

}
