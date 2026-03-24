# UC-[NNN]: [Feature Title]

> Copy this template for each feature as `use-case-NNN-short-name.md`.
> Replace all `[bracketed text]` with your content.

---

**As a** Vaadin application developer, **I want to** [capability] **so that** [benefit for the end user or developer].

**Status:** [Draft | Approved | Implemented]
**Date:** [YYYY-MM-DD]

---

## Scope

**Addon module:** [addon / addon-scheduler / both]
**Related Options:** [`Option.X`, `Option.Y`]
**Related Events:** [`SomeEvent`, `OtherEvent`]

---

## User-Facing Behavior

[Describe what the end user sees and can do in the browser. Write from the perspective of someone interacting with the calendar in a Vaadin application.]

- [The calendar displays...]
- [When the user clicks/drags/selects...]
- [The calendar responds by...]

---

## Java API Usage

[Show how the Vaadin developer configures this feature in Java code.]

```java
// Example configuration
calendar.setOption(Option.EXAMPLE, value);
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | [Rule — e.g., "Entries with editable=false cannot be dragged or resized"] |
| BR-02 | [Rule — e.g., "Per-entry settings override global calendar options"] |

---

## Acceptance Criteria

- [ ] [Criterion 1 — testable statement of expected behavior]
- [ ] [Criterion 2 — edge case or validation check]
- [ ] [Criterion 3]

---

## Tests

### Unit Tests

- [ ] [Test class and what it covers]

### E2E Tests

- [ ] [Playwright spec and what it verifies]

---

## Related FullCalendar Docs

- [FC option/feature name](https://fullcalendar.io/docs/[option-name])
