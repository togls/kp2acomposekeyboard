package io.github.togls.kp2acomposekeyboard.domain.session

import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardField

data class KeyboardSession(
    val entryId: String?,
    val entryName: String?,
    val fields: List<KeyboardField>,
    val createdAtMillis: Long,
)
