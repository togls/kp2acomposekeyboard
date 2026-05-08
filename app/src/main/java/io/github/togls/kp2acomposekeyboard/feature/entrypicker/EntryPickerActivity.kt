package io.github.togls.kp2acomposekeyboard.feature.entrypicker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntryPickerActivity : ComponentActivity() {

    private val viewModel: EntryPickerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(viewModel) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            EntryPickerEffect.Finish -> finish()
                        }
                    }
                }

                EntryPickerScreen(
                    state = state,
                    onIntent = viewModel::onIntent,
                )
            }
        }
    }
}