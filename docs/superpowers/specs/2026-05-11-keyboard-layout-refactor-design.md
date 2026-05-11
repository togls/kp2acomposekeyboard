# Keyboard Layout Refactor Design

## Status

Approved for design documentation.

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
- Keep the utility or candidate row height independent from the four-row
  keyboard area.
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

- `KeyboardFrame`: hosts the utility or candidate row, keyboard content area,
  bottom spacer, and navigation spacer.
- `KeyboardLayoutMetrics`: calculates shared row height and key widths from the
  actual available constraints.
- `KeyboardRow`: applies common row width, spacing, alignment, and clipping.
- `DefaultKeyboardContent`: renders letter, number, and symbol modes.
- `EntryKeyboardContent`: renders normal and expanded entry modes.
- Key composables: keep visual state, semantics, and click behavior close to the
  existing key components.

The exact names can change during implementation if a clearer local convention
emerges.

## Frame Model

The root frame is conceptually:

```text
[utility / candidate row]
[keyboard area]
[bottom spacer]
[navigation spacer]
```

The utility or candidate row has independent height. It does not count as one of
the four keyboard rows.

The keyboard area gets the remaining height after excluding:

- utility or candidate row height
- vertical outer padding
- row spacing
- bottom spacer
- navigation bar spacer

The keyboard area key height is calculated by dividing its available height by
four rows. Every key in default and entry keyboard content uses that resolved
height.

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

`sideKeyWidth` is not a generic action-key width. Action keys still default to
`standardKeyWidth`. `sideKeyWidth` is only for rows that explicitly want matching
left and right edge keys.

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

Width rules:

- Field buttons use `fieldKeyWidth(columns)`.
- The current field column count is 3.
- The structure should allow changing the column count to 4 later without
  rewriting row measurement.
- Non-field action buttons default to `standardKeyWidth` unless a row explicitly
  requests flexible or side widths.

Normal entry mode removes previous and next page buttons.

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
- Previous and next page buttons scroll by one page.
- Vertical drag can move the list continuously while the gesture is active, then
  should settle near a page boundary when practical.
- The underlying content remains one continuous scroll area.

Field buttons still use the configured field column count.

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

## Comment Rules

Code comments must be written in English.

Add comments only when they explain non-obvious behavior, for example:

- why utility/candidate row, bottom spacer, and navigation spacer are excluded
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
- Expanded entry mode puts fixed fields first inside one list.
- Expanded entry mode keeps previous and next page controls.
- Field values are not displayed.

The implementation may add test tags or semantics for layout areas and safe
field labels. Test tags must not include sensitive values.

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

No unresolved product decisions are intentionally left in this design.
