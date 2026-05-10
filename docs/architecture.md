# Architecture

## Overview

KP2A Compose Keyboard uses a layered architecture with a strict separation between UI state, platform input, Keepass2Android integration, and in-memory sensitive data.

The main design goals are:

- Keep UI rendering simple and state-driven.
- Keep `InputMethodService` responsible only for platform-level IME behavior.
- Keep sensitive values out of UI state.
- Keep Keepass2Android protocol details isolated.
- Keep settings persistence separate from keyboard behavior.

## Module Overview

```text
ime/
├─ KeyboardImeService
├─ InputConnectionDispatcher
└─ KeyboardViewModelFactory

feature/keyboard/
├─ KeyboardViewModel
├─ KeyboardIntent
├─ KeyboardEffect
├─ KeyboardUiState
├─ MainKeyboardLayout
├─ DefaultInputMode
├─ EntryFieldDisplayMode
└─ ScrollDirection

feature/entrypicker/
├─ EntryPickerActivity
├─ EntryPickerViewModel
├─ EntryPickerScreen
├─ EntryPickerIntent
├─ EntryPickerEffect
└─ EntryPickerUiState

feature/settings/
├─ SettingsActivity
├─ SettingsViewModel
├─ SettingsScreen
├─ SettingsIntent
├─ SettingsEffect
├─ SettingsUiState
├─ SettingsRepository
├─ KeyboardSettings
├─ KeyboardThemeMode
└─ KeyboardHeightMode

kp2a/
├─ Kp2aContract
├─ Kp2aEntryResult
├─ Kp2aEntryResultParser
├─ Kp2aEntryMapper
├─ Kp2aPluginAccess
├─ Kp2aActionReceiver
└─ Kp2aPluginAccessReceiver

session/
├─ KeyboardSession
├─ KeyboardSessionRepository
├─ KeyboardSessionSnapshot
├─ KeyboardSessionMappings
└─ SessionTimeoutController

domain/
├─ KeyboardField
├─ KeyboardFieldType
├─ KeyboardFieldUiModel
├─ KeyboardFieldClassifier
├─ KeyboardFieldMappings
└─ SensitiveFieldPolicy

ui/theme/
└─ KeyboardTheme

ui/keyboard/
├─ KeyboardRoot
├─ KeyboardBottomGap
├─ KeyboardNavigationBarSpacer
├─ entry/
│  ├─ EntryHeader
│  ├─ FieldButton
│  ├─ FixedFieldRow
│  ├─ ExtraFieldPagedPanel
│  └─ AllFieldsExpandedPanel
├─ key/
│  ├─ KeyboardKey
│  ├─ LetterKey
│  ├─ KeyboardActionKeys
│  └─ KeyboardTextKeys
├─ layout/
│  ├─ KeyboardContentArea
│  ├─ KeyboardWidthLayout
│  ├─ DefaultKeyboardLayout
│  ├─ EntryKeyboardLayout
│  ├─ LetterKeyboard
│  ├─ NumberKeyboard
│  └─ SymbolKeyboard
├─ row/
│  ├─ LetterRow
│  ├─ TextKeyRow
│  └─ EntryActionRows
├─ utility/
│  ├─ KeyboardUtilityItem
│  ├─ UtilityRow
│  ├─ UtilityPanel
│  ├─ UtilitySlot
│  ├─ UtilitySlotModels
│  └─ UtilityDragState
├─ style/
│  ├─ KeyboardKeyColors
│  └─ KeyboardKeyEmphasis
└─ token/
   ├─ KeyboardAdaptiveMetrics
   ├─ KeyboardHeight
   ├─ KeyboardInsets
   ├─ KeyboardMetrics
   └─ KeyboardOrientation
```

## Keyboard Input Flow

```text
Compose UI
    ↓ KeyboardIntent
KeyboardViewModel
    ↓ KeyboardEffect
KeyboardImeService
    ↓
InputConnectionDispatcher
    ↓
InputConnection.commitText()
```

### Responsibilities

`KeyboardRoot` and keyboard UI components:

- Render `KeyboardUiState`.
- Send `KeyboardIntent`.
- Do not access `InputConnection`.
- Do not access repositories.
- Do not access Keepass2Android.
- Do not receive or display field values.

`KeyboardRoot`:

- Owns the bounded IME surface height.
- Resolves portrait or landscape adaptive metrics.
- Keeps keyboard content above bottom gap and navigation bar spacer.

`KeyboardContentArea`:

- Hosts either the default layout or entry layout.
- Resolves default-layout key height from the actual available content height.
- Clips overflowing content so compact landscape screens do not expand the IME window.

`KeyboardWidthLayout`:

- Calculates standard key width from the available row width.
- Calculates flexible action key width after fixed keys and gaps are removed.
- Keeps letter, number, and symbol rows aligned through shared width logic.

`KeyboardViewModel`:

- Receives `KeyboardIntent`.
- Updates `KeyboardUiState`.
- Emits `KeyboardEffect`.
- Observes `KeyboardSessionRepository.session`.
- Converts safe session snapshots into UI state.
- Fetches field values only when handling `CommitField(fieldId)`.

`KeyboardImeService`:

- Hosts the Compose input view.
- Starts activities such as `EntryPickerActivity` and `SettingsActivity`.
- Collects `KeyboardEffect`.
- Delegates input operations to `InputConnectionDispatcher`.

`InputConnectionDispatcher`:

- Sends text to the active input target.
- Sends delete and enter actions.
- Must not log committed text.

## Entry Selection Flow

```text
KeyboardIntent.SelectEntry
    ↓
KeyboardEffect.LaunchEntryPicker
    ↓
KeyboardImeService
    ↓
EntryPickerActivity
    ↓
Keepass2Android Plugin SDK2
    ↓
Kp2aEntryResultParser
    ↓
Kp2aEntryMapper
    ↓
KeyboardSessionRepository.setSession()
    ↓
KeyboardViewModel observes session
    ↓
KeyboardUiState
    ↓
Entry keyboard layout
```

## Keepass2Android Integration

The project uses Keepass2Android Plugin SDK2.

Current integration points:

- `PluginAccessBroadcastReceiver`
- `AccessManager`
- `Kp2aControl`
- `Strings`
- `KeepassDefs`

`Kp2aPluginAccessReceiver` is responsible for plugin access scopes.

The manual entry selection flow uses:

```kotlin
Kp2aControl.getQueryEntryIntent(null)
```

This intentionally does not pass `androidapp://packageName` by default. On some ROMs, especially when launched from an IME flow, the current package may be a launcher or system component such as `com.miui.home`, which is not suitable for manual entry selection.

## Session Architecture

Sensitive field values are stored only in memory:

```text
KeyboardSessionRepository
└─ KeyboardSession
   └─ KeyboardField.value
```

UI-safe projection:

```text
KeyboardSession
    ↓ toSnapshot()
KeyboardSessionSnapshot
    ↓
KeyboardUiState
```

`KeyboardSessionSnapshot` and `KeyboardFieldUiModel` must not contain field values.

## Field Commit Flow

```text
FieldButton
    ↓ KeyboardIntent.CommitField(fieldId)
KeyboardViewModel
    ↓ sessionRepository.getFieldValue(fieldId)
KeyboardEffect.CommitText(value)
    ↓
KeyboardImeService
    ↓
InputConnectionDispatcher.commitText(value)
```

The field value is passed through the shortest possible path and is never stored in UI state.

## Settings Architecture

```text
SettingsActivity
    ↓
SettingsViewModel
    ↓
SettingsRepository
    ↓
DataStore Preferences
```

Settings currently include:

- Theme mode
- Dynamic color
- Session timeout
- Keyboard height
- Haptic feedback
- Key sound
- Key preview
- Utility slots

Settings must never contain Keepass2Android field values or access tokens.

`KeyboardUtilitySlots` is persisted through `SettingsRepository` as utility IDs only. It must not contain entry names, field labels, field values, KP2A JSON, access tokens, or committed text.

## Theme Architecture

`KeyboardTheme` wraps Material 3.

It supports:

- Light mode
- Dark mode
- System mode
- Android 12+ dynamic color
- Fallback to default Material 3 color schemes when dynamic color is unavailable

The same theme is used by:

- IME keyboard UI
- Entry picker UI
- Settings UI

## Keyboard Height, Width, and Orientation

The root keyboard surface has a bounded height based on `KeyboardHeightMode`.

Portrait and landscape share the same layout in P0.

`KeyboardAdaptiveMetrics` provides orientation-aware:

- Key height
- Key horizontal padding
- Key corner radius
- Bottom safe padding
- Maximum navigation-aware bottom padding

The selected `KeyboardHeightMode` scales key height while preserving the portrait or landscape baseline metrics.

`KeyboardWidthLayout` centralizes width calculation for rows that mix fixed standard keys and flexible action keys.

Landscape behavior:

- Uses compressed height.
- Uses smaller key height.
- Uses smaller bottom safe padding.
- Clips overflow.
- Keeps entry header fixed.
- Keeps bottom action rows fixed.
- Allows expanded field area to scroll internally.

A dedicated landscape layout is deferred to P1.

## Logging

Logging must use:

- `SecureLog`

Do not use raw `Log.d`, `println`, or `printStackTrace` for sensitive flows.

Allowed log content:

- Event names
- Result codes
- Whether data exists
- Extras key names
- Field count
- Protected field count
- Error type

Forbidden log content:

- Password values
- TOTP values
- Recovery codes
- Tokens
- Secrets
- Credentials
- Access tokens
- Raw KP2A entry JSON

## ROM Compatibility

IME behavior is heavily affected by device and ROM implementation details.

Known variable areas:

- IME window height
- Navigation bar insets
- Gesture navigation safe area
- Three-button navigation
- Activity launches from IME
- Activity result delivery
- IME lifecycle
- Background activity restrictions

The current architecture avoids relying on a single ROM behavior where possible, but full compatibility is not guaranteed.
