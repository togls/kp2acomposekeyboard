package io.github.togls.kp2acomposekeyboard.security

import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.github.togls.kp2acomposekeyboard.BuildConfig

const val DEFAULT_TAG = "Kp2aKeyboardIme"

object DebugLog {
    private val sensitiveKeys = listOf(
        "password",
        "pass",
        "token",
        "accessToken",
        "secret",
        "totp",
        "otp",
        "credential",
    )

    fun d(
        message: String,
        vararg fields: Pair<String, Any?>,
    ) {
        if (!BuildConfig.DEBUG) {
            return
        }

        Log.d(DEFAULT_TAG, format(message, fields))
    }

    fun w(
        message: String,
        throwable: Throwable? = null,
        vararg fields: Pair<String, Any?>,
    ) {
        if (!BuildConfig.DEBUG) {
            return
        }

        if (throwable == null) {
            Log.w(DEFAULT_TAG, format(message, fields))
        } else {
            Log.w(DEFAULT_TAG, format(message, fields), throwable)
        }
    }

    fun e(
        message: String,
        throwable: Throwable? = null,
        vararg fields: Pair<String, Any?>,
    ) {
        if (!BuildConfig.DEBUG) {
            return
        }

        if (throwable == null) {
            Log.e(DEFAULT_TAG, format(message, fields))
        } else {
            Log.e(DEFAULT_TAG, format(message, fields), throwable)
        }
    }

    fun intent(
        message: String,
        intent: Intent?,
        vararg fields: Pair<String, Any?>,
    ) {
        if (!BuildConfig.DEBUG) {
            return
        }

        d(
            message = message,
            fields = arrayOf(
                "action" to intent?.action,
                "package" to intent?.`package`,
                "component" to intent?.component?.flattenToShortString(),
                "extras" to intent?.extras?.keySet()?.joinToString(),
                *fields,
            ),
        )
    }

    fun bundleKeys(
        message: String,
        bundle: Bundle?,
        vararg fields: Pair<String, Any?>,
    ) {
        if (!BuildConfig.DEBUG) {
            return
        }

        d(
            message = message,
            fields = arrayOf(
                "extras" to bundle?.keySet()?.joinToString(),
                *fields,
            ),
        )
    }

    fun entryFields(
        message: String,
        fields: Map<String, String>,
    ) {
        if (!BuildConfig.DEBUG) {
            return
        }

        val summary = fields.entries.joinToString(separator = ",") { entry ->
            val key = entry.key
            val value = entry.value

            if (isSensitiveKey(key)) {
                "$key=<redacted>"
            } else {
                "$key.length=${value.length}"
            }
        }

        d(
            message = message,
            "fieldCount" to fields.size,
            "fields" to summary,
        )
    }

    private fun format(
        message: String,
        fields: Array<out Pair<String, Any?>>,
    ): String {
        val parts = mutableListOf<String>()

        parts += "message=${render(message)}"

        fields.forEach { field ->
            val key = field.first
            val value = field.second

            parts += "$key=${renderValue(key, value)}"
        }

        return parts.joinToString(separator = " ")
    }

    private fun renderValue(
        key: String,
        value: Any?,
    ): String {
        if (isSensitiveKey(key)) {
            return "<redacted>"
        }

        return when (value) {
            null -> "null"
            is Boolean -> value.toString()
            is Number -> value.toString()
            is Throwable -> render("${value::class.java.simpleName}: ${value.message}")
            else -> render(value.toString())
        }
    }

    private fun render(value: String): String {
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")

        val shouldQuote = escaped.any { it.isWhitespace() } ||
                escaped.contains("=") ||
                escaped.contains(",")

        return if (shouldQuote) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    private fun isSensitiveKey(key: String): Boolean {
        return sensitiveKeys.any { sensitive ->
            key.contains(sensitive, ignoreCase = true)
        }
    }
}