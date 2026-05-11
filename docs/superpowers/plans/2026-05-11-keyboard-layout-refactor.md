# Keyboard Layout Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the keyboard UI below `KeyboardRoot` around centralized layout metrics, staged default and entry layout migration, and Compose UI regression tests.

**Architecture:** Keep `KeyboardRoot` as the stable external entry point. Add a pure `KeyboardLayoutMetrics` calculation layer, then migrate default keyboard, normal entry scrolling, expanded entry paging, drag-end snap, tests, previews, and cleanup in separate reviewable stages.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Android InputMethodService UI, JUnit, Robolectric, Compose UI Test.

---

## Spec Source

Implement against:

- `docs/superpowers/specs/2026-05-11-keyboard-layout-refactor-design.md`

The implementation must not introduce a full keyboard layout DSL, must not add `minKeyHeight`, and must not pass sensitive field values into UI state, Composables, semantics, test tags, logs, tests, screenshots, or docs.

## File Structure

### New Files

- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetrics.kt`
  - Pure input/output models and layout metric formulas.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardFrame.kt`
  - Internal frame below `KeyboardRoot`; owns candidate row, keyboard area, bottom spacer, and navigation spacer measurement.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardRow.kt`
  - Lightweight row helper for spacing, alignment, and clipping.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutLocals.kt`
  - CompositionLocal for `KeyboardLayoutMetrics`.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldGrid.kt`
  - Shared field-grid rendering for normal and expanded entry modes.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPaging.kt`
  - Pure paging, enablement, clamp, and snap helpers for expanded mode.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardTestTags.kt`
  - Safe, value-free test tags for layout areas and field keys.
- `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetricsTest.kt`
  - JVM tests for metrics formulas.
- `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPagingTest.kt`
  - JVM tests for expanded paging formulas.
- `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootLayoutTest.kt`
  - Compose UI tests for default, normal entry, expanded entry, controls, scrolling, and sensitive-value absence.

### Modified Files

- `gradle/libs.versions.toml`
  - Add Compose UI test dependency aliases.
- `app/build.gradle.kts`
  - Add `testInstrumentationRunner` and Compose UI test dependencies.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRoot.kt`
  - Keep public signature; delegate internal rendering to `KeyboardFrame`.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardBottomGap.kt`
  - Expose a pure bottom gap height helper used by metrics.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardNavigationBarSpacer.kt`
  - Keep rendering the actual navigation spacer; metrics reads the same inset value from `WindowInsets.navigationBars`.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/FieldKey.kt`
  - Add safe test tag based on field id only.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/DefaultKeyboardLayout.kt`
  - Consume shared metrics.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/LetterKeyboard.kt`
  - Consume shared metrics and row helper.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/NumberKeyboard.kt`
  - Consume shared metrics and row helper.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/SymbolKeyboard.kt`
  - Consume shared metrics and row helper.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt`
  - Render normal continuous scroll and expanded continuous list with page controls.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/ExtraFieldPagedPanel.kt`
  - Remove from active normal entry path after migration.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/AllFieldsExpandedPanel.kt`
  - Replace active path or simplify around `EntryFieldGrid`.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/EntryActionRows.kt`
  - Remove normal previous/next controls.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/ExpandedEntryActionRows.kt`
  - Wire previous/next to page-scroll helpers.
- `app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/KeyboardPreviewFixtures.kt`
  - Keep safe preview labels; add long-label and low-field fixtures.
- `app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/EntryKeyboardPreviews.kt`
  - Add normal, expanded, landscape, dark, and long-label previews.

---

## Task 1: Configure Compose UI Test Dependencies

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add version catalog aliases**

Modify `gradle/libs.versions.toml` under `[libraries]`:

```toml
androidx-compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
androidx-compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
```

- [ ] **Step 2: Configure the instrumentation runner and dependencies**

Modify `app/build.gradle.kts`.

Inside `defaultConfig`:

```kotlin
testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
```

Inside `dependencies`:

```kotlin
androidTestImplementation(platform(libs.androidx.compose.bom))
androidTestImplementation(libs.androidx.compose.ui.test.junit4)
debugImplementation(libs.androidx.compose.ui.test.manifest)
```

- [ ] **Step 3: Verify dependency configuration**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds. No Compose UI test has been added yet.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "test(keyboard): add compose ui test dependencies"
```

---

## Task 2: Add Pure Keyboard Layout Metrics

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetrics.kt`
- Create: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetricsTest.kt`

- [ ] **Step 1: Write failing metrics tests**

Create `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetricsTest.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class KeyboardLayoutMetricsTest {
    @Test
    fun `standard key width uses ten key reference row`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 106.dp,
                totalHeight = 300.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 0.dp,
                verticalOuterPadding = 0.dp,
                keySpacing = 2.dp,
                rowSpacing = 4.dp,
                bottomSpacerHeight = 0.dp,
                navigationSpacerHeight = 0.dp,
            ),
        )

        assertEquals(8.8f, metrics.standardKeyWidth.value, 0.001f)
    }

    @Test
    fun `keyboard row height subtracts candidate spacer navigation and three row gaps once`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 300.dp,
                totalHeight = 260.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 8.dp,
                verticalOuterPadding = 10.dp,
                keySpacing = 6.dp,
                rowSpacing = 5.dp,
                bottomSpacerHeight = 20.dp,
                navigationSpacerHeight = 25.dp,
            ),
        )

        assertEquals(35f, metrics.keyboardRowHeight.value, 0.001f)
        assertEquals(75f, metrics.remainingFieldsAreaHeight.value, 0.001f)
    }

    @Test
    fun `field width supports three and four columns`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 320.dp,
                totalHeight = 260.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 10.dp,
                verticalOuterPadding = 8.dp,
                keySpacing = 5.dp,
                rowSpacing = 4.dp,
                bottomSpacerHeight = 0.dp,
                navigationSpacerHeight = 0.dp,
            ),
        )

        assertEquals(96.666f, metrics.fieldKeyWidth(3).value, 0.001f)
        assertEquals(71.25f, metrics.fieldKeyWidth(4).value, 0.001f)
    }

    @Test
    fun `metrics never returns negative dimensions`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 10.dp,
                totalHeight = 10.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 20.dp,
                verticalOuterPadding = 20.dp,
                keySpacing = 6.dp,
                rowSpacing = 40.dp,
                bottomSpacerHeight = 20.dp,
                navigationSpacerHeight = 20.dp,
            ),
        )

        assertEquals(0f, metrics.standardKeyWidth.value, 0.001f)
        assertEquals(0f, metrics.sideKeyWidth.value, 0.001f)
        assertEquals(0f, metrics.keyboardRowHeight.value, 0.001f)
        assertEquals(40f, metrics.remainingFieldsAreaHeight.value, 0.001f)
        assertEquals(0f, metrics.fieldKeyWidth(3).value, 0.001f)
    }

    @Test
    fun `field width rejects invalid columns`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 300.dp,
                totalHeight = 260.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 8.dp,
                verticalOuterPadding = 8.dp,
                keySpacing = 6.dp,
                rowSpacing = 4.dp,
                bottomSpacerHeight = 0.dp,
                navigationSpacerHeight = 0.dp,
            ),
        )

        assertThrows(IllegalArgumentException::class.java) {
            metrics.fieldKeyWidth(0)
        }
    }
}
```

- [ ] **Step 2: Run metrics tests and verify they fail**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.KeyboardLayoutMetricsTest"
```

Expected: fails because `KeyboardLayoutInput` and `calculateKeyboardLayoutMetrics` do not exist.

- [ ] **Step 3: Implement metrics**

Create `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetrics.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
internal data class KeyboardLayoutInput(
    val totalWidth: Dp,
    val totalHeight: Dp,
    val candidateRowHeight: Dp,
    val horizontalPadding: Dp,
    val verticalOuterPadding: Dp,
    val keySpacing: Dp,
    val rowSpacing: Dp,
    val bottomSpacerHeight: Dp,
    val navigationSpacerHeight: Dp,
)

@Immutable
internal data class KeyboardLayoutMetrics(
    val standardKeyWidth: Dp,
    val sideKeyWidth: Dp,
    val keyboardRowHeight: Dp,
    val remainingFieldsAreaHeight: Dp,
    private val availableWidth: Dp,
    private val keySpacing: Dp,
) {
    fun fieldKeyWidth(columns: Int): Dp {
        require(columns >= 1) { "columns must be >= 1." }

        val gapCount = columns - 1
        return ((availableWidth - keySpacing * gapCount.toFloat()) / columns.toFloat())
            .coerceAtLeast(0.dp)
    }
}

internal fun calculateKeyboardLayoutMetrics(
    input: KeyboardLayoutInput,
): KeyboardLayoutMetrics {
    val availableWidth = (input.totalWidth - input.horizontalPadding * 2f)
        .coerceAtLeast(0.dp)
    val standardKeyWidth = ((availableWidth - input.keySpacing * STANDARD_GAP_COUNT) /
        STANDARD_KEY_COUNT.toFloat()).coerceAtLeast(0.dp)
    val sideKeyWidth = ((availableWidth -
        standardKeyWidth * SIDE_KEY_STANDARD_COUNT.toFloat() -
        input.keySpacing * SIDE_KEY_GAP_COUNT.toFloat()) / 2f).coerceAtLeast(0.dp)

    val keyboardAreaHeight = input.totalHeight -
        input.candidateRowHeight -
        input.verticalOuterPadding * 2f -
        input.bottomSpacerHeight -
        input.navigationSpacerHeight
    val keyboardRowHeight = ((keyboardAreaHeight - input.rowSpacing * KEYBOARD_ROW_GAP_COUNT) /
        KEYBOARD_ROW_COUNT.toFloat()).coerceAtLeast(0.dp)

    return KeyboardLayoutMetrics(
        standardKeyWidth = standardKeyWidth,
        sideKeyWidth = sideKeyWidth,
        keyboardRowHeight = keyboardRowHeight,
        remainingFieldsAreaHeight = keyboardRowHeight * REMAINING_FIELD_ROW_COUNT.toFloat() +
            input.rowSpacing,
        availableWidth = availableWidth,
        keySpacing = input.keySpacing,
    )
}

private const val STANDARD_KEY_COUNT = 10
private const val STANDARD_GAP_COUNT = STANDARD_KEY_COUNT - 1
private const val KEYBOARD_ROW_COUNT = 4
private const val KEYBOARD_ROW_GAP_COUNT = KEYBOARD_ROW_COUNT - 1
private const val SIDE_KEY_STANDARD_COUNT = 7
private const val SIDE_KEY_GAP_COUNT = SIDE_KEY_STANDARD_COUNT + 1
private const val REMAINING_FIELD_ROW_COUNT = 2
```

- [ ] **Step 4: Run metrics tests and verify they pass**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.KeyboardLayoutMetricsTest"
```

Expected: PASS.

- [ ] **Step 5: Run build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetrics.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetricsTest.kt
git commit -m "feat(keyboard): add layout metrics calculator"
```

---

## Task 3: Add Frame, Layout Local, Row Helper, and Safe Test Tags

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutLocals.kt`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardRow.kt`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardFrame.kt`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardTestTags.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRoot.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardBottomGap.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/FieldKey.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardContentArea.kt`

- [ ] **Step 1: Add layout local**

Create `KeyboardLayoutLocals.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

internal val LocalKeyboardLayoutMetrics = compositionLocalOf {
    calculateKeyboardLayoutMetrics(
        KeyboardLayoutInput(
            totalWidth = 0.dp,
            totalHeight = 0.dp,
            candidateRowHeight = 0.dp,
            horizontalPadding = 0.dp,
            verticalOuterPadding = 0.dp,
            keySpacing = 0.dp,
            rowSpacing = 0.dp,
            bottomSpacerHeight = 0.dp,
            navigationSpacerHeight = 0.dp,
        ),
    )
}
```

- [ ] **Step 2: Add row helper**

Create `KeyboardRow.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics

@Composable
internal fun KeyboardRow(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
        horizontalArrangement = Arrangement.spacedBy(
            space = KeyboardMetrics.KeySpacing,
            alignment = horizontalAlignment,
        ),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
```

- [ ] **Step 3: Add safe test tags**

Create `KeyboardTestTags.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard

internal object KeyboardTestTags {
    const val Root = "keyboard-root"
    const val CandidateRow = "keyboard-candidate-row"
    const val DefaultContent = "keyboard-default-content"
    const val EntryNormalContent = "keyboard-entry-normal-content"
    const val EntryExpandedContent = "keyboard-entry-expanded-content"
    const val RemainingFields = "keyboard-remaining-fields"
    const val ExpandedFields = "keyboard-expanded-fields"
    const val PreviousPage = "keyboard-previous-page"
    const val NextPage = "keyboard-next-page"

    fun field(fieldId: String): String = "keyboard-field-$fieldId"
}
```

- [ ] **Step 4: Expose bottom gap height**

Modify `KeyboardBottomGap.kt` so `KeyboardFrame` and the spacer use the same value:

```kotlin
@Composable
internal fun KeyboardBottomGap(
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(keyboardBottomGapHeight(isLandscape)),
    )
}

internal fun keyboardBottomGapHeight(isLandscape: Boolean) = if (isLandscape) {
    0.dp
} else {
    32.dp
}
```

- [ ] **Step 5: Add field test tag without exposing values**

Modify `FieldKey.kt`:

```kotlin
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardTestTags
```

Then update the `KeyboardKey` modifier:

```kotlin
modifier = modifier.testTag(KeyboardTestTags.field(field.id)),
```

Keep the click as:

```kotlin
onClick = { onIntent(KeyboardIntent.CommitField(field.id)) },
```

- [ ] **Step 6: Add frame, split candidate row from keyboard area, and delegate from `KeyboardRoot`**

Create `KeyboardFrame.kt`. The frame owns the candidate row, utility panel, drag preview, bottom gap, and navigation spacer. `KeyboardContentArea` must stop rendering `UtilityRow`, `UtilityPanel`, and `UtilityDragPreview`; it should render only `DefaultKeyboardLayout` or `EntryKeyboardLayout` for the keyboard area.

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.feature.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardBottomGap
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardNavigationBarSpacer
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardTestTags
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keyboardBottomGapHeight
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardAdaptiveMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.LocalKeyboardAdaptiveMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.UtilityDragPreview
import io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.UtilityPanel
import io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.UtilityRow
import io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.rememberUtilityDragState

@Composable
internal fun KeyboardFrame(
    state: KeyboardUiState,
    adaptiveMetrics: KeyboardAdaptiveMetrics,
    isLandscape: Boolean,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val utilityDragState = rememberUtilityDragState()
    var frameBounds by remember { mutableStateOf<Rect?>(null) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .onGloballyPositioned { coordinates ->
                frameBounds = coordinates.boundsInRoot()
            }
            .testTag(KeyboardTestTags.Root),
    ) {
        val navigationSpacerHeight = with(density) {
            WindowInsets.navigationBars.getBottom(this).toDp()
        }
        val bottomSpacerHeight = keyboardBottomGapHeight(isLandscape)
        val metrics = remember(
            maxWidth,
            maxHeight,
            adaptiveMetrics,
            isLandscape,
            navigationSpacerHeight,
            bottomSpacerHeight,
        ) {
            calculateKeyboardLayoutMetrics(
                KeyboardLayoutInput(
                    totalWidth = maxWidth,
                    totalHeight = maxHeight,
                    candidateRowHeight = adaptiveMetrics.keyHeight,
                    horizontalPadding = KeyboardMetrics.OuterPaddingHorizontal,
                    verticalOuterPadding = KeyboardMetrics.OuterPaddingVertical,
                    keySpacing = KeyboardMetrics.KeySpacing,
                    rowSpacing = KeyboardMetrics.RowSpacing,
                    bottomSpacerHeight = bottomSpacerHeight,
                    navigationSpacerHeight = navigationSpacerHeight,
                ),
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            CompositionLocalProvider(
                LocalKeyboardAdaptiveMetrics provides adaptiveMetrics,
            ) {
                UtilityRow(
                    state = state,
                    dragState = utilityDragState,
                    onIntent = onIntent,
                    modifier = Modifier.testTag(KeyboardTestTags.CandidateRow),
                )
            }

            CompositionLocalProvider(
                LocalKeyboardLayoutMetrics provides metrics,
                LocalKeyboardAdaptiveMetrics provides adaptiveMetrics.copy(
                    keyHeight = metrics.keyboardRowHeight,
                ),
            ) {
                if (state.isUtilityPanelExpanded) {
                    UtilityPanel(
                        state = state,
                        dragState = utilityDragState,
                        onIntent = onIntent,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    when (state.mainLayout) {
                        MainKeyboardLayout.Default -> DefaultKeyboardLayout(
                            state = state,
                            onIntent = onIntent,
                            modifier = Modifier.weight(1f),
                        )

                        MainKeyboardLayout.Entry -> EntryKeyboardLayout(
                            state = state,
                            onIntent = onIntent,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            KeyboardBottomGap(isLandscape = isLandscape)
            KeyboardNavigationBarSpacer()
        }

        UtilityDragPreview(
            dragState = utilityDragState,
            containerBoundsInRoot = frameBounds,
        )
    }
}
```

Then simplify `KeyboardContentArea.kt` or stop calling it. If the file remains temporarily, its implementation must not render the candidate row or utility panel; the active path must be owned by `KeyboardFrame`.

Then replace the internal `Column` body in `KeyboardRoot.kt` with:

```kotlin
KeyboardFrame(
    state = state,
    adaptiveMetrics = adaptiveMetrics,
    isLandscape = isLandscape,
    onIntent = onIntent,
)
```

Keep the `KeyboardRoot` function signature unchanged.

- [ ] **Step 7: Run checks**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutLocals.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardRow.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardFrame.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardContentArea.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardTestTags.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRoot.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardBottomGap.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/FieldKey.kt
git commit -m "refactor(keyboard): introduce layout frame helpers"
```

---

## Task 4: Migrate Default Keyboard to Shared Metrics

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/DefaultKeyboardLayout.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/LetterKeyboard.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/NumberKeyboard.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/SymbolKeyboard.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardContentArea.kt`

- [ ] **Step 1: Remove duplicate default key-height calculation**

In `KeyboardContentArea.kt`, remove `resolveDefaultKeyHeight`, `defaultVisualRowCount`, and related constants. Do not compute metrics from `onGloballyPositioned`; the existing drag-preview bounds state may remain because it does not calculate metrics.

- [ ] **Step 2: Use metrics in `LetterKeyboard`**

Replace the internal `KeyboardWidthLayout` block in `LetterKeyboard.kt` with `LocalKeyboardLayoutMetrics`:

```kotlin
val metrics = LocalKeyboardLayoutMetrics.current
val standardWidth = metrics.standardKeyWidth
val sideWidth = metrics.sideKeyWidth
```

Use `KeyboardRow` for each row:

```kotlin
KeyboardRow {
    "qwertyuiop".forEach { letter ->
        LetterKey(
            modifier = Modifier.width(standardWidth),
            letter = letter,
            isUppercase = state.isUppercase,
            onIntent = onIntent,
        )
    }
}
```

For the third row:

```kotlin
KeyboardRow {
    ShiftKey(
        onIntent = onIntent,
        modifier = Modifier.width(sideWidth),
    )

    "zxcvbnm".forEach { letter ->
        LetterKey(
            modifier = Modifier.width(standardWidth),
            letter = letter,
            isUppercase = state.isUppercase,
            onIntent = onIntent,
        )
    }

    DeleteKey(
        modifier = Modifier.width(sideWidth),
        onIntent = onIntent,
    )
}
```

- [ ] **Step 3: Use metrics in `NumberKeyboard` and `SymbolKeyboard`**

Replace local width calculation with:

```kotlin
val metrics = LocalKeyboardLayoutMetrics.current
val standardWidth = metrics.standardKeyWidth
val sideWidth = metrics.sideKeyWidth
```

Keys default to `Modifier.width(standardWidth)`. Only explicit matching edge keys use `sideWidth`.

- [ ] **Step 4: Keep bottom action row explicit**

In `DefaultKeyboardLayout.kt`, keep existing bottom action content, but avoid hidden key-width inference. If a key should be flexible, use `Modifier.weight(...)` explicitly. If a key should be standard, use `Modifier.width(LocalKeyboardLayoutMetrics.current.standardKeyWidth)` explicitly.

- [ ] **Step 5: Run checks**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/DefaultKeyboardLayout.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/LetterKeyboard.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/NumberKeyboard.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/SymbolKeyboard.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardContentArea.kt
git commit -m "refactor(keyboard): migrate default layout to metrics"
```

---

## Task 5: Add Entry Paging Pure Helpers

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPaging.kt`
- Create: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPagingTest.kt`

- [ ] **Step 1: Write failing paging tests**

Create `EntryFieldPagingTest.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryFieldPagingTest {
    @Test
    fun `previous and next targets clamp to bounds`() {
        val page = EntryFieldPageState(
            currentOffsetPx = 150f,
            maxScrollOffsetPx = 260f,
            visibleFieldListAreaHeightPx = 100f,
            contentHeightPx = 360f,
        )

        assertEquals(50f, page.previousTargetPx(), 0.001f)
        assertEquals(250f, page.nextTargetPx(), 0.001f)
    }

    @Test
    fun `controls disable when content fits one page`() {
        val page = EntryFieldPageState(
            currentOffsetPx = 0f,
            maxScrollOffsetPx = 0f,
            visibleFieldListAreaHeightPx = 200f,
            contentHeightPx = 180f,
        )

        assertFalse(page.previousEnabled)
        assertFalse(page.nextEnabled)
        assertEquals(0f, page.snapTargetPx(), 0.001f)
    }

    @Test
    fun `snap target uses nearest page and clamps`() {
        val page = EntryFieldPageState(
            currentOffsetPx = 151f,
            maxScrollOffsetPx = 260f,
            visibleFieldListAreaHeightPx = 100f,
            contentHeightPx = 360f,
        )

        assertEquals(200f, page.snapTargetPx(), 0.001f)
    }

    @Test
    fun `zero visible height disables paging math`() {
        val page = EntryFieldPageState(
            currentOffsetPx = 100f,
            maxScrollOffsetPx = 200f,
            visibleFieldListAreaHeightPx = 0f,
            contentHeightPx = 300f,
        )

        assertFalse(page.previousEnabled)
        assertFalse(page.nextEnabled)
        assertEquals(0f, page.previousTargetPx(), 0.001f)
        assertEquals(0f, page.nextTargetPx(), 0.001f)
        assertEquals(0f, page.snapTargetPx(), 0.001f)
    }
}
```

- [ ] **Step 2: Run paging tests and verify they fail**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.EntryFieldPagingTest"
```

Expected: fails because `EntryFieldPageState` does not exist.

- [ ] **Step 3: Implement paging helpers**

Create `EntryFieldPaging.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import kotlin.math.round

internal data class EntryFieldPageState(
    val currentOffsetPx: Float,
    val maxScrollOffsetPx: Float,
    val visibleFieldListAreaHeightPx: Float,
    val contentHeightPx: Float,
) {
    val previousEnabled: Boolean
        get() = canPage && currentOffsetPx > 0f

    val nextEnabled: Boolean
        get() = canPage && currentOffsetPx < maxScrollOffsetPx

    fun previousTargetPx(): Float {
        if (!canPage) return 0f
        return (currentOffsetPx - visibleFieldListAreaHeightPx)
            .coerceAtLeast(0f)
    }

    fun nextTargetPx(): Float {
        if (!canPage) return 0f
        return (currentOffsetPx + visibleFieldListAreaHeightPx)
            .coerceAtMost(maxScrollOffsetPx)
    }

    fun snapTargetPx(): Float {
        if (!canPage) return 0f
        val targetPage = round(currentOffsetPx / visibleFieldListAreaHeightPx)
        return (targetPage * visibleFieldListAreaHeightPx)
            .coerceIn(0f, maxScrollOffsetPx)
    }

    private val canPage: Boolean
        get() = visibleFieldListAreaHeightPx > 0f &&
            contentHeightPx > visibleFieldListAreaHeightPx &&
            maxScrollOffsetPx > 0f
}
```

- [ ] **Step 4: Run paging tests and full unit tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.EntryFieldPagingTest"
./gradlew :app:testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPaging.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPagingTest.kt
git commit -m "feat(keyboard): add entry field paging math"
```

---

## Task 6: Implement Normal Entry Continuous Scroll

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldGrid.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/EntryActionRows.kt`

- [ ] **Step 1: Add field grid renderer**

Create `EntryFieldGrid.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.FieldKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics

@Composable
internal fun EntryFieldGrid(
    fields: List<KeyboardFieldUiModel>,
    columns: Int,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(columns >= 1) { "columns must be >= 1." }

    val metrics = LocalKeyboardLayoutMetrics.current
    val fieldWidth = metrics.fieldKeyWidth(columns)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        fields.chunked(columns).forEach { rowFields ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
            ) {
                rowFields.forEach { field ->
                    FieldKey(
                        modifier = Modifier.width(fieldWidth),
                        field = field,
                        onIntent = onIntent,
                    )
                }

                repeat(columns - rowFields.size) {
                    Box(modifier = Modifier.width(fieldWidth))
                }
            }
        }
    }
}
```

- [ ] **Step 2: Replace normal entry paging path**

In `EntryKeyboardLayout.kt`, keep the external function signature. In `EntryFieldDisplayMode.Paged`, render:

```kotlin
NormalEntryContent(
    state = state,
    onIntent = onIntent,
    modifier = Modifier.weight(1f),
)
```

Add `NormalEntryContent`:

```kotlin
@Composable
private fun NormalEntryContent(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalKeyboardLayoutMetrics.current
    val scrollState = rememberScrollState()

    LaunchedEffect(state.currentEntryName, state.fixedFields, state.extraFields) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(KeyboardTestTags.EntryNormalContent),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        EntryFieldGrid(
            fields = state.fixedFields,
            columns = ENTRY_FIELD_COLUMNS,
            onIntent = onIntent,
            modifier = Modifier.height(metrics.keyboardRowHeight),
        )

        EntryFieldGrid(
            fields = state.extraFields,
            columns = ENTRY_FIELD_COLUMNS,
            onIntent = onIntent,
            modifier = Modifier
                .height(metrics.remainingFieldsAreaHeight)
                .verticalScroll(scrollState)
                .testTag(KeyboardTestTags.RemainingFields),
        )

        NormalEntryActionRow(
            onIntent = onIntent,
            modifier = Modifier.height(metrics.keyboardRowHeight),
        )
    }
}

private const val ENTRY_FIELD_COLUMNS = 3
```

Add imports:

```kotlin
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardTestTags
```

- [ ] **Step 3: Remove previous and next from normal action row**

In `EntryActionRows.kt`, replace `PagedEntryActionRow` with `NormalEntryActionRow`:

```kotlin
@Composable
fun NormalEntryActionRow(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardRow(modifier = modifier) {
        SwitchToDefaultLayoutKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1.4f),
        )

        SelectEntryKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1.5f),
        )

        ExpandFieldsKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1f),
        )

        ClearEntryKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1f),
        )

        DeleteKey(
            modifier = Modifier.weight(1f),
            onIntent = onIntent,
        )
    }
}
```

- [ ] **Step 4: Run checks**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldGrid.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/EntryActionRows.kt
git commit -m "feat(keyboard): make normal entry fields scroll"
```

---

## Task 7: Implement Expanded Entry Paging Controls

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/ExpandedEntryActionRows.kt`

- [ ] **Step 1: Render expanded list as one continuous grid**

In `EntryKeyboardLayout.kt`, replace `ExpandedEntryContent` internals with a single field list:

```kotlin
val expandedFields = state.fixedFields + state.extraFields
```

Render with:

```kotlin
EntryFieldGrid(
    fields = expandedFields,
    columns = ENTRY_FIELD_COLUMNS,
    onIntent = onIntent,
    modifier = Modifier
        .weight(1f)
        .verticalScroll(scrollState)
        .testTag(KeyboardTestTags.ExpandedFields),
)
```

- [ ] **Step 2: Add page-state measurement without metrics state writes**

Inside `ExpandedEntryContent`, derive current page state from `scrollState` and the visible field-list area. This uses size state only for scroll controls; it must not feed back into layout metrics.

```kotlin
var visibleFieldListAreaHeightPx by remember { mutableFloatStateOf(0f) }
val pageState = EntryFieldPageState(
    currentOffsetPx = scrollState.value.toFloat(),
    maxScrollOffsetPx = scrollState.maxValue.toFloat(),
    visibleFieldListAreaHeightPx = visibleFieldListAreaHeightPx,
    contentHeightPx = scrollState.maxValue.toFloat() + visibleFieldListAreaHeightPx,
)

Box(modifier = Modifier.weight(1f)) {
    EntryFieldGrid(
        fields = state.fixedFields + state.extraFields,
        columns = ENTRY_FIELD_COLUMNS,
        onIntent = onIntent,
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                visibleFieldListAreaHeightPx = size.height.toFloat()
            }
            .verticalScroll(scrollState)
            .testTag(KeyboardTestTags.ExpandedFields),
    )
}
```

Add imports:

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onSizeChanged
```

- [ ] **Step 3: Wire previous and next buttons to page targets**

Use a `CoroutineScope` already created in `EntryKeyboardLayout`:

```kotlin
ExpandedEntryActionRows(
    canScrollUp = pageState.previousEnabled,
    canScrollDown = pageState.nextEnabled,
    onScrollUp = {
        coroutineScope.launch {
            scrollState.animateScrollTo(pageState.previousTargetPx().toInt())
        }
    },
    onScrollDown = {
        coroutineScope.launch {
            scrollState.animateScrollTo(pageState.nextTargetPx().toInt())
        }
    },
    onIntent = onIntent,
)
```

The expanded content root must expose the safe test tag and keep `pageState` in the same scope as the action rows:

```kotlin
Column(
    modifier = modifier
        .fillMaxWidth()
        .testTag(KeyboardTestTags.EntryExpandedContent),
    verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
) {
    Box(modifier = Modifier.weight(1f)) {
        EntryFieldGrid(
            fields = state.fixedFields + state.extraFields,
            columns = ENTRY_FIELD_COLUMNS,
            onIntent = onIntent,
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    visibleFieldListAreaHeightPx = size.height.toFloat()
                }
                .verticalScroll(scrollState)
                .testTag(KeyboardTestTags.ExpandedFields),
        )
    }

    ExpandedEntryActionRows(
        canScrollUp = pageState.previousEnabled,
        canScrollDown = pageState.nextEnabled,
        onScrollUp = {
            coroutineScope.launch {
                scrollState.animateScrollTo(pageState.previousTargetPx().toInt())
            }
        },
        onScrollDown = {
            coroutineScope.launch {
                scrollState.animateScrollTo(pageState.nextTargetPx().toInt())
            }
        },
        onIntent = onIntent,
    )
}
```

In `ExpandedEntryActionRows.kt`, attach tags to previous and next page controls:

```kotlin
PreviousPageKey(
    enabled = canScrollUp,
    onClick = onScrollUp,
    modifier = Modifier
        .weight(1f)
        .testTag(KeyboardTestTags.PreviousPage),
)

NextPageKey(
    enabled = canScrollDown,
    onClick = onScrollDown,
    modifier = Modifier
        .weight(1f)
        .testTag(KeyboardTestTags.NextPage),
)
```

- [ ] **Step 4: Reset and clamp scroll when mode or content changes**

Keep the existing reset effect and expand it:

```kotlin
LaunchedEffect(
    state.entryFieldDisplayMode,
    state.currentEntryName,
    state.fixedFields,
    state.extraFields,
) {
    scrollState.scrollTo(0)
}
```

Add a clamp effect:

```kotlin
LaunchedEffect(scrollState.maxValue) {
    if (scrollState.value > scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }
}
```

- [ ] **Step 5: Run checks**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/ExpandedEntryActionRows.kt
git commit -m "feat(keyboard): page expanded entry fields"
```

---

## Task 8: Add Drag-End Snap for Expanded Entry

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt`

- [ ] **Step 1: Add snap state guard**

Inside expanded field list composition, track user scroll completion with `LaunchedEffect`:

```kotlin
LaunchedEffect(scrollState.isScrollInProgress, pageState) {
    if (!scrollState.isScrollInProgress) {
        val target = pageState.snapTargetPx().toInt()
        if (target != scrollState.value) {
            scrollState.animateScrollTo(target)
        }
    }
}
```

Keep the `EntryFieldPageState` `canPage` guard as the only place deciding whether snap is meaningful.

- [ ] **Step 2: Avoid snap during reset**

If the snap animation fights the reset effect, add a local boolean:

```kotlin
var isResettingScroll by remember { mutableStateOf(false) }
```

Set it around `scrollState.scrollTo(0)`:

```kotlin
isResettingScroll = true
scrollState.scrollTo(0)
isResettingScroll = false
```

Then guard snap:

```kotlin
if (!isResettingScroll && !scrollState.isScrollInProgress) {
    val target = pageState.snapTargetPx().toInt()
    if (target != scrollState.value) {
        scrollState.animateScrollTo(target)
    }
}
```

- [ ] **Step 3: Run checks**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt
git commit -m "feat(keyboard): snap expanded fields to pages"
```

---

## Task 9: Add Compose UI Regression Tests

**Files:**
- Create: `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootLayoutTest.kt`

- [ ] **Step 1: Add Compose UI tests**

Create `KeyboardRootLayoutTest.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel
import io.github.togls.kp2acomposekeyboard.feature.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.feature.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.ui.theme.KeyboardTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class KeyboardRootLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersDefaultKeyboard() {
        setKeyboardContent(KeyboardUiState())

        composeRule.onNodeWithTag(KeyboardTestTags.Root).assertIsDisplayed()
        composeRule.onNodeWithText("q").assertIsDisplayed()
        composeRule.onNodeWithText("p").assertIsDisplayed()
    }

    @Test
    fun normalEntryDoesNotShowPageControls() {
        setKeyboardContent(entryState(EntryFieldDisplayMode.Paged))

        composeRule.onNodeWithTag(KeyboardTestTags.EntryNormalContent).assertIsDisplayed()
        composeRule.onAllNodes(hasTestTag(KeyboardTestTags.PreviousPage)).assertCountEquals(0)
        composeRule.onAllNodes(hasTestTag(KeyboardTestTags.NextPage)).assertCountEquals(0)
    }

    @Test
    fun expandedEntryShowsPageControls() {
        setKeyboardContent(entryState(EntryFieldDisplayMode.Expanded))

        composeRule.onNodeWithTag(KeyboardTestTags.EntryExpandedContent).assertIsDisplayed()
        composeRule.onNodeWithTag(KeyboardTestTags.PreviousPage).assertExists()
        composeRule.onNodeWithTag(KeyboardTestTags.NextPage).assertExists()
    }

    @Test
    fun fieldValuesDoNotAppearInTextSemanticsOrTags() {
        setKeyboardContent(entryState(EntryFieldDisplayMode.Paged))

        forbiddenValues.forEach { forbiddenValue ->
            composeRule.onAllNodes(hasText(forbiddenValue, substring = true)).assertCountEquals(0)
            composeRule.onAllNodes(hasContentDescription(forbiddenValue, substring = true)).assertCountEquals(0)
            composeRule.onAllNodes(hasTestTag(forbiddenValue)).assertCountEquals(0)
        }
    }

    @Test
    fun clickingFieldSendsFieldIdOnly() {
        val intents = mutableListOf<KeyboardIntent>()
        setKeyboardContent(entryState(EntryFieldDisplayMode.Paged), onIntent = intents::add)

        composeRule.onNodeWithTag(KeyboardTestTags.field("password")).performClick()

        assertTrue(intents.contains(KeyboardIntent.CommitField("password")))
        assertFalse(intents.contains(KeyboardIntent.CommitText(PASSWORD_SHOULD_NOT_APPEAR)))
    }

    private fun setKeyboardContent(
        state: KeyboardUiState,
        onIntent: (KeyboardIntent) -> Unit = {},
    ) {
        val settings = KeyboardSettings()
        composeRule.setContent {
            KeyboardTheme(settings = settings) {
                KeyboardRoot(
                    state = state,
                    settings = settings,
                    onIntent = onIntent,
                )
            }
        }
    }

    private fun entryState(displayMode: EntryFieldDisplayMode): KeyboardUiState {
        val fixedFields = listOf(
            KeyboardFieldUiModel("username", "Username", KeyboardFieldType.Username, sensitive = false),
            KeyboardFieldUiModel("password", "Password", KeyboardFieldType.Password, sensitive = true),
            KeyboardFieldUiModel("totp", "TOTP", KeyboardFieldType.Totp, sensitive = true),
        )
        val extraFields = listOf(
            KeyboardFieldUiModel("email", "Email", KeyboardFieldType.Email, sensitive = false),
            KeyboardFieldUiModel("recovery", "Recovery", KeyboardFieldType.Recovery, sensitive = true),
            KeyboardFieldUiModel("token", "Token", KeyboardFieldType.Custom, sensitive = true),
            KeyboardFieldUiModel("notes", "Notes", KeyboardFieldType.Notes, sensitive = false),
        )
        return KeyboardUiState(
            mainLayout = MainKeyboardLayout.Entry,
            entryFieldDisplayMode = displayMode,
            currentEntryName = "Example",
            hasActiveSession = true,
            fixedFields = fixedFields,
            extraFields = extraFields,
            allFields = fixedFields + extraFields,
        )
    }

    private companion object {
        const val PASSWORD_SHOULD_NOT_APPEAR = "PASSWORD_SHOULD_NOT_APPEAR"
        const val TOTP_SHOULD_NOT_APPEAR = "TOTP_SHOULD_NOT_APPEAR"
        const val RECOVERY_CODE_SHOULD_NOT_APPEAR = "RECOVERY_CODE_SHOULD_NOT_APPEAR"

        val forbiddenValues = listOf(
            PASSWORD_SHOULD_NOT_APPEAR,
            TOTP_SHOULD_NOT_APPEAR,
            RECOVERY_CODE_SHOULD_NOT_APPEAR,
        )
    }
}
```

- [ ] **Step 2: Run instrumented tests**

Run with an attached emulator or device:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected: PASS. If no device is attached, record the exact failure in the implementation report and continue only after unit tests and assemble pass.

- [ ] **Step 3: Run full local checks**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootLayoutTest.kt
git commit -m "test(keyboard): cover keyboard root layout behavior"
```

---

## Task 10: Add Previews and Cleanup Old Components

**Files:**
- Modify: `app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/KeyboardPreviewFixtures.kt`
- Modify: `app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/EntryKeyboardPreviews.kt`
- Delete only after references are gone: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardWidthLayout.kt`
- Candidate delete after references are gone: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/TextKeyRow.kt`
- Candidate delete after references are gone: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/LetterRow.kt`
- Candidate delete after references are gone: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/FixedTextKeyRow.kt`
- Candidate delete after references are gone: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/FixedFieldRow.kt`
- Candidate delete after references are gone: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/ExtraFieldPagedPanel.kt`
- Candidate delete after references are gone: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/AllFieldsExpandedPanel.kt`

- [ ] **Step 1: Add preview fixtures**

Add safe long-label fields to `KeyboardPreviewFixtures.kt`:

```kotlin
internal fun previewLongLabelEntryKeyboardState(): KeyboardUiState {
    return previewEntryKeyboardState().copy(
        extraFields = listOf(
            KeyboardFieldUiModel(
                id = "very-long-label",
                label = "Very Long Field Label For Layout",
                type = KeyboardFieldType.Custom,
                sensitive = false,
            ),
            KeyboardFieldUiModel(
                id = "blank-label",
                label = "",
                type = KeyboardFieldType.Custom,
                sensitive = false,
            ),
        ),
    )
}
```

- [ ] **Step 2: Add entry previews**

Add previews in `EntryKeyboardPreviews.kt`:

```kotlin
@Preview(
    name = "Normal - Long Labels",
    group = "Keyboard / Entry",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
)
@Composable
private fun EntryKeyboardNormalLongLabelsPreview() {
    KeyboardPreviewContent(
        state = previewLongLabelEntryKeyboardState(),
    )
}
```

Keep existing dark and landscape previews, updating names from `Paged` to `Normal` where the UI has changed.

- [ ] **Step 3: Find unused old components**

Run:

```bash
rg -n "KeyboardWidthLayout|TextKeyRow|LetterRow|FixedTextKeyRow|FixedFieldRow|ExtraFieldPagedPanel|AllFieldsExpandedPanel|PagedEntryActionRow" app/src
```

Expected: any remaining matches are either intentional compatibility wrappers or files safe to delete.

- [ ] **Step 4: Delete unused old files with `apply_patch`**

Delete only files with no references from Step 3. Use one delete hunk per file, for example:

```diff
*** Begin Patch
*** Delete File: app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardWidthLayout.kt
*** End Patch
```

- [ ] **Step 5: Run final validation**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

Expected: unit tests and assemble pass. `connectedDebugAndroidTest` passes when a device or emulator is attached. If no device exists, record the exact no-device result in the final report.

- [ ] **Step 6: Commit**

```bash
git add app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/KeyboardPreviewFixtures.kt app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/EntryKeyboardPreviews.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard
git commit -m "refactor(keyboard): clean up migrated layout components"
```

---

## Final Verification Checklist

- [ ] `KeyboardRoot` public function signature is unchanged.
- [ ] No full layout DSL was introduced.
- [ ] No `minKeyHeight` visual fallback was introduced.
- [ ] Metrics formulas are centralized in `KeyboardLayoutMetrics.kt`.
- [ ] Metrics calculation does not write Compose state.
- [ ] Default keyboard uses shared metrics.
- [ ] Normal entry mode has no previous or next page controls.
- [ ] Normal entry remaining fields scroll vertically.
- [ ] Expanded entry fields render as one continuous list.
- [ ] Expanded entry previous and next controls page by visible field-list height.
- [ ] Expanded drag-end snap clamps to valid bounds.
- [ ] Field commit remains `KeyboardIntent.CommitField(fieldId)`.
- [ ] Field values do not appear in UI state, Composable params, semantics, test tags, logs, tests, screenshots, or docs.
- [ ] `./gradlew :app:testDebugUnitTest` passes.
- [ ] `./gradlew :app:assembleDebug` passes.
- [ ] `./gradlew :app:connectedDebugAndroidTest` passes, or the final report states no device/emulator was available.
