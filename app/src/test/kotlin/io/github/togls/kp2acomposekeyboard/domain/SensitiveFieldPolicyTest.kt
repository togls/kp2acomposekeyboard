package io.github.togls.kp2acomposekeyboard.domain

import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.policy.SensitiveFieldPolicy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SensitiveFieldPolicyTest {

    private val policy = SensitiveFieldPolicy()

    @Test
    fun isSensitive_returnsTrueForSensitiveTypes() {
        assertTrue(
            policy.isSensitive(
                key = "Password",
                type = KeyboardFieldType.Password,
                protectedFields = emptySet(),
            ),
        )

        assertTrue(
            policy.isSensitive(
                key = "TOTP",
                type = KeyboardFieldType.Totp,
                protectedFields = emptySet(),
            ),
        )

        assertTrue(
            policy.isSensitive(
                key = "Recovery Code",
                type = KeyboardFieldType.Recovery,
                protectedFields = emptySet(),
            ),
        )
    }

    @Test
    fun isSensitive_returnsTrueForProtectedFields() {
        assertTrue(
            policy.isSensitive(
                key = "ApiKey",
                type = KeyboardFieldType.Custom,
                protectedFields = setOf("ApiKey"),
            ),
        )
    }

    @Test
    fun isSensitive_returnsTrueForSensitiveKeywords() {
        assertTrue(
            policy.isSensitive(
                key = "access_token",
                type = KeyboardFieldType.Custom,
                protectedFields = emptySet(),
            ),
        )

        assertTrue(
            policy.isSensitive(
                key = "private-key",
                type = KeyboardFieldType.Custom,
                protectedFields = emptySet(),
            ),
        )
    }

    @Test
    fun isSensitive_returnsFalseForNormalFields() {
        assertFalse(
            policy.isSensitive(
                key = "Email",
                type = KeyboardFieldType.Email,
                protectedFields = emptySet(),
            ),
        )
    }
}
