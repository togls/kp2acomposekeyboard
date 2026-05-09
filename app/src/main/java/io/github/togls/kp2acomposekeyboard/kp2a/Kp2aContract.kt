package io.github.togls.kp2acomposekeyboard.kp2a

import android.app.Activity
import android.content.Intent
import keepass2android.pluginsdk.Kp2aControl
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object Kp2aContract {

    object Actions {
        const val QUERY_CREDENTIALS =
            "keepass2android.ACTION_QUERY_CREDENTIALS"

        const val QUERY_CREDENTIALS_FOR_OWN_PACKAGE =
            "keepass2android.ACTION_QUERY_CREDENTIALS_FOR_OWN_PACKAGE"

        const val TRIGGER_REQUEST_ACCESS =
            "keepass2android.ACTION_TRIGGER_REQUEST_ACCESS"

        const val REQUEST_ACCESS =
            "keepass2android.ACTION_REQUEST_ACCESS"

        const val RECEIVE_ACCESS =
            "keepass2android.ACTION_RECEIVE_ACCESS"

        const val REVOKE_ACCESS =
            "keepass2android.ACTION_REVOKE_ACCESS"
    }

    object Extras {
        const val QUERY_STRING =
            "keepass2android.EXTRA_QUERY_STRING"

        const val ENTRY_OUTPUT_DATA =
            "keepass2android.EXTRA_ENTRY_OUTPUT_DATA"

        const val PROTECTED_FIELDS_LIST =
            "keepass2android.EXTRA_PROTECTED_FIELDS_LIST"

        const val ENTRY_ID =
            "keepass2android.EXTRA_ENTRY_DATA"

        const val SCOPES =
            "keepass2android.EXTRA_SCOPES"

        const val PLUGIN_PACKAGE =
            "keepass2android.EXTRA_PLUGIN_PACKAGE"

        const val SENDER =
            "keepass2android.EXTRA_SENDER"

        const val REQUEST_TOKEN =
            "keepass2android.EXTRA_REQUEST_TOKEN"

        const val ACCESS_TOKEN =
            "keepass2android.EXTRA_ACCESS_TOKEN"
    }

    object Scopes {
        const val QUERY_CREDENTIALS =
            "keepass2android.SCOPE_QUERY_CREDENTIALS"

        const val QUERY_CREDENTIALS_FOR_OWN_PACKAGE =
            "keepass2android.SCOPE_QUERY_CREDENTIALS_FOR_OWN_PACKAGE"
    }

    fun createQueryEntryIntent(searchText: String?): Intent {
        return Kp2aControl.getQueryEntryIntent(searchText)
    }

    fun createQueryOwnPackageIntent(): Intent {
        return Intent(Actions.QUERY_CREDENTIALS_FOR_OWN_PACKAGE)
    }

    fun isSuccessfulResult(
        resultCode: Int,
        data: Intent?,
    ): Boolean {
        return resultCode == Activity.RESULT_OK &&
                !data?.getStringExtra(Extras.ENTRY_OUTPUT_DATA).isNullOrBlank()
    }

    fun parseEntryResult(data: Intent?): Kp2aEntryResult {
        return Kp2aEntryResult(
            fields = parseEntryFields(data),
            protectedFields = parseProtectedFields(data),
            entryId = data?.getStringExtra(Extras.ENTRY_ID),
        )
    }

    fun parseEntryFields(data: Intent?): Map<String, String> {
        val rawJson = data?.getStringExtra(Extras.ENTRY_OUTPUT_DATA)
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
            // KP2A 返回内容可能包含密码字段；解析失败时不要打印原始 JSON。
            emptyMap()
        }
    }

    fun parseProtectedFields(data: Intent?): Set<String> {
        val rawJson = data?.getStringExtra(Extras.PROTECTED_FIELDS_LIST)
            ?: return emptySet()

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
            emptySet()
        }
    }

    fun appQuery(packageName: String?): String {
        val safePackageName = packageName?.trim().orEmpty()

        if (safePackageName.isEmpty()) {
            return ""
        }

        return "androidapp://$safePackageName"
    }
}