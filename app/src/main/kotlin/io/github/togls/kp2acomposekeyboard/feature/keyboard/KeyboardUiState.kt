package io.github.togls.kp2acomposekeyboard.feature.keyboard

import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel

/**
 * Represents the complete UI state required to render the keyboard.
 */
data class KeyboardUiState(
    val mainLayout: MainKeyboardLayout = MainKeyboardLayout.Default,
    val defaultInputMode: DefaultInputMode = DefaultInputMode.Letters,
    val entryFieldDisplayMode: EntryFieldDisplayMode = EntryFieldDisplayMode.Paged,
    val currentEntryName: String? = null,

    /**
     * Whether the keyboard is currently bound to an active entry session.
     */
    val hasActiveSession: Boolean = false,

    /**
     * Fields that are always visible in the entry keyboard layout.
     */
    val fixedFields: List<KeyboardFieldUiModel> = emptyList(),

    /**
     * Additional fields that may be displayed by page or other display modes.
     */
    val extraFields: List<KeyboardFieldUiModel> = emptyList(),

    /**
     * All available fields, including both fixed and extra fields.
     */
    val allFields: List<KeyboardFieldUiModel> = emptyList(),

    val extraFieldPageIndex: Int = 0,
    val extraFieldPageSize: Int = DEFAULT_EXTRA_FIELD_PAGE_SIZE,
    val isUppercase: Boolean = false,
    val utilitySlots: KeyboardUtilitySlots = KeyboardUtilitySlots(),
    val isUtilityPanelExpanded: Boolean = false,
) {
    companion object {
        /**
         * Default number of extra fields shown per page.
         *
         * P0 requires the remaining fields to be displayed three per page.
         * Keeping this value centralized avoids duplicating the same constant
         * across the UI and ViewModel layers.
         */
        const val DEFAULT_EXTRA_FIELD_PAGE_SIZE = 3
    }
}
