package io.github.togls.kp2acomposekeyboard.data.kp2a

data class Kp2aEntryResult(
    val fields: Map<String, String>,
    val protectedFields: Set<String>,
    val entryId: String?,
) {
    val isEmpty: Boolean
        get() = fields.isEmpty()
}