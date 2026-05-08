package io.github.togls.kp2acomposekeyboard.kp2a

import android.app.Activity
import android.content.Intent
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * 这里先采用 ACTION_QUERY_CREDENTIALS_FOR_OWN_PACKAGE 作为可编译、可隔离的查询入口。
 *
 * 开源样例中的 Kp2aControl.getQueryEntryForOwnPackageIntent() 就是返回这个 action 的 Intent，字段结果通过 EXTRA_ENTRY_OUTPUT_DATA JSON 解析。
 */
object Kp2aContract {

    object Packages {
        const val KEEPASS2ANDROID = "keepass2android.keepass2android"
        const val KEEPASS2ANDROID_OFFLINE = "keepass2android.keepass2android_nonet"
    }

    object Actions {
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

        const val START_WITH_TASK =
            "keepass2android.ACTION_START_WITH_TASK"
    }

    object Extras {
        const val ENTRY_OUTPUT_DATA =
            "keepass2android.EXTRA_ENTRY_OUTPUT_DATA"

        const val PROTECTED_FIELDS_LIST =
            "keepass2android.EXTRA_PROTECTED_FIELDS_LIST"

        const val ENTRY_ID =
            "keepass2android.EXTRA_ENTRY_ID"

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
        const val DATABASE_ACTIONS =
            "keepass2android.SCOPE_DATABASE_ACTIONS"

        const val CURRENT_ENTRY =
            "keepass2android.SCOPE_CURRENT_ENTRY"

        const val QUERY_CREDENTIALS =
            "keepass2android.SCOPE_QUERY_CREDENTIALS"

        const val QUERY_CREDENTIALS_FOR_OWN_PACKAGE =
            "keepass2android.SCOPE_QUERY_CREDENTIALS_FOR_OWN_PACKAGE"
    }

    object FieldNames {
        const val TITLE = "Title"
        const val USERNAME = "UserName"
        const val PASSWORD = "Password"
        const val URL = "URL"
        const val NOTES = "Notes"
        const val TOTP = "TOTP"
        const val OTP = "otp"
    }

    fun createQueryOwnPackageIntent(): Intent {
        return Intent(Actions.QUERY_CREDENTIALS_FOR_OWN_PACKAGE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
    }

    fun isSuccessfulResult(
        resultCode: Int,
        data: Intent?,
    ): Boolean {
        return resultCode == Activity.RESULT_OK &&
                data?.getStringExtra(Extras.ENTRY_OUTPUT_DATA).isNullOrBlank().not()
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
            // KP2A 返回内容如果不是合法 JSON，只能视为选择失败；不能把原始内容写日志，里面可能包含敏感字段。
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
            // 受保护字段列表解析失败时，不影响基础字段读取；不要记录原始 JSON。
            emptySet()
        }
    }

    fun defaultScopes(): ArrayList<String> {
        return arrayListOf(
            Scopes.CURRENT_ENTRY,
            Scopes.QUERY_CREDENTIALS,
            Scopes.QUERY_CREDENTIALS_FOR_OWN_PACKAGE,
        )
    }
}