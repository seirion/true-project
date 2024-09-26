package com.trueedu.project.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.trueedu.project.ui.theme2.AppTheme2

private val DarkColorScheme = darkColorScheme()

private val LightColorScheme = lightColorScheme()

@Composable
fun TrueProjectTheme(
    n: Int = 1,
    forceDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = forceDark || isSystemInDarkTheme()
    when (n) {
        0 -> TrueProjectThemeDefault(darkTheme, false, content)
        1 -> AppTheme2(darkTheme, false, content)
        else -> TrueProjectThemeDefault(darkTheme, false, content) // default
    }
}

@Composable
fun TrueProjectThemeDefault(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}