package io.github.togls.kp2acomposekeyboard.data.kp2a

import keepass2android.pluginsdk.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Kp2aPluginScopesTest {

    @Test
    fun requiredScopes_includeCurrentEntryQueryCredentialsAndDatabaseActions() {
        assertEquals(
            listOf(
                Strings.SCOPE_CURRENT_ENTRY,
                Strings.SCOPE_QUERY_CREDENTIALS,
                Strings.SCOPE_DATABASE_ACTIONS,
            ),
            Kp2aPluginScopes.REQUIRED_SCOPES,
        )
    }

    @Test
    fun requiredScopesForAccessManager_returnsMutableArrayListCopy() {
        val scopes = Kp2aPluginScopes.requiredScopesForAccessManager()
        scopes.add("test-scope")

        assertTrue("test-scope" !in Kp2aPluginScopes.REQUIRED_SCOPES)
    }
}
