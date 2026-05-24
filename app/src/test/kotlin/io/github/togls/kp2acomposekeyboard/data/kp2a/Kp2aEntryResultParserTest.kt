package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.content.Intent
import keepass2android.pluginsdk.KeepassDefs
import keepass2android.pluginsdk.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Kp2aEntryResultParserTest {

    private val parser = Kp2aEntryResultParser()

    @Test
    fun parse_returnsFieldsFromEntryOutputData() {
        val intent = Intent().apply {
            putExtra(
                Strings.EXTRA_ENTRY_OUTPUT_DATA,
                """
                {
                  "${KeepassDefs.TitleField}": "GitHub",
                  "${KeepassDefs.UserNameField}": "octocat",
                  "${KeepassDefs.PasswordField}": "secret-password",
                  "TOTP": "123456"
                }
                """.trimIndent(),
            )
            putExtra(Strings.EXTRA_ENTRY_ID, "entry-1")
        }

        val result = parser.parse(intent)

        assertEquals("entry-1", result.entryId)
        assertEquals("GitHub", result.fields[KeepassDefs.TitleField])
        assertEquals("octocat", result.fields[KeepassDefs.UserNameField])
        assertEquals("secret-password", result.fields[KeepassDefs.PasswordField])
        assertEquals("123456", result.fields["TOTP"])
    }

    @Test
    fun parse_returnsProtectedFieldsFromStringArrayListExtra() {
        val intent = Intent().apply {
            putExtra(
                Strings.EXTRA_ENTRY_OUTPUT_DATA,
                """{"${KeepassDefs.UserNameField}":"octocat"}""",
            )
            putStringArrayListExtra(
                Strings.EXTRA_PROTECTED_FIELDS_LIST,
                arrayListOf(KeepassDefs.PasswordField, "TOTP"),
            )
        }

        val result = parser.parse(intent)

        assertEquals(
            setOf(KeepassDefs.PasswordField, "TOTP"),
            result.protectedFields,
        )
    }

    @Test
    fun parse_returnsEmptyFieldsForInvalidJson() {
        val intent = Intent().apply {
            putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, "{ invalid json")
        }

        val result = parser.parse(intent)

        assertTrue(result.fields.isEmpty())
    }

    @Test
    fun parse_returnsEmptyResultForNullIntent() {
        val result = parser.parse(null)

        assertTrue(result.fields.isEmpty())
        assertTrue(result.protectedFields.isEmpty())
        assertEquals(null, result.entryId)
    }
}