# 72: All column settings of `resourceColumns`

| | |
|---|---|
| GitHub | #403 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-17, e28bc813) |
| Blocked by | `38-scheduler-resource-columns`, `40-scheduler-hooks-cells-columns` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A resource column (`ResourceColumn`) takes every setting of the library's `ColSpec` (`fullcalendar-scheduler/chunks/dc3ec45f.d.ts`) that can come from the server: missing are `headerInnerClass`, `cellInnerClass`, `headerResizerClass` and `isMain` (the column that carries the indent of hierarchical rows, `8f1316dc.js`), the `{html}` form of `headerContent` and `cellContent`, and a width in pixels as a number. The library reads a width string only when it ends in `px` or `%` (`parseDimConfig` in `@full-ui/headless-grid`), so the Javadoc of `withWidth(String)` ("any CSS width value") is wrong. Class and content settings could take the value types `ClassNameValue` and `ContentValue`, the width `WidthValue`, as the option fields do. `headerDefault` (a React node) cannot come from the server. Found by 42 (2026-10-16). Decide the shape with the maintainer.

**Decided by the maintainer (triage, 2026-10-17):** `ResourceColumn` takes the value types of the option fields, with the same shortcut overloads as the setter table in `README.md`: content settings (`headerContent`, `cellContent`) take `ContentValue` plus `String` (text), so HTML goes through `ContentValue.Html`; class settings (`headerClass`, `headerInnerClass`, `headerResizerClass`, `cellClass`, `cellInnerClass`) take `ClassNameValue` plus `String`; the width takes `WidthValue` plus `int` (pixels). `isMain` gets `withMain(boolean)`. The overloads taking `JsCallback` for content and class settings go, because the value types hold the callback as a form. `headerContent` gets a setter (today only the constructor sets it, as text). The maintainer's condition: the column maps the library's `ColSpec` 1:1. Left out, because they cannot come from the server: `headerDefault` (a React node) and the `{domNodes}` content form (as for every `ContentValue`). The constructor Javadoc "plain text or HTML" is wrong too: a string is shown as text.

**Decided by the maintainer (2026-10-17, ticket 75):** the `with…` methods move into an abstract base with a self type, as `RemoteEntrySource<S>` does: `AbstractResourceColumn<S extends AbstractResourceColumn<S>>` holds every `with…` method and returns `self()`; `ResourceColumn extends AbstractResourceColumn<ResourceColumn>` and `ComponentResourceColumn<T> extends AbstractResourceColumn<ComponentResourceColumn<T>>`. The covariant overrides in `ComponentResourceColumn` go. `setResourceColumns` takes `AbstractResourceColumn<?>`. Check what this means for the deprecated `ResourceAreaColumn` and `ComponentResourceAreaColumn`.

**Blocked by:** `38-scheduler-resource-columns`, `40-scheduler-hooks-cells-columns`

**Status:** done (2026-10-17, e28bc813)

- [x] Each listed `ColSpec` setting can be set on a `ResourceColumn` and reaches the client.
- [x] A `ComponentResourceColumn` keeps its binding and its own cell content.
- [x] `ResourceColumn` and `ComponentResourceColumn` share a self-typed base; a chain keeps the subtype, and `ComponentResourceColumn` has no covariant overrides.
- [x] The `withWidth` Javadoc says what the library reads, and the constructor Javadoc no longer promises HTML.
- [x] The `JsCallback` overloads of the content and class settings are gone; `withHeaderContent` exists.

**Done (2026-10-17):** `AbstractResourceColumn<S>` holds the settings of `ColSpec` apart from the cell content and its hooks, with `with…` methods returning `self()`; `ResourceColumn` adds `cellContent`, `cellDidMount` and `cellWillUnmount`; `ComponentResourceColumn<T>` writes those itself and has no `with…` method of its own. The columns write their values with the options' converters (`ContentValueConverter`, `ClassNameValueConverter`, `WidthValueConverter`). `setResourceColumns` and the field take `AbstractResourceColumn<?>`.

**Deviation from the decision, accepted by the maintainer (2026-10-17):** `withMain(boolean)` is left out. The wiki is to say so later, when the docs are rewritten (noted in 52), not in *Samples*. The library overwrites `isMain` of every column in `processColOptions` (`fullcalendar-scheduler/chunks/8f1316dc.js`, about line 2078: the first column without `group` gets `true`, every other `false`), and no other code reads it from the column, so a setter would have no effect. The Javadoc of `AbstractResourceColumn` says so.

**Deprecated classes:** `ResourceAreaColumn` and `ComponentResourceAreaColumn` keep extending `ResourceColumn` and `ComponentResourceColumn` with covariant overrides of the `with…` methods 7.x had, plus the deprecated `withWidth(String)` (a whole number of pixels or a percentage; another string leaves the width unset, as the library ignored it) and the `…ClassNames` methods. A callback for content or classes still compiles, because `JsCallback` implements the value types. The getters return the value types; the migration guide says so.

Wiki (branch 8.0): `Samples.md` and `Features.md` use `withWidth(int)`; the migration guide's resource column section describes the shared base, the width and the getter types. Review: three rounds with three agents (round 1 two majors in the migration guide, fixed). Gate: `mvn clean install` green; E2E 463 passed, 2 failed, the date-dependent theme drag test of 76.
