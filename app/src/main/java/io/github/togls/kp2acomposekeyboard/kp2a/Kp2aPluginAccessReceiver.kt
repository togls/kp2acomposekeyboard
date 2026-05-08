package io.github.togls.kp2acomposekeyboard.kp2a

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.json.JSONArray

class Kp2aPluginAccessReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            Kp2aContract.Actions.TRIGGER_REQUEST_ACCESS -> {
                requestAccess(
                    context = context,
                    triggerIntent = intent,
                )
            }

            Kp2aContract.Actions.RECEIVE_ACCESS -> {
                receiveAccess(
                    context = context,
                    intent = intent,
                )
            }

            Kp2aContract.Actions.REVOKE_ACCESS -> {
                revokeAccess(
                    context = context,
                    intent = intent,
                )
            }
        }
    }

    private fun requestAccess(
        context: Context,
        triggerIntent: Intent,
    ) {
        val requestToken = triggerIntent.getStringExtra(Kp2aContract.Extras.REQUEST_TOKEN)
            ?: return

        val hostPackage = triggerIntent.getStringExtra(Kp2aContract.Extras.SENDER)

        val scopes = listOf(
            Kp2aContract.Scopes.QUERY_CREDENTIALS,
            Kp2aContract.Scopes.QUERY_CREDENTIALS_FOR_OWN_PACKAGE,
        )

        val requestIntent = Intent(Kp2aContract.Actions.REQUEST_ACCESS).apply {
            putExtra(Kp2aContract.Extras.REQUEST_TOKEN, requestToken)
            putExtra(Kp2aContract.Extras.PLUGIN_PACKAGE, context.packageName)
            putExtra(Kp2aContract.Extras.SENDER, context.packageName)
            putExtra(Kp2aContract.Extras.SCOPES, JSONArray(scopes).toString())

            // KP2A 发来的 sender 是授权宿主包名；设置 package 可以避免广播被无关应用接收。
            if (!hostPackage.isNullOrBlank()) {
                setPackage(hostPackage)
            }
        }

        context.sendBroadcast(requestIntent)
    }

    private fun receiveAccess(
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

    private fun revokeAccess(
        context: Context,
        intent: Intent,
    ) {
        val hostPackage = intent.getStringExtra(Kp2aContract.Extras.SENDER)
            ?: return

        Kp2aAccessTokenStore(context).clearAccess(hostPackage)
    }
}