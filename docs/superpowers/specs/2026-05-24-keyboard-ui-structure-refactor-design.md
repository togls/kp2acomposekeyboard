# Keyboard UI Structure Refactor Design

## Status

Approved for design documentation. Awaiting user review before implementation
planning.

## Context

The keyboard UI under `ui/keyboard` has grown around technical buckets such as
`layout`, `key`, `row`, `style`, and `utility`. Those buckets no longer describe
the actual responsibilities clearly enough:

- `layout` contains the root content switch, frame metrics consumers, text input
  layouts, entry layouts, field grid, row helpers, and paging math.
- `row` contains entry-specific action rows, not generic rows.
- `style` contains layout metrics, orientation, and height policies in addition
  to visual styling concerns.
- `utility` represents a keyboard quick-action system, not generic utilities.
- `DefaultKeyboardLayout` and `DefaultInputMode` are misleading because the
  entry keyboard is the primary/default workflow.

This refactor should improve package boundaries and naming while preserving
runtime behavior.

This project is primarily built for personal devices and personal needs. It does
not guarantee compatibility with all Android, HyperOS, MIUI, or vendor ROM
versions.

## Goals

- Reorganize `ui/keyboard` around product responsibilities rather than generic
  technical buckets.
- Rename misleading keyboard concepts:
  - `Default*` text input concepts become `TextInput*`.
  - `Utility*` quick-action concepts become `QuickAction*`.
  - `entrylayout` becomes `entry`.
- Allow the public Compose keyboard entry point to change from `KeyboardRoot` to
  a clearer name.
- Keep all keyboard UI behavior unchanged unless the user explicitly approves a
  behavior change during implementation.
- Preserve the sensitive-data boundary: Keepass2Android field values must not
  enter UI-facing state, Compose parameters, semantics, test tags, logs,
  DataStore, documentation, screenshots, or tests.
- Update debug previews, JVM tests, and Compose UI tests to match the new names.

## Non-Goals

- Do not change keyboard visual design, spacing, height formulas, or navigation
  inset behavior.
- Do not change key text, icons, content descriptions, or click behavior.
- Do not change entry field paging, snapping, scroll reset, or scroll clamping
  behavior.
- Do not introduce a keyboard layout DSL or key-spec renderer.
- Do not change Keepass2Android integration behavior.
- Do not add new keyboard features.
- Do not preserve old DataStore quick-action compatibility keys or values.

## Chosen Approach

Use a responsibility-based package and naming refactor while keeping the current
direct Compose rendering model.

This approach is intentionally conservative about behavior. Most work should be
`git mv`, package/import updates, renames, and small extractions where a file has
an obvious single-responsibility problem. It avoids introducing a layout DSL
because the current keyboard does not need user-defined layouts, dynamic layout
packs, or another rendering layer.

The alternative of introducing an internal layout-spec model would reduce some
duplicate row declarations, but it would also add abstraction over intents,
enabled states, field safety, sizing, and accessibility. That is not justified
for a behavior-preserving cleanup.

## Target Package Structure

```text
ui/keyboard/
  KeyboardImeContent.kt
  frame/
  metrics/
  shared/
  textinput/
  entry/
  quickactions/
```

### `KeyboardImeContent.kt`

`KeyboardImeContent` replaces `KeyboardRoot`.

Responsibilities:

- Resolve orientation-aware keyboard metrics.
- Provide `LocalKeyboardAdaptiveMetrics`.
- Own the top-level keyboard `Surface`.
- Clip the IME content to the configured keyboard height.
- Delegate frame composition to `KeyboardFrame`.

The call site in `KeyboardImeService` may be updated to use the new entry point.
The UI still receives only safe `KeyboardUiState`, `KeyboardSettings`, and
`KeyboardIntent` callback inputs.

### `frame`

Owns the IME frame and high-level content placement.

Expected contents:

- `KeyboardFrame`
- `KeyboardContentArea`
- `KeyboardBottomSpacer`
- `KeyboardNavigationBarSpacer`

Responsibilities:

- Measure available constraints.
- Clamp navigation bar insets using existing metrics.
- Provide `LocalKeyboardLayoutMetrics`.
- Host the quick-action bar, quick-action panel, text input keyboard, and entry
  keyboard.
- Keep bottom spacer and navigation spacer outside keyboard row height
  calculation.

### `metrics`

Owns keyboard sizing, orientation, and layout measurement contracts.

Expected contents:

- `KeyboardAdaptiveMetrics`
- `KeyboardLayoutMetrics`
- `KeyboardLayoutInput`
- `KeyboardHeight`
- `KeyboardOrientation`
- `KeyboardMetrics`
- `LocalKeyboardAdaptiveMetrics`
- `LocalKeyboardLayoutMetrics`

Responsibilities:

- Keep all width and height formulas centralized.
- Keep existing non-negative dimension safeguards.
- Keep existing pixel snapping behavior.
- Avoid Compose measurement-state loops by calculating from constraints and
  stable inputs.

### `shared`

Owns reusable keyboard UI primitives and test contracts.

Expected contents:

- `KeyboardKey`
- `KeyboardIconKey`
- `KeyboardKeyColors`
- `KeyboardKeyEmphasis`
- `CommitTextKey`
- `LetterKey`
- `FieldKey`
- `KeyboardRow`
- `KeyboardTestTags`

Responsibilities:

- Provide shared key visuals, semantics, press state, disabled state, and click
  behavior.
- Provide row spacing/alignment helper.
- Keep safe field display behavior id-based and value-free.
- Keep test tags stable unless a test-only rename is needed alongside updated
  tests.

`ExistingEntryHint` should move to `quickactions` because it is part of the
quick-action bar's active-entry affordance, not a generic keyboard primitive.

### `textinput`

Owns ordinary text input layouts.

Expected contents:

- `TextInputKeyboardLayout`
- `LetterTextInputLayout`
- `NumberTextInputLayout`
- `SymbolTextInputLayout`
- `TextInputActionRow`
- `CommitTextKeyRow`

Domain/feature naming changes:

- `DefaultInputMode` -> `TextInputMode`
- `MainKeyboardLayout.Default` -> `MainKeyboardLayout.TextInput`

Responsibilities:

- Render letters, numbers, symbols, and text-input action row.
- Keep current key order, widths, actions, and accessibility labels unchanged.
- Keep `SpaceKey`, `EnterKey`, language switch, number/symbol switch, and entry
  switch behavior unchanged.

### `entry`

Owns KP2A entry field keyboard UI.

Expected contents:

- `EntryKeyboardLayout`
- `NormalEntryContent`
- `ExpandedEntryContent`
- `EntryFieldGrid`
- `EntryFieldPageState`
- `NormalEntryActionRow`
- `ExpandedEntryActionRows`

Responsibilities:

- Render fixed fields, remaining fields, and entry action rows.
- Keep field buttons id-based through `KeyboardIntent.CommitField(fieldId)`.
- Keep normal entry scrolling behavior unchanged.
- Keep expanded entry page buttons, drag-end snap, reset, and clamp behavior
  unchanged.

If `EntryKeyboardLayout.kt` remains too dense after the package move,
`NormalEntryContent` and `ExpandedEntryContent` may be extracted into separate
files. Any extraction must preserve existing scroll keys and `LaunchedEffect`
dependencies.

### `quickactions`

Owns the keyboard quick-action system. This replaces all `utility` naming.

Expected UI contents:

- `QuickActionBar`
- `QuickActionPanel`
- `QuickActionSlot`
- `QuickActionIconButton`
- `QuickActionDragState`
- `QuickActionDragPreview`
- `QuickActionDropTarget`
- `ExistingEntryHint`

Expected quick-action model contents:

- `KeyboardQuickAction`

Expected domain contents:

- `KeyboardQuickActionId`
- `KeyboardQuickActionSlots`
- `KeyboardQuickActionSlotsReducer`

Expected intent/state naming changes:

- `KeyboardIntent.ToggleUtilityPanel` -> `KeyboardIntent.ToggleQuickActionPanel`
- `KeyboardIntent.CloseUtilityPanel` -> `KeyboardIntent.CloseQuickActionPanel`
- `KeyboardIntent.ClickUtilityItem` -> `KeyboardIntent.ClickQuickAction`
- `KeyboardIntent.MoveUtilityItemToCenter` ->
  `KeyboardIntent.MoveQuickActionToCenter`
- `KeyboardIntent.MoveUtilityItemToRight` ->
  `KeyboardIntent.MoveQuickActionToRight`
- `KeyboardIntent.RemoveUtilityItem` -> `KeyboardIntent.RemoveQuickAction`
- `KeyboardUiState.utilitySlots` -> `KeyboardUiState.quickActionSlots`
- `KeyboardUiState.isUtilityPanelExpanded` ->
  `KeyboardUiState.isQuickActionPanelExpanded`

Responsibilities:

- Render pinned quick actions.
- Render the expanded quick-action panel.
- Measure drag targets.
- Resolve drop targets.
- Dispatch quick-action move/remove intents.
- Preserve current drag, drop, replacement, and removal behavior.

DataStore compatibility is not required for this rename. Old quick-action
preference keys and values may be replaced without migration.

## Behavior-Preservation Contract

Behavior preservation is a hard constraint.

The implementation must not intentionally change:

- Which keyboard layout is shown for a given state.
- Any key's emitted `KeyboardIntent`.
- Any key's enabled/disabled state.
- Any visible text or icon.
- Any content description.
- Existing layout height and width formulas.
- Existing navigation inset clamp behavior.
- Entry field scroll, paging, snap, reset, and clamp behavior.
- Quick-action drag/drop behavior.
- Sensitive-data restrictions.

If implementation reveals a necessary behavior change, stop and request user
approval before continuing.

The approved exception is DataStore compatibility for renamed quick-action
settings. Old stored quick-action preferences do not need to be migrated.

## Sensitive Data Rules

The refactor must preserve the current security model.

Allowed to hold sensitive field values:

- `KeyboardSessionRepository`
- `KeyboardSession`
- `KeyboardField.value`

Not allowed to hold sensitive field values:

- `KeyboardUiState`
- `KeyboardFieldSummary`
- Compose UI parameters
- displayed text
- content descriptions
- semantics
- test tags
- logs
- DataStore
- documentation
- screenshots
- tests

Field commit must stay id-based:

```kotlin
KeyboardIntent.CommitField(fieldId)
```

Forbidden examples:

```kotlin
Text(field.value)
Modifier.testTag(field.value)
contentDescription = field.value
KeyboardIntent.CommitText(field.value)
```

Allowed examples:

```kotlin
Modifier.testTag(KeyboardTestTags.field(field.id))
Text(field.safeLabel)
KeyboardIntent.CommitField(field.id)
```

## Migration Stages

### Stage 1: Domain and Feature Naming

Rename domain and feature concepts before moving UI packages.

Expected changes:

- `DefaultInputMode` -> `TextInputMode`
- `MainKeyboardLayout.Default` -> `MainKeyboardLayout.TextInput`
- `KeyboardUtilityItemId` -> `KeyboardQuickActionId`
- `SettingsUtilityItemId` -> `SettingsQuickActionId`
- `ClearEntryUtilityItemId` -> `ClearEntryQuickActionId`
- `KeyboardUtilitySlots` -> `KeyboardQuickActionSlots`
- `KeyboardUtilitySlotsReducer` -> `KeyboardQuickActionSlotsReducer`
- `KeyboardIntent.*Utility*` -> `KeyboardIntent.*QuickAction*`
- `KeyboardUiState.utility*` -> `KeyboardUiState.quickAction*`
- Settings repository and tests update to the new names.

No reducer behavior should change.

### Stage 2: UI Package Move and Entry Point Rename

Move files to target packages with `git mv` and update imports.

Expected changes:

- `KeyboardRoot.kt` -> `KeyboardImeContent.kt`
- Root call sites update from `KeyboardRoot` to `KeyboardImeContent`.
- `layout` frame files move to `frame`.
- sizing/orientation files move to `metrics`.
- shared key and row files move to `shared`.
- text input files move to `textinput`.
- entry files move to `entry`.
- utility files move to `quickactions`.

At the end of this stage, the project should compile with equivalent behavior.

### Stage 3: Quick Actions Naming Cleanup

Rename quick-action UI types and pure functions.

Expected changes:

- `UtilityRow` -> `QuickActionBar`
- `UtilityPanel` -> `QuickActionPanel`
- `UtilityItemSlot` -> `QuickActionSlot`
- `UtilityIconButton` -> `QuickActionIconButton`
- `UtilityDragState` -> `QuickActionDragState`
- `UtilityDragPreview` -> `QuickActionDragPreview`
- `UtilityDragSource` -> `QuickActionDragSource`
- `UtilityDropTarget` -> `QuickActionDropTarget`
- `resolveUtilityDropTarget` -> `resolveQuickActionDropTarget`
- `dispatchUtilityDrop` -> `dispatchQuickActionDrop`
- utility row policy functions become quick-action policy functions.

Existing unit tests should be renamed and should continue to assert the same
drop target and preview offset behavior.

### Stage 4: Entry File Responsibility Cleanup

Split only where the current file still mixes obvious responsibilities.

Allowed extractions:

- `NormalEntryContent` to its own file.
- `ExpandedEntryContent` to its own file.
- Expanded entry scroll/paging helper if the extraction is a direct move of
  existing state handling.

Do not change scroll keys, dependencies, or page math.

### Stage 5: Tests and Previews

Update all affected tests and previews.

Expected changes:

- JVM unit tests under `ui/keyboard/layout` or `ui/keyboard/utility` move to
  matching new packages such as `metrics`, `entry`, or `quickactions`.
- Compose UI tests update root/test-tag imports and fixture entry point names.
- Debug previews update `KeyboardImeContent` imports and renamed state fields.

Existing assertions should remain behavior-focused and value-free.

## Validation

Run after implementation:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Run when an Android device or emulator is available:

```bash
./gradlew :app:connectedDebugAndroidTest
```

If `connectedDebugAndroidTest` cannot run because no device or emulator is
available, the implementation summary must say so explicitly.

## Acceptance Criteria

- `ui/keyboard` uses the target responsibility-based package structure.
- `Default*` text input naming is removed from keyboard layout/domain concepts.
- `Utility*` quick-action naming is removed from keyboard UI/domain/feature
  concepts.
- `entry` is the KP2A entry package name.
- `KeyboardImeService`, debug previews, JVM tests, and Compose UI tests compile
  against the new names.
- Unit tests pass.
- Debug build succeeds.
- Sensitive-data UI tests still verify that field values are not exposed.
- No behavior change is introduced without separate user approval.

## Open Decisions

No product behavior decisions remain open. Implementation may choose exact file
boundaries inside each package when the choice is purely structural and preserves
behavior.
