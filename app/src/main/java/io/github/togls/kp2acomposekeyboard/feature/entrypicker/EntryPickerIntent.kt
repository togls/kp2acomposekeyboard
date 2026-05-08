package io.github.togls.kp2acomposekeyboard.feature.entrypicker

sealed interface EntryPickerIntent {
    data object Retry : EntryPickerIntent
    data object Cancel : EntryPickerIntent
}