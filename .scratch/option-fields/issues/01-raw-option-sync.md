# 01: Raw options and the new option sync replace the option bookkeeping

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | None (can start immediately) |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The calendar keeps its options in one raw-option map and syncs it in `beforeClientResponse`: before the client is initialized it writes the full state to the element property `initialOptions`, afterwards it sends only the changed keys in one call and a missing key as a removal. `setRawOption(String, Object)` and `getRawOption(String)` become public (expert feature; the value is converted to JSON at once, so a bad value throws at the caller). The old `setOption`/`getOption`/`removeOption` methods and the constructor JSON keep working by writing into this map (with their converters; `getOption` still returns the server-side value). This bridge exists only until 51. The server-side bookkeeping (`options`, `initialOptions` map, `optionsRemovedWhileDetached`, the resend in `onAttach`, `addonDefaultOptions`) goes away. Spec: *Sync*, *Raw options*.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Only changed keys reach the client; a removed key is sent as removal.
- [ ] Several changes in one request arrive in one client call.
- [ ] `setRawOption` with a value that cannot be converted throws at the call.
- [ ] Constructor JSON options are raw options (`views` and `timeZone` stay as on master until 08 and 14).
- [ ] The existing tests that use the old API still pass, apart from tests of the removed bookkeeping, which are deleted with it.
- [ ] `SerializationTest` covers a calendar with raw options.
