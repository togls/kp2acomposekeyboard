package io.github.togls.kp2acomposekeyboard.feature.keyboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.togls.kp2acomposekeyboard.domain.KeyboardField
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import io.github.togls.kp2acomposekeyboard.security.SecureLogEvent
import io.github.togls.kp2acomposekeyboard.session.KeyboardSession
import io.github.togls.kp2acomposekeyboard.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.session.KeyboardSessionSnapshot
import io.github.togls.kp2acomposekeyboard.session.SessionTimeoutController
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KeyboardViewModel(
    private val sessionRepository: KeyboardSessionRepository,
    private val sessionTimeoutController: SessionTimeoutController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(KeyboardUiState())
    val uiState: StateFlow<KeyboardUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<KeyboardEffect>(
        extraBufferCapacity = EFFECT_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effect: SharedFlow<KeyboardEffect> = _effect.asSharedFlow()

    init {
        observeSession()
    }

    fun onIntent(intent: KeyboardIntent) {
        when (intent) {
            is KeyboardIntent.CommitText -> commitText(intent.text)
            KeyboardIntent.DeleteBackward -> sendEffect(KeyboardEffect.DeleteBackward)
            KeyboardIntent.Enter -> sendEffect(KeyboardEffect.SendEnter)

            KeyboardIntent.SelectEntry -> loadFakeSession()
            KeyboardIntent.ClearEntry -> sessionTimeoutController.clearNow()

            KeyboardIntent.SwitchToDefaultLayout -> switchToDefaultLayout()
            KeyboardIntent.SwitchToEntryLayout -> switchToEntryLayout()

            KeyboardIntent.SwitchToLetters -> updateDefaultInputMode(DefaultInputMode.Letters)
            KeyboardIntent.SwitchToNumbers -> updateDefaultInputMode(DefaultInputMode.Numbers)
            KeyboardIntent.SwitchToSymbols -> updateDefaultInputMode(DefaultInputMode.Symbols)

            KeyboardIntent.ToggleUppercase -> toggleUppercase()

            is KeyboardIntent.CommitField -> commitField(intent.fieldId)

            KeyboardIntent.PrevExtraFieldPage -> previousExtraFieldPage()
            KeyboardIntent.NextExtraFieldPage -> nextExtraFieldPage()

            KeyboardIntent.ExpandFields -> expandFields()
            KeyboardIntent.CollapseFields -> collapseFields()

            KeyboardIntent.OpenSettings -> sendEffect(KeyboardEffect.LaunchSettings)

            KeyboardIntent.ScrollExpandedFieldsUp -> Unit
            KeyboardIntent.ScrollExpandedFieldsDown -> Unit
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionRepository.session.collect { session ->
                val snapshot = sessionRepository.getSnapshot()

                if (session != null) {
                    sessionTimeoutController.restartTimeout(viewModelScope)
                } else {
                    sessionTimeoutController.cancelTimeout()
                }

                _uiState.update { state ->
                    when {
                        snapshot != null -> state.withSessionSnapshot(snapshot)

                        state.hasActiveSession -> state.withoutSession()

                        else -> state
                    }
                }
            }
        }
    }

    override fun onCleared() {
        sessionTimeoutController.cancelTimeout()
        super.onCleared()
    }

    private fun KeyboardUiState.withSessionSnapshot(
        snapshot: KeyboardSessionSnapshot,
    ): KeyboardUiState {
        return copy(
            mainLayout = MainKeyboardLayout.Entry,
            entryFieldDisplayMode = EntryFieldDisplayMode.Paged,
            currentEntryName = snapshot.entryName,
            hasActiveSession = true,
            fixedFields = snapshot.fixedFields,
            extraFields = snapshot.extraFields,
            allFields = snapshot.allFields,
            extraFieldPageIndex = 0,
        )
    }

    private fun KeyboardUiState.withoutSession(): KeyboardUiState {
        return copy(
            mainLayout = MainKeyboardLayout.Default,
            entryFieldDisplayMode = EntryFieldDisplayMode.Paged,
            currentEntryName = null,
            hasActiveSession = false,
            fixedFields = emptyList(),
            extraFields = emptyList(),
            allFields = emptyList(),
            extraFieldPageIndex = 0,
        )
    }

    private fun commitText(text: String) {
        if (text.isEmpty()) {
            return
        }

        sendEffect(KeyboardEffect.CommitText(text))
    }

    private fun loadFakeSession() {
        sessionRepository.setSession(
            KeyboardSession(
                entryId = "fake_entry_github",
                entryName = "GitHub - Personal Account",
                fields = createFakeFields(),
                createdAtMillis = System.currentTimeMillis(),
            ),
        )
    }

    private fun createFakeFields(): List<KeyboardField> {
        return listOf(
            KeyboardField(
                id = "fake_username",
                key = "username",
                label = "Username",
                value = "octocat",
                type = KeyboardFieldType.Username,
                sensitive = false,
            ),
            KeyboardField(
                id = "fake_password",
                key = "password",
                label = "Password",
                value = "fake-password-for-dev-only",
                type = KeyboardFieldType.Password,
                sensitive = true,
            ),
            KeyboardField(
                id = "fake_totp",
                key = "totp",
                label = "TOTP",
                value = "123456",
                type = KeyboardFieldType.Totp,
                sensitive = true,
            ),
            KeyboardField(
                id = "fake_url",
                key = "url",
                label = "URL",
                value = "https://github.com",
                type = KeyboardFieldType.Url,
                sensitive = false,
            ),
            KeyboardField(
                id = "fake_email",
                key = "email",
                label = "Email",
                value = "octocat@example.com",
                type = KeyboardFieldType.Email,
                sensitive = false,
            ),
            KeyboardField(
                id = "fake_recovery",
                key = "recovery",
                label = "Recovery",
                value = "fake-recovery-code",
                type = KeyboardFieldType.Recovery,
                sensitive = true,
            ),
            KeyboardField(
                id = "fake_phone",
                key = "phone",
                label = "Phone",
                value = "+1 000 000 0000",
                type = KeyboardFieldType.Phone,
                sensitive = false,
            ),
            KeyboardField(
                id = "fake_address",
                key = "address",
                label = "Address",
                value = "Fake Address",
                type = KeyboardFieldType.Address,
                sensitive = false,
            ),
            KeyboardField(
                id = "fake_notes",
                key = "notes",
                label = "Notes",
                value = "Fake notes",
                type = KeyboardFieldType.Notes,
                sensitive = false,
            ),
            KeyboardField(
                id = "fake_custom_1",
                key = "custom1",
                label = "Custom1",
                value = "Fake custom 1",
                type = KeyboardFieldType.Custom,
                sensitive = false,
            ),
            KeyboardField(
                id = "fake_custom_2",
                key = "custom2",
                label = "Custom2",
                value = "Fake custom 2",
                type = KeyboardFieldType.Custom,
                sensitive = false,
            ),
        )
    }

    private fun switchToDefaultLayout() {
        _uiState.update { state ->
            state.copy(mainLayout = MainKeyboardLayout.Default)
        }
    }

    private fun switchToEntryLayout() {
        _uiState.update { state ->
            if (!state.hasActiveSession) {
                state
            } else {
                state.copy(mainLayout = MainKeyboardLayout.Entry)
            }
        }
    }

    private fun updateDefaultInputMode(inputMode: DefaultInputMode) {
        _uiState.update { state ->
            state.copy(defaultInputMode = inputMode)
        }
    }

    private fun toggleUppercase() {
        _uiState.update { state ->
            state.copy(isUppercase = !state.isUppercase)
        }
    }

    private fun commitField(fieldId: String) {
        val value = sessionRepository.getFieldValue(fieldId)

        if (value.isNullOrEmpty()) {
            SecureLog.debug(SecureLogEvent.FieldCommitIgnored)
            return
        }

        // value 可能是 Password / TOTP / Recovery Code，不能写入 UiState，也不能打印日志。
        sendEffect(KeyboardEffect.CommitText(value))
    }

    private fun previousExtraFieldPage() {
        _uiState.update { state ->
            state.copy(
                extraFieldPageIndex = (state.extraFieldPageIndex - 1).coerceAtLeast(0),
            )
        }
    }

    private fun nextExtraFieldPage() {
        _uiState.update { state ->
            val maxPageIndex = state.maxExtraFieldPageIndex()

            state.copy(
                extraFieldPageIndex = (state.extraFieldPageIndex + 1).coerceAtMost(maxPageIndex),
            )
        }
    }

    private fun expandFields() {
        _uiState.update { state ->
            if (!state.hasActiveSession) {
                state
            } else {
                state.copy(entryFieldDisplayMode = EntryFieldDisplayMode.Expanded)
            }
        }
    }

    private fun collapseFields() {
        _uiState.update { state ->
            state.copy(
                entryFieldDisplayMode = EntryFieldDisplayMode.Paged,
                extraFieldPageIndex = 0,
            )
        }
    }

    private fun KeyboardUiState.maxExtraFieldPageIndex(): Int {
        val pageSize = extraFieldPageSize.coerceAtLeast(1)
        if (extraFields.isEmpty()) {
            return 0
        }

        return (extraFields.size - 1) / pageSize
    }

    private fun sendEffect(effect: KeyboardEffect) {
        // effect 里的 CommitText 后续可能承载密码/TOTP，所以这里不能打印 effect 内容。
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    private companion object {
        // 输入法按键可能连续快速点击，保留少量缓冲可避免短时间内没有 collector 时丢失过多动作。
        const val EFFECT_BUFFER_CAPACITY = 16
    }
}