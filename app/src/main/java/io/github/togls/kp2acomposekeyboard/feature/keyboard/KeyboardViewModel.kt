package io.github.togls.kp2acomposekeyboard.feature.keyboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KeyboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(KeyboardUiState())
    val uiState: StateFlow<KeyboardUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<KeyboardEffect>(
        extraBufferCapacity = EFFECT_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effect: SharedFlow<KeyboardEffect> = _effect.asSharedFlow()

    fun onIntent(intent: KeyboardIntent) {
        when (intent) {
            is KeyboardIntent.CommitText -> commitText(intent.text)
            KeyboardIntent.DeleteBackward -> sendEffect(KeyboardEffect.DeleteBackward)
            KeyboardIntent.Enter -> sendEffect(KeyboardEffect.SendEnter)

            KeyboardIntent.SelectEntry -> {
                loadFakeEntryForPagedMode()
                Log.d("Kp2aKeyboardIme", "SelectEntry clicked, load fake entry")
            }
            KeyboardIntent.ClearEntry -> clearEntry()

            KeyboardIntent.SwitchToDefaultLayout -> switchToDefaultLayout()
            KeyboardIntent.SwitchToEntryLayout -> switchToEntryLayout()

            KeyboardIntent.SwitchToLetters -> updateDefaultInputMode(DefaultInputMode.Letters)
            KeyboardIntent.SwitchToNumbers -> updateDefaultInputMode(DefaultInputMode.Numbers)
            KeyboardIntent.SwitchToSymbols -> updateDefaultInputMode(DefaultInputMode.Symbols)

            KeyboardIntent.ToggleUppercase -> toggleUppercase()

            is KeyboardIntent.CommitField -> Unit

            KeyboardIntent.PrevExtraFieldPage -> previousExtraFieldPage()
            KeyboardIntent.NextExtraFieldPage -> nextExtraFieldPage()

            KeyboardIntent.ExpandFields -> expandFields()
            KeyboardIntent.CollapseFields -> collapseFields()

            KeyboardIntent.OpenSettings -> sendEffect(KeyboardEffect.LaunchSettings)

            KeyboardIntent.ScrollExpandedFieldsUp -> Unit
            KeyboardIntent.ScrollExpandedFieldsDown -> Unit
        }
    }

    private fun commitText(text: String) {
        if (text.isEmpty()) {
            return
        }

        sendEffect(KeyboardEffect.CommitText(text))
    }

    private fun loadFakeEntryForPagedMode() {
        val fixedFields = listOf(
            KeyboardFieldUiModel(
                id = "fake_username",
                label = "Username",
                type = KeyboardFieldType.Username,
                sensitive = false,
            ),
            KeyboardFieldUiModel(
                id = "fake_password",
                label = "Password",
                type = KeyboardFieldType.Password,
                sensitive = true,
            ),
            KeyboardFieldUiModel(
                id = "fake_totp",
                label = "TOTP",
                type = KeyboardFieldType.Totp,
                sensitive = true,
            ),
        )

        val extraFields = listOf(
            KeyboardFieldUiModel(
                id = "fake_url",
                label = "URL",
                type = KeyboardFieldType.Url,
                sensitive = false,
            ),
            KeyboardFieldUiModel(
                id = "fake_email",
                label = "Email",
                type = KeyboardFieldType.Email,
                sensitive = false,
            ),
            KeyboardFieldUiModel(
                id = "fake_recovery",
                label = "Recovery",
                type = KeyboardFieldType.Recovery,
                sensitive = true,
            ),
            KeyboardFieldUiModel(
                id = "fake_phone",
                label = "Phone",
                type = KeyboardFieldType.Phone,
                sensitive = false,
            ),
            KeyboardFieldUiModel(
                id = "fake_address",
                label = "Address",
                type = KeyboardFieldType.Address,
                sensitive = false,
            ),
            KeyboardFieldUiModel(
                id = "fake_notes",
                label = "Notes",
                type = KeyboardFieldType.Notes,
                sensitive = false,
            ),
            KeyboardFieldUiModel(
                id = "fake_custom_1",
                label = "Custom1",
                type = KeyboardFieldType.Custom,
                sensitive = false,
            ),
            KeyboardFieldUiModel(
                id = "fake_custom_2",
                label = "Custom2",
                type = KeyboardFieldType.Custom,
                sensitive = false,
            ),
        )

        _uiState.update { state ->
            state.copy(
                mainLayout = MainKeyboardLayout.Entry,
                entryFieldDisplayMode = EntryFieldDisplayMode.Paged,
                currentEntryName = "GitHub",
                hasActiveSession = true,
                fixedFields = fixedFields,
                extraFields = extraFields,
                allFields = fixedFields + extraFields,
                extraFieldPageIndex = 0,
            )
        }
    }

    private fun clearEntry() {
        _uiState.update { state ->
            state.copy(
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
            state.copy(entryFieldDisplayMode = EntryFieldDisplayMode.Paged)
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