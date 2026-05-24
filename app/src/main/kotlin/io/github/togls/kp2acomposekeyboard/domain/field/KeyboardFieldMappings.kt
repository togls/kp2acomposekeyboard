package io.github.togls.kp2acomposekeyboard.domain.field

fun KeyboardField.toSummary(): KeyboardFieldSummary {
    return KeyboardFieldSummary(
        id = id,
        label = label,
        type = type,
        sensitive = sensitive,
    )
}
