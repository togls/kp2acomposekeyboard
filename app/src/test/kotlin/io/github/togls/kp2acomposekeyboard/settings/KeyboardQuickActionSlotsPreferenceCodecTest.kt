package io.github.togls.kp2acomposekeyboard.settings

import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlots
import io.github.togls.kp2acomposekeyboard.domain.keyboard.SettingsQuickActionId
import io.github.togls.kp2acomposekeyboard.data.settings.KeyboardQuickActionSlotsPreferenceCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeyboardQuickActionSlotsPreferenceCodecTest {

    @Test
    fun encode_sanitizesSlotsBeforeWriting() {
        val rawValue = KeyboardQuickActionSlotsPreferenceCodec.encode(
            KeyboardQuickActionSlots(
                centerItemIds = listOf(SettingsQuickActionId, SettingsQuickActionId),
                rightItemId = SettingsQuickActionId,
            ),
        )

        assertEquals("center=settings;right=", rawValue)
    }

    @Test
    fun decode_readsEmptyCenterAndEmptyRight() {
        val slots = KeyboardQuickActionSlotsPreferenceCodec.decode("center=;right=")

        assertEquals(emptyList<KeyboardQuickActionId>(), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun decode_removesDuplicatesAndRightDuplicate() {
        val slots = KeyboardQuickActionSlotsPreferenceCodec.decode(
            "center=settings,settings;right=settings",
        )

        assertEquals(listOf(SettingsQuickActionId), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun decode_ignoresUnknownIds() {
        val slots = KeyboardQuickActionSlotsPreferenceCodec.decode(
            "center=settings,unknown;right=missing",
        )

        assertEquals(listOf(SettingsQuickActionId), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun decode_acceptsCenterAndRightInAnyOrder() {
        val slots = KeyboardQuickActionSlotsPreferenceCodec.decode("right=;center=settings")

        assertEquals(listOf(SettingsQuickActionId), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun decode_ignoresExtraParts() {
        val slots = KeyboardQuickActionSlotsPreferenceCodec.decode("center=settings;right=;extra=x")

        assertEquals(listOf(SettingsQuickActionId), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun decode_returnsDefaultForMalformedValues() {
        val slots = KeyboardQuickActionSlotsPreferenceCodec.decode("broken-value")

        assertEquals(KeyboardQuickActionSlots(), slots)
    }
}
