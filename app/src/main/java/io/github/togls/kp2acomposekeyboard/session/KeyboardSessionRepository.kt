package io.github.togls.kp2acomposekeyboard.session

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class KeyboardSessionRepository @Inject constructor() {

    private val _session = MutableStateFlow<KeyboardSession?>(null)
    val session: StateFlow<KeyboardSession?> = _session.asStateFlow()

    fun setSession(session: KeyboardSession) {
        _session.value = session
    }

    fun clear() {
        _session.value = null
    }

    fun getFieldValue(fieldId: String): String? {
        if (fieldId.isBlank()) {
            return null
        }

        return _session.value
            ?.fields
            ?.firstOrNull { field -> field.id == fieldId }
            ?.value
    }

    fun getSnapshot(): KeyboardSessionSnapshot? {
        // Snapshot 是 UI 安全边界，必须通过 mapper 去掉字段真实 value。
        return _session.value?.toSnapshot()
    }
}