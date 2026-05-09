package io.github.togls.kp2acomposekeyboard.feature.settings

data class KeyboardSettings(
    val themeMode: KeyboardThemeMode = KeyboardThemeMode.System,
    val useDynamicColor: Boolean = true,
    val sessionTimeoutSeconds: Int = DEFAULT_SESSION_TIMEOUT_SECONDS,
    val keyboardHeightMode: KeyboardHeightMode = KeyboardHeightMode.Normal,
    val hapticFeedbackEnabled: Boolean = false,
    val keySoundEnabled: Boolean = false,
    val showKeyPreview: Boolean = false,
) {
    init {
        require(sessionTimeoutSeconds in MIN_SESSION_TIMEOUT_SECONDS..MAX_SESSION_TIMEOUT_SECONDS) {
            "sessionTimeoutSeconds must be in $MIN_SESSION_TIMEOUT_SECONDS..$MAX_SESSION_TIMEOUT_SECONDS"
        }
    }

    companion object {
        // Session 持有密码/TOTP 等字段值，默认 60 秒用于降低遗留敏感数据的风险。
        const val DEFAULT_SESSION_TIMEOUT_SECONDS = 60
        const val MIN_SESSION_TIMEOUT_SECONDS = 15
        const val MAX_SESSION_TIMEOUT_SECONDS = 300
    }
}