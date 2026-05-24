package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import keepass2android.pluginsdk.PluginActionBroadcastReceiver
import keepass2android.pluginsdk.Strings

class Kp2aPluginActionReceiver : PluginActionBroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        runCatching {
            val handler = entryPoint(context).kp2aEntrySyncHandler()
            routeAction(
                handler = handler,
                intent = intent,
            )
        }.onFailure { error ->
            SecureLog.w(
                message = "kp2a action handling failed",
                throwable = error,
                "errorType" to error::class.java.simpleName,
            )
        }
    }

    private fun routeAction(
        handler: Kp2aEntrySyncHandler,
        intent: Intent,
    ) {
        when (intent.action) {
            Strings.ACTION_OPEN_ENTRY -> handler.openEntry(intent)
            Strings.ACTION_ENTRY_OUTPUT_MODIFIED -> handler.entryOutputModified(intent)
            Strings.ACTION_CLOSE_ENTRY_VIEW -> {
                handler.closeEntryView(intent.getStringExtra(Strings.EXTRA_ENTRY_ID))
            }

            Strings.ACTION_LOCK_DATABASE -> handler.lockDatabase()
            Strings.ACTION_CLOSE_DATABASE -> handler.closeDatabase()
            Strings.ACTION_OPEN_DATABASE -> handler.openDatabase()
            Strings.ACTION_UNLOCK_DATABASE -> handler.unlockDatabase()
            else -> {
                SecureLog.d(
                    message = "kp2a action ignored",
                    "hasAction" to !intent.action.isNullOrBlank(),
                )
            }
        }
    }

    private fun entryPoint(context: Context): Kp2aPluginActionReceiverEntryPoint {
        val appContext = context.applicationContext ?: context

        return EntryPointAccessors.fromApplication(
            appContext,
            Kp2aPluginActionReceiverEntryPoint::class.java,
        )
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface Kp2aPluginActionReceiverEntryPoint {
    fun kp2aEntrySyncHandler(): Kp2aEntrySyncHandler
}
