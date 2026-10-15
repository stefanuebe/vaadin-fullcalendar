# 38: `resourceColumns` through the sync hook, resource re-add flush

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `03-flush-before-client-calls`, `18-sync-hook-entry-did-mount`, `37-scheduler-options-object` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The hand-written `setResourceColumns` moves to the scheduler options object; component resource columns keep their binding through the hook. The scheduler's resource re-add calls `flushOptions()` first. `resources: []` stays in the scheduler client. Spec: user story 58.

**Blocked by:** `03-flush-before-client-calls`, `18-sync-hook-entry-did-mount`, `37-scheduler-options-object`

**Status:** ready-for-agent

- [ ] Browserless: the hook output for `resourceColumns` keeps component columns.
- [ ] The resource re-add calls `flushOptions()` first.
- [ ] Playwright: component resource columns render after being set through the options object; the scheduler starts with an empty resource list.
