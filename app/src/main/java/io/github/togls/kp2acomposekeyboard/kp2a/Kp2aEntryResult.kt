package io.github.togls.kp2acomposekeyboard.kp2a

data class Kp2aEntryResult(
    val fields: Map<String, String>,
    val protectedFields: Set<String>,
    val entryId: String?,
) {
    val isEmpty: Boolean
        get() = fields.isEmpty()
}