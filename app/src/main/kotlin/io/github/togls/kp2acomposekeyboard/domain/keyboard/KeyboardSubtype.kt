package io.github.togls.kp2acomposekeyboard.domain.keyboard

enum class KeyboardSubtype(
    val mainLayout: MainKeyboardLayout,
) {
    Entry(MainKeyboardLayout.Entry),
    EnglishUs(MainKeyboardLayout.Default),
}
