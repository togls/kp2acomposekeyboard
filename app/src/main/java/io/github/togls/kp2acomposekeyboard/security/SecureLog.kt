package io.github.togls.kp2acomposekeyboard.security

import android.util.Log
import io.github.togls.kp2acomposekeyboard.BuildConfig

/**
 * 这里 SecureLog 只接收 SecureLogEvent，不提供 debug(message: String) 这种自由字符串入口。
 * 原因是自由字符串很容易被临时拼接成：
 *
 * ```
 * Log.d(TAG, "value=$value")
 * ```
 *
 * 而 SecureLogEvent 可以把可打印内容收敛到固定白名单。
 */
object SecureLog {

    private const val DEFAULT_TAG = "Kp2aKeyboard"

    fun debug(
        event: SecureLogEvent,
        tag: String = DEFAULT_TAG,
    ) {
        if (!BuildConfig.DEBUG) {
            return
        }

        Log.d(tag, event.message)
    }

    fun info(
        event: SecureLogEvent,
        tag: String = DEFAULT_TAG,
    ) {
        Log.i(tag, event.message)
    }

    fun warn(
        event: SecureLogEvent,
        tag: String = DEFAULT_TAG,
        throwable: Throwable? = null,
    ) {
        Log.w(tag, event.message, throwable)
    }
}