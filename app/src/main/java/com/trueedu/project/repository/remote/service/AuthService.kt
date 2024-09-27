package com.trueedu.project.repository.remote.service

import com.trueedu.project.model.dto.HashKeyRequest
import com.trueedu.project.model.dto.HashKeyResponse
import com.trueedu.project.model.dto.RevokeTokenRequest
import com.trueedu.project.model.dto.RevokeTokenResponse
import com.trueedu.project.model.dto.TokenRequest
import com.trueedu.project.model.dto.TokenResponse
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
