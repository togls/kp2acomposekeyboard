package io.github.togls.kp2acomposekeyboard.application.settings

import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlots
import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardSettings
import kotlinx.coroutines.flow.Flow

interface KeyboardSettingsStore {
    val settings: Flow<KeyboardSettings>

    suspend fun updateQuickActionSlots(slots: KeyboardQuickActionSlots)
}
