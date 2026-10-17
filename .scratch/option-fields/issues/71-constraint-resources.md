# 71: Constraints limited to resources

| | |
|---|---|
| GitHub | #402 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-17) |
| Blocked by | `61-constraint-rrule-window` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** In a resource view, `eventConstraint` and `selectConstraint` can limit dragging and selecting to resources. A constraint object is parsed as an entry (`normalizeConstraint` → `parseEvents` in `fullcalendar/chunks/642eba18.js`), so the scheduler reads its `resourceId` and `resourceIds` and refuses every other resource (`filterConstraints` in `fullcalendar-scheduler/chunks/0a9e97be.js`); docs `events/eventConstraint.md`, section *Resources*. No window form of `ConstraintValue` carries resource ids. Found by 42 (2026-10-16). Decide with the maintainer where the resource ids go (a scheduler form, or resource ids on the windows), following *Value types* in `README.md`.

**Decided by the maintainer (triage, 2026-10-17):** the resource ids belong to the whole `ConstraintValue`, not to each window: `ConstraintValue.ofResources(String...)` sends `{resourceIds: [...]}`, and `withResources(String...)` on a window constraint writes the same ids onto every window object. Ids are strings, because the core module does not know `Resource`; the Javadoc says the ids take effect with the scheduler only. Reason: `filterConstraints` refuses a resource for the whole constraint as soon as one window names other resources, so the library's effect of different ids per window is their intersection on every window, which this form expresses. The one case it cannot send is an empty intersection, which blocks every resource. The maintainer's condition: resource constraints map 1:1 in effect. `resourceId` (one id) is the same as `resourceIds` with one id and gets no form of its own. Different hours per resource are not this ticket: they come from the business hours of each resource and the constraint `businessHours` (`ResourceSplitter` in `0a9e97be.js`), which the add-on already supports.

**Blocked by:** `61-constraint-rrule-window`

**Status:** done (2026-10-17)

- [x] `ConstraintValue.ofResources(...)` and `withResources(...)` reach the client as `resourceIds`, alone or on every window.
- [x] The setter table in `README.md` has the new form or setter; the converter tests include it.

**Done (2026-10-17):** a form `ConstraintValue.Resources` (`ofResources`) and a second component `resourceIds` on `ConstraintValue.Windows`, set through `withResources`, which returns a copy because the record is immutable. `ofWindows` returns `Windows`, so `withResources` can follow it. The resources have no setter per form: `setX(String...)` with one id would call the group id setter `setX(String)`. Reading back rejects windows with different ids. Found in the review and checked in `isPropsValidWithResources` and `ResourceSplitter` (`0a9e97be.js`): with the scheduler, an entry or selection without a resource has the key `''`, which no id list contains, so a resource constraint refuses it, also in a view without resources; an entry with several resources is checked per resource. The Javadoc says so. Review: three rounds with three agents (round 1 one major, the claim "resource views only", fixed). Rejected: a Playwright test, because the add-on tests its wiring at the JSON level and the library behaviour was checked in its source. Gate: `mvn clean install` green; E2E 463 passed, 2 failed, the date-dependent theme drag test of 76.
