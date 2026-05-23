# IME Subtypes and Language Switch Design

## Context

KP2A Compose Keyboard currently registers one Android IME subtype:
`English (US)`. The keyboard also has two internal layouts:

- `Entry`, which commits selected Keepass2Android entry fields.
- `Default`, which provides English letters, numbers, and symbols.

The desired behavior is system-level subtype management similar to Gboard:

- Android input method settings should always show `Entry` for KP2A Compose
  Keyboard.
- Android input method settings should show `English (US)` only when the user
  enables it in the app settings.
- The current input method picker should expose enabled KP2A subtypes as
  layouts.
- The keyboard should include a language switch key that cycles from `Entry`
  to `English (US)` and then to other enabled input methods.

This feature affects Android IME subtype registration, app settings,
InputMethodService behavior, keyboard UI, and tests.

## Goals

- Keep `Entry` as the default and always available KP2A subtype.
- Let the app setting decide whether `English (US)` is registered with the
  system.
- Hide `English (US)` from system IME subtype settings when it is disabled in
  the app.
- Show an empty `Entry` layout when there is no selected KP2A session instead
  of automatically launching Keepass2Android.
- Replace the `Entry` layout's switch-to-default key with a language switch
  key.
- Add a language switch key next to the number-switch key in the English letter
  layout.
- Preserve the sensitive data boundary: subtype settings and UI state must not
  contain Keepass2Android field values.

## Non-Goals

- Do not implement multi-language text layouts beyond `English (US)`.
- Do not attempt to force Android system settings to make a visible subtype
  non-removable. Third-party IMEs cannot reliably make a visible subtype
  checked but impossible to uncheck in system settings.
- Do not automatically open Keepass2Android when the `Entry` subtype becomes
  active.
- Do not replace Android's global input method switching order for other IMEs.
  The system still controls transitions outside KP2A Compose Keyboard.
- Do not use the clipboard.

## Android API Constraints

Android settings owns the final UI for input method subtype lists. A static
subtype declared in `method.xml` is visible to the system as long as the IME is
enabled. Additional subtypes can be supplied with
`InputMethodManager.setAdditionalInputMethodSubtypes()`, which allows the app
to add or remove `English (US)` based on app settings.

`setAdditionalInputMethodSubtypes()` is deprecated but remains the Android API
that supports dynamic subtype registration for this use case. ROMs may cache
subtype lists or delay settings UI refreshes, so real-device validation is
required.

For language switching, Android's `switchToNextInputMethod(false)` lets the
system choose the next enabled IME or subtype. This matches the platform
contract for leaving KP2A Compose Keyboard, but it does not let the app fully
define the global order. KP2A Compose Keyboard can explicitly switch from its
`Entry` subtype to its `English (US)` subtype when English is enabled, then
delegate the next step to the system.

References:

- https://developer.android.com/reference/android/view/inputmethod/InputMethodSubtype
- https://developer.android.com/reference/android/view/inputmethod/InputMethodManager#setAdditionalInputMethodSubtypes(java.lang.String,%20android.view.inputmethod.InputMethodSubtype[])
- https://developer.android.com/reference/android/inputmethodservice/InputMethodService#switchInputMethod(java.lang.String,%20android.view.inputmethod.InputMethodSubtype)
- https://developer.android.com/reference/android/inputmethodservice/InputMethodService#switchToNextInputMethod(boolean)

## Recommended Approach

Use a hybrid static-and-dynamic subtype model:

- Declare `Entry` statically in `app/src/main/res/xml/method.xml`.
- Remove `English (US)` from static XML.
- Add an app setting, default `false`, named `English (US)`.
- When the setting is enabled, dynamically register `English (US)` as an
  additional subtype.
- When the setting is disabled, register no additional subtypes, leaving only
  the static `Entry` subtype visible to Android.
- Store only the boolean setting in DataStore. Do not store active field values
  or selected entry data.

This best matches the requested system settings behavior:

- Disabled English: `KP2A Compose Keyboard -> Entry`.
- Enabled English: `KP2A Compose Keyboard -> Entry and English (US)`.

## Subtype Model

Create a small subtype model for stable identifiers and parsing:

```text
KeyboardSubtype.Entry
KeyboardSubtype.EnglishUs
```

Each subtype should have:

- A stable integer subtype ID.
- A stable layout extra value.
- A display label resource.
- A mapping to `MainKeyboardLayout`.

Suggested extra values:

```text
layout=entry
layout=english_us
```

`Entry` should use an empty or neutral locale because it is not a language
layout. `English (US)` should use `en_US` and be ASCII capable.

## Settings Design

Extend `KeyboardSettings`:

```kotlin
val englishUsSubtypeEnabled: Boolean = false
```

Add DataStore persistence:

```text
english_us_subtype_enabled
```

Add a settings row in the input/layout section:

- `Entry`: fixed enabled state, not interactive.
- `English (US)`: switch, default off.

The setting controls system subtype registration only. It does not contain or
expose sensitive Keepass2Android entry data.

## Subtype Synchronization

Add a small synchronizer owned by the app/IME layer:

```text
KeyboardSubtypeSynchronizer
```

Responsibilities:

- Build the additional subtype list from `KeyboardSettings`.
- Call `InputMethodManager.setAdditionalInputMethodSubtypes()` for the KP2A IME
  component.
- Log only subtype IDs or boolean state through `SecureLog`.
- Avoid logging current entry names, field labels that might be sensitive, or
  any field values.

Synchronization should run when:

- The settings screen starts and observes settings.
- The user changes the `English (US)` setting.
- `KeyboardImeService` starts or creates its input view.

This repeated synchronization is intentional because Android settings and some
ROMs may cache dynamic subtype information.

## IME Runtime Behavior

`KeyboardImeService` should track the current Android subtype and send layout
selection to the ViewModel.

Required platform hooks:

- Read the current subtype when input starts.
- Handle subtype changes through the IME callback when the system switches
  between KP2A subtypes.
- Parse subtype extras into `KeyboardSubtype`.

The ViewModel should expose intents for platform-driven subtype changes and
language switch requests:

```text
KeyboardIntent.ChangeSubtype(KeyboardSubtype)
KeyboardIntent.SwitchLanguage
```

Behavior:

- `Entry` subtype sets `MainKeyboardLayout.Entry`.
- `English (US)` subtype sets `MainKeyboardLayout.Default`.
- A selected KP2A session still updates entry fields and can keep the user in
  `Entry`.
- Clearing the session leaves the active subtype layout intact. If the active
  subtype is `Entry`, show the empty entry layout.

## Empty Entry Layout

When `MainKeyboardLayout.Entry` is active and no session exists:

- Do not show field buttons.
- Do not show current entry name.
- Do not auto-launch Keepass2Android.
- Show the action row with safe actions:
  - Language switch.
  - Select entry.
  - Settings, if needed for usability.
  - Delete/backspace, where the current row model supports it.

The exact row composition should reuse existing key components and metrics
instead of introducing a new layout system.

## Language Switch Key

Add a language switch key using a standard icon resource and content
description.

UI placement:

- In `Entry` layout, replace the current switch-to-default-layout key with the
  language switch key.
- In the English letter layout action row, add the language switch key next to
  the `?123` key.
- Number and symbol modes may keep the existing `ABC` switch behavior.

Switch behavior:

1. If the active subtype is `Entry` and `English (US)` is enabled, switch to the
   KP2A `English (US)` subtype.
2. If the active subtype is `Entry` and `English (US)` is disabled, call
   Android's next-input-method switch.
3. If the active subtype is `English (US)`, call Android's next-input-method
   switch.

This gives the requested order where KP2A controls its internal first hop:

```text
Entry -> English (US) -> other input method
```

After leaving KP2A Compose Keyboard, the system decides the remaining IME order.

## Effects and Platform Actions

Add keyboard effects that keep platform APIs out of the ViewModel:

```text
KeyboardEffect.SwitchToSubtype(KeyboardSubtype)
KeyboardEffect.SwitchToNextInputMethod
```

`KeyboardImeService` handles these effects:

- `SwitchToSubtype(EnglishUs)` calls `switchInputMethod(currentImeId, englishSubtype)`.
- `SwitchToNextInputMethod` calls `switchToNextInputMethod(false)`.

Errors from platform calls should be logged through `SecureLog.w()` with
non-sensitive structured fields such as the target subtype ID and exception
type.

## Error Handling

- If dynamic subtype registration fails, keep the app usable with the static
  `Entry` subtype and show a settings save error only when the user's setting
  action fails.
- If switching to `English (US)` fails, log the platform error and fall back to
  `switchToNextInputMethod(false)` where safe.
- If subtype extras are missing or unknown, treat the subtype as `Entry` to keep
  the secure primary layout active.
- Do not silently ignore DataStore IO errors. Existing settings error handling
  should continue to rethrow non-IO failures.

## Tests

Add or update focused tests:

- `KeyboardSettingsTest`: default English subtype setting is disabled.
- Repository/settings tests where practical: invalid or missing preference
  defaults to disabled.
- Subtype parser tests:
  - `layout=entry` maps to `Entry`.
  - `layout=english_us` maps to `EnglishUs`.
  - missing or unknown extras map to `Entry`.
- ViewModel tests:
  - `Entry` subtype selects entry layout without requiring a session.
  - `EnglishUs` subtype selects default layout.
  - language switch from `Entry` emits `SwitchToSubtype(EnglishUs)` when enabled.
  - language switch from `Entry` emits `SwitchToNextInputMethod` when English is
    disabled.
  - language switch from `EnglishUs` emits `SwitchToNextInputMethod`.
- Compose tests:
  - Entry empty state renders safe actions and no field buttons.
  - Entry layout action row contains language switch instead of
    switch-to-default-layout.
  - Letter layout contains language switch next to the number switch key.

Validation commands:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Manual validation:

- Install the debug build.
- Enable KP2A Compose Keyboard.
- With app English setting disabled, verify Android input method settings show
  only `Entry`.
- Enable app English setting and verify Android input method settings show
  `Entry` and `English (US)`.
- Open the current input method picker and verify only enabled KP2A subtypes
  appear.
- Verify language switch order:
  - `Entry -> English (US)` when English is enabled.
  - `English (US) -> next system IME`.
  - `Entry -> next system IME` when English is disabled.

## Risks

- `setAdditionalInputMethodSubtypes()` is deprecated and may behave differently
  on future Android versions or vendor ROMs.
- Android settings may cache dynamic subtype registration and require reopening
  settings, restarting the IME, or toggling the IME to refresh.
- The system controls global IME switching order after KP2A leaves its own
  subtypes.
- Some ROMs may render the subtype summary differently from AOSP or Gboard.

## Security Notes

- Subtype settings are not sensitive.
- The empty `Entry` layout must not reveal any previous field values after
  session clear.
- Language switching must not log committed text or entry field values.
- Dynamic subtype logs must include only boolean state, subtype IDs, and
  exception types.
