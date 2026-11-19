# 05: `calendar.options()` with the first typed fields

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `01-raw-option-sync` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `calendar.options()` returns `FullCalendarOptions`, created once in the constructor. It exposes the accessors of the data classes `CalendarOptions` (global) and `ViewOptions` (all views) through Lombok `@Delegate` on private accessors that schedule the sync, so a kept reference works in a later request. First fields: `editable`, `firstDay`, `initialDate`. Field values are laid under the raw options: a raw option wins, removing it brings the field back, including changes made meanwhile. `null` means not set. Spec: *Types*, *Names*, user stories 1–13, 28–30.

**Blocked by:** `01-raw-option-sync`

**Status:** done (2026-10-15)

- [x] `options().setEditable(true)` reaches the client; `setEditable(null)` sends a removal.
- [x] A kept `options()` reference changes the client in a later request.
- [x] A raw option wins over the field with the same name; removing it brings the current field value back.
- [x] `initialDate` takes effect at creation (Playwright).
- [x] `FullCalendarOptions` has no public constructor; its `options()` Javadoc names the session lock.
- [x] `SerializationTest` covers a calendar with fields set.

**Notes from the implementation (2026-10-15):**

- `firstDay` and `initialDate` are in `CalendarOptions`: the library reads `initialDate` only from the calendar options (`getInitialDate(optionsData.calendarOptions, …)`), `firstDay` is a `buildDateEnv` input. `editable` is in `ViewOptions`.
- The data classes are package-private. `FullCalendarOptions.toJson` reads their fields by reflection, because `BeanProperties` needs public getters on a public class.
- `FullCalendarOptions` calls `optionsChanged()` before the delegated accessor runs, so before the first sync the `initialOptions` property misses that one change until the response. The sync before every response writes the current state, so the browser never sees the stale property.
- The add-on default `editable` is now the field, see the note in 17.
