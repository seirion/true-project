package com.trueedu.project.repository.remote

import com.trueedu.project.model.dto.auth.ApprovalKeyResponse
import com.trueedu.project.model.dto.auth.HashKeyRequest
import com.trueedu.project.model.dto.auth.HashKeyResponse
import com.trueedu.project.model.dto.auth.RevokeTokenRequest
import com.trueedu.project.model.dto.auth.RevokeTokenResponse
import com.trueedu.project.model.dto.auth.TokenRequest
import com.trueedu.project.model.dto.auth.TokenResponse
import com.trueedu.project.model.dto.auth.WebSocketKeyRequest
import kotlinx.coroutines.flow.Flow

interface AuthRemote {
    fun refreshToken(request: TokenRequest): Flow<TokenResponse>

    fun revokeToken(request: RevokeTokenRequest): Flow<RevokeTokenResponse>

    fun webSocketKey(request: WebSocketKeyRequest): Flow<ApprovalKeyResponse>

    fun hashKey(request: HashKeyRequest): Flow<HashKeyResponse>
}