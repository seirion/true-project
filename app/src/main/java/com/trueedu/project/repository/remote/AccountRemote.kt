package com.trueedu.project.repository.remote

import com.trueedu.project.model.dto.account.AccountResponse
import com.trueedu.project.model.dto.account.PensionAccountResponse
import com.trueedu.project.model.dto.account.PensionFundResponse
import kotlinx.coroutines.flow.Flow

interface AccountRemote {
    fun getUserStocks(
        accountNum: String,
        fk100: String = "",
        nk100: String = "",
    ): Flow<AccountResponse>

    fun getPensionStocks(
        accountNum: String,
        fk100: String = "",
        nk100: String = "",
    ): Flow<PensionAccountResponse>

    fun getPensionFundStocks(
        accountNum: String,
        fk100: String = "",
        nk100: String = "",
    ): Flow<PensionFundResponse>
}
