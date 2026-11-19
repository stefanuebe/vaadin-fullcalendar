# 77: Merging into the user's `entryDidMount` callback

| | |
|---|---|
| GitHub | – |
| Labels | bug, ready-for-agent |
| Status | done (2026-10-17, ba062685, v7_master f3268112) |
| Blocked by | – |
| Spec | – |

---

**What to build:** The add-on builds one `eventDidMount` function from three parts: its snippet that sets the entry id (on by default, `autoProvideEntryIdOnClient`), the user's `entryDidMount` callback and the native entry listeners (`addEntryNativeEventListener`). It merges them by splicing its code between the first `{` and the last `}` of the user's function string (`FullCalendar.buildEntryDidMountMerged`). Found by the review of ticket 67 (D7), the same code is on `v7_master`:

- An arrow function with a block body, `info => { … }`, throws: the spliced snippet reads `arguments[0]`, and an arrow function has no `arguments` of its own. It gets those of the zero-argument wrapper in `full-calendar.ts` (`evaluateCallbacks`), so `arguments[0]` is undefined.
- An arrow function with an expression body, `info => info.el.title = 'x'`, has no braces. The add-on returns it unchanged and drops the id snippet and the native listeners without a word.
- A `{` inside the user's code before its body (a default parameter, a destructured parameter) puts the splice in the wrong place.
- The snippet's `return` for drag mirrors also skips the user's callback, which the library calls for mirrors too.

Fix: wrap instead of splice. The merged function calls the id snippet, then the user's function with `this` and all arguments, then registers the native listeners, for example `function(info) { <snippet as its own function>.call(this, info); var r = (<user>).apply(this, arguments); <listeners>; return r; }`. Any function form works then, and each part keeps its own scope.

**Blocked by:** none

**Status:** done

- [x] An `entryDidMount` given as an arrow function with a block body runs, and the entry ids are set (browser test).
- [x] An `entryDidMount` given as an arrow function with an expression body runs, and the entry ids and native listeners are still set (browser test).
- [x] The user's callback can still override the id the snippet sets.
- [x] Ported back to `v7_master` (about 1:1, committed on the version line directly).

**Outcome:** The merged function does not return the callback's result, because the library ignores the return value of `eventDidMount`. A callback without the id snippet and without native listeners is sent as it is. The callback and the listeners now also run for drag previews, as the library calls `eventDidMount` for them; only the id is left out there. The 8.0 release notes (52–56) and the next 7.x release notes name the fix and the drag-preview change. The released notes of 6.5 and 7.2 still describe the old limitation and stay as published.
