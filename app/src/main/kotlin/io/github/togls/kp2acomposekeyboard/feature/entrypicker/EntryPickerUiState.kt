package io.github.togls.kp2acomposekeyboard.feature.entrypicker

data class EntryPickerUiState(
    val status: EntryPickerStatus = EntryPickerStatus.Idle,
    val message: String? = null,
)

enum class EntryPickerStatus {
    Idle,
    Selecting,
    Completed,
    Failed,
    Cancelled,
}