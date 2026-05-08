package io.github.togls.kp2acomposekeyboard.session

import io.github.togls.kp2acomposekeyboard.domain.KeyboardField
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.toUiModel

fun KeyboardSession.toSnapshot(): KeyboardSessionSnapshot {
    return KeyboardSessionSnapshot(
        entryName = entryName,
        fixedFields = fields
            .filter { it.isFixedField() }
            .sortedByFixedFieldOrder()
            .map { it.toUiModel() },
        extraFields = fields
            .filterNot { it.isFixedField() }
            .map { it.toUiModel() },
        allFields = fields
            .sortedByDisplayOrder()
            .map { it.toUiModel() },
    )
}

private fun KeyboardField.isFixedField(): Boolean {
    return type == KeyboardFieldType.Username ||
            type == KeyboardFieldType.Password ||
            type == KeyboardFieldType.Totp
}

private fun List<KeyboardField>.sortedByFixedFieldOrder(): List<KeyboardField> {
    return sortedBy { field ->
        when (field.type) {
            KeyboardFieldType.Username -> 0
            KeyboardFieldType.Password -> 1
            KeyboardFieldType.Totp -> 2
            else -> 3
        }
    }
}

private fun List<KeyboardField>.sortedByDisplayOrder(): List<KeyboardField> {
    return sortedBy { field ->
        when (field.type) {
            KeyboardFieldType.Username -> 0
            KeyboardFieldType.Password -> 1
            KeyboardFieldType.Totp -> 2
            KeyboardFieldType.Url -> 3
            KeyboardFieldType.Email -> 4
            KeyboardFieldType.Recovery -> 5
            KeyboardFieldType.Phone -> 6
            KeyboardFieldType.Address -> 7
            KeyboardFieldType.Notes -> 8
            KeyboardFieldType.Custom -> 9
        }
    }
}