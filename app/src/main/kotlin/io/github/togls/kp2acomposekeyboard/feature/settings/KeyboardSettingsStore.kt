package io.github.togls.kp2acomposekeyboard.feature.settings

import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardSettings

import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlots
import kotlinx.coroutines.flow.Flow

interface KeyboardSettingsStore {
    val settings: Flow<KeyboardSettings>

    suspend fun updateUtilitySlots(slots: KeyboardUtilitySlots)
}
