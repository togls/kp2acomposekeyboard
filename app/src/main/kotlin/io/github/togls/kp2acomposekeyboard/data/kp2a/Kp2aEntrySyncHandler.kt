package io.github.togls.kp2acomposekeyboard.data.kp2a

import android.content.Intent
import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import javax.inject.Inject

class Kp2aEntrySyncHandler @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
    private val resultParser: Kp2aEntryResultParser,
    private val entryMapper: Kp2aEntryMapper,
) {

    fun openEntry(intent: Intent) {
        val parsedEntry = parseEntry(intent)

        if (parsedEntry == null) {
            logSyncResult(source = SOURCE_OPEN_ENTRY, result = RESULT_IGNORED)
            return
        }

        sessionRepository.setSession(parsedEntry.session)
        logSyncResult(
            source = SOURCE_OPEN_ENTRY,
            result = RESULT_APPLIED,
            parsedEntry = parsedEntry,
        )
    }

    fun entryOutputModified(intent: Intent) {
        val parsedEntry = parseEntry(intent)

        if (parsedEntry == null) {
            logSyncResult(source = SOURCE_ENTRY_OUTPUT_MODIFIED, result = RESULT_IGNORED)
            return
        }

        if (!shouldApplyModifiedSession(parsedEntry.session)) {
            logSyncResult(
                source = SOURCE_ENTRY_OUTPUT_MODIFIED,
                result = RESULT_IGNORED,
                parsedEntry = parsedEntry,
            )
            return
        }

        sessionRepository.setSession(parsedEntry.session)
        logSyncResult(
            source = SOURCE_ENTRY_OUTPUT_MODIFIED,
            result = RESULT_APPLIED,
            parsedEntry = parsedEntry,
        )
    }

    fun closeEntryView(closedEntryId: String?) {
        logSyncResult(
            source = SOURCE_CLOSE_ENTRY_VIEW,
            result = RESULT_IGNORED,
            hasEntryId = !closedEntryId.isNullOrBlank(),
        )
    }

    fun lockDatabase() {
        clearForDatabaseAction(source = SOURCE_LOCK_DATABASE)
    }

    fun closeDatabase() {
        clearForDatabaseAction(source = SOURCE_CLOSE_DATABASE)
    }

    fun openDatabase() {
        logSyncResult(source = SOURCE_OPEN_DATABASE, result = RESULT_IGNORED)
    }

    fun unlockDatabase() {
        logSyncResult(source = SOURCE_UNLOCK_DATABASE, result = RESULT_IGNORED)
    }

    fun accessRevoked() {
        clearForSecurityBoundary(source = SOURCE_ACCESS_REVOKED)
    }

    private fun parseEntry(intent: Intent): ParsedEntry? {
        val result = resultParser.parse(intent)
        val session = entryMapper.mapToSession(result) ?: return null
        return ParsedEntry(
            result = result,
            session = session,
        )
    }

    private fun shouldApplyModifiedSession(
        incomingSession: KeyboardSession,
    ): Boolean {
        val currentSession = sessionRepository.currentSession() ?: return true
        val currentEntryId = currentSession.entryId
        val incomingEntryId = incomingSession.entryId

        if (currentEntryId.isNullOrBlank() || incomingEntryId.isNullOrBlank()) {
            return true
        }

        return currentEntryId == incomingEntryId
    }

    private fun clearForDatabaseAction(source: String) {
        clearForSecurityBoundary(source)
    }

    private fun clearForSecurityBoundary(source: String) {
        sessionRepository.clear()
        logSyncResult(source = source, result = RESULT_CLEARED)
    }

    private fun logSyncResult(
        source: String,
        result: String,
        parsedEntry: ParsedEntry? = null,
        hasEntryId: Boolean = !parsedEntry?.session?.entryId.isNullOrBlank(),
    ) {
        SecureLog.d(
            message = "kp2a entry sync handled",
            "source" to source,
            "result" to result,
            "hasEntryId" to hasEntryId,
            "fieldCount" to (parsedEntry?.result?.fields?.size ?: 0),
            "protectedFieldCount" to (parsedEntry?.result?.protectedFields?.size ?: 0),
        )
    }

    private data class ParsedEntry(
        val result: Kp2aEntryResult,
        val session: KeyboardSession,
    )

    private companion object {
        const val SOURCE_OPEN_ENTRY = "open_entry"
        const val SOURCE_ENTRY_OUTPUT_MODIFIED = "entry_output_modified"
        const val SOURCE_CLOSE_ENTRY_VIEW = "close_entry_view"
        const val SOURCE_LOCK_DATABASE = "lock_database"
        const val SOURCE_CLOSE_DATABASE = "close_database"
        const val SOURCE_OPEN_DATABASE = "open_database"
        const val SOURCE_UNLOCK_DATABASE = "unlock_database"
        const val SOURCE_ACCESS_REVOKED = "access_revoked"

        const val RESULT_APPLIED = "applied"
        const val RESULT_CLEARED = "cleared"
        const val RESULT_IGNORED = "ignored"
    }
}
