package io.github.togls.kp2acomposekeyboard.session

import io.github.togls.kp2acomposekeyboard.domain.KeyboardField
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class KeyboardSessionMappingsTest {

    @Test
    fun toSnapshot_removesFieldValues() {
        val session = KeyboardSession(
            entryId = "entry-1",
            entryName = "GitHub",
            createdAtMillis = 123L,
            fields = listOf(
                field(
                    id = "password",
                    label = "Password",
                    value = "secret-password",
                    type = KeyboardFieldType.Password,
                    sensitive = true,
                ),
            ),
        )

        val snapshot = session.toSnapshot()

        assertEquals("GitHub", snapshot.entryName)
        assertEquals(1, snapshot.fixedFields.size)
        assertEquals("password", snapshot.fixedFields.first().id)
        assertEquals("Password", snapshot.fixedFields.first().label)

        val snapshotText = snapshot.toString()
        assertFalse(snapshotText.contains("secret-password"))
    }

    @Test
    fun toSnapshot_splitsFixedAndExtraFields() {
        val session = KeyboardSession(
            entryId = "entry-1",
            entryName = "GitHub",
            createdAtMillis = 123L,
            fields = listOf(
                field("email", "Email", "a@example.com", KeyboardFieldType.Email),
                field("password", "Password", "secret", KeyboardFieldType.Password, true),
                field("username", "Username", "octocat", KeyboardFieldType.Username),
                field("totp", "TOTP", "123456", KeyboardFieldType.Totp, true),
            ),
        )

        val snapshot = session.toSnapshot()

        assertEquals(
            listOf("Username", "Password", "TOTP"),
            snapshot.fixedFields.map { field -> field.label },
        )

        assertEquals(
            listOf("Email"),
            snapshot.extraFields.map { field -> field.label },
        )
    }

    private fun field(
        id: String,
        label: String,
        value: String,
        type: KeyboardFieldType,
        sensitive: Boolean = false,
    ): KeyboardField {
        return KeyboardField(
            id = id,
            key = label,
            label = label,
            value = value,
            type = type,
            sensitive = sensitive,
        )
    }
}