# App Architecture Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reorganize the `app` module into explicit domain, application, data, platform, feature, and UI boundaries while preserving keyboard behavior and sensitive-data safety.

**Architecture:** Keep one Gradle module and move code by responsibility. Domain contains pure models and policies, data contains KP2A/DataStore/session implementations, application contains only high-value keyboard/session use cases, platform contains Android IME adapters, feature contains MVI state and view models, and UI contains Compose rendering.

**Tech Stack:** Kotlin, Android InputMethodService, Jetpack Compose, Hilt, DataStore Preferences, Keepass2Android Plugin SDK2, JUnit, Robolectric, Gradle.

---

## File Structure

### Create

- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/CommitFieldResult.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/CommitKeyboardFieldUseCase.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/ClearKeyboardSessionUseCase.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/ObserveKeyboardSessionSnapshotUseCase.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/settings/KeyboardSettingsStore.kt`
- `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/CommitKeyboardFieldUseCaseTest.kt`
- `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/ObserveKeyboardSessionSnapshotUseCaseTest.kt`

### Move

- `domain/KeyboardField.kt` -> `domain/field/KeyboardField.kt`
- `domain/KeyboardFieldType.kt` -> `domain/field/KeyboardFieldType.kt`
- `domain/KeyboardFieldUiModel.kt` -> `domain/field/KeyboardFieldSummary.kt`
- `domain/KeyboardFieldMappings.kt` -> `domain/field/KeyboardFieldMappings.kt`
- `domain/KeyboardFieldClassifier.kt` -> `domain/policy/KeyboardFieldClassifier.kt`
- `domain/SensitiveFieldPolicy.kt` -> `domain/policy/SensitiveFieldPolicy.kt`
- `session/KeyboardSession.kt` -> `domain/session/KeyboardSession.kt`
- `session/KeyboardSessionSnapshot.kt` -> `domain/session/KeyboardSessionSnapshot.kt`
- `feature/keyboard/KeyboardSubtype.kt` -> `domain/keyboard/KeyboardSubtype.kt`
- `feature/keyboard/MainKeyboardLayout.kt` -> `domain/keyboard/MainKeyboardLayout.kt`
- `feature/keyboard/DefaultInputMode.kt` -> `domain/keyboard/DefaultInputMode.kt`
- `feature/keyboard/EntryFieldDisplayMode.kt` -> `domain/keyboard/EntryFieldDisplayMode.kt`
- `feature/keyboard/KeyboardUtilitySlots.kt` -> `domain/keyboard/KeyboardUtilitySlots.kt`
- `feature/keyboard/KeyboardUtilityItemId.kt` -> `domain/keyboard/KeyboardUtilityItemId.kt`
- `feature/settings/KeyboardSettings.kt` -> `domain/settings/KeyboardSettings.kt`
- `feature/settings/KeyboardThemeMode.kt` -> `domain/settings/KeyboardThemeMode.kt`
- `feature/settings/KeyboardHeightMode.kt` -> `domain/settings/KeyboardHeightMode.kt`
- `kp2a/*` -> `data/kp2a/*`
- `session/KeyboardSessionRepository.kt` -> `data/session/KeyboardSessionRepository.kt`
- `session/KeyboardSessionMappings.kt` -> `data/session/KeyboardSessionMappings.kt`
- `session/SessionTimeoutController.kt` -> `application/session/SessionTimeoutController.kt`
- `feature/settings/SettingsRepository.kt` -> `data/settings/SettingsRepository.kt`
- `feature/settings/KeyboardUtilitySlotsPreferenceCodec.kt` -> `data/settings/KeyboardUtilitySlotsPreferenceCodec.kt`
- `ime/KeyboardImeService.kt` -> `platform/ime/KeyboardImeService.kt`
- `ime/KeyboardSubtypeRegistry.kt` -> `platform/ime/KeyboardSubtypeRegistry.kt`
- `ime/KeyboardSubtypeSynchronizer.kt` -> `platform/ime/KeyboardSubtypeSynchronizer.kt`
- `ime/KeyboardViewModelFactory.kt` -> `platform/ime/KeyboardViewModelFactory.kt`
- `ime/InputConnectionDispatcher.kt` -> `platform/input/InputConnectionDispatcher.kt`

### Delete

- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/session/KeyboardSessionSnapshotFactory.kt`

### Modify

- `app/src/main/AndroidManifest.xml`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/di/AppModule.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardUiState.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModel.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardEffect.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardIntent.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/entrypicker/EntryPickerActivity.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/entrypicker/EntryPickerViewModel.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/SettingsActivity.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/SettingsScreen.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/SettingsUiState.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/SettingsViewModel.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/**/*.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/theme/KeyboardTheme.kt`
- `app/src/test/kotlin/**/*.kt`
- `app/src/androidTest/kotlin/**/*.kt`
- `docs/architecture.md`

## Task 1: Baseline Verification

**Files:**
- Read: `docs/superpowers/specs/2026-05-24-app-architecture-refactor-design.md`
- Read: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModel.kt`
- Read: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardImeService.kt`

- [ ] **Step 1: Confirm clean working tree**

Run:

```powershell
git status --short --branch
```

Expected: only the current branch line. Stop if there are uncommitted files not created by this plan.

- [ ] **Step 2: Run unit test baseline**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run debug build baseline**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit baseline if generated files changed**

Run:

```powershell
git status --short
```

Expected: no source changes. If Gradle generated ignored files only, do not commit.

## Task 2: Move Pure Domain Models

**Files:**
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/KeyboardField.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/KeyboardFieldType.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/KeyboardFieldUiModel.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/KeyboardFieldMappings.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/session/KeyboardSession.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/session/KeyboardSessionSnapshot.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardSubtype.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/MainKeyboardLayout.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/DefaultInputMode.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/EntryFieldDisplayMode.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardUtilitySlots.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardUtilityItemId.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/KeyboardSettings.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/KeyboardThemeMode.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/KeyboardHeightMode.kt`
- Modify: `app/src/main/kotlin/**/*.kt`
- Modify: `app/src/test/kotlin/**/*.kt`
- Modify: `app/src/androidTest/kotlin/**/*.kt`

- [ ] **Step 1: Move files with git**

Run:

```powershell
New-Item -ItemType Directory -Force `
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/field,`
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard,`
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/session,`
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/settings

git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/KeyboardField.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/field/KeyboardField.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/KeyboardFieldType.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/field/KeyboardFieldType.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/KeyboardFieldUiModel.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/field/KeyboardFieldSummary.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/KeyboardFieldMappings.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/field/KeyboardFieldMappings.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/session/KeyboardSession.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/session/KeyboardSession.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/session/KeyboardSessionSnapshot.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/session/KeyboardSessionSnapshot.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardSubtype.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardSubtype.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/MainKeyboardLayout.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/MainKeyboardLayout.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/DefaultInputMode.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/DefaultInputMode.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/EntryFieldDisplayMode.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/EntryFieldDisplayMode.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardUtilitySlots.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilitySlots.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardUtilityItemId.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilityItemId.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/KeyboardSettings.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/settings/KeyboardSettings.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/KeyboardThemeMode.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/settings/KeyboardThemeMode.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/KeyboardHeightMode.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/settings/KeyboardHeightMode.kt
```

Expected: each `git mv` exits successfully.

- [ ] **Step 2: Update moved file package declarations**

Edit these moved files so their first line is exactly:

```kotlin
package io.github.togls.kp2acomposekeyboard.domain.field
```

for:

```text
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/field/KeyboardField.kt
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/field/KeyboardFieldType.kt
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/field/KeyboardFieldSummary.kt
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/field/KeyboardFieldMappings.kt
```

Edit `KeyboardFieldSummary.kt` to contain:

```kotlin
package io.github.togls.kp2acomposekeyboard.domain.field

data class KeyboardFieldSummary(
    val id: String,
    val label: String,
    val type: KeyboardFieldType,
    val sensitive: Boolean,
)
```

Edit `KeyboardFieldMappings.kt` so `toUiModel()` becomes:

```kotlin
fun KeyboardField.toSummary(): KeyboardFieldSummary {
    return KeyboardFieldSummary(
        id = id,
        label = label,
        type = type,
        sensitive = sensitive,
    )
}
```

- [ ] **Step 3: Update keyboard domain package declarations**

Set these package declarations:

```kotlin
package io.github.togls.kp2acomposekeyboard.domain.keyboard
```

for:

```text
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardSubtype.kt
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/MainKeyboardLayout.kt
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/DefaultInputMode.kt
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/EntryFieldDisplayMode.kt
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilitySlots.kt
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilityItemId.kt
```

Update `KeyboardSubtype.kt`, `KeyboardUtilitySlots.kt`, and `KeyboardUtilityItemId.kt` imports so same-package domain keyboard types use no imports.

- [ ] **Step 4: Update session domain package declarations**

Set these package declarations:

```kotlin
package io.github.togls.kp2acomposekeyboard.domain.session
```

for:

```text
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/session/KeyboardSession.kt
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/session/KeyboardSessionSnapshot.kt
```

Update `KeyboardSession.kt` import to:

```kotlin
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardField
```

Update `KeyboardSessionSnapshot.kt` import to:

```kotlin
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldSummary
```

and update list types from `KeyboardFieldUiModel` to `KeyboardFieldSummary`.

- [ ] **Step 5: Update settings domain package declarations**

Set these package declarations:

```kotlin
package io.github.togls.kp2acomposekeyboard.domain.settings
```

for:

```text
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/settings/KeyboardSettings.kt
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/settings/KeyboardThemeMode.kt
app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/settings/KeyboardHeightMode.kt
```

In `KeyboardSettings.kt`, import utility slots from the domain keyboard package:

```kotlin
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlots
```

- [ ] **Step 6: Run compile to reveal unresolved imports**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected: failure only from unresolved imports and renamed `KeyboardFieldUiModel` or `toUiModel` references.

- [ ] **Step 7: Replace domain imports and names**

Apply these exact replacements across `app/src/main/kotlin`, `app/src/test/kotlin`, and `app/src/androidTest/kotlin`:

```text
io.github.togls.kp2acomposekeyboard.domain.KeyboardField -> io.github.togls.kp2acomposekeyboard.domain.field.KeyboardField
io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldType -> io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldType
io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel -> io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldSummary
io.github.togls.kp2acomposekeyboard.domain.toUiModel -> io.github.togls.kp2acomposekeyboard.domain.field.toSummary
KeyboardFieldUiModel -> KeyboardFieldSummary
toUiModel() -> toSummary()
io.github.togls.kp2acomposekeyboard.session.KeyboardSession -> io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
io.github.togls.kp2acomposekeyboard.session.KeyboardSessionSnapshot -> io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSessionSnapshot
io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardSubtype -> io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardSubtype
io.github.togls.kp2acomposekeyboard.feature.keyboard.MainKeyboardLayout -> io.github.togls.kp2acomposekeyboard.domain.keyboard.MainKeyboardLayout
io.github.togls.kp2acomposekeyboard.feature.keyboard.DefaultInputMode -> io.github.togls.kp2acomposekeyboard.domain.keyboard.DefaultInputMode
io.github.togls.kp2acomposekeyboard.feature.keyboard.EntryFieldDisplayMode -> io.github.togls.kp2acomposekeyboard.domain.keyboard.EntryFieldDisplayMode
io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilitySlots -> io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlots
io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilityItemId -> io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilityItemId
io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings -> io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardSettings
io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardThemeMode -> io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardThemeMode
io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardHeightMode -> io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardHeightMode
```

- [ ] **Step 8: Verify domain no longer imports platform libraries**

Run:

```powershell
rg -n "android\\.|androidx\\.|dagger\\.|keepass2android" app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain
```

Expected: no matches.

- [ ] **Step 9: Run tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 10: Commit domain move**

Run:

```powershell
git add app/src/main app/src/test app/src/androidTest
git commit -m "refactor(domain): move pure app models into domain packages" -m "Separate pure field, session, keyboard, and settings models from feature and data concerns while preserving behavior." -m "* Domain: Move pure models into focused domain subpackages and rename the safe field projection to KeyboardFieldSummary." -m "* Imports: Update main, unit test, and Android test references to the new package names." -m "* Validation: .\\gradlew.bat :app:testDebugUnitTest"
```

Expected: commit succeeds.

## Task 3: Move Data and Platform Adapters

**Files:**
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/kp2a/*`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/session/KeyboardSessionRepository.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/session/KeyboardSessionMappings.kt`
- Delete: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/session/KeyboardSessionSnapshotFactory.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/SettingsRepository.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/KeyboardUtilitySlotsPreferenceCodec.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/*`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/kotlin/**/*.kt`
- Modify: `app/src/test/kotlin/**/*.kt`
- Modify: `app/src/androidTest/kotlin/**/*.kt`

- [ ] **Step 1: Move adapter files with git**

Run:

```powershell
New-Item -ItemType Directory -Force `
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a,`
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/session,`
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/settings,`
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/platform/ime,`
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/platform/input

git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/kp2a/*.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/session/KeyboardSessionRepository.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/session/KeyboardSessionRepository.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/session/KeyboardSessionMappings.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/session/KeyboardSessionMappings.kt
git rm app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/session/KeyboardSessionSnapshotFactory.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/SettingsRepository.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/settings/SettingsRepository.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/KeyboardUtilitySlotsPreferenceCodec.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/settings/KeyboardUtilitySlotsPreferenceCodec.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardImeService.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/platform/ime/KeyboardImeService.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeRegistry.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/platform/ime/KeyboardSubtypeRegistry.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeSynchronizer.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/platform/ime/KeyboardSubtypeSynchronizer.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardViewModelFactory.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/platform/ime/KeyboardViewModelFactory.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/InputConnectionDispatcher.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/platform/input/InputConnectionDispatcher.kt
```

Expected: each move exits successfully.

- [ ] **Step 2: Update package declarations**

Set package declarations as follows:

```text
data/kp2a/*.kt -> package io.github.togls.kp2acomposekeyboard.data.kp2a
data/session/KeyboardSessionRepository.kt -> package io.github.togls.kp2acomposekeyboard.data.session
data/session/KeyboardSessionMappings.kt -> package io.github.togls.kp2acomposekeyboard.data.session
data/settings/SettingsRepository.kt -> package io.github.togls.kp2acomposekeyboard.data.settings
data/settings/KeyboardUtilitySlotsPreferenceCodec.kt -> package io.github.togls.kp2acomposekeyboard.data.settings
platform/ime/*.kt -> package io.github.togls.kp2acomposekeyboard.platform.ime
platform/input/InputConnectionDispatcher.kt -> package io.github.togls.kp2acomposekeyboard.platform.input
```

- [ ] **Step 3: Update manifest service and receiver names**

Open `app/src/main/AndroidManifest.xml` and replace old package names with:

```xml
<service
    android:name=".platform.ime.KeyboardImeService"
```

and:

```xml
<receiver
    android:name=".data.kp2a.Kp2aPluginAccessReceiver"
```

Keep all existing attributes and intent filters unchanged.

- [ ] **Step 4: Update imports for moved adapters**

Apply these exact replacements across main, unit test, and Android test sources:

```text
io.github.togls.kp2acomposekeyboard.kp2a. -> io.github.togls.kp2acomposekeyboard.data.kp2a.
io.github.togls.kp2acomposekeyboard.session.KeyboardSessionRepository -> io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
io.github.togls.kp2acomposekeyboard.session.toSnapshot -> io.github.togls.kp2acomposekeyboard.data.session.toSnapshot
io.github.togls.kp2acomposekeyboard.feature.settings.SettingsRepository -> io.github.togls.kp2acomposekeyboard.data.settings.SettingsRepository
io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardUtilitySlotsPreferenceCodec -> io.github.togls.kp2acomposekeyboard.data.settings.KeyboardUtilitySlotsPreferenceCodec
io.github.togls.kp2acomposekeyboard.ime. -> io.github.togls.kp2acomposekeyboard.platform.ime.
io.github.togls.kp2acomposekeyboard.ime.InputConnectionDispatcher -> io.github.togls.kp2acomposekeyboard.platform.input.InputConnectionDispatcher
```

- [ ] **Step 5: Fix `data/session/KeyboardSessionMappings.kt` imports**

Ensure the file imports only domain field/session types:

```kotlin
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardField
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.field.toSummary
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSessionSnapshot
```

Ensure the mapping calls `toSummary()` in all three projection lists.

- [ ] **Step 6: Fix `data/session/KeyboardSessionRepository.kt` imports**

Ensure it imports the domain session model and mapping:

```kotlin
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSessionSnapshot
```

Keep `SecureLog` in this file because repository state changes are important diagnostic events.

- [ ] **Step 7: Run compile**

Run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Run tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit adapter move**

Run:

```powershell
git add app/src/main app/src/test app/src/androidTest
git commit -m "refactor(architecture): move data and platform adapters" -m "Move external protocol, persistence, session storage, and IME platform code behind clearer package boundaries without changing behavior." -m "* Data: Move KP2A, DataStore, and in-memory session implementations into data packages." -m "* Platform: Move IME, subtype, and input connection adapters into platform packages." -m "* Validation: .\\gradlew.bat :app:testDebugUnitTest"
```

Expected: commit succeeds.

## Task 4: Move Policy and Remove KP2A SDK Dependency From Domain

**Files:**
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/KeyboardFieldClassifier.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/SensitiveFieldPolicy.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/kp2a/Kp2aEntryMapper.kt`
- Modify: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/domain/KeyboardFieldClassifierTest.kt`
- Modify: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/domain/SensitiveFieldPolicyTest.kt`
- Modify: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/kp2a/Kp2aEntryMapperTest.kt`

- [ ] **Step 1: Move policy files**

Run:

```powershell
New-Item -ItemType Directory -Force app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/policy
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/KeyboardFieldClassifier.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/policy/KeyboardFieldClassifier.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/SensitiveFieldPolicy.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/policy/SensitiveFieldPolicy.kt
```

Expected: both moves succeed.

- [ ] **Step 2: Update policy package declarations and imports**

Set package declarations to:

```kotlin
package io.github.togls.kp2acomposekeyboard.domain.policy
```

In both files, import:

```kotlin
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldType
```

Remove every `keepass2android.pluginsdk` import from the policy package.

- [ ] **Step 3: Replace exact Keepass2Android constants in classifier**

In `KeyboardFieldClassifier.classify`, remove checks against `KeepassDefs.UserNameField`, `KeepassDefs.PasswordField`, `KeepassDefs.UrlField`, and `KeepassDefs.NotesField`. Keep the normalized-key branch so the behavior is preserved for those labels after `data/kp2a` normalization.

The first branch should start with:

```kotlin
return when {
    normalizedKey == "username" ||
            normalizedKey == "user" ||
            normalizedKey == "login" ||
            normalizedKey == "account" -> KeyboardFieldType.Username
```

- [ ] **Step 4: Normalize KP2A field keys before classification**

In `data/kp2a/Kp2aEntryMapper.kt`, add:

```kotlin
private fun normalizeKp2aFieldKey(key: String): String {
    return when (key) {
        KeepassDefs.UserNameField -> "username"
        KeepassDefs.PasswordField -> "password"
        KeepassDefs.UrlField -> "url"
        KeepassDefs.NotesField -> "notes"
        else -> key
    }
}
```

Then change `mapField` so classifier and policy use the normalized key:

```kotlin
val normalizedKey = normalizeKp2aFieldKey(key)
val type = fieldClassifier.classify(normalizedKey)
```

Use `normalizedKey` for `displayLabel` and `sensitiveFieldPolicy.isSensitive`, but keep the original `key` in `KeyboardField.key`.

- [ ] **Step 5: Update imports for moved policies**

Apply these replacements:

```text
io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldClassifier -> io.github.togls.kp2acomposekeyboard.domain.policy.KeyboardFieldClassifier
io.github.togls.kp2acomposekeyboard.domain.SensitiveFieldPolicy -> io.github.togls.kp2acomposekeyboard.domain.policy.SensitiveFieldPolicy
```

- [ ] **Step 6: Verify domain import boundary**

Run:

```powershell
rg -n "android\\.|androidx\\.|dagger\\.|keepass2android" app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain
```

Expected: no matches.

- [ ] **Step 7: Run focused tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*KeyboardFieldClassifierTest" --tests "*SensitiveFieldPolicyTest" --tests "*Kp2aEntryMapperTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit policy boundary**

Run:

```powershell
git add app/src/main app/src/test
git commit -m "refactor(domain): isolate field policies from kp2a sdk" -m "Keep field classification and sensitivity policy independent from external SDK constants by moving KP2A normalization into the data adapter." -m "* Domain: Move field policy classes into domain.policy and remove SDK imports." -m "* KP2A: Normalize standard field keys before classification while preserving original field keys." -m "* Validation: .\\gradlew.bat :app:testDebugUnitTest --tests \"*KeyboardFieldClassifierTest\" --tests \"*SensitiveFieldPolicyTest\" --tests \"*Kp2aEntryMapperTest\""
```

Expected: commit succeeds.

## Task 5: Add Application Use Cases

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/CommitFieldResult.kt`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/CommitKeyboardFieldUseCase.kt`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/ClearKeyboardSessionUseCase.kt`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/ObserveKeyboardSessionSnapshotUseCase.kt`
- Create: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/CommitKeyboardFieldUseCaseTest.kt`
- Create: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/ObserveKeyboardSessionSnapshotUseCaseTest.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/session/KeyboardSessionRepository.kt`

- [ ] **Step 1: Add repository commit lookup result support**

Modify `data/session/KeyboardSessionRepository.kt` so `getFieldValue` no longer logs blank IDs and becomes a simple query:

```kotlin
fun getFieldValue(fieldId: String): String? {
    return _session.value
        ?.fields
        ?.firstOrNull { field -> field.id == fieldId }
        ?.value
}
```

Expected: repository only queries state; commit decisions move into the use case.

- [ ] **Step 2: Write failing commit use case tests**

Create `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/CommitKeyboardFieldUseCaseTest.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.application.keyboard

import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardField
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
import org.junit.Assert.assertEquals
import org.junit.Test

class CommitKeyboardFieldUseCaseTest {

    @Test
    fun blankFieldId_returnsIgnoredReason() {
        val useCase = CommitKeyboardFieldUseCase(KeyboardSessionRepository())

        val result = useCase(" ")

        assertEquals(CommitFieldResult.Ignored(CommitFieldIgnoredReason.BlankFieldId), result)
    }

    @Test
    fun missingField_returnsIgnoredReason() {
        val repository = KeyboardSessionRepository()
        repository.setSession(session(field("username", "octocat")))
        val useCase = CommitKeyboardFieldUseCase(repository)

        val result = useCase("password")

        assertEquals(CommitFieldResult.Ignored(CommitFieldIgnoredReason.FieldNotFound), result)
    }

    @Test
    fun emptyFieldValue_returnsIgnoredReason() {
        val repository = KeyboardSessionRepository()
        repository.setSession(session(field("username", "")))
        val useCase = CommitKeyboardFieldUseCase(repository)

        val result = useCase("username")

        assertEquals(CommitFieldResult.Ignored(CommitFieldIgnoredReason.EmptyValue), result)
    }

    @Test
    fun existingFieldValue_returnsCommitWithoutChangingText() {
        val repository = KeyboardSessionRepository()
        repository.setSession(session(field("password", "field-value-for-commit")))
        val useCase = CommitKeyboardFieldUseCase(repository)

        val result = useCase("password")

        assertEquals(CommitFieldResult.Commit("field-value-for-commit"), result)
    }

    private fun session(field: KeyboardField): KeyboardSession {
        return KeyboardSession(
            entryId = "entry-1",
            entryName = "GitHub",
            fields = listOf(field),
            createdAtMillis = 123L,
        )
    }

    private fun field(
        id: String,
        value: String,
    ): KeyboardField {
        return KeyboardField(
            id = id,
            key = id,
            label = id,
            value = value,
            type = KeyboardFieldType.Custom,
            sensitive = true,
        )
    }
}
```

- [ ] **Step 3: Run commit use case test to verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*CommitKeyboardFieldUseCaseTest"
```

Expected: test compilation fails because `CommitKeyboardFieldUseCase` and `CommitFieldResult` do not exist.

- [ ] **Step 4: Implement commit result types**

Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/CommitFieldResult.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.application.keyboard

sealed interface CommitFieldResult {
    data class Commit(val text: String) : CommitFieldResult
    data class Ignored(val reason: CommitFieldIgnoredReason) : CommitFieldResult
}

enum class CommitFieldIgnoredReason {
    BlankFieldId,
    FieldNotFound,
    EmptyValue,
}
```

- [ ] **Step 5: Implement commit use case**

Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/CommitKeyboardFieldUseCase.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.application.keyboard

import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import javax.inject.Inject

class CommitKeyboardFieldUseCase @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
) {

    operator fun invoke(fieldId: String): CommitFieldResult {
        if (fieldId.isBlank()) {
            return CommitFieldResult.Ignored(CommitFieldIgnoredReason.BlankFieldId)
        }

        val value = sessionRepository.getFieldValue(fieldId)
            ?: return CommitFieldResult.Ignored(CommitFieldIgnoredReason.FieldNotFound)

        if (value.isEmpty()) {
            return CommitFieldResult.Ignored(CommitFieldIgnoredReason.EmptyValue)
        }

        return CommitFieldResult.Commit(value)
    }
}
```

- [ ] **Step 6: Write failing snapshot observer test**

Create `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/ObserveKeyboardSessionSnapshotUseCaseTest.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.application.keyboard

import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardField
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveKeyboardSessionSnapshotUseCaseTest {

    @Test
    fun invoke_emitsNullWhenNoSessionExists() = runTest {
        val useCase = ObserveKeyboardSessionSnapshotUseCase(KeyboardSessionRepository())

        assertNull(useCase().first())
    }

    @Test
    fun invoke_emitsSafeSnapshotWithoutFieldValue() = runTest {
        val repository = KeyboardSessionRepository()
        repository.setSession(
            KeyboardSession(
                entryId = "entry-1",
                entryName = "GitHub",
                fields = listOf(
                    KeyboardField(
                        id = "password",
                        key = "Password",
                        label = "Password",
                        value = "field-value-for-snapshot",
                        type = KeyboardFieldType.Password,
                        sensitive = true,
                    ),
                ),
                createdAtMillis = 123L,
            ),
        )
        val useCase = ObserveKeyboardSessionSnapshotUseCase(repository)

        val snapshot = useCase().first()

        assertEquals("GitHub", snapshot?.entryName)
        assertEquals("password", snapshot?.fixedFields?.first()?.id)
        assertFalse(snapshot.toString().contains("field-value-for-snapshot"))
    }
}
```

- [ ] **Step 7: Implement snapshot observer and clear use cases**

Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/ObserveKeyboardSessionSnapshotUseCase.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.application.keyboard

import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.data.session.toSnapshot
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSessionSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveKeyboardSessionSnapshotUseCase @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
) {

    operator fun invoke(): Flow<KeyboardSessionSnapshot?> {
        return sessionRepository.session.map { session ->
            session?.toSnapshot()
        }
    }
}
```

Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/keyboard/ClearKeyboardSessionUseCase.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.application.keyboard

import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import javax.inject.Inject

class ClearKeyboardSessionUseCase @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
) {

    operator fun invoke() {
        sessionRepository.clear()
    }
}
```

- [ ] **Step 8: Run use case tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*CommitKeyboardFieldUseCaseTest" --tests "*ObserveKeyboardSessionSnapshotUseCaseTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit application use cases**

Run:

```powershell
git add app/src/main app/src/test
git commit -m "refactor(keyboard): add session application use cases" -m "Introduce focused application use cases for field commits, safe session snapshots, and session clearing so ViewModel code no longer needs to read raw session data directly." -m "* Application: Add commit, observe snapshot, and clear session use cases." -m "* Session: Keep repository as storage and move commit decision reasons into the use case." -m "* Validation: .\\gradlew.bat :app:testDebugUnitTest --tests \"*CommitKeyboardFieldUseCaseTest\" --tests \"*ObserveKeyboardSessionSnapshotUseCaseTest\""
```

Expected: commit succeeds.

## Task 6: Move Settings Store Port and Update DI

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/settings/KeyboardSettingsStore.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/settings/SettingsRepository.kt`
- Delete: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/KeyboardSettingsStore.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/di/AppModule.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModel.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/platform/ime/KeyboardViewModelFactory.kt`
- Modify: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModelUtilityTest.kt`
- Modify: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModelSubtypeTest.kt`

- [ ] **Step 1: Create settings store port**

Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/settings/KeyboardSettingsStore.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.application.settings

import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlots
import kotlinx.coroutines.flow.Flow

interface KeyboardSettingsStore {
    val settings: Flow<KeyboardSettings>

    suspend fun updateUtilitySlots(slots: KeyboardUtilitySlots)
}
```

- [ ] **Step 2: Update repository implementation**

In `data/settings/SettingsRepository.kt`, replace:

```kotlin
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettingsStore
```

with:

```kotlin
import io.github.togls.kp2acomposekeyboard.application.settings.KeyboardSettingsStore
import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardHeightMode
import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardThemeMode
```

- [ ] **Step 3: Delete old settings store interface**

Run:

```powershell
git rm app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/KeyboardSettingsStore.kt
```

Expected: file is staged for deletion.

- [ ] **Step 4: Update DI import**

In `di/AppModule.kt`, imports should be:

```kotlin
import io.github.togls.kp2acomposekeyboard.application.settings.KeyboardSettingsStore
import io.github.togls.kp2acomposekeyboard.data.settings.SettingsRepository
```

Keep the provider body unchanged.

- [ ] **Step 5: Update feature and test imports**

Apply this replacement:

```text
io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettingsStore -> io.github.togls.kp2acomposekeyboard.application.settings.KeyboardSettingsStore
io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilitySlots -> io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlots
io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilityItemId -> io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilityItemId
```

- [ ] **Step 6: Run focused settings and ViewModel tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*SettingsRepositoryTest" --tests "*KeyboardViewModelUtilityTest" --tests "*KeyboardViewModelSubtypeTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit settings port**

Run:

```powershell
git add app/src/main app/src/test
git commit -m "refactor(settings): move keyboard settings store port" -m "Move the runtime settings port into the application layer so keyboard runtime code depends on a small capability interface instead of the settings feature package." -m "* Application: Add KeyboardSettingsStore as the runtime settings port." -m "* Data: Keep SettingsRepository as the DataStore implementation." -m "* Validation: .\\gradlew.bat :app:testDebugUnitTest --tests \"*SettingsRepositoryTest\" --tests \"*KeyboardViewModelUtilityTest\" --tests \"*KeyboardViewModelSubtypeTest\""
```

Expected: commit succeeds.

## Task 7: Refactor KeyboardViewModel To Use Application Boundaries

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModel.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/platform/ime/KeyboardViewModelFactory.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/session/SessionTimeoutController.kt`
- Modify: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModelUtilityTest.kt`
- Modify: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModelSubtypeTest.kt`

- [ ] **Step 1: Update session timeout controller dependency**

In `application/session/SessionTimeoutController.kt`, replace repository dependency with:

```kotlin
import io.github.togls.kp2acomposekeyboard.application.keyboard.ClearKeyboardSessionUseCase
```

Constructor:

```kotlin
class SessionTimeoutController @Inject constructor(
    private val clearKeyboardSession: ClearKeyboardSessionUseCase,
) {
```

Replace `sessionRepository.clear()` calls with:

```kotlin
clearKeyboardSession()
```

- [ ] **Step 2: Update ViewModel constructor**

In `KeyboardViewModel.kt`, constructor should be:

```kotlin
class KeyboardViewModel(
    private val observeKeyboardSessionSnapshot: ObserveKeyboardSessionSnapshotUseCase,
    private val commitKeyboardField: CommitKeyboardFieldUseCase,
    private val sessionTimeoutController: SessionTimeoutController,
    private val settingsStore: KeyboardSettingsStore,
) : ViewModel() {
```

Required imports:

```kotlin
import io.github.togls.kp2acomposekeyboard.application.keyboard.CommitFieldResult
import io.github.togls.kp2acomposekeyboard.application.keyboard.CommitKeyboardFieldUseCase
import io.github.togls.kp2acomposekeyboard.application.keyboard.ObserveKeyboardSessionSnapshotUseCase
import io.github.togls.kp2acomposekeyboard.application.session.SessionTimeoutController
import io.github.togls.kp2acomposekeyboard.application.settings.KeyboardSettingsStore
import io.github.togls.kp2acomposekeyboard.domain.keyboard.DefaultInputMode
import io.github.togls.kp2acomposekeyboard.domain.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardSubtype
import io.github.togls.kp2acomposekeyboard.domain.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSessionSnapshot
```

Remove import of `data.session.KeyboardSessionRepository`.

- [ ] **Step 3: Replace session observation**

Replace `observeSession()` body with:

```kotlin
private fun observeSession() {
    viewModelScope.launch {
        observeKeyboardSessionSnapshot().collect { snapshot ->
            if (snapshot != null) {
                sessionTimeoutController.restartTimeout(viewModelScope)
            } else {
                sessionTimeoutController.cancelTimeout()
            }

            _uiState.update { state ->
                when {
                    snapshot != null -> state.withSessionSnapshot(snapshot)
                    state.hasActiveSession -> state.withoutSession()
                    else -> state
                }
            }
        }
    }
}
```

- [ ] **Step 4: Replace field commit behavior**

Replace `commitField(fieldId: String)` with:

```kotlin
private fun commitField(fieldId: String) {
    when (val result = commitKeyboardField(fieldId)) {
        is CommitFieldResult.Commit -> {
            sendEffect(KeyboardEffect.CommitText(result.text))
        }

        is CommitFieldResult.Ignored -> {
            SecureLog.d(
                message = "Field commit ignored",
                "reason" to result.reason.name,
            )
        }
    }
}
```

Do not log `result.text`.

- [ ] **Step 5: Update factory wiring**

In `platform/ime/KeyboardViewModelFactory.kt`, constructor should inject:

```kotlin
private val observeKeyboardSessionSnapshot: ObserveKeyboardSessionSnapshotUseCase,
private val commitKeyboardField: CommitKeyboardFieldUseCase,
private val sessionTimeoutController: SessionTimeoutController,
private val settingsStore: KeyboardSettingsStore,
```

`create()` should pass all four values to `KeyboardViewModel`.

- [ ] **Step 6: Update ViewModel test helpers**

In `KeyboardViewModelUtilityTest` and `KeyboardViewModelSubtypeTest`, build the ViewModel with:

```kotlin
val sessionRepository = KeyboardSessionRepository()
val clearKeyboardSession = ClearKeyboardSessionUseCase(sessionRepository)
return KeyboardViewModel(
    observeKeyboardSessionSnapshot = ObserveKeyboardSessionSnapshotUseCase(sessionRepository),
    commitKeyboardField = CommitKeyboardFieldUseCase(sessionRepository),
    sessionTimeoutController = SessionTimeoutController(clearKeyboardSession),
    settingsStore = settingsStore,
)
```

Required imports:

```kotlin
import io.github.togls.kp2acomposekeyboard.application.keyboard.ClearKeyboardSessionUseCase
import io.github.togls.kp2acomposekeyboard.application.keyboard.CommitKeyboardFieldUseCase
import io.github.togls.kp2acomposekeyboard.application.keyboard.ObserveKeyboardSessionSnapshotUseCase
import io.github.togls.kp2acomposekeyboard.application.session.SessionTimeoutController
import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
```

- [ ] **Step 7: Verify ViewModel no longer imports session repository**

Run:

```powershell
rg -n "KeyboardSessionRepository|getFieldValue|getSnapshot" app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModel.kt
```

Expected: no matches.

- [ ] **Step 8: Run ViewModel tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*KeyboardViewModelUtilityTest" --tests "*KeyboardViewModelSubtypeTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit ViewModel boundary**

Run:

```powershell
git add app/src/main app/src/test
git commit -m "refactor(keyboard): route session access through use cases" -m "Reduce KeyboardViewModel coupling by routing safe session observation and field commits through application use cases." -m "* Keyboard: Replace direct session repository access in KeyboardViewModel." -m "* Session: Let SessionTimeoutController clear through the application use case." -m "* Validation: .\\gradlew.bat :app:testDebugUnitTest --tests \"*KeyboardViewModelUtilityTest\" --tests \"*KeyboardViewModelSubtypeTest\""
```

Expected: commit succeeds.

## Task 8: Run Boundary Checks and Full Validation

**Files:**
- Modify: import cleanup only if checks reveal stale references.

- [ ] **Step 1: Check stale old packages**

Run:

```powershell
rg -n "io\\.github\\.togls\\.kp2acomposekeyboard\\.(kp2a|ime|session|feature\\.settings\\.KeyboardSettings|feature\\.settings\\.KeyboardThemeMode|feature\\.settings\\.KeyboardHeightMode|feature\\.settings\\.KeyboardSettingsStore|feature\\.keyboard\\.KeyboardUtilitySlots|feature\\.keyboard\\.KeyboardUtilityItemId|domain\\.KeyboardField|domain\\.KeyboardFieldType|domain\\.KeyboardFieldUiModel|domain\\.KeyboardFieldClassifier|domain\\.SensitiveFieldPolicy)" app/src/main/kotlin app/src/test/kotlin app/src/androidTest/kotlin
```

Expected: no matches.

- [ ] **Step 2: Check domain dependency boundary**

Run:

```powershell
rg -n "android\\.|androidx\\.|dagger\\.|javax\\.inject|keepass2android|DataStore|Preferences" app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain
```

Expected: no matches.

- [ ] **Step 3: Check feature and UI do not reference raw field values**

Run:

```powershell
rg -n "KeyboardField\\(|\\.value|getFieldValue|KeyboardEffect\\.CommitText\\.text" app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui
```

Expected: no matches except declarations of `KeyboardEffect.CommitText(val text: String)` and intent text for normal keyboard characters.

- [ ] **Step 4: Run full unit tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Run debug build**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit validation cleanup if changes were needed**

Run:

```powershell
git status --short
```

If there are import cleanup changes, commit them:

```powershell
git add app/src/main app/src/test app/src/androidTest
git commit -m "refactor(architecture): clean stale package references" -m "Remove stale imports and verify architecture boundary checks after the package migration." -m "* Boundaries: Confirm domain has no Android, Compose, DataStore, Hilt, or KP2A SDK imports." -m "* Validation: .\\gradlew.bat :app:testDebugUnitTest; .\\gradlew.bat :app:assembleDebug"
```

Expected: commit succeeds when cleanup changes exist. If there are no changes, continue without a commit.

## Task 9: Update Architecture Documentation

**Files:**
- Modify: `docs/architecture.md`

- [ ] **Step 1: Update module overview**

Replace the old package overview in `docs/architecture.md` with:

```text
domain/
  field/
  keyboard/
  policy/
  session/
  settings/
application/
  keyboard/
  session/
  settings/
data/
  kp2a/
  session/
  settings/
platform/
  ime/
  input/
feature/
  keyboard/
  entrypicker/
  settings/
ui/
  keyboard/
  theme/
security/
di/
```

- [ ] **Step 2: Update responsibility text**

Ensure `docs/architecture.md` states:

```text
Domain code contains pure models and policies only. It must not depend on Android, Compose, DataStore, Hilt, or the Keepass2Android SDK.

Data code owns external protocol and persistence implementations, including Keepass2Android mapping, DataStore settings, and in-memory session storage.

Application code owns focused keyboard/session use cases that protect the sensitive session boundary. It is intentionally small and does not wrap every repository method.

Platform code owns Android IME hosting, InputConnection dispatching, subtype synchronization, and platform activity launches.
```

- [ ] **Step 3: Update field commit flow**

Replace the field commit flow with:

```text
FieldButton
    -> KeyboardIntent.CommitField(fieldId)
KeyboardViewModel
    -> CommitKeyboardFieldUseCase
    -> KeyboardSessionRepository.getFieldValue(fieldId)
    -> KeyboardEffect.CommitText(value)
KeyboardImeService
    -> InputConnectionDispatcher.commitText(value)
```

- [ ] **Step 4: Keep required disclaimer**

Ensure the document includes this exact text:

```text
This project is primarily built for personal devices and personal needs. It does not guarantee compatibility with all Android, HyperOS, MIUI, or vendor ROM versions.
```

- [ ] **Step 5: Run documentation checks**

Run:

```powershell
rg -n "KeyboardBottomGap|Kp2aActionReceiver|feature/settings/SettingsRepository|session/KeyboardSessionRepository|ime/KeyboardImeService" docs/architecture.md
```

Expected: no matches.

- [ ] **Step 6: Run final validation**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both commands end with `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit docs update**

Run:

```powershell
git add docs/architecture.md
git commit -m "docs(architecture): describe app module boundaries" -m "Update the architecture documentation to match the refactored app package structure, dependency direction, and sensitive field commit flow." -m "* Architecture: Document domain, application, data, platform, feature, and UI responsibilities." -m "* Security: Keep the field commit flow and ROM compatibility disclaimer explicit." -m "* Validation: .\\gradlew.bat :app:testDebugUnitTest; .\\gradlew.bat :app:assembleDebug"
```

Expected: commit succeeds.

## Task 10: Final Review

**Files:**
- Review: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/**`
- Review: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/**`
- Review: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/**`
- Review: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/platform/**`
- Review: `docs/architecture.md`

- [ ] **Step 1: Inspect final git status**

Run:

```powershell
git status --short --branch
```

Expected: clean working tree and branch ahead count reflecting this refactor's commits.

- [ ] **Step 2: Inspect changed package layout**

Run:

```powershell
rg --files app/src/main/kotlin/io/github/togls/kp2acomposekeyboard | Sort-Object
```

Expected: files appear under `domain`, `application`, `data`, `platform`, `feature`, `ui`, `security`, and `di`; no files remain under root `kp2a`, `ime`, or `session` packages.

- [ ] **Step 3: Inspect final log**

Run:

```powershell
git log --oneline -8
```

Expected: recent commits include domain move, adapter move, policy boundary, application use cases, ViewModel boundary, validation cleanup if needed, and architecture docs.

- [ ] **Step 4: Record final verification output for handoff**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both commands end with `BUILD SUCCESSFUL`. Include these two commands and their success in the final handoff.
