package io.github.togls.kp2acomposekeyboard.feature.entrypicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.togls.kp2acomposekeyboard.kp2a.Kp2aEntryMapper
import io.github.togls.kp2acomposekeyboard.kp2a.Kp2aEntryResult
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import io.github.togls.kp2acomposekeyboard.session.KeyboardSessionRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryPickerViewModel @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
    private val entryMapper: Kp2aEntryMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryPickerUiState())
    val uiState: StateFlow<EntryPickerUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<EntryPickerEffect>(
        extraBufferCapacity = EFFECT_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effect: SharedFlow<EntryPickerEffect> = _effect.asSharedFlow()

    private var selectionStarted = false

    fun onIntent(intent: EntryPickerIntent) {
        when (intent) {
            EntryPickerIntent.StartSelection -> startSelectionIfNeeded()
            EntryPickerIntent.Retry -> restartSelection()
            EntryPickerIntent.Cancel -> cancelSelection()

            is EntryPickerIntent.Kp2aResultSucceeded -> {
                handleKp2aSuccess(intent.result)
            }

            is EntryPickerIntent.Kp2aEntrySelected -> {
                handleKp2aEntrySelected(intent.fields)
            }

            EntryPickerIntent.Kp2aResultCancelled -> handleKp2aCancelled()
            EntryPickerIntent.Kp2aResultFailed -> handleKp2aFailed()
            EntryPickerIntent.Kp2aLaunchFailed -> handleKp2aLaunchFailed()
        }
    }

    private fun handleKp2aEntrySelected(fields: Map<String, String>) {
        SecureLog.d("handleKp2aEntrySelected not implemented!")
    }

    private fun startSelectionIfNeeded() {
        if (selectionStarted) {
            return
        }

        selectionStarted = true
        _uiState.value = EntryPickerUiState(
            status = EntryPickerStatus.Selecting,
            message = "正在打开 Keepass2Android...",
        )

        SecureLog.d("KP2A launch requested")
    }

    private fun restartSelection() {
        selectionStarted = false
        startSelectionIfNeeded()
    }

    private fun cancelSelection() {
        _uiState.value = EntryPickerUiState(
            status = EntryPickerStatus.Cancelled,
            message = "已取消选择",
        )

        SecureLog.d("KP2A selection cancelled")
        sendEffect(EntryPickerEffect.Finish)
    }

    private fun handleKp2aSuccess(result: Kp2aEntryResult) {
        val session = entryMapper.mapToSession(result)

        if (session == null) {
            handleKp2aFailed()
            return
        }

        sessionRepository.setSession(session)

        _uiState.value = EntryPickerUiState(
            status = EntryPickerStatus.Completed,
            message = "已选择 Keepass2Android 条目",
        )

        SecureLog.d(
            message = "kp2a entry mapped",
            "fieldCount" to result.fields.size,
            "protectedFieldCount" to result.protectedFields.size,
            "hasEntryId" to (result.entryId != null),
        )

        SecureLog.d("KP2A result received")
        sendEffect(EntryPickerEffect.Finish)
    }

    private fun handleKp2aCancelled() {
        _uiState.value = EntryPickerUiState(
            status = EntryPickerStatus.Cancelled,
            message = "已取消选择",
        )

        // 用户取消选择时不能清除旧 Session，避免破坏当前可用条目。
        SecureLog.d("KP2A selection cancelled")
        sendEffect(EntryPickerEffect.Finish)
    }

    private fun handleKp2aFailed() {
        selectionStarted = false
        _uiState.value = EntryPickerUiState(
            status = EntryPickerStatus.Failed,
            message = "未能从 Keepass2Android 获取条目",
        )

        SecureLog.d("KP2A selection failed")
    }

    private fun handleKp2aLaunchFailed() {
        selectionStarted = false
        _uiState.value = EntryPickerUiState(
            status = EntryPickerStatus.Failed,
            message = "无法打开 Keepass2Android",
        )

        SecureLog.d("KP2A launch failed")
    }

    private fun sendEffect(effect: EntryPickerEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    private companion object {
        // Activity 可能在 KP2A 返回后马上 finish，保留一个缓冲避免一次性 effect 被生命周期切换吞掉。
        const val EFFECT_BUFFER_CAPACITY = 4
    }
}