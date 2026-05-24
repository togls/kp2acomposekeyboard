# Testing

## Unit Tests

Current unit tests focus on pure logic, state reducers, settings persistence, subtype behavior, layout math, and security boundaries.

Covered areas:

- KP2A entry result parsing.
- Protected field parsing.
- Field classification.
- Sensitive field detection.
- KP2A result to session mapping.
- KP2A plugin action session synchronization.
- KP2A plugin scope registration.
- Filtering title fields.
- Filtering binary fields.
- Filtering empty fields.
- Safe session snapshot mapping.
- Commit-field use case behavior.
- Session fallback timeout.
- Settings default values.
- Settings timeout bounds.
- Settings persistence.
- Keyboard height mode persistence and bounds.
- English (US) subtype registry behavior.
- Keyboard ViewModel subtype state changes.
- Quick-action slot reducer behavior.
- Quick-action slot preference codec.
- Quick-action bar policy.
- Quick-action slot model mapping.
- Entry field paging math.
- Keyboard layout metrics and pixel snapping.

Expected test files:

```text
CommitKeyboardFieldUseCaseTest
ObserveKeyboardSessionSnapshotUseCaseTest
Kp2aEntryResultParserTest
Kp2aEntryMapperTest
Kp2aEntrySyncHandlerTest
Kp2aPluginScopesTest
KeyboardSessionMappingsTest
KeyboardFieldClassifierTest
SensitiveFieldPolicyTest
SessionTimeoutControllerTest
KeyboardQuickActionSlotsReducerTest
KeyboardViewModelQuickActionTest
KeyboardViewModelSubtypeTest
KeyboardSubtypeRegistryTest
KeyboardQuickActionSlotsPreferenceCodecTest
KeyboardSettingsTest
SettingsRepositoryTest
EntryFieldPagingTest
KeyboardLayoutMetricsTest
QuickActionBarPolicyTest
QuickActionSlotModelsTest
```

## Instrumentation Tests

Current Android tests focus on Compose keyboard rendering and device/emulator behavior.

Expected test files:

```text
KeyboardHeightProbeTest
KeyboardImeContentEntryTest
KeyboardImeContentSensitiveDataTest
KeyboardImeContentTextInputTest
```

Covered areas:

- Text input layout behavior.
- Entry layout behavior.
- Sensitive values not being displayed.
- Keyboard height and bounded content behavior.

## Run Tests

Run unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Run one unit test class:

```bash
./gradlew :app:testDebugUnitTest --tests "*Kp2aEntryMapperTest"
```

Run KP2A plugin action sync tests:

```bash
./gradlew :app:testDebugUnitTest --tests "*Kp2aEntrySyncHandlerTest"
./gradlew :app:testDebugUnitTest --tests "*Kp2aPluginScopesTest"
./gradlew :app:testDebugUnitTest --tests "*SessionTimeoutControllerTest"
```

Run instrumentation tests on a connected device or emulator:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Run one instrumentation test class:

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardImeContentEntryTest
```

## Build and Lint Validation

Build debug APK:

```bash
./gradlew :app:assembleDebug
```

Run lint:

```bash
./gradlew :app:lintDebug
```

Build release APK when signing inputs are available:

```bash
./gradlew :app:assembleRelease
```

## Robolectric SDK

If Robolectric fails because the project uses a newer compile or target SDK, pin the test SDK.

Option 1: per test class:

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Kp2aEntryResultParserTest
```

Option 2: global test resource:

```text
app/src/test/resources/robolectric.properties
```

```properties
sdk=35
```

## Manual Test Checklist

### Text Input Layout

- [ ] Letter keys input text.
- [ ] Shift toggles uppercase.
- [ ] Delete removes previous character.
- [ ] Space inputs a space.
- [ ] Enter sends enter.
- [ ] Number mode opens.
- [ ] Symbol mode opens.
- [ ] Number and symbol layouts keep fixed and flexible keys aligned.
- [ ] Language key switches to English (US) when that subtype is enabled.
- [ ] Language key delegates to Android next input method behavior when English (US) is disabled.
- [ ] Select Entry opens the Keepass2Android selection flow.

### Quick Actions

- [ ] Quick-action panel opens and closes.
- [ ] Settings quick action opens settings.
- [ ] Clear entry quick action clears the active session.
- [ ] Pinned quick actions can be dragged into the center area.
- [ ] Pinned quick actions can be dragged into the right slot.
- [ ] Duplicate quick-action slots are sanitized.
- [ ] Quick-action slot persistence survives reopening settings or IME.
- [ ] Quick-action persistence stores only action IDs.

### Keepass2Android Flow

- [ ] Tap `[Select Entry]`.
- [ ] Entry picker activity opens.
- [ ] Keepass2Android opens.
- [ ] Plugin access page appears when access is not granted.
- [ ] Plugin scopes are shown.
- [ ] Entry selection returns a result.
- [ ] Entry picker activity finishes.
- [ ] Keyboard displays entry layout.
- [ ] Canceling selection preserves the previous session.

### Entry Layout

- [ ] Username / Password / TOTP fixed fields are shown when available.
- [ ] Extra fields are displayed in the remaining field area.
- [ ] Extra field area scrolls internally when needed.
- [ ] Expanded mode displays all fields.
- [ ] Expanded field area scrolls internally.
- [ ] Previous and next actions page expanded fields.
- [ ] Bottom action rows stay fixed.
- [ ] Field buttons input values.
- [ ] Sensitive values are not displayed in UI.

### Settings

- [ ] Launcher icon opens settings.
- [ ] Keyboard settings quick action opens settings.
- [ ] Theme mode can be changed.
- [ ] Dynamic color can be toggled.
- [ ] Keyboard height can be changed.
- [ ] Keyboard height mode changes are reflected when reopening the IME.
- [ ] English (US) subtype can be enabled and disabled.
- [ ] Session timeout can be changed within the allowed range.
- [ ] Reset to default works.

### Security

- [ ] No clipboard usage for field values.
- [ ] No field values in UI.
- [ ] No field values in logs.
- [ ] No access tokens in logs.
- [ ] No committed text in logs.
- [ ] Session clears after timeout.
- [ ] Manual clear works.
- [ ] Canceling KP2A selection does not clear old session.

### Layout and Insets

- [ ] Compact, Normal, and Tall keyboard heights stay bounded.
- [ ] Portrait bottom action rows stay clear of gesture navigation.
- [ ] Landscape layout uses compressed metrics and does not expand the IME window.
- [ ] Expanded fields scroll internally without pushing bottom actions into the navigation area.
- [ ] Row height snapping avoids cumulative pixel overflow.

## Runtime Log Check

Example:

```bash
adb logcat | rg "password|token|secret|totp|otp|recovery"
```

Expected:

- Event names and labels may appear.
- Actual secret values must not appear.

Also check for crashes:

```bash
adb logcat | rg "Kp2aKeyboardIme|AndroidRuntime|FATAL EXCEPTION"
```
