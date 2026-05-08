package io.github.togls.kp2acomposekeyboard.feature.entrypicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EntryPickerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(EntryPickerUiState())
    val uiState: StateFlow<EntryPickerUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<EntryPickerEffect>(
        extraBufferCapacity = EFFECT_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effect: SharedFlow<EntryPickerEffect> = _effect.asSharedFlow()

    fun onIntent(intent: EntryPickerIntent) {
        when (intent) {
            EntryPickerIntent.Retry -> showSelecting()
            EntryPickerIntent.Cancel -> showCancelledAndFinish()
        }
    }

    private fun showSelecting() {
        _uiState.value = EntryPickerUiState(
            status = EntryPickerStatus.Selecting,
            message = "正在准备选择 Keepass2Android 条目",
        )
    }

    private fun showCancelledAndFinish() {
        _uiState.value = EntryPickerUiState(
            status = EntryPickerStatus.Cancelled,
            message = "已取消选择",
        )

        viewModelScope.launch {
            _effect.emit(EntryPickerEffect.Finish)
        }
    }

    private companion object {
        // Activity 可能在选择结果返回后马上 finish，保留一个缓冲避免 finish effect 被生命周期切换吞掉。
        const val EFFECT_BUFFER_CAPACITY = 4
    }
}