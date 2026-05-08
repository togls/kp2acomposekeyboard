package io.github.togls.kp2acomposekeyboard.feature.keyboard

sealed interface KeyboardEffect {
    data class CommitText(val text: String) : KeyboardEffect

    data object DeleteBackward : KeyboardEffect
    data object SendEnter : KeyboardEffect

    data object LaunchEntryPicker : KeyboardEffect
    data object LaunchSettings : KeyboardEffect
}