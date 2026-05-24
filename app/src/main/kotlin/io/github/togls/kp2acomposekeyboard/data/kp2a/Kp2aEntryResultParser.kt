package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.content.Intent
import keepass2android.pluginsdk.Strings
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class Kp2aEntryResultParser @Inject constructor() {

    fun parse(data: Intent?): Kp2aEntryResult {
        return Kp2aEntryResult(
            fields = parseEntryFields(data),
            protectedFields = parseProtectedFields(data),
            entryId = data?.getStringExtra(Strings.EXTRA_ENTRY_ID),
        )
    }

    private fun parseEntryFields(data: Intent?): Map<String, String> {
        val rawJson = data?.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA)
            ?: return emptyMap()

        if (rawJson.isBlank()) {
            return emptyMap()
        }

        return try {
            val json = JSONObject(rawJson)

            buildMap {
                val keys = json.keys()

                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = json.optString(key)

                    if (key.isNotBlank()) {
                        put(key, value)
                    }
                }
            }
        } catch (_: JSONException) {
            // KP2A output may contain passwords; never include raw JSON in parse-failure logs.
            emptyMap()
        }
    }

    private fun parseProtectedFields(data: Intent?): Set<String> {
        return when (val extra = protectedFieldsExtra(data)) {
            is ArrayList<*> -> extra.parseProtectedFieldList()
            is String -> parseProtectedFieldJson(extra)
            else -> emptySet()
        }
    }

    @Suppress("DEPRECATION")
    private fun protectedFieldsExtra(data: Intent?): Any? {
        // KP2A versions may send this value as either ArrayList<String> or JSON string.
        // Reading the raw extra avoids Android's typed getter cast warning for mixed formats.
        return data?.extras?.get(Strings.EXTRA_PROTECTED_FIELDS_LIST)
    }

    private fun ArrayList<*>.parseProtectedFieldList(): Set<String> {
        return filterIsInstance<String>()
            .filter { fieldName -> fieldName.isNotBlank() }
            .toSet()
    }

    private fun parseProtectedFieldJson(rawJson: String): Set<String> {
        if (rawJson.isBlank()) {
            return emptySet()
        }

        return try {
            val json = JSONArray(rawJson)

            buildSet {
                for (index in 0 until json.length()) {
                    val fieldName = json.optString(index)

                    if (fieldName.isNotBlank()) {
                        add(fieldName)
                    }
                }
            }
        } catch (_: JSONException) {
            // Protected-field metadata only affects safety labels, so malformed data can degrade to empty.
            emptySet()
        }
    }
}
