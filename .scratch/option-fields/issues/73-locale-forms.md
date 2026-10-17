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

**Decided by the maintainer (triage, 2026-10-17):** a value type `LocaleValue` with two forms: `LocaleValue.of(Locale)`, sent as today, and `LocaleValue.ofCustom(Locale, Map<String, Object> options)`, sent as `{code, ...options}` (the library's `LocaleInput`). The setter `setLocale(Locale)` stays; `getLocale()` returns `LocaleValue`. The custom locale takes a `Locale` and not a `String`, because the library passes the code to `new Intl.NumberFormat(code)` (`71d3d952.js:99`), which throws on a code that is no valid language tag, and a `Locale` always gives one. Its options are a `Map`, like `DateFormatValue`, because `LocaleInput` takes every calendar option and a typed form would repeat all of them. Left out: the list of codes (`string[]`), because the client registers every library locale and the server knows the same list through `CalendarLocale`, so a fallback can be chosen on the server. Today the custom locale is only reachable through `setRawOption("locale", …)`.

**Blocked by:** `16-header-toolbar-and-locale-initial-values`

**Status:** ready-for-agent

- [ ] `LocaleValue.of(Locale)` and `LocaleValue.ofCustom(Locale, Map)` reach the client as the library reads them; the initial value and the rule that the calendar always has a locale stay.
- [ ] The Javadoc of `locale` names the forms and why the list of codes is left out.
