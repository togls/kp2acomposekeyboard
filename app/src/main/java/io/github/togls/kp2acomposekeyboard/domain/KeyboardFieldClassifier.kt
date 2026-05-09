package io.github.togls.kp2acomposekeyboard.domain

import keepass2android.pluginsdk.KeepassDefs
import javax.inject.Inject

class KeyboardFieldClassifier @Inject constructor() {

    fun classify(key: String): KeyboardFieldType {
        val normalizedKey = key.normalizeFieldKey()

        return when {
            key == KeepassDefs.UserNameField -> KeyboardFieldType.Username
            key == KeepassDefs.PasswordField -> KeyboardFieldType.Password
            key == KeepassDefs.UrlField -> KeyboardFieldType.Url
            key == KeepassDefs.NotesField -> KeyboardFieldType.Notes

            normalizedKey == "username" ||
                    normalizedKey == "user" ||
                    normalizedKey == "login" ||
                    normalizedKey == "account" -> KeyboardFieldType.Username

            normalizedKey == "password" ||
                    normalizedKey == "passwd" ||
                    normalizedKey == "pwd" -> KeyboardFieldType.Password

            normalizedKey == "totp" ||
                    normalizedKey == "otp" ||
                    normalizedKey == "2fa" ||
                    normalizedKey == "mfa" -> KeyboardFieldType.Totp

            normalizedKey == "url" ||
                    normalizedKey == "uri" ||
                    normalizedKey == "website" ||
                    normalizedKey == "site" -> KeyboardFieldType.Url

            normalizedKey == "email" ||
                    normalizedKey == "mail" -> KeyboardFieldType.Email

            normalizedKey.contains("recovery") ||
                    normalizedKey.contains("backupcode") -> KeyboardFieldType.Recovery

            normalizedKey == "phone" ||
                    normalizedKey == "mobile" ||
                    normalizedKey == "tel" -> KeyboardFieldType.Phone

            normalizedKey.contains("address") -> KeyboardFieldType.Address

            normalizedKey == "notes" ||
                    normalizedKey == "note" -> KeyboardFieldType.Notes

            else -> KeyboardFieldType.Custom
        }
    }

    fun displayLabel(
        key: String,
        type: KeyboardFieldType,
    ): String {
        return when (type) {
            KeyboardFieldType.Username -> "Username"
            KeyboardFieldType.Password -> "Password"
            KeyboardFieldType.Totp -> "TOTP"
            KeyboardFieldType.Url -> "URL"
            KeyboardFieldType.Email -> "Email"
            KeyboardFieldType.Recovery -> "Recovery"
            KeyboardFieldType.Phone -> "Phone"
            KeyboardFieldType.Address -> "Address"
            KeyboardFieldType.Notes -> "Notes"
            KeyboardFieldType.Custom -> key.ifBlank { "Custom" }
        }
    }
}