# 13: Time zone field with `Timezone.LOCAL` (server)

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `05-options-object-first-fields` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The field `timeZone` of type `Timezone`, initial value `LOCAL`, the library default. `Timezone.LOCAL` is `new Timezone(ZONE_ID_UTC, "local")`. The hand-written `setTimeZone` on `FullCalendarOptions` rejects `null` and sets or clears a follow-browser flag. While the flag is set, `setBrowserTimezone` (`@ClientCallable`) stores the reported zone and the sync sends `local`, so a report changes nothing on the client. A concrete zone is sent as it is. If the browser zone is known when `LOCAL` is set, it applies at once. The offset helpers use UTC until the report. `withAutoBrowserTimezone()` becomes a synonym for `setTimeZone(LOCAL)`. Spec: *Time zone*, user stories 34–38, 59.

**Blocked by:** `05-options-object-first-fields`

**Status:** ready-for-agent

- [ ] `options().setTimeZone(null)` throws.
- [ ] Before a report the getter returns `LOCAL` and the offset helpers use UTC.
- [ ] A report sets the getter to the concrete zone and sends nothing to the client.
- [ ] With `LOCAL`, a second report replaces the zone; with a concrete zone it does not.
- [ ] `withAutoBrowserTimezone()` has the same effect as `setTimeZone(LOCAL)`.
