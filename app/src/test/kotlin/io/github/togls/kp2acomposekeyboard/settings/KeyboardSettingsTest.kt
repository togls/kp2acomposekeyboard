package io.github.togls.kp2acomposekeyboard.settings

import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardHeightMode
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardSettingsTest {

    @Test
    fun defaultSettings_areExpectedValues() {
        val settings = KeyboardSettings()

        assertEquals(KeyboardThemeMode.System, settings.themeMode)
        assertEquals(true, settings.useDynamicColor)
        assertEquals(60, settings.sessionTimeoutSeconds)
        assertEquals(KeyboardHeightMode.Normal, settings.keyboardHeightMode)
        assertEquals(false, settings.hapticFeedbackEnabled)
        assertEquals(false, settings.keySoundEnabled)
        assertEquals(false, settings.showKeyPreview)
    }

    @Test(expected = IllegalArgumentException::class)
    fun settings_throwsWhenTimeoutIsTooSmall() {
        KeyboardSettings(sessionTimeoutSeconds = 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun settings_throwsWhenTimeoutIsTooLarge() {
        KeyboardSettings(sessionTimeoutSeconds = 999)
    }
}