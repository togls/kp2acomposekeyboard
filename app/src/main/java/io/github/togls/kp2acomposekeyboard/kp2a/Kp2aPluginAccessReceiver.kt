package io.github.togls.kp2acomposekeyboard.kp2a

import android.content.Context
import android.content.Intent
import io.github.togls.kp2acomposekeyboard.security.DebugLog
import keepass2android.pluginsdk.PluginAccessBroadcastReceiver
import keepass2android.pluginsdk.Strings
import org.json.JSONArray

class Kp2aPluginAccessReceiver : PluginAccessBroadcastReceiver() {

    override fun getScopes(): ArrayList<String> {
        DebugLog.d("kp2a: AccessReceiver getScopes called")

        return arrayListOf(
            Strings.SCOPE_CURRENT_ENTRY,
            Strings.SCOPE_QUERY_CREDENTIALS_FOR_OWN_PACKAGE,
            Strings.SCOPE_QUERY_CREDENTIALS,
        )
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)

        DebugLog.d("receive access: ${intent.data}")


        when (intent.action) {
            Kp2aContract.Actions.TRIGGER_REQUEST_ACCESS -> {
                // 授权成功，不需要处理
            }

            Kp2aContract.Actions.RECEIVE_ACCESS -> {
                keyboardReceiveAccess(
                    context = context,
                    intent = intent,
                )
            }

            Kp2aContract.Actions.REVOKE_ACCESS -> {
                keyboardRevokeAccess(
                    context = context,
                    intent = intent,
                )
            }

            else -> DebugLog.d("not implemented intent action")
        }
    }

    private fun keyboardReceiveAccess(
        context: Context,
        intent: Intent,
    ) {
        val hostPackage = intent.getStringExtra(Kp2aContract.Extras.SENDER)
            ?: return

        val accessToken = intent.getStringExtra(Kp2aContract.Extras.ACCESS_TOKEN)
            ?: return

        Kp2aAccessTokenStore(context).saveAccess(
            hostPackage = hostPackage,
            accessToken = accessToken,
            scopes = listOf(
                Kp2aContract.Scopes.QUERY_CREDENTIALS,
                Kp2aContract.Scopes.QUERY_CREDENTIALS_FOR_OWN_PACKAGE,
            ),
        )
    }

    private fun keyboardRevokeAccess(
        context: Context,
        intent: Intent,
    ) {
        val hostPackage = intent.getStringExtra(Kp2aContract.Extras.SENDER)
            ?: return

        Kp2aAccessTokenStore(context).clearAccess(hostPackage)
    }
}