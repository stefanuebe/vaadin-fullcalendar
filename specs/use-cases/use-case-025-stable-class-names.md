# UC-025: Stable Class Names

**As a** Vaadin application developer, **I want** the familiar v6 class names (`fc-event`, `fc-daygrid-day`, `fc-day-today`, ...) to stay on the elements FullCalendar 7 renders **so that** my class-based CSS and my browser tests keep working after the upgrade.

**Status:** Implemented
**Date:** 2026-10-06

---

## Scope

**Addon module:** both (core list in `addon`, scheduler list in `addon-scheduler`)
**Related Options:** FullCalendar's `*Class` / `className` options
**Related Events:** —

---

## User-Facing Behavior

- FullCalendar 7 renders build-generated class names only. The addon adds a curated subset of the FullCalendar 6 class names to the same elements, so existing selectors find them.
- The classes also appear on elements FullCalendar renders outside the calendar element: the more-link popover and the entry being dragged (attached to `body`).
- The list is public API and documented in the wiki page *Stable class names*. Classes that are not on the list are gone.
- Selectors that depend on the v6 DOM structure (`table td`, `.fc-daygrid-day-frame`, `> a`) do not work anymore, even when they combine stable classes.

---

## Java API Usage

No Java API. The classes are always present.

```css
/* application stylesheet */
vaadin-full-calendar .fc-event.fc-event-past {
    opacity: 0.6;
}
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | The classes are delivered as `optionDefaults` / `views` of an FC plugin. FullCalendar joins `*Class` values of plugins, themes and user options, so the stable classes never replace theme or user classes. |
| BR-02 | Each class is mapped to the option and info flag that FullCalendar's CSS migration guide (`upgrading-from-v6-css`) names for it. Classes the guide lists as obsolete or without replacement are not offered. |
| BR-03 | Scheduler classes (timeline, datagrid, resource) live in the scheduler module, core classes in the core module. |
| BR-04 | `fc-event-mirror` marks rendered mirror entries (e.g. during a resize). The element that follows the pointer during a drag carries `fc-event-dragging`. |
| BR-05 | The calendar root (the component element) carries `fc`. The migration guide drops this class, so keeping it is an add-on decision for existing `.fc` selectors. The view container carries `fc-view`, `fc-<viewType>-view` (e.g. `fc-dayGridMonth-view`) and the view family class (`fc-daygrid`, `fc-timegrid`, `fc-list`, `fc-multimonth`, `fc-timeline`, `fc-resource-timeline`). |

---

## Acceptance Criteria

- [x] Root, view, day header, day cell, day number, time grid slot / lane / column and entry elements carry their v6 classes.
- [x] The more-link popover carries `fc-popover`, `fc-more-popover`, `fc-popover-header`, `fc-popover-body`.
- [x] The dragged entry outside the calendar carries `fc-event` and `fc-event-dragging`. A resize mirror carries `fc-event-mirror`.
- [x] The day number carries `fc-daygrid-day-number` also when its text is more than the number (e.g. `10日` in Japanese).
- [x] Theme classes stay on elements that carry stable classes.
- [x] Resource timeline, resource time grid and resource day grid views carry the scheduler classes (wiki page *Stable class names*, section Scheduler).

---

## Tests

### Unit Tests

- none, the classes exist on the client only

### E2E Tests

- [x] `stable-class-names.spec.js`: representative class per element family in day grid, time grid, popover, drag and resize mirror; joined with the theme's classes
- [x] `scheduler-views.spec.js`: the timeline test "carries the scheduler stable classes", and `fc-resource` on the resource day headers in the time grid and day grid tests
- [x] The other specs use stable classes, `data-date` / `data-time` and ARIA attributes as selectors

---

## Related FullCalendar Docs

- [Custom CSS migration guide](https://fullcalendar.io/docs/upgrading-from-v6-css)
- [ClassName input](https://fullcalendar.io/docs/classname-input)
- ADR 0001 (`docs/adr/0001-stable-v6-class-names.md`)
