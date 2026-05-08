package io.github.togls.kp2acomposekeyboard.ime

import androidx.lifecycle.ViewModelProvider
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardViewModel
import javax.inject.Inject

class KeyboardViewModelFactory @Inject constructor() {

    fun create(): KeyboardViewModel {
        return ViewModelProvider.NewInstanceFactory()
            .create(KeyboardViewModel::class.java)
    }
}