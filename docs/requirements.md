# Requirements

## P0 Scope

P0 implements a minimal secure Keepass2Android-focused keyboard.

Included:

- Android input method host.
- Default text input layout.
- Entry layout.
- Keepass2Android entry selection.
- Field button input.
- In-memory session security layer.
- Automatic session timeout.
- Manual session clearing.
- Settings page.
- Material 3 theme.
- Basic portrait / landscape support.
- Adaptive keyboard metrics.
- Navigation-aware bottom spacing.
- Basic unit tests.

Excluded from P0:

- Pinyin input.
- Candidate words.
- Word suggestions.
- Autocorrect.
- Handwriting.
- Voice input.
- Multi-language layouts.
- Full Gboard feature parity.
- KeePassDX support.
- Dedicated landscape layout.
- Custom search result UI.
- Full password manager behavior.

## Default Keyboard Layout

The default layout supports:

- Letter mode
- Number mode
- Symbol mode

Bottom action row:

```text
[?123/ABC] [#+=/ABC] [Space] [Select Entry] [Enter]
```

Optional quick-action row:

```text
[Quick-action panel] [Pinned quick actions or current entry hint] [Optional pinned quick action]
```

If an active session exists, the top area shows:

```text
Current entry: {entryName} [Back to Entry Layout]
```

Requirements:

- Tapping letters commits letters.
- Tapping numbers commits numbers.
- Tapping symbols commits symbols.
- Tapping `Space` commits a space.
- Tapping `Enter` sends enter.
- Tapping delete removes the previous character.
- Tapping `Select Entry` starts the Keepass2Android entry selection flow.
- Tapping `Settings` opens the settings page.
- Tapping the quick-action panel button expands or closes the quick-action panel.
- The quick-action panel occupies the main keyboard content area and leaves only the bottom IME safe/navigation area.
- Quick-action items can be tapped directly from the panel.
- Quick-action items can be long-pressed and dragged into the center area or right slot.
- Dragging only updates hover feedback until release; the final slot change is committed on drag end.
- The left quick-action panel button does not count toward the five pinned quick actions.
- If an active session exists, the center area shows the current entry hint while the quick-action panel is closed.
- Quick-action slot persistence stores only quick-action IDs and never stores entry or field values.
- If an active session exists, tapping `Back to Entry Layout` returns to the entry layout without querying Keepass2Android again.
- Letter, number, and symbol rows should use shared width calculation when mixing fixed and flexible keys.

## Entry Keyboard Layout

The entry layout contains:

- Current entry header
- Fixed field row
- Extra field panel
- Bottom action row

Fixed fields:

```text
Username
Password
TOTP
```

Extra fields:

- Displayed in pages.
- Default page size is 3.
- `prev` and `next` switch pages.
- `All` switches to expanded mode.

Expanded mode:

- Displays all fields.
- Field area scrolls internally.
- Current entry header stays fixed.
- Bottom action rows stay fixed.
- Expanded fields must not increase the overall keyboard height.

## Field Button Requirements

Field buttons must:

- Show only field labels.
- Never show field values.
- Commit values through `KeyboardIntent.CommitField(fieldId)`.
- Use cautious visual style for sensitive fields.
- Support disabled state when needed.
- Support consistent spacing, rounded corners, and press feedback.

Sensitive fields include:

- Password
- TOTP
- OTP
- Recovery Code
- Token
- Secret
- Credential
- API key
- Private key
- Fields marked as protected by Keepass2Android

## Keepass2Android Integration Requirements

The app uses Keepass2Android Plugin SDK2.

Required behavior:

- Register as a Keepass2Android plugin.
- Request required plugin scopes.
- Use `Kp2aControl.getQueryEntryIntent(null)` for manual entry selection.
- Parse returned entry data.
- Map returned fields into an in-memory `KeyboardSession`.
- Do not log raw KP2A result JSON.
- Do not persist returned field values.

Plugin scopes for the current manual query flow:

```text
SCOPE_CURRENT_ENTRY
SCOPE_QUERY_CREDENTIALS
```

`SCOPE_QUERY_CREDENTIALS_FOR_OWN_PACKAGE` should only be added if the app later implements an own-package query mode.

## Session Requirements

The session stores the currently selected entry in memory.

Requirements:

- A session contains entry id, entry name, and fields.
- Field values are stored only in memory.
- UI state receives only a safe snapshot without values.
- Default timeout is 60 seconds.
- Allowed timeout range is 15 to 300 seconds.
- User can manually clear the session.
- Session is cleared on normal IME destruction.
- Session must not be cleared accidentally while launching Keepass2Android selection.
- If the user cancels entry selection, the old session remains available.

## Settings Requirements

Settings page supports:

- Theme mode: System, Light, and Dark.
- Dynamic color.
- Keyboard height: Compact, Normal, and Tall.
- Session timeout.
- Haptic feedback.
- Key sound.
- Key preview.
- Reset to default.

Settings must be persisted using DataStore Preferences.

Settings must not store:

- Entry field values
- KP2A raw JSON
- KP2A access tokens
- Session data

## Theme Requirements

The app must use Material 3.

Theme support:

- Light mode
- Dark mode
- System mode
- Android 12+ dynamic color
- Fallback default Material 3 color schemes when dynamic color is unavailable

The same theme system is used for:

- Keyboard UI
- Entry picker UI
- Settings UI

## Keyboard Height Requirements

The keyboard must have a bounded height.

Requirements:

- Compact / Normal / Tall height modes.
- Entry fields must not increase overall keyboard height.
- Expanded mode must scroll field content internally.
- Header stays fixed.
- Bottom action rows stay fixed.
- Bottom action rows must avoid gesture navigation / navigation bar areas.
- Landscape mode uses compressed height.
- Key height, horizontal padding, corner radius, bottom safe padding, and navigation-aware bottom padding must come from adaptive keyboard metrics when possible.

## Portrait / Landscape Requirements

P0 requirements:

- Portrait layout must be usable.
- Landscape must not crash.
- Landscape can use the same layout with compressed height.
- Landscape key height and spacing can be reduced.
- Dedicated landscape layout is deferred to P1.

## Logging Requirements

Allowed:

- Event names
- Result codes
- Whether data exists
- Extras key names
- Field count
- Protected field count
- Error type

Forbidden:

- Password values
- TOTP values
- Recovery code values
- Token values
- Secret values
- Access token values
- Raw KP2A field JSON
- Full Intent extras values

## Testing Requirements

Unit tests should cover:

- KP2A result parsing
- Field classification
- Sensitive field detection
- KP2A result mapping
- Session snapshot safety
- Settings defaults and bounds

Integration and UI tests are deferred.
