package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.content.Context
import keepass2android.pluginsdk.AccessManager
import keepass2android.pluginsdk.Strings

object Kp2aPluginAccess {

    private val requiredScopes = arrayListOf(
        Strings.SCOPE_CURRENT_ENTRY,
        Strings.SCOPE_QUERY_CREDENTIALS,
    )

    fun hasRequiredAccess(context: Context): Boolean {
        return findAccessibleHostPackage(context) != null
    }

    fun findAccessibleHostPackage(context: Context): String? {
        return hostPackages.firstOrNull { hostPackage ->
            AccessManager.tryGetAccessToken(
                context,
                hostPackage,
                requiredScopes,
            ) != null
        }
    }

    private val hostPackages = listOf(
        "keepass2android.keepass2android",
        "keepass2android.keepass2android_nonet",
    )
}