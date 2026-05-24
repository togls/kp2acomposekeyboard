# Architecture

## Overview

KP2A Compose Keyboard uses a single Gradle app module with explicit package boundaries. The app keeps Android platform hosting, Keepass2Android integration, runtime application use cases, pure domain rules, feature MVI state, and Compose rendering separated.

Primary design goals:

- Keep UI rendering state-driven and free of sensitive values.
- Keep `InputMethodService` focused on Android IME hosting.
- Keep Keepass2Android protocol details isolated in data adapters.
- Keep field classification and settings models pure.
- Keep sensitive session access behind small application use cases.

## Module Overview

```text
domain/
  field/
  keyboard/
  policy/
  session/
  settings/
application/
  keyboard/
  session/
  settings/
data/
  kp2a/
  session/
  settings/
platform/
  ime/
  input/
feature/
  keyboard/
  entrypicker/
  settings/
ui/
  keyboard/
  theme/
security/
di/
```

Domain code contains pure models and policies only. It must not depend on Android, Compose, DataStore, Hilt, or the Keepass2Android SDK.

Data code owns external protocol and persistence implementations, including Keepass2Android mapping, DataStore settings, and in-memory session storage.

Application code owns focused keyboard/session use cases that protect the sensitive session boundary. It is intentionally small and does not wrap every repository method.

Platform code owns Android IME hosting, InputConnection dispatching, subtype synchronization, and platform activity launches.

Feature code owns MVI state, intents, effects, and view models.

UI code owns Compose rendering and receives only safe state.

## Keyboard Input Flow

```text
Compose UI
    -> KeyboardIntent
KeyboardViewModel
    -> KeyboardEffect
KeyboardImeService
    -> InputConnectionDispatcher
    -> InputConnection
```

`KeyboardViewModel` receives `KeyboardIntent`, updates `KeyboardUiState`, and emits `KeyboardEffect`. It observes safe session snapshots through `ObserveKeyboardSessionSnapshotUseCase` and commits entry fields through `CommitKeyboardFieldUseCase`.

`KeyboardImeService` hosts the Compose input view, collects effects, starts platform activities, and delegates editor operations to `InputConnectionDispatcher`.

`InputConnectionDispatcher` sends text, delete, and enter actions to the active input connection. It must not log committed text.

## Entry Selection Flow

```text
KeyboardIntent.SelectEntry
    -> KeyboardEffect.LaunchEntryPicker
KeyboardImeService
    -> EntryPickerActivity
    -> Keepass2Android Plugin SDK2
    -> Kp2aEntryResultParser
    -> Kp2aEntryMapper
    -> KeyboardSessionRepository
    -> KeyboardViewModel observes Session
    -> KeyboardUiState
```

The project uses Keepass2Android Plugin SDK2 integration points such as `PluginAccessBroadcastReceiver`, `AccessManager`, `Kp2aControl`, `Strings`, and `KeepassDefs`.

Manual entry selection uses:

```kotlin
Kp2aControl.getQueryEntryIntent(null)
```

This intentionally does not pass `androidapp://packageName` by default. When launched from an IME flow, some Android, MIUI, or HyperOS builds may report a launcher or system component package, which is not suitable for manual credential lookup.

## Session Boundary

Sensitive field values are allowed only in:

```text
KeyboardSessionRepository
KeyboardSession
KeyboardField.value
```

UI-safe projection:

```text
KeyboardSession
    -> toSnapshot()
KeyboardSessionSnapshot
    -> KeyboardUiState
```

`KeyboardSessionSnapshot` and `KeyboardFieldSummary` must not contain field values.

## Field Commit Flow

```text
FieldButton
    -> KeyboardIntent.CommitField(fieldId)
KeyboardViewModel
    -> CommitKeyboardFieldUseCase
    -> KeyboardSessionRepository.getFieldValue(fieldId)
    -> KeyboardEffect.CommitText(value)
KeyboardImeService
    -> InputConnectionDispatcher.commitText(value)
```

The raw field value leaves the session only at commit time. It is never copied into `KeyboardUiState`, Compose UI state, logs, documentation, screenshots, DataStore, or clipboard.

## Settings Flow

```text
SettingsScreen
    -> SettingsIntent
SettingsViewModel
    -> SettingsRepository
    -> DataStore Preferences
```

Keyboard runtime code depends on `application.settings.KeyboardSettingsStore`, a small port that exposes settings observation and quick-action slot updates. `data.settings.SettingsRepository` implements that port and owns DataStore persistence.

Settings may store theme mode, dynamic color, session timeout, keyboard height, subtype enablement, haptic feedback, key sound, key preview, and quick-action slots.

Settings must never contain Keepass2Android field values, raw entry JSON, access tokens, or committed text.

## UI Responsibilities

`KeyboardImeContent` and keyboard UI components:

- Render `KeyboardUiState`.
- Send `KeyboardIntent`.
- Do not access `InputConnection`.
- Do not access repositories.
- Do not access Keepass2Android.
- Do not receive or display field values.

The keyboard surface has bounded height through `KeyboardHeightMode` and shared adaptive metrics. Entry layouts keep the current entry header and bottom actions stable while field-heavy areas scroll internally.

`KeyboardTheme` wraps Material 3 and is shared by the IME keyboard UI, entry picker UI, and settings UI.

## Logging

Logging must use `SecureLog`.

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
- Committed text

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

This project is primarily built for personal devices and personal needs. It does not guarantee compatibility with all Android, HyperOS, MIUI, or vendor ROM versions.
