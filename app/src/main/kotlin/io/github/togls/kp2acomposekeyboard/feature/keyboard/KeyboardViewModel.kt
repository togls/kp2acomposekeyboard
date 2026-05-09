package io.github.togls.kp2acomposekeyboard.feature.keyboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.togls.kp2acomposekeyboard.security.SecureLog
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

            KeyboardIntent.SelectEntry -> sendEffect(
                // 真正的 targetPackageName 应由 KeyboardImeService 从 currentInputEditorInfo.packageName 补进去更合适，
                // 因为 ViewModel 不应该持有 IME / EditorInfo
                KeyboardEffect.LaunchEntryPicker(targetPackageName = null),
            )

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
            SecureLog.d("Field commit ignored")
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