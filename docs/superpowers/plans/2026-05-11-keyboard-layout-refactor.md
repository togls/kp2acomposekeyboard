# Keyboard Layout Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor keyboard layout internals under the stable `KeyboardRoot` entry point with centralized metrics, staged UI migration, entry scrolling and paging behavior, and regression tests.

**Architecture:** Keep `KeyboardRoot` public API unchanged. Add pure measurement and paging helpers first, then migrate frame, metrics provider, row helpers, default keyboard, normal entry, expanded paging, drag snap, tests, previews, and cleanup as separate reviewable stages.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Android IME UI, JUnit, Robolectric, Compose UI Test.

---

## Source Documents

Implement against:

- `docs/superpowers/specs/2026-05-11-keyboard-layout-refactor-design.md`

This plan intentionally splits work more finely than the design document:

- Stage 0: Compose UI test dependencies
- Stage 1: pure `KeyboardLayoutMetrics`
- Stage 2: minimal `KeyboardFrame`
- Stage 3: metrics provider and layout local
- Stage 4: `KeyboardRow` and safe test tags
- Stage 5: default keyboard migration
- Stage 6: normal entry continuous scroll
- Stage 7: expanded entry paging controls
- Stage 8: drag-end snap
- Stage 9: Compose UI regression tests
- Stage 10: previews
- Stage 11: cleanup old components

Do not implement multiple stages in one patch unless the user explicitly asks for a batch and the batch still compiles between logical checkpoints.

## Hard Constraints

- Keep `KeyboardRoot` public function signature unchanged.
- Do not introduce a full layout DSL or key-spec renderer.
- Do not introduce `minKeyHeight`.
- Use `coerceAtLeast(0.dp)` only to prevent invalid negative layout dimensions.
- Do not change current bottom gap visual values while extracting helpers.
- Do not pass field values into UI state, Composable parameters, semantics, test tags, content descriptions, logs, tests, screenshots, or docs.
- Keep field commit id-based: `KeyboardIntent.CommitField(field.id)`.
- Metrics calculation must not write Compose state or use measurement callbacks to drive layout metrics.
- Old component deletion must be a standalone cleanup stage after the new path is tested.

## File Structure

### New Runtime Files

- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetrics.kt`
  - Pure metrics input, output, and formulas.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardFrame.kt`
  - Internal frame below `KeyboardRoot`; introduced minimally before behavior migration.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutLocals.kt`
  - `LocalKeyboardLayoutMetrics`.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardRow.kt`
  - Lightweight row helper, not a DSL.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardTestTags.kt`
  - Safe test tags based on layout names and field ids.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldGrid.kt`
  - Shared field grid for normal and expanded entry modes.
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPaging.kt`
  - Pure page target, disabled state, clamp, and snap math.

### New Test Files

- `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetricsTest.kt`
- `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPagingTest.kt`
- `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootDefaultLayoutTest.kt`
- `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootEntryLayoutTest.kt`
- `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootSensitiveDataTest.kt`

### Modified Runtime Files

- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRoot.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardBottomGap.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardContentArea.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/FieldKey.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/ActionKeys.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/DefaultKeyboardLayout.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/LetterKeyboard.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/NumberKeyboard.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/SymbolKeyboard.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/EntryActionRows.kt`
- `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/ExpandedEntryActionRows.kt`
- `app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/KeyboardPreviewFixtures.kt`
- `app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/EntryKeyboardPreviews.kt`

---

## Stage 0: Configure Compose UI Test Dependencies

**Goal:** Add Compose UI test infrastructure without changing keyboard runtime behavior.

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 0.1: Add version catalog aliases**

Modify `gradle/libs.versions.toml` under `[libraries]`:

```toml
androidx-compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
androidx-compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
```

- [ ] **Step 0.2: Add instrumentation runner**

Modify `app/build.gradle.kts` inside `defaultConfig`:

```kotlin
testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
```

- [ ] **Step 0.3: Add Compose UI test dependencies**

Modify `app/build.gradle.kts` inside `dependencies`:

```kotlin
androidTestImplementation(platform(libs.androidx.compose.bom))
androidTestImplementation(libs.androidx.compose.ui.test.junit4)
debugImplementation(libs.androidx.compose.ui.test.manifest)
```

- [ ] **Step 0.4: Verify build configuration**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 0.5: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "test(keyboard): add compose ui test dependencies"
```

---

## Stage 1: Add Pure KeyboardLayoutMetrics

**Goal:** Add pure metrics inputs, outputs, and formulas without changing current UI behavior.

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetrics.kt`
- Create: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetricsTest.kt`

- [ ] **Step 1.1: Write failing metrics tests**

Create `KeyboardLayoutMetricsTest.kt`:

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
                sideKeyStandardKeyCount = 7,
            ),
        )

        assertEquals(8.8f, metrics.standardKeyWidth.value, 0.001f)
    }

    @Test
    fun `row height subtracts candidate paddings spacers and three row gaps`() {
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
                sideKeyStandardKeyCount = 7,
            ),
        )

        assertEquals(35f, metrics.keyboardRowHeight.value, 0.001f)
        assertEquals(75f, metrics.remainingFieldsAreaHeight.value, 0.001f)
    }

    @Test
    fun `bottom and navigation spacers may be zero`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 300.dp,
                totalHeight = 220.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 8.dp,
                verticalOuterPadding = 10.dp,
                keySpacing = 6.dp,
                rowSpacing = 5.dp,
                bottomSpacerHeight = 0.dp,
                navigationSpacerHeight = 0.dp,
                sideKeyStandardKeyCount = 7,
            ),
        )

        assertEquals(41.25f, metrics.keyboardRowHeight.value, 0.001f)
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
                sideKeyStandardKeyCount = 7,
            ),
        )

        assertEquals(96.666f, metrics.fieldKeyWidth(3).value, 0.001f)
        assertEquals(71.25f, metrics.fieldKeyWidth(4).value, 0.001f)
    }

    @Test
    fun `small height and large spacing do not produce negative dimensions`() {
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
                sideKeyStandardKeyCount = 7,
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
                sideKeyStandardKeyCount = 7,
            ),
        )

        assertThrows(IllegalArgumentException::class.java) {
            metrics.fieldKeyWidth(0)
        }
    }
}
```

- [ ] **Step 1.2: Run metrics tests and verify failure**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.KeyboardLayoutMetricsTest"
```

Expected: FAIL because `KeyboardLayoutInput` does not exist.

- [ ] **Step 1.3: Implement metrics**

Create `KeyboardLayoutMetrics.kt`:

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
    val sideKeyStandardKeyCount: Int,
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

        return ((availableWidth - keySpacing * (columns - 1).toFloat()) / columns.toFloat())
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
    val sideKeyWidth = sideKeyWidth(
        availableWidth = availableWidth,
        standardKeyWidth = standardKeyWidth,
        keySpacing = input.keySpacing,
        standardKeyCount = input.sideKeyStandardKeyCount,
    )

    // Candidate, bottom, and navigation areas live outside the four keyboard rows.
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

private fun sideKeyWidth(
    availableWidth: Dp,
    standardKeyWidth: Dp,
    keySpacing: Dp,
    standardKeyCount: Int,
): Dp {
    require(standardKeyCount >= 0) { "standardKeyCount must be >= 0." }
    val totalKeyCount = standardKeyCount + SIDE_KEY_COUNT
    val gapCount = (totalKeyCount - 1).coerceAtLeast(0)
    return ((availableWidth -
        standardKeyWidth * standardKeyCount.toFloat() -
        keySpacing * gapCount.toFloat()) / SIDE_KEY_COUNT.toFloat()).coerceAtLeast(0.dp)
}

private const val STANDARD_KEY_COUNT = 10
private const val STANDARD_GAP_COUNT = STANDARD_KEY_COUNT - 1
private const val KEYBOARD_ROW_COUNT = 4
private const val KEYBOARD_ROW_GAP_COUNT = KEYBOARD_ROW_COUNT - 1
private const val REMAINING_FIELD_ROW_COUNT = 2
private const val SIDE_KEY_COUNT = 2
```

- [ ] **Step 1.4: Run metrics tests and full local checks**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.KeyboardLayoutMetricsTest"
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 1.5: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetrics.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutMetricsTest.kt
git commit -m "feat(keyboard): add layout metrics calculator"
```

---

## Stage 2: Minimal KeyboardFrame

**Goal:** Establish the frame boundary while preserving current utility row, utility panel, drag preview, bottom gap, and keyboard content behavior.

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardFrame.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRoot.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardBottomGap.kt`

- [ ] **Step 2.1: Extract bottom gap helper without changing values**

Modify `KeyboardBottomGap.kt`:

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

This preserves the existing values exactly.

- [ ] **Step 2.2: Add minimal frame wrapper**

Create `KeyboardFrame.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardBottomGap
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardNavigationBarSpacer
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardTestTags
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardAdaptiveMetrics

@Composable
internal fun KeyboardFrame(
    state: KeyboardUiState,
    adaptiveMetrics: KeyboardAdaptiveMetrics,
    isLandscape: Boolean,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .testTag(KeyboardTestTags.Root),
    ) {
        KeyboardContentArea(
            state = state,
            adaptiveMetrics = adaptiveMetrics,
            isLandscape = isLandscape,
            onIntent = onIntent,
            modifier = Modifier.weight(1f),
        )

        KeyboardBottomGap(isLandscape = isLandscape)
        KeyboardNavigationBarSpacer()
    }
}
```

- [ ] **Step 2.3: Delegate from `KeyboardRoot`**

In `KeyboardRoot.kt`, replace only the internal `Column` with:

```kotlin
KeyboardFrame(
    state = state,
    adaptiveMetrics = adaptiveMetrics,
    isLandscape = isLandscape,
    onIntent = onIntent,
)
```

Keep `KeyboardRoot` signature unchanged.

- [ ] **Step 2.4: Run checks**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 2.5: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardFrame.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRoot.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardBottomGap.kt
git commit -m "refactor(keyboard): introduce keyboard frame wrapper"
```

---

## Stage 3: Metrics Provider and Layout Local

**Goal:** Provide `KeyboardLayoutMetrics` from frame constraints without migrating row rendering yet.

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutLocals.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardFrame.kt`

- [ ] **Step 3.1: Add layout local**

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
            sideKeyStandardKeyCount = 7,
        ),
    )
}
```

- [ ] **Step 3.2: Calculate metrics from stable inputs**

Modify `KeyboardFrame.kt` to use `BoxWithConstraints`, `WindowInsets.navigationBars`, and `remember`. The metrics provider must not use `onGloballyPositioned`.

```kotlin
BoxWithConstraints(
    modifier = modifier
        .fillMaxWidth()
        .clipToBounds()
        .testTag(KeyboardTestTags.Root),
) {
    val density = LocalDensity.current
    val navigationSpacerHeight = with(density) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }
    val bottomSpacerHeight = keyboardBottomGapHeight(isLandscape)
    val metrics = remember(
        maxWidth,
        maxHeight,
        adaptiveMetrics.keyHeight,
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
                sideKeyStandardKeyCount = 7,
            ),
        )
    }

    CompositionLocalProvider(LocalKeyboardLayoutMetrics provides metrics) {
        Column(modifier = Modifier.fillMaxWidth()) {
            KeyboardContentArea(
                state = state,
                adaptiveMetrics = adaptiveMetrics,
                isLandscape = isLandscape,
                onIntent = onIntent,
                modifier = Modifier.weight(1f),
            )
            KeyboardBottomGap(isLandscape = isLandscape)
            KeyboardNavigationBarSpacer()
        }
    }
}
```

Required imports:

```kotlin
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keyboardBottomGapHeight
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics
```

- [ ] **Step 3.3: Run checks**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 3.4: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardLayoutLocals.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardFrame.kt
git commit -m "refactor(keyboard): provide layout metrics from frame"
```

---

## Stage 4: KeyboardRow and Safe Test Tags

**Goal:** Add row helper and value-free tags without changing layout semantics.

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardRow.kt`
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardTestTags.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/FieldKey.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/ActionKeys.kt`

- [ ] **Step 4.1: Add `KeyboardRow`**

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

- [ ] **Step 4.2: Add value-free test tags**

Create `KeyboardTestTags.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard

internal object KeyboardTestTags {
    const val Root = "keyboard-root"
    const val CandidateRow = "keyboard-candidate-row"
    const val DefaultContent = "keyboard-default-content"
    const val LetterReferenceRow = "keyboard-letter-reference-row"
    const val WidthPolicySample = "keyboard-width-policy-sample"
    const val EntryNormalContent = "keyboard-entry-normal-content"
    const val EntryFixedFields = "keyboard-entry-fixed-fields"
    const val EntryRemainingFields = "keyboard-entry-remaining-fields"
    const val EntryActions = "keyboard-entry-actions"
    const val EntryExpandedContent = "keyboard-entry-expanded-content"
    const val EntryExpandedFields = "keyboard-entry-expanded-fields"
    const val PreviousPage = "keyboard-previous-page"
    const val NextPage = "keyboard-next-page"

    fun field(fieldId: String): String = "keyboard-field-$fieldId"
}
```

- [ ] **Step 4.3: Tag fields by id only**

Modify `FieldKey.kt`:

```kotlin
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardTestTags
```

Then pass:

```kotlin
modifier = modifier.testTag(KeyboardTestTags.field(field.id)),
```

Do not change:

```kotlin
onClick = { onIntent(KeyboardIntent.CommitField(field.id)) }
```

- [ ] **Step 4.4: Tag previous and next page buttons**

Modify `ActionKeys.kt`:

```kotlin
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardTestTags
```

Update `PreviousPageKey`:

```kotlin
modifier = modifier.testTag(KeyboardTestTags.PreviousPage),
```

Update `NextPageKey`:

```kotlin
modifier = modifier.testTag(KeyboardTestTags.NextPage),
```

- [ ] **Step 4.5: Run checks**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 4.6: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardRow.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardTestTags.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/FieldKey.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/key/ActionKeys.kt
git commit -m "refactor(keyboard): add row helper and safe tags"
```

---

## Stage 5: Migrate Default Keyboard to Shared Metrics

**Goal:** Make letter, number, and symbol layouts use shared metrics and explicit width choices.

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/DefaultKeyboardLayout.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/LetterKeyboard.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/NumberKeyboard.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/SymbolKeyboard.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardContentArea.kt`

- [ ] **Step 5.1: Stop overriding default key height inside `KeyboardContentArea`**

Remove the old `resolveDefaultKeyHeight`, `defaultVisualRowCount`, and related constants from `KeyboardContentArea.kt`. Keep drag-preview bounds only if still needed for utility drag; drag-preview bounds must not affect metrics.

- [ ] **Step 5.2: Use metrics in `LetterKeyboard`**

Use:

```kotlin
val metrics = LocalKeyboardLayoutMetrics.current
val standardWidth = metrics.standardKeyWidth
val sideWidth = metrics.sideKeyWidth
```

Render first row with the reference tag:

```kotlin
KeyboardRow(modifier = Modifier.testTag(KeyboardTestTags.LetterReferenceRow)) {
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

Render the third row with explicit side widths:

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

- [ ] **Step 5.3: Use metrics in `NumberKeyboard` and `SymbolKeyboard`**

Replace local width calculations with `LocalKeyboardLayoutMetrics`. Standard keys use `Modifier.width(metrics.standardKeyWidth)`. Only explicitly matching edge keys use `metrics.sideKeyWidth`.

- [ ] **Step 5.4: Keep bottom action widths explicit**

In `DefaultKeyboardLayout.kt`, keep existing product content. Use `Modifier.weight(...)` only for keys that intentionally flex, such as space or select-entry. Use `Modifier.width(metrics.standardKeyWidth)` for standard-size keys. Do not infer width from action key type.

- [ ] **Step 5.5: Run checks**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 5.6: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/DefaultKeyboardLayout.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/LetterKeyboard.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/NumberKeyboard.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/SymbolKeyboard.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardContentArea.kt
git commit -m "refactor(keyboard): migrate default layout to metrics"
```

---

## Stage 6: Implement Normal Entry Continuous Scroll

**Goal:** Render normal entry as fixed fields, remaining fields scroll area, and actions; remove normal previous/next controls.

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldGrid.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/EntryActionRows.kt`

- [ ] **Step 6.1: Add field grid**

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

- [ ] **Step 6.2: Add reset key helper**

In `EntryKeyboardLayout.kt`, add:

```kotlin
private fun KeyboardUiState.entryScrollResetKey(): String {
    val fixedIds = fixedFields.joinToString(separator = "|") { it.id }
    val extraIds = extraFields.joinToString(separator = "|") { it.id }
    return listOf(
        mainLayout.name,
        entryFieldDisplayMode.name,
        hasActiveSession.toString(),
        currentEntryName.orEmpty(),
        fixedIds,
        extraIds,
    ).joinToString(separator = "::")
}
```

This key intentionally uses field ids and safe state only.

- [ ] **Step 6.3: Render normal entry**

Replace the normal entry path with:

```kotlin
@Composable
private fun NormalEntryContent(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalKeyboardLayoutMetrics.current
    val scrollState = rememberScrollState()
    val resetKey = state.entryScrollResetKey()

    LaunchedEffect(resetKey) {
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
            modifier = Modifier
                .height(metrics.keyboardRowHeight)
                .testTag(KeyboardTestTags.EntryFixedFields),
        )

        EntryFieldGrid(
            fields = state.extraFields,
            columns = ENTRY_FIELD_COLUMNS,
            onIntent = onIntent,
            modifier = Modifier
                .height(metrics.remainingFieldsAreaHeight)
                .verticalScroll(scrollState)
                .testTag(KeyboardTestTags.EntryRemainingFields),
        )

        NormalEntryActionRow(
            onIntent = onIntent,
            modifier = Modifier
                .height(metrics.keyboardRowHeight)
                .testTag(KeyboardTestTags.EntryActions),
        )
    }
}

private const val ENTRY_FIELD_COLUMNS = 3
```

Required imports include `height`, `verticalScroll`, `rememberScrollState`, `LaunchedEffect`, and `testTag`.

- [ ] **Step 6.4: Replace normal action row**

In `EntryActionRows.kt`, replace `PagedEntryActionRow` with:

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

- [ ] **Step 6.5: Run checks**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 6.6: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldGrid.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/EntryActionRows.kt
git commit -m "feat(keyboard): make normal entry fields scroll"
```

---

## Stage 7: Expanded Entry Paging Controls

**Goal:** Render expanded entry as one continuous list and wire previous/next to page math.

**Files:**
- Create: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPaging.kt`
- Create: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPagingTest.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt`
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/ExpandedEntryActionRows.kt`

- [ ] **Step 7.1: Write failing paging tests**

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
        val state = EntryFieldPageState(
            currentOffsetPx = 150f,
            maxScrollOffsetPx = 260f,
            visibleFieldListAreaHeightPx = 100f,
            contentHeightPx = 360f,
        )

        assertEquals(50f, state.previousTargetPx(), 0.001f)
        assertEquals(250f, state.nextTargetPx(), 0.001f)
    }

    @Test
    fun `controls disable when content fits one page`() {
        val state = EntryFieldPageState(
            currentOffsetPx = 0f,
            maxScrollOffsetPx = 0f,
            visibleFieldListAreaHeightPx = 200f,
            contentHeightPx = 180f,
        )

        assertFalse(state.previousEnabled)
        assertFalse(state.nextEnabled)
        assertEquals(0f, state.snapTargetPx(), 0.001f)
    }

    @Test
    fun `snap target uses nearest page and clamps`() {
        val state = EntryFieldPageState(
            currentOffsetPx = 151f,
            maxScrollOffsetPx = 260f,
            visibleFieldListAreaHeightPx = 100f,
            contentHeightPx = 360f,
        )

        assertEquals(200f, state.snapTargetPx(), 0.001f)
    }

    @Test
    fun `zero visible height disables page math`() {
        val state = EntryFieldPageState(
            currentOffsetPx = 100f,
            maxScrollOffsetPx = 200f,
            visibleFieldListAreaHeightPx = 0f,
            contentHeightPx = 300f,
        )

        assertFalse(state.previousEnabled)
        assertFalse(state.nextEnabled)
        assertEquals(0f, state.previousTargetPx(), 0.001f)
        assertEquals(0f, state.nextTargetPx(), 0.001f)
        assertEquals(0f, state.snapTargetPx(), 0.001f)
    }
}
```

- [ ] **Step 7.2: Run paging tests and verify failure**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.EntryFieldPagingTest"
```

Expected: FAIL because `EntryFieldPageState` does not exist.

- [ ] **Step 7.3: Implement paging helper**

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
        return (currentOffsetPx - visibleFieldListAreaHeightPx).coerceAtLeast(0f)
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

- [ ] **Step 7.4: Render expanded list with page controls**

In `EntryKeyboardLayout.kt`, expanded mode uses `state.fixedFields + state.extraFields` exactly once as the field source.

Use local visible-height state for scroll controls. This state must not feed metrics:

```kotlin
var visibleFieldListAreaHeightPx by remember { mutableFloatStateOf(0f) }
val pageState = EntryFieldPageState(
    currentOffsetPx = scrollState.value.toFloat(),
    maxScrollOffsetPx = scrollState.maxValue.toFloat(),
    visibleFieldListAreaHeightPx = visibleFieldListAreaHeightPx,
    contentHeightPx = scrollState.maxValue.toFloat() + visibleFieldListAreaHeightPx,
)
```

Render:

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
                .testTag(KeyboardTestTags.EntryExpandedFields),
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

- [ ] **Step 7.5: Reset and clamp expanded scroll**

Reset key:

```kotlin
val resetKey = state.entryScrollResetKey()

LaunchedEffect(resetKey) {
    scrollState.scrollTo(0)
}
```

Clamp key:

```kotlin
LaunchedEffect(
    scrollState.maxValue,
    visibleFieldListAreaHeightPx,
    state.fixedFields.map { it.id },
    state.extraFields.map { it.id },
    state.entryFieldDisplayMode,
) {
    if (scrollState.value > scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }
}
```

- [ ] **Step 7.6: Run checks**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.EntryFieldPagingTest"
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 7.7: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPaging.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPagingTest.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/ExpandedEntryActionRows.kt
git commit -m "feat(keyboard): page expanded entry fields"
```

---

## Stage 8: Drag-End Snap

**Goal:** Snap expanded entry list to the nearest page boundary after user drag ends without restarting effects on every offset change.

**Files:**
- Modify: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt`
- Modify: `app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPagingTest.kt`

- [ ] **Step 8.1: Add snap boundary test**

Add to `EntryFieldPagingTest.kt`:

```kotlin
@Test
fun `snap target clamps at bottom boundary`() {
    val state = EntryFieldPageState(
        currentOffsetPx = 255f,
        maxScrollOffsetPx = 260f,
        visibleFieldListAreaHeightPx = 100f,
        contentHeightPx = 360f,
    )

    assertEquals(260f, state.snapTargetPx(), 0.001f)
}
```

- [ ] **Step 8.2: Use snapshotFlow for scroll-end detection**

In expanded content, add guards:

```kotlin
var isResettingScroll by remember { mutableStateOf(false) }
var isProgrammaticScroll by remember { mutableStateOf(false) }
var latestPageState by remember { mutableStateOf(pageState) }
latestPageState = pageState
```

Use `snapshotFlow`:

```kotlin
LaunchedEffect(scrollState) {
    var wasScrolling = scrollState.isScrollInProgress
    snapshotFlow { scrollState.isScrollInProgress }
        .collect { isScrolling ->
            val endedUserScroll = wasScrolling &&
                !isScrolling &&
                !isResettingScroll &&
                !isProgrammaticScroll
            wasScrolling = isScrolling

            if (endedUserScroll) {
                val target = latestPageState.snapTargetPx().toInt()
                if (target != scrollState.value) {
                    isProgrammaticScroll = true
                    try {
                        scrollState.animateScrollTo(target)
                    } finally {
                        isProgrammaticScroll = false
                    }
                }
            }
        }
}
```

Required imports:

```kotlin
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
```

- [ ] **Step 8.3: Guard reset and programmatic page buttons**

Around resets:

```kotlin
LaunchedEffect(resetKey) {
    isResettingScroll = true
    try {
        scrollState.scrollTo(0)
    } finally {
        isResettingScroll = false
    }
}
```

Around previous/next animations:

```kotlin
isProgrammaticScroll = true
try {
    scrollState.animateScrollTo(pageState.nextTargetPx().toInt())
} finally {
    isProgrammaticScroll = false
}
```

- [ ] **Step 8.4: Run checks**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.EntryFieldPagingTest"
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 8.5: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryKeyboardLayout.kt app/src/test/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/EntryFieldPagingTest.kt
git commit -m "feat(keyboard): snap expanded fields to pages"
```

---

## Stage 9: Compose UI Regression Tests

**Goal:** Cover the design-critical layout behavior and sensitive-data boundary with instrumented Compose tests.

**Files:**
- Create: `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootTestFixtures.kt`
- Create: `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootDefaultLayoutTest.kt`
- Create: `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootEntryLayoutTest.kt`
- Create: `app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootSensitiveDataTest.kt`

- [ ] **Step 9.1: Add test fixtures**

Create `KeyboardRootTestFixtures.kt` with fake sensitive values in source data comments only if needed, never in UI labels:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.runtime.Composable
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldType
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel
import io.github.togls.kp2acomposekeyboard.feature.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.feature.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.ui.theme.KeyboardTheme

internal const val PASSWORD_SHOULD_NOT_APPEAR = "PASSWORD_SHOULD_NOT_APPEAR"
internal const val TOTP_SHOULD_NOT_APPEAR = "TOTP_SHOULD_NOT_APPEAR"
internal const val RECOVERY_CODE_SHOULD_NOT_APPEAR = "RECOVERY_CODE_SHOULD_NOT_APPEAR"

internal val forbiddenSensitiveValues = listOf(
    PASSWORD_SHOULD_NOT_APPEAR,
    TOTP_SHOULD_NOT_APPEAR,
    RECOVERY_CODE_SHOULD_NOT_APPEAR,
)

@Composable
internal fun KeyboardRootTestContent(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit = {},
) {
    val settings = KeyboardSettings()
    KeyboardTheme(settings = settings) {
        KeyboardRoot(
            state = state,
            settings = settings,
            onIntent = onIntent,
        )
    }
}

internal fun testDefaultState() = KeyboardUiState()

internal fun testEntryState(
    displayMode: EntryFieldDisplayMode,
    extraFieldCount: Int = 8,
) = KeyboardUiState(
    mainLayout = MainKeyboardLayout.Entry,
    entryFieldDisplayMode = displayMode,
    currentEntryName = "Example",
    hasActiveSession = true,
    fixedFields = fixedFields(),
    extraFields = extraFields(extraFieldCount),
    allFields = fixedFields() + extraFields(extraFieldCount),
)

private fun fixedFields() = listOf(
    KeyboardFieldUiModel("username", "Username", KeyboardFieldType.Username, sensitive = false),
    KeyboardFieldUiModel("password", "Password", KeyboardFieldType.Password, sensitive = true),
    KeyboardFieldUiModel("totp", "TOTP", KeyboardFieldType.Totp, sensitive = true),
)

private fun extraFields(count: Int) = List(count) { index ->
    KeyboardFieldUiModel(
        id = "extra-$index",
        label = "Extra $index",
        type = KeyboardFieldType.Custom,
        sensitive = index % 2 == 0,
    )
}
```

- [ ] **Step 9.2: Add default keyboard tests**

Create `KeyboardRootDefaultLayoutTest.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class KeyboardRootDefaultLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersDefaultKeyboard() {
        composeRule.setContent {
            KeyboardRootTestContent(state = testDefaultState())
        }

        composeRule.onNodeWithTag(KeyboardTestTags.Root).assertIsDisplayed()
        composeRule.onNodeWithText("q").assertIsDisplayed()
        composeRule.onNodeWithText("p").assertIsDisplayed()
    }

    @Test
    fun tenKeyReferenceRowUsesConsistentWidths() {
        composeRule.setContent {
            KeyboardRootTestContent(state = testDefaultState())
        }

        val widths = "qwertyuiop".map { key ->
            composeRule.onNodeWithText(key.toString())
                .getUnclippedBoundsInRoot()
                .width
        }
        val firstWidth = widths.first()

        widths.forEach { width ->
            assertEquals(firstWidth.value, width.value, 0.5f)
        }
    }
}
```

- [ ] **Step 9.3: Add entry layout tests**

Create `KeyboardRootEntryLayoutTest.kt` with tests for normal structure, no previous/next, expanded controls, disabled content-fits controls, and scroll. Use `performTouchInput { swipeUp() }` for scrollable areas.

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import io.github.togls.kp2acomposekeyboard.feature.keyboard.EntryFieldDisplayMode
import org.junit.Rule
import org.junit.Test

class KeyboardRootEntryLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun normalEntryHasNoPageControls() {
        composeRule.setContent {
            KeyboardRootTestContent(testEntryState(EntryFieldDisplayMode.Paged))
        }

        composeRule.onNodeWithTag(KeyboardTestTags.EntryNormalContent).assertIsDisplayed()
        composeRule.onAllNodes(hasTestTag(KeyboardTestTags.PreviousPage)).assertCountEquals(0)
        composeRule.onAllNodes(hasTestTag(KeyboardTestTags.NextPage)).assertCountEquals(0)
    }

    @Test
    fun normalRemainingFieldsScrollVertically() {
        composeRule.setContent {
            KeyboardRootTestContent(testEntryState(EntryFieldDisplayMode.Paged, extraFieldCount = 12))
        }

        composeRule.onNodeWithTag(KeyboardTestTags.EntryRemainingFields)
            .performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Extra 11").assertIsDisplayed()
    }

    @Test
    fun expandedEntryShowsPageControls() {
        composeRule.setContent {
            KeyboardRootTestContent(testEntryState(EntryFieldDisplayMode.Expanded))
        }

        composeRule.onNodeWithTag(KeyboardTestTags.EntryExpandedContent).assertIsDisplayed()
        composeRule.onNodeWithTag(KeyboardTestTags.PreviousPage).assertIsDisplayed()
        composeRule.onNodeWithTag(KeyboardTestTags.NextPage).assertIsDisplayed()
    }

    @Test
    fun expandedControlsDisableWhenContentFitsOnePage() {
        composeRule.setContent {
            KeyboardRootTestContent(testEntryState(EntryFieldDisplayMode.Expanded, extraFieldCount = 0))
        }

        composeRule.onNodeWithTag(KeyboardTestTags.PreviousPage).assertIsNotEnabled()
        composeRule.onNodeWithTag(KeyboardTestTags.NextPage).assertIsNotEnabled()
    }
}
```

- [ ] **Step 9.4: Add sensitive-data tests**

Create `KeyboardRootSensitiveDataTest.kt`:

```kotlin
package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithTag
import io.github.togls.kp2acomposekeyboard.feature.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class KeyboardRootSensitiveDataTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sensitiveValuesDoNotAppearInTextContentDescriptionsOrSemanticsDump() {
        composeRule.setContent {
            KeyboardRootTestContent(testEntryState(EntryFieldDisplayMode.Paged))
        }

        val semanticsDump = composeRule.onRoot(useUnmergedTree = true).printToString()

        forbiddenSensitiveValues.forEach { forbiddenValue ->
            composeRule.onAllNodes(hasText(forbiddenValue, substring = true)).assertCountEquals(0)
            composeRule.onAllNodes(hasContentDescription(forbiddenValue, substring = true))
                .assertCountEquals(0)
            assertFalse(semanticsDump.contains(forbiddenValue))
        }
    }

    @Test
    fun fieldClickSendsFieldIdOnly() {
        val intents = mutableListOf<KeyboardIntent>()
        composeRule.setContent {
            KeyboardRootTestContent(
                state = testEntryState(EntryFieldDisplayMode.Paged),
                onIntent = intents::add,
            )
        }

        composeRule.onNodeWithTag(KeyboardTestTags.field("password")).performClick()

        assertTrue(intents.contains(KeyboardIntent.CommitField("password")))
        assertFalse(intents.contains(KeyboardIntent.CommitText(PASSWORD_SHOULD_NOT_APPEAR)))
    }
}
```

- [ ] **Step 9.5: Run instrumented tests**

Run:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected: PASS with an attached Android device or emulator. If no device exists, capture the exact no-device output in the implementation report.

- [ ] **Step 9.6: Run local checks**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 9.7: Commit**

```bash
git add app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootTestFixtures.kt app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootDefaultLayoutTest.kt app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootEntryLayoutTest.kt app/src/androidTest/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/KeyboardRootSensitiveDataTest.kt
git commit -m "test(keyboard): cover keyboard root layout behavior"
```

---

## Stage 10: Add Previews

**Goal:** Add preview coverage without runtime behavior changes.

**Files:**
- Modify: `app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/KeyboardPreviewFixtures.kt`
- Modify: `app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/EntryKeyboardPreviews.kt`
- Modify: `app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/LandscapeKeyboardPreviews.kt`

- [ ] **Step 10.1: Add safe preview fixtures**

Add to `KeyboardPreviewFixtures.kt`:

```kotlin
internal fun previewLongLabelEntryKeyboardState(): KeyboardUiState {
    val fixedFields = listOf(
        KeyboardFieldUiModel("username", "Username", KeyboardFieldType.Username, sensitive = false),
        KeyboardFieldUiModel("password", "Password", KeyboardFieldType.Password, sensitive = true),
    )
    val extraFields = listOf(
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
        KeyboardFieldUiModel(
            id = "duplicate-label",
            label = "Username",
            type = KeyboardFieldType.Custom,
            sensitive = false,
        ),
    )
    return KeyboardUiState(
        mainLayout = MainKeyboardLayout.Entry,
        entryFieldDisplayMode = EntryFieldDisplayMode.Paged,
        currentEntryName = "Preview",
        hasActiveSession = true,
        fixedFields = fixedFields,
        extraFields = extraFields,
        allFields = fixedFields + extraFields,
    )
}
```

- [ ] **Step 10.2: Add normal and expanded long-label previews**

Add to `EntryKeyboardPreviews.kt`:

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

@Preview(
    name = "Expanded - Long Labels",
    group = "Keyboard / Entry",
    showBackground = true,
    widthDp = 411,
    heightDp = 360,
)
@Composable
private fun EntryKeyboardExpandedLongLabelsPreview() {
    KeyboardPreviewContent(
        state = previewLongLabelEntryKeyboardState().copy(
            entryFieldDisplayMode = EntryFieldDisplayMode.Expanded,
        ),
        settings = previewTallLightSettings(),
    )
}
```

- [ ] **Step 10.3: Rename preview labels from Paged to Normal**

In `EntryKeyboardPreviews.kt`, rename preview display names from `"Paged"` to `"Normal"` where the UI now renders normal continuous-scroll entry mode. Do not rename `EntryFieldDisplayMode.Paged` yet unless a separate model cleanup is planned.

- [ ] **Step 10.4: Run preview build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 10.5: Commit**

```bash
git add app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/KeyboardPreviewFixtures.kt app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/EntryKeyboardPreviews.kt app/src/debug/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/preview/LandscapeKeyboardPreviews.kt
git commit -m "test(keyboard): add layout refactor previews"
```

---

## Stage 11: Cleanup Old Components

**Goal:** Delete unused old layout components only after new paths are covered.

**Files:**
- Candidate delete: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardWidthLayout.kt`
- Candidate delete: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/TextKeyRow.kt`
- Candidate delete: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/LetterRow.kt`
- Candidate delete: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/FixedTextKeyRow.kt`
- Candidate delete: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/row/FixedFieldRow.kt`
- Candidate delete: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/ExtraFieldPagedPanel.kt`
- Candidate delete: `app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/entry/AllFieldsExpandedPanel.kt`

- [ ] **Step 11.1: Confirm references before deleting**

Run:

```bash
rg -n "KeyboardWidthLayout|TextKeyRow|LetterRow|FixedTextKeyRow|FixedFieldRow|ExtraFieldPagedPanel|AllFieldsExpandedPanel|PagedEntryActionRow" app/src
```

Expected: only definitions in candidate files remain. If any active reference remains, do not delete that file.

- [ ] **Step 11.2: Delete files with no references**

Use `apply_patch` delete hunks. Example:

```diff
*** Begin Patch
*** Delete File: app/src/main/kotlin/io/github/togls/kp2acomposekeyboard/ui/keyboard/layout/KeyboardWidthLayout.kt
*** End Patch
```

Repeat only for files confirmed safe by Step 11.1.

- [ ] **Step 11.3: Run final validation**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

Expected: unit tests and assemble pass. `connectedDebugAndroidTest` passes when a device or emulator is attached. If no device exists, record the exact no-device result.

- [ ] **Step 11.4: Commit**

```bash
git add app/src/main/kotlin/io/github/togls/kp2acomposekeyboard
git commit -m "refactor(keyboard): remove migrated layout components"
```

---

## Final Verification Checklist

- [ ] `KeyboardRoot` public signature is unchanged.
- [ ] No full layout DSL or key-spec renderer was introduced.
- [ ] No `minKeyHeight` fallback was introduced.
- [ ] Bottom gap visual value is unchanged.
- [ ] Metrics formulas are centralized in `KeyboardLayoutMetrics.kt`.
- [ ] Metrics calculation does not write Compose state.
- [ ] Default keyboard uses shared metrics.
- [ ] 10-key reference row has consistent key widths.
- [ ] Width policy sample can express standard, side, and flexible keys.
- [ ] Normal entry mode has no previous or next page controls.
- [ ] Normal entry remaining fields scroll vertically.
- [ ] Expanded entry fields render as one continuous list.
- [ ] Expanded entry previous and next controls page by visible field-list height.
- [ ] Expanded drag-end snap uses `snapshotFlow` and guards reset/programmatic scroll.
- [ ] Field commit remains `KeyboardIntent.CommitField(fieldId)`.
- [ ] Field values do not appear in UI state, Composable params, semantics, test tags, logs, tests, screenshots, or docs.
- [ ] `./gradlew :app:testDebugUnitTest` passes.
- [ ] `./gradlew :app:assembleDebug` passes.
- [ ] `./gradlew :app:connectedDebugAndroidTest` passes or no-device failure is reported.
