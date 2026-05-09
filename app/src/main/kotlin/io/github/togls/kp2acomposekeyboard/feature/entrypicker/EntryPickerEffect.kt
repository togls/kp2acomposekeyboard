package io.github.togls.kp2acomposekeyboard.feature.entrypicker

sealed interface EntryPickerEffect {
    data object Finish : EntryPickerEffect
}