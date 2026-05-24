# Known Limitations

## Personal-device Scope

This project is primarily built for personal devices and personal needs.

It does not guarantee compatibility with:

- All Android versions.
- All HyperOS versions.
- All MIUI versions.
- All OEM ROMs.
- All Keepass2Android versions.
- Modified Keepass2Android builds.

## ROM Compatibility

IME and activity behavior varies by ROM.

Potentially unstable areas:

- IME window height.
- Navigation bar insets.
- Gesture navigation safe area.
- Three-button navigation layout.
- Launching activities from an input method.
- Activity result delivery after leaving the IME.
- Input method lifecycle callbacks.
- Android or vendor ROM settings may cache dynamically registered English (US) subtypes until the settings screen or IME is reopened.
- Android 14+ explicit subtype APIs may behave differently across vendors.
- Background activity restrictions.
- Vendor power management.
- Vendor security policies.

## Keepass2Android Dependency

This project depends on Keepass2Android Plugin SDK2.

Not supported:

- KeePassDX.
- Other KeePass clients.
- Generic password manager integrations.
- Clipboard-based fallback.
- Non-KP2A plugin APIs.

## Input Method Limitations

This is not intended to be a full general-purpose keyboard.

Not supported:

- Pinyin input.
- Candidate words.
- Word suggestions.
- Autocorrect.
- Additional language layouts beyond English (US).
- Gesture typing.
- Handwriting.
- Voice input.
- Advanced symbol panels.
- Full Gboard feature parity.

## Dynamic Subtype Limitations

The `Entry` subtype is static and always visible to Android once the IME is enabled.

The `English (US)` subtype is dynamic and controlled by app settings, but Android and vendor settings screens may cache subtype lists.

Known tradeoffs:

- Disabling English (US) may require reopening input method settings before the system UI refreshes.
- Some ROMs may delay or ignore additional subtype updates.
- The app cannot force Android settings to make a visible subtype non-removable.
- Unknown subtype values fall back to `Entry` for safety.

## Quick-action Limitations

Current production quick actions are limited to:

- Settings.
- Clear entry.

Known tradeoffs:

- Quick actions are not a general macro system.
- Quick actions must not store KP2A entry or field data.
- Drag-and-drop behavior depends on Compose pointer and layout behavior on the device.

## Landscape Limitations

Landscape mode currently uses the same layout as portrait with compressed metrics.

Known tradeoffs:

- Some buttons may feel dense.
- Field layouts are not optimized for wide screens.
- Entry action rows may be crowded.
- The shared layout is clipped to the bounded IME height instead of using a dedicated wide-screen arrangement.
- A dedicated landscape layout is deferred to P1.

## Field Classification Limitations

Field classification is based on:

- Keepass standard field names.
- Common field name heuristics.
- Protected fields reported by Keepass2Android.

Custom fields may be misclassified.

Examples that may require tuning:

- Non-English field names.
- Organization-specific field names.
- Abbreviations.
- Multiple custom token fields.
- Custom TOTP naming.

## TOTP Limitations

The keyboard treats TOTP as a field value returned by Keepass2Android.

It does not:

- Generate TOTP codes.
- Refresh TOTP codes.
- Track TOTP expiration.
- Display countdowns.

## Settings Limitations

Some settings may exist before their behavior is fully implemented.

Current reserved or partially wired settings:

- Haptic feedback.
- Key sound.
- Key preview.

These can be enabled in the model and UI before they are fully wired into input behavior.

## Build and Release Limitations

Current release builds are signed but not minified or obfuscated.

Nightly and release workflows require signing secrets. They cannot publish signed APKs when any required signing secret is missing.

## Security Limitations

The project reduces clipboard and UI exposure, but cannot fully protect against:

- Compromised devices.
- Malicious input targets.
- Screen recording.
- Accessibility malware.
- Root-level inspection.
- Debug builds with unsafe local changes.
- OEM logging outside the app's control.

## Compatibility Statement

> This project is primarily built for personal devices and personal needs. It does not guarantee compatibility with all Android, HyperOS, MIUI, or vendor ROM versions.
