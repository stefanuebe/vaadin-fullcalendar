# 14: Time zone from constructor JSON, raw time zone rejected

| | |
|---|---|
| GitHub | #385 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, 55884638) |
| Blocked by | `13-timezone-local-server` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Constructor JSON `timeZone` goes into the field (`"local"` becomes `LOCAL` with the flag set, a concrete id becomes that zone; new compared with master, which converted with UTC). `setRawOption("timeZone", …)` throws with a pointer to the field. Spec: *Raw options*.

**Blocked by:** `13-timezone-local-server`

**Status:** done (2026-10-16, 55884638)

- [x] Constructor JSON `"local"` gives `LOCAL`, `"Europe/Berlin"` gives that zone in the getter and the offset helpers.
- [x] `setRawOption("timeZone", …)` throws and names `options().setTimeZone`.

**Note from 13 (2026-10-15):** until 14, a raw `timeZone` (constructor JSON with a concrete id, or `setRawOption`) wins on the client over the `local` the sync sends, while the server converts with the field. The rejection in `setRawOption` and the constructor's move into the field close that gap.

**Notes from the implementation (2026-10-16):**

- The constructor removes `timeZone` from its JSON whenever the key is present. A JSON null is dropped and leaves the field at `LOCAL`. Found by the review: before, a JSON null became a raw option that `setRawOption` could no longer remove.
- `AbstractOptions.setRawOption` throws for `timeZone` on the calendar and on the view options, also for a removal (null).
- Left as it is: the old constructor Javadoc ("not cached in the server side state"), rewritten by the migration tickets; `setOption(Option.TIMEZONE, JsonNode)` throws a `ClassCastException`, legacy API removed by 51.
