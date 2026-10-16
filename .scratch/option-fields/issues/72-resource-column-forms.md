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

**Blocked by:** `38-scheduler-resource-columns`, `40-scheduler-hooks-cells-columns`

**Status:** ready-for-agent

- [ ] Each listed `ColSpec` setting can be set on a `ResourceColumn` and reaches the client.
- [ ] A `ComponentResourceColumn` keeps its binding and its own cell content.
- [ ] The `withWidth` Javadoc says what the library reads.
