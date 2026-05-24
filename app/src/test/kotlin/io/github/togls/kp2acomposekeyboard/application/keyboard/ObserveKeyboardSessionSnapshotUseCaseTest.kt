package io.github.togls.kp2acomposekeyboard.application.keyboard

import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardField
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ObserveKeyboardSessionSnapshotUseCaseTest {

    @Test
    fun invoke_emitsNullWhenNoSessionExists() = runTest {
        val useCase = ObserveKeyboardSessionSnapshotUseCase(KeyboardSessionRepository())

        assertNull(useCase().first())
    }

    @Test
    fun invoke_emitsSafeSnapshotWithoutFieldValue() = runTest {
        val repository = KeyboardSessionRepository()
        repository.setSession(
            KeyboardSession(
                entryId = "entry-1",
                entryName = "GitHub",
                fields = listOf(
                    KeyboardField(
                        id = "password",
                        key = "Password",
                        label = "Password",
                        value = "field-value-for-snapshot",
                        type = KeyboardFieldType.Password,
                        sensitive = true,
                    ),
                ),
                createdAtMillis = 123L,
            ),
        )
        val useCase = ObserveKeyboardSessionSnapshotUseCase(repository)

        val snapshot = useCase().first()

        assertEquals("GitHub", snapshot?.entryName)
        assertEquals("password", snapshot?.fixedFields?.first()?.id)
        assertFalse(snapshot.toString().contains("field-value-for-snapshot"))
    }
}
