package io.github.togls.kp2acomposekeyboard.feature.entrypicker

import io.github.togls.kp2acomposekeyboard.data.kp2a.Kp2aEntryResult

sealed interface EntryPickerIntent {
    data object StartSelection : EntryPickerIntent
    data object Retry : EntryPickerIntent
    data object Cancel : EntryPickerIntent

    data class Kp2aResultSucceeded(
        val result: Kp2aEntryResult,
    ) : EntryPickerIntent

    data class Kp2aEntrySelected(
        val fields: Map<String, String>
    ) : EntryPickerIntent

    data object Kp2aResultCancelled : EntryPickerIntent
    data object Kp2aResultFailed : EntryPickerIntent
    data object Kp2aLaunchFailed : EntryPickerIntent
}