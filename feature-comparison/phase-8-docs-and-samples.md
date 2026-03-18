# Phase 8: Update Docs and Samples

## Goal

Update the addon's documentation and demo/sample application to reflect all new features added in Phases 0–7. Ensure new APIs are discoverable, well-explained, and accompanied by working code examples.

---

## 8.1 Release Notes / Changelog

Update `docs/Release-notes.md` with a summary of all additions:

- Phase 0: API improvements (typed setters/getters, naming aliases, new enums, builder methods)
- Phase 1: Entry model additions (`url`, `interactive`, `recurringDuration`, `RRule`, `overlap` nullable)
- Phase 2: Display options and render hook callbacks
- Phase 3: Interaction callbacks (drag/resize lifecycle, unselect, navLink, external drag-drop)
- Phase 4: Event source typed API (JSON feed, Google Calendar, iCal, `ExternalEntryDroppedEvent`)
- Phase 5: Scheduler resource features
- Phase 6: Accessibility and touch options
- Phase 7: Advanced/niche options

Include a migration note for any breaking or behavior-changing items:
- `Entry.overlap` changed from `boolean` (default `true`) to `Boolean` (default `null` = inherit). Code that relied on `entry.isOverlap()` must switch to `entry.getOverlap()`. Code that depended on `overlap = true` being serialized always will now see the field omitted when not set (FC's default is `true`, so behavior is unchanged unless a global `slotEventOverlap` was set).
- `FullCalendarBuilder` methods: no longer return a new immutable instance — the same instance is mutated. Storing intermediate builder states and calling `.build()` on each will now return the same final state. (Non-issue in practice since intermediate builder states are never stored.)
- `setResourceLablelWillUnmountCallback` is deprecated (forRemoval=true) — use `setResourceLabelWillUnmountCallback`.

---

## 8.2 Feature Documentation (`docs/Features.md`)

Add or expand sections for each new capability:

### Entry properties
- **`url`**: Document that FC navigates to the URL on click; note interaction with `entryClickedListener`.
- **`interactive`**: Document keyboard accessibility use case; distinguish from `editable` (drag/drop).
- **`recurringDuration`**: Document the multi-day all-day recurring event use case; note it only applies when recurrence fields are set.
- **`overlap`**: Document the change from `boolean` to `Boolean` and the null-means-inherit semantics.

### RRule
Deserves its own section. Document:
- When to use RRule vs. built-in recurrence (`recurringDaysOfWeek` etc.) — richer patterns vs. simplicity
- The two modes: structured (`RRule.weekly().byWeekday(...)`) and raw string (`RRule.ofRaw(...)`)
- The `exdate` field for exclusion dates
- The `@fullcalendar/rrule` plugin dependency (bundled automatically via `@NpmPackage`)
- Mutually exclusive with built-in recurrence on a per-entry basis

### Event sources (Phase 4)
- New typed event source classes
- `ExternalEntryDroppedEvent` for mixed-source drag-and-drop

### Display options (Phase 2)
- Render hook callbacks (`dayCellClassNames`, `dayHeaderContent`, etc.)

### Interaction (Phase 3)
- Drag/resize lifecycle events
- External drag-and-drop support

### Scheduler (Phase 5)
- Any new resource features added

---

## 8.3 Migration Guide (`docs/Migration-guides.md`)

Add a section for the new version with:
- Breaking changes (see 8.1 above)
- Deprecated methods and their replacements
- Any new required dependencies (none — rrule is bundled)

---

## 8.4 Demo Application

Update or add demo views in `demo/` to showcase new features:

### RRule demo
Show a working calendar with RRule-based recurring events:
```java
Entry weeklyMeeting = new Entry();
weeklyMeeting.setTitle("Weekly Standup");
weeklyMeeting.setRrule(RRule.weekly()
    .byWeekday(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
    .dtstart(LocalDate.now().withDayOfMonth(1))
    .until(LocalDate.now().plusYears(1)));

Entry lastFriday = new Entry();
lastFriday.setTitle("Last Friday of Month");
lastFriday.setRrule(RRule.monthly().byWeekday("-1fr"));
```

### Entry URL demo
Show entries with URLs and the browser-navigation behavior. Contrast with `entryClickedListener`.

### Entry `interactive` demo
Show keyboard-accessible entries (non-URL, non-draggable events that are still tab-navigable).

### `overlap` demo
Show the difference between `entry.setOverlap(false)` (this entry cannot be overlapped) and the global `setSlotEntryOverlap(false)`.

### Phase 3: drag/resize lifecycle demo
If drag/resize lifecycle events are added (Phase 3), show a status indicator that lights up during drag start/stop.

### Phase 4: External entry drop demo
If `ExternalEntryDroppedEvent` is added (Phase 4), show a drag-and-drop from an external list into the calendar.

---

## 8.5 Javadoc Review

Walk through all newly added public API and verify:
- All setters/getters have concise Javadoc explaining the FC option they map to
- FC documentation links (`@see <a href="...">`) are present for non-obvious options
- Parameter/return Javadoc is present
- Deprecated methods document their replacement

After the initial Javadoc pass, run an `end-user-reviewer` agent on the completed Javadoc, all `docs/*.md` files updated in this phase, and any user-facing error messages. Fix any unclear, misleading, or confusing documentation found.

Key areas to review:
- All 43 new typed setters in `FullCalendar.java` (Phase 0)
- `RRule.java` — factory methods, fluent setters, `toJson()`
- `Entry.java` — new fields `url`, `interactive`, `recurringDuration`, `rrule`, `exdate`
- New event source classes (Phase 4)
- New event classes (Phase 3)

---

## 8.6 MCP Server Docs Sync

The addon ships an MCP server (`mcp-server/`) with its own documentation. Sync any new API to:
- `mcp-server/README.md` → then copy to `docs/MCP-Server.md` (per CLAUDE.md: keep in sync)
- Update any code examples the MCP server exposes to include Phase 0–7 API

---

## Files to Modify

| File | Action |
|---|---|
| `docs/Release-notes.md` | Add new version section with changes and migration notes |
| `docs/Features.md` | Expand/add sections for new features |
| `docs/Migration-guides.md` | Add migration section for breaking/behavior changes |
| `demo/src/main/java/.../` | Add demo views for RRule, url, interactive, overlap |
| All new public Java classes | Javadoc review pass |
| `mcp-server/README.md` + `docs/MCP-Server.md` | Sync MCP server docs |
