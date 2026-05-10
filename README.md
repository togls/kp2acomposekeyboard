# KP2A Compose Keyboard

KP2A Compose Keyboard is an Android input method designed to work with Keepass2Android through the Keepass2Android Plugin SDK2.

The keyboard lets the user select a KeePass entry from Keepass2Android and then input selected entry fields directly into the active text field through `InputConnection`, without using the clipboard.

## Current Status

Implemented:

- Android `InputMethodService` host.
- Jetpack Compose keyboard UI.
- Material 3 theme.
- Light theme, dark theme, system theme, and Android 12+ dynamic color.
- Default keyboard layout: letters, numbers, and symbols.
- Entry keyboard layout: current entry header, fixed fields, paged extra fields, and expanded all-fields mode.
- Adaptive keyboard sizing for compact, normal, and tall height modes.
- Orientation-aware key metrics, bottom spacing, and navigation bar clearance.
- Shared key, field button, row, and width layout components for the keyboard UI.
- Keepass2Android Plugin SDK2 integration.
- Keepass2Android plugin access flow.
- Entry selection through Keepass2Android.
- Field mapping from KP2A result to in-memory session.
- Field input through `InputConnection.commitText()`.
- In-memory session repository.
- Default 60-second session timeout.
- Settings page.
- Keyboard height settings.
- Portrait layout and compressed landscape adaptation.
- Debug-only secure logging through the project logging wrapper.
- Basic unit tests for parsing, mapping, classification, sensitivity detection, settings, and safe snapshots.

## Project Goals

The project is designed around these constraints:

- Do not use the clipboard to transfer passwords or secrets.
- Do not show field values in the keyboard UI.
- Do not log passwords, TOTP codes, recovery codes, tokens, secrets, credentials, or access tokens.
- Do not persist entry field values to DataStore, SharedPreferences, files, or databases.
- Keep entry field values only in memory for a short period.
- Clear the active entry session automatically after a timeout.
- Allow the user to manually clear the session.

## Basic Usage

1. Install Keepass2Android.
2. Install KP2A Compose Keyboard.
3. Enable KP2A Compose Keyboard in Android input method settings.
4. Open the app from the launcher icon to configure keyboard settings.
5. Switch to KP2A Compose Keyboard in any text field.
6. Tap `[Select Entry]`.
7. Grant plugin access in Keepass2Android if prompted.
8. Select an entry in Keepass2Android.
9. Return to the keyboard.
10. Tap field buttons such as `[Username]`, `[Password]`, `[TOTP]`, or custom fields to input values.

## Keyboard Layouts

Keyboard UI is split into focused Compose components under `ui/keyboard/`:

- `KeyboardRoot` owns the bounded IME surface, orientation-aware metrics, and bottom system spacing.
- `KeyboardContentArea` resolves content-height-dependent key sizing.
- `DefaultKeyboardLayout` hosts letter, number, and symbol input modes.
- `EntryKeyboardLayout` hosts selected-entry field actions.
- `KeyboardWidthLayout` centralizes row width calculations so fixed and flexible keys align consistently.

### Default Layout

The default layout supports letter mode, number mode, and symbol mode.

Bottom action row:

```text
[?123/ABC] [#+=/ABC] [Space] [Select Entry] [Enter]
```

If an active session exists, the default layout shows a top hint:

```text
Current entry: {entryName} [Back to Entry Layout]
```

### Entry Layout

The entry layout supports:

- Current entry header.
- Fixed fields: Username, Password, and TOTP.
- Extra fields with paging.
- Expanded all-fields mode.
- Bottom action rows.

Sensitive field buttons use a cautious visual style, but still show only the field label, never the field value.

The entry layout keeps the current entry header and bottom action rows fixed while the expanded field area scrolls internally.

## Security Model

Field values are allowed only inside the in-memory session:

```text
KeyboardSessionRepository
└─ KeyboardSession
   └─ KeyboardField.value
```

Field values must not enter:

- `KeyboardUiState`
- `KeyboardFieldUiModel`
- Compose UI
- DataStore
- SharedPreferences
- Logcat
- Crash reports

See [`docs/security.md`](docs/security.md) for details.

## Settings

The settings page currently supports:

- Theme mode: System, Light, or Dark.
- Dynamic color.
- Session timeout.
- Keyboard height.
- Haptic feedback toggle.
- Key sound toggle.
- Key preview toggle.

Some settings may be reserved for later behavior integration.

Keyboard height is applied through bounded Compact, Normal, and Tall modes. The app also resolves orientation-aware key metrics for portrait and landscape instead of letting field-heavy layouts expand the IME window.

## Known Limitations

- The project is not a full general-purpose keyboard.
- It does not support pinyin, suggestions, autocorrect, candidate words, handwriting, or voice input.
- Landscape mode currently uses compressed shared layouts and metrics.
- Dedicated landscape layout is planned for a later phase.
- OEM ROMs may behave differently around IME window height, navigation bars, activity launching, and input method lifecycle.
- Keepass2Android Plugin SDK2 is required.
- KeePassDX is not supported by this project.
- Field classification for custom fields is heuristic and may need tuning.

See [`docs/known-limitations.md`](docs/known-limitations.md) for details.

## Build and Test

Build:

```bash
./gradlew :app:assembleDebug
```

Run unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Install debug build:

```bash
adb uninstall io.github.togls.kp2acomposekeyboard
./gradlew :app:installDebug
```

Open settings page directly:

```bash
adb shell am start -n io.github.togls.kp2acomposekeyboard/.feature.settings.SettingsActivity
```

## Log Safety Check

Runtime logs should not contain real field values.

Example check:

```bash
adb logcat | rg "password|token|secret|totp|otp|recovery"
```

The output may contain event names or labels, but must not contain actual secret values.
