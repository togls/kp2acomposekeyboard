# Security

## Core Principles

KP2A Compose Keyboard follows these principles:

```text
Do not use the clipboard for secrets.
Do not display sensitive values in UI.
Do not log sensitive values.
Do not persist sensitive values.
Keep sensitive values only in memory for a short period.
Clear sensitive values automatically.
```

## Sensitive Data

Sensitive data includes:

- Passwords
- TOTP codes
- OTP codes
- Recovery codes
- Backup codes
- Tokens
- Access tokens
- Secrets
- Credentials
- API keys
- Private keys
- Protected fields reported by Keepass2Android

## Allowed Storage

Field values are allowed only in memory:

```text
KeyboardSessionRepository
└─ KeyboardSession
   └─ KeyboardField.value
```

## Forbidden Storage

Field values must not be stored in:

- `KeyboardUiState`
- `KeyboardFieldUiModel`
- Compose UI state
- DataStore
- SharedPreferences
- Files
- Databases
- Logs
- Crash reports

## Field Value Lifecycle

```text
Keepass2Android
    ↓
EntryPickerActivity
    ↓
Kp2aEntryResultParser
    ↓
Kp2aEntryMapper
    ↓
KeyboardSessionRepository
    ↓
KeyboardViewModel.commitField(fieldId)
    ↓
KeyboardEffect.CommitText(value)
    ↓
KeyboardImeService
    ↓
InputConnectionDispatcher
    ↓
InputConnection.commitText(value)
```

The value is passed through the shortest possible path and is not placed in UI state.

## UI Safety

The UI receives only safe field metadata:

```text
KeyboardFieldUiModel
├─ id
├─ label
├─ type
└─ sensitive
```

The UI must never receive:

```text
value
```

Sensitive fields may use a cautious visual style, but must still show only the label.

## Logging Rules

Use only:

- `SecureLog`

Allowed log fields:

- Event name
- Result code
- Boolean flags
- Error type
- Field count
- Protected field count
- Extras key names

Forbidden log fields:

- Field values
- KP2A raw JSON
- Passwords
- TOTP codes
- Recovery codes
- Tokens
- Secrets
- Credentials
- Access tokens
- Full Intent extras values

## SecureLog Rules

`SecureLog` is the only logging wrapper for new code. It logs only in debug builds.

`SecureLog.intent()` may log:

- Action
- Package
- Component
- Extras key names

It must not log extras values.

`SecureLog.bundleKeys()` may log:

- Bundle key names

It must not log values.

`SecureLog.entryFields()` may log:

- Field count
- Redacted sensitive fields
- Length of non-sensitive values

It must not log sensitive values.

Avoid putting dynamic values directly into the `message` string. Prefer structured fields:

```kotlin
SecureLog.d(
    message = "launch kp2a query entry",
    "queryMode" to "manual",
)
```

Avoid:

```kotlin
SecureLog.d("launch query: $searchText")
```

## Session Timeout

Default timeout:

```text
60 seconds
```

Allowed range:

```text
15 to 300 seconds
```

Clear triggers:

- Timeout
- Manual clear
- Normal IME destruction
- New successful entry selection replacing old session

Do not clear the old session when:

- Launching Keepass2Android selection temporarily hides or destroys the IME.
- The user cancels entry selection.
- Entry selection fails before a new session is created.

## Clipboard Policy

The app must not use the clipboard for passwords or field values.

Allowed input path:

```text
InputConnection.commitText(value)
```

Forbidden input path:

```text
ClipboardManager.setPrimaryClip(...)
```

## Keepass2Android Access Token

Keepass2Android Plugin SDK2 manages plugin access tokens.

Rules:

- Do not log access tokens.
- Do not expose access tokens in UI.
- Do not copy access tokens to custom logs.
- Prefer official `AccessManager` over custom token storage.

## Developer Checklist

Before merging changes involving field values:

- [ ] No field value in `KeyboardUiState`.
- [ ] No field value in `KeyboardFieldUiModel`.
- [ ] No field value in Compose UI.
- [ ] No raw KP2A JSON logs.
- [ ] No access token logs.
- [ ] No clipboard usage.
- [ ] Unit tests still pass.
- [ ] Runtime logs do not expose real field values.
