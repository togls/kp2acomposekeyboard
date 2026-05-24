package io.github.togls.kp2acomposekeyboard.ime

import android.view.inputmethod.InputMethodSubtype
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardSubtype
import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class KeyboardSubtypeRegistryTest {

    @Test
    fun fromInputMethodSubtype_mapsEntryExtraToEntry() {
        val subtype = subtypeWithExtra("layout=entry")

        assertEquals(KeyboardSubtype.Entry, KeyboardSubtypeRegistry.fromInputMethodSubtype(subtype))
    }

    @Test
    fun fromInputMethodSubtype_mapsEnglishExtraToEnglishUs() {
        val subtype = subtypeWithExtra("layout=english_us")

        assertEquals(KeyboardSubtype.EnglishUs, KeyboardSubtypeRegistry.fromInputMethodSubtype(subtype))
    }

    @Test
    fun fromInputMethodSubtype_mapsMissingExtraToEntry() {
        val subtype = subtypeWithExtra("")

        assertEquals(KeyboardSubtype.Entry, KeyboardSubtypeRegistry.fromInputMethodSubtype(subtype))
    }

    @Test
    fun fromInputMethodSubtype_mapsUnknownExtraToEntry() {
        val subtype = subtypeWithExtra("layout=unknown")

        assertEquals(KeyboardSubtype.Entry, KeyboardSubtypeRegistry.fromInputMethodSubtype(subtype))
    }

    @Test
    fun englishUsSubtype_usesStableMetadata() {
        val subtype = KeyboardSubtypeRegistry.englishUsInputMethodSubtype()

        assertEquals("keyboard", subtype.mode)
        assertEquals("en_US", subtype.locale)
        assertEquals("layout=english_us", subtype.extraValue)
        assertTrue(subtype.isAsciiCapable)
        assertFalse(subtype.isAuxiliary)
    }

    @Test
    fun additionalSubtypes_returnsEmptyArrayWhenEnglishDisabled() {
        val subtypes = KeyboardSubtypeRegistry.additionalSubtypes(
            KeyboardSettings(englishUsSubtypeEnabled = false),
        )

        assertEquals(0, subtypes.size)
    }

    @Test
    fun additionalSubtypes_returnsEnglishSubtypeWhenEnglishEnabled() {
        val subtypes = KeyboardSubtypeRegistry.additionalSubtypes(
            KeyboardSettings(englishUsSubtypeEnabled = true),
        )

        assertEquals(1, subtypes.size)
        assertEquals(KeyboardSubtype.EnglishUs, KeyboardSubtypeRegistry.fromInputMethodSubtype(subtypes.single()))
    }

    @Test
    fun explicitlyEnabledSubtypeHashCodes_returnsEntryOnlyWhenEnglishDisabled() {
        val hashCodes = KeyboardSubtypeRegistry.explicitlyEnabledSubtypeHashCodes(
            KeyboardSettings(englishUsSubtypeEnabled = false),
        )

        assertEquals(listOf(KeyboardSubtypeRegistry.ENTRY_SUBTYPE_ID), hashCodes.toList())
    }

    @Test
    fun explicitlyEnabledSubtypeHashCodes_returnsEntryAndEnglishWhenEnglishEnabled() {
        val hashCodes = KeyboardSubtypeRegistry.explicitlyEnabledSubtypeHashCodes(
            KeyboardSettings(englishUsSubtypeEnabled = true),
        )

        assertEquals(
            listOf(
                KeyboardSubtypeRegistry.ENTRY_SUBTYPE_ID,
                KeyboardSubtypeRegistry.englishUsInputMethodSubtype().hashCode(),
            ),
            hashCodes.toList(),
        )
    }

    private fun subtypeWithExtra(extraValue: String): InputMethodSubtype {
        return InputMethodSubtype.InputMethodSubtypeBuilder()
            .setSubtypeId(9999)
            .setSubtypeNameResId(0)
            .setSubtypeLocale("")
            .setSubtypeMode("keyboard")
            .setSubtypeExtraValue(extraValue)
            .build()
    }
}
