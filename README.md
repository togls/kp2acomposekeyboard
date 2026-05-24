# KP2A Compose Keyboard

KP2A Compose Keyboard is an Android input method designed to work with Keepass2Android through the Keepass2Android Plugin SDK2.

The keyboard lets the user select a KeePass entry from Keepass2Android and then input selected entry fields directly into the active text field through `InputConnection`, without using the clipboard.

## Current Status

Implemented:

- Android `InputMethodService` host.
- Jetpack Compose keyboard UI.
- Material 3 theme.
- Light theme, dark theme, system theme, and Android 12+ dynamic color.
- Static `Entry` system subtype plus optional dynamic `English (US)` subtype.
- English letters, numbers, and symbols when `English (US)` is enabled.
- Entry keyboard layout with fixed fields, scrollable extra fields, and expanded all-fields mode.
- Quick-action bar, quick-action panel, draggable pinned quick actions, and current-entry hint.
- Adaptive keyboard sizing for compact, normal, and tall height modes.
- Orientation-aware key metrics, pixel-snapped row height, bottom spacing, and navigation bar clearance.
- Shared key, field key, row, and layout metric components for the keyboard UI.
- Keepass2Android Plugin SDK2 integration.
- Keepass2Android plugin access flow.
- Entry selection through Keepass2Android.
- Field mapping from KP2A result to an in-memory session.
- Field input through `InputConnection.commitText()`.
- In-memory session repository.
- Default 60-second session timeout.
- Settings page.
- Keyboard height and English (US) subtype settings.
- Portrait layout and compressed landscape adaptation.
- Debug-only secure logging through `SecureLog`.
- GitHub Actions for CI, signed nightly APKs, and tagged release APKs.
- Unit and instrumentation tests for parsing, mapping, classification, sensitivity detection, settings, subtypes, quick actions, metrics, and safe snapshots.

## Project Goals

The project is designed around these constraints:

- Do not use the clipboard to transfer passwords or secrets.
- Do not show field values in the keyboard UI.
- Do not log passwords, TOTP codes, recovery codes, tokens, secrets, credentials, access tokens, or committed text.
- Do not persist entry field values to DataStore, SharedPreferences, files, or databases.
- Keep entry field values only in memory for a short period.
- Clear the active entry session automatically after a timeout.
- Allow the user to manually clear the session.

## Basic Usage

1. Install Keepass2Android.
2. Install KP2A Compose Keyboard.
3. Enable KP2A Compose Keyboard in Android input method settings.
4. Open the app from the launcher icon to configure keyboard settings.
5. Enable `English (US)` in the app settings if a text input subtype is needed.
6. Switch to KP2A Compose Keyboard in any text field.
7. Tap `[Select Entry]`.
8. Grant plugin access in Keepass2Android if prompted.
9. Select an entry in Keepass2Android.
10. Return to the keyboard.
11. Tap field buttons such as `[Username]`, `[Password]`, `[TOTP]`, or custom fields to input values.

## Documentation

Canonical project documentation lives in this README and the top-level files under [`docs/`](docs/):

- [`docs/architecture.md`](docs/architecture.md): package boundaries and runtime flows.
- [`docs/requirements.md`](docs/requirements.md): current product and engineering requirements.
- [`docs/security.md`](docs/security.md): sensitive data boundary and logging rules.
- [`docs/testing.md`](docs/testing.md): test coverage and validation commands.
- [`docs/build-and-release.md`](docs/build-and-release.md): local build, signing, GitHub Actions, nightly, and release notes.
- [`docs/known-limitations.md`](docs/known-limitations.md): known tradeoffs and compatibility limits.

Historical planning notes are not canonical documentation. Move still-useful decisions into the canonical docs before deleting or archiving planning notes.

## Keyboard Layouts

Android system input method settings always expose the static `Entry` subtype from `app/src/main/res/xml/method.xml`. `English (US)` is registered dynamically through `KeyboardSubtypeSynchronizer` only when enabled from app settings.

Keyboard UI is split into focused Compose components under `ui/keyboard/`:

- `KeyboardImeContent` owns the bounded IME surface, theme colors, orientation-aware metrics, and overall keyboard height.
- `KeyboardFrame` calculates row height and remaining field area height from available width, available height, bottom gap, and navigation inset.
- `KeyboardContentArea` hosts the quick-action bar, quick-action panel, text input layout, entry layout, and drag preview.
- `TextInputKeyboardLayout` hosts letter, number, and symbol input modes.
- `EntryKeyboardLayout` hosts selected-entry field actions.
- `QuickActionBar` shows the panel toggle, pinned quick actions, or current-entry hint.
- `QuickActionPanel` shows available quick actions and supports drag-to-pin.
- `KeyboardRow`, `KeyboardKey`, `FieldKey`, and `EntryFieldGrid` keep row spacing, fixed widths, field columns, and key styling consistent.

### Text Input Layout

The text input layout supports letter mode, number mode, and symbol mode.

Bottom action row:

```text
[?123/ABC] [Language when letters] [Space] [Select Entry] [Enter]
```

The language key switches from the `Entry` subtype to `English (US)` when English is enabled. Otherwise, it asks Android to switch to the next input method.

When an active session exists and the quick-action panel is closed, the top quick-action area shows:

```text
Current entry: {entryName} [Back to Entry Layout]
```

### Entry Layout

The entry layout supports:

- Fixed fields: Username, Password, and TOTP when present.
- Extra fields in a scrollable field area.
- Expanded all-fields mode.
- Bottom action rows for selecting another entry, switching language, inserting space, expanding/collapsing fields, paging expanded fields, clearing entry, and deleting.

Sensitive field buttons use a cautious visual style, but still show only the field label, never the field value.

Field-heavy areas scroll internally. They must not increase the overall IME height or push bottom actions into the navigation area.

### Quick Actions

The quick-action system stores only quick-action IDs in DataStore. It must never store entry names, field labels that came from KP2A, or field values.

Current production quick actions:

- Settings.
- Clear entry.

The center pinned area and optional right slot support drag-and-drop from the panel. The total pinned count is capped by `KeyboardQuickActionSlots.MAX_PINNED_ITEMS`.

## Security Model

Field values are allowed only inside the in-memory session:

```text
KeyboardSessionRepository
└─ KeyboardSession
   └─ KeyboardField.value
```

Field values must not enter:

- `KeyboardUiState`
- `KeyboardFieldSummary`
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
- English (US) subtype visibility.
- Haptic feedback toggle.
- Key sound toggle.
- Key preview toggle.
- Reset to defaults.

Some feedback settings may be present in the model before their runtime behavior is fully wired.

Keyboard height is applied through bounded Compact, Normal, and Tall modes. The app resolves orientation-aware key metrics for portrait and landscape instead of letting field-heavy layouts expand the IME window.

## Known Limitations

- The project is not a full general-purpose keyboard.
- It does not support pinyin, suggestions, autocorrect, candidate words, handwriting, or voice input.
- Additional language layouts beyond English (US) are not implemented.
- Landscape mode currently uses compressed shared layouts and metrics.
- Dedicated landscape layout is planned for a later phase.
- OEM ROMs may behave differently around IME window height, navigation bars, dynamic subtype visibility, activity launching, and input method lifecycle.
- Keepass2Android Plugin SDK2 is required.
- KeePassDX is not supported by this project.
- Field classification for custom fields is heuristic and may need tuning.

See [`docs/known-limitations.md`](docs/known-limitations.md) for details.

## Build and Test

Build debug APK:

```bash
./gradlew :app:assembleDebug
```

Run unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Run lint:

```bash
./gradlew :app:lintDebug
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

Release signing can be configured with `keystore.properties` or environment variables. GitHub Actions use `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD` secrets. See [`docs/build-and-release.md`](docs/build-and-release.md).

## Log Safety Check

Runtime logs should not contain real field values.

Example check:

```bash
adb logcat | rg "password|token|secret|totp|otp|recovery"
```

The output may contain event names or labels, but must not contain actual secret values.
