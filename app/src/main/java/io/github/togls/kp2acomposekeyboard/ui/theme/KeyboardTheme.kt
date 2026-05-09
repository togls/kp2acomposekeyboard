package io.github.togls.kp2acomposekeyboard.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.togls.kp2acomposekeyboard.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.settings.KeyboardThemeMode

private val LightColorScheme = lightColorScheme()

private val DarkColorScheme = darkColorScheme()

@Composable
fun KeyboardTheme(
    settings: KeyboardSettings = KeyboardSettings(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val darkTheme = when (settings.themeMode) {
        KeyboardThemeMode.System -> isSystemInDarkTheme()
        KeyboardThemeMode.Light -> false
        KeyboardThemeMode.Dark -> true
    }

    val colorScheme = when {
        // 动态颜色只在 Android 12+ 可用；低版本直接回退到默认 Material 3 色彩。
        settings.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColorScheme

        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}