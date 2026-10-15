# 09: Option-name resource for the core module

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | None (can start immediately) |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A resource file in `addon` lists every library option name the core add-on supports, generated once from `Option` (before 51 deletes it), corrected where the key differs from the library (`class` becomes `className`), with one flag per name: global or per view. The global names are the two groups in *Types* of the spec (read only from the calendar options by FullCalendar 7.1: `buildDateEnv` and `parseToolbars` inputs, `views`, `plugins`, `initialEvents`, `events`, `eventSources`; global by the add-on's choice: listeners, `viewClass`, `viewDidMount`, `viewWillUnmount`, `className`, `eventDidMount`, `moreLinkClick`). Each group is re-derived from the library source. Carries an `FC-UPDATE:` note. Spec: *Library updates*.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] The resource lists every non-deprecated `Option` key once, with its flag and corrected names.
- [ ] The global flags are checked against the FullCalendar 7.1 source in `e2e-test-app/node_modules/fullcalendar`; the ticket notes where each group was found.
