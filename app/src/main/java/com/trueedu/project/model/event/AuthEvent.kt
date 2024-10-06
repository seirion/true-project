package com.trueedu.project.model.event

sealed class AuthEvent

// 새 토큰이 발행 될 때
class TokenIssued: AuthEvent()

class TokenExpired: AuthEvent()

class TokenRevoked: AuthEvent()

// 새 websocket key가 발행 될 때
class WebSocketKeyIssued: AuthEvent()
