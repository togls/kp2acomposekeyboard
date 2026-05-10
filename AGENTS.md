# AGENTS.md

This file defines the working rules for AI coding agents in this repository.

## Project Context

This project is **KP2A Compose Keyboard**, an Android input method for Keepass2Android.

Primary goals:

- Provide a Jetpack Compose based IME.
- Let the user choose an entry from Keepass2Android through Plugin SDK2.
- Let the user input selected entry fields through `InputConnection.commitText()`.
- Avoid clipboard based password transfer.
- Keep sensitive values out of UI state, logs, persistent storage, and documentation.

Core stack:

- Kotlin
- Android InputMethodService
- Jetpack Compose
- Material 3
- Hilt
- DataStore Preferences
- Keepass2Android Plugin SDK2
- MVI / unidirectional data flow
- JUnit / Robolectric

## Language and Communication

- All user-facing replies must be in Chinese.
- Implementation plans, task lists, progress summaries, and review notes must be in Chinese.
- Code, identifiers, comments, commit messages, and technical docs may use English when it improves clarity or tooling compatibility.
- Be direct and factual. Correct mistakes clearly.
- Do not exaggerate certainty. State uncertainty when behavior depends on Android version, ROM behavior, or Keepass2Android internals.

## Workflow Rules

These workflow rules apply to the main/coordinator agent.

Before implementation, ask for confirmation when the task involves:

- Feature design
- Architecture changes
- Cross-file edits
- Multi-step implementation
- Security-sensitive changes
- Keepass2Android integration changes
- InputMethodService lifecycle changes

For small, clearly scoped fixes, implement directly.

For larger work:

1. Clarify requirements and constraints.
2. Propose the minimal design.
3. Break work into small plans.
4. Wait for user approval.
5. Execute one plan at a time.
6. After each plan, summarize changed files and validation steps.

If requirements or constraints change during implementation, update the plan first.

Subagents executing an already approved task should not restart the proposal/approval flow. They should only escalate if the approved plan is no longer valid.

## Engineering Principles

Use these principles in order:

1. Correctness and safety.
2. Simplicity.
3. Maintainability.
4. Testability.
5. Extensibility only when there is a clear need.

Prefer KISS:

- Avoid over-engineering.
- Avoid abstractions without immediate value.
- Keep implementation explicit and readable.
- Prefer small focused classes/functions.

Use pragmatic SOLID:

- Separate platform code from business logic.
- Keep UI state separate from sensitive domain values.
- Do not introduce interfaces unless they improve testing, decoupling, or future replacement.

## Code Style

General rules:

- Prefer Kotlin idioms, but do not sacrifice clarity.
- Prefer explicit error handling.
- Do not swallow exceptions silently.
- Add logs only for useful state transitions or failures.
- Use `rg` instead of `grep` when searching.

Size guidelines:

- Keep files under 1000 lines when reasonable.
- Keep functions under 50 lines when reasonable.
- Split code when a function has multiple responsibilities.

Comments:

- Do not add comments that repeat what the code obviously does.
- Add comments only for non-obvious logic.
- Comments should explain **why**, not **what**.
- Add background comments for API compatibility, vendor ROM differences, Android lifecycle behavior, and security boundaries.

Examples of good comments:

```kotlin
// InputMethodService is not an Activity context; launching UI from IME requires a new task.
intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
```

```kotlin
// IME windows may already avoid the navigation bar on some ROMs; clamp the inset to avoid double padding.
```

## Architecture Rules

Follow unidirectional data flow:

```text
Compose UI
    -> Intent
ViewModel
    -> State / Effect
IME or Activity
    -> platform action
```

Keyboard input flow:

```text
Keyboard UI
    -> KeyboardIntent
KeyboardViewModel
    -> KeyboardEffect
KeyboardImeService
    -> InputConnectionDispatcher
    -> InputConnection
```

Entry selection flow:

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

Settings flow:

```text
SettingsScreen
    -> SettingsIntent
SettingsViewModel
    -> SettingsRepository
    -> DataStore Preferences
```

## Sensitive Data Rules

This is a security-sensitive project. Treat all Keepass2Android field values as sensitive unless proven otherwise.

Sensitive values include:

- Password
- TOTP / OTP
- Recovery code
- Token
- Access token
- Secret
- API key
- Private key
- Credential value
- Keepass2Android raw entry JSON

Allowed to hold sensitive field values:

```text
KeyboardSessionRepository
KeyboardSession
KeyboardField.value
```

Not allowed to hold sensitive field values:

```text
KeyboardUiState
KeyboardFieldUiModel
Compose UI
SettingsRepository
DataStore
SharedPreferences
Logs
Crash reports
Docs
Screenshots
```

Field buttons must only use:

- `id`
- `label`
- `type`
- `sensitive`

Field buttons must never display or log `value`.

Do not use clipboard for field transfer. Use:

```kotlin
InputConnection.commitText(value, 1)
```

## Logging Rules

Use the project logging wrapper only:

- `SecureLog`

Do not use raw logging in new code:

```kotlin
Log.d(...)
println(...)
printStackTrace()
```

`SecureLog` is debug-build-only and must be structured.

Do not interpolate sensitive or contextual values directly into the `message` string:

```kotlin
// Bad
SecureLog.d("launch query: searchText=$searchText")
```

Use structured fields instead, and avoid sensitive values:

```kotlin
// Good
SecureLog.d(
    message = "launch kp2a query entry",
    "queryMode" to "manual",
    "hasQuery" to !searchText.isNullOrBlank(),
)
```

Intent logging may list extra keys, but must not log extra values.

Never log:

- `KeyboardField.value`
- `KeyboardEffect.CommitText.text`
- `Strings.EXTRA_ENTRY_OUTPUT_DATA`
- access tokens
- raw KP2A result JSON

## Keepass2Android Integration Rules

Use Keepass2Android Plugin SDK2 where possible:

- `PluginAccessBroadcastReceiver`
- `AccessManager`
- `Kp2aControl`
- `Strings`
- `KeepassDefs`

Do not duplicate SDK access-token logic unless there is a proven need.

`Kp2aPluginAccessReceiver` should be minimal and should override `getScopes()`.

For manual entry selection, prefer:

```kotlin
Kp2aControl.getQueryEntryIntent(null)
```

Do not default to:

```text
androidapp://<package>
```

Reason: when launched from an IME flow, Android/MIUI/HyperOS may report the launcher or system UI package, for example `com.miui.home`, which causes Keepass2Android to search for the wrong credential.

Only use `androidapp://<package>` for a deliberate “match current app” mode.

When requesting plugin access, request only required scopes. Start with:

- `Strings.SCOPE_CURRENT_ENTRY`
- `Strings.SCOPE_QUERY_CREDENTIALS`

Only add `Strings.SCOPE_QUERY_CREDENTIALS_FOR_OWN_PACKAGE` when using the own-package query API.

## IME and Android Lifecycle Rules

InputMethodService lifecycle differs across Android versions and vendor ROMs.

Be careful with:

- `onCreateInputView`
- `onStartInputView`
- `onFinishInputView`
- `onDestroy`
- launching Activity from IME
- session cleanup during EntryPicker flow

When launching Activity from IME, use:

```kotlin
Intent.FLAG_ACTIVITY_NEW_TASK
```

Do not clear the active session when IME is temporarily destroyed because the user is selecting a KP2A entry. User cancellation must preserve the previous session.

Session cleanup should occur on:

- timeout
- manual clear
- normal IME destruction
- replacement by a newly selected entry

## UI Rules

Use `KeyboardTheme` for Material 3 theming.

Support:

- light mode
- dark mode
- system mode
- Android 12+ dynamic color
- default Material 3 fallback color schemes

Use shared keyboard components:

- `KeyboardKey`
- `FieldButton`
- `TextKeyRow`
- `KeyboardWidthLayout`
- `KeyboardContentArea`

Keyboard UI should support:

- rounded keys
- consistent spacing
- press feedback
- disabled state
- action-button visual hierarchy
- cautious visual styling for sensitive field buttons

Sensitive field buttons may look different, but must not display field values.

Keyboard height must be bounded. Field-heavy layouts must not expand the IME window indefinitely.

Use `KeyboardAdaptiveMetrics` for orientation-aware key height, key padding, corner radius, bottom safe padding, and navigation-aware bottom padding. Do not hard-code new keyboard sizing tokens in feature components when an existing token or adaptive metric fits.

Use `KeyboardWidthLayout` when a row mixes standard-width keys with flexible action keys, so row alignment stays consistent across letter, number, and symbol layouts.

Entry layout requirements:

- current entry header fixed at top
- bottom action row fixed at bottom
- expanded field area scrolls internally
- fields do not push bottom action buttons into navigation bars

Account for modern Android navigation:

- gesture navigation
- three-button navigation
- vendor ROM IME inset differences

Do not blindly apply full `navigationBarsPadding()` to the whole IME window. Use clamped bottom safe padding to avoid double inset issues.

## Settings Rules

Settings are stored with DataStore Preferences.

Allowed settings:

- theme mode
- dynamic color
- session timeout seconds
- keyboard height mode
- haptic feedback
- key sound
- key preview

Do not store sensitive KP2A values in DataStore.

Session timeout must stay within the configured safe range.

## Testing Rules

Run unit tests after logic changes:

```bash
./gradlew :app:testDebugUnitTest
```

Run debug build after UI or Android integration changes:

```bash
./gradlew :app:assembleDebug
```

Use Robolectric only for Android framework dependent unit tests.

If Robolectric fails because of unsupported SDK, pin SDK for the test:

```kotlin
@Config(sdk = [35])
```

or use:

```properties
sdk=35
```

Prefer unit tests for:

- KP2A result parsing
- field classification
- sensitive field policy
- KP2A result mapping
- session snapshot value stripping
- settings boundaries

Do not write tests that assert or print real passwords or tokens.

## Documentation Rules

Keep documentation in English unless the user explicitly asks otherwise.

Document:

- current architecture
- known limitations
- security constraints
- tested devices
- ROM compatibility disclaimer
- setup and validation commands

Always include the disclaimer:

```text
This project is primarily built for personal devices and personal needs. It does not guarantee compatibility with all Android, HyperOS, MIUI, or vendor ROM versions.
```

## Build and Validation Commands

Common commands:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:installDebug
```

ADB checks:

```bash
adb logcat | rg "Kp2aKeyboardIme|AndroidRuntime|FATAL EXCEPTION"
```

Sensitive value log checks should never reveal actual values.

## Commit Message Style

Use Conventional Commits.

Format:

```text
type(scope): concise imperative summary

Explain why the change is needed and what user-facing or engineering problem it solves.

* **Area**: Describe the concrete change.
* **Area**: Describe another concrete change if needed.
```

Rules:

- Use lowercase `type(scope)`.
- Keep the subject concise and imperative.
- Prefer clear scopes, for example `keyboard`, `ime`, `kp2a`, `settings`, `theme`, `session`, `docs`, `test`.
- Explain the reason for the change in the body.
- Use bullet points for notable changed areas.
- Do not include noisy implementation details unless they help future maintenance.
- Do not include sensitive values, tokens, passwords, KP2A field values, or logs containing secrets.

Common types:

```text
feat
fix
refactor
ui
docs
test
chore
build
```

Example:

```text
ui(keyboard): adjust keyboard bottom safe padding token

Increase the safety margin at the bottom of the keyboard UI to improve visual clearance and ergonomics relative to system navigation bars.

* **Keyboard UI Tokens**: Update `BottomSafePadding` in `KeyboardUiTokens.kt` from `10.dp` to `15.dp` to provide additional spacing from the gesture or three-button navigation area.
```

## Current Known Limitations

- The app is not a full general-purpose keyboard.
- Pinyin, suggestions, autocorrect, and candidate words are out of scope for P0.
- Landscape mode uses compressed shared layout for now.
- Field classification uses Keepass standard fields plus heuristics.
- ROM behavior may affect IME height, Activity launch, navigation insets, and lifecycle timing.
- Keepass2Android Plugin SDK2 is required.
- KeePassDX is not supported.
