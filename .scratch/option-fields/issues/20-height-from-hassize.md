# 20: `height` from `HasSize` through the sync hook

| | |
|---|---|
| GitHub | #389 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, 9f94986e) |
| Blocked by | `18-sync-hook-entry-did-mount`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `setHeight`/`setHeightFull` write the library option `height` through the hook. A raw `height` wins. The constructor skips `setHeightFull()` when its JSON has `height`, as on master. No `height` field (out of scope). Spec: *Height*, user story 41.

**Blocked by:** `18-sync-hook-entry-did-mount`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16, 9f94986e)

- [x] `setHeight` and the constructor's `setHeightFull()` reach the client.
- [x] `setHeightFull()` is skipped when the constructor JSON has `height`.
- [x] A raw `height` wins over `setHeight`.

**Notes from the implementation (2026-10-16):**

- `setHeight` stores the value in a private field and calls `optionsChanged()`; `contributeOptions` puts it in as `height`. The raw options lie over it, so a constructor JSON or `setRawOption` height wins and removing it brings back the `setHeight` value. `HasSize.setHeightFull`, `setSizeUndefined` and `setHeight(float, Unit)` all route through `setHeight(String)`.
- `getOption(Option.HEIGHT)` no longer shows the height of `setHeight`; it shows only what was passed to `setOption`. `getHeight()` stays null as before, the override never set the element style.
- `HeightSyncTest` covers initial full height, before and after attach, `setSizeUndefined`, the constructor JSON skip (also that `setHeight(null)` keeps the JSON height, as the wiki says) and the raw precedence. With `setHeight` reverted to the raw option route the precedence test fails.
- The vacuous `FullCalendarOptionsTest.undefinedSizeRemovesTheHeight` is removed; the `Option.HEIGHT` Javadoc describes the precedence.
- Gate green: full build, E2E 463 passed.
