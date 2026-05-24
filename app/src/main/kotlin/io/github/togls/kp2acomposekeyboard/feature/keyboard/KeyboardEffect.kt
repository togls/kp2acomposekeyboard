package io.github.togls.kp2acomposekeyboard.feature.keyboard

import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardSubtype

sealed interface KeyboardEffect {
    data class CommitText(val text: String) : KeyboardEffect

    data object DeleteBackward : KeyboardEffect
    data object SendEnter : KeyboardEffect

    data class LaunchEntryPicker(
        val targetPackageName: String?,
    ) : KeyboardEffect

    data object LaunchSettings : KeyboardEffect
    data class SwitchToSubtype(val subtype: KeyboardSubtype) : KeyboardEffect
    data object SwitchToNextInputMethod : KeyboardEffect
}
