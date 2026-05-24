package io.github.togls.kp2acomposekeyboard.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (SettingsIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings_title))
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(text = stringResource(R.string.settings_back))
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            if (state.isLoading) {
                LoadingContent()
            } else {
                SettingsContent(
                    settings = state.settings,
                    errorMessage = state.errorMessage,
                    onIntent = onIntent,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SettingsContent(
    settings: KeyboardSettings,
    errorMessage: String?,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        SectionTitle(text = stringResource(R.string.settings_section_appearance))

        ThemeModeSetting(
            selected = settings.themeMode,
            onSelected = { themeMode ->
                onIntent(SettingsIntent.ChangeThemeMode(themeMode))
            },
        )

        SwitchSettingRow(
            title = stringResource(R.string.settings_dynamic_color_title),
            description = stringResource(R.string.settings_dynamic_color_description),
            checked = settings.useDynamicColor,
            onCheckedChange = { enabled ->
                onIntent(SettingsIntent.ChangeDynamicColorEnabled(enabled))
            },
        )

        HeightModeSetting(
            selected = settings.keyboardHeightMode,
            onSelected = { heightMode ->
                onIntent(SettingsIntent.ChangeKeyboardHeightMode(heightMode))
            },
        )

        HorizontalDivider()

        SectionTitle(text = stringResource(R.string.settings_section_input_layouts))

        FixedEnabledSettingRow(
            title = stringResource(R.string.settings_entry_layout_title),
            description = stringResource(R.string.settings_entry_layout_description),
        )

        SwitchSettingRow(
            title = stringResource(R.string.settings_english_us_layout_title),
            description = stringResource(R.string.settings_english_us_layout_description),
            checked = settings.englishUsSubtypeEnabled,
            onCheckedChange = { enabled ->
                onIntent(SettingsIntent.ChangeEnglishUsSubtypeEnabled(enabled))
            },
        )

        HorizontalDivider()

        SectionTitle(text = stringResource(R.string.settings_section_security))

        SessionTimeoutSetting(
            seconds = settings.sessionTimeoutSeconds,
            onChange = { seconds ->
                onIntent(SettingsIntent.ChangeSessionTimeoutSeconds(seconds))
            },
        )

        HorizontalDivider()

        SectionTitle(text = stringResource(R.string.settings_section_input_feedback))

        SwitchSettingRow(
            title = stringResource(R.string.settings_haptic_feedback_title),
            description = stringResource(R.string.settings_haptic_feedback_description),
            checked = settings.hapticFeedbackEnabled,
            onCheckedChange = { enabled ->
                onIntent(SettingsIntent.ChangeHapticFeedbackEnabled(enabled))
            },
        )

        SwitchSettingRow(
            title = stringResource(R.string.settings_key_sound_title),
            description = stringResource(R.string.settings_key_sound_description),
            checked = settings.keySoundEnabled,
            onCheckedChange = { enabled ->
                onIntent(SettingsIntent.ChangeKeySoundEnabled(enabled))
            },
        )

        SwitchSettingRow(
            title = stringResource(R.string.settings_key_preview_title),
            description = stringResource(R.string.settings_key_preview_description),
            checked = settings.showKeyPreview,
            onCheckedChange = { enabled ->
                onIntent(SettingsIntent.ChangeKeyPreviewEnabled(enabled))
            },
        )

        HorizontalDivider()

        OutlinedButton(
            onClick = { onIntent(SettingsIntent.ResetToDefault) },
        ) {
            Text(text = stringResource(R.string.settings_reset_defaults))
        }
    }
}

@Composable
private fun ThemeModeSetting(
    selected: KeyboardThemeMode,
    onSelected: (KeyboardThemeMode) -> Unit,
) {
    SettingBlock(
        title = stringResource(R.string.settings_theme_mode_title),
        description = stringResource(R.string.settings_theme_mode_description),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemeChip(
                text = stringResource(R.string.settings_theme_mode_system),
                selected = selected == KeyboardThemeMode.System,
                onClick = { onSelected(KeyboardThemeMode.System) },
            )

            ThemeChip(
                text = stringResource(R.string.settings_theme_mode_light),
                selected = selected == KeyboardThemeMode.Light,
                onClick = { onSelected(KeyboardThemeMode.Light) },
            )

            ThemeChip(
                text = stringResource(R.string.settings_theme_mode_dark),
                selected = selected == KeyboardThemeMode.Dark,
                onClick = { onSelected(KeyboardThemeMode.Dark) },
            )
        }
    }
}

@Composable
private fun HeightModeSetting(
    selected: KeyboardHeightMode,
    onSelected: (KeyboardHeightMode) -> Unit,
) {
    SettingBlock(
        title = stringResource(R.string.settings_keyboard_height_title),
        description = stringResource(R.string.settings_keyboard_height_description),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemeChip(
                text = stringResource(R.string.settings_keyboard_height_compact),
                selected = selected == KeyboardHeightMode.Compact,
                onClick = { onSelected(KeyboardHeightMode.Compact) },
            )

            ThemeChip(
                text = stringResource(R.string.settings_keyboard_height_normal),
                selected = selected == KeyboardHeightMode.Normal,
                onClick = { onSelected(KeyboardHeightMode.Normal) },
            )

            ThemeChip(
                text = stringResource(R.string.settings_keyboard_height_tall),
                selected = selected == KeyboardHeightMode.Tall,
                onClick = { onSelected(KeyboardHeightMode.Tall) },
            )
        }
    }
}

@Composable
private fun SessionTimeoutSetting(
    seconds: Int,
    onChange: (Int) -> Unit,
) {
    SettingBlock(
        title = stringResource(R.string.settings_session_timeout_title),
        description = stringResource(R.string.settings_session_timeout_description),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Bounds mirror KeyboardSettings so the UI cannot request an invalid session lifetime.
            OutlinedButton(
                enabled = seconds > KeyboardSettings.MIN_SESSION_TIMEOUT_SECONDS,
                onClick = {
                    onChange(seconds - SESSION_TIMEOUT_STEP_SECONDS)
                },
            ) {
                Text(text = stringResource(R.string.settings_session_timeout_decrease))
            }

            Text(
                text = stringResource(R.string.settings_session_timeout_seconds, seconds),
                style = MaterialTheme.typography.titleMedium,
            )

            Button(
                enabled = seconds < KeyboardSettings.MAX_SESSION_TIMEOUT_SECONDS,
                onClick = {
                    onChange(seconds + SESSION_TIMEOUT_STEP_SECONDS)
                },
            ) {
                Text(text = stringResource(R.string.settings_session_timeout_increase))
            }
        }
    }
}

@Composable
private fun FixedEnabledSettingRow(
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = true,
            enabled = false,
            onCheckedChange = null,
        )
    }
}

@Composable
private fun SwitchSettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingBlock(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        content()
    }
}

@Composable
private fun SectionTitle(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
    )
}

@Composable
private fun ThemeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text = text)
        },
    )
}

private const val SESSION_TIMEOUT_STEP_SECONDS = 15
