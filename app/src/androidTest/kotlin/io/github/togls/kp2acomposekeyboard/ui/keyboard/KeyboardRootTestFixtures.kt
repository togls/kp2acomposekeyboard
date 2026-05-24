package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldSummary
import io.github.togls.kp2acomposekeyboard.domain.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardSubtype
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.domain.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.ui.theme.KeyboardTheme

internal const val PASSWORD_SHOULD_NOT_APPEAR = "PASSWORD_SHOULD_NOT_APPEAR"
internal const val TOTP_SHOULD_NOT_APPEAR = "TOTP_SHOULD_NOT_APPEAR"
internal const val RECOVERY_CODE_SHOULD_NOT_APPEAR = "RECOVERY_CODE_SHOULD_NOT_APPEAR"

internal val forbiddenSensitiveValues = listOf(
    PASSWORD_SHOULD_NOT_APPEAR,
    TOTP_SHOULD_NOT_APPEAR,
    RECOVERY_CODE_SHOULD_NOT_APPEAR,
)

@Composable
internal fun KeyboardRootTestContent(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit = {},
) {
    val settings = KeyboardSettings(useDynamicColor = false)
    KeyboardTheme(settings = settings) {
        Box(modifier = Modifier.width(360.dp)) {
            KeyboardRoot(
                state = state,
                settings = settings,
                onIntent = onIntent,
            )
        }
    }
}

internal fun testDefaultState() = KeyboardUiState(
    mainLayout = MainKeyboardLayout.Default,
    currentSubtype = KeyboardSubtype.EnglishUs,
    englishUsSubtypeEnabled = true,
)

internal fun testEntryEmptyState() = KeyboardUiState(
    mainLayout = MainKeyboardLayout.Entry,
    currentSubtype = KeyboardSubtype.Entry,
    englishUsSubtypeEnabled = true,
    hasActiveSession = false,
)

internal fun testEntryState(
    displayMode: EntryFieldDisplayMode,
    extraFieldCount: Int = 8,
) = KeyboardUiState(
    mainLayout = MainKeyboardLayout.Entry,
    entryFieldDisplayMode = displayMode,
    currentEntryName = "Example",
    hasActiveSession = true,
    fixedFields = fixedFields(),
    extraFields = extraFields(extraFieldCount),
    allFields = fixedFields() + extraFields(extraFieldCount),
)

private fun fixedFields() = listOf(
    KeyboardFieldSummary("username", "Username", KeyboardFieldType.Username, sensitive = false),
    KeyboardFieldSummary("password", "Password", KeyboardFieldType.Password, sensitive = true),
    KeyboardFieldSummary("totp", "TOTP", KeyboardFieldType.Totp, sensitive = true),
)

private fun extraFields(count: Int) = List(count) { index ->
    KeyboardFieldSummary(
        id = "extra-$index",
        label = "Extra $index",
        type = KeyboardFieldType.Custom,
        sensitive = index % 2 == 0,
    )
}
