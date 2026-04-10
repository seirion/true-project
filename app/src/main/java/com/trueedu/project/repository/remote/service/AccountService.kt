package com.trueedu.project.repository.remote.service

import com.trueedu.project.model.dto.account.AccountResponse
import com.trueedu.project.model.dto.account.PensionAccountResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap

interface AccountService {
    @GET("uapi/domestic-stock/v1/trading/inquire-balance")
    suspend fun getAccount(
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>
    ): Response<AccountResponse>

    @GET("uapi/domestic-stock/v1/trading/pension/inquire-balance")
    suspend fun getPensionAccount(
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>
    ): Response<PensionAccountResponse>
}
