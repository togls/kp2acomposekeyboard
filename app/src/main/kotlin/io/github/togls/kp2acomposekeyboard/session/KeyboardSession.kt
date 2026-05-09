package io.github.togls.kp2acomposekeyboard.session

import io.github.togls.kp2acomposekeyboard.domain.KeyboardField

data class KeyboardSession(
    val entryId: String?,
    val entryName: String?,
    val fields: List<KeyboardField>,
    val createdAtMillis: Long,
)