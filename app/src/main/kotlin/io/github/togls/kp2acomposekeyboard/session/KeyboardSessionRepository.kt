package io.github.togls.kp2acomposekeyboard.session

import io.github.togls.kp2acomposekeyboard.security.SecureLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyboardSessionRepository @Inject constructor() {

    private val _session = MutableStateFlow<KeyboardSession?>(null)
    val session: StateFlow<KeyboardSession?> = _session.asStateFlow()

    fun setSession(session: KeyboardSession) {
        _session.value = session
        SecureLog.d("Session created")
    }

    fun clear() {
        _session.value = null
        SecureLog.d("Session cleared")
    }

    fun getFieldValue(fieldId: String): String? {
        if (fieldId.isBlank()) {
            SecureLog.d("Field commit ignored")
            return null
        }

        // Raw values leave the session only at commit time and are never copied into UI state.
        return _session.value
            ?.fields
            ?.firstOrNull { field -> field.id == fieldId }
            ?.value
    }

    fun getSnapshot(): KeyboardSessionSnapshot? {
        // Snapshots are the UI safety boundary and must not expose raw field values.
        return _session.value?.toSnapshot()
    }
}
