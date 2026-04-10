package com.trueedu.project

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.UserAssets
import com.trueedu.project.data.log.logD
import com.trueedu.project.model.dto.account.AccountResponse
import com.trueedu.project.model.dto.account.PensionAccountResponse
import com.trueedu.project.model.dto.account.PensionFundResponse
import com.trueedu.project.data.firebase.FirebaseRealtimeDatabase
import com.trueedu.project.model.dto.account.AccountAsset
import com.trueedu.project.model.dto.firebase.AppNotice
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

    val loading = mutableStateOf(true)
    val googleSignInAccount = mutableStateOf<GoogleSignInAccount?>(null)
    val accountNum = mutableStateOf("")
    val userStocks = mutableStateOf<AccountResponse?>(null)
    val pensionStocks = mutableStateOf<PensionAccountResponse?>(null)
    val pensionFundStocks = mutableStateOf<PensionFundResponse?>(null)
    val marketPriceMode = mutableStateOf(local.marketPriceMode)
    val forceUpdateVisible = mutableStateOf(false)

    // notice
    val appNotice = mutableStateOf(AppNotice())

    // disclaimer
    val disclaimerVisible = mutableStateOf(local.disclaimerVisible)

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
                    logD("need app update")
                    forceUpdateVisible.value = true
                }
            }
            launch {
                firebaseDatabase.appNotice().let {
                    logD("notice: $it")
                    appNotice.value = it
                }
            }
            launch {
                userAssets.assets.collectLatest {
                    userStocks.value = it
                    loading.value = false
                }
            }
            launch {
                userAssets.pensionAssets.collectLatest {
                    pensionStocks.value = it
                }
            }
            launch {
                userAssets.pensionFundAssets.collectLatest {
                    pensionFundStocks.value = it
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

    fun refreshPension(accountNum: String, onSuccess: () -> Unit = {}) {
        userAssets.loadPensionStocks(
            accountNum = accountNum,
            onSuccess = onSuccess,
            onFail = {},
        )
        userAssets.loadPensionFundStocks(
            accountNum = accountNum,
            onFail = {},
        )
    }

    fun isPensionAccount(accountNum: String): Boolean {
        // 계좌번호 끝 2자리가 29이면 IRP 계좌
        return accountNum.length >= 2 && accountNum.takeLast(2) == "29"
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
