package io.github.togls.kp2acomposekeyboard.ui.keyboard.preview

import androidx.compose.runtime.Composable
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldSummary
import io.github.togls.kp2acomposekeyboard.domain.keyboard.TextInputMode
import io.github.togls.kp2acomposekeyboard.domain.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlots
import io.github.togls.kp2acomposekeyboard.domain.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.domain.keyboard.SettingsQuickActionId
import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardHeightMode
import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardThemeMode
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardImeContent
import io.github.togls.kp2acomposekeyboard.ui.theme.KeyboardTheme

/**
 * Wraps keyboard previews with the same theme and root surface used by the app.
 */
@Composable
internal fun KeyboardPreviewContent(
    state: KeyboardUiState,
    settings: KeyboardSettings = previewLightSettings(),
) {
    KeyboardTheme(settings = settings) {
        KeyboardImeContent(
            state = state,
            settings = settings,
            onIntent = {},
        )
    }
}

internal fun previewLightSettings(): KeyboardSettings {
    return KeyboardSettings(
        themeMode = KeyboardThemeMode.Light,
        useDynamicColor = false,
        keyboardHeightMode = KeyboardHeightMode.Normal,
    )
}

internal fun previewTallLightSettings(): KeyboardSettings {
    return KeyboardSettings(
        themeMode = KeyboardThemeMode.Light,
        useDynamicColor = false,
        keyboardHeightMode = KeyboardHeightMode.Tall,
    )
}

internal fun previewCompactLightSettings(): KeyboardSettings {
    return KeyboardSettings(
        themeMode = KeyboardThemeMode.Light,
        useDynamicColor = false,
        keyboardHeightMode = KeyboardHeightMode.Compact,
    )
}

internal fun previewDarkSettings(): KeyboardSettings {
    return KeyboardSettings(
        themeMode = KeyboardThemeMode.Dark,
        useDynamicColor = false,
        keyboardHeightMode = KeyboardHeightMode.Normal,
    )
}

internal fun previewTextInputKeyboardState(
    inputMode: TextInputMode = TextInputMode.Letters,
): KeyboardUiState {
    return KeyboardUiState(
        mainLayout = MainKeyboardLayout.TextInput,
        textInputMode = inputMode,
        currentEntryName = "GitHub Personal",
        hasActiveSession = true,
        isUppercase = false,
    )
}

internal fun previewQuickActionPanelState(): KeyboardUiState {
    return previewTextInputKeyboardState().copy(
        hasActiveSession = false,
        currentEntryName = null,
        isQuickActionPanelExpanded = true,
    )
}

internal fun previewRightQuickActionSlotState(): KeyboardUiState {
    return previewTextInputKeyboardState().copy(
        hasActiveSession = false,
        currentEntryName = null,
        quickActionSlots = KeyboardQuickActionSlots(
            centerItemIds = emptyList(),
            rightItemId = SettingsQuickActionId,
        ),
    )
}

internal fun previewEntryKeyboardState(
    displayMode: EntryFieldDisplayMode = EntryFieldDisplayMode.Paged,
): KeyboardUiState {
    return previewEntryKeyboardState(
        displayMode = displayMode,
        fixedFields = previewFixedFields(),
        extraFields = previewExtraFields(),
        currentEntryName = "GitHub Personal",
    )
}

internal fun previewLongLabelEntryKeyboardState(): KeyboardUiState {
    return previewEntryKeyboardState(
        displayMode = EntryFieldDisplayMode.Paged,
        fixedFields = listOf(
            previewField("username", "Very Long Username Label", KeyboardFieldType.Username),
            previewField("password", "Long Password Field Label", KeyboardFieldType.Password, true),
            previewField("totp", "One Time Password", KeyboardFieldType.Totp, true),
        ),
        extraFields = listOf(
            previewField("backup-1", "Backup Code", KeyboardFieldType.Recovery, true),
            previewField("backup-2", "Backup Code", KeyboardFieldType.Recovery, true),
            previewField("blank-label", "", KeyboardFieldType.Custom),
            previewField(
                "security-question",
                "Security Question With A Long Label",
                KeyboardFieldType.Custom,
                true
            ),
        ),
        currentEntryName = "Long Label Entry",
    )
}

internal fun previewEmptyEntryKeyboardState(
    displayMode: EntryFieldDisplayMode = EntryFieldDisplayMode.Paged,
): KeyboardUiState {
    return previewEntryKeyboardState(
        displayMode = displayMode,
        fixedFields = emptyList(),
        extraFields = emptyList(),
        currentEntryName = "Empty Entry",
    )
}

private fun previewEntryKeyboardState(
    displayMode: EntryFieldDisplayMode,
    fixedFields: List<KeyboardFieldSummary>,
    extraFields: List<KeyboardFieldSummary>,
    currentEntryName: String,
): KeyboardUiState {
    return KeyboardUiState(
        mainLayout = MainKeyboardLayout.Entry,
        entryFieldDisplayMode = displayMode,
        currentEntryName = currentEntryName,
        hasActiveSession = true,
        fixedFields = fixedFields,
        extraFields = extraFields,
        allFields = fixedFields + extraFields,
    )
}

/**
 * Fixed fields are always visible in the entry keyboard.
 */
private fun previewFixedFields(): List<KeyboardFieldSummary> {
    return listOf(
        previewField(
            id = "username",
            label = "Username",
            type = KeyboardFieldType.Username,
        ),
        previewField(
            id = "password",
            label = "Password",
            type = KeyboardFieldType.Password,
            sensitive = true,
        ),
        previewField(
            id = "totp",
            label = "TOTP",
            type = KeyboardFieldType.Totp,
            sensitive = true,
        ),
    )
}

/**
 * Extra fields use mixed label lengths to reveal truncation and spacing issues.
 */
private fun previewExtraFields(): List<KeyboardFieldSummary> {
    return listOf(
        previewField(
            id = "url",
            label = "Website",
            type = KeyboardFieldType.Url,
        ),
        previewField(
            id = "email",
            label = "Email",
            type = KeyboardFieldType.Email,
        ),
        previewField(
            id = "phone",
            label = "Phone",
            type = KeyboardFieldType.Phone,
        ),
        previewField(
            id = "recovery-code",
            label = "Recovery Code",
            type = KeyboardFieldType.Recovery,
            sensitive = true,
        ),
        previewField(
            id = "billing-address",
            label = "Billing Address",
            type = KeyboardFieldType.Address,
        ),
        previewField(
            id = "notes",
            label = "Notes",
            type = KeyboardFieldType.Notes,
        ),
        previewField(
            id = "security-question",
            label = "Security Question",
            type = KeyboardFieldType.Custom,
            sensitive = true,
        ),
        previewField(
            id = "custom-field",
            label = "Custom Field",
            type = KeyboardFieldType.Custom,
        ),
    )
}

private fun previewField(
    id: String,
    label: String,
    type: KeyboardFieldType,
    sensitive: Boolean = false,
): KeyboardFieldSummary {
    return KeyboardFieldSummary(
        id = id,
        label = label,
        type = type,
        sensitive = sensitive,
    )
}
