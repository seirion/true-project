package com.trueedu.project

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.UserInfo
import com.trueedu.project.model.dto.account.AccountResponse
import com.trueedu.project.repository.local.Local
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val local: Local,
    private val userInfo: UserInfo,
    private val trueAnalytics: TrueAnalytics,
): ViewModel() {

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }

    val accountNum = mutableStateOf("")
    val account = mutableStateOf<AccountResponse?>(null)
    val dailyProfitMode = mutableStateOf(local.dailyProfitMode)

    fun init() {
        viewModelScope.launch {
            launch {
                userInfo.account.collectLatest {
                    accountNum.value = accountNumFormat(local.currentAccountNumber)
                    account.value = it
                }
            }
        }
    }

    private fun accountNumFormat(str: String): String {
        return if (str.length == 10) {
            str.take(8) + "-" + str.drop(8)
        } else {
            str
        }
    }

    fun onChangeDailyProfitMode(state: Boolean) {
        trueAnalytics.clickToggleButton("main__daily_profit_mode__click", !state)
        local.dailyProfitMode = state
        dailyProfitMode.value = state
    }
}
