package io.github.togls.kp2acomposekeyboard.domain

fun KeyboardField.toUiModel(): KeyboardFieldUiModel {
    return KeyboardFieldUiModel(
        id = id,
        label = label,
        type = type,
        sensitive = sensitive,
    )
}