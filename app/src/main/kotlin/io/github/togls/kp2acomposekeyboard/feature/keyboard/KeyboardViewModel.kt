package io.github.togls.kp2acomposekeyboard.feature.keyboard

import io.github.togls.kp2acomposekeyboard.application.keyboard.CommitFieldResult
import io.github.togls.kp2acomposekeyboard.application.keyboard.CommitKeyboardFieldUseCase
import io.github.togls.kp2acomposekeyboard.application.keyboard.ObserveKeyboardSessionSnapshotUseCase
import io.github.togls.kp2acomposekeyboard.application.session.SessionTimeoutController
import io.github.togls.kp2acomposekeyboard.application.settings.KeyboardSettingsStore
import io.github.togls.kp2acomposekeyboard.domain.keyboard.ClearEntryUtilityItemId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.TextInputMode
import io.github.togls.kp2acomposekeyboard.domain.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardSubtype
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilityItemId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlots
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlotsReducer
import io.github.togls.kp2acomposekeyboard.domain.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.domain.keyboard.SettingsUtilityItemId

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSessionSnapshot
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
    private val observeKeyboardSessionSnapshot: ObserveKeyboardSessionSnapshotUseCase,
    private val commitKeyboardField: CommitKeyboardFieldUseCase,
    private val sessionTimeoutController: SessionTimeoutController,
    private val settingsStore: KeyboardSettingsStore,
) : ViewModel() {

    private val utilitySlotsReducer = KeyboardUtilitySlotsReducer()

    private val _uiState = MutableStateFlow(KeyboardUiState())
    val uiState: StateFlow<KeyboardUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<KeyboardEffect>(
        extraBufferCapacity = EFFECT_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effect: SharedFlow<KeyboardEffect> = _effect.asSharedFlow()

    init {
        observeSession()
        observeSettings()
    }

    fun onIntent(intent: KeyboardIntent) {
        when (intent) {
            is KeyboardIntent.CommitText -> commitText(intent.text)
            KeyboardIntent.DeleteBackward -> sendEffect(KeyboardEffect.DeleteBackward)
            KeyboardIntent.Enter -> sendEffect(KeyboardEffect.SendEnter)

            KeyboardIntent.SelectEntry -> sendEffect(
                // KeyboardImeService owns EditorInfo, so it should fill the target package name.
                KeyboardEffect.LaunchEntryPicker(targetPackageName = null),
            )

            KeyboardIntent.ClearEntry -> sessionTimeoutController.clearNow()
            KeyboardIntent.ToggleUtilityPanel -> toggleUtilityPanel()
            KeyboardIntent.CloseUtilityPanel -> closeUtilityPanel()
            is KeyboardIntent.ClickUtilityItem -> clickUtilityItem(intent.itemId)
            is KeyboardIntent.MoveUtilityItemToCenter -> updateUtilitySlots { slots ->
                utilitySlotsReducer.moveToCenter(
                    slots = slots,
                    itemId = intent.itemId,
                    targetIndex = intent.targetIndex,
                )
            }

            is KeyboardIntent.MoveUtilityItemToRight -> updateUtilitySlots { slots ->
                utilitySlotsReducer.moveToRight(
                    slots = slots,
                    itemId = intent.itemId,
                )
            }

            is KeyboardIntent.RemoveUtilityItem -> updateUtilitySlots { slots ->
                utilitySlotsReducer.remove(
                    slots = slots,
                    itemId = intent.itemId,
                )
            }

            KeyboardIntent.SwitchToDefaultLayout -> switchToDefaultLayout()
            KeyboardIntent.SwitchToEntryLayout -> switchToEntryLayout()
            is KeyboardIntent.ChangeSubtype -> changeSubtype(intent.subtype)
            KeyboardIntent.SwitchLanguage -> switchLanguage()

            KeyboardIntent.SwitchToLetters -> updateTextInputMode(TextInputMode.Letters)
            KeyboardIntent.SwitchToNumbers -> updateTextInputMode(TextInputMode.Numbers)
            KeyboardIntent.SwitchToSymbols -> updateTextInputMode(TextInputMode.Symbols)

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

    private fun observeSettings() {
        viewModelScope.launch {
            settingsStore.settings.collect { settings ->
                _uiState.update { state ->
                    state.copy(
                        utilitySlots = settings.utilitySlots,
                        englishUsSubtypeEnabled = settings.englishUsSubtypeEnabled,
                    )
                }
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            observeKeyboardSessionSnapshot().collect { snapshot ->
                if (snapshot != null) {
                    // Timeout is tied to the ViewModel scope so it stops with the IME UI lifecycle.
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
            mainLayout = currentSubtype.mainLayout,
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
            state.copy(mainLayout = MainKeyboardLayout.TextInput)
        }
    }

    private fun switchToEntryLayout() {
        _uiState.update { state ->
            // Entry layout without a session would show empty credential controls.
            if (!state.hasActiveSession) {
                state
            } else {
                state.copy(mainLayout = MainKeyboardLayout.Entry)
            }
        }
    }

    private fun updateTextInputMode(inputMode: TextInputMode) {
        _uiState.update { state ->
            state.copy(textInputMode = inputMode)
        }
    }

    private fun changeSubtype(subtype: KeyboardSubtype) {
        _uiState.update { state ->
            state.copy(
                currentSubtype = subtype,
                mainLayout = subtype.mainLayout,
                entryFieldDisplayMode = EntryFieldDisplayMode.Paged,
                extraFieldPageIndex = 0,
            )
        }
    }

    private fun switchLanguage() {
        val state = _uiState.value

        if (state.mainLayout == MainKeyboardLayout.Entry && state.englishUsSubtypeEnabled) {
            _uiState.update { currentState ->
                currentState.copy(
                    currentSubtype = KeyboardSubtype.EnglishUs,
                    mainLayout = MainKeyboardLayout.TextInput,
                    entryFieldDisplayMode = EntryFieldDisplayMode.Paged,
                    extraFieldPageIndex = 0,
                )
            }
            sendEffect(KeyboardEffect.SwitchToSubtype(KeyboardSubtype.EnglishUs))
            return
        }

        sendEffect(KeyboardEffect.SwitchToNextInputMethod)
    }

    private fun toggleUtilityPanel() {
        _uiState.update { state ->
            state.copy(isUtilityPanelExpanded = !state.isUtilityPanelExpanded)
        }
    }

    private fun closeUtilityPanel() {
        _uiState.update { state ->
            state.copy(isUtilityPanelExpanded = false)
        }
    }

    private fun clickUtilityItem(itemId: KeyboardUtilityItemId) {
        if (itemId == SettingsUtilityItemId) {
            sendEffect(KeyboardEffect.LaunchSettings)
        }
        if (itemId == ClearEntryUtilityItemId) {
            sessionTimeoutController.clearNow()
        }
    }

    private fun updateUtilitySlots(
        transform: (KeyboardUtilitySlots) -> KeyboardUtilitySlots,
    ) {
        viewModelScope.launch {
            settingsStore.updateUtilitySlots(transform(_uiState.value.utilitySlots))
        }
    }

    private fun toggleUppercase() {
        _uiState.update { state ->
            state.copy(isUppercase = !state.isUppercase)
        }
    }

    private fun commitField(fieldId: String) {
        when (val result = commitKeyboardField(fieldId)) {
            is CommitFieldResult.Commit -> {
                sendEffect(KeyboardEffect.CommitText(result.text))
            }

            is CommitFieldResult.Ignored -> {
                SecureLog.d(
                    message = "Field commit ignored",
                    "reason" to result.reason.name,
                )
            }
        }
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
            // Expanded fields are meaningful only while a sanitized session snapshot is active.
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
        // CommitText effects may carry secrets, so do not log effect payloads here.
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    private companion object {
        // Keyboard actions can arrive in bursts while collectors briefly restart.
        const val EFFECT_BUFFER_CAPACITY = 16
    }
}
