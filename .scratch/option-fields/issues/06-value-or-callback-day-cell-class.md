# 06: A value-or-callback option: `dayCellClass`

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `05-options-object-first-fields` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The first option with two value forms. `ClassNameValue` is a sealed interface permitting `ClassName` and `JsCallback`; `JsCallback` becomes `final`. The field has a converter and two hand-written setters (`String`, `JsCallback`), no setter taking `ClassNameValue`; the getter returns `ClassNameValue`. Shape as in the prototype (spec, *Types*). Spec: *Options with more than one value form*, user stories 24, 25.

**Blocked by:** `05-options-object-first-fields`

**Status:** ready-for-agent

- [ ] Both setters reach the client in their form; `null` removes the option.
- [ ] A test switches over the getter's value without a `default` branch (compile-time exhaustiveness).
- [ ] Playwright: a class set as string and one returned by a callback both appear on day cells.
- [ ] `SerializationTest` covers a `JsCallback` value.
