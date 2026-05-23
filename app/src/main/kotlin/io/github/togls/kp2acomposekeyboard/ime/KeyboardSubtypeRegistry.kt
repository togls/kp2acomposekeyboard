package io.github.togls.kp2acomposekeyboard.ime

import android.view.inputmethod.InputMethodSubtype
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardSubtype

object KeyboardSubtypeRegistry {
    const val ENTRY_SUBTYPE_ID = 1001
    const val ENGLISH_US_SUBTYPE_ID = 1002
    const val EXTRA_KEY_LAYOUT = "layout"
    const val ENTRY_LAYOUT_EXTRA = "layout=entry"
    const val ENGLISH_US_LAYOUT_EXTRA = "layout=english_us"

    fun fromInputMethodSubtype(subtype: InputMethodSubtype?): KeyboardSubtype {
        return when (subtype?.getExtraValueOf(EXTRA_KEY_LAYOUT)) {
            "english_us" -> KeyboardSubtype.EnglishUs
            "entry" -> KeyboardSubtype.Entry
            else -> KeyboardSubtype.Entry
        }
    }

    fun inputMethodSubtypeFor(subtype: KeyboardSubtype): InputMethodSubtype? {
        return when (subtype) {
            KeyboardSubtype.Entry -> null
            KeyboardSubtype.EnglishUs -> englishUsInputMethodSubtype()
        }
    }

    fun englishUsInputMethodSubtype(): InputMethodSubtype {
        return InputMethodSubtype.InputMethodSubtypeBuilder()
            .setSubtypeId(ENGLISH_US_SUBTYPE_ID)
            .setSubtypeNameResId(R.string.ime_subtype_en_us)
            .setSubtypeLocale("en_US")
            .setSubtypeMode("keyboard")
            .setSubtypeExtraValue(ENGLISH_US_LAYOUT_EXTRA)
            .setIsAsciiCapable(true)
            .setIsAuxiliary(false)
            .setOverridesImplicitlyEnabledSubtype(false)
            .build()
    }
}
