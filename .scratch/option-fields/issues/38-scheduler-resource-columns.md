# 38: `resourceColumns` through the sync hook, resource re-add flush

| | |
|---|---|
| GitHub | #394 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16) |
| Blocked by | `03-flush-before-client-calls`, `18-sync-hook-entry-did-mount`, `37-scheduler-options-object` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The hand-written `setResourceColumns` moves to the scheduler options object; component resource columns keep their binding through the hook. The scheduler's resource re-add calls `flushOptions()` first. `resources: []` stays in the scheduler client. Spec: user story 58.

**Blocked by:** `03-flush-before-client-calls`, `18-sync-hook-entry-did-mount`, `37-scheduler-options-object`

**Status:** done (2026-10-16)

- [x] Browserless: the hook output for `resourceColumns` keeps component columns.
- [x] The resource re-add calls `flushOptions()` first.
- [x] Playwright: component resource columns render after being set through the options object; the scheduler starts with an empty resource list.

**Notes from the implementation (2026-10-16):**

- `resourceColumns` is a plain field in `SchedulerCalendarOptions` with `@JsonConverter(ResourceColumnsConverter.class)`, not a `contributeOptions` override. The column JSON is static (`ComponentResourceColumn#toJson` reads no calendar state), so nothing needs computing at sync time. The binding happens in the hand-written setter `FullCalendarSchedulerOptions#setResourceColumns(List)`, which calls `FullCalendarScheduler#applyResourceColumns`: validate and bind, then set the field (unmodifiable copy). An empty list or null removes the option, as in 7.x. The field setter is package-private, like `timeZone`'s.
- One setter only, taking `List`, as `setHiddenDays(Set)` (one form, no varargs overload).
- `setOption(SchedulerOption.RESOURCE_COLUMNS, list)` goes into the field and removes a raw value set before; any other value clears the field (unbinds) and goes raw. A raw `resourceColumns` wins over the field, as every raw option does. `getOption(String, boolean)` is overridden in the scheduler so `getOption(RESOURCE_COLUMNS)` still returns the columns until 51 removes the old API.
- The scheduler's `setResourceColumns(List)` and `(ResourceColumn...)` are deprecated for removal and delegate to the options object. Only `ComponentResourceColumnsTestView` moved to the new setter (criterion); the other callers move in 47–50.
- `flushOptions()` is now `protected` (maintainer rule: protected over package-private). `flushResourceOps()` calls it first, which covers the reattach re-add and every other resource call. On a reattach the core restore callback already flushes before; the test `reattachInOneRoundTrip_reAddsTheResourcesAfterTheOptions` guards that order only. `resources_reachTheClientAfterTheOptionsOfTheSameRequest` fails without the flush.
- `resources: []` stays in the client. Without it FullCalendar 7.1 still shows resources added later, so the Playwright test reads `calendar.getOption('resources')`, which fails without the line.
- Tests: `SchedulerResourceColumnsOptionTest` (10), `List` registered in `SchedulerConverterTest.ONE_FORM_TYPES` and `SchedulerTypedOptionsSyncTest.ONE_FORM_SAMPLES`, pinned list updated, one Playwright test in `component-resource-columns.spec.js` (465 E2E tests now). Mutation check: removing the flush, the binding, the raw removal or the client's `resources ?? []` each fails a test.
- Review: two rounds, no major. Left as it was on master and 7.x: a `ComponentResourceColumn` bound to another calendar throws after the old columns were already unbound.
