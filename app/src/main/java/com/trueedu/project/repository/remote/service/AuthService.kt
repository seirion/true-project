package com.trueedu.project.repository.remote.service

import com.trueedu.project.model.dto.auth.HashKeyRequest
import com.trueedu.project.model.dto.auth.HashKeyResponse
import com.trueedu.project.model.dto.auth.RevokeTokenRequest
import com.trueedu.project.model.dto.auth.RevokeTokenResponse
import com.trueedu.project.model.dto.auth.TokenRequest
import com.trueedu.project.model.dto.auth.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("oauth2/tokenP")
    suspend fun refreshToken(@Body request: TokenRequest): Response<TokenResponse>

    @POST("oauth2/revokeP")
    suspend fun revokeToken(@Body request: RevokeTokenRequest): Response<RevokeTokenResponse>

    @POST("uapi/hashkey")
    suspend fun hashKey(@Body request: HashKeyRequest): Response<HashKeyResponse>
}
