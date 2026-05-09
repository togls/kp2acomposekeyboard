package io.github.togls.kp2acomposekeyboard.domain

import keepass2android.pluginsdk.KeepassDefs
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardFieldClassifierTest {

    private val classifier = KeyboardFieldClassifier()

    @Test
    fun classify_returnsStandardKeepassFieldTypes() {
        assertEquals(
            KeyboardFieldType.Username,
            classifier.classify(KeepassDefs.UserNameField),
        )

        assertEquals(
            KeyboardFieldType.Password,
            classifier.classify(KeepassDefs.PasswordField),
        )

        assertEquals(
            KeyboardFieldType.Url,
            classifier.classify(KeepassDefs.UrlField),
        )

        assertEquals(
            KeyboardFieldType.Notes,
            classifier.classify(KeepassDefs.NotesField),
        )
    }

    @Test
    fun classify_returnsCustomTypesByCommonNames() {
        assertEquals(KeyboardFieldType.Totp, classifier.classify("TOTP"))
        assertEquals(KeyboardFieldType.Email, classifier.classify("email"))
        assertEquals(KeyboardFieldType.Recovery, classifier.classify("Recovery Code"))
        assertEquals(KeyboardFieldType.Phone, classifier.classify("mobile"))
        assertEquals(KeyboardFieldType.Address, classifier.classify("address_line_1"))
    }

    @Test
    fun displayLabel_returnsStableLabelsForKnownTypes() {
        assertEquals(
            "Username",
            classifier.displayLabel("UserName", KeyboardFieldType.Username),
        )

        assertEquals(
            "Password",
            classifier.displayLabel("Password", KeyboardFieldType.Password),
        )

        assertEquals(
            "TOTP",
            classifier.displayLabel("otp", KeyboardFieldType.Totp),
        )

        assertEquals(
            "CustomField",
            classifier.displayLabel("CustomField", KeyboardFieldType.Custom),
        )
    }
}