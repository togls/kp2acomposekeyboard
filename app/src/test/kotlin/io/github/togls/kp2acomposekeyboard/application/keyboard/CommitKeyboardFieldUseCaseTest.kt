package io.github.togls.kp2acomposekeyboard.application.keyboard

import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardField
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CommitKeyboardFieldUseCaseTest {

    @Test
    fun blankFieldId_returnsIgnoredReason() {
        val useCase = CommitKeyboardFieldUseCase(KeyboardSessionRepository())

        val result = useCase(" ")

        assertEquals(CommitFieldResult.Ignored(CommitFieldIgnoredReason.BlankFieldId), result)
    }

    @Test
    fun missingField_returnsIgnoredReason() {
        val repository = KeyboardSessionRepository()
        repository.setSession(session(field("username", "octocat")))
        val useCase = CommitKeyboardFieldUseCase(repository)

        val result = useCase("password")

        assertEquals(CommitFieldResult.Ignored(CommitFieldIgnoredReason.FieldNotFound), result)
    }

    @Test
    fun emptyFieldValue_returnsIgnoredReason() {
        val repository = KeyboardSessionRepository()
        repository.setSession(session(field("username", "")))
        val useCase = CommitKeyboardFieldUseCase(repository)

        val result = useCase("username")

        assertEquals(CommitFieldResult.Ignored(CommitFieldIgnoredReason.EmptyValue), result)
    }

    @Test
    fun existingFieldValue_returnsCommitWithoutChangingText() {
        val repository = KeyboardSessionRepository()
        repository.setSession(session(field("password", "field-value-for-commit")))
        val useCase = CommitKeyboardFieldUseCase(repository)

        val result = useCase("password")

        assertEquals(CommitFieldResult.Commit("field-value-for-commit"), result)
    }

    private fun session(field: KeyboardField): KeyboardSession {
        return KeyboardSession(
            entryId = "entry-1",
            entryName = "GitHub",
            fields = listOf(field),
            createdAtMillis = 123L,
        )
    }

    private fun field(
        id: String,
        value: String,
    ): KeyboardField {
        return KeyboardField(
            id = id,
            key = id,
            label = id,
            value = value,
            type = KeyboardFieldType.Custom,
            sensitive = true,
        )
    }
}
