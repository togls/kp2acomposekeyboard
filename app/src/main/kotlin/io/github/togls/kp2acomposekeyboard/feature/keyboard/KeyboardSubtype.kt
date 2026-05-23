package io.github.togls.kp2acomposekeyboard.feature.keyboard

enum class KeyboardSubtype(
    val mainLayout: MainKeyboardLayout,
) {
    Entry(MainKeyboardLayout.Entry),
    EnglishUs(MainKeyboardLayout.Default),
}
