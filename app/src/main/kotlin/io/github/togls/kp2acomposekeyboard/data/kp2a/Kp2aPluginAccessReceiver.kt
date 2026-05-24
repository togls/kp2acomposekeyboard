package io.github.togls.kp2acomposekeyboard.data.kp2a

import io.github.togls.kp2acomposekeyboard.security.SecureLog
import keepass2android.pluginsdk.PluginAccessBroadcastReceiver

class Kp2aPluginAccessReceiver : PluginAccessBroadcastReceiver() {

    override fun getScopes(): ArrayList<String> {
        SecureLog.d(
            message = "kp2a access scopes requested",
            "scopeCount" to REQUIRED_SCOPES.size,
        )

        return Kp2aPluginScopes.requiredScopesForAccessManager()
    }

    companion object {
        val REQUIRED_SCOPES = Kp2aPluginScopes.REQUIRED_SCOPES
    }
}
