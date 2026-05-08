package io.github.togls.kp2acomposekeyboard.kp2a

import android.content.Context
import org.json.JSONArray
import androidx.core.content.edit

class Kp2aAccessTokenStore(
    context: Context,
) {

    private val appContext = context.applicationContext

    fun saveAccess(
        hostPackage: String,
        accessToken: String,
        scopes: List<String>,
    ) {
        val prefs = appContext.getSharedPreferences(
            "kp2a_plugin_access_$hostPackage",
            Context.MODE_PRIVATE,
        )

        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_SCOPES, JSONArray(scopes).toString())
        }
    }

    fun clearAccess(hostPackage: String) {
        appContext.getSharedPreferences(
            "kp2a_plugin_access_$hostPackage",
            Context.MODE_PRIVATE,
        ).edit { clear() }
    }

    fun getAccessToken(hostPackage: String): String? {
        return appContext.getSharedPreferences(
            "kp2a_plugin_access_$hostPackage",
            Context.MODE_PRIVATE,
        ).getString(KEY_ACCESS_TOKEN, null)
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_SCOPES = "scopes"
    }
}