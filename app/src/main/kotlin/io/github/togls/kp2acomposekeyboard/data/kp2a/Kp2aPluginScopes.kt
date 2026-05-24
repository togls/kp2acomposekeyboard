package io.github.togls.kp2acomposekeyboard.data.kp2a

import keepass2android.pluginsdk.Strings

object Kp2aPluginScopes {

    val REQUIRED_SCOPES = listOf(
        Strings.SCOPE_CURRENT_ENTRY,
        Strings.SCOPE_QUERY_CREDENTIALS,
        Strings.SCOPE_DATABASE_ACTIONS,
    )

    fun requiredScopesForAccessManager(): ArrayList<String> {
        return ArrayList(REQUIRED_SCOPES)
    }
}
