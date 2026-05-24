# Architecture

## Overview

KP2A Compose Keyboard uses one Android app module plus a local `kp2a-plugin-sdk` module. The app keeps Android platform hosting, Keepass2Android integration, runtime application use cases, pure domain rules, feature MVI state, and Compose rendering separated.

Primary design goals:

- Keep UI rendering state-driven and free of sensitive values.
- Keep `InputMethodService` focused on Android IME hosting and platform operations.
- Keep Keepass2Android protocol details isolated in data adapters.
- Keep field classification, subtype models, quick-action models, and settings models pure.
- Keep sensitive session access behind small application use cases.
- Keep documentation aligned with implemented code instead of historical planning notes.

## Package Overview

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
  plugin/
  settings/
ui/
  keyboard/
    entry/
    frame/
    metrics/
    quickactions/
    shared/
    textinput/
  theme/
security/
di/
```

Domain code contains pure models and policies only. It must not depend on Android, Compose, DataStore, Hilt, or the Keepass2Android SDK.

Data code owns external protocol and persistence implementations, including Keepass2Android mapping, DataStore settings, and in-memory session storage.

Application code owns focused keyboard/session/settings use cases that protect the sensitive session boundary. It is intentionally small and does not wrap every repository method.

Platform code owns Android IME hosting, `InputConnection` dispatching, runtime subtype synchronization, and platform activity launches.

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

`KeyboardImeService` hosts the Compose input view, collects effects, starts platform activities, switches subtypes, and delegates editor operations to `InputConnectionDispatcher`.

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

## KP2A Plugin Action Sync Flow

Keepass2Android can notify the plugin when the current entry or database state changes:

```text
Keepass2Android plugin action broadcast
    -> Kp2aPluginActionReceiver
    -> Kp2aEntrySyncHandler
    -> Kp2aEntryResultParser
    -> Kp2aEntryMapper
    -> KeyboardSessionRepository
    -> KeyboardViewModel observes Session
    -> KeyboardUiState
```

`ACTION_OPEN_ENTRY` maps full entry output into a new keyboard session. `ACTION_ENTRY_OUTPUT_MODIFIED` maps the full modified output and replaces the active session when the entry identity is matching or unavailable. `ACTION_CLOSE_ENTRY_VIEW` is ignored because Keepass2Android also sends it after a normal entry selection flow closes the entry activity. `ACTION_LOCK_DATABASE` and `ACTION_CLOSE_DATABASE` clear the session unconditionally. `ACTION_OPEN_DATABASE` and `ACTION_UNLOCK_DATABASE` never restore entry data. `ACTION_REVOKE_ACCESS` is handled by `Kp2aPluginAccessReceiver` and clears the active session after the SDK processes the access revocation.

The receiver must not log raw entry JSON, field values, entry IDs, field IDs, database paths, or database display names.

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
FieldKey
    -> KeyboardIntent.CommitField(fieldId)
KeyboardViewModel
    -> CommitKeyboardFieldUseCase
    -> KeyboardSessionRepository.getFieldValue(fieldId)
    -> KeyboardEffect.CommitText(value)
KeyboardImeService
    -> InputConnectionDispatcher.commitText(value)
```

The raw field value leaves the session only at commit time. It is never copied into `KeyboardUiState`, Compose UI state, logs, documentation, screenshots, DataStore, or clipboard.

## Subtype Flow

The IME uses a hybrid static-and-dynamic subtype model:

- `Entry` is declared statically in `app/src/main/res/xml/method.xml` with subtype ID `1001`.
- `English (US)` is built dynamically by `KeyboardSubtypeRegistry.englishUsInputMethodSubtype()` with subtype ID `1002` and extra value `layout=english_us`.
- `KeyboardSubtypeSynchronizer` calls `InputMethodManager.setAdditionalInputMethodSubtypes()` from settings and IME startup paths.
- On Android 14 and newer, it also calls `setExplicitlyEnabledInputMethodSubtypes()` so enabled subtype state follows the app setting when supported.
- `KeyboardImeService` reads `currentInputMethodSubtype` and sends `KeyboardIntent.ChangeSubtype` to the ViewModel.
- The language key switches from `Entry` to `English (US)` when English is enabled; otherwise it asks Android to switch to the next input method.

Subtype settings are safe to persist because they contain only booleans and stable subtype identifiers, not entry values.

## Settings Flow

```text
SettingsScreen
    -> SettingsIntent
SettingsViewModel
    -> SettingsRepository
    -> DataStore Preferences
```

Keyboard runtime code depends on `application.settings.KeyboardSettingsStore`, a small port that exposes settings observation and quick-action slot updates. `data.settings.SettingsRepository` implements that port and owns DataStore persistence.

Settings may store theme mode, dynamic color, session timeout, keyboard height, English (US) subtype enablement, haptic feedback, key sound, key preview, and quick-action slots.

Settings must never contain Keepass2Android field values, raw entry JSON, access tokens, or committed text.

## Quick-action Flow

```text
QuickActionBar / QuickActionPanel
    -> KeyboardIntent.ClickQuickAction / MoveQuickAction* / RemoveQuickAction
KeyboardViewModel
    -> KeyboardQuickActionSlotsReducer
    -> KeyboardSettingsStore.updateQuickActionSlots()
    -> DataStore Preferences
```

Quick actions are identified by `KeyboardQuickActionId.storageValue`. The reducer sanitizes unsupported IDs, removes duplicates, prevents the same action from occupying both center and right slots, and caps the total pinned count with `KeyboardQuickActionSlots.MAX_PINNED_ITEMS`.

Current production quick actions are Settings and Clear entry. Only quick-action IDs are persisted.

## UI Responsibilities

`KeyboardImeContent` owns the bounded IME surface, Material color usage, orientation detection, keyboard height mode, and adaptive metrics provider.

`KeyboardFrame` calculates layout metrics from `BoxWithConstraints`, bottom gap, navigation inset, density, and `KeyboardAdaptiveMetrics`. It provides `LocalKeyboardLayoutMetrics` so child layouts do not trigger measurement-state loops.

`KeyboardContentArea` hosts the quick-action bar, optional quick-action panel, text input layout, entry layout, and quick-action drag preview.

`TextInputKeyboardLayout` renders letters, numbers, and symbols. `EntryKeyboardLayout` renders fixed fields, extra fields, expanded fields, and entry action rows.

UI components must:

- Render `KeyboardUiState`.
- Send `KeyboardIntent`.
- Do not access `InputConnection`.
- Do not access repositories.
- Do not access Keepass2Android directly.
- Do not receive or display field values.

The keyboard surface has bounded height through `KeyboardHeightMode` and shared adaptive metrics. Field-heavy layouts scroll internally and must not increase the IME window height.

`KeyboardTheme` wraps Material 3 and is shared by the IME keyboard UI, entry picker UI, and settings UI.

## Build and Release Flow

The Gradle app module reads Android SDK versions from `gradle.properties` and release signing from either `keystore.properties` or environment variables.

Release signing environment variables:

```text
ANDROID_KEYSTORE_PATH
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

GitHub Actions decode `ANDROID_KEYSTORE_BASE64` into a temporary keystore file, then pass the release signing environment variables to Gradle.

Current workflows:

- `ci.yml`: unit tests, debug APK, and lint on main pushes and pull requests.
- `nightly.yml`: scheduled or manual signed release APK artifact.
- `release.yml`: signed release APK attached to `v*.*.*` tags.

## Logging

Logging must use `SecureLog`.

Do not use raw `Log.d`, `println`, or `printStackTrace` for sensitive flows.

Allowed log content:

- Event names
- Result codes
- Boolean flags
- Extras key names
- Field count
- Protected field count
- Subtype IDs or names
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
- Full Intent extras values

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
- Dynamic subtype caching in system settings
- Background activity restrictions

This project is primarily built for personal devices and personal needs. It does not guarantee compatibility with all Android, HyperOS, MIUI, or vendor ROM versions.
