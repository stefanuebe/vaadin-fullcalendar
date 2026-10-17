# 72: All column settings of `resourceColumns`

| | |
|---|---|
| GitHub | – |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `38-scheduler-resource-columns`, `40-scheduler-hooks-cells-columns` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A resource column (`ResourceColumn`) takes every setting of the library's `ColSpec` (`fullcalendar-scheduler/chunks/dc3ec45f.d.ts`) that can come from the server: missing are `headerInnerClass`, `cellInnerClass`, `headerResizerClass` and `isMain` (the column that carries the indent of hierarchical rows, `8f1316dc.js`), the `{html}` form of `headerContent` and `cellContent`, and a width in pixels as a number. The library reads a width string only when it ends in `px` or `%` (`parseDimConfig` in `@full-ui/headless-grid`), so the Javadoc of `withWidth(String)` ("any CSS width value") is wrong. Class and content settings could take the value types `ClassNameValue` and `ContentValue`, the width `WidthValue`, as the option fields do. `headerDefault` (a React node) cannot come from the server. Found by 42 (2026-10-16). Decide the shape with the maintainer.

**Decided by the maintainer (triage, 2026-10-17):** `ResourceColumn` takes the value types of the option fields, with the same shortcut overloads as the setter table in `README.md`: content settings (`headerContent`, `cellContent`) take `ContentValue` plus `String` (text), so HTML goes through `ContentValue.Html`; class settings (`headerClass`, `headerInnerClass`, `headerResizerClass`, `cellClass`, `cellInnerClass`) take `ClassNameValue` plus `String`; the width takes `WidthValue` plus `int` (pixels). `isMain` gets `withMain(boolean)`. The overloads taking `JsCallback` for content and class settings go, because the value types hold the callback as a form. `headerContent` gets a setter (today only the constructor sets it, as text). The maintainer's condition: the column maps the library's `ColSpec` 1:1. Left out, because they cannot come from the server: `headerDefault` (a React node) and the `{domNodes}` content form (as for every `ContentValue`). The constructor Javadoc "plain text or HTML" is wrong too: a string is shown as text.

**Blocked by:** `38-scheduler-resource-columns`, `40-scheduler-hooks-cells-columns`

**Status:** ready-for-agent

- [ ] Each listed `ColSpec` setting can be set on a `ResourceColumn` and reaches the client.
- [ ] A `ComponentResourceColumn` keeps its binding and its own cell content.
- [ ] The `withWidth` Javadoc says what the library reads, and the constructor Javadoc no longer promises HTML.
- [ ] The `JsCallback` overloads of the content and class settings are gone; `withHeaderContent` exists.
