package com.trueedu.project.repository.remote

import com.trueedu.project.di.NormalService
import com.trueedu.project.model.dto.RevokeTokenRequest
import com.trueedu.project.model.dto.TokenRequest
import com.trueedu.project.network.apiCallFlow
import com.trueedu.project.repository.remote.service.AuthService

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
}
