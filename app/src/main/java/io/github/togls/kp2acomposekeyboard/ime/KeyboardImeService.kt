package io.github.togls.kp2acomposekeyboard.ime

import android.inputmethodservice.InputMethodService

/**
 * kp2acomposekeyboard 的输入法宿主入口。
 *
 * Plan 0.1 只创建最小 Service 类型，确保 Manifest 中的输入法入口有实际类承载。
 * Compose 输入视图、InputConnectionDispatcher 和最小输入验证将在后续阶段实现。
 */
class KeyboardImeService : InputMethodService()
