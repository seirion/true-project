package com.trueedu.project.ui.views.home

import androidx.compose.runtime.Composable

interface BottomNavScreen {
    fun onCreate()
    fun onStart() // 화면이 보일 때
    fun onStop() // 화면이 가려질 때

    fun screenName(): String {
        val simpleName = this::class.java.simpleName
        return simpleName
            .substring(0, simpleName.length - "Screen".length)
            .replace(Regex("(?<=[a-z])([A-Z])")) {
                "_${it.value}"
            }
            .lowercase()
    }

    @Composable
    fun Draw()
}