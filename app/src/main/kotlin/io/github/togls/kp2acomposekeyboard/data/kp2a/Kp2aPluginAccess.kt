package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.content.Context
import keepass2android.pluginsdk.AccessManager

object Kp2aPluginAccess {

    fun hasRequiredAccess(context: Context): Boolean {
        return findAccessibleHostPackage(context) != null
    }

    fun findAccessibleHostPackage(context: Context): String? {
        return hostPackages.firstOrNull { hostPackage ->
            AccessManager.tryGetAccessToken(
                context,
                hostPackage,
                Kp2aPluginScopes.requiredScopesForAccessManager(),
            ) != null
        }
    }

    private val hostPackages = listOf(
        "keepass2android.keepass2android",
        "keepass2android.keepass2android_nonet",
    )
}
