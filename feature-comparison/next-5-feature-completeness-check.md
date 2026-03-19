# Next Step 5: Feature Completeness Check vs FC Docs

## Goal
Verify that every implemented feature actually behaves as FC v6 documents.
Cross-reference our Java API and TypeScript against the downloaded FC docs
(`/workspace/fc-docs/_docs-v6/`). Flag discrepancies, missing mappings, and
FC options we implemented incorrectly or not at all.

## Status
✅ DONE — commit cf7de8f

## Known issues to investigate first

### exdate format (HIGH PRIORITY)
- FC docs show: `exdate: ['2025-03-10']` (array)
- Our impl sends: `"2025-03-10"` (single string) or `"2025-03-10,2025-03-17"` (comma-separated)
- FC docs also say: "A date input or array of date inputs"
- **Question**: Does FC accept a plain string, or must it be an array?
- **Source**: `/workspace/fc-docs/_docs-v6/event-model/rrule-plugin.md`
- **Resolution**: Confirm via E2E test (next-3). If string fails → fix `Entry.exdate`
  to serialize as a JSON array.

### RRule mutual exclusivity (MEDIUM)
- Our docs say "mutually exclusive with built-in recurrence"
- FC docs do NOT state this explicitly
- **Question**: What actually happens if both `rrule` and `daysOfWeek` are set?
- **Resolution**: Read FC source or test empirically.

## Methodology
For each FC options doc page in `/workspace/fc-docs/_docs-v6/`, check:
1. Is the option exposed in our Java API? (typed setter or raw `setOption` only?)
2. Is the TypeScript `setXxx` method correct (right option key, right value format)?
3. Does the serialization match what FC expects (type, format, casing)?

## Sections to review

### Event / Entry model
- `/workspace/fc-docs/_docs-v6/event-model/` — all files
- Key: `event-parsing.md`, `recurring-events.md`, `rrule-plugin.md`

### Calendar options
- `/workspace/fc-docs/_docs-v6/date-display/` — slot labels, now indicator, etc.
- `/workspace/fc-docs/_docs-v6/event-display/` — eventConstraint, eventOverlap, etc.
- `/workspace/fc-docs/_docs-v6/interaction/` — selectAllow, eventAllow, dragScroll, etc.
- `/workspace/fc-docs/_docs-v6/toolbar/` — customButtons, buttonIcons, buttonHints, etc.
- `/workspace/fc-docs/_docs-v6/views/` — dateIncrement, dateAlignment, viewSpecific, etc.
- `/workspace/fc-docs/_docs-v6/accessibility/` — eventInteractive, hints, etc.

### Event sources
- `/workspace/fc-docs/_docs-v6/event-sources/` — jsonFeed, googleCalendar, iCal
- Verify: query param names, response format expectations, failure handling

### Scheduler
- `/workspace/fc-docs/_docs-v6/scheduler/` — resourceAreaColumns, resourceGroupField, etc.

## Output
A list of:
- ✅ Confirmed correct
- ⚠️ Partially correct / minor deviation (document in Known-issues.md)
- ❌ Wrong / missing — fix required (file as issue or fix inline)

Priority fix: `exdate` format if confirmed wrong by E2E test in next-3.

## Docs / Javadoc updates
Every ❌ or ⚠️ finding must be fixed consistently across all layers in the same PR:
- Javadoc on the affected Java method/field
- `docs/Features.md`
- `docs/Release-notes.md` (7.1.x section)
- `docs/Samples.md` (if a code example is affected)
- `docs/Migration-guides.md` (if behaviour change affects upgrading users)
- TypeScript implementation in `full-calendar.ts` (if serialization/option key is wrong)

## Mandatory: Code Review
Run the `code-reviewer` agent on all changed files before marking this step complete.
This is non-negotiable — no step is done without a code review.

## Mandatory: End-User Review
Run the `end-user-reviewer` agent on any changed docs or Javadoc.
Fix all findings and re-run until the reviewer gives a clean pass.
No step is done without end-user sign-off on documentation.
