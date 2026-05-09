package io.github.togls.kp2acomposekeyboard.session

import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel

data class KeyboardSessionSnapshot(
    val entryName: String?,
    val fixedFields: List<KeyboardFieldUiModel>,
    val extraFields: List<KeyboardFieldUiModel>,
    val allFields: List<KeyboardFieldUiModel>,
) {
    val hasFields: Boolean
        get() = allFields.isNotEmpty()
}