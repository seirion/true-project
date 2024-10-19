package com.trueedu.project.model.event

sealed class TokenKeyEvent

// 새 토큰이 발행 될 때
class TokenIssued: TokenKeyEvent()

class TokenIssueFail: TokenKeyEvent()

class TokenExpired: TokenKeyEvent()

class TokenRevoked: TokenKeyEvent()

// 새 websocket key가 발행 될 때
class WebSocketKeyIssued: TokenKeyEvent()
