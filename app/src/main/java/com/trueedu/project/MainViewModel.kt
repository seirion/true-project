package com.trueedu.project

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.UserInfo
import com.trueedu.project.model.dto.account.AccountResponse
import com.trueedu.project.repository.FirebaseRealtimeDatabase
import com.trueedu.project.repository.local.Local
import com.trueedu.project.utils.toAccountNumFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val local: Local,
    private val userInfo: UserInfo,
    private val googleAccount: GoogleAccount,
    private val trueAnalytics: TrueAnalytics,
    private val firebaseDatabase: FirebaseRealtimeDatabase,
): ViewModel() {

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }

    val googleSignInAccount = mutableStateOf<GoogleSignInAccount?>(null)
    val accountNum = mutableStateOf("")
    val account = mutableStateOf<AccountResponse?>(null)
    val marketPriceMode = mutableStateOf(local.marketPriceMode)
    val forceUpdateVisible = mutableStateOf(false)

    fun init() {
        viewModelScope.launch {
            launch {
                if (firebaseDatabase.needForceUpdate()) {
                    Log.d(TAG, "need app update")
                    forceUpdateVisible.value = true
                }
            }
            launch {
                userInfo.userStocks.collectLatest {
                    accountNum.value = local.getUserKeys()
                        .lastOrNull()
                        ?.accountNum
                        .toAccountNumFormat()
                    account.value = it
                }
            }
            launch {
                googleAccount.loginSignal
                    .collect {
                        googleSignInAccount.value = googleAccount.googleSignInAccount
                    }
            }
        }
    }

    fun onChangeMarketPriceMode(selected: Int) {
        val state = selected == 0
        trueAnalytics.log(
            "main__daily_profit_mode__click",
            mapOf("selected" to selected)
        )
        local.marketPriceMode = state
        marketPriceMode.value = state
    }
}
