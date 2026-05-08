package io.github.togls.kp2acomposekeyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main input method host for KP2A Compose Keyboard.
 *
 * Stage 1.1 only establishes the service entry point that Android can discover
 * and bind as an IME. The actual Compose input view is implemented in stage 1.2.
 */
@AndroidEntryPoint
class KeyboardImeService : InputMethodService() {

    override fun onCreate() {
        super.onCreate()
        // Keep this method intentionally lightweight. ViewModel and Compose setup
        // will be introduced after the minimal IME host is verified by Android.
    }

    override fun onCreateInputView(): View? {
        // Stage 1.2 will return a ComposeView from here.
        return super.onCreateInputView()
    }
}
