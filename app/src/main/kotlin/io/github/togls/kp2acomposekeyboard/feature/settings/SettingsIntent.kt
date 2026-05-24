package io.github.togls.kp2acomposekeyboard.feature.settings

sealed interface SettingsIntent {
    data class ChangeThemeMode(
        val themeMode: KeyboardThemeMode,
    ) : SettingsIntent

    data class ChangeDynamicColorEnabled(
        val enabled: Boolean,
    ) : SettingsIntent

    data class ChangeSessionTimeoutSeconds(
        val seconds: Int,
    ) : SettingsIntent

    data class ChangeKeyboardHeightMode(
        val heightMode: KeyboardHeightMode,
    ) : SettingsIntent

    data class ChangeEnglishUsSubtypeEnabled(
        val enabled: Boolean,
    ) : SettingsIntent

    data class ChangeHapticFeedbackEnabled(
        val enabled: Boolean,
    ) : SettingsIntent

    data class ChangeKeySoundEnabled(
        val enabled: Boolean,
    ) : SettingsIntent

    data class ChangeKeyPreviewEnabled(
        val enabled: Boolean,
    ) : SettingsIntent

    data object ResetToDefault : SettingsIntent
}
