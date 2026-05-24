package io.github.togls.kp2acomposekeyboard.kp2a

import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.policy.KeyboardFieldClassifier
import io.github.togls.kp2acomposekeyboard.domain.policy.SensitiveFieldPolicy
import keepass2android.pluginsdk.KeepassDefs
import keepass2android.pluginsdk.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Kp2aEntryMapperTest {

    private val mapper = Kp2aEntryMapper(
        fieldClassifier = KeyboardFieldClassifier(),
        sensitiveFieldPolicy = SensitiveFieldPolicy(),
    )

    @Test
    fun mapToSession_mapsTitleToEntryNameAndDoesNotCreateTitleField() {
        val result = Kp2aEntryResult(
            entryId = "entry-1",
            protectedFields = emptySet(),
            fields = mapOf(
                KeepassDefs.TitleField to "GitHub",
                KeepassDefs.UserNameField to "octocat",
            ),
        )

        val session = mapper.mapToSession(result)

        assertNotNull(session)
        requireNotNull(session)

        assertEquals("entry-1", session.entryId)
        assertEquals("GitHub", session.entryName)
        assertEquals(1, session.fields.size)
        assertEquals(KeyboardFieldType.Username, session.fields.first().type)
    }

    @Test
    fun mapToSession_filtersBinaryAndEmptyFields() {
        val result = Kp2aEntryResult(
            entryId = "entry-1",
            protectedFields = emptySet(),
            fields = mapOf(
                KeepassDefs.TitleField to "GitHub",
                "${Strings.PREFIX_BINARY}Attachment" to "binary-data",
                "Empty" to "",
                KeepassDefs.UserNameField to "octocat",
            ),
        )

        val session = mapper.mapToSession(result)

        assertNotNull(session)
        requireNotNull(session)

        assertEquals(1, session.fields.size)
        assertEquals("octocat", session.fields.first().value)
    }

    @Test
    fun mapToSession_marksSensitiveFields() {
        val result = Kp2aEntryResult(
            entryId = "entry-1",
            protectedFields = setOf("ApiKey"),
            fields = mapOf(
                KeepassDefs.PasswordField to "secret-password",
                "TOTP" to "123456",
                "ApiKey" to "secret-api-key",
            ),
        )

        val session = mapper.mapToSession(result)

        assertNotNull(session)
        requireNotNull(session)

        val password = session.fields.first { field ->
            field.type == KeyboardFieldType.Password
        }
        val totp = session.fields.first { field ->
            field.type == KeyboardFieldType.Totp
        }
        val apiKey = session.fields.first { field ->
            field.key == "ApiKey"
        }

        assertTrue(password.sensitive)
        assertTrue(totp.sensitive)
        assertTrue(apiKey.sensitive)
    }

    @Test
    fun mapToSession_returnsNullWhenNoInputFieldsRemain() {
        val result = Kp2aEntryResult(
            entryId = "entry-1",
            protectedFields = emptySet(),
            fields = mapOf(
                KeepassDefs.TitleField to "Only Title",
                "Empty" to "",
            ),
        )

        val session = mapper.mapToSession(result)

        assertEquals(null, session)
    }
}
