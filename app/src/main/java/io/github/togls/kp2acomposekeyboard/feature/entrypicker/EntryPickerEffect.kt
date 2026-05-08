package io.github.togls.kp2acomposekeyboard.feature.entrypicker

sealed interface EntryPickerEffect {
    data object LaunchKp2a : EntryPickerEffect
    data object Finish : EntryPickerEffect
}