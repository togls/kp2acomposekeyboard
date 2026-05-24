package io.github.togls.kp2acomposekeyboard.domain.session

import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldSummary

data class KeyboardSessionSnapshot(
    val entryName: String?,
    val fixedFields: List<KeyboardFieldSummary>,
    val extraFields: List<KeyboardFieldSummary>,
    val allFields: List<KeyboardFieldSummary>,
) {
    val hasFields: Boolean
        get() = allFields.isNotEmpty()
}
