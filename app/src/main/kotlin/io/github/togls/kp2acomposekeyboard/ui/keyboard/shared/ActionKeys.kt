package io.github.togls.kp2acomposekeyboard.ui.keyboard.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardTestTags
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardKeyEmphasis

@Composable
internal fun DeleteKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier,
        iconRes = R.drawable.ic_backspace_24,
        contentDescription = stringResource(R.string.cd_key_delete),
        onClick = { onIntent(KeyboardIntent.DeleteBackward) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun SettingsKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier,
        iconRes = R.drawable.ic_settings_24,
        contentDescription = stringResource(R.string.cd_key_open_settings),
        onClick = { onIntent(KeyboardIntent.OpenSettings) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun SelectEntryKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier,
        iconRes = R.drawable.ic_key_24,
        contentDescription = stringResource(R.string.cd_key_select_entry),
        onClick = { onIntent(KeyboardIntent.SelectEntry) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun SwitchToTextInputKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier,
        iconRes = R.drawable.ic_keyboard_24,
        contentDescription = stringResource(R.string.cd_key_switch_to_default_layout),
        onClick = { onIntent(KeyboardIntent.SwitchToTextInput) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun LanguageSwitchKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier.testTag(KeyboardTestTags.LanguageSwitchKey),
        iconRes = R.drawable.ic_language_24,
        contentDescription = stringResource(R.string.cd_key_switch_language),
        onClick = { onIntent(KeyboardIntent.SwitchLanguage) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun ClearEntryKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier,
        iconRes = R.drawable.ic_delete_sweep_24,
        contentDescription = stringResource(R.string.cd_key_clear_entry),
        onClick = { onIntent(KeyboardIntent.ClearEntry) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun EnterKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier,
        iconRes = R.drawable.ic_keyboard_return_24,
        contentDescription = stringResource(R.string.cd_key_enter),
        onClick = { onIntent(KeyboardIntent.Enter) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun SpaceKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier,
        iconRes = R.drawable.ic_space_bar_24,
        contentDescription = stringResource(R.string.cd_key_space),
        onClick = { onIntent(KeyboardIntent.CommitText(" ")) },
    )
}

@Composable
internal fun PreviousPageKey(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier.testTag(KeyboardTestTags.PreviousPage),
        iconRes = R.drawable.ic_navigate_before_24,
        contentDescription = stringResource(R.string.cd_key_previous_page),
        enabled = enabled,
        onClick = onClick,
    )
}

@Composable
internal fun NextPageKey(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier.testTag(KeyboardTestTags.NextPage),
        iconRes = R.drawable.ic_navigate_next_24,
        contentDescription = stringResource(R.string.cd_key_next_page),
        enabled = enabled,
        onClick = onClick,
    )
}

@Composable
internal fun ExpandFieldsKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier,
        iconRes = R.drawable.ic_expand_all_24,
        contentDescription = stringResource(R.string.cd_key_expand_fields),
        onClick = { onIntent(KeyboardIntent.ExpandFields) },
    )
}

@Composable
internal fun CollapseFieldsKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier,
        iconRes = R.drawable.ic_collapse_all_24,
        contentDescription = stringResource(R.string.cd_key_collapse_fields),
        onClick = { onIntent(KeyboardIntent.CollapseFields) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}
