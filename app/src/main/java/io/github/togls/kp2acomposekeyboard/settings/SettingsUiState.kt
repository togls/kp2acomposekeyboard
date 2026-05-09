package io.github.togls.kp2acomposekeyboard.feature.settings

import io.github.togls.kp2acomposekeyboard.settings.KeyboardSettings

data class SettingsUiState(
    val settings: KeyboardSettings = KeyboardSettings(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)