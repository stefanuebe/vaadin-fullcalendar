# Fix 2: Rename "Event" to "Entry" in new API methods and Option constants

## Context

PR #223 review comments by stefanuebe:
> The FC names its calendar items "events", but we name them "entry" to prevent misinterpretation with events (like a click event). Therefore these new methods should be named `setEntry...`

> auch hier nochmal die Erinnerung: in Java heißen die FC Events (also calendar items) nicht Event sondern Entry, also API entsprechend benennen

This addon uses "Entry" instead of "Event" for calendar items (to avoid confusion with Java/Vaadin component events). All new methods AND Option enum constants that reference calendar items as "Event" must be renamed to "Entry".

**Run Fix 1 first.** Only methods/options that survive Fix 1 need renaming here.

---

## A. Option enum constants to rename

The `Option` enum has a built-in mapping: `name().replace("ENTRY", "EVENT")` → camelCase. So renaming `EVENT_*` to `ENTRY_*` preserves the correct FC key automatically (e.g., `ENTRY_OVERLAP` → `eventOverlap`).

All these are **new** in this branch (none on master):

| Current name | New name | FC key (unchanged) |
|---|---|---|
| `EVENT_DRAG_MIN_DISTANCE` | `ENTRY_DRAG_MIN_DISTANCE` | `eventDragMinDistance` |
| `EVENT_LONG_PRESS_DELAY` | `ENTRY_LONG_PRESS_DELAY` | `eventLongPressDelay` |
| `EVENT_ALLOW` | `ENTRY_ALLOW` | `eventAllow` |
| `EVENT_OVERLAP` | `ENTRY_OVERLAP` | `eventOverlap` |
| `EVENT_INTERACTIVE` | `ENTRY_INTERACTIVE` | `eventInteractive` |
| `EVENT_CONSTRAINT` | `ENTRY_CONSTRAINT` | `eventConstraint` |
| `EVENT_HINT` | `ENTRY_HINT` | `eventHint` |

**No `Option(String)` constructor needed** — the existing `ENTRY→EVENT` replacement in the default constructor handles the mapping.

Also check existing master options with ENTRY prefix — they already follow this pattern:
`ENTRY_DISPLAY`, `ENTRY_COLOR`, `ENTRY_MIN_HEIGHT`, etc. The rename makes the new options consistent.

### Options that should NOT be renamed

These contain "EVENT" but refer to **event sources** (not calendar entries):
- `EXTERNAL_EVENT_SOURCE_*` — about client-side event sources, not entries

These contain "EVENT" but are FC feature names, not calendar-item references:
- `DAY_MAX_EVENT_ROWS` — FC calls it "eventRows" but it's about display, not entries. Keep as-is (matches FC docs).
- `FORCE_EVENT_DURATION` — same, FC naming. Keep as-is.
- `PROGRESSIVE_EVENT_RENDERING` — FC naming. Keep as-is.
- `DISPLAY_EVENT_END` — FC naming for displayEventEnd. Keep as-is.

**Rule of thumb:** Only rename if the "Event" refers to a single calendar entry that the user interacts with (drag, click, constrain, overlap). Display/rendering options that happen to have "event" in the FC name stay as-is.

---

## B. Methods to rename

Methods that survived Fix 1 and contain "Event" where it refers to a calendar entry:

| Current name | New name |
|---|---|
| `setEventAllowCallback(String)` | `setEntryAllowCallback(String)` |
| `setEventOverlapCallback(String)` | `setEntryOverlapCallback(String)` |
| `setEventDataTransformCallback(String)` | `setEntryDataTransformCallback(String)` |
| `setEventConstraint(BusinessHours)` | `setEntryConstraint(BusinessHours)` |
| `setDefaultTimedEventDuration(String)` | `setDefaultTimedEntryDuration(String)` |
| `setDefaultAllDayEventDuration(String)` | `setDefaultAllDayEntryDuration(String)` |
| `setDisplayEventTime(boolean)` | `setDisplayEntryTime(boolean)` — alias that delegates, check if still needed |

Methods that should **NOT** be renamed:
- `setEventSourceSuccessCallback(String)` — about event *sources*, not entries
- `addEventSourceFailureListener(...)` — about event *sources*
- `addExternalEntryDroppedListener(...)` — already uses "Entry"
- `addExternalEntryResizedListener(...)` — already uses "Entry"

---

## C. CallbackOption enum (Fix 99)

The `CallbackOption` enum (created in Fix 99) should also follow Entry naming for entry-related callbacks:

| CallbackOption constant | FC key |
|---|---|
| `ENTRY_CLASS_NAMES` | `"eventClassNames"` |
| `ENTRY_CONTENT` | `"eventContent"` |
| `ENTRY_DID_MOUNT` | `"eventDidMount"` |
| `ENTRY_WILL_UNMOUNT` | `"eventWillUnmount"` |
| `EVENT_ALLOW` → `ENTRY_ALLOW` | `"eventAllow"` |
| `EVENT_OVERLAP` → `ENTRY_OVERLAP` | `"eventOverlap"` |
| `EVENT_DATA_TRANSFORM` → `ENTRY_DATA_TRANSFORM` | `"eventDataTransform"` |
| `ENTRY_ORDER` | `"eventOrder"` — already correct |

Keep as `EVENT_SOURCE_SUCCESS` (about sources, not entries).

---

## D. TS methods to rename

In `full-calendar.ts`, rename the corresponding TS methods:

| Current TS name | New TS name |
|---|---|
| `setEventClassNamesCallback` | `setEntryClassNamesCallback` |
| `setEventDidMountCallback` | `setEntryDidMountCallback` |
| `setEventWillUnmountCallback` | `setEntryWillUnmountCallback` |
| `setEventAllowCallback` | `setEntryAllowCallback` |
| `setEventOverlapCallback` | `setEntryOverlapCallback` |

Also update the Java-side `callJsFunction("setEventXxxCallback", ...)` calls to match.

**Note:** These TS methods will be removed in Fix 99a when `setOptionFunction` replaces them. But until then they need consistent naming.

---

## E. Tests, views, docs

Update all references to renamed methods/options across:
- `addon/src/test/java/org/vaadin/stefan/fullcalendar/*.java`
- `e2e-test-app/src/main/java/org/vaadin/stefan/ui/view/testviews/*.java`
- `e2e-tests/tests/*.spec.js`
- `docs/Samples.md`, `docs/Features.md`, `docs/Release-notes.md`

---

## Verification

1. `mvn test -pl addon` — all tests pass
2. `mvn clean install -DskipTests` — compiles
3. Grep for old names (`EVENT_DRAG_MIN_DISTANCE`, `EVENT_ALLOW`, `setEventAllowCallback`, etc.) — zero hits in production code
4. Verify Option key mapping: `assertEquals("eventOverlap", Option.ENTRY_OVERLAP.getOptionKey())`
