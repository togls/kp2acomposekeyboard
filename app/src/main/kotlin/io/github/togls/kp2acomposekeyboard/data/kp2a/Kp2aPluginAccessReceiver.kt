package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import keepass2android.pluginsdk.PluginAccessBroadcastReceiver
import keepass2android.pluginsdk.Strings

class Kp2aPluginAccessReceiver : PluginAccessBroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)

        if (intent.action == Strings.ACTION_REVOKE_ACCESS) {
            clearSessionAfterAccessRevoked(context)
        }
    }

    override fun getScopes(): ArrayList<String> {
        SecureLog.d(
            message = "kp2a access scopes requested",
            "scopeCount" to REQUIRED_SCOPES.size,
        )

        return Kp2aPluginScopes.requiredScopesForAccessManager()
    }

    private fun clearSessionAfterAccessRevoked(context: Context) {
        runCatching {
            entryPoint(context).kp2aEntrySyncHandler().accessRevoked()
        }.onFailure { error ->
            SecureLog.w(
                message = "kp2a access revoke handling failed",
                throwable = error,
                "errorType" to error::class.java.simpleName,
            )
        }
    }

    private fun entryPoint(context: Context): Kp2aPluginAccessReceiverEntryPoint {
        val appContext = context.applicationContext ?: context

        return EntryPointAccessors.fromApplication(
            appContext,
            Kp2aPluginAccessReceiverEntryPoint::class.java,
        )
    }

    companion object {
        val REQUIRED_SCOPES = Kp2aPluginScopes.REQUIRED_SCOPES
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface Kp2aPluginAccessReceiverEntryPoint {
    fun kp2aEntrySyncHandler(): Kp2aEntrySyncHandler
}
