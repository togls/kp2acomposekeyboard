package io.github.togls.kp2acomposekeyboard.feature.entrypicker

data class EntryPickerUiState(
    val status: EntryPickerStatus = EntryPickerStatus.Selecting,
    val message: String? = null,
)

enum class EntryPickerStatus {
    Selecting,
    Failed,
    Cancelled,
}