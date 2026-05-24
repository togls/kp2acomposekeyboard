package io.github.togls.kp2acomposekeyboard.domain.field

data class KeyboardFieldSummary(
    val id: String,
    val label: String,
    val type: KeyboardFieldType,
    val sensitive: Boolean,
)
