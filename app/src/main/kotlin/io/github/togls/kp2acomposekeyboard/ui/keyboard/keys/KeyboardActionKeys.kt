package io.github.togls.kp2acomposekeyboard.ui.keyboard.keys

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.style.KeyboardKeyEmphasis

@Composable
internal fun DeleteKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = "⌫",
        contentDescription = "删除",
        onClick = { onIntent(KeyboardIntent.DeleteBackward) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun SettingsKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = "设置",
        contentDescription = "打开设置",
        onClick = { onIntent(KeyboardIntent.OpenSettings) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun SelectEntryKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = "选择条目",
        contentDescription = "选择 KeePass 条目",
        onClick = { onIntent(KeyboardIntent.SelectEntry) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun SwitchToDefaultLayoutKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = "默认布局",
        contentDescription = "切换到默认键盘布局",
        onClick = { onIntent(KeyboardIntent.SwitchToDefaultLayout) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun ClearEntryKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = "清除",
        contentDescription = "清除当前条目",
        onClick = { onIntent(KeyboardIntent.ClearEntry) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun EnterKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = "换行",
        contentDescription = "换行",
        onClick = { onIntent(KeyboardIntent.Enter) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}

@Composable
internal fun SpaceKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = "空格",
        contentDescription = "输入空格",
        onClick = { onIntent(KeyboardIntent.CommitText(" ")) },
    )
}

@Composable
internal fun PreviousPageKey(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = "上一页",
        contentDescription = "上一页",
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
    KeyboardKey(
        modifier = modifier,
        text = "下一页",
        contentDescription = "下一页",
        enabled = enabled,
        onClick = onClick,
    )
}

@Composable
internal fun ExpandFieldsKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = "全部",
        contentDescription = "展开全部字段",
        onClick = { onIntent(KeyboardIntent.ExpandFields) },
    )
}

@Composable
internal fun CollapseFieldsKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = "收起",
        contentDescription = "收起字段列表",
        onClick = { onIntent(KeyboardIntent.CollapseFields) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}