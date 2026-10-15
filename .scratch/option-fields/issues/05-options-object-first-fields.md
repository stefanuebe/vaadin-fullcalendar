# 05: `calendar.options()` with the first typed fields

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `01-raw-option-sync` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `calendar.options()` returns `FullCalendarOptions`, created once in the constructor. It exposes the accessors of the data classes `CalendarOptions` (global) and `ViewOptions` (all views) through Lombok `@Delegate` on private accessors that schedule the sync, so a kept reference works in a later request. First fields: `editable`, `firstDay`, `initialDate`. Field values are laid under the raw options: a raw option wins, removing it brings the field back, including changes made meanwhile. `null` means not set. Spec: *Types*, *Names*, user stories 1–13, 28–30.

**Blocked by:** `01-raw-option-sync`

**Status:** ready-for-agent

- [ ] `options().setEditable(true)` reaches the client; `setEditable(null)` sends a removal.
- [ ] A kept `options()` reference changes the client in a later request.
- [ ] A raw option wins over the field with the same name; removing it brings the current field value back.
- [ ] `initialDate` takes effect at creation (Playwright).
- [ ] `FullCalendarOptions` has no public constructor; its `options()` Javadoc names the session lock.
- [ ] `SerializationTest` covers a calendar with fields set.
