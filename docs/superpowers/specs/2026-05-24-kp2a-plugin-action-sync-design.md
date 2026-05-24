# KP2A Plugin Action Session Sync Design

Date: 2026-05-24

## Context

KP2A Compose Keyboard currently creates keyboard sessions only through the manual entry picker flow:

```text
EntryPickerActivity
    -> Kp2aEntryResultParser
    -> Kp2aEntryMapper
    -> KeyboardSessionRepository
```

The app already exposes `Kp2aPluginAccessReceiver`, but it does not receive Keepass2Android plugin action broadcasts. As a result, the keyboard cannot react when KP2A opens an entry, closes an entry view, locks the database, or modifies an entry output field.

Session cleanup is also driven by a short fixed timeout in `SessionTimeoutController`. That can clear the keyboard while the KP2A entry is still open, which conflicts with the desired KP2A-driven session lifecycle.

## Goals

- Clear the keyboard session when KP2A closes the active entry view.
- Clear all keyboard-sensitive data when KP2A locks or closes the database.
- Treat KP2A plugin action events as the primary session lifecycle source.
- Keep a longer timeout only as a fallback for missed broadcasts or process leftovers.
- Sync a KP2A-opened entry into the keyboard session.
- Sync KP2A entry output modifications into the active keyboard session.
- Preserve the existing sensitive data boundary: raw field values stay only in `KeyboardSessionRepository`, `KeyboardSession`, and `KeyboardField.value`.

## Non-Goals

- Do not add clipboard-based transfer.
- Do not persist entry IDs, entry names, field IDs, field labels, field values, database paths, or raw KP2A JSON.
- Do not add KeePassDX support.
- Do not build a general macro/action system for KP2A entry actions.
- Do not change keyboard UI layout behavior except what naturally follows from session updates.

## Selected Approach

Use a thin `PluginActionBroadcastReceiver` subclass and delegate all sync behavior to a testable Kotlin handler.

```text
Keepass2Android broadcast
    -> Kp2aPluginActionReceiver
    -> Kp2aEntrySyncHandler
    -> Kp2aEntryResultParser / Kp2aEntryMapper
    -> KeyboardSessionRepository
    -> KeyboardViewModel observes safe snapshot
    -> KeyboardUiState
```

The receiver owns only Android broadcast adaptation. The handler owns event semantics, parsing, mapping, entry matching, session replacement, and clearing rules. The implementation should prefer the app's existing safe parser over SDK helpers that print parsing failures directly.

## Permission Scope

Add `Strings.SCOPE_DATABASE_ACTIONS` to the plugin access scopes in addition to the existing scopes:

```text
Strings.SCOPE_CURRENT_ENTRY
Strings.SCOPE_QUERY_CREDENTIALS
Strings.SCOPE_DATABASE_ACTIONS
```

Changing scopes can invalidate the existing KP2A plugin access token. After this change, users may need to re-grant plugin access in Keepass2Android once.

## Manifest Registration

Register a new exported receiver for KP2A plugin action broadcasts:

```text
keepass2android.ACTION_OPEN_ENTRY
keepass2android.ACTION_ENTRY_OUTPUT_MODIFIED
keepass2android.ACTION_CLOSE_ENTRY_VIEW
keepass2android.ACTION_LOCK_DATABASE
keepass2android.ACTION_CLOSE_DATABASE
keepass2android.ACTION_OPEN_DATABASE
keepass2android.ACTION_UNLOCK_DATABASE
```

`OPEN_DATABASE` and `UNLOCK_DATABASE` are registered so the receiver can safely observe lifecycle state, but they must not restore or create a keyboard session.

## Event Semantics

### Open Entry

`ACTION_OPEN_ENTRY` contains full entry output data.

Behavior:

- Parse the broadcast extras with the same safe parser used by manual selection.
- Map the result to `KeyboardSession`.
- Replace the current session only when mapping succeeds.
- Leave the previous session unchanged when the broadcast has no usable fields or malformed output.

### Entry Output Modified

`ACTION_ENTRY_OUTPUT_MODIFIED` contains full new entry output data plus the modified field ID.

Behavior:

- Parse and map the full new entry data.
- Create a session when no current session exists, because KP2A sends the complete modified entry output.
- Replace the current session only when the modified entry is the active session.
- If the current session has no `entryId`, allow replacement because older or variant KP2A flows may omit IDs.
- If both current and incoming sessions have IDs and they differ, ignore the event.
- Do not log the modified field ID because field identifiers can reveal user-defined field names.

### Close Entry View

`ACTION_CLOSE_ENTRY_VIEW` contains the closed entry ID.

Behavior:

- Clear the session when the closed entry ID matches the active session ID.
- If the active session has no `entryId`, clear conservatively because the app cannot prove the close event is unrelated.
- If both IDs exist and differ, ignore the close event.

### Lock Or Close Database

`ACTION_LOCK_DATABASE` and `ACTION_CLOSE_DATABASE` are security boundaries.

Behavior:

- Clear the session unconditionally.
- Do not log database file path or display name.

### Open Or Unlock Database

`ACTION_OPEN_DATABASE` and `ACTION_UNLOCK_DATABASE` are not enough to rebuild a safe session.

Behavior:

- Do not create or restore a keyboard session.
- Log only a safe event result if needed.

## Timeout Strategy

Use KP2A action broadcasts as the primary cleanup mechanism. Extend the automatic timeout so it becomes a fallback rather than the normal path.

Initial implementation:

- Replace the hard-coded 60-second runtime fallback with a 300-second fallback.
- Keep manual clear immediate.
- Keep normal IME destruction cleanup behavior.
- Keep cancellation of the entry picker preserving the previous session.
- Do not broaden the persisted settings range in this change.

The existing settings model already allows up to 300 seconds. A later change can expose a clearer UI label if needed.

## Security And Logging

All new code must use `SecureLog`.

Allowed log metadata:

- KP2A action category.
- Whether the event has an entry ID.
- Field count.
- Protected field count.
- Sync result type such as `applied`, `ignored`, or `cleared`.
- Error type when handling fails.

Forbidden log metadata:

- Raw KP2A JSON.
- Field values.
- Access tokens.
- Entry IDs.
- Field IDs.
- Field labels from KP2A.
- Database file paths.
- Database display names.
- Committed text.

The SDK's base receiver logs action names internally. New app code must not add raw Android logging.

## Error Handling

Parsing errors must degrade to an empty result through the existing parser behavior. The sync handler should avoid clearing a usable current session on malformed open or modified broadcasts.

Receiver-level failures should be caught at the receiver boundary and logged through `SecureLog.w` with only the error type. A bad broadcast must not crash the app process.

## Test Plan

Add focused unit tests for the sync handler:

- `openEntry` creates a session from valid KP2A output.
- `openEntry` keeps the previous session when output is invalid or empty.
- `entryOutputModified` replaces the active session when entry IDs match.
- `entryOutputModified` creates a session when no active session exists.
- `entryOutputModified` replaces when the current active session has no entry ID.
- `entryOutputModified` ignores a different entry ID.
- `closeEntryView` clears when entry IDs match.
- `closeEntryView` clears conservatively when the current session has no entry ID.
- `closeEntryView` ignores a different entry ID.
- `lockDatabase` clears unconditionally.
- `closeDatabase` clears unconditionally.
- `openDatabase` and `unlockDatabase` do not create or restore a session.
- Required plugin scopes include `SCOPE_DATABASE_ACTIONS`.

Run:

```bash
./gradlew :app:testDebugUnitTest
```

If implementation touches manifest or Android integration behavior, also run:

```bash
./gradlew :app:assembleDebug
```

## Documentation Updates

Update canonical docs after implementation:

- `docs/architecture.md`: add the plugin action sync flow and event semantics.
- `docs/security.md`: update session cleanup triggers and timeout description.
- `docs/testing.md`: add plugin action sync test coverage.
- `AGENTS.md`: update workflow rules only if the implemented behavior changes durable agent guidance.

## ROM Compatibility

KP2A plugin action broadcasts depend on Keepass2Android Plugin SDK2 behavior and the host app granting the requested scopes. Android, HyperOS, MIUI, and other vendor ROM behavior may still affect IME lifecycle, background broadcast delivery, and plugin settings flow.

This project is primarily built for personal devices and personal needs. It does not guarantee compatibility with all Android, HyperOS, MIUI, or vendor ROM versions.
