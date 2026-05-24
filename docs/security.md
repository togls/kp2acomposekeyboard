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
- Raw Keepass2Android entry result JSON
- Text carried by `KeyboardEffect.CommitText`

Treat all Keepass2Android field values as sensitive unless proven otherwise.

## Allowed Storage

Field values are allowed only in memory:

```text
KeyboardSessionRepository
└─ KeyboardSession
   └─ KeyboardField.value
```

Safe persistent settings may include:

- Theme mode enum name.
- Dynamic color boolean.
- Keyboard height enum name.
- Session timeout seconds after bounds checking.
- English (US) subtype enabled boolean.
- Haptic feedback boolean.
- Key sound boolean.
- Key preview boolean.
- Quick-action IDs such as `settings`.

## Forbidden Storage

Field values must not be stored in:

- `KeyboardUiState`
- `KeyboardFieldSummary`
- Compose UI state
- DataStore
- SharedPreferences
- Files
- Databases
- Logs
- Crash reports
- Documentation
- Screenshots
- Quick-action slot preferences

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
CommitKeyboardFieldUseCase(fieldId)
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
KeyboardFieldSummary
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

The current-entry hint may show the selected entry name. It must never include field values or raw KP2A JSON.

## Quick-action Safety

Quick-action persistence stores only action IDs.

Allowed examples:

```text
settings
clear_entry
```

Forbidden quick-action data:

- Entry ids.
- Entry names.
- Field ids from KP2A.
- Field labels from KP2A.
- Field values.
- Raw KP2A JSON.
- Access tokens.

The quick-action reducer must sanitize unsupported IDs, remove duplicates, and enforce the maximum pinned count before values are persisted.

## Subtype and Settings Safety

Subtype settings are safe because they store only booleans and stable subtype identifiers. They must not include current app package credential queries, entry ids, field ids, or values.

`KeyboardSubtypeRegistry` may log subtype names or IDs. It must not log active field values or committed text.

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
- Subtype ID or subtype name

Forbidden log fields:

- Field values
- Committed text
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

Default runtime fallback timeout:

```text
300 seconds
```

Keepass2Android action broadcasts are the primary cleanup path. The timeout is a fallback for missed broadcasts or process leftovers.

Clear triggers:

- Keepass2Android locks the database.
- Keepass2Android closes the database.
- Keepass2Android revokes plugin access.
- Timeout fallback.
- Manual clear.
- Normal IME destruction.
- New successful entry selection or open-entry broadcast replacing the old session.

Do not clear the old session when:

- Launching Keepass2Android selection temporarily hides or destroys the IME.
- The user cancels entry selection.
- Entry selection fails before a new session is created.
- Keepass2Android closes the entry view after a normal entry selection.
- A modified entry broadcast clearly belongs to a different entry.

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

Do not log, copy, persist, or document access token values.

## Signing Secret Policy

Release signing uses local `keystore.properties` or CI secrets.

Sensitive signing data includes:

- Keystore file contents.
- Keystore Base64 content.
- Keystore password.
- Key alias when it reveals private naming.
- Key password.

These values must not be committed, logged, or copied into documentation examples with real values.

Use placeholders in docs:

```properties
storePassword=...
keyPassword=...
```
