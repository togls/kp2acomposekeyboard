package io.github.togls.kp2acomposekeyboard.application.keyboard

import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import javax.inject.Inject

class CommitKeyboardFieldUseCase @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
) {

    operator fun invoke(fieldId: String): CommitFieldResult {
        if (fieldId.isBlank()) {
            return CommitFieldResult.Ignored(CommitFieldIgnoredReason.BlankFieldId)
        }

        val value = sessionRepository.getFieldValue(fieldId)
            ?: return CommitFieldResult.Ignored(CommitFieldIgnoredReason.FieldNotFound)

        if (value.isEmpty()) {
            return CommitFieldResult.Ignored(CommitFieldIgnoredReason.EmptyValue)
        }

        return CommitFieldResult.Commit(value)
    }
}
