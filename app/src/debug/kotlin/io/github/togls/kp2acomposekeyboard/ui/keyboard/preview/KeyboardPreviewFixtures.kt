package io.github.togls.kp2acomposekeyboard.ui.keyboard.preview

import androidx.compose.runtime.Composable
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel
import io.github.togls.kp2acomposekeyboard.feature.keyboard.DefaultInputMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilitySlots
import io.github.togls.kp2acomposekeyboard.feature.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.feature.keyboard.SettingsUtilityItemId
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardHeightMode
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardThemeMode
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardRoot
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
        KeyboardRoot(
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

internal fun previewDefaultKeyboardState(
    inputMode: DefaultInputMode = DefaultInputMode.Letters,
): KeyboardUiState {
    return KeyboardUiState(
        mainLayout = MainKeyboardLayout.Default,
        defaultInputMode = inputMode,
        currentEntryName = "GitHub Personal",
        hasActiveSession = true,
        isUppercase = false,
    )
}

internal fun previewUtilityPanelState(): KeyboardUiState {
    return previewDefaultKeyboardState().copy(
        hasActiveSession = false,
        currentEntryName = null,
        isUtilityPanelExpanded = true,
    )
}

internal fun previewRightUtilitySlotState(): KeyboardUiState {
    return previewDefaultKeyboardState().copy(
        hasActiveSession = false,
        currentEntryName = null,
        utilitySlots = KeyboardUtilitySlots(
            centerItemIds = emptyList(),
            rightItemId = SettingsUtilityItemId,
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
    fixedFields: List<KeyboardFieldUiModel>,
    extraFields: List<KeyboardFieldUiModel>,
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
private fun previewFixedFields(): List<KeyboardFieldUiModel> {
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
private fun previewExtraFields(): List<KeyboardFieldUiModel> {
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
): KeyboardFieldUiModel {
    return KeyboardFieldUiModel(
        id = id,
        label = label,
        type = type,
        sensitive = sensitive,
    )
}
