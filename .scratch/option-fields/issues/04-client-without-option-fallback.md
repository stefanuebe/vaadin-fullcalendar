# 04: The client keeps no option fallback of its own

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `01-raw-option-sync`, `02-detach-attach-resync` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The client creates its calendar from `initialOptions` plus plugins and client-side handlers only. `_optionsWithoutServer` and the `initialJsonOptions` property go away; a removed option gets the library default, not a value the client remembered. Spec: *Sync* (third bullet).

**Blocked by:** `01-raw-option-sync`, `02-detach-attach-resync`

**Status:** ready-for-agent

- [ ] `_optionsWithoutServer` and `initialJsonOptions` are gone from the client and the server.
- [ ] Removing an option on the server gives the library default on the client.
- [ ] Playwright: `option-reset.spec.js` is adjusted to the library defaults and passes.
