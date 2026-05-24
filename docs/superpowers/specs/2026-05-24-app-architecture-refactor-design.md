# App Architecture Refactor Design

## Context

KP2A Compose Keyboard has grown from a small proof-of-concept IME into a
Compose keyboard with Keepass2Android integration, in-memory session handling,
runtime subtype synchronization, keyboard settings, and security-sensitive
field commit flows.

The current `app` module already uses feature-oriented packages, but several
architecture boundaries are still too soft:

- `KeyboardViewModel` coordinates keyboard state, settings, session projection,
  session clearing, field commit lookup, and one-shot effects.
- `KeyboardImeService` hosts Compose, drives a manual lifecycle, installs
  ViewTree owners, applies IME navigation bar behavior, synchronizes subtypes,
  launches activities, and executes platform effects.
- Domain rules and external SDK details are mixed in places. For example,
  field classification currently knows Keepass2Android SDK constants.
- Session snapshot mapping has more than one representation path, which weakens
  the single source of truth for UI-safe projections.
- Settings models, DataStore persistence, keyboard runtime settings access, and
  settings UI flow live close together.

This refactor reorganizes the `app` module around explicit responsibility
boundaries while keeping the project in a single Gradle module.

## Design Rules

The refactor follows these rules in priority order:

- `solid-srp`: each package and class should have one main reason to change.
- `solid-dip`: feature and application code should not depend directly on
  Android, DataStore, or Keepass2Android SDK details when a real boundary exists.
- `core-separation-of-concerns`: UI rendering, state orchestration, session
  security, external protocol parsing, and platform IME behavior must be
  separated.
- `core-dry`: settings defaults, session safety projection, and field
  classification rules must each have one authoritative representation.
- `core-kiss`: prefer direct, readable code over framework-like layering.
- `core-yagni`: add abstractions only where they reduce current complexity or
  protect a real volatility boundary.
- `pattern-repository`: use repositories for DataStore and in-memory session
  access, but do not introduce a generic repository base class.

## Goals

- Reorganize `app` package boundaries into `domain`, `application`, `data`,
  `platform`, `feature`, `ui`, `security`, and `di`.
- Keep `app` as a single Gradle module.
- Make the sensitive data path shorter and easier to audit.
- Move pure models and pure rules out of feature and platform packages.
- Move Android, DataStore, and Keepass2Android SDK details into adapter packages.
- Introduce only a few high-value application use cases for keyboard runtime
  behavior.
- Reduce `KeyboardViewModel` to state orchestration, intent handling, and effect
  emission.
- Keep `KeyboardImeService` focused on Android IME hosting and platform action
  execution.
- Update tests and architecture documentation to match the new boundaries.

## Non-Goals

- Do not split the project into multiple Gradle modules.
- Do not change user-visible keyboard behavior.
- Do not change UI visual design.
- Do not change the Keepass2Android manual query mode.
- Do not change session timeout semantics.
- Do not introduce a full Clean Architecture template.
- Do not introduce generic repository, mapper, use case, or port base classes.
- Do not wrap every DataStore write in a use case.
- Do not solve all long Compose files unless their structure blocks the
  architecture boundary.
- Do not add pinyin, suggestions, autocorrect, multi-language layouts, or other
  keyboard features.

## Package Layout

The target package layout is:

```text
io.github.togls.kp2acomposekeyboard
|- domain/
|  |- field/
|  |- keyboard/
|  |- policy/
|  |- session/
|  `- settings/
|- application/
|  |- keyboard/
|  |- session/
|  `- settings/
|- data/
|  |- kp2a/
|  |- session/
|  `- settings/
|- platform/
|  |- ime/
|  |- input/
|  `- navigation/
|- feature/
|  |- keyboard/
|  |- entrypicker/
|  `- settings/
|- ui/
|  |- keyboard/
|  `- theme/
|- security/
`- di/
```

Dependency direction:

```text
ui -> feature -> application -> domain
feature -> domain
application -> domain
data -> domain / application ports
platform -> feature / application
di -> concrete wiring
```

Rules:

- `domain` must not depend on Android, Compose, DataStore, Hilt, or the
  Keepass2Android SDK.
- `data/kp2a` may depend on the Keepass2Android SDK and maps SDK output into
  domain models.
- `data/settings` owns DataStore persistence.
- `data/session` is the only long-lived in-memory storage boundary for
  sensitive field values.
- `platform` owns Android IME, InputConnection, subtype, and activity-launch
  details.
- `feature` owns MVI state, intents, effects, and view models.
- `ui` owns Compose rendering only.

## Component Mapping

### Domain

Move pure field, session, settings, and policy concepts into domain:

```text
domain/KeyboardField.kt                -> domain/field/KeyboardField.kt
domain/KeyboardFieldType.kt            -> domain/field/KeyboardFieldType.kt
domain/KeyboardFieldUiModel.kt         -> domain/field/KeyboardFieldSummary.kt
domain/KeyboardFieldMappings.kt        -> domain/field/KeyboardFieldMappings.kt
domain/KeyboardFieldClassifier.kt      -> domain/policy/KeyboardFieldClassifier.kt
domain/SensitiveFieldPolicy.kt         -> domain/policy/SensitiveFieldPolicy.kt
session/KeyboardSession.kt             -> domain/session/KeyboardSession.kt
session/KeyboardSessionSnapshot.kt     -> domain/session/KeyboardSessionSnapshot.kt
feature/keyboard/KeyboardSubtype.kt    -> domain/keyboard/KeyboardSubtype.kt
feature/keyboard/MainKeyboardLayout.kt -> domain/keyboard/MainKeyboardLayout.kt
feature/keyboard/DefaultInputMode.kt   -> domain/keyboard/DefaultInputMode.kt
feature/keyboard/EntryFieldDisplayMode.kt -> domain/keyboard/EntryFieldDisplayMode.kt
feature/settings/KeyboardSettings.kt   -> domain/settings/KeyboardSettings.kt
feature/settings/KeyboardThemeMode.kt  -> domain/settings/KeyboardThemeMode.kt
feature/settings/KeyboardHeightMode.kt -> domain/settings/KeyboardHeightMode.kt
```

`KeyboardFieldUiModel` should be renamed to `KeyboardFieldSummary` or a similar
domain-safe projection name. It must keep only:

```text
id
label
type
sensitive
```

It must never contain `value`.

`KeyboardFieldClassifier` must not reference `keepass2android.pluginsdk`
constants. Keepass2Android field names should be normalized in `data/kp2a`
before domain policy is invoked.

### Data

Move external protocol and persistence implementations into data:

```text
kp2a/* -> data/kp2a/*
feature/settings/SettingsRepository.kt -> data/settings/SettingsRepository.kt
feature/settings/KeyboardUtilitySlotsPreferenceCodec.kt -> data/settings/KeyboardUtilitySlotsPreferenceCodec.kt
session/KeyboardSessionRepository.kt -> data/session/KeyboardSessionRepository.kt
session/KeyboardSessionMappings.kt -> data/session/KeyboardSessionMappings.kt
session/KeyboardSessionSnapshotFactory.kt -> remove or merge into one projection path
```

`data/session/KeyboardSessionRepository` remains the only repository that holds
raw `KeyboardField.value` after mapping succeeds.

`data/session/KeyboardSessionMappings` should become the single source of truth
for converting `KeyboardSession` into a UI-safe `KeyboardSessionSnapshot`.

### Application

Introduce only use cases that reduce current coupling:

```text
application/keyboard/CommitKeyboardFieldUseCase.kt
application/keyboard/ObserveKeyboardSessionSnapshotUseCase.kt
application/keyboard/ClearKeyboardSessionUseCase.kt
application/session/SessionTimeoutController.kt
application/settings/KeyboardSettingsStore.kt
```

These use cases protect the sensitive session boundary and keep
`KeyboardViewModel` from directly reading raw session values.

Do not add use cases for simple settings writes such as `updateThemeMode()` or
`updateKeySoundEnabled()` unless a real behavior rule appears.

### Platform

Move Android platform code into platform packages:

```text
ime/KeyboardImeService.kt -> platform/ime/KeyboardImeService.kt
ime/KeyboardSubtypeRegistry.kt -> platform/ime/KeyboardSubtypeRegistry.kt
ime/KeyboardSubtypeSynchronizer.kt -> platform/ime/KeyboardSubtypeSynchronizer.kt
ime/KeyboardViewModelFactory.kt -> platform/ime/KeyboardViewModelFactory.kt
ime/InputConnectionDispatcher.kt -> platform/input/InputConnectionDispatcher.kt
```

Optional extractions are allowed only if they reduce current responsibilities:

```text
platform/navigation/ImeActivityLauncher.kt
platform/ime/ImeThemeController.kt
platform/ime/ImeViewTreeOwners.kt
```

These should not be created speculatively.

### Feature and UI

Keep feature and UI package responsibilities:

```text
feature/keyboard/*
feature/entrypicker/*
feature/settings/*
ui/keyboard/*
ui/theme/*
```

`feature/keyboard/KeyboardViewModel` should depend on application use cases and
domain models, not directly on the session repository.

Compose UI must continue to receive only `UiState` and intent callbacks. It must
not access repositories, use cases, platform APIs, or raw field values.

## Data Flows

### Entry Selection

```text
EntryPickerActivity
  -> data.kp2a.Kp2aEntryResultParser
  -> data.kp2a.Kp2aEntryMapper
  -> data.session.KeyboardSessionRepository.setSession()
  -> application.keyboard.ObserveKeyboardSessionSnapshotUseCase
  -> feature.keyboard.KeyboardViewModel
  -> KeyboardUiState
  -> ui.keyboard
```

Rules:

- Keepass2Android raw JSON exists only temporarily inside `data/kp2a`.
- The mapper creates `KeyboardSession` domain data.
- The session repository stores raw field values only in memory.
- ViewModel observes only safe snapshots.
- UI state stores only field summaries and session metadata.

### Field Commit

```text
FieldKey
  -> KeyboardIntent.CommitField(fieldId)
  -> KeyboardViewModel
  -> CommitKeyboardFieldUseCase(fieldId)
  -> data.session.KeyboardSessionRepository.getFieldValue(fieldId)
  -> KeyboardEffect.CommitText(value)
  -> platform.ime.KeyboardImeService
  -> platform.input.InputConnectionDispatcher.commitText(value)
```

`CommitKeyboardFieldUseCase` is the only application entry point where a raw
field value leaves the session repository.

The result should be explicit:

```text
CommitFieldResult.Commit(text)
CommitFieldResult.Ignored(reason)
```

Expected ignored reasons include:

```text
BlankFieldId
FieldNotFound
EmptyValue
```

The commit text may contain passwords, TOTP codes, or recovery codes. It must
never be logged or copied into UI state.

### Session Clear and Timeout

```text
KeyboardViewModel
  -> application.session.SessionTimeoutController
  -> ClearKeyboardSessionUseCase
  -> data.session.KeyboardSessionRepository.clear()
```

Rules:

- Timeout policy lives in `application/session`.
- Clearing uses `ClearKeyboardSessionUseCase`.
- Normal IME destruction clears the session.
- Temporary IME destruction while launching entry selection must preserve the
  previous session.
- Cancelling entry selection must preserve the previous session.

### Settings

```text
SettingsScreen
  -> SettingsIntent
  -> SettingsViewModel
  -> data.settings.SettingsRepository
  -> DataStore Preferences
  -> domain.settings.KeyboardSettings
```

Rules:

- `KeyboardSettings` is a domain model.
- DataStore keys and codecs live in `data/settings`.
- Settings must never store entry values, raw KP2A JSON, access tokens, or
  session data.
- `KeyboardSettingsStore` is a minimal runtime port used by keyboard runtime
  code that only needs settings flow and utility slot updates.

### Subtypes and Platform Actions

```text
KeyboardViewModel
  -> KeyboardEffect.SwitchToSubtype / SwitchToNextInputMethod
  -> platform.ime.KeyboardImeService
  -> platform.ime.KeyboardSubtypeRegistry
  -> Android InputMethodManager
```

Rules:

- `KeyboardSubtype` is a pure domain keyboard concept.
- Android `InputMethodSubtype` stays in `platform/ime`.
- Platform logs may include subtype IDs, enabled flags, and exception types.
- Platform logs must not include entry names, field labels that may be
  sensitive, or field values.

## Error Handling

Use explicit results for expected input outcomes:

- Blank field ID.
- Missing field.
- Empty field value.

Use exceptions and structured logs for external failures:

- DataStore non-IO failures continue to rethrow.
- DataStore IO failures may fall back to `emptyPreferences()`.
- Activity launch failures catch `ActivityNotFoundException` and
  `SecurityException`.
- IME subtype switching failures log the platform error and fall back where safe.

Parsing rules:

- Malformed KP2A entry JSON returns an empty field map without logging raw JSON.
- Malformed protected-field metadata degrades to an empty protected set because
  the metadata only affects safety labels.

Logging rules:

- Use `SecureLog` only.
- Do not interpolate sensitive values into log messages.
- Allowed structured fields include result code, boolean flags, error type,
  field count, protected field count, subtype ID, and ignored reason.
- Never log `KeyboardField.value`, `KeyboardEffect.CommitText.text`, raw KP2A
  JSON, full Intent extras values, or access tokens.

## Testing Strategy

Keep existing tests and update package names. Add or adjust focused tests where
the new boundaries introduce behavior.

Suggested test ownership:

```text
domain/
  KeyboardFieldClassifierTest
  SensitiveFieldPolicyTest
  KeyboardSettingsTest

data/kp2a/
  Kp2aEntryResultParserTest
  Kp2aEntryMapperTest

data/session/
  KeyboardSessionMappingsTest
  KeyboardSessionRepositoryTest

application/keyboard/
  CommitKeyboardFieldUseCaseTest
  ObserveKeyboardSessionSnapshotUseCaseTest

feature/keyboard/
  KeyboardViewModelSubtypeTest
  KeyboardViewModelUtilityTest
  KeyboardViewModelSessionTest

platform/ime/
  KeyboardSubtypeRegistryTest
```

Required validation commands:

```bash
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Because this is an architecture-level refactor, `assembleDebug` is required in
addition to unit tests.

## Migration Constraints

- Preserve behavior during the refactor.
- Move files and update package names once; do not leave duplicate model types
  or compatibility aliases.
- Keep all functions and files within the repository size guidance where
  reasonable.
- Do not introduce new features.
- Do not persist or log sensitive field values.
- Do not add interfaces where there is only one implementation and no real
  volatility boundary.
- Do not create a generic repository base.
- Do not create use cases for every settings mutation.
- Keep `domain` free of Android, Compose, DataStore, Hilt, and KP2A SDK imports.
- Keep `feature` and `ui` free of `KeyboardField.value`.

## Implementation Order

The implementation should still be executed in verifiable steps:

1. Move pure domain models and update imports.
2. Move data adapters and repositories.
3. Move platform IME and input code.
4. Add application keyboard/session use cases.
5. Replace direct session repository access in `KeyboardViewModel`.
6. Consolidate session safe projection and remove duplicate mapping.
7. Move settings models and minimal settings runtime port.
8. Remove domain dependency on Keepass2Android SDK constants.
9. Update tests for package names and new use cases.
10. Run unit tests.
11. Run debug build.
12. Update `docs/architecture.md`.

## Success Criteria

- The package structure reflects real responsibilities and change reasons.
- `KeyboardViewModel` no longer reads raw session values directly.
- `KeyboardImeService` is limited to IME hosting and platform effect handling.
- Domain code has no Android, Compose, DataStore, Hilt, or Keepass2Android SDK
  imports.
- The sensitive data path is shorter and easier to audit.
- Session snapshot projection has one authoritative implementation.
- Unit tests pass.
- Debug build passes.
- Architecture documentation matches the new structure.

## Compatibility Disclaimer

This project is primarily built for personal devices and personal needs. It
does not guarantee compatibility with all Android, HyperOS, MIUI, or vendor ROM
versions.

## Commit Message

Recommended implementation commit:

```text
refactor(architecture): reorganize app module boundaries
```

The commit body should explain why the boundaries were changed, which layers
were introduced, how the sensitive data path remains protected, and which
validation commands passed.
