package io.github.togls.kp2acomposekeyboard.application.keyboard

sealed interface CommitFieldResult {
    data class Commit(val text: String) : CommitFieldResult
    data class Ignored(val reason: CommitFieldIgnoredReason) : CommitFieldResult
}

enum class CommitFieldIgnoredReason {
    BlankFieldId,
    FieldNotFound,
    EmptyValue,
}
