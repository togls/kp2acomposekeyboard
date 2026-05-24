package io.github.togls.kp2acomposekeyboard.data.session

import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSessionSnapshot

import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardField
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.field.toSummary

fun KeyboardSession.toSnapshot(): KeyboardSessionSnapshot {
    val fixedFields = fields
        .filter { field -> field.isFixedField() }
        .sortedByFixedFieldOrder()
        .map { field -> field.toSummary() }

    val extraFields = fields
        .filterNot { field -> field.isFixedField() }
        .map { field -> field.toSummary() }

    val allFields = fields
        .sortedByDisplayOrder()
        .map { field -> field.toSummary() }

    return KeyboardSessionSnapshot(
        entryName = entryName,
        fixedFields = fixedFields,
        extraFields = extraFields,
        allFields = allFields,
    )
}

private fun KeyboardField.isFixedField(): Boolean {
    return when (type) {
        KeyboardFieldType.Username,
        KeyboardFieldType.Password,
        KeyboardFieldType.Totp,
            -> true

        KeyboardFieldType.Url,
        KeyboardFieldType.Email,
        KeyboardFieldType.Recovery,
        KeyboardFieldType.Phone,
        KeyboardFieldType.Address,
        KeyboardFieldType.Notes,
        KeyboardFieldType.Custom,
            -> false
    }
}

private fun List<KeyboardField>.sortedByFixedFieldOrder(): List<KeyboardField> {
    return sortedWith(
        compareBy { field ->
            when (field.type) {
                KeyboardFieldType.Username -> 0
                KeyboardFieldType.Password -> 1
                KeyboardFieldType.Totp -> 2
                else -> 3
            }
        },
    )
}

private fun List<KeyboardField>.sortedByDisplayOrder(): List<KeyboardField> {
    return sortedWith(
        compareBy { field ->
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
        },
    )
}