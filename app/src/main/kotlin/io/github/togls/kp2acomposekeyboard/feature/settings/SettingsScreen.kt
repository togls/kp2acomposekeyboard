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
import androidx.compose.ui.unit.dp

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
                    Text(text = "键盘设置")
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(text = "返回")
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

        SectionTitle(text = "外观")

        ThemeModeSetting(
            selected = settings.themeMode,
            onSelected = { themeMode ->
                onIntent(SettingsIntent.ChangeThemeMode(themeMode))
            },
        )

        SwitchSettingRow(
            title = "动态颜色",
            description = "跟随系统动态颜色主题",
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

        SectionTitle(text = "安全")

        SessionTimeoutSetting(
            seconds = settings.sessionTimeoutSeconds,
            onChange = { seconds ->
                onIntent(SettingsIntent.ChangeSessionTimeoutSeconds(seconds))
            },
        )

        HorizontalDivider()

        SectionTitle(text = "输入反馈")

        SwitchSettingRow(
            title = "震动反馈",
            description = "点击按键时触发轻微震动",
            checked = settings.hapticFeedbackEnabled,
            onCheckedChange = { enabled ->
                onIntent(SettingsIntent.ChangeHapticFeedbackEnabled(enabled))
            },
        )

        SwitchSettingRow(
            title = "按键音",
            description = "点击按键时播放按键音",
            checked = settings.keySoundEnabled,
            onCheckedChange = { enabled ->
                onIntent(SettingsIntent.ChangeKeySoundEnabled(enabled))
            },
        )

        SwitchSettingRow(
            title = "按键预览",
            description = "点击按键时显示预览气泡",
            checked = settings.showKeyPreview,
            onCheckedChange = { enabled ->
                onIntent(SettingsIntent.ChangeKeyPreviewEnabled(enabled))
            },
        )

        HorizontalDivider()

        OutlinedButton(
            onClick = { onIntent(SettingsIntent.ResetToDefault) },
        ) {
            Text(text = "恢复默认设置")
        }
    }
}

@Composable
private fun ThemeModeSetting(
    selected: KeyboardThemeMode,
    onSelected: (KeyboardThemeMode) -> Unit,
) {
    SettingBlock(
        title = "主题模式",
        description = "控制设置页和键盘后续使用的主题模式",
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemeChip(
                text = "跟随系统",
                selected = selected == KeyboardThemeMode.System,
                onClick = { onSelected(KeyboardThemeMode.System) },
            )

            ThemeChip(
                text = "浅色",
                selected = selected == KeyboardThemeMode.Light,
                onClick = { onSelected(KeyboardThemeMode.Light) },
            )

            ThemeChip(
                text = "深色",
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
        title = "键盘高度",
        description = "后续会用于调整输入法整体高度",
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemeChip(
                text = "紧凑",
                selected = selected == KeyboardHeightMode.Compact,
                onClick = { onSelected(KeyboardHeightMode.Compact) },
            )

            ThemeChip(
                text = "标准",
                selected = selected == KeyboardHeightMode.Normal,
                onClick = { onSelected(KeyboardHeightMode.Normal) },
            )

            ThemeChip(
                text = "较高",
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
        title = "条目自动清理",
        description = "控制密码字段在内存 Session 中保留的最长时间",
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                enabled = seconds > KeyboardSettings.MIN_SESSION_TIMEOUT_SECONDS,
                onClick = {
                    onChange(seconds - SESSION_TIMEOUT_STEP_SECONDS)
                },
            ) {
                Text(text = "-15 秒")
            }

            Text(
                text = "$seconds 秒",
                style = MaterialTheme.typography.titleMedium,
            )

            Button(
                enabled = seconds < KeyboardSettings.MAX_SESSION_TIMEOUT_SECONDS,
                onClick = {
                    onChange(seconds + SESSION_TIMEOUT_STEP_SECONDS)
                },
            ) {
                Text(text = "+15 秒")
            }
        }
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