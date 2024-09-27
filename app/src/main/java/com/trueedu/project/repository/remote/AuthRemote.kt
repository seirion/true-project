package com.trueedu.project.repository.remote

import com.trueedu.project.model.dto.HashKeyRequest
import com.trueedu.project.model.dto.HashKeyResponse
import com.trueedu.project.model.dto.RevokeTokenRequest
import com.trueedu.project.model.dto.RevokeTokenResponse
import com.trueedu.project.model.dto.TokenRequest
import com.trueedu.project.model.dto.TokenResponse
import kotlinx.coroutines.flow.Flow

interface AuthRemote {
    fun refreshToken(request: TokenRequest): Flow<TokenResponse>

    fun revokeToken(request: RevokeTokenRequest): Flow<RevokeTokenResponse>

    fun hashKey(request: HashKeyRequest): Flow<HashKeyResponse>
}