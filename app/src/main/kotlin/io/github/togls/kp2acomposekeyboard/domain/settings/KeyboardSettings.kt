package io.github.togls.kp2acomposekeyboard.domain.settings

import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlots

data class KeyboardSettings(
    val themeMode: KeyboardThemeMode = KeyboardThemeMode.System,
    val useDynamicColor: Boolean = true,
    val sessionTimeoutSeconds: Int = DEFAULT_SESSION_TIMEOUT_SECONDS,
    val keyboardHeightMode: KeyboardHeightMode = KeyboardHeightMode.Normal,
    val englishUsSubtypeEnabled: Boolean = false,
    val hapticFeedbackEnabled: Boolean = false,
    val keySoundEnabled: Boolean = false,
    val showKeyPreview: Boolean = false,
    val quickActionSlots: KeyboardQuickActionSlots = KeyboardQuickActionSlots(),
) {
    init {
        // Keep the invariant close to the model so storage, UI, and tests share one safe range.
        require(sessionTimeoutSeconds in MIN_SESSION_TIMEOUT_SECONDS..MAX_SESSION_TIMEOUT_SECONDS) {
            "sessionTimeoutSeconds must be in $MIN_SESSION_TIMEOUT_SECONDS..$MAX_SESSION_TIMEOUT_SECONDS"
        }
    }

    companion object {
        // Sessions hold password, TOTP, and recovery-code values; keep the default lifetime short.
        const val DEFAULT_SESSION_TIMEOUT_SECONDS = 60
        const val MIN_SESSION_TIMEOUT_SECONDS = 15
        const val MAX_SESSION_TIMEOUT_SECONDS = 300
    }
}
