# Keyboard UI Structure Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reorganize the keyboard UI around clear `frame`, `metrics`, `shared`, `textinput`, `entry`, and `quickactions` responsibilities while preserving current behavior.

**Architecture:** Keep the existing direct Compose rendering model. Rename misleading domain, feature, settings, and UI concepts first, then move UI files into responsibility-based packages, then split entry-only content where the current file mixes rendering and scroll coordination. The only approved behavior exception is that old DataStore quick-action preference keys do not need migration.

**Tech Stack:** Kotlin, Android InputMethodService, Jetpack Compose, Material 3, Hilt, DataStore Preferences, JUnit, Robolectric, Compose UI tests, Gradle.

---

## Behavior Guardrails

- Behavior is a hard constraint: do not intentionally change key order, text, icons, content descriptions, enabled states, emitted intents, layout formulas, scroll behavior, snap behavior, or quick-action drag/drop behavior.
- If a compile or test failure reveals a true behavior change is needed, stop and ask the user before implementing that change.
- Do not put Keepass2Android field values into UI state, Compose parameters, semantics, test tags, logs, DataStore, docs, screenshots, or tests.
- Keep `KeyboardIntent.CommitField(fieldId)` id-based.
- Do not add `ClearEntryQuickActionId` to `KeyboardQuickActionId.productionItems` unless the user approves; the current behavior only persists the settings quick action.
- Ignore existing untracked `app/debug/` build artifacts unless the user explicitly asks to clean them.

## Target File Structure

### Create or Rename

- Rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/DefaultInputMode.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/TextInputMode.kt`
- Rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilityItemId.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardQuickActionId.kt`
- Rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilitySlots.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardQuickActionSlots.kt`
- Rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilitySlotsReducer.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardQuickActionSlotsReducer.kt`
- Rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/settings/KeyboardUtilitySlotsPreferenceCodec.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/settings/KeyboardQuickActionSlotsPreferenceCodec.kt`
- Rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRoot.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardImeContent.kt`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/frame/`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/metrics/`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/shared/`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/textinput/`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/`

### Move to `frame`

- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardFrame.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardContentArea.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardBottomSpacer.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardNavigationBarSpacer.kt`

### Move to `metrics`

- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutLocals.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetrics.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/style/KeyboardAdaptiveMetrics.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/style/KeyboardHeight.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/style/KeyboardMetrics.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/style/KeyboardOrientation.kt`

### Move to `shared`

- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardTestTags.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardRow.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/ActionKeys.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/FieldKey.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/KeyboardKey.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/LetterKey.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/TextKeys.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/style/KeyboardKeyColors.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/style/KeyboardKeyEmphasis.kt`

### Move to `textinput`

- Move and rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/DefaultKeyboardLayout.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/textinput/TextInputKeyboardLayout.kt`
- Move and rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/LetterKeyboard.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/textinput/LetterTextInputLayout.kt`
- Move and rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/NumberKeyboard.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/textinput/NumberTextInputLayout.kt`
- Move and rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/SymbolKeyboard.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/textinput/SymbolTextInputLayout.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/TextKeyRows.kt`

### Move to `entry`

- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldGrid.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPaging.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/EntryActionRows.kt`
- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/ExpandedEntryActionRows.kt`
- Create during extraction: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/NormalEntryContent.kt`
- Create during extraction: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/ExpandedEntryContent.kt`

### Move to `quickactions`

- Move: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/ExistingEntryHint.kt`
- Move and rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/KeyboardUtilityItem.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/KeyboardQuickAction.kt`
- Move and rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilityDragPreview.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionDragPreview.kt`
- Move and rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilityDragState.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionDragState.kt`
- Move and rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilityPanel.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionPanel.kt`
- Move and rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilityRow.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionBar.kt`
- Move and rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilityRowPolicy.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionBarPolicy.kt`
- Move and rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilitySlot.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionSlot.kt`
- Move and rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilitySlotModels.kt` -> `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionSlotModels.kt`

---

### Task 1: Establish Baseline

**Files:**
- Read: `docs/superpowers/specs/2026-05-24-keyboard-ui-structure-refactor-design.md`
- Read: `AGENTS.md`

- [ ] **Step 1: Verify working tree state**

Run:

```powershell
git status --short
```

Expected: only known unrelated untracked build artifacts such as `?? app/debug/`, plus no implementation files modified for this refactor.

- [ ] **Step 2: Run focused JVM tests before refactor**

Run:

```powershell
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlotsReducerTest" --tests "io.github.togls.kp2acomposekeyboard.settings.KeyboardUtilitySlotsPreferenceCodecTest" --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.KeyboardLayoutMetricsTest" --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.EntryFieldPagingTest" --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.UtilityRowPolicyTest" --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.UtilitySlotModelsTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit only if baseline files changed**

Do not commit if Step 1 and Step 2 changed no files. If Gradle creates ignored outputs, leave them ignored. If tracked files changed unexpectedly, stop and inspect before continuing.

---

### Task 2: Rename Text Input Domain and Feature Concepts

**Files:**
- Rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/DefaultInputMode.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/MainKeyboardLayout.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardSubtype.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardUiState.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModel.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/**/*.kt`
- Modify: `app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/*.kt`
- Modify: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModelSubtypeTest.kt`
- Modify: `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/*.kt`

- [ ] **Step 1: Rename the input mode file**

Run:

```powershell
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/DefaultInputMode.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/TextInputMode.kt
```

- [ ] **Step 2: Update the text input enum**

Set `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/TextInputMode.kt` to:

```kotlin
package io.github.togls.kp2acomposekeyboard.domain.keyboard

enum class TextInputMode {
    Letters,
    Numbers,
    Symbols,
}
```

- [ ] **Step 3: Update the main layout enum**

Set `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/MainKeyboardLayout.kt` to:

```kotlin
package io.github.togls.kp2acomposekeyboard.domain.keyboard

enum class MainKeyboardLayout {
    TextInput,
    Entry,
}
```

- [ ] **Step 4: Update subtype mapping**

Set `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardSubtype.kt` to:

```kotlin
package io.github.togls.kp2acomposekeyboard.domain.keyboard

enum class KeyboardSubtype(
    val mainLayout: MainKeyboardLayout,
) {
    Entry(MainKeyboardLayout.Entry),
    EnglishUs(MainKeyboardLayout.TextInput),
}
```

- [ ] **Step 5: Apply mechanical name replacements**

Run:

```powershell
$paths = @('app/src/main','app/src/test','app/src/androidTest','app/src/debug')
Get-ChildItem -Path $paths -Recurse -Include *.kt | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw
    $text = $text -replace '\bDefaultInputMode\b', 'TextInputMode'
    $text = $text -replace '\bdefaultInputMode\b', 'textInputMode'
    $text = $text -replace '\bupdateDefaultInputMode\b', 'updateTextInputMode'
    $text = $text -replace '\bMainKeyboardLayout\.Default\b', 'MainKeyboardLayout.TextInput'
    Set-Content -LiteralPath $_.FullName -Value $text
}
```

- [ ] **Step 6: Verify no old text-input names remain**

Run:

```powershell
rg -n "DefaultInputMode|defaultInputMode|updateDefaultInputMode|MainKeyboardLayout\.Default" app/src/main app/src/test app/src/androidTest app/src/debug
```

Expected: no matches.

- [ ] **Step 7: Run subtype and keyboard unit tests**

Run:

```powershell
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardViewModelSubtypeTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit**

Run:

```powershell
git add app/src/main app/src/test app/src/androidTest app/src/debug
git commit -m "refactor(keyboard): rename default mode to text input"
```

---

### Task 3: Rename Quick Action Domain, Settings, and Feature Concepts

**Files:**
- Rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilityItemId.kt`
- Rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilitySlots.kt`
- Rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilitySlotsReducer.kt`
- Rename: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/settings/KeyboardUtilitySlotsPreferenceCodec.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/application/settings/KeyboardSettingsStore.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/settings/KeyboardSettings.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/settings/SettingsRepository.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardIntent.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardUiState.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModel.kt`
- Rename tests: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilitySlotsReducerTest.kt`
- Rename tests: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/settings/KeyboardUtilitySlotsPreferenceCodecTest.kt`
- Rename tests: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModelUtilityTest.kt`

- [ ] **Step 1: Rename files with git**

Run:

```powershell
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilityItemId.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardQuickActionId.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilitySlots.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardQuickActionSlots.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilitySlotsReducer.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardQuickActionSlotsReducer.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/settings/KeyboardUtilitySlotsPreferenceCodec.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/settings/KeyboardQuickActionSlotsPreferenceCodec.kt
git mv app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardUtilitySlotsReducerTest.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardQuickActionSlotsReducerTest.kt
git mv app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/settings/KeyboardUtilitySlotsPreferenceCodecTest.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/settings/KeyboardQuickActionSlotsPreferenceCodecTest.kt
git mv app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModelUtilityTest.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/feature/keyboard/KeyboardViewModelQuickActionTest.kt
```

- [ ] **Step 2: Update quick-action ids**

Set `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardQuickActionId.kt` to:

```kotlin
package io.github.togls.kp2acomposekeyboard.domain.keyboard

interface KeyboardQuickActionId {
    val storageValue: String

    companion object {
        val productionItems: List<KeyboardQuickActionId> = listOf(SettingsQuickActionId)

        fun fromStorageValue(value: String): KeyboardQuickActionId? {
            return productionItems.firstOrNull { itemId ->
                itemId.storageValue == value
            }
        }
    }
}

data object SettingsQuickActionId : KeyboardQuickActionId {
    override val storageValue = "settings"
}

data object ClearEntryQuickActionId : KeyboardQuickActionId {
    override val storageValue = "clear_entry"
}
```

- [ ] **Step 3: Update quick-action slots**

Set `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardQuickActionSlots.kt` to:

```kotlin
package io.github.togls.kp2acomposekeyboard.domain.keyboard

data class KeyboardQuickActionSlots(
    val centerItemIds: List<KeyboardQuickActionId> = listOf(SettingsQuickActionId),
    val rightItemId: KeyboardQuickActionId? = null,
) {
    val pinnedCount: Int
        get() = centerItemIds.size + if (rightItemId == null) 0 else 1

    companion object {
        const val MAX_PINNED_ITEMS = 5
    }
}
```

- [ ] **Step 4: Update reducer type names**

In `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/domain/keyboard/KeyboardQuickActionSlotsReducer.kt`, apply these exact type renames without changing algorithm branches:

```text
KeyboardUtilitySlotsReducer -> KeyboardQuickActionSlotsReducer
KeyboardUtilitySlots -> KeyboardQuickActionSlots
KeyboardUtilityItemId -> KeyboardQuickActionId
SettingsUtilityItemId -> SettingsQuickActionId
ClearEntryUtilityItemId -> ClearEntryQuickActionId
```

The first lines of the file should become:

```kotlin
package io.github.togls.kp2acomposekeyboard.domain.keyboard

class KeyboardQuickActionSlotsReducer(
    private val allowedItemIds: List<KeyboardQuickActionId> = KeyboardQuickActionId.productionItems,
) {
```

- [ ] **Step 5: Apply project-wide quick-action replacements**

Run:

```powershell
$paths = @('app/src/main','app/src/test','app/src/androidTest','app/src/debug')
Get-ChildItem -Path $paths -Recurse -Include *.kt | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw
    $text = $text -replace '\bKeyboardUtilityItemId\b', 'KeyboardQuickActionId'
    $text = $text -replace '\bSettingsUtilityItemId\b', 'SettingsQuickActionId'
    $text = $text -replace '\bClearEntryUtilityItemId\b', 'ClearEntryQuickActionId'
    $text = $text -replace '\bKeyboardUtilitySlotsReducer\b', 'KeyboardQuickActionSlotsReducer'
    $text = $text -replace '\bKeyboardUtilitySlotsPreferenceCodec\b', 'KeyboardQuickActionSlotsPreferenceCodec'
    $text = $text -replace '\bKeyboardUtilitySlots\b', 'KeyboardQuickActionSlots'
    $text = $text -replace '\butilitySlots\b', 'quickActionSlots'
    $text = $text -replace '\bupdateUtilitySlots\b', 'updateQuickActionSlots'
    $text = $text -replace '\butilitySlotsReducer\b', 'quickActionSlotsReducer'
    $text = $text -replace '\bisUtilityPanelExpanded\b', 'isQuickActionPanelExpanded'
    $text = $text -replace '\bToggleUtilityPanel\b', 'ToggleQuickActionPanel'
    $text = $text -replace '\bCloseUtilityPanel\b', 'CloseQuickActionPanel'
    $text = $text -replace '\bClickUtilityItem\b', 'ClickQuickAction'
    $text = $text -replace '\bMoveUtilityItemToCenter\b', 'MoveQuickActionToCenter'
    $text = $text -replace '\bMoveUtilityItemToRight\b', 'MoveQuickActionToRight'
    $text = $text -replace '\bRemoveUtilityItem\b', 'RemoveQuickAction'
    $text = $text -replace '\bclickUtilityItem\b', 'clickQuickAction'
    $text = $text -replace '\btoggleUtilityPanel\b', 'toggleQuickActionPanel'
    $text = $text -replace '\bcloseUtilityPanel\b', 'closeQuickActionPanel'
    Set-Content -LiteralPath $_.FullName -Value $text
}
```

- [ ] **Step 6: Rename DataStore key constant**

In `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/settings/SettingsRepository.kt`, replace the quick-action preference key section with:

```kotlin
            quickActionSlots = KeyboardQuickActionSlotsPreferenceCodec.decode(
                this[Keys.QUICK_ACTION_SLOTS],
            ),
```

and:

```kotlin
        val QUICK_ACTION_SLOTS = stringPreferencesKey("quick_action_slots")
```

In `updateQuickActionSlots`, write:

```kotlin
    override suspend fun updateQuickActionSlots(slots: KeyboardQuickActionSlots) {
        context.keyboardSettingsDataStore.edit { preferences ->
            preferences[Keys.QUICK_ACTION_SLOTS] = KeyboardQuickActionSlotsPreferenceCodec.encode(slots)
        }
    }
```

- [ ] **Step 7: Update the quick-action codec object declaration**

The first lines of `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/data/settings/KeyboardQuickActionSlotsPreferenceCodec.kt` should be:

```kotlin
package io.github.togls.kp2acomposekeyboard.data.settings

import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlots
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlotsReducer

object KeyboardQuickActionSlotsPreferenceCodec {

    private val reducer = KeyboardQuickActionSlotsReducer()
```

Update the comment in `encode` to:

```kotlin
        // Keep the value compact and non-sensitive: only quick-action IDs are stored.
```

- [ ] **Step 8: Verify no utility domain or feature names remain**

Run:

```powershell
rg -n "KeyboardUtility|SettingsUtility|ClearEntryUtility|UtilitySlots|utilitySlots|updateUtilitySlots|isUtilityPanelExpanded|ToggleUtility|CloseUtility|ClickUtility|MoveUtility|RemoveUtility" app/src/main app/src/test app/src/androidTest app/src/debug
```

Expected: matches only inside UI file names or comments that have not moved yet in `ui/keyboard/utility`; no domain, data, feature, settings, preview, or test class names should remain with `Utility`.

- [ ] **Step 9: Run quick-action and settings tests**

Run:

```powershell
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlotsReducerTest" --tests "io.github.togls.kp2acomposekeyboard.settings.KeyboardQuickActionSlotsPreferenceCodecTest" --tests "io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardViewModelQuickActionTest" --tests "io.github.togls.kp2acomposekeyboard.settings.KeyboardSettingsTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 10: Commit**

Run:

```powershell
git add app/src/main app/src/test app/src/androidTest app/src/debug
git commit -m "refactor(keyboard): rename utility slots to quick actions"
```

---

### Task 4: Move Frame, Metrics, Shared, Text Input, Entry, and Quick Action UI Packages

**Files:**
- Move all files listed in the Target File Structure section.
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/platform/ime/KeyboardImeService.kt`
- Modify: `app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/*.kt`
- Modify: `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/*.kt`
- Modify: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/**/*.kt`

- [ ] **Step 1: Create package directories**

Run:

```powershell
New-Item -ItemType Directory -Force -Path `
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/frame, `
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/metrics, `
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/shared, `
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/textinput, `
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry, `
  app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions
```

- [ ] **Step 2: Move root, frame, and metrics files**

Run:

```powershell
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRoot.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardImeContent.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardBottomSpacer.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/frame/KeyboardBottomSpacer.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardNavigationBarSpacer.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/frame/KeyboardNavigationBarSpacer.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardFrame.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/frame/KeyboardFrame.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardContentArea.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/frame/KeyboardContentArea.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutLocals.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/metrics/KeyboardLayoutLocals.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetrics.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/metrics/KeyboardLayoutMetrics.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/style/KeyboardAdaptiveMetrics.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/metrics/KeyboardAdaptiveMetrics.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/style/KeyboardHeight.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/metrics/KeyboardHeight.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/style/KeyboardMetrics.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/metrics/KeyboardMetrics.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/style/KeyboardOrientation.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/metrics/KeyboardOrientation.kt
```

- [ ] **Step 3: Move shared files**

Run:

```powershell
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardTestTags.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/shared/KeyboardTestTags.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardRow.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/shared/KeyboardRow.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/ActionKeys.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/shared/ActionKeys.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/FieldKey.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/shared/FieldKey.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/KeyboardKey.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/shared/KeyboardKey.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/LetterKey.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/shared/LetterKey.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/TextKeys.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/shared/TextKeys.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/style/KeyboardKeyColors.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/shared/KeyboardKeyColors.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/style/KeyboardKeyEmphasis.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/shared/KeyboardKeyEmphasis.kt
```

- [ ] **Step 4: Move text input, entry, and quick-action files**

Run:

```powershell
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/DefaultKeyboardLayout.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/textinput/TextInputKeyboardLayout.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/LetterKeyboard.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/textinput/LetterTextInputLayout.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/NumberKeyboard.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/textinput/NumberTextInputLayout.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/SymbolKeyboard.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/textinput/SymbolTextInputLayout.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/TextKeyRows.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/textinput/TextKeyRows.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/EntryKeyboardLayout.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldGrid.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/EntryFieldGrid.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPaging.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/EntryFieldPaging.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/EntryActionRows.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/EntryActionRows.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/ExpandedEntryActionRows.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/ExpandedEntryActionRows.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/ExistingEntryHint.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/ExistingEntryHint.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/KeyboardUtilityItem.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/KeyboardQuickAction.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilityDragPreview.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionDragPreview.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilityDragState.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionDragState.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilityPanel.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionPanel.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilityRow.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionBar.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilityRowPolicy.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionBarPolicy.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilitySlot.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionSlot.kt
git mv app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility/UtilitySlotModels.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionSlotModels.kt
```

- [ ] **Step 5: Update package declarations mechanically**

Run:

```powershell
$replacements = @{
  'package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout' = 'package io.github.togls.kp2acomposekeyboard.ui.keyboard.frame'
  'package io.github.togls.kp2acomposekeyboard.ui.keyboard.style' = 'package io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics'
  'package io.github.togls.kp2acomposekeyboard.ui.keyboard.key' = 'package io.github.togls.kp2acomposekeyboard.ui.keyboard.shared'
  'package io.github.togls.kp2acomposekeyboard.ui.keyboard.row' = 'package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry'
  'package io.github.togls.kp2acomposekeyboard.ui.keyboard.utility' = 'package io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions'
}
Get-ChildItem -Path app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard -Recurse -Include *.kt | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw
    foreach ($key in $replacements.Keys) {
        $text = $text.Replace($key, $replacements[$key])
    }
    Set-Content -LiteralPath $_.FullName -Value $text
}
```

Then manually correct package declarations for moved text input, entry, and metrics files:

```text
TextInputKeyboardLayout.kt, LetterTextInputLayout.kt, NumberTextInputLayout.kt, SymbolTextInputLayout.kt, TextKeyRows.kt -> package io.github.togls.kp2acomposekeyboard.ui.keyboard.textinput
EntryKeyboardLayout.kt, EntryFieldGrid.kt, EntryFieldPaging.kt, EntryActionRows.kt, ExpandedEntryActionRows.kt -> package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry
KeyboardLayoutLocals.kt, KeyboardLayoutMetrics.kt -> package io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics
KeyboardBottomSpacer.kt, KeyboardNavigationBarSpacer.kt -> package io.github.togls.kp2acomposekeyboard.ui.keyboard.frame
KeyboardImeContent.kt -> package io.github.togls.kp2acomposekeyboard.ui.keyboard
```

- [ ] **Step 6: Update import package names**

Run:

```powershell
$paths = @('app/src/main','app/src/test','app/src/androidTest','app/src/debug')
Get-ChildItem -Path $paths -Recurse -Include *.kt | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw
    $text = $text -replace 'io\.github\.togls\.kp2acomposekeyboard\.ui\.keyboard\.style\.(KeyboardKeyColors|KeyboardKeyEmphasis)', 'io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.$1'
    $text = $text -replace 'io\.github\.togls\.kp2acomposekeyboard\.ui\.keyboard\.style\.', 'io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.'
    $text = $text -replace 'io\.github\.togls\.kp2acomposekeyboard\.ui\.keyboard\.key\.', 'io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.'
    $text = $text -replace 'io\.github\.togls\.kp2acomposekeyboard\.ui\.keyboard\.row\.', 'io.github.togls.kp2acomposekeyboard.ui.keyboard.entry.'
    $text = $text -replace 'io\.github\.togls\.kp2acomposekeyboard\.ui\.keyboard\.utility\.', 'io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions.'
    $text = $text -replace 'io\.github\.togls\.kp2acomposekeyboard\.ui\.keyboard\.layout\.(KeyboardFrame|KeyboardContentArea)', 'io.github.togls.kp2acomposekeyboard.ui.keyboard.frame.$1'
    $text = $text -replace 'io\.github\.togls\.kp2acomposekeyboard\.ui\.keyboard\.layout\.(KeyboardLayoutInput|KeyboardLayoutMetrics|LocalKeyboardLayoutMetrics|calculateKeyboardLayoutMetrics)', 'io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.$1'
    $text = $text -replace 'io\.github\.togls\.kp2acomposekeyboard\.ui\.keyboard\.layout\.(DefaultKeyboardLayout|LetterKeyboard|NumberKeyboard|SymbolKeyboard|CommitTextKeyRow)', 'io.github.togls.kp2acomposekeyboard.ui.keyboard.textinput.$1'
    $text = $text -replace 'io\.github\.togls\.kp2acomposekeyboard\.ui\.keyboard\.layout\.(EntryKeyboardLayout|EntryFieldGrid|EntryFieldPageState|EntryFieldColumnCount)', 'io.github.togls.kp2acomposekeyboard.ui.keyboard.entry.$1'
    $text = $text -replace 'io\.github\.togls\.kp2acomposekeyboard\.ui\.keyboard\.KeyboardTestTags', 'io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardTestTags'
    Set-Content -LiteralPath $_.FullName -Value $text
}
```

- [ ] **Step 7: Rename root entry point**

In `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardImeContent.kt`, rename the composable:

```kotlin
@Composable
fun KeyboardImeContent(
    state: KeyboardUiState,
    settings: KeyboardSettings,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
```

Update `KeyboardImeService`, debug previews, and androidTest fixtures:

```powershell
$paths = @('app/src/main','app/src/debug','app/src/androidTest')
Get-ChildItem -Path $paths -Recurse -Include *.kt | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw
    $text = $text -replace '\bKeyboardRoot\b', 'KeyboardImeContent'
    $text = $text -replace 'io\.github\.togls\.kp2acomposekeyboard\.ui\.keyboard\.KeyboardRoot', 'io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardImeContent'
    Set-Content -LiteralPath $_.FullName -Value $text
}
```

- [ ] **Step 8: Rename text input composables**

Apply these exact replacements:

```powershell
$paths = @('app/src/main','app/src/test','app/src/androidTest','app/src/debug')
Get-ChildItem -Path $paths -Recurse -Include *.kt | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw
    $text = $text -replace '\bDefaultKeyboardLayout\b', 'TextInputKeyboardLayout'
    $text = $text -replace '\bDefaultKeyboardContent\b', 'TextInputKeyboardContent'
    $text = $text -replace '\bDefaultKeyboardActionRow\b', 'TextInputActionRow'
    $text = $text -replace '\bLetterKeyboard\b', 'LetterTextInputLayout'
    $text = $text -replace '\bNumberKeyboard\b', 'NumberTextInputLayout'
    $text = $text -replace '\bSymbolKeyboard\b', 'SymbolTextInputLayout'
    Set-Content -LiteralPath $_.FullName -Value $text
}
```

- [ ] **Step 9: Run compile and focused tests**

Run:

```powershell
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.KeyboardLayoutMetricsTest" --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.entry.EntryFieldPagingTest"
./gradlew :app:assembleDebug
```

Expected: both commands end with `BUILD SUCCESSFUL`.

- [ ] **Step 10: Verify old UI package buckets are empty or gone**

Run:

```powershell
Get-ChildItem -LiteralPath app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard -Directory | Select-Object Name
rg -n "ui\.keyboard\.(layout|key|row|style|utility)" app/src/main app/src/test app/src/androidTest app/src/debug
```

Expected: directory list includes `entry`, `frame`, `metrics`, `quickactions`, `shared`, and `textinput`; the `rg` command has no matches.

- [ ] **Step 11: Commit**

Run:

```powershell
git add app/src/main app/src/test app/src/androidTest app/src/debug
git commit -m "refactor(keyboard): reorganize ui packages by responsibility"
```

---

### Task 5: Finish Quick Action UI Naming

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/*.kt`
- Modify: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/*.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/frame/KeyboardContentArea.kt`

- [ ] **Step 1: Rename quick-action UI symbols**

Run:

```powershell
$paths = @('app/src/main','app/src/test','app/src/androidTest','app/src/debug')
Get-ChildItem -Path $paths -Recurse -Include *.kt | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw
    $text = $text -replace '\bKeyboardUtilityItem\b', 'KeyboardQuickAction'
    $text = $text -replace '\bavailableKeyboardUtilityItems\b', 'availableKeyboardQuickActions'
    $text = $text -replace '\btoKeyboardUtilityItem\b', 'toKeyboardQuickAction'
    $text = $text -replace '\bUtilityRow\b', 'QuickActionBar'
    $text = $text -replace '\bUtilityPanel\b', 'QuickActionPanel'
    $text = $text -replace '\bUtilityItemSlot\b', 'QuickActionSlot'
    $text = $text -replace '\bUtilityIconButton\b', 'QuickActionIconButton'
    $text = $text -replace '\bUtilityDragState\b', 'QuickActionDragState'
    $text = $text -replace '\brememberUtilityDragState\b', 'rememberQuickActionDragState'
    $text = $text -replace '\bUtilityDragPreview\b', 'QuickActionDragPreview'
    $text = $text -replace '\bUtilityDragSource\b', 'QuickActionDragSource'
    $text = $text -replace '\bUtilityDropTarget\b', 'QuickActionDropTarget'
    $text = $text -replace '\bresolveUtilityDropTarget\b', 'resolveQuickActionDropTarget'
    $text = $text -replace '\bdispatchUtilityDrop\b', 'dispatchQuickActionDrop'
    $text = $text -replace '\bshouldShowRightUtilitySlot\b', 'shouldShowRightQuickActionSlot'
    $text = $text -replace '\bresolveUtilityDragPreviewOffset\b', 'resolveQuickActionDragPreviewOffset'
    $text = $text -replace '\bshouldShowPanelUtilityItem\b', 'shouldShowPanelQuickAction'
    $text = $text -replace '\bshouldShowDraggedSourceItem\b', 'shouldShowDraggedSourceAction'
    Set-Content -LiteralPath $_.FullName -Value $text
}
```

- [ ] **Step 2: Update quick-action data class header**

In `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/KeyboardQuickAction.kt`, ensure the data class and functions start like this:

```kotlin
internal data class KeyboardQuickAction(
    val id: KeyboardQuickActionId,
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val labelRes: Int,
    @param:StringRes val contentDescriptionRes: Int,
)

internal fun availableKeyboardQuickActions(): List<KeyboardQuickAction> {
    return AvailableKeyboardQuickActions
}

internal fun KeyboardQuickActionId.toKeyboardQuickAction(): KeyboardQuickAction? {
    return AvailableKeyboardQuickActions.firstOrNull { action ->
        action.id == this
    }
}
```

- [ ] **Step 3: Update quick-action drop target model header**

In `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionSlotModels.kt`, ensure the sealed interfaces start like this:

```kotlin
internal sealed interface QuickActionDragSource {
    data object Panel : QuickActionDragSource
    data object Pinned : QuickActionDragSource
}

internal sealed interface QuickActionDropTarget {
    data class Center(
        val targetIndex: Int,
    ) : QuickActionDropTarget

    data object Right : QuickActionDropTarget
    data object Outside : QuickActionDropTarget
}
```

Do not change the midpoint insertion algorithm.

- [ ] **Step 4: Rename quick-action tests**

Run:

```powershell
git mv app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/utility app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions
git mv app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/UtilitySlotModelsTest.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionSlotModelsTest.kt
git mv app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/UtilityRowPolicyTest.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions/QuickActionBarPolicyTest.kt
```

Then apply:

```powershell
Get-ChildItem -Path app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/quickactions -Recurse -Include *.kt | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw
    $text = $text -replace 'package io\.github\.togls\.kp2acomposekeyboard\.ui\.keyboard\.utility', 'package io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions'
    $text = $text -replace '\bUtilitySlotModelsTest\b', 'QuickActionSlotModelsTest'
    $text = $text -replace '\bUtilityRowPolicyTest\b', 'QuickActionBarPolicyTest'
    Set-Content -LiteralPath $_.FullName -Value $text
}
```

- [ ] **Step 5: Verify no utility UI names remain**

Run:

```powershell
rg -n "Utility|utility" app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard
```

Expected: no matches.

- [ ] **Step 6: Run quick-action UI tests**

Run:

```powershell
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions.QuickActionBarPolicyTest" --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions.QuickActionSlotModelsTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

Run:

```powershell
git add app/src/main app/src/test
git commit -m "refactor(keyboard): rename utility ui to quick actions"
```

---

### Task 6: Extract Entry Normal and Expanded Content

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/EntryKeyboardLayout.kt`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/NormalEntryContent.kt`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/ExpandedEntryContent.kt`

- [ ] **Step 1: Move `NormalEntryContent` unchanged except visibility**

Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/NormalEntryContent.kt` with imports matching the moved function. The function signature must be:

```kotlin
@Composable
internal fun NormalEntryContent(
    state: KeyboardUiState,
    scrollState: ScrollState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
```

Move the existing body from `EntryKeyboardLayout.kt` without changing:

```kotlin
EntryFieldGrid(
    fields = state.fixedFields,
    onIntent = onIntent,
    modifier = Modifier
        .height(metrics.keyboardRowHeight)
        .testTag(KeyboardTestTags.EntryFixedFields),
)
```

and without changing:

```kotlin
EntryFieldGrid(
    fields = state.extraFields,
    onIntent = onIntent,
    modifier = Modifier
        .height(metrics.remainingFieldsAreaHeight)
        .verticalScroll(scrollState)
        .testTag(KeyboardTestTags.EntryRemainingFields),
)
```

- [ ] **Step 2: Move `ExpandedEntryContent` unchanged except visibility**

Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/ExpandedEntryContent.kt` with imports matching the moved function. The function signature must be:

```kotlin
@Composable
internal fun ExpandedEntryContent(
    state: KeyboardUiState,
    scrollState: ScrollState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
```

Move the existing body from `EntryKeyboardLayout.kt` without changing these behavior-critical blocks:

```kotlin
val pageState = EntryFieldPageState(
    currentOffsetPx = scrollState.value.toFloat(),
    maxScrollOffsetPx = scrollState.maxValue.toFloat(),
    visibleFieldListAreaHeightPx = visibleFieldListAreaHeightPx,
    contentHeightPx = scrollState.maxValue.toFloat() + visibleFieldListAreaHeightPx,
)
```

```kotlin
LaunchedEffect(
    state.mainLayout,
    state.entryFieldDisplayMode,
    state.hasActiveSession,
    state.currentEntryName,
    expandedFieldIds,
) {
    isResettingScroll = true
    try {
        scrollState.scrollTo(0)
    } finally {
        isResettingScroll = false
    }
}
```

```kotlin
val endedUserScroll = wasScrolling &&
        !isScrolling &&
        !isResettingScroll &&
        !isProgrammaticScroll
```

- [ ] **Step 3: Remove moved private functions from `EntryKeyboardLayout.kt`**

After extraction, `EntryKeyboardLayout.kt` should retain:

```kotlin
@Composable
internal fun EntryKeyboardLayout(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
```

and its `when (state.entryFieldDisplayMode)` call should still call:

```kotlin
NormalEntryContent(
    state = state,
    scrollState = normalScrollState,
    onIntent = onIntent,
    modifier = Modifier.weight(1f),
)
```

```kotlin
ExpandedEntryContent(
    state = state,
    scrollState = expandedScrollState,
    onIntent = onIntent,
    modifier = Modifier.weight(1f),
)
```

- [ ] **Step 4: Run entry paging tests**

Run:

```powershell
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.entry.EntryFieldPagingTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

Run:

```powershell
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry
git commit -m "refactor(keyboard): split entry keyboard content"
```

---

### Task 7: Update Test Names, Preview Names, and Documentation References

**Files:**
- Modify: `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/*.kt`
- Modify: `app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/*.kt`
- Modify: `docs/architecture.md`
- Modify: `docs/requirements.md`
- Modify: `docs/testing.md`

- [ ] **Step 1: Rename androidTest classes and fixture names**

Run:

```powershell
git mv app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootTestFixtures.kt app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardImeContentTestFixtures.kt
git mv app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootDefaultLayoutTest.kt app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardImeContentTextInputTest.kt
git mv app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootEntryLayoutTest.kt app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardImeContentEntryTest.kt
git mv app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootSensitiveDataTest.kt app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardImeContentSensitiveDataTest.kt
```

Apply replacements:

```powershell
Get-ChildItem -Path app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard -Recurse -Include *.kt | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw
    $text = $text -replace '\bKeyboardRootTestContent\b', 'KeyboardImeContentTestContent'
    $text = $text -replace '\bKeyboardRootDefaultLayoutTest\b', 'KeyboardImeContentTextInputTest'
    $text = $text -replace '\bKeyboardRootEntryLayoutTest\b', 'KeyboardImeContentEntryTest'
    $text = $text -replace '\bKeyboardRootSensitiveDataTest\b', 'KeyboardImeContentSensitiveDataTest'
    Set-Content -LiteralPath $_.FullName -Value $text
}
```

- [ ] **Step 2: Rename debug preview files and functions**

Run:

```powershell
git mv app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/DefaultKeyboardPreviews.kt app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/TextInputKeyboardPreviews.kt
```

Apply replacements:

```powershell
Get-ChildItem -Path app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview -Recurse -Include *.kt | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw
    $text = $text -replace '\bDefaultKeyboard', 'TextInputKeyboard'
    $text = $text -replace '\bpreviewDefaultState\b', 'previewTextInputState'
    $text = $text -replace '\bpreviewUtilityPanelState\b', 'previewQuickActionPanelState'
    Set-Content -LiteralPath $_.FullName -Value $text
}
```

- [ ] **Step 3: Update documentation references**

Run:

```powershell
$paths = @('docs/architecture.md','docs/requirements.md','docs/testing.md')
foreach ($path in $paths) {
    if (Test-Path -LiteralPath $path) {
        $text = Get-Content -LiteralPath $path -Raw
        $text = $text -replace '\bKeyboardRoot\b', 'KeyboardImeContent'
        $text = $text -replace '\bDefaultInputMode\b', 'TextInputMode'
        $text = $text -replace '\bMainKeyboardLayout\.Default\b', 'MainKeyboardLayout.TextInput'
        $text = $text -replace '\butility slot', 'quick-action slot'
        $text = $text -replace '\butility slots', 'quick-action slots'
        $text = $text -replace '\butility panel', 'quick-action panel'
        Set-Content -LiteralPath $path -Value $text
    }
}
```

- [ ] **Step 4: Verify naming cleanup**

Run:

```powershell
rg -n "KeyboardRoot|DefaultInputMode|MainKeyboardLayout\.Default|KeyboardUtility|UtilityPanel|UtilitySlot|utilitySlots|isUtilityPanelExpanded" app/src/main app/src/test app/src/androidTest app/src/debug docs/architecture.md docs/requirements.md docs/testing.md
```

Expected: no matches except historical references inside already-committed old plan/spec documents under `docs/superpowers`.

- [ ] **Step 5: Run full JVM tests**

Run:

```powershell
./gradlew :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

Run:

```powershell
git add app/src/androidTest app/src/debug docs/architecture.md docs/requirements.md docs/testing.md
git commit -m "refactor(keyboard): update tests and previews for new ui names"
```

---

### Task 8: Final Build and Behavior Verification

**Files:**
- Read: all modified files
- Optional command output only: Gradle build and tests

- [ ] **Step 1: Run full JVM tests**

Run:

```powershell
./gradlew :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run debug build**

Run:

```powershell
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run connected tests if a device is available**

Check:

```powershell
adb devices
```

If a device or emulator is listed as `device`, run:

```powershell
./gradlew :app:connectedDebugAndroidTest
```

Expected when run: `BUILD SUCCESSFUL`.

If no device or emulator is listed, record this exact final summary line:

```text
connectedDebugAndroidTest was not run because no Android device or emulator was available.
```

- [ ] **Step 4: Inspect final diff for behavior drift**

Run:

```powershell
git diff --stat HEAD
rg -n "CommitText\\(|CommitField\\(|PASSWORD|TOTP|RECOVERY|println|printStackTrace|Log\\." app/src/main app/src/test app/src/androidTest app/src/debug
```

Expected:

- `git diff --stat HEAD` shows no uncommitted tracked implementation changes after Task 7.
- Sensitive-value search does not reveal new field values in UI code.
- Logging search does not show newly introduced raw logging.

- [ ] **Step 5: If final verification required fixes, commit them**

If Step 1, Step 2, or Step 4 required tracked file changes, run:

```powershell
git add app/src/main app/src/test app/src/androidTest app/src/debug docs
git commit -m "fix(keyboard): preserve behavior after ui structure refactor"
```

If no tracked files changed, do not create an empty commit.

## Self-Review

- Spec coverage: Tasks cover text input naming, quick-action naming, UI entry point rename, package reorganization, entry content extraction, tests, previews, docs, and final verification.
- Placeholder scan: The plan contains no open implementation placeholders. Every command has an expected outcome, and behavior-change situations instruct the worker to stop and ask the user.
- Type consistency: The plan consistently uses `TextInputMode`, `MainKeyboardLayout.TextInput`, `KeyboardQuickActionId`, `KeyboardQuickActionSlots`, `KeyboardQuickActionSlotsReducer`, `KeyboardImeContent`, `quickActionSlots`, and `isQuickActionPanelExpanded`.
