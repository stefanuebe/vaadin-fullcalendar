# 65: `slotHeaderFormat` with one format per header tier

| | |
|---|---|
| GitHub | – (local tracker) |
| Labels | enhancement, needs-triage |
| Status | needs-triage (needs a decision by the maintainer) |
| Blocked by | `29-fields-texts-formats` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Field `slotHeaderFormat` (per view). The library types it as `FormatterInput | FormatterInput[]`. A single format covers the time grid and one header row of a timeline. A list gives one format per header tier, so a timeline shows for example days above hours. `DateFormatValue` covers the single form only.

Found by batch 29 (2026-10-16), which left the option out because no value type takes a list of formats.

**Open decision:** a new value type with a list form (named per *Value types* in `README.md`, with its row in the setter table), or the list as a form of `DateFormatValue`, which other options would then accept although the library rejects a list there.

**Blocked by:** `29-fields-texts-formats`

**Status:** needs-triage

- [ ] The field takes every form the library takes, a single format, a callback and a list.
- [ ] The static checks, the pinned lists and the converter round trip include it.
- [ ] A browserless test sets one value per form and finds it in the client state.

**Maintainer (2026-10-16):** "was genau spricht gegen eigenen Wertetyp?" Not answered yet. Answer: nothing speaks against it, the README rule prefers it (a value type per option kind, forms only where the library takes them). Recommendation to give: own value type. Then set the ticket ready-for-agent.
