package io.github.togls.kp2acomposekeyboard.platform.input

import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import io.github.togls.kp2acomposekeyboard.security.SecureLog

class InputConnectionDispatcher(
    private val inputConnectionProvider: () -> InputConnection?,
) {

    fun commitText(text: String) {
        if (text.isEmpty()) {
            return
        }

        // Text may contain passwords, TOTP codes, or recovery codes, so never log the value.
        SecureLog.d("Text commit requested")
        // commitText avoids clipboard exposure and lets the target editor own the inserted text.
        inputConnectionProvider()?.commitText(text, 1)
    }

    fun deleteBackward() {
        SecureLog.d("Delete backward requested")
        inputConnectionProvider()?.deleteSurroundingText(1, 0)
    }

    fun sendEnter() {
        val inputConnection = inputConnectionProvider() ?: return

        SecureLog.d("Enter requested")

        // Some editors only handle a complete down/up pair for enter key events.
        inputConnection.sendKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER),
        )
        inputConnection.sendKeyEvent(
            KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER),
        )
    }
}
