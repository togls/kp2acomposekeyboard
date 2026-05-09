package io.github.togls.kp2acomposekeyboard.domain

data class KeyboardFieldUiModel(
    val id: String,
    val label: String,
    val type: KeyboardFieldType,
    val sensitive: Boolean,
)