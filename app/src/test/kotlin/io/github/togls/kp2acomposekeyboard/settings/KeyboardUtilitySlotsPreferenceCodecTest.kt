package io.github.togls.kp2acomposekeyboard.settings

import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilityItemId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlots
import io.github.togls.kp2acomposekeyboard.domain.keyboard.SettingsUtilityItemId
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardUtilitySlotsPreferenceCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeyboardUtilitySlotsPreferenceCodecTest {

    @Test
    fun encode_sanitizesSlotsBeforeWriting() {
        val rawValue = KeyboardUtilitySlotsPreferenceCodec.encode(
            KeyboardUtilitySlots(
                centerItemIds = listOf(SettingsUtilityItemId, SettingsUtilityItemId),
                rightItemId = SettingsUtilityItemId,
            ),
        )

        assertEquals("center=settings;right=", rawValue)
    }

    @Test
    fun decode_readsEmptyCenterAndEmptyRight() {
        val slots = KeyboardUtilitySlotsPreferenceCodec.decode("center=;right=")

        assertEquals(emptyList<KeyboardUtilityItemId>(), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun decode_removesDuplicatesAndRightDuplicate() {
        val slots = KeyboardUtilitySlotsPreferenceCodec.decode(
            "center=settings,settings;right=settings",
        )

        assertEquals(listOf(SettingsUtilityItemId), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun decode_ignoresUnknownIds() {
        val slots = KeyboardUtilitySlotsPreferenceCodec.decode(
            "center=settings,unknown;right=missing",
        )

        assertEquals(listOf(SettingsUtilityItemId), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun decode_acceptsCenterAndRightInAnyOrder() {
        val slots = KeyboardUtilitySlotsPreferenceCodec.decode("right=;center=settings")

        assertEquals(listOf(SettingsUtilityItemId), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun decode_ignoresExtraParts() {
        val slots = KeyboardUtilitySlotsPreferenceCodec.decode("center=settings;right=;extra=x")

        assertEquals(listOf(SettingsUtilityItemId), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun decode_returnsDefaultForMalformedValues() {
        val slots = KeyboardUtilitySlotsPreferenceCodec.decode("broken-value")

        assertEquals(KeyboardUtilitySlots(), slots)
    }
}
