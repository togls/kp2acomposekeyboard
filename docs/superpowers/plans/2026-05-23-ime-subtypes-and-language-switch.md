# IME Subtypes and Language Switch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `Entry` the always-available system IME subtype, expose `English (US)` only when enabled in app settings, and add a language switch key that cycles from Entry to English to the next system IME.

**Architecture:** Keep Android platform subtype objects in the IME layer, keep the ViewModel on a pure `KeyboardSubtype` enum, and synchronize system subtypes from settings through a small injected synchronizer. `KeyboardImeService` owns platform calls for reading the active subtype, switching to a subtype, and switching to the next IME.

**Tech Stack:** Kotlin, Android `InputMethodService`, `InputMethodManager`, `InputMethodSubtype`, Jetpack Compose, Material 3, Hilt, DataStore Preferences, JUnit, Robolectric, Compose UI tests.

---

## File Structure

- Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardSubtype.kt`
  - Pure enum used by ViewModel, state, and effects.
- Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeRegistry.kt`
  - Converts Android `InputMethodSubtype` to `KeyboardSubtype`, builds the dynamic `English (US)` subtype, and exposes stable subtype IDs/extras.
- Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeSynchronizer.kt`
  - Calls `InputMethodManager.setAdditionalInputMethodSubtypes()` from settings/IME lifecycle.
- Modify `app/src/main/res/xml/method.xml`
  - Statically declare only `Entry`.
- Modify `app/src/main/res/values/strings.xml`
  - Add `Entry`, layout setting labels, and language-switch content description.
- Create `app/src/main/res/drawable/ic_language_24.xml`
  - Globe/language key icon.
- Modify `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/*`
  - Persist the `English (US)` subtype setting and render app settings controls.
- Modify `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/*`
  - Track active subtype, English enablement, and language-switch effects.
- Modify `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardImeService.kt`
  - Sync current subtype on startup/input-view start and handle platform switch effects.
- Modify `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/*`
  - Add the language switch key and update row placement.
- Add/update unit tests under `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/` and Compose UI tests under `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/`.

---

### Task 1: Add Subtype Model, Registry, XML, and Parser Tests

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardSubtype.kt`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeRegistry.kt`
- Modify: `app/src/main/res/xml/method.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeRegistryTest.kt`

- [ ] **Step 1: Write the failing subtype registry tests**

Create `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeRegistryTest.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ime

import android.view.inputmethod.InputMethodSubtype
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardSubtype
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class KeyboardSubtypeRegistryTest {

    @Test
    fun fromInputMethodSubtype_mapsEntryExtraToEntry() {
        val subtype = subtypeWithExtra("layout=entry")

        assertEquals(KeyboardSubtype.Entry, KeyboardSubtypeRegistry.fromInputMethodSubtype(subtype))
    }

    @Test
    fun fromInputMethodSubtype_mapsEnglishExtraToEnglishUs() {
        val subtype = subtypeWithExtra("layout=english_us")

        assertEquals(KeyboardSubtype.EnglishUs, KeyboardSubtypeRegistry.fromInputMethodSubtype(subtype))
    }

    @Test
    fun fromInputMethodSubtype_mapsMissingExtraToEntry() {
        val subtype = subtypeWithExtra("")

        assertEquals(KeyboardSubtype.Entry, KeyboardSubtypeRegistry.fromInputMethodSubtype(subtype))
    }

    @Test
    fun fromInputMethodSubtype_mapsUnknownExtraToEntry() {
        val subtype = subtypeWithExtra("layout=unknown")

        assertEquals(KeyboardSubtype.Entry, KeyboardSubtypeRegistry.fromInputMethodSubtype(subtype))
    }

    @Test
    fun englishUsSubtype_usesStableMetadata() {
        val subtype = KeyboardSubtypeRegistry.englishUsInputMethodSubtype()

        assertEquals("keyboard", subtype.mode)
        assertEquals("en_US", subtype.locale)
        assertEquals("layout=english_us", subtype.extraValue)
        assertTrue(subtype.isAsciiCapable)
        assertFalse(subtype.isAuxiliary)
    }

    private fun subtypeWithExtra(extraValue: String): InputMethodSubtype {
        return InputMethodSubtype.InputMethodSubtypeBuilder()
            .setSubtypeId(9999)
            .setSubtypeNameResId(0)
            .setSubtypeLocale("")
            .setSubtypeMode("keyboard")
            .setSubtypeExtraValue(extraValue)
            .build()
    }
}
```

- [ ] **Step 2: Run the failing tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ime.KeyboardSubtypeRegistryTest"
```

Expected: fail because `KeyboardSubtype` and `KeyboardSubtypeRegistry` do not exist.

- [ ] **Step 3: Add the pure subtype enum**

Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardSubtype.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.feature.keyboard

enum class KeyboardSubtype(
    val mainLayout: MainKeyboardLayout,
) {
    Entry(MainKeyboardLayout.Entry),
    EnglishUs(MainKeyboardLayout.Default),
}
```

- [ ] **Step 4: Add the Android subtype registry**

Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeRegistry.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ime

import android.view.inputmethod.InputMethodSubtype
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardSubtype
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings

object KeyboardSubtypeRegistry {
    const val ENTRY_SUBTYPE_ID = 1001
    const val ENGLISH_US_SUBTYPE_ID = 1002
    const val EXTRA_KEY_LAYOUT = "layout"
    const val ENTRY_LAYOUT_EXTRA = "layout=entry"
    const val ENGLISH_US_LAYOUT_EXTRA = "layout=english_us"

    fun fromInputMethodSubtype(subtype: InputMethodSubtype?): KeyboardSubtype {
        return when (subtype?.getExtraValueOf(EXTRA_KEY_LAYOUT)) {
            "english_us" -> KeyboardSubtype.EnglishUs
            "entry" -> KeyboardSubtype.Entry
            else -> KeyboardSubtype.Entry
        }
    }

    fun additionalSubtypes(settings: KeyboardSettings): Array<InputMethodSubtype> {
        return if (settings.englishUsSubtypeEnabled) {
            arrayOf(englishUsInputMethodSubtype())
        } else {
            emptyArray()
        }
    }

    fun inputMethodSubtypeFor(subtype: KeyboardSubtype): InputMethodSubtype? {
        return when (subtype) {
            KeyboardSubtype.Entry -> null
            KeyboardSubtype.EnglishUs -> englishUsInputMethodSubtype()
        }
    }

    fun englishUsInputMethodSubtype(): InputMethodSubtype {
        return InputMethodSubtype.InputMethodSubtypeBuilder()
            .setSubtypeId(ENGLISH_US_SUBTYPE_ID)
            .setSubtypeNameResId(R.string.ime_subtype_en_us)
            .setSubtypeLocale("en_US")
            .setSubtypeMode("keyboard")
            .setSubtypeExtraValue(ENGLISH_US_LAYOUT_EXTRA)
            .setIsAsciiCapable(true)
            .setIsAuxiliary(false)
            .setOverridesImplicitlyEnabledSubtype(false)
            .build()
    }
}
```

- [ ] **Step 5: Update the static IME subtype XML**

Replace `app/src/main/res/xml/method.xml` with:

```xml
<?xml version="1.0" encoding="utf-8"?>
<input-method xmlns:android="http://schemas.android.com/apk/res/android"
    android:supportsSwitchingToNextInputMethod="true">

    <subtype
        android:imeSubtypeExtraValue="layout=entry"
        android:imeSubtypeMode="keyboard"
        android:label="@string/ime_subtype_entry"
        android:subtypeId="1001" />
</input-method>
```

- [ ] **Step 6: Add the Entry subtype string**

Add to `app/src/main/res/values/strings.xml` near the existing IME strings:

```xml
<string name="ime_subtype_entry">Entry</string>
```

Keep the existing `ime_subtype_en_us` string; it will be used by the dynamic subtype builder.

- [ ] **Step 7: Run the subtype registry tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ime.KeyboardSubtypeRegistryTest"
```

Expected: pass.

- [ ] **Step 8: Commit Task 1**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardSubtype.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeRegistry.kt app/src/main/res/xml/method.xml app/src/main/res/values/strings.xml app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeRegistryTest.kt
git commit -m "feat(ime): add entry and english subtype model"
```

---

### Task 2: Add English Subtype Setting and Settings UI

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/KeyboardSettings.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/SettingsRepository.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/SettingsIntent.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/SettingsViewModel.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/SettingsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/settings/KeyboardSettingsTest.kt`
- Test: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/settings/SettingsRepositoryTest.kt`

- [ ] **Step 1: Extend settings model tests**

Update `KeyboardSettingsTest.defaultSettings_areExpectedValues()`:

```kotlin
assertEquals(false, settings.englishUsSubtypeEnabled)
```

Create `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/settings/SettingsRepositoryTest.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.settings

import io.github.togls.kp2acomposekeyboard.feature.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsRepositoryTest {

    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() = runTest {
        repository = SettingsRepository(RuntimeEnvironment.getApplication())
        repository.resetToDefault()
    }

    @After
    fun tearDown() = runTest {
        repository.resetToDefault()
    }

    @Test
    fun settings_defaultsEnglishSubtypeDisabled() = runTest {
        assertFalse(repository.settings.first().englishUsSubtypeEnabled)
    }

    @Test
    fun updateEnglishUsSubtypeEnabled_persistsEnabledState() = runTest {
        repository.updateEnglishUsSubtypeEnabled(true)

        assertTrue(repository.settings.first().englishUsSubtypeEnabled)
    }

    @Test
    fun updateEnglishUsSubtypeEnabled_persistsDisabledState() = runTest {
        repository.updateEnglishUsSubtypeEnabled(true)
        repository.updateEnglishUsSubtypeEnabled(false)

        assertFalse(repository.settings.first().englishUsSubtypeEnabled)
    }
}
```

- [ ] **Step 2: Run the failing settings tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.settings.KeyboardSettingsTest" --tests "io.github.togls.kp2acomposekeyboard.settings.SettingsRepositoryTest"
```

Expected: fail because the settings property and repository method do not exist.

- [ ] **Step 3: Add the setting to `KeyboardSettings`**

Modify `KeyboardSettings` constructor:

```kotlin
data class KeyboardSettings(
    val themeMode: KeyboardThemeMode = KeyboardThemeMode.System,
    val useDynamicColor: Boolean = true,
    val sessionTimeoutSeconds: Int = DEFAULT_SESSION_TIMEOUT_SECONDS,
    val keyboardHeightMode: KeyboardHeightMode = KeyboardHeightMode.Normal,
    val englishUsSubtypeEnabled: Boolean = false,
    val hapticFeedbackEnabled: Boolean = false,
    val keySoundEnabled: Boolean = false,
    val showKeyPreview: Boolean = false,
    val utilitySlots: KeyboardUtilitySlots = KeyboardUtilitySlots(),
)
```

- [ ] **Step 4: Persist the setting in `SettingsRepository`**

Add the update method:

```kotlin
suspend fun updateEnglishUsSubtypeEnabled(enabled: Boolean) {
    context.keyboardSettingsDataStore.edit { preferences ->
        preferences[Keys.ENGLISH_US_SUBTYPE_ENABLED] = enabled
    }
}
```

Add to `toKeyboardSettings()`:

```kotlin
englishUsSubtypeEnabled = this[Keys.ENGLISH_US_SUBTYPE_ENABLED]
    ?: KeyboardSettings().englishUsSubtypeEnabled,
```

Add to `Keys`:

```kotlin
val ENGLISH_US_SUBTYPE_ENABLED = booleanPreferencesKey("english_us_subtype_enabled")
```

- [ ] **Step 5: Add the settings intent and ViewModel branch**

Add to `SettingsIntent`:

```kotlin
data class ChangeEnglishUsSubtypeEnabled(
    val enabled: Boolean,
) : SettingsIntent
```

Add to `SettingsViewModel.onIntent()`:

```kotlin
is SettingsIntent.ChangeEnglishUsSubtypeEnabled -> {
    saveSetting {
        settingsRepository.updateEnglishUsSubtypeEnabled(intent.enabled)
    }
}
```

- [ ] **Step 6: Add settings screen strings**

Add to `strings.xml`:

```xml
<string name="settings_section_input_layouts">Input layouts</string>
<string name="settings_entry_layout_title">Entry</string>
<string name="settings_entry_layout_description">Always available for selected KeePass entry fields</string>
<string name="settings_english_us_layout_title">English (US)</string>
<string name="settings_english_us_layout_description">Show English (US) as a system input method layout</string>
```

- [ ] **Step 7: Render the setting row**

In `SettingsContent`, insert this block immediately after the existing `HeightModeSetting` call that sends `SettingsIntent.ChangeKeyboardHeightMode`, and before the security section divider:

```kotlin
HorizontalDivider()

SectionTitle(text = stringResource(R.string.settings_section_input_layouts))

FixedEnabledSettingRow(
    title = stringResource(R.string.settings_entry_layout_title),
    description = stringResource(R.string.settings_entry_layout_description),
)

SwitchSettingRow(
    title = stringResource(R.string.settings_english_us_layout_title),
    description = stringResource(R.string.settings_english_us_layout_description),
    checked = settings.englishUsSubtypeEnabled,
    onCheckedChange = { enabled ->
        onIntent(SettingsIntent.ChangeEnglishUsSubtypeEnabled(enabled))
    },
)
```

Add this composable near `SwitchSettingRow`:

```kotlin
@Composable
private fun FixedEnabledSettingRow(
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = true,
            enabled = false,
            onCheckedChange = null,
        )
    }
}
```

- [ ] **Step 8: Run settings tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.settings.KeyboardSettingsTest" --tests "io.github.togls.kp2acomposekeyboard.settings.SettingsRepositoryTest"
```

Expected: pass.

- [ ] **Step 9: Commit Task 2**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings app/src/main/res/values/strings.xml app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/settings
git commit -m "feat(settings): add english subtype toggle"
```

---

### Task 3: Add Dynamic Subtype Synchronization

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeSynchronizer.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/SettingsActivity.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardImeService.kt`
- Test: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeRegistryTest.kt`

- [ ] **Step 1: Add registry tests for additional subtype lists**

Append these tests to `KeyboardSubtypeRegistryTest`:

```kotlin
@Test
fun additionalSubtypes_returnsEmptyArrayWhenEnglishDisabled() {
    val subtypes = KeyboardSubtypeRegistry.additionalSubtypes(
        io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings(
            englishUsSubtypeEnabled = false,
        ),
    )

    assertEquals(0, subtypes.size)
}

@Test
fun additionalSubtypes_returnsEnglishSubtypeWhenEnglishEnabled() {
    val subtypes = KeyboardSubtypeRegistry.additionalSubtypes(
        io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings(
            englishUsSubtypeEnabled = true,
        ),
    )

    assertEquals(1, subtypes.size)
    assertEquals(KeyboardSubtype.EnglishUs, KeyboardSubtypeRegistry.fromInputMethodSubtype(subtypes.single()))
}
```

- [ ] **Step 2: Run the registry tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ime.KeyboardSubtypeRegistryTest"
```

Expected: pass after Task 2 because `additionalSubtypes()` already exists and now reads `englishUsSubtypeEnabled`.

- [ ] **Step 3: Add the synchronizer**

Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeSynchronizer.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ime

import android.content.ComponentName
import android.content.Context
import android.view.inputmethod.InputMethodManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyboardSubtypeSynchronizer @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val inputMethodManager: InputMethodManager? =
        context.getSystemService(InputMethodManager::class.java)

    @Suppress("DEPRECATION")
    fun synchronize(settings: KeyboardSettings): Boolean {
        val manager = inputMethodManager
        if (manager == null) {
            SecureLog.w(
                message = "input method manager unavailable for subtype sync",
                "englishUsEnabled" to settings.englishUsSubtypeEnabled,
            )
            return false
        }

        return runCatching {
            val additionalSubtypes = KeyboardSubtypeRegistry.additionalSubtypes(settings)
            manager.setAdditionalInputMethodSubtypes(imeId(), additionalSubtypes)
            SecureLog.d(
                message = "ime subtypes synchronized",
                "englishUsEnabled" to settings.englishUsSubtypeEnabled,
                "additionalSubtypeCount" to additionalSubtypes.size,
            )
            true
        }.getOrElse { error ->
            SecureLog.w(
                message = "ime subtype sync failed",
                throwable = error,
                "englishUsEnabled" to settings.englishUsSubtypeEnabled,
                "errorType" to error::class.java.simpleName,
            )
            false
        }
    }

    private fun imeId(): String {
        return ComponentName(context, KeyboardImeService::class.java).flattenToShortString()
    }
}
```

- [ ] **Step 4: Synchronize from settings screen startup and changes**

Modify `SettingsActivity`:

```kotlin
@Inject
lateinit var subtypeSynchronizer: KeyboardSubtypeSynchronizer
```

Add imports:

```kotlin
import javax.inject.Inject
import io.github.togls.kp2acomposekeyboard.ime.KeyboardSubtypeSynchronizer
```

Inside the `KeyboardTheme(settings = state.settings)` content block in `SettingsActivity`, after the existing snackbar effect collector and before the `SettingsScreen` call, add:

```kotlin
LaunchedEffect(state.isLoading, state.settings.englishUsSubtypeEnabled) {
    if (!state.isLoading) {
        subtypeSynchronizer.synchronize(state.settings)
    }
}
```

- [ ] **Step 5: Synchronize from IME settings collection**

Modify `KeyboardImeService`:

```kotlin
@Inject
lateinit var subtypeSynchronizer: KeyboardSubtypeSynchronizer
```

Add this method:

```kotlin
private fun collectSettingsForSubtypeSync() {
    serviceScope.launch {
        settingsRepository.settings.collect { settings ->
            subtypeSynchronizer.synchronize(settings)
        }
    }
}
```

Call it in `onCreate()` after `collectKeyboardEffects()`:

```kotlin
collectSettingsForSubtypeSync()
```

- [ ] **Step 6: Run unit tests and assemble**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ime.KeyboardSubtypeRegistryTest"
./gradlew :app:assembleDebug
```

Expected: both commands pass.

- [ ] **Step 7: Commit Task 3**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeSynchronizer.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/settings/SettingsActivity.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardImeService.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardSubtypeRegistryTest.kt
git commit -m "feat(ime): sync dynamic english subtype"
```

---

### Task 4: Add ViewModel Subtype State and Language-Switch Effects

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardUiState.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardIntent.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardEffect.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModel.kt`
- Test: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModelSubtypeTest.kt`
- Test: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModelUtilityTest.kt`

- [ ] **Step 1: Write ViewModel subtype tests**

Create `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModelSubtypeTest.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.feature.keyboard

import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettingsStore
import io.github.togls.kp2acomposekeyboard.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.session.SessionTimeoutController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class KeyboardViewModelSubtypeTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_defaultsToEntryLayout() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(KeyboardSubtype.Entry, viewModel.uiState.value.currentSubtype)
        assertEquals(MainKeyboardLayout.Entry, viewModel.uiState.value.mainLayout)
    }

    @Test
    fun changeSubtypeToEnglish_selectsDefaultLayout() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(KeyboardIntent.ChangeSubtype(KeyboardSubtype.EnglishUs))
        advanceUntilIdle()

        assertEquals(KeyboardSubtype.EnglishUs, viewModel.uiState.value.currentSubtype)
        assertEquals(MainKeyboardLayout.Default, viewModel.uiState.value.mainLayout)
    }

    @Test
    fun changeSubtypeToEntry_selectsEntryLayoutWithoutSession() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(KeyboardIntent.ChangeSubtype(KeyboardSubtype.Entry))
        advanceUntilIdle()

        assertEquals(KeyboardSubtype.Entry, viewModel.uiState.value.currentSubtype)
        assertEquals(MainKeyboardLayout.Entry, viewModel.uiState.value.mainLayout)
        assertEquals(false, viewModel.uiState.value.hasActiveSession)
    }

    @Test
    fun settingsFlow_updatesEnglishSubtypeEnabled() = runTest(dispatcher) {
        val settingsStore = FakeKeyboardSettingsStore()
        val viewModel = createViewModel(settingsStore)

        settingsStore.settingsFlow.value = KeyboardSettings(englishUsSubtypeEnabled = true)
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.englishUsSubtypeEnabled)
    }

    @Test
    fun switchLanguageFromEntryWithEnglishEnabled_emitsSwitchToEnglishSubtype() = runTest(dispatcher) {
        val settingsStore = FakeKeyboardSettingsStore(
            KeyboardSettings(englishUsSubtypeEnabled = true),
        )
        val viewModel = createViewModel(settingsStore)
        advanceUntilIdle()
        val effects = mutableListOf<KeyboardEffect>()
        val job = launch { viewModel.effect.collect { effect -> effects.add(effect) } }
        runCurrent()

        viewModel.onIntent(KeyboardIntent.ChangeSubtype(KeyboardSubtype.Entry))
        viewModel.onIntent(KeyboardIntent.SwitchLanguage)
        advanceUntilIdle()

        assertEquals(MainKeyboardLayout.Default, viewModel.uiState.value.mainLayout)
        assertEquals(listOf(KeyboardEffect.SwitchToSubtype(KeyboardSubtype.EnglishUs)), effects)
        job.cancel()
    }

    @Test
    fun switchLanguageFromEntryWithEnglishDisabled_emitsSwitchToNextInputMethod() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<KeyboardEffect>()
        val job = launch { viewModel.effect.collect { effect -> effects.add(effect) } }
        runCurrent()

        viewModel.onIntent(KeyboardIntent.ChangeSubtype(KeyboardSubtype.Entry))
        viewModel.onIntent(KeyboardIntent.SwitchLanguage)
        advanceUntilIdle()

        assertEquals(listOf(KeyboardEffect.SwitchToNextInputMethod), effects)
        job.cancel()
    }

    @Test
    fun switchLanguageFromEnglish_emitsSwitchToNextInputMethod() = runTest(dispatcher) {
        val settingsStore = FakeKeyboardSettingsStore(
            KeyboardSettings(englishUsSubtypeEnabled = true),
        )
        val viewModel = createViewModel(settingsStore)
        advanceUntilIdle()
        val effects = mutableListOf<KeyboardEffect>()
        val job = launch { viewModel.effect.collect { effect -> effects.add(effect) } }
        runCurrent()

        viewModel.onIntent(KeyboardIntent.ChangeSubtype(KeyboardSubtype.EnglishUs))
        viewModel.onIntent(KeyboardIntent.SwitchLanguage)
        advanceUntilIdle()

        assertEquals(listOf(KeyboardEffect.SwitchToNextInputMethod), effects)
        job.cancel()
    }

    private fun createViewModel(
        settingsStore: FakeKeyboardSettingsStore = FakeKeyboardSettingsStore(),
    ): KeyboardViewModel {
        val sessionRepository = KeyboardSessionRepository()
        return KeyboardViewModel(
            sessionRepository = sessionRepository,
            sessionTimeoutController = SessionTimeoutController(sessionRepository),
            settingsStore = settingsStore,
        )
    }

    private class FakeKeyboardSettingsStore(
        initialSettings: KeyboardSettings = KeyboardSettings(),
    ) : KeyboardSettingsStore {
        val settingsFlow = MutableStateFlow(initialSettings)

        override val settings = settingsFlow

        override suspend fun updateUtilitySlots(slots: KeyboardUtilitySlots) {
            settingsFlow.value = settingsFlow.value.copy(utilitySlots = slots)
        }
    }
}
```

- [ ] **Step 2: Run the failing subtype ViewModel tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardViewModelSubtypeTest"
```

Expected: fail because intents, effects, and state fields do not exist.

- [ ] **Step 3: Update `KeyboardUiState`**

Change the state defaults:

```kotlin
data class KeyboardUiState(
    val mainLayout: MainKeyboardLayout = MainKeyboardLayout.Entry,
    val currentSubtype: KeyboardSubtype = KeyboardSubtype.Entry,
    val englishUsSubtypeEnabled: Boolean = false,
    val defaultInputMode: DefaultInputMode = DefaultInputMode.Letters,
    val entryFieldDisplayMode: EntryFieldDisplayMode = EntryFieldDisplayMode.Paged,
    val currentEntryName: String? = null,
    val hasActiveSession: Boolean = false,
    val fixedFields: List<KeyboardFieldUiModel> = emptyList(),
    val extraFields: List<KeyboardFieldUiModel> = emptyList(),
    val allFields: List<KeyboardFieldUiModel> = emptyList(),
    val extraFieldPageIndex: Int = 0,
    val extraFieldPageSize: Int = DEFAULT_EXTRA_FIELD_PAGE_SIZE,
    val isUppercase: Boolean = false,
    val utilitySlots: KeyboardUtilitySlots = KeyboardUtilitySlots(),
    val isUtilityPanelExpanded: Boolean = false,
)
```

Keep the remaining existing properties unchanged.

- [ ] **Step 4: Add keyboard intents and effects**

Add to `KeyboardIntent`:

```kotlin
data class ChangeSubtype(val subtype: KeyboardSubtype) : KeyboardIntent
data object SwitchLanguage : KeyboardIntent
```

Add to `KeyboardEffect`:

```kotlin
data class SwitchToSubtype(val subtype: KeyboardSubtype) : KeyboardEffect
data object SwitchToNextInputMethod : KeyboardEffect
```

- [ ] **Step 5: Update `KeyboardViewModel.onIntent()`**

Add branches:

```kotlin
is KeyboardIntent.ChangeSubtype -> changeSubtype(intent.subtype)
KeyboardIntent.SwitchLanguage -> switchLanguage()
```

- [ ] **Step 6: Update settings observation in the ViewModel**

Replace the existing `observeSettings()` update with:

```kotlin
_uiState.update { state ->
    state.copy(
        utilitySlots = settings.utilitySlots,
        englishUsSubtypeEnabled = settings.englishUsSubtypeEnabled,
    )
}
```

- [ ] **Step 7: Add subtype behavior methods**

Add to `KeyboardViewModel`:

```kotlin
private fun changeSubtype(subtype: KeyboardSubtype) {
    _uiState.update { state ->
        state.copy(
            currentSubtype = subtype,
            mainLayout = subtype.mainLayout,
            entryFieldDisplayMode = EntryFieldDisplayMode.Paged,
            extraFieldPageIndex = 0,
        )
    }
}

private fun switchLanguage() {
    val state = _uiState.value

    if (state.mainLayout == MainKeyboardLayout.Entry && state.englishUsSubtypeEnabled) {
        _uiState.update { currentState ->
            currentState.copy(
                currentSubtype = KeyboardSubtype.EnglishUs,
                mainLayout = MainKeyboardLayout.Default,
                entryFieldDisplayMode = EntryFieldDisplayMode.Paged,
                extraFieldPageIndex = 0,
            )
        }
        sendEffect(KeyboardEffect.SwitchToSubtype(KeyboardSubtype.EnglishUs))
        return
    }

    sendEffect(KeyboardEffect.SwitchToNextInputMethod)
}
```

- [ ] **Step 8: Preserve active subtype when clearing a session**

Replace `withoutSession()` with:

```kotlin
private fun KeyboardUiState.withoutSession(): KeyboardUiState {
    return copy(
        mainLayout = currentSubtype.mainLayout,
        entryFieldDisplayMode = EntryFieldDisplayMode.Paged,
        currentEntryName = null,
        hasActiveSession = false,
        fixedFields = emptyList(),
        extraFields = emptyList(),
        allFields = emptyList(),
        extraFieldPageIndex = 0,
    )
}
```

Keep `withSessionSnapshot()` switching to `MainKeyboardLayout.Entry`, but do not change `currentSubtype` there.

- [ ] **Step 9: Update existing utility test fake store**

In `KeyboardViewModelUtilityTest.FakeKeyboardSettingsStore`, keep the same class. The existing `settingsFlow.value.copy(utilitySlots = slots)` call remains valid after adding `englishUsSubtypeEnabled`, because Kotlin data-class `copy` preserves properties that are not supplied.

- [ ] **Step 10: Run ViewModel tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardViewModelSubtypeTest" --tests "io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardViewModelUtilityTest"
```

Expected: pass.

- [ ] **Step 11: Commit Task 4**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard
git commit -m "feat(keyboard): track subtype language switching"
```

---

### Task 5: Wire IME Startup Subtype Detection and Platform Switching

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardImeService.kt`
- Test: build validation through `:app:assembleDebug`

- [ ] **Step 1: Add imports to `KeyboardImeService`**

Add:

```kotlin
import android.content.ComponentName
import android.os.Build
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardSubtype
```

- [ ] **Step 2: Add the platform manager and IME ID helpers**

Add inside `KeyboardImeService`:

```kotlin
private val inputMethodManager: InputMethodManager? by lazy {
    getSystemService(InputMethodManager::class.java)
}

private val imeId: String
    get() = ComponentName(this, KeyboardImeService::class.java).flattenToShortString()

private val imeToken: IBinder?
    get() = window?.window?.attributes?.token
```

- [ ] **Step 3: Synchronize current subtype before the first Compose render**

Call `syncCurrentSubtypeFromSystem()` in `onCreateInputView()` immediately after `configureImeNavigationBar(isDarkTheme = false)` and before returning the `ComposeView`:

```kotlin
syncCurrentSubtypeFromSystem()
```

Call it in `onStartInputView()` immediately after `installViewTreeOwners(window?.window?.decorView)`:

```kotlin
syncCurrentSubtypeFromSystem()
```

Add the override:

```kotlin
override fun onCurrentInputMethodSubtypeChanged(newSubtype: InputMethodSubtype) {
    super.onCurrentInputMethodSubtypeChanged(newSubtype)
    applyCurrentSubtype(newSubtype)
}
```

Add the helper methods:

```kotlin
private fun syncCurrentSubtypeFromSystem() {
    val subtype = inputMethodManager?.currentInputMethodSubtype
    applyCurrentSubtype(subtype)
}

private fun applyCurrentSubtype(subtype: InputMethodSubtype?) {
    val keyboardSubtype = KeyboardSubtypeRegistry.fromInputMethodSubtype(subtype)
    viewModel.onIntent(KeyboardIntent.ChangeSubtype(keyboardSubtype))
    SecureLog.d(
        message = "ime subtype applied",
        "subtype" to keyboardSubtype.name,
    )
}
```

- [ ] **Step 4: Handle new keyboard effects**

Add branches to `handleKeyboardEffect()`:

```kotlin
is KeyboardEffect.SwitchToSubtype -> {
    switchToSubtype(effect.subtype)
}

KeyboardEffect.SwitchToNextInputMethod -> {
    switchToNextKeyboard()
}
```

- [ ] **Step 5: Add platform switch methods**

Add:

```kotlin
private fun switchToSubtype(subtype: KeyboardSubtype) {
    val inputMethodSubtype = KeyboardSubtypeRegistry.inputMethodSubtypeFor(subtype)
    if (inputMethodSubtype == null) {
        SecureLog.w(
            message = "subtype switch ignored",
            "subtype" to subtype.name,
        )
        return
    }

    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            switchInputMethod(imeId, inputMethodSubtype)
        } else {
            val token = imeToken ?: error("IME token unavailable")
            @Suppress("DEPRECATION")
            inputMethodManager?.setInputMethodAndSubtype(token, imeId, inputMethodSubtype)
                ?: error("InputMethodManager unavailable")
        }
    }.onFailure { error ->
        SecureLog.w(
            message = "subtype switch failed",
            throwable = error,
            "subtype" to subtype.name,
            "errorType" to error::class.java.simpleName,
        )
        switchToNextKeyboard()
    }
}

private fun switchToNextKeyboard() {
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            switchToNextInputMethod(false)
        } else {
            val token = imeToken ?: error("IME token unavailable")
            @Suppress("DEPRECATION")
            inputMethodManager?.switchToNextInputMethod(token, false)
                ?: error("InputMethodManager unavailable")
        }
    }.onFailure { error ->
        SecureLog.w(
            message = "next input method switch failed",
            throwable = error,
            "errorType" to error::class.java.simpleName,
        )
    }
}
```

- [ ] **Step 6: Run build validation**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: pass.

- [ ] **Step 7: Commit Task 5**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ime/KeyboardImeService.kt
git commit -m "feat(ime): apply active subtype on startup"
```

---

### Task 6: Add Language Switch UI and Entry Empty-State Coverage

**Files:**
- Create: `app/src/main/res/drawable/ic_language_24.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardTestTags.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/ActionKeys.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/EntryActionRows.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/DefaultKeyboardLayout.kt`
- Modify: `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootTestFixtures.kt`
- Modify: `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootDefaultLayoutTest.kt`
- Modify: `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootEntryLayoutTest.kt`

- [ ] **Step 1: Add UI tests**

In `KeyboardRootTestFixtures.kt`, change `testDefaultState()` to:

```kotlin
internal fun testDefaultState() = KeyboardUiState(
    mainLayout = MainKeyboardLayout.Default,
    currentSubtype = KeyboardSubtype.EnglishUs,
    englishUsSubtypeEnabled = true,
)
```

Add imports:

```kotlin
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardSubtype
```

Add:

```kotlin
internal fun testEntryEmptyState() = KeyboardUiState(
    mainLayout = MainKeyboardLayout.Entry,
    currentSubtype = KeyboardSubtype.Entry,
    englishUsSubtypeEnabled = true,
    hasActiveSession = false,
)
```

In `KeyboardRootDefaultLayoutTest`, add:

```kotlin
@Test
fun letterLayoutShowsLanguageSwitchNextToNumberSwitch() {
    composeRule.setContent {
        KeyboardRootTestContent(state = testDefaultState())
    }

    val switchBounds = composeRule.onNodeWithTag(KeyboardTestTags.DefaultSwitchKey)
        .fetchSemanticsNode()
        .boundsInRoot
    val languageBounds = composeRule.onNodeWithTag(KeyboardTestTags.LanguageSwitchKey)
        .fetchSemanticsNode()
        .boundsInRoot

    assertTrue(
        "switchBounds=$switchBounds languageBounds=$languageBounds",
        languageBounds.left >= switchBounds.right,
    )
}
```

In `KeyboardRootEntryLayoutTest`, add:

```kotlin
@Test
fun emptyEntryLayoutShowsLanguageSwitchAndNoFieldButtons() {
    composeRule.setContent {
        KeyboardRootTestContent(testEntryEmptyState())
    }

    composeRule.onNodeWithTag(KeyboardTestTags.EntryNormalContent).assertIsDisplayed()
    composeRule.onNodeWithTag(KeyboardTestTags.LanguageSwitchKey).assertIsDisplayed()
    composeRule.onNodeWithText("Username").assertDoesNotExist()
    composeRule.onNodeWithText("Password").assertDoesNotExist()
    composeRule.onNodeWithText("TOTP").assertDoesNotExist()
}
```

Add import:

```kotlin
import androidx.compose.ui.test.assertDoesNotExist
```

- [ ] **Step 2: Run the failing UI tests**

Run:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected: fail because `KeyboardTestTags.LanguageSwitchKey` and the language key do not exist. If no device is connected, record this and run `./gradlew :app:assembleDebug` after implementation.

- [ ] **Step 3: Add strings and icon**

Add to `strings.xml`:

```xml
<string name="cd_key_switch_language">Switch language</string>
```

Create `app/src/main/res/drawable/ic_language_24.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#00000000"
        android:pathData="M12,3C7.03,3 3,7.03 3,12s4.03,9 9,9 9,-4.03 9,-9 -4.03,-9 -9,-9Z"
        android:strokeColor="?attr/colorControlNormal"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:strokeWidth="2" />
    <path
        android:fillColor="#00000000"
        android:pathData="M3.6,9h16.8M3.6,15h16.8M12,3c2,2.3 3,5.3 3,9s-1,6.7 -3,9M12,3c-2,2.3 -3,5.3 -3,9s1,6.7 3,9"
        android:strokeColor="?attr/colorControlNormal"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:strokeWidth="2" />
</vector>
```

- [ ] **Step 4: Add test tag**

Add to `KeyboardTestTags`:

```kotlin
const val LanguageSwitchKey = "language-switch-key"
```

- [ ] **Step 5: Add language switch key component**

In `ActionKeys.kt`, add:

```kotlin
@Composable
internal fun LanguageSwitchKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier.testTag(KeyboardTestTags.LanguageSwitchKey),
        iconRes = R.drawable.ic_language_24,
        contentDescription = stringResource(R.string.cd_key_switch_language),
        onClick = { onIntent(KeyboardIntent.SwitchLanguage) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}
```

- [ ] **Step 6: Replace Entry row switch key**

In `EntryActionRows.kt`, replace the import and usage of `SwitchToDefaultLayoutKey` with `LanguageSwitchKey`:

```kotlin
LanguageSwitchKey(
    onIntent = onIntent,
    modifier = Modifier.width(metrics.standardKeyWidth),
)
```

- [ ] **Step 7: Add language key next to `?123` in letter layout**

In `DefaultKeyboardLayout.kt`, import `LanguageSwitchKey` and insert after the `KeyboardKey` switch:

```kotlin
if (state.defaultInputMode == DefaultInputMode.Letters) {
    LanguageSwitchKey(
        onIntent = onIntent,
        modifier = Modifier.width(metrics.sideKeyWidth),
    )
}
```

- [ ] **Step 8: Run build and UI tests**

Run:

```bash
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

Expected: `assembleDebug` passes. `connectedDebugAndroidTest` passes when a device or emulator is connected; if there is no device, the command fails with no connected device and the final validation must report that limitation.

- [ ] **Step 9: Commit Task 6**

```bash
git add app/src/main/res/drawable/ic_language_24.xml app/src/main/res/values/strings.xml app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard
git commit -m "ui(keyboard): add subtype language switch key"
```

---

### Task 7: Final Regression, Documentation, and Manual Validation Notes

**Files:**
- Modify: `README.md`
- Modify: `docs/known-limitations.md`

- [ ] **Step 1: Update README implemented features**

In `README.md`, update the implemented list by replacing the current default-layout bullet with:

```markdown
- Entry system input layout with an optional English (US) system subtype.
- English letters, numbers, and symbols layout when English (US) is enabled.
```

Add a short note under `## Keyboard Layouts`:

```markdown
Android system input method settings always expose the Entry subtype. English (US) is registered as an additional subtype only when enabled from the app settings.
```

- [ ] **Step 2: Update known limitations with ROM behavior**

In `docs/known-limitations.md`, add this bullet under the existing ROM/input-method limitations section:

```markdown
- Android or vendor ROM settings may cache dynamically registered English (US) subtypes until the settings screen or IME is reopened.
```

Ensure the existing disclaimer remains present:

```text
This project is primarily built for personal devices and personal needs. It does not guarantee compatibility with all Android, HyperOS, MIUI, or vendor ROM versions.
```

- [ ] **Step 3: Run unit tests**

Run:

```bash
./gradlew :app:testDebugUnitTest
```

Expected: pass.

- [ ] **Step 4: Run debug build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: pass.

- [ ] **Step 5: Run Compose UI tests when a device is available**

Run:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected with connected device/emulator: pass.

Expected without device/emulator: fail with a no-device message; record that UI tests were not executed because no Android target was connected.

- [ ] **Step 6: Manual system settings checks**

Install the debug build:

```bash
./gradlew :app:installDebug
```

Check these on device:

```text
1. Enable KP2A Compose Keyboard in Android system input method settings.
2. Open KP2A Compose Keyboard app settings.
3. Confirm Entry is shown as fixed enabled.
4. Confirm English (US) is off by default.
5. Open Android system input method management and confirm KP2A shows only Entry.
6. Enable English (US) in KP2A app settings.
7. Reopen Android system input method management and confirm KP2A shows Entry and English (US).
8. From Gboard, open the system input method picker and select KP2A Entry; confirm the Entry empty layout appears.
9. From Gboard, open the system input method picker and select KP2A English (US); confirm the English letter layout appears.
10. From KP2A Entry with English enabled, tap the language key; confirm it switches to English (US).
11. From KP2A English (US), tap the language key; confirm it switches to the next system IME.
12. Disable English (US) in KP2A app settings.
13. From KP2A Entry, tap the language key; confirm it switches to the next system IME.
```

- [ ] **Step 7: Commit Task 7**

```bash
git add README.md docs/known-limitations.md
git commit -m "docs(ime): document subtype settings behavior"
```

---

## Final Verification Before Completion

- [ ] Run:

```bash
./gradlew :app:testDebugUnitTest
```

Expected: pass.

- [ ] Run:

```bash
./gradlew :app:assembleDebug
```

Expected: pass.

- [ ] Run, when an Android device or emulator is connected:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected with connected target: pass.

- [ ] Confirm `git status --short` contains no uncommitted implementation files.

- [ ] Summarize:
  - commits created,
  - tests/builds run,
  - connected-device test status,
  - manual system settings checks completed or deferred.

## Self-Review Notes

- Spec coverage:
  - Static `Entry` subtype: Task 1.
  - Dynamic `English (US)` registration: Tasks 1, 2, 3.
  - Settings option: Task 2.
  - Direct system picker activation: Task 5.
  - Entry empty layout: Tasks 4 and 6.
  - Language switch order: Tasks 4, 5, 6.
  - Security/logging boundaries: Tasks 3 and 5.
  - Manual ROM/system settings validation: Task 7.
- Red-flag scan: this plan contains no incomplete implementation steps.
- Type consistency:
  - Pure subtype enum is `KeyboardSubtype`.
  - Android conversion object is `KeyboardSubtypeRegistry`.
  - App setting is `englishUsSubtypeEnabled`.
  - ViewModel intent is `KeyboardIntent.ChangeSubtype`.
  - Language intent is `KeyboardIntent.SwitchLanguage`.
  - Platform effects are `KeyboardEffect.SwitchToSubtype` and `KeyboardEffect.SwitchToNextInputMethod`.
