package io.github.togls.kp2acomposekeyboard.feature.keyboard

import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel

data class KeyboardUiState(
    val mainLayout: MainKeyboardLayout = MainKeyboardLayout.Default,
    val defaultInputMode: DefaultInputMode = DefaultInputMode.Letters,
    val entryFieldDisplayMode: EntryFieldDisplayMode = EntryFieldDisplayMode.Paged,
    val currentEntryName: String? = null,
    val hasActiveSession: Boolean = false,
    val fixedFields: List<KeyboardFieldUiModel> = emptyList(),
    val extraFields: List<KeyboardFieldUiModel> = emptyList(),
    val allFields: List<KeyboardFieldUiModel> = emptyList(),
    val extraFieldPageIndex: Int = 0,
    val extraFieldPageSize: Int = DEFAULT_EXTRA_FIELD_PAGE_SIZE,
    val isUppercase: Boolean = false,
) {
    companion object {
        // P0 需求中其余字段默认每页 3 个；集中成常量，避免后续 UI 和 ViewModel 各写一份。
        const val DEFAULT_EXTRA_FIELD_PAGE_SIZE = 3
    }
}