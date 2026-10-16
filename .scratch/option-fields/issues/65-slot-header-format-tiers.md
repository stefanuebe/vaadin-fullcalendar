# 65: `slotHeaderFormat` with one format per header tier

| | |
|---|---|
| GitHub | #378 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, 3ef5605e) |
| Blocked by | `29-fields-texts-formats` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Field `slotHeaderFormat` (per view). The library types it as `FormatterInput | FormatterInput[]`. A single format covers the time grid and one header row of a timeline. A list gives one format per header tier, so a timeline shows for example days above hours. `DateFormatValue` covers the single form only.

Found by batch 29 (2026-10-16), which left the option out because no value type takes a list of formats.

**Open decision:** a new value type with a list form (named per *Value types* in `README.md`, with its row in the setter table), or the list as a form of `DateFormatValue`, which other options would then accept although the library rejects a list there.

**Blocked by:** `29-fields-texts-formats`

**Status:** done (2026-10-16, 3ef5605e)

- [x] The field takes every form the library takes, a single format, a callback and a list.
- [x] The static checks, the pinned lists and the converter round trip include it.
- [x] A browserless test sets one value per form and finds it in the client state.

**Decided (maintainer, 2026-10-16):** an own value type for `slotHeaderFormat`, with the single format, the callback and the list as its forms, named and built per *Value types* in `README.md`, with its row in the setter table. Rejected: the list as a form of `DateFormatValue`, which every other format option would then accept although the library rejects a list there.

**Done (2026-10-16):** `SlotHeaderFormatValue` with `DateFormat`, `JsCallback` and `Tiers` (`of(DateFormatValue...)`), its converter, the field in `ViewOptions` with a `Map` setter and the value-type setter. The time grid uses the first format of a list only, and a timeline without a value picks its formats from its slot duration (library code). 3 review rounds (the third the extra one after a round without major), gate green (656+175 unit, 446 E2E).
