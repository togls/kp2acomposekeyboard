package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.content.Intent
import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.domain.policy.KeyboardFieldClassifier
import io.github.togls.kp2acomposekeyboard.domain.policy.SensitiveFieldPolicy
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
import keepass2android.pluginsdk.KeepassDefs
import keepass2android.pluginsdk.Strings
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Kp2aEntrySyncHandlerTest {

    @Test
    fun openEntry_createsSessionFromValidOutput() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)

        handler.openEntry(entryIntent(entryId = "entry-1", username = "open-user"))

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("Example Entry", session?.entryName)
        assertEquals("open-user", session?.usernameValue())
    }

    @Test
    fun openEntry_keepsPreviousSessionWhenOutputIsInvalid() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.openEntry(
            Intent().putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, "{ invalid json"),
        )

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("previous-user", session?.usernameValue())
    }

    @Test
    fun entryOutputModified_createsSessionWhenNoActiveSessionExists() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)

        handler.entryOutputModified(entryIntent(entryId = "entry-1", username = "modified-user"))

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("modified-user", session?.usernameValue())
    }

    @Test
    fun entryOutputModified_replacesSessionWhenEntryIdsMatch() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.entryOutputModified(entryIntent(entryId = "entry-1", username = "updated-user"))

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("updated-user", session?.usernameValue())
    }

    @Test
    fun entryOutputModified_replacesSessionWhenCurrentSessionHasNoEntryId() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = null, username = "previous-user"))

        handler.entryOutputModified(entryIntent(entryId = "entry-1", username = "updated-user"))

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("updated-user", session?.usernameValue())
    }

    @Test
    fun entryOutputModified_ignoresDifferentEntryId() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.entryOutputModified(entryIntent(entryId = "entry-2", username = "other-user"))

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("previous-user", session?.usernameValue())
    }

    @Test
    fun entryOutputModified_keepsPreviousSessionWhenOutputIsInvalid() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.entryOutputModified(
            Intent().putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, "{ invalid json"),
        )

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("previous-user", session?.usernameValue())
    }

    @Test
    fun closeEntryView_keepsSessionWhenEntryIdsMatch() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.closeEntryView(closedEntryId = "entry-1")

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("previous-user", session?.usernameValue())
    }

    @Test
    fun closeEntryView_keepsSessionWhenCurrentSessionHasNoEntryId() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = null, username = "previous-user"))

        handler.closeEntryView(closedEntryId = "entry-1")

        val session = repository.currentSession()
        assertNull(session?.entryId)
        assertEquals("previous-user", session?.usernameValue())
    }

    @Test
    fun closeEntryView_keepsSessionWhenClosedEntryIdIsMissing() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.closeEntryView(closedEntryId = null)

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("previous-user", session?.usernameValue())
    }

    @Test
    fun closeEntryView_ignoresDifferentEntryId() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.closeEntryView(closedEntryId = "entry-2")

        val session = repository.currentSession()
        assertEquals("entry-1", session?.entryId)
        assertEquals("previous-user", session?.usernameValue())
    }

    @Test
    fun lockDatabase_clearsSessionUnconditionally() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.lockDatabase()

        assertNull(repository.currentSession())
    }

    @Test
    fun closeDatabase_clearsSessionUnconditionally() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.closeDatabase()

        assertNull(repository.currentSession())
    }

    @Test
    fun accessRevoked_clearsSessionUnconditionally() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)
        handler.openEntry(entryIntent(entryId = "entry-1", username = "previous-user"))

        handler.accessRevoked()

        assertNull(repository.currentSession())
    }

    @Test
    fun openDatabase_doesNotCreateSession() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)

        handler.openDatabase()

        assertNull(repository.currentSession())
    }

    @Test
    fun unlockDatabase_doesNotCreateSession() {
        val repository = KeyboardSessionRepository()
        val handler = createHandler(repository)

        handler.unlockDatabase()

        assertNull(repository.currentSession())
    }

    private fun createHandler(
        repository: KeyboardSessionRepository,
    ): Kp2aEntrySyncHandler {
        return Kp2aEntrySyncHandler(
            sessionRepository = repository,
            resultParser = Kp2aEntryResultParser(),
            entryMapper = Kp2aEntryMapper(
                fieldClassifier = KeyboardFieldClassifier(),
                sensitiveFieldPolicy = SensitiveFieldPolicy(),
            ),
        )
    }

    private fun KeyboardSession.usernameValue(): String? {
        return fields.firstOrNull { field ->
            field.key == KeepassDefs.UserNameField
        }?.value
    }

    private fun entryIntent(
        entryId: String?,
        username: String,
    ): Intent {
        val fields = mapOf(
            KeepassDefs.TitleField to "Example Entry",
            KeepassDefs.UserNameField to username,
            KeepassDefs.PasswordField to "password-value",
        )

        return Intent().apply {
            if (entryId != null) {
                putExtra(Strings.EXTRA_ENTRY_ID, entryId)
            }
            putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, JSONObject(fields).toString())
            putExtra(
                Strings.EXTRA_PROTECTED_FIELDS_LIST,
                JSONArray(listOf(KeepassDefs.PasswordField)).toString(),
            )
        }
    }
}
