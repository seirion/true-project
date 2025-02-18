package com.trueedu.project.repository.remote

import com.trueedu.project.model.dto.account.AccountResponse
import kotlinx.coroutines.flow.Flow

interface AccountRemote {
    fun getUserStocks(
        accountNum: String,
        fk100: String = "",
        nk100: String = "",
    ): Flow<AccountResponse>
}
