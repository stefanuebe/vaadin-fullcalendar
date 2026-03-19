# Next Step 1: Remove "Phase N" Labels from Code and Docs

## Goal
All "Phase N" terminology was an internal planning device — it must not appear in
production code, comments, test names, view names, routes, or documentation.

## Status
⬜ NOT STARTED

## Scope

### Java test views (`demo/src/main/java/.../testviews/`)
Rename files + classes + routes + @MenuItem labels. Current → target:

| Current file | New file | Current route | New route |
|---|---|---|---|
| `Phase1EntryModelTestView.java` | `EntryModelTestView.java` | `phase1-entry-model` | `test/entry-model` |
| `Phase2TestView.java` | `DisplayOptionsTestView.java` | `phase2` | `test/display-options` |
| `Phase3TestView.java` | `InteractionCallbacksTestView.java` | `phase3` | `test/interaction-callbacks` |
| `Phase4EventSourcesTestView.java` | `EventSourcesTestView.java` | `phase4-event-sources` | `test/event-sources` |
| `Phase5SchedulerTestView.java` | `SchedulerFeaturesTestView.java` | `phase5-scheduler` | `test/scheduler-features` |
| `Phase6AccessibilityTestView.java` | `AccessibilityTestView.java` | `phase6-accessibility` | `test/accessibility` |
| `Phase7AdvancedTestView.java` | `AdvancedOptionsTestView.java` | `phase7-advanced` | `test/advanced-options` |

### Playwright spec files (`e2e-tests/tests/`)
Rename files + `test.describe` labels + `goto*` helper names:

| Current | New |
|---|---|
| `phase1-entry-model.spec.js` | `entry-model.spec.js` |
| `phase2.spec.js` | `display-options.spec.js` |
| `phase3.spec.js` | `interaction-callbacks.spec.js` |
| `phase4-event-sources.spec.js` | `event-sources.spec.js` |
| `phase5-scheduler.spec.js` | `scheduler-features.spec.js` |
| `phase6-accessibility.spec.js` | `accessibility.spec.js` |
| `phase7-advanced.spec.js` | `advanced-options.spec.js` |

### Java unit tests (`addon/src/test/java/`)
Rename files + classes + `@DisplayName` strings:

| Current | New |
|---|---|
| `Phase7AdvancedTest.java` | `AdvancedOptionsTest.java` |

Check all other test files for "Phase N" in class names, method names, display names.

### Code comments
Search for `// Phase` or `Phase N` in all `.java` and `.ts` files and remove/replace.

### Docs
`docs/Release-notes.md` already cleaned. Double-check all other docs files.

## How to verify
- `grep -rn "phase[0-9]\|Phase [0-9]\|Phase[0-9]" /workspace/addon /workspace/addon-scheduler /workspace/demo /workspace/e2e-tests` returns no matches (excluding this plan file and `/workspace/feature-comparison/`)

## Mandatory: Code Review
Run the `code-reviewer` agent on all changed files before marking this step complete.
This is non-negotiable — no step is done without a code review.

## Mandatory: End-User Review
Run the `end-user-reviewer` agent on any changed docs or Javadoc.
Fix all findings and re-run until the reviewer gives a clean pass.
No step is done without end-user sign-off on documentation.
