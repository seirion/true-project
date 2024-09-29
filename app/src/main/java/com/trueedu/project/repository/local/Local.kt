package com.trueedu.project.repository.local

import android.content.SharedPreferences
import com.trueedu.project.extensions.boolean
import com.trueedu.project.extensions.int
import com.trueedu.project.extensions.long
import com.trueedu.project.extensions.string
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Local @Inject constructor(private val preferences: SharedPreferences) {
    private val latestVersion = 1
    private var migratedVersion by preferences.int(latestVersion)

    var launchingCount by preferences.long(0) // 앱 실행 횟수
        private set

    var appKey by preferences.string("")
    var appSecret by preferences.string("")

    var accessToken by preferences.string("")

    // 토큰 만료 예정 시각
    var accessTokenExpiredAt by preferences.long(0L)

    var refreshToken by preferences.string("")

    // UI
    var forceDark by preferences.boolean(false)
    var theme by preferences.int(1)
}
