# 07: Per-view options with `options().view(view)` and `clear()`

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `05-options-object-first-fields` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `options().view(CalendarView)` returns a `FullCalendarViewOptions` that exposes only the per-view accessors, through `@Delegate` on a private accessor that gets or creates that view's data instance and schedules the sync. It holds the calendar and the view's client-side value, has no public constructor and no state of its own. The getter returns only the view's own value. `clear()` drops that view's values. An empty view instance adds no `views` entry. The view values reach the client under `views`. Spec: *Types* (view's options object), *View options*, user stories 17–23, 45.

**Blocked by:** `05-options-object-first-fields`

**Status:** ready-for-agent

- [ ] A view value is sent only under `views.<view>`.
- [ ] The view getter returns `null` when only a calendar-wide value exists.
- [ ] `null` removes one view value, `clear()` all values of that view, also while detached (reaches the client after the reattach).
- [ ] A view getter alone sends nothing.
- [ ] A kept `view(…)` reference changes the client in a later request.
- [ ] `SerializationTest` covers view values and a kept `view(…)` reference.
