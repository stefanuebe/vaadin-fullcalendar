# 75: Review `set…` versus `with…`

| | |
|---|---|
| GitHub | – |
| Labels | enhancement |
| Status | done (2026-10-17) |
| Blocked by | – |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A review of where the add-on's public API uses `set…` setters and where fluent `with…` methods (for example `ResourceColumn.withWidth`, the options objects' `set…`), which rule decides it and why, and whether they should be aligned. The result is a written rule, with the maintainer's decision, and a list of the places that break it. Raised by the maintainer during the triage of 73 (2026-10-17).

**Analysis (2026-10-17):** no written rule (`STYLEGUIDE.md`, `CONTEXT.md`, `docs/adr/`). Every public `with…` method mutates and returns `this`; no type returns a copy and no type mixes `set…` and `with…`. No `set…` method returns `this`.

| Style | Types |
|---|---|
| `set…`, void (explicit or Lombok `@Setter`) | the options classes, `Entry`, `Resource`, `ResourceEntry`, `BusinessHours` area (~447 setters) |
| `with…`, returns `this` | `RemoteEntrySource` (21), `JsonFeedEntrySource` (5), `GoogleCalendarEntrySource` (1), `Draggable` (2), `ResourceColumn` (16), `ComponentResourceColumn` (16), deprecated `ResourceAreaColumn` / `ComponentResourceAreaColumn` |
| fluent without prefix, returns `this` | `RRule` (`dtstart`, `count`, `interval`, … about 20) |
| Lombok `@Builder` | `Delta`, `EntryQuery` |

From 7.x: the entry sources, `Draggable`, `ResourceAreaColumn` and the `RRule` style. New on master: `ResourceColumn` and `ComponentResourceColumn`, which follow the `with…` style. Master dropped `FullCalendarBuilder` and the `with…` methods of `FullCalendar`. The de facto rule: `set…` void for options and domain classes, `with…` returning `this` for small configuration objects handed to the calendar (sources, draggables, resource columns).

**Decided by the maintainer (2026-10-17):** the de facto rule is written into `STYLEGUIDE.md` (*Architecture & package layout*), with one addition by the maintainer: a type meant for inheritance has no `with…` or chaining return, unless it chains through a self type. `RemoteEntrySource<S>` (from 7.x) already does; `ResourceColumn` gets a self-typed base in ticket 72, so the 16 covariant overrides in `ComponentResourceColumn` go. Rejected: chaining on the options objects, also through a self type (`AbstractCalendarOptions<S>`), because Lombok cannot generate setters returning `S` and `@Delegate` would go, so about 450 setters would have to be written by hand; Lombok `@With` (returns a copy, the options objects must change the synced instance) and `@Accessors(chain = true)` (returns the inner data class, its return type cannot be set). `RRule` keeps its iCalendar names.

**Blocked by:** none; the rule needed no code change, `ResourceColumn` follows it in ticket 72.

**Status:** done (2026-10-17)

- [x] Every public type with `set…` or `with…` methods is listed with the style it uses.
- [x] The rule and its reason are written down in `STYLEGUIDE.md`, decided by the maintainer.
- [x] The places that break the rule have their own tickets or are changed: `ResourceColumn`, in ticket 72.
