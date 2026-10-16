# 73: Locale fallbacks and custom locales

| | |
|---|---|
| GitHub | – |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `16-header-toolbar-and-locale-initial-values` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The `locale` option takes, besides one code, a list of codes that the library tries in order (`LocaleCodeArg = string | string[]`, `queryRawLocale` in `fullcalendar/chunks/71d3d952.js`) and a locale object with a `code` and its own options (`LocaleInput`, `buildLocale`). The field takes one `java.util.Locale`, sent as its lower-case language tag; the client registers all library locales (`locales-all` in `full-calendar.ts`). Found by 42 (2026-10-16). Decide with the maintainer which forms the field takes and how, following *Value types* in `README.md`; the initial value `CalendarLocale.getDefaultLocale()` and the rule that the calendar always has a locale stay.

**Blocked by:** `16-header-toolbar-and-locale-initial-values`

**Status:** ready-for-agent

- [ ] Each form the maintainer chooses reaches the client as the library reads it.
- [ ] The Javadoc of `locale` names the forms.
