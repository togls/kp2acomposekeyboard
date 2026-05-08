package io.github.togls.kp2acomposekeyboard.domain

data class KeyboardField(
    val id: String,
    val key: String,
    val label: String,
    val value: String,
    val type: KeyboardFieldType,
    val sensitive: Boolean,
)