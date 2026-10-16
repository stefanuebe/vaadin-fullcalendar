# 70: Durations in weeks

| | |
|---|---|
| GitHub | – |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `57-value-types-order-ranges-lengths-durations` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A duration given in weeks reaches the library as weeks. The library marks `{weeks: n}` as given in weeks (`parseObject` in `@full-ui/headless-calendar`), and `greatestDurationDenominator` then returns the unit `week`: `duration` and `dateIncrement` align their range to the start of the week (`fullcalendar/chunks/54e239b5.js`), the scheduler uses it for slot units and labels (`slotDuration`, `slotHeaderInterval`, `fullcalendar-scheduler/chunks/fc014b1a.js`). `{days: 7}` gives the unit `day` and no week alignment. `DurationValue` and its converter write days only, so `Period.ofWeeks(1)` arrives as seven days. The old `DURATION` Type block listed `weeks`, and the Javadoc of the `duration` field already promises the week alignment. Found by 42 (2026-10-16). Decide the form (for example a weeks form of `DurationValue` with its data setter) with the maintainer, following *Value types* in `README.md`.

**Blocked by:** `57-value-types-order-ranges-lengths-durations`

**Status:** ready-for-agent

- [ ] Every field of type `DurationValue` can send a duration in weeks, and the client receives `{weeks: n}`.
- [ ] The setter table in `README.md` has the new form; `ConverterTest` and `ValueTypeSwitchTest` include it.
- [ ] The Javadoc of `duration` and `DurationValue` matches what is sent.
