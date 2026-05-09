package io.github.togls.kp2acomposekeyboard.domain

import javax.inject.Inject

class SensitiveFieldPolicy @Inject constructor() {

    fun isSensitive(
        key: String,
        type: KeyboardFieldType,
        protectedFields: Set<String>,
    ): Boolean {
        if (type == KeyboardFieldType.Password ||
            type == KeyboardFieldType.Totp ||
            type == KeyboardFieldType.Recovery
        ) {
            return true
        }

        if (protectedFields.any { protectedKey ->
                protectedKey.equals(key, ignoreCase = true)
            }
        ) {
            return true
        }

        val normalizedKey = key.normalizeFieldKey()

        return sensitiveKeywords.any { keyword ->
            normalizedKey.contains(keyword)
        }
    }

    private companion object {
        val sensitiveKeywords = listOf(
            "password",
            "passwd",
            "pwd",
            "pass",
            "token",
            "accesstoken",
            "secret",
            "totp",
            "otp",
            "recovery",
            "backupcode",
            "privatekey",
            "apikey",
        )
    }
}

internal fun String.normalizeFieldKey(): String {
    return lowercase()
        .replace(" ", "")
        .replace("_", "")
        .replace("-", "")
        .replace(".", "")
}