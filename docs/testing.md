# Testing

## Unit Tests

Current unit tests focus on pure logic and security boundaries.

Covered areas:

- KP2A entry result parsing
- Protected field parsing
- Field classification
- Sensitive field detection
- KP2A result to session mapping
- Filtering title fields
- Filtering binary fields
- Filtering empty fields
- Safe session snapshot mapping
- Settings default values
- Settings timeout bounds

Expected test files:

```text
Kp2aEntryResultParserTest
Kp2aEntryMapperTest
KeyboardFieldClassifierTest
SensitiveFieldPolicyTest
KeyboardSessionMappingsTest
KeyboardSettingsTest
```

## Run Tests

```bash
./gradlew :app:testDebugUnitTest
```

Run one test:

```bash
./gradlew :app:testDebugUnitTest --tests "*Kp2aEntryMapperTest"
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

### Default Layout

- [ ] Letter keys input text.
- [ ] Shift toggles uppercase.
- [ ] Delete removes previous character.
- [ ] Space inputs a space.
- [ ] Enter sends enter.
- [ ] Number mode opens.
- [ ] Symbol mode opens.
- [ ] Settings button opens settings.

### Keepass2Android Flow

- [ ] Tap `[Select Entry]`.
- [ ] Entry picker activity opens.
- [ ] Keepass2Android opens.
- [ ] Plugin access page appears when access is not granted.
- [ ] Plugin scopes are shown.
- [ ] Entry selection returns a result.
- [ ] Entry picker activity finishes.
- [ ] Keyboard displays entry layout.

### Entry Layout

- [ ] Current entry header is shown.
- [ ] Username / Password / TOTP fixed fields are shown when available.
- [ ] Extra fields are paged.
- [ ] Expanded mode displays all fields.
- [ ] Expanded field area scrolls internally.
- [ ] Bottom action rows stay fixed.
- [ ] Field buttons input values.
- [ ] Sensitive values are not displayed in UI.

### Settings

- [ ] Launcher icon opens settings.
- [ ] Keyboard settings button opens settings.
- [ ] Theme mode can be changed.
- [ ] Dynamic color can be toggled.
- [ ] Keyboard height can be changed.
- [ ] Session timeout can be changed.
- [ ] Reset to default works.

### Security

- [ ] No clipboard usage for field values.
- [ ] No field values in UI.
- [ ] No field values in logs.
- [ ] No access tokens in logs.
- [ ] Session clears after timeout.
- [ ] Manual clear works.
- [ ] Canceling KP2A selection does not clear old session.

## Runtime Log Check

Example:

```bash
adb logcat | rg "password|token|secret|totp|otp|recovery"
```

Expected:

- Event names and labels may appear.
- Actual secret values must not appear.
