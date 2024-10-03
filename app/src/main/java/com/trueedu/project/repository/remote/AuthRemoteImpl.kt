package com.trueedu.project.repository.remote

import com.trueedu.project.di.NormalService
import com.trueedu.project.model.dto.auth.HashKeyRequest
import com.trueedu.project.model.dto.auth.RevokeTokenRequest
import com.trueedu.project.model.dto.auth.TokenRequest
import com.trueedu.project.model.dto.auth.WebSocketKeyRequest
import com.trueedu.project.network.apiCallFlow
import com.trueedu.project.repository.remote.service.AuthService
import kotlinx.coroutines.flow.Flow

class AuthRemoteImpl(
    @NormalService
    private val authService: AuthService
): AuthRemote {
    override fun refreshToken(request: TokenRequest) = apiCallFlow {
        authService.refreshToken(request)
    }

    override fun revokeToken(request: RevokeTokenRequest) = apiCallFlow {
        authService.revokeToken(request)
    }

    override fun webSocketKey(request: WebSocketKeyRequest) = apiCallFlow {
        authService.webSocketKey(request)
    }

    override fun hashKey(request: HashKeyRequest) = apiCallFlow {
        authService.hashKey(request)
    }
}
