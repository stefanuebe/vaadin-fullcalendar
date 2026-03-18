# FullCalendar v6 vs. Java/Vaadin Addon Feature Comparison

## Summary

| Metric | Count |
|---|---|
| FC v6 options/callbacks analyzed | ~150 |
| Fully covered (typed Java API) | ~60 |
| Partially covered (raw `setOption(String, Object)` only) | ~30 |
| Missing (no equivalent) | ~60 |

## Versions

- **FullCalendar JS**: v6.1.20 (as declared in `FullCalendar.FC_CLIENT_VERSION`)
- **Addon**: Version 7.x (Vaadin 25 / Java 21)
- **Analysis date**: 2026-03-18

## Files in this directory

| File | Description |
|---|---|
| `README.md` | This file — overview and summary |
| `missing-features.md` | Complete feature-by-feature status table |
| `phase-0-api-improvements.md` | Improvements to existing API (naming, typed overloads, etc.) |
| `phase-1-entry-model.md` | Missing Entry/Event model properties |
| `phase-2-display-options.md` | Missing calendar-level display options and render hooks |
| `phase-3-interaction-callbacks.md` | Missing interaction callbacks and server-side events |
| `phase-4-event-sources.md` | Event source improvements (JSON feed, Google Calendar, iCal) |
| `phase-5-scheduler.md` | Missing scheduler/resource features |
| `phase-6-accessibility-touch.md` | Accessibility, print, touch options |
| `phase-7-advanced.md` | Advanced/niche options |
| `phase-8-docs-and-samples.md` | Update documentation and demo/sample application |

## Key Findings

1. **Entry model is mostly complete** — all core FC event properties are covered, except `url`, `interactive`, and the `rrule` plugin properties.

2. **Render hooks are incomplete** — `eventClassNames`, `eventContent`, `eventDidMount`, `eventWillUnmount` are covered via JS string callbacks. But `dayCellClassNames/Content/DidMount/WillUnmount`, `dayHeaderClassNames/Content/DidMount/WillUnmount`, `slotLabel*`, `slotLane*`, `viewClassNames/DidMount/WillUnmount`, `nowIndicator*`, `moreLinkClassNames/Content/DidMount/WillUnmount`, `weekNumber*`, `noEvents*` render hooks are entirely missing.

3. **Drag/drop lifecycle callbacks missing** — `eventDragStart`, `eventDragStop`, `eventResizeStart`, `eventResizeStop` (the "begin/end" callbacks) have no Java server-side event equivalents. Only the final `eventDrop` and `eventResize` are covered.

4. **Event lifecycle callbacks missing** — `eventAdd`, `eventChange`, `eventRemove`, `eventsSet` (FC mutation callbacks) have no Java equivalents.

5. **External drag-drop not supported** — `droppable`, `dropAccept`, `drop`, `eventReceive`, `eventLeave` are entirely missing.

6. **Event source features incomplete** — JSON feed, Google Calendar, and iCalendar event sources are client-side-only (via `setOption(String, Object)`). No typed Java API. `loading`, `eventSourceFailure`, `eventSourceSuccess`, `lazyFetching` callbacks missing.

7. **Scheduler resource options partially covered** — `resourceAreaColumns`, `resourceGroupField`, `datesAboveResources`, `resourceAdd/Change/Remove/Set` callbacks, `refetchResourcesOnNavigate` (in enum but no typed setter) are missing typed API.

8. **Nav-link callbacks not server-round-trippable** — `navLinkDayClick` and `navLinkWeekClick` are JS-only options.

9. **Several display options missing typed API** — `displayEventEnd`, `displayEventTime`, `progressiveEventRendering`, `rerenderDelay`, `windowResizeDelay`, `handleWindowResize`, `now`, `nowIndicatorSnap`, `themeSystem`, `initialDate`, `dateAlignment`, `dateIncrement`, `lazyFetching`.
