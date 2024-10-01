package com.trueedu.project.repository.remote

import com.trueedu.project.model.dto.account.AccountResponse
import kotlinx.coroutines.flow.Flow

interface AccountRemote {
    fun getAccount(
        accountNum: String,
    ): Flow<AccountResponse>
}
