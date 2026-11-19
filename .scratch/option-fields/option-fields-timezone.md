# Time zone as an option field

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement |
| Status | decided (maintainer, 2026-10-15), merged into `option-fields-spec.md` (newer; API there is `options().setTimeZone(…)`) |
| Blocked by | `option-fields-design.md` |

---

## Why

With options as fields, `null` means the library default (`option-fields-design.md`, point 5). For the time zone that is `local`, the time zone of the browser (maintainer, 2026-10-15). The server does not know that zone, but needs one: `getTimezoneForOffsets()` converts the offsets of entries and events with it and today falls back to UTC.

## Questions (all settled, see "Decided")

- How the server gets the browser zone when the field is `null` (today `withAutoBrowserTimezone()` asks the client and stores the answer).
- What the offset helpers do before the browser has answered.
- Whether `withAutoBrowserTimezone()` stays or `setTimezone(null)` replaces it.
- The client default `timeZone: 'UTC'` in `createInitOptions` goes away; what the field's initial value is (UTC, as today, or `null`).

## Decided

- **`Timezone.LOCAL` instead of `null` (maintainer, 2026-10-15).** `Timezone` gets a constant `LOCAL` (client value `local`). The setter rejects `null`, as `setTimezone` already does today. The time zone is an exception to "null = library default" in `option-fields-design.md`, point 5. This settles "whether `setTimezone(null)` replaces `withAutoBrowserTimezone()`": no, `setTimezone(Timezone.LOCAL)` is the candidate.
- **`LOCAL` is resolved, as today (maintainer, 2026-10-15).** `setTimezone(Timezone.LOCAL)` asks the browser; afterwards `getTimezone()` and the client hold the concrete zone. `withAutoBrowserTimezone()` becomes a synonym. Rejected: keeping `local` on the client and fetching the zone separately for the server, because then the getter cannot say which zone the server computes with.
- **Before the browser answers: UTC, no extra code (maintainer, 2026-10-15).** The client reports its zone when it creates the calendar, so the gap covers only server code that runs before the first round trip (view constructor, browserless tests). The offset helpers keep today's UTC fallback; the Javadoc of `LOCAL` says so.
- **Initial value `LOCAL`, the library default (maintainer, 2026-10-15).** The add-on's client default `UTC` goes away. Whoever wants UTC sets `Timezone.UTC`. Breaking change for every application that sets no time zone: displayed times move to the browser zone; the 8.0 migration guide says so. Rejected: UTC as initial value (today's add-on default).
