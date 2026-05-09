# Test Devices

## Primary Test Device

```text
Device: Xiaomi 15 Ultra / Xiaomi 15U
ROM: HyperOS 3.0.303.0
Android version: device-dependent
Password manager: Keepass2Android
Input method: KP2A Compose Keyboard
Navigation mode: gesture navigation and/or system navigation modes as available
```

## Verified Areas

The following areas have been tested or targeted during development:

- IME can be enabled.
- IME can be selected as the current input method.
- Default keyboard layout can input letters.
- Number mode can input numbers.
- Symbol mode can input symbols.
- Delete and enter actions work through `InputConnection`.
- Entry picker activity can be launched from the keyboard.
- Keepass2Android can be opened through Plugin SDK2.
- Plugin authorization flow can be reached.
- Entry result can be parsed.
- Entry fields can be mapped into an in-memory session.
- Entry layout can show safe field labels.
- Field buttons can commit values through `InputConnection`.
- Settings page can be opened from the launcher icon.
- Settings page can be opened from the keyboard.
- Material 3 theme is applied.
- Basic portrait layout is usable.
- Basic landscape layout does not crash.
- Expanded fields scroll internally.

## ROM-specific Areas to Watch

Different devices or ROMs may need extra testing around:

- IME window height
- Navigation bar insets
- Gesture navigation bottom safe area
- Three-button navigation
- Activity launch from IME
- Activity result return path
- IME lifecycle during activity transitions
- Background activity restrictions
- Power management restrictions
- Keepass2Android plugin authorization behavior

## Compatibility Statement

> This project is primarily built for personal devices and personal requirements. It does not guarantee compatibility with all Android versions, HyperOS versions, MIUI versions, OEM ROMs, or Keepass2Android configurations.

## Suggested Test Matrix

| Area | Portrait | Landscape | Notes |
|---|---:|---:|---|
| Default letter layout | Required | Required | Landscape may be compressed |
| Number layout | Required | Required | |
| Symbol layout | Required | Required | |
| Entry layout | Required | Required | |
| Expanded field layout | Required | Required | Internal scroll required |
| Settings page | Required | Optional | |
| KP2A authorization | Required | Optional | Depends on installed KP2A |
| KP2A entry selection | Required | Optional | Depends on KP2A configuration |
| Gesture navigation | Required | Optional | Bottom spacing must be checked |
| Three-button navigation | Optional | Optional | Device dependent |
