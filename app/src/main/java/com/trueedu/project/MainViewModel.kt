package com.trueedu.project

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.UserAssets
import com.trueedu.project.model.dto.account.AccountResponse
import com.trueedu.project.data.firebase.FirebaseRealtimeDatabase
import com.trueedu.project.model.dto.account.AccountAsset
import com.trueedu.project.repository.local.Local
import com.trueedu.project.utils.toAccountNumFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val local: Local,
    private val userAssets: UserAssets,
    private val googleAccount: GoogleAccount,
    private val tokenKeyManager: TokenKeyManager,
    private val trueAnalytics: TrueAnalytics,
    private val firebaseDatabase: FirebaseRealtimeDatabase,
): ViewModel() {

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(true)
    val googleSignInAccount = mutableStateOf<GoogleSignInAccount?>(null)
    val accountNum = mutableStateOf("")
    val userStocks = mutableStateOf<AccountResponse?>(null)
    val marketPriceMode = mutableStateOf(local.marketPriceMode)
    val forceUpdateVisible = mutableStateOf(false)

    fun init() {
        if (local.getUserKeys().isEmpty()) {
            // 키가 없어서 자산을 못 불러오면 로딩 상태가 불필요함
            loading.value = false
        }
        viewModelScope.launch {
            launch {
                if (firebaseDatabase.needForceUpdate()) {
                    trueAnalytics.log(
                        "force_update__need",
                        mapOf("version" to BuildConfig.VERSION_NAME)
                    )
                    Log.d(TAG, "need app update")
                    forceUpdateVisible.value = true
                }
            }
            launch {
                userAssets.assets.collectLatest {
                    userStocks.value = it
                    loading.value = false
                }
            }
            launch {
                googleAccount.loginSignal
                    .collect {
                        googleSignInAccount.value = googleAccount.googleSignInAccount
                    }
            }
            launch {
                snapshotFlow { tokenKeyManager.userKey.value }
                    .filterNotNull()
                    .collect {
                        accountNum.value = tokenKeyManager.userKey.value
                            ?.accountNum
                            .toAccountNumFormat()
                    }
            }
        }
    }

    fun refresh(onSuccess: () -> Unit) {
        tokenKeyManager.userKey.value ?: return
        userAssets.loadUserStocks(
            onSuccess = {
                onSuccess()
            },
            onFail = {
            }
        )
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

    fun getUserStock(code: String): AccountAsset? {
        return userStocks.value?.output1?.firstOrNull { it.code == code }
    }
}
