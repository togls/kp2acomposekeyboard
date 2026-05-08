package io.github.togls.kp2acomposekeyboard.feature.keyboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

            KeyboardIntent.SelectEntry -> sendEffect(KeyboardEffect.LaunchEntryPicker)

            KeyboardIntent.SwitchToLetters -> updateDefaultInputMode(DefaultInputMode.Letters)
            KeyboardIntent.SwitchToNumbers -> updateDefaultInputMode(DefaultInputMode.Numbers)
            KeyboardIntent.SwitchToSymbols -> updateDefaultInputMode(DefaultInputMode.Symbols)
            KeyboardIntent.ToggleUppercase -> toggleUppercase()

            KeyboardIntent.OpenSettings,
            KeyboardIntent.ClearEntry,
            KeyboardIntent.SwitchToDefaultLayout,
            KeyboardIntent.SwitchToEntryLayout,
            is KeyboardIntent.CommitField,
            KeyboardIntent.PrevExtraFieldPage,
            KeyboardIntent.NextExtraFieldPage,
            KeyboardIntent.ExpandFields,
            KeyboardIntent.CollapseFields,
            KeyboardIntent.ScrollExpandedFieldsUp,
            KeyboardIntent.ScrollExpandedFieldsDown,
                -> Unit
        }
    }

    private fun commitText(text: String) {
        if (text.isEmpty()) {
            return
        }

        sendEffect(KeyboardEffect.CommitText(text))
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