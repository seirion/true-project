package com.trueedu.project.base

import androidx.compose.runtime.Composable

interface ComposableDrawer {
    @Composable
    fun Draw()

    fun screenName(): String {
        val simpleName = this::class.java.simpleName
        return simpleName
            .substring(0, simpleName.length - "Drawer".length)
            .replace(Regex("(?<=[a-z])([A-Z])")) {
                "_${it.value}"
            }
            .lowercase()
    }
}
