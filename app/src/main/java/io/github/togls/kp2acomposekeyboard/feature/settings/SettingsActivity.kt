package io.github.togls.kp2acomposekeyboard.feature.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.togls.kp2acomposekeyboard.ui.theme.KeyboardTheme

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            KeyboardTheme(settings = state.settings) {
                LaunchedEffect(viewModel) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            SettingsEffect.ShowSavedMessage -> {
                                snackbarHostState.showSnackbar("已保存")
                            }

                            is SettingsEffect.ShowError -> {
                                snackbarHostState.showSnackbar(effect.message)
                            }
                        }
                    }
                }

                SettingsScreen(
                    state = state,
                    snackbarHostState = snackbarHostState,
                    onIntent = viewModel::onIntent,
                    onBackClick = ::finish,
                )
            }
        }
    }
}