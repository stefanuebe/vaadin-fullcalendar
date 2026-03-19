# Next Step 4: E2E Coverage Review — All New Features

## Goal
Systematically audit every new feature implemented across all phases against
the existing E2E tests. Add missing tests. Err on the side of more tests.

## Status
⬜ NOT STARTED

## How to do this
Read each test view + spec file pair. For each feature in the implementation,
check: is there an E2E test that exercises it and verifies observable DOM output?

## Known gaps (pre-audit)

### Entry model features
- `setUrl` — test checks `href` attribute exists? ✅ check
- `setInteractive` per-entry — test checks `tabindex="0"`? ✅ check
- `setRecurringDuration` — test checks multi-day span renders? ✅ check
- `setRrule` basic weekly — ✅ exists
- `setExdate` — ⬜ MISSING (→ next-3)
- `setRrule` monthly/raw — ⬜ MISSING (→ next-3)
- `setOverlap(Boolean)` — no DOM-observable effect, skip

### Display options features
Check `DisplayOptionsTestView` + `display-options.spec.js`:
- Render hook callbacks (dayCellClassNames, dayHeaderContent, slotLabel, nowIndicator) — do tests verify DOM changes from callbacks?
- `setProgressiveEventRendering` — hard to test, skip
- `setHandleWindowResize` / `setWindowResizeDelay` — hard to test, skip
- `setThemeSystem` — is the correct CSS class applied?

### Interaction callback features
Check `InteractionCallbacksTestView` + `interaction-callbacks.spec.js`:
- `EntryDragStartEvent` / `EntryDragStopEvent` — is drag simulated?
- `EntryResizeStartEvent` / `EntryResizeStopEvent` — is resize simulated?
- `setSelectAllow` / `setEventAllow` — is callback invoked?
- `setUnselectCancel` — hard to test
- `ExternalEntryDroppedEvent` — is external drop simulated?
- `setDropAccept` — is CSS selector filter tested?

### Event source features
Check `EventSourcesTestView` + `event-sources.spec.js`:
- `JsonFeedEventSource` — does calendar fetch and render from feed?
- `GoogleCalendarEventSource` — likely skip (needs API key)
- `ICalendarEventSource` — does calendar fetch .ics and render?
- `EventSourceFailureEvent` — is failure case tested?
- `setStartParam` / `setEndParam` / `setTimeZoneParam` — are query params verified?

### Scheduler features
Check `SchedulerFeaturesTestView` + `scheduler-features.spec.js`:
- `setResourceAreaColumns` — do columns render?
- `setResourceGroupField` — do groups render?
- `setResourceGroupClassNamesCallback` — is CSS class applied?
- `setRefetchResourcesOnNavigate` — is refetch triggered on nav?

### Accessibility features
Check `AccessibilityTestView` + `accessibility.spec.js`:
- `setEventInteractive` — tabindex="0" on all events?
- `setButtonHints` — aria-label on toolbar buttons?
- `setNavLinkHint` / `setMoreLinkHint` / `setViewHint` — aria attributes?
- `setCloseHint` / `setTimeHint` / `setEventHint` — aria attributes?

### Advanced options features
Check `AdvancedOptionsTestView` + `advanced-options.spec.js`:
- `CustomButton` click → `CustomButtonClickedEvent` — ✅ exists
- `setViewSpecificOption` — "+N more" link enforced in month view? ✅ check
- `setButtonIcons` — icon CSS class applied to buttons? ⬜ check
- `setEventConstraint` — drag constrained to business hours? (hard to E2E test)
- `getCurrentIntervalStart` / `getCurrentIntervalEnd` — server reads correct dates?
- `incrementDate` / `previousYear` / `nextYear` / `updateSize` — navigation works?
- `setValidRangeCallback` / `setSelectOverlapCallback` — hard to E2E, skip
- `setFixedMirrorParent` / `setDragScrollEls` — hard to E2E, skip
- `setContentSecurityPolicyNonce` — hard to E2E, skip

## Output
After audit: one PR adding all missing test view entries + Playwright assertions.
Document any features deemed untestable via E2E (with reason) in a comment in the spec file.

## Docs / Javadoc updates
Any bug or missing behaviour discovered during this audit must be fixed in the same PR:
- Javadoc on the affected method/field in `FullCalendar.java`, `Entry.java`, `Scheduler.java`, etc.
- `docs/Features.md` — relevant section
- `docs/Release-notes.md` — 7.1.x entry if the description was wrong
- `docs/Samples.md` — any affected code example

## Mandatory: Code Review
Run the `code-reviewer` agent on all changed files before marking this step complete.
This is non-negotiable — no step is done without a code review.

## Mandatory: End-User Review
Run the `end-user-reviewer` agent on any changed docs or Javadoc.
Fix all findings and re-run until the reviewer gives a clean pass.
No step is done without end-user sign-off on documentation.
