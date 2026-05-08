package io.github.togls.kp2acomposekeyboard.ime

import android.view.KeyEvent
import android.view.inputmethod.InputConnection

class InputConnectionDispatcher(
    private val inputConnectionProvider: () -> InputConnection?,
) {

    fun commitText(text: String) {
        if (text.isEmpty()) {
            return
        }

        // text 后续可能是密码、TOTP 或恢复码，所以这里不能写日志。
        inputConnectionProvider()?.commitText(text, 1)
    }

    fun deleteBackward() {
        inputConnectionProvider()?.deleteSurroundingText(1, 0)
    }

    fun sendEnter() {
        val inputConnection = inputConnectionProvider() ?: return

        // 部分输入目标只处理完整的按下/抬起事件对，单独发送 ACTION_DOWN 兼容性较差。
        inputConnection.sendKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER),
        )
        inputConnection.sendKeyEvent(
            KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER),
        )
    }
}