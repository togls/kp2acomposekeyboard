# Keyboard Layout Refactor Design

## Status

Approved for staged implementation planning after this revision is reviewed.

## Context

KP2A Compose Keyboard currently has separate Compose components for the root
IME frame, default keyboard layouts, entry layouts, rows, keys, utility row, and
navigation spacing. The current structure already centralizes part of the width
calculation in `KeyboardWidthLayout`, but row height, entry field layout, and
some width policies are still spread across multiple files.

The refactor should keep the external `KeyboardRoot` entry point stable while
allowing the internal keyboard UI structure to be reorganized. The goal is to
make keyboard sizing rules explicit, easy to test, and easy to adjust without
introducing a full layout DSL.

This project is primarily built for personal devices and personal needs. It
does not guarantee compatibility with all Android, HyperOS, MIUI, or vendor ROM
versions.

## Goals

- Keep `KeyboardRoot` as the stable external entry point for IME rendering.
- Allow all keyboard UI internals below `KeyboardRoot` to be refactored.
- Centralize keyboard width and height calculation in a lightweight measurement
  layer.
- Keep Compose implementation direct and readable instead of introducing a full
  layout specification DSL.
- Use a four-row keyboard area height model for default and entry layouts.
- Keep the candidate row height independent from the four-row keyboard area.
  The current utility row is treated as the candidate row for this refactor.
- Keep the bottom spacer and navigation bar spacer outside keyboard row height
  calculation.
- Add Compose UI tests for the new layout behavior.
- Preserve the sensitive-data boundary: field values must not appear in UI
  state, displayed text, logs, tests, screenshots, or documentation.

## Non-Goals

- Do not add a general-purpose keyboard layout DSL.
- Do not add pinyin, suggestions, autocorrect, candidate words, or multilanguage
  keyboard layouts.
- Do not change Keepass2Android integration behavior.
- Do not change `KeyboardRoot` callers.
- Do not persist keyboard layout measurements.
- Do not store or expose Keepass2Android field values in UI-facing models.

## Chosen Approach

Use a lightweight measurement layer plus direct Compose rendering.

This approach keeps the sizing policy centralized while preserving simple
Composable code. It avoids a full DSL because the current product scope does not
require dynamic user-defined layouts, multilanguage layout packs, or runtime
layout generation.

The alternative full-DSL approach would make layout declarations shorter, but it
would also require a second mapping layer from layout specs to composables,
intents, enabled states, field models, and security-sensitive behavior. That is
too much abstraction for the current keyboard scope.

## P0 Execution Constraints

Implementation must be staged. Do not implement this as one large keyboard UI
rewrite.

Required stages:

1. Add `KeyboardLayoutMetrics` and pure measurement formulas.
2. Migrate the default keyboard to the shared metrics.
3. Implement normal entry continuous scrolling.
4. Implement expanded entry continuous list and page controls.
5. Implement drag-end page snapping after expanded paging is stable.
6. Add Compose UI tests, previews where useful, and then clean up old components.

Each stage must compile independently and be reviewable on its own. Old
components should not be deleted in the same patch that changes behavior.

Do not introduce a `minKeyHeight` visual fallback. The only lower bound in the
measurement formulas is `coerceAtLeast(0.dp)` to prevent invalid negative sizes
from reaching Compose layout modifiers.

## Architecture

`KeyboardRoot` remains the public entry point.

Internally, the refactor may replace, merge, rename, or remove existing keyboard
layout components, including:

- `KeyboardContentArea`
- `KeyboardWidthLayout`
- `LetterRow`
- `TextKeyRow`
- `FixedTextKeyRow`
- `FixedFieldRow`
- entry panel components
- entry action row components

The new internal structure should be organized around these responsibilities:

- `KeyboardFrame`: hosts the candidate row, keyboard content area, bottom
  spacer, and navigation spacer.
- `KeyboardLayoutMetrics`: calculates shared row height and key widths from the
  actual available constraints.
- `KeyboardRow`: applies common row width, spacing, alignment, and clipping.
- `DefaultKeyboardContent`: renders letter, number, and symbol modes.
- `EntryKeyboardContent`: renders normal and expanded entry modes.
- Key composables: keep visual state, semantics, and click behavior close to the
  existing key components.

The exact names can change during implementation if a clearer local convention
emerges.

## Metrics Contract

`KeyboardLayoutMetrics` must be a read-only result derived from constraints,
density, tokens, and known spacer heights. ViewModels must not calculate layout
sizes.

The implementation should use this shape or an equivalent one:

```kotlin
data class KeyboardLayoutInput(
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

data class KeyboardLayoutMetrics(
    val standardKeyWidth: Dp,
    val sideKeyWidth: Dp,
    val keyboardRowHeight: Dp,
    val remainingFieldsAreaHeight: Dp,
) {
    fun fieldKeyWidth(columns: Int): Dp
}
```

`columns` must be at least 1. The implementation must either throw a clear
argument error for invalid columns or coerce the value in one centralized place.

All width and height formulas must live in the metrics layer. Composables may
consume metrics but must not duplicate the measurement formulas.

### Metrics Recomposition Rules

Metrics calculation must not write Compose state.

Avoid this pattern when the state can affect the same layout:

```kotlin
onGloballyPositioned {
    state = calculateKeyboardLayoutMetrics(...)
}
```

Prefer calculating from constraints and stable inputs:

```kotlin
val metrics = remember(
    constraints.maxWidth,
    constraints.maxHeight,
    density,
    tokens,
    bottomSpacerHeight,
    navigationSpacerHeight,
) {
    calculateKeyboardLayoutMetrics(...)
}
```

or an equivalent `derivedStateOf` based on the same stable inputs.

Only constraints, density, tokens, and spacer heights should trigger metrics
recalculation. The metrics path must avoid measurement -> state write ->
recomposition loops.

## Frame Model

The root frame is conceptually:

```text
[candidate row]
[keyboard area]
[bottom spacer]
[navigation spacer]
```

The candidate row has independent height. It does not count as one of
the four keyboard rows.

The keyboard area gets the remaining height after excluding:

- candidate row height
- vertical outer padding
- bottom spacer
- navigation bar spacer

The required formula is:

```kotlin
keyboardAreaHeight =
    totalHeight -
        candidateRowHeight -
        verticalOuterPadding * 2 -
        bottomSpacerHeight -
        navigationSpacerHeight

keyboardRowHeight =
    ((keyboardAreaHeight - rowSpacing * 3) / 4).coerceAtLeast(0.dp)
```

`rowSpacing * 3` is removed once because the keyboard area has four rows. The
bottom spacer and navigation spacer are not mixed into `keyboardRowHeight`.
Every key in default and entry keyboard content uses this resolved height.

Normal entry remaining fields use:

```kotlin
remainingFieldsAreaHeight = keyboardRowHeight * 2 + rowSpacing
```

Necessary code comments should be written in English and explain why this
separation exists. In particular, comments should document that IME navigation
insets differ across Android and vendor ROM versions, so bottom spacer and
navigation spacer must not be mixed into keyboard row height.

## Width Policy

The default width policy is:

- Every key uses `standardKeyWidth` unless a row explicitly overrides it.
- `standardKeyWidth` is calculated from the available width using a 10-key
  reference row, such as `qwertyuiop`.
- Rows with fewer standard keys are centered by default.
- Field keys use field-specific column width, not `standardKeyWidth`.
- Flexible keys, such as space, fill remaining row width only when explicitly
  requested.

The measurement layer should expose at least:

- `standardKeyWidth`
- `sideKeyWidth`
- `fieldKeyWidth(columns)`
- `keyboardRowHeight`
- spacing and padding values from existing keyboard tokens

The required width formulas are:

```kotlin
availableWidth =
    (totalWidth - horizontalPadding * 2).coerceAtLeast(0.dp)

standardKeyWidth =
    ((availableWidth - keySpacing * 9) / 10).coerceAtLeast(0.dp)

fieldKeyWidth(columns) =
    ((availableWidth - keySpacing * (columns - 1)) / columns)
        .coerceAtLeast(0.dp)
```

`sideKeyWidth` is not a generic action-key width. Action keys still default to
`standardKeyWidth`. `sideKeyWidth` is only for rows that explicitly want matching
left and right edge keys.

Rows must not infer key widths from key type. `space` and other long keys must
explicitly request flexible width.

### Width Policy Acceptance Sample

This sample is a validation example for the width policy, not a required change
to the current keyboard content:

```text
[shift] [z] [x] [c] [v] [b] [n] [m] [delete]
[switch numbers] [,] [space] [.] [enter]
```

Expected sizing:

- `zxcvbnm`, `,`, and `.` use `standardKeyWidth`.
- `shift`, `delete`, `switch numbers`, and `enter` share `sideKeyWidth`.
- `space` is flexible and fills the remaining width.
- The row code should not need a full layout DSL to express this.

## Default Keyboard Layout

The default keyboard still supports:

- letter mode
- number mode
- symbol mode

The existing key content can remain unless implementation discovers a simpler
equivalent structure.

Sizing rules:

- The default keyboard area uses four rows.
- Letter, number, symbol, and action keys use `standardKeyWidth` by default.
- Rows can explicitly use `sideKeyWidth` for matching edge keys.
- Rows can explicitly use flexible width for long keys such as space.
- A row helper should avoid repeating `Row(fillMaxWidth, spacedBy(...))`.

The layout should stay readable Compose code. The desired shape is direct
composition with centralized measurement, not a separate key-spec renderer.

## Entry Keyboard Layout

Entry normal mode uses this area model:

```text
[fixed fields]
[remaining fields scroll area]
[actions]
```

Height rules:

- Entry keyboard area uses the same four-row key height as the default keyboard.
- Fixed fields occupy one key row.
- Actions occupy one key row.
- Remaining fields occupy the leftover height, equivalent to two key rows plus
  the internal row spacing.
- Remaining fields are shown as a continuous scrollable list, not paged.
- Scroll edges may clip partial field keys when content does not align exactly
  with the visible area.
- Remaining fields are accessed only by vertical drag scrolling.

Width rules:

- Field buttons use `fieldKeyWidth(columns)`.
- The current field column count is 3.
- The structure should allow changing the column count to 4 later without
  rewriting row measurement.
- Non-field action buttons default to `standardKeyWidth` unless a row explicitly
  requests flexible or side widths.

Normal entry mode removes previous and next page buttons.
If field count is smaller than the visible area, the remaining field area does
not force filler rows.

## Entry Expanded Mode

Expanded mode displays all fields in one continuous list.

Ordering:

- Fixed fields appear first.
- Remaining fields fill immediately after fixed fields.
- If fewer fixed fields exist, remaining fields fill the visible row naturally.
- The UI should look like a single list, not separate fixed and remaining
  sections.

Scrolling:

- Expanded mode keeps previous and next page buttons.
- One page equals the visible field-list area height.
- Previous and next page buttons scroll by one page and clamp to valid bounds.
- Vertical drag can move the list continuously while the gesture is active.
- Drag end snaps to the nearest page boundary when content is taller than one
  page.
- Snap does not run when content fits inside one visible page.
- The underlying content remains one continuous scroll area.

Field buttons still use the configured field column count.

Required paging formulas:

```kotlin
pageSizePx = visibleFieldListAreaHeightPx

previousTarget =
    (currentOffsetPx - pageSizePx).coerceAtLeast(0f)

nextTarget =
    (currentOffsetPx + pageSizePx).coerceAtMost(maxScrollOffsetPx)

targetPage =
    round(currentOffsetPx / pageSizePx)

targetOffsetPx =
    (targetPage * pageSizePx).coerceIn(0f, maxScrollOffsetPx)

previousEnabled = currentOffsetPx > 0f
nextEnabled = currentOffsetPx < maxScrollOffsetPx
```

If `contentHeightPx <= visibleFieldListAreaHeightPx`, both controls are disabled
and snap is skipped.

If `visibleFieldListAreaHeightPx <= 0f`, both controls are disabled and page
math is skipped.

## Scroll State Rules

Scroll state must reset when:

- the active entry changes
- the session is cleared or expires
- normal and expanded modes switch
- the UI returns from entry layout to default layout

Scroll state must clamp to the valid range when:

- field list size changes
- visible field-list area height changes
- page size changes
- orientation changes
- Compose view is rebuilt

The implementation may use `ScrollState`, `LazyListState`, `LazyGridState`, or a
small custom state holder. Regardless of API, the effective offset must satisfy:

```kotlin
currentOffsetPx = currentOffsetPx.coerceIn(0f, maxScrollOffsetPx)
```

## Sensitive Data Rules

The refactor must preserve the existing security model.

Allowed to hold field values:

- `KeyboardSessionRepository`
- `KeyboardSession`
- `KeyboardField.value`

Not allowed to hold field values:

- `KeyboardUiState`
- `KeyboardFieldUiModel`
- Compose UI
- settings
- DataStore
- logs
- tests
- docs

Compose UI tests must assert labels, layout behavior, and interaction effects
without embedding real passwords, tokens, TOTP values, recovery codes, or raw
Keepass2Android output.

The field commit intent must stay id-based:

```kotlin
KeyboardIntent.CommitField(fieldId)
```

Do not replace it with value-based UI intents.

Forbidden examples:

```kotlin
Text(field.value)
Modifier.testTag(field.value)
contentDescription = field.value
KeyboardFieldUiModel(value = field.value)
```

Allowed examples:

```kotlin
Modifier.testTag("field-${field.id}")
Text(field.safeLabel)
contentDescription = field.safeLabel
KeyboardIntent.CommitField(field.id)
```

## Comment Rules

Code comments must be written in English.

Add comments only when they explain non-obvious behavior, for example:

- why candidate row, bottom spacer, and navigation spacer are excluded
  from four-row key height calculation
- why IME navigation insets are handled defensively for vendor ROM differences
- why entry scroll areas allow partially clipped field keys at the edges
- why UI tests assert safe labels and not sensitive field values

Do not add comments that restate simple Compose layout code.

## Compose UI Testing

Add Compose UI test support.

Build configuration should add Compose UI test dependencies through the existing
Compose BOM:

- `androidTestImplementation(platform(libs.androidx.compose.bom))`
- `androidTestImplementation(libs.androidx.compose.ui.test.junit4)`
- `debugImplementation(libs.androidx.compose.ui.test.manifest)`

The version catalog should add aliases for:

- `androidx.compose.ui:ui-test-junit4`
- `androidx.compose.ui:ui-test-manifest`

Add `app/src/androidTest/...` tests using `createComposeRule()` or
`createAndroidComposeRule<ComponentActivity>()`, depending on whether the test
needs activity resources.

If metrics are implemented as a pure function, add JVM unit tests for
`calculateKeyboardLayoutMetrics(input)` where practical. Compose UI tests remain
the required layout-behavior guard.

Test coverage should include:

- `KeyboardRoot` renders default keyboard content.
- `KeyboardRoot` renders normal entry content.
- `KeyboardRoot` renders expanded entry content.
- Standard keys in a 10-key reference row have consistent width.
- The width policy acceptance sample can express standard keys, matching side
  keys, and a flexible space key.
- Normal entry mode shows fixed fields, remaining fields, and actions in the
  expected structure.
- Normal entry mode does not expose previous or next page buttons.
- Normal entry remaining fields can scroll when content exceeds the visible
  area.
- Expanded entry mode puts fixed fields first inside one list.
- Expanded entry mode keeps previous and next page controls.
- Expanded entry previous and next controls scroll by one page.
- Expanded entry previous and next controls are disabled at the top, bottom, and
  when content fits in one page.
- Field values are not displayed.
- Field values are not exposed through semantics or test tags.

The implementation may add test tags or semantics for layout areas and safe
field labels. Test tags must not include sensitive values.

Sensitive-data regression tests must use obvious fake values, such as:

```text
PASSWORD_SHOULD_NOT_APPEAR
TOTP_SHOULD_NOT_APPEAR
RECOVERY_CODE_SHOULD_NOT_APPEAR
```

and assert that they do not appear in UI text, semantics, or test tags.

## Implementation Stages

### Stage 1: Metrics

Add the metrics input model, output model, and pure calculation path. This stage
must not change existing keyboard behavior.

Validation:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

### Stage 2: Default Keyboard

Migrate letter, number, and symbol layouts to shared metrics and the lightweight
row helper. Keep existing key visuals, semantics, and click behavior.

Validation must confirm:

- letter, number, and symbol modes render
- 10-key reference rows use consistent widths
- rows with fewer keys remain centered
- action keys default to `standardKeyWidth`
- flexible width is explicit

### Stage 3: Normal Entry

Implement normal entry as fixed fields, continuous remaining-field scroll area,
and actions. Remove normal-mode previous and next controls.

Validation must confirm:

- fixed fields occupy one row
- actions occupy one row
- remaining fields occupy the two-row scroll area
- remaining fields can scroll when content exceeds the visible area
- field values are not displayed or exposed

### Stage 4: Expanded Entry Paging

Implement the continuous expanded field list with previous and next controls
that scroll one visible field-list page.

Validation must confirm:

- fixed fields appear first in the same list
- remaining fields immediately follow fixed fields
- previous and next targets clamp
- controls are disabled at top, bottom, and when content fits in one page

### Stage 5: Drag-End Snap

Add drag-end snapping after expanded paging is stable.

Validation must confirm:

- drag end snaps to the nearest page boundary
- snap does not run for content that fits in one page
- orientation or content changes clamp the offset

### Stage 6: Tests, Previews, Cleanup

Add Compose UI tests, add useful previews, and delete old components only after
the new paths are covered. Deleting old components should be a separate cleanup
change from behavior migration.

## Boundary Conditions

Implementation must handle these cases without crashing:

- very small available height
- landscape orientation
- split-screen or otherwise constrained IME height
- candidate row height changes
- navigation spacer height is zero
- bottom spacer height is zero
- row spacing is larger than the available keyboard area
- field column count changes from 3 to 4
- fixed fields are empty, fewer than one row, or more than one row capacity
- remaining fields are empty, less than one screen, exactly one screen, or more
  than one screen
- field labels are long, duplicated, or blank
- active session is missing, replaced, cleared, or expired
- default, entry normal, and entry expanded modes switch repeatedly
- Compose view is rebuilt

All final dimensions passed to Compose layout modifiers must be non-negative.
No field value may enter UI state, Composable parameters, semantics, test tags,
content descriptions, logs, tests, screenshots, or docs.

## Validation

Implementation should run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

`connectedDebugAndroidTest` requires an attached Android device or emulator. If
no device is available in the implementation environment, the final
implementation report must state that the command could not be run and include
the reason.

## Open Decisions

No product decisions are intentionally left open in this design.
