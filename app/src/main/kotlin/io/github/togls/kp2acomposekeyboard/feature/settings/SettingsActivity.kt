package io.github.togls.kp2acomposekeyboard.feature.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.platform.ime.KeyboardSubtypeSynchronizer
import io.github.togls.kp2acomposekeyboard.ui.theme.KeyboardTheme
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var subtypeSynchronizer: KeyboardSubtypeSynchronizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            val savedMessage = stringResource(R.string.settings_saved)
            val currentSavedMessage by rememberUpdatedState(savedMessage)

            KeyboardTheme(settings = state.settings) {
                LaunchedEffect(viewModel) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            SettingsEffect.ShowSavedMessage -> {
                                snackbarHostState.showSnackbar(currentSavedMessage)
                            }

                            is SettingsEffect.ShowError -> {
                                snackbarHostState.showSnackbar(effect.message)
                            }
                        }
                    }
                }

                LaunchedEffect(state.isLoading, state.settings.englishUsSubtypeEnabled) {
                    if (!state.isLoading) {
                        subtypeSynchronizer.synchronize(state.settings)
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
