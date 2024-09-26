package com.trueedu.project.data

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.repository.local.Local
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenControl @Inject constructor(
    local: Local,
) {
    val forceDark = mutableStateOf(local.forceDark)
    val theme = mutableIntStateOf(local.theme)
}
