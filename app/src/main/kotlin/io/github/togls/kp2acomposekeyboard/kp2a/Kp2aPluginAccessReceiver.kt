package io.github.togls.kp2acomposekeyboard.kp2a

import io.github.togls.kp2acomposekeyboard.security.SecureLog
import keepass2android.pluginsdk.PluginAccessBroadcastReceiver
import keepass2android.pluginsdk.Strings

class Kp2aPluginAccessReceiver : PluginAccessBroadcastReceiver() {

    override fun getScopes(): ArrayList<String> {
        SecureLog.d(
            message = "kp2a access scopes requested",
            "scopeCount" to REQUIRED_SCOPES.size,
        )

        return ArrayList(REQUIRED_SCOPES)
    }

    companion object {
        val REQUIRED_SCOPES = listOf(
            Strings.SCOPE_CURRENT_ENTRY,
            Strings.SCOPE_QUERY_CREDENTIALS,
        )
    }
}