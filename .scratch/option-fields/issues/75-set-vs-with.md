# 75: Review `set…` versus `with…`

| | |
|---|---|
| GitHub | – |
| Labels | enhancement, needs-triage |
| Status | needs-triage |
| Blocked by | `72-resource-column-forms` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A review of where the add-on's public API uses `set…` setters and where fluent `with…` methods (for example `ResourceColumn.withWidth`, the options objects' `set…`), which rule decides it and why, and whether they should be aligned. The result is a written rule, with the maintainer's decision, and a list of the places that break it. Raised by the maintainer during the triage of 73 (2026-10-17).

**Blocked by:** `72-resource-column-forms`, because 72 changes the `with…` methods of `ResourceColumn`.

**Status:** needs-triage

- [ ] Every public type with `set…` or `with…` methods is listed with the style it uses.
- [ ] The rule and its reason are written down in `STYLEGUIDE.md`, decided by the maintainer.
- [ ] The places that break the rule have their own tickets or are changed.
