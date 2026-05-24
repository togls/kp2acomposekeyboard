# Requirements

## P0 Scope

P0 implements a minimal secure Keepass2Android-focused keyboard.

Included:

- Android input method host.
- Entry system subtype.
- Optional English (US) text input subtype.
- Text input layout for English letters, numbers, and symbols.
- Entry layout for selected KeePass fields.
- Keepass2Android entry selection.
- Field button input.
- In-memory session security layer.
- Automatic session timeout.
- Manual session clearing.
- Settings page.
- Material 3 theme.
- Quick-action bar and quick-action panel.
- Basic portrait / landscape support.
- Adaptive keyboard metrics.
- Navigation-aware bottom spacing.
- Basic unit and instrumentation tests.
- CI, nightly, and tagged release workflows.

Excluded from P0:

- Pinyin input.
- Candidate words.
- Word suggestions.
- Autocorrect.
- Handwriting.
- Voice input.
- Additional language layouts beyond English (US).
- Full Gboard feature parity.
- KeePassDX support.
- Dedicated landscape layout.
- Custom search result UI.
- Full password manager behavior.

## Text Input Layout

The text input layout supports:

- Letter mode.
- Number mode.
- Symbol mode.

Bottom action row:

```text
[?123/ABC] [Language when letters] [Space] [Select Entry] [Enter]
```

Requirements:

- Tapping letters commits letters.
- Tapping numbers commits numbers.
- Tapping symbols commits symbols.
- Tapping `Space` commits a space.
- Tapping `Enter` sends enter.
- Tapping delete removes the previous character.
- Tapping `Select Entry` starts the Keepass2Android entry selection flow.
- Tapping the language key switches from `Entry` to `English (US)` when English is enabled.
- If English is not enabled, tapping the language key delegates to Android's next input method behavior.
- Letter, number, and symbol rows use shared row metrics so fixed and flexible keys align consistently.

## Quick-action Requirements

The quick-action area appears above the main keyboard content.

Requirements:

- The left quick-action button expands or closes the quick-action panel.
- When an active session exists and the panel is closed, the center area shows the current entry hint.
- Tapping the current entry hint returns to the entry layout without querying Keepass2Android again.
- The panel occupies the main keyboard content area and leaves the bottom IME safe/navigation area untouched.
- Quick-action items can be tapped directly from the panel.
- Quick-action items can be long-pressed and dragged into the center area or right slot.
- Dragging updates hover feedback before release; the final slot change is committed on drag end.
- Pinned quick actions are capped by `KeyboardQuickActionSlots.MAX_PINNED_ITEMS`.
- Quick-action slot persistence stores only quick-action IDs.
- Quick-action persistence must never store entry names, field labels from KP2A, field values, or KP2A raw JSON.

Current production quick actions:

- Settings.
- Clear entry.

## Entry Keyboard Layout

The entry layout contains:

- Fixed field row.
- Extra field panel.
- Normal entry action row.
- Expanded field list and expanded action rows.

Fixed fields:

```text
Username
Password
TOTP
```

Normal mode:

- Fixed fields use one row when present.
- Extra fields use the remaining field area.
- The extra field area scrolls internally.
- The normal action row stays fixed at the bottom of the entry content.

Expanded mode:

- Displays fixed and extra fields together.
- Field area scrolls internally.
- Expanded action rows stay fixed at the bottom.
- Previous and next actions page through the scroll area.
- Scroll end snaps to page-sized offsets.
- Expanded fields must not increase the overall keyboard height.

## Field Key Requirements

Field keys must:

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

## Subtype Requirements

The IME uses a static `Entry` subtype and a dynamic `English (US)` subtype.

Requirements:

- `Entry` is always available through `method.xml`.
- `English (US)` is registered as an additional subtype only when enabled in app settings.
- Subtype IDs and extra values must stay stable.
- Unknown or missing Android subtype values must fall back to `Entry`.
- Subtype changes must update `KeyboardUiState.currentSubtype` and `KeyboardUiState.mainLayout`.
- Disabling `English (US)` should leave only the `Entry` subtype registered by the app.
- ROM subtype caching must be treated as a compatibility limitation, not as app state truth.

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
- English (US) subtype enablement.
- Haptic feedback.
- Key sound.
- Key preview.
- Quick-action slots.
- Reset to default.

Settings must be persisted using DataStore Preferences.

Settings may store only safe values:

- Enum names.
- Booleans.
- Bounded timeout seconds.
- Quick-action IDs.

Settings must not store:

- Entry field values.
- KP2A raw JSON.
- KP2A access tokens.
- Session data.
- Committed text.

## Theme Requirements

The app must use Material 3.

Theme support:

- Light mode.
- Dark mode.
- System mode.
- Android 12+ dynamic color.
- Fallback default Material 3 color schemes when dynamic color is unavailable.

The same theme system is used for:

- Keyboard UI.
- Entry picker UI.
- Settings UI.

## Keyboard Height Requirements

The keyboard must have a bounded height.

Requirements:

- Compact / Normal / Tall height modes.
- Entry fields must not increase overall keyboard height.
- Expanded mode must scroll field content internally.
- Bottom action rows must stay fixed.
- Bottom action rows must avoid gesture navigation / navigation bar areas.
- Landscape mode uses compressed height.
- Key height, horizontal padding, corner radius, bottom safe padding, and navigation-aware bottom padding must come from adaptive keyboard metrics when possible.
- Row height must be snapped down by density when needed to avoid cumulative pixel overflow.

## Portrait / Landscape Requirements

P0 requirements:

- Portrait layout must be usable.
- Landscape must not crash.
- Landscape can use the same layout with compressed height.
- Landscape key height and spacing can be reduced.
- Dedicated landscape layout is deferred to P1.

## Build and Release Requirements

Local debug builds must work without signing secrets.

Release and nightly builds require signing inputs:

```text
ANDROID_KEYSTORE_PATH
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

GitHub Actions additionally require this secret so the keystore can be reconstructed on the runner:

```text
ANDROID_KEYSTORE_BASE64
```

Requirements:

- CI runs unit tests, debug APK build, and debug lint.
- Nightly builds run unit tests and publish a signed release APK artifact.
- Release builds run on tags matching `v*.*.*` and publish a signed APK to GitHub Releases.
- Keystore files and `keystore.properties` must not be committed.

## Logging Requirements

Allowed:

- Event names.
- Result codes.
- Whether data exists.
- Extras key names.
- Field count.
- Protected field count.
- Subtype IDs or names.
- Error type.

Forbidden:

- Password values.
- TOTP values.
- Recovery code values.
- Token values.
- Secret values.
- Access token values.
- Raw KP2A field JSON.
- Full Intent extras values.
- Committed text.

## Testing Requirements

Unit tests should cover:

- KP2A result parsing.
- Field classification.
- Sensitive field detection.
- KP2A result mapping.
- Commit-field behavior.
- Session snapshot safety.
- Settings defaults and bounds.
- Settings persistence codecs.
- Runtime subtype registry behavior.
- Quick-action reducer and UI policies.
- Keyboard layout metrics and paging math.

Instrumentation tests should cover:

- Text input layout behavior.
- Entry layout behavior.
- Sensitive data non-display guarantees.
- Height and inset behavior on device/emulator.
