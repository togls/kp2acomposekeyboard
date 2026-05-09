package io.github.togls.kp2acomposekeyboard.feature.settings

sealed interface SettingsEffect {
    data object ShowSavedMessage : SettingsEffect

    data class ShowError(
        val message: String,
    ) : SettingsEffect
}