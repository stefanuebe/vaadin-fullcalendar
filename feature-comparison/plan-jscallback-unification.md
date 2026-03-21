# Plan: JsCallback Wrapper — Unify Option + CallbackOption API

## Problem

### 1. Broken nested callbacks
JS function strings inside composite JSON objects are sent as plain strings to FC, which expects function objects. Affected:
- `ResourceAreaColumn.toJson()`: `headerClassNames`, `headerDidMount`, `headerWillUnmount`, `cellContent`, `cellClassNames`, `cellDidMount`, `cellWillUnmount`
- `ClientSideEventSource.addCommonToJson()`: `eventAllow`, `success`, `eventDataTransform`

These bypass the `setCallbackOption` path which does `new Function("return " + s)()` — so the callbacks silently do nothing.

### 2. Confusing dual API
Some FC options exist as both `Option.X` and `CallbackOption.X` (e.g. `ENTRY_OVERLAP`, `SELECT_OVERLAP`, `DROP_ACCEPT`, `VALID_RANGE`, `ENTRY_ORDER`, `FIXED_MIRROR_PARENT`, `MORE_LINK_CLICK`). The developer has to know which enum + method to use based on whether they're passing a static value or a function — even though it's the same FC option.

### 3. Two separate API methods for one concept
```java
calendar.setOption(Option.EDITABLE, true);                              // plain value
calendar.setCallbackOption(CallbackOption.ENTRY_DID_MOUNT, "function(arg) { ... }"); // function
```
This split is artificial. The value type (plain vs. function) should determine the behavior, not the method signature.

## Solution: `JsCallback` wrapper type

### Core idea
Introduce `JsCallback` as a Java-side marker that tells the client "evaluate this string as a JavaScript function". All option-setting goes through `setOption`. No more `setCallbackOption`.

```java
// Before (two paths)
calendar.setOption(Option.ENTRY_OVERLAP, false);
calendar.setCallbackOption(CallbackOption.ENTRY_OVERLAP, "function(a, b) { return a.id !== b.id; }");

// After (one path)
calendar.setOption(Option.ENTRY_OVERLAP, false);
calendar.setOption(Option.ENTRY_OVERLAP, JsCallback.of("function(a, b) { return a.id !== b.id; }"));
```

Works automatically in nested objects too:
```java
new ResourceAreaColumn("title", "Name")
    .withCellDidMount(JsCallback.of("function(info) { info.el.title = 'Tooltip'; }"));
```

---

## Design

### `JsCallback` class

```java
package org.vaadin.stefan.fullcalendar;

/**
 * Wraps a JavaScript function string for safe transport from Java to the client.
 * When the client encounters a JsCallback value in option JSON, it evaluates the
 * string via {@code new Function("return " + jsFunction)()} to produce a real
 * JS function object before passing it to FullCalendar.
 *
 * <p><b>CSP note:</b> This class uses {@code new Function()} on the client side, which requires
 * {@code unsafe-eval} in the Content Security Policy. This is compatible with Vaadin's default
 * CSP setup (which already requires {@code unsafe-eval}), but is <b>incompatible with Vaadin's
 * experimental strict CSP mode</b>. This is not a new limitation — the previous
 * {@code setCallbackOption} API had the same requirement.
 *
 * <p>Example:
 * <pre>{@code
 * calendar.setOption(Option.ENTRY_DID_MOUNT,
 *     JsCallback.of("function(arg) { arg.el.title = arg.event.title; }"));
 * }</pre>
 */
public class JsCallback implements Serializable {

    private final String jsFunction;
    private final boolean injectCustomProperties;

    private JsCallback(String jsFunction, boolean injectCustomProperties) {
        Objects.requireNonNull(jsFunction);
        this.jsFunction = jsFunction.strip();
        this.injectCustomProperties = injectCustomProperties;
        // No syntactic validation — the developer is responsible for the JS they write.
        // Server-side regex checks give false security (can't prevent runtime SyntaxErrors)
        // and reject valid patterns (multi-line IIFEs, destructuring arrows, etc.).
        // Invalid JS will produce a clear error in the browser console at evaluation time.
    }

    /**
     * Creates a JsCallback without custom property injection.
     * Returns {@code null} if {@code jsFunction} is {@code null} (convenience for
     * "clear callback" patterns: {@code setOption(Option.X, JsCallback.of(null))} clears the option).
     */
    public static JsCallback of(String jsFunction) {
        return jsFunction == null ? null : new JsCallback(jsFunction, false);
    }

    /**
     * Creates a JsCallback with explicit control over custom property injection.
     * When {@code true}, the client wraps the function so that {@code event.getCustomProperty()}
     * is available on entry/event arguments — matching the behavior of entry render hooks.
     * <p>
     * For well-known entry callback keys (eventDidMount, eventContent, etc.) this injection
     * happens automatically regardless of this flag. This flag is for custom or less common
     * option keys where injection is desired but not automatic.
     */
    public static JsCallback of(String jsFunction, boolean injectCustomProperties) {
        return jsFunction == null ? null : new JsCallback(jsFunction, injectCustomProperties);
    }

    public String getJsFunction() { return jsFunction; }
    public boolean isInjectCustomProperties() { return injectCustomProperties; }
}
```

### JSON serialization — how `JsCallback` becomes the wire marker

`JsCallback` is detected in `callOptionUpdate` (the Java-side method that sends options to the client),
following the same pattern as `ClientSideValue`. When the value is a `JsCallback`, it is NOT serialized
via Jackson. Instead, `callOptionUpdate` converts it to the marker JSON explicitly.

**CRITICAL: The JsCallback→marker conversion must happen BEFORE the attached/not-attached branch.**
`callOptionUpdate` has two paths:
- **Attached** (calendar already in DOM): calls `getElement().callJsFunction("setOption", key, value)`
- **Not attached** (pre-attach): stores value in `initialOptions` via `JsonUtils.toJsonNode(value)`

If the `JsCallback` detection is placed only inside the attached branch, pre-attach callbacks
silently break — `JsonUtils.toJsonNode(JsCallback)` would produce garbage JSON (serializing the
Java object fields literally), and the client would never see the `__jsCallback` marker.

Correct approach — convert at the top of the method, before the branch:

```java
// In callOptionUpdate — FIRST thing, before attached/not-attached split:

// 1. ENTRY_DID_MOUNT intercept: detect this key and route to merge logic instead of
//    the normal path. This MUST be here (not in setCallbackOption, which is being removed).
//    See "Fix applyEntryDidMountMerge" section for details.
if (Option.ENTRY_DID_MOUNT.getOptionKey().equals(key) && value instanceof JsCallback cb) {
    userEntryDidMountCallback = cb;
    applyEntryDidMountMerge();
    return; // do NOT fall through to normal option handling
}

// 2. Preserve original JsCallback for getOption() round-trip via serverSideOptions.
//    MUST happen BEFORE converting to marker, otherwise getOption() returns the ObjectNode marker.
Object valueToSend = value;
if (value instanceof JsCallback cb) {
    if (valueForServerSide == null) {
        valueForServerSide = cb;  // store original JsCallback for getOption()
    }
    valueToSend = cb.toMarkerJson();  // ObjectNode with __jsCallback marker
}

// 3. Then the existing attached/not-attached logic uses valueToSend instead of value:
if (isAttached()) {
    getElement().callJsFunction("setOption", key, valueToSend);
} else {
    // store in initialOptions — valueToSend is already an ObjectNode marker
    initialOptions.set(key, JsonUtils.toJsonNode(valueToSend));
}
```

The server-side `options` map stores the marker `ObjectNode` (for reattachment serialization).
The `serverSideOptions` map stores the original `JsCallback` instance — `getOption()` checks
`serverSideOptions` first, so callers get back the original wrapper, not the marker.

**Note on `callJsFunction` with `ObjectNode`**: Vaadin's `callJsFunction` already handles Jackson
`JsonNode`/`ObjectNode` arguments correctly — the existing `restoreStateFromServer` call (line ~286
in `FullCalendar.java`) passes an `ObjectNode` built from `JsonUtils.toJsonNode()`. So passing
`cb.toMarkerJson()` (an `ObjectNode`) to `callJsFunction` follows the established pattern.

For **nested objects** (e.g. `ResourceAreaColumn.toJson()`), the `toJson()` method does the same
detection when serializing fields:

```java
// In ResourceAreaColumn.toJson():
if (cellDidMount != null) {
    json.set("cellDidMount", cellDidMount.toMarkerJson()); // produces { "__jsCallback": "..." }
}
```

Add a `toMarkerJson()` method to `JsCallback`:
```java
public ObjectNode toMarkerJson() {
    ObjectNode marker = JsonFactory.createObject();
    marker.put("__jsCallback", jsFunction);
    marker.put("__injectCustomProperties", injectCustomProperties);
    return marker;
}
```

This avoids any Jackson serializer magic — the marker is produced explicitly at known call sites.

### Client-side handling

Add a utility function in `full-calendar.ts`:

```typescript
/**
 * Well-known FC option keys where entry/event arguments automatically receive
 * getCustomProperty() injection. This matches the current setCallbackOption behavior.
 *
 * MAINTENANCE: Keep in sync with the Option enum constants that pass event/entry objects
 * to the callback. When adding a new Option constant whose callback receives event
 * arguments with custom properties, add its client-side key here.
 */
const KNOWN_INJECTION_KEYS = new Set([
    "eventContent", "eventDidMount", "eventClassNames", "eventWillUnmount",
    "eventOverlap", "eventAllow", "selectOverlap"
]);

/**
 * Recursively walks a value and evaluates any JsCallback markers.
 * A JsCallback marker is an object with a "__jsCallback" string property.
 */
function evaluateCallbacks(value: any, knownInjectionKeys?: Set<string>, currentKey?: string): any {
    if (value == null || typeof value !== 'object') return value;

    // JsCallback marker object
    if (typeof value.__jsCallback === 'string') {
        const fn = new Function("return " + value.__jsCallback)();
        const needsInjection = value.__injectCustomProperties === true
            || (currentKey && knownInjectionKeys?.has(currentKey));
        if (needsInjection) {
            return wrapWithCustomPropertyInjection(fn, currentKey);
        }
        return fn;
    }

    // Array — recurse into elements.
    // NOTE: currentKey is intentionally NOT passed here. Array elements are not named options,
    // so key-based injection does not apply to them. If a callback inside an array element needs
    // injection, use JsCallback.of(fn, true) on the Java side (flag-based).
    if (Array.isArray(value)) {
        return value.map((item, i) => evaluateCallbacks(item, knownInjectionKeys));
    }

    // Plain object — recurse into properties
    const result: any = {};
    for (const key of Object.keys(value)) {
        result[key] = evaluateCallbacks(value[key], knownInjectionKeys, key);
    }
    return result;
}
```

### Client-side call sites (all 5)

**IMPORTANT**: The TS component has two `setOption` scopes that must not be confused:
- `this.setOption(key, value)` — the web component's own method (line ~641)
- `calendar.setOption(key, value)` — the FC Calendar instance's method (monkey-patched in `initCalendar`)

Both ultimately call FC's `calendar.setOption`, but `setOptions` (bulk) bypasses `this.setOption` and
calls `calendar.setOption` directly. `evaluateCallbacks` must run before FC sees the value in ALL paths.

1. **`this.setOption(key, value)`** (line ~641) — `value = evaluateCallbacks(value, KNOWN_INJECTION_KEYS, key)` before `calendar.setOption(key, value)` at line ~651. This is the per-option path used by `callJsFunction("setOption", ...)`.

2. **`setOptions(options)` (bulk path)** (line ~628) — inside the `for (let key in options)` loop, apply `value = evaluateCallbacks(value, KNOWN_INJECTION_KEYS, key)` before `calendar.setOption(key, value)` at line ~636. This method is called by `restoreStateFromServer` for reattachment. The fix MUST go in `setOptions` itself (not only in `restoreStateFromServer`) because other callers may also use `setOptions` directly.

3. **`addEventSource(sourceJson)`** — `config = evaluateCallbacks({...sourceJson}, KNOWN_INJECTION_KEYS)` before passing to `calendar.addEventSource(config)`

4. **Initial options processing (`createInitOptions` / `initCalendar`)** — apply `evaluateCallbacks` to the merged initial options object BEFORE passing it to `new Calendar(el, options)`. The FC Calendar constructor receives the options — markers must already be evaluated at that point. The existing `eventContent` string special-case (line ~207-209) becomes redundant and can be removed since `evaluateCallbacks` handles it generically.

5. **`updateResource` in `full-calendar-scheduler.ts`** (line ~67-81) — per-resource callback values (`eventAllow`, `eventClassNames`, etc.) are passed via `resource.setProp(key, value)`. Apply `evaluateCallbacks` to each callback value before `resource.setProp()`:
   ```typescript
   if (data.eventAllow !== undefined)
       resource.setProp('eventAllow', evaluateCallbacks(data.eventAllow, KNOWN_INJECTION_KEYS, 'eventAllow'));
   ```

### `@PreserveOnRefresh` lifecycle note

In `@PreserveOnRefresh` scenarios (browser refresh, same tab):
- Java state is preserved (all fields survive including `serverSideOptions` with `JsCallback` instances)
- TS element is re-created from scratch → `initCalendar()` runs again
- `initialOptions` property is re-synced by Vaadin Flow from the element property
- `onAttach` fires with `!isInitialAttach()` → `restoreStateFromServer` → `setOptions` bulk path
- `applyEntryDidMountMerge()` is called to re-send the merged function

All paths are covered by the 5 call sites above. No special handling needed for `@PreserveOnRefresh`
as long as call sites 2 and 4 are correctly wired.

### Remove `setFixedMirrorParent` TS method

The dedicated `setFixedMirrorParent(s)` method in `full-calendar.ts` (line 770-776) manually calls
`new Function("return " + s)()`. This is now redundant — the normal `setOption` path with
`evaluateCallbacks` handles `JsCallback` markers automatically. Remove this TS method. The Java-side
`Option.FIXED_MIRROR_PARENT` continues to work:
- Static expression: `setOption(Option.FIXED_MIRROR_PARENT, JsCallback.of("document.body"))` — client evaluates via marker
- To clear: `setOption(Option.FIXED_MIRROR_PARENT, null)`

Note: The existing TS `setFixedMirrorParent` evaluates the expression and passes the resulting DOM
element to FC. With the `JsCallback` path, the function string itself is set as the FC option. FC's
`fixedMirrorParent` accepts either a DOM element or a function returning one. So the JsCallback value
should be a function: `JsCallback.of("function() { return document.body; }")`.

Update the `Option.FIXED_MIRROR_PARENT` Javadoc accordingly — it now takes a `JsCallback` with a
function returning a DOM element, not a raw expression string.

### Custom property injection: key-based + flag-based (both)

- **Key-based** (automatic): For the well-known keys in `KNOWN_INJECTION_KEYS`, injection happens automatically — no developer action needed. This matches today's behavior.
- **Flag-based** (opt-in): For any other key, `JsCallback.of(fn, true)` explicitly requests injection. This covers edge cases where a developer uses `setOption(String, JsCallback)` with a custom/less-common key and wants entry data accessible.

Both mechanisms coexist. Key-based is the default experience; the flag is an escape hatch.

---

## API Migration

### Merge CallbackOption into Option, SchedulerCallbackOption into SchedulerOption

Every `CallbackOption` constant moves into `Option`. Same for scheduler. The Javadoc on each constant documents both usage patterns:

```java
/**
 * Called after an entry element is added to the DOM. Use for post-render setup (e.g., tooltips).
 * <dl>
 *   <dt>Static value</dt> <dd>not applicable — this option only accepts a {@link JsCallback}</dd>
 *   <dt>Function</dt>     <dd>{@code JsCallback.of("function(arg) { arg.el.title = arg.event.title; }")}</dd>
 *   <dt>Arguments</dt>    <dd>{@code {event, el, view}}</dd>
 * </dl>
 * <p>Example:
 * <pre>{@code
 * calendar.setOption(Option.ENTRY_DID_MOUNT,
 *     JsCallback.of("function(arg) { arg.el.title = arg.event.title; }"));
 * }</pre>
 *
 * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventDidMount</a>
 */
ENTRY_DID_MOUNT,
```

For dual-use options (accept both plain value and function):

```java
/**
 * Controls whether entries may overlap during dragging.
 * <dl>
 *   <dt>Static value</dt> <dd>{@code boolean} — {@code false} prevents all overlap</dd>
 *   <dt>Function</dt>     <dd>{@code JsCallback.of("function(stillEvent, movingEvent) { return true; }")}</dd>
 *   <dt>Arguments</dt>    <dd>{@code (stillEvent, movingEvent)}</dd>
 *   <dt>Returns</dt>      <dd>{@code boolean}</dd>
 * </dl>
 *
 * @see <a href="https://fullcalendar.io/docs/eventOverlap">eventOverlap</a>
 */
ENTRY_OVERLAP,
```

### Remove `CallbackOption` enum and `setCallbackOption` methods

1. Move all `CallbackOption` constants into `Option` enum (merge Javadoc)
2. Move all `SchedulerCallbackOption` constants into `SchedulerOption` enum (merge Javadoc)
3. Remove `setCallbackOption(CallbackOption, String)` method from `FullCalendar`
4. Remove `setCallbackOption(SchedulerCallbackOption, String)` method from `FullCalendarScheduler`
5. Remove `setCallbackOption(String, String)` method from `FullCalendar`
6. Remove client-side `setCallbackOption(optionKey, jsFunction)` TS method
7. Remove `CallbackOption` + `SchedulerCallbackOption` enums

**Decision: Remove outright** — never released, no migration needed.

### Update deprecated wrapper methods

Existing deprecated methods that currently delegate to `setCallbackOption` must be updated:

```java
// Before:
@Deprecated
public void setEntryDidMountCallback(String s) {
    setCallbackOption(CallbackOption.ENTRY_DID_MOUNT, s);
}

// After:
@Deprecated
public void setEntryDidMountCallback(String s) {
    setOption(Option.ENTRY_DID_MOUNT, JsCallback.of(s));
}
```

Applies to: `setEntryClassNamesCallback`, `setEntryContentCallback`, `setEntryDidMountCallback`,
`setEntryWillUnmountCallback`, and all scheduler callback wrapper methods in `Scheduler.java` /
`FullCalendarScheduler.java` (`setResourceLabelClassNamesCallback`, etc.).

### Fix `ResourceAreaColumn`

Callback fields use overloaded methods — `String` for static values, `JsCallback` for functions:

```java
private Object cellContent;    // String or JsCallback
private JsCallback cellDidMount;  // only JsCallback (FC only accepts function)

// Static string content
public ResourceAreaColumn withCellContent(String staticContent) {
    this.cellContent = staticContent;
    return this;
}

// Function content
public ResourceAreaColumn withCellContent(JsCallback callback) {
    this.cellContent = callback;
    return this;
}

// Unambiguous callback — only JsCallback makes sense
public ResourceAreaColumn withCellDidMount(String jsFunction) {
    this.cellDidMount = JsCallback.of(jsFunction);
    return this;
}

public ResourceAreaColumn withCellDidMount(JsCallback callback) {
    this.cellDidMount = callback;
    return this;
}
```

FC's "Content Injection Inputs" (`cellContent`, `headerContent`) accept both static strings and
functions. FC's "ClassName Inputs" (`cellClassNames`, `headerClassNames`) accept both string arrays
and functions. The `didMount`/`willUnmount` hooks accept only functions.

Field types:
- `headerContent`: `Object` (String or JsCallback) — String for static, JsCallback for function
- `cellContent`: `Object` (String or JsCallback) — same
- `headerClassNames`: `Object` (String or JsCallback) — String for static class, JsCallback for function
- `cellClassNames`: `Object` (String or JsCallback) — same
- `headerDidMount`, `headerWillUnmount`, `cellDidMount`, `cellWillUnmount`: `JsCallback` only

`toJson()` checks: `if (value instanceof JsCallback cb) json.set(key, cb.toMarkerJson()); else json.put(key, (String) value);`

**Getter return types** must match the new field types. The current getters all return `String`.
After the change:
- `getCellDidMount()`, `getCellWillUnmount()`, `getHeaderDidMount()`, `getHeaderWillUnmount()`:
  return `JsCallback` (was `String`). This is a source-breaking change but acceptable since the
  class was introduced on this branch and never released.
- `getCellContent()`, `getHeaderContent()`: return `Object` (was `String`). Callers must check
  `instanceof String` vs `instanceof JsCallback`. Alternatively, provide two getters each:
  `getCellContentText(): String` and `getCellContentCallback(): JsCallback`, returning null if the
  other type is set. This is cleaner but more verbose.
- `getCellClassNames()`, `getHeaderClassNames()`: same pattern as content — return `Object` or
  provide typed getter pairs.

**Decision**: Use `Object` return type for the mixed fields and `JsCallback` for the function-only
fields. The class is a builder-style config object — getters are primarily used by `toJson()` internally,
not by end users. Keeping it simple with `Object` avoids getter explosion.

```java
public Object getCellContent() { return cellContent; }       // String or JsCallback
public JsCallback getCellDidMount() { return cellDidMount; } // JsCallback only
```

### Fix `ClientSideEventSource`

Change callback fields to `JsCallback` internally. The `withXxx(String)` methods wrap in `JsCallback.of()`.
`addCommonToJson()` serializes via `toMarkerJson()`.

Fields affected: `allow` (→ JsCallback), `success` (→ JsCallback), `eventDataTransform` (→ JsCallback).
`constraint` stays String (it's a groupId string, not a function). `overlap` stays Boolean.

### Fix `applyEntryDidMountMerge` (C2 from code review)

#### Current state
- `entryDidMountCallback` field (String) stores the user's raw JS function string
- `setCallbackOption(ENTRY_DID_MOUNT, s)` stores the string in `entryDidMountCallback` instead of
  the normal options map, then calls `applyEntryDidMountMerge()`
- `applyEntryDidMountMerge()` textually splices the user string + native listener strings into one
  combined function string, then sends it via `callJsFunction("setCallbackOption", "eventDidMount", merged)`
- **Bug**: when `merged == null` (both user callback and native listeners cleared), nothing is sent →
  old callback lingers in the browser

#### New design with JsCallback
- Keep a dedicated `userEntryDidMountCallback` field, but typed as `JsCallback` (not String)
- `setOption(Option.ENTRY_DID_MOUNT, JsCallback.of(...))` detects the ENTRY_DID_MOUNT key, stores
  the JsCallback in `userEntryDidMountCallback` (not in the normal options map), and triggers merge
- `applyEntryDidMountMerge()`:
  1. Extract the JS function string from `userEntryDidMountCallback` (if not null)
  2. Combine with native event listener strings (if any)
  3. If merged is non-null: construct `JsCallback.of(mergedFunctionString)`, then send `getElement().callJsFunction("setOption", "eventDidMount", JsCallback.of(mergedFunctionString).toMarkerJson())`
  4. **If merged is null**: explicitly send `getElement().callJsFunction("setOption", "eventDidMount", (JsonNode) null)` to clear the client-side callback. This fixes the null-clear bug.
- `getOption(Option.ENTRY_DID_MOUNT)` returns `userEntryDidMountCallback` (the original, unmerged JsCallback)
  so the user gets back what they set, not the internal merged version
- On reattachment (`restoreStateFromServer`), `applyEntryDidMountMerge()` is called to re-send the
  merged function — the user's original JsCallback is preserved in the field

---

## Implementation Order

1. **Create `JsCallback` class** — wrapper (no JS validation), `of()` factory, `toMarkerJson()`, CSP Javadoc note
2. **Client-side `evaluateCallbacks`** — recursive marker evaluation utility in TS
3. **Wire into all 5 client call sites** — `this.setOption`, `setOptions` (bulk), `addEventSource`, `createInitOptions`/`initCalendar`, `updateResource` (scheduler TS)
4. **Detect `JsCallback` in `callOptionUpdate`** — ENTRY_DID_MOUNT intercept first, then `valueForServerSide` preservation, then marker conversion, all BEFORE attached/not-attached branch
5. **Migrate `CallbackOption` constants into `Option`** — move all 57 constants, merge Javadoc. **Skip constants that already exist in `Option`** (e.g. `ENTRY_OVERLAP`, `SELECT_OVERLAP`, `DROP_ACCEPT`, `VALID_RANGE`, `ENTRY_ORDER`, `FIXED_MIRROR_PARENT`, `MORE_LINK_CLICK`) — those are deduplicated in step 14 by merging their Javadoc into the existing `Option` constant.
6. **Migrate `SchedulerCallbackOption` constants into `SchedulerOption`** — move all 23 constants
7. **Update deprecated wrapper methods** — `setEntryDidMountCallback` etc. → `setOption(Option.X, JsCallback.of(s))`
8. **Remove `setCallbackOption` methods** — Java (all 3 overloads) + TS
9. **Remove `CallbackOption` + `SchedulerCallbackOption` enums**
10. **Remove `setFixedMirrorParent` TS method** — now handled by `evaluateCallbacks` in `setOption` path
11. **Fix `ResourceAreaColumn`** — overloaded methods (String vs JsCallback), `toJson()` with marker
12. **Fix `ClientSideEventSource`** — JsCallback for callback fields, `addCommonToJson()` with marker
13. **Fix `applyEntryDidMountMerge`** — dedicated `userEntryDidMountCallback: JsCallback` field, null-clear fix
14. **Remove duplicate Option constants** — constants that existed in both `Option` and `CallbackOption` (e.g. `ENTRY_OVERLAP`, `SELECT_OVERLAP`, `DROP_ACCEPT`, `VALID_RANGE`, `ENTRY_ORDER`, `FIXED_MIRROR_PARENT`, `MORE_LINK_CLICK`) are now single entries in `Option` with merged Javadoc
15. **Update docs** — Samples.md, Features.md, Migration-guides.md, Release-notes-7.1.md
16. **Update tests** — adapt existing callback tests, add JsCallback-specific tests
17. **Compile + test** — `mvn test -pl addon,addon-scheduler -am`

---

## Files affected

| File | Changes |
|------|---------|
| `addon/.../JsCallback.java` | **NEW** — wrapper class with validation, factory, toMarkerJson |
| `addon/.../FullCalendar.java` | Merge CallbackOption into Option, remove setCallbackOption, detect JsCallback in callOptionUpdate, adapt ENTRY_DID_MOUNT merge, update deprecated methods |
| `addon-scheduler/.../FullCalendarScheduler.java` | Merge SchedulerCallbackOption into SchedulerOption, remove setCallbackOption |
| `addon-scheduler/.../Scheduler.java` | Update deprecated method @see references and delegation |
| `addon-scheduler/.../ResourceAreaColumn.java` | Overloaded String/JsCallback methods, toJson with marker |
| `addon/.../ClientSideEventSource.java` | JsCallback for callback fields |
| `addon/.../full-calendar.ts` | Add evaluateCallbacks(), wire into setOption/addEventSource/initial/bulk, remove setCallbackOption(), remove setFixedMirrorParent() |
| `addon-scheduler/.../full-calendar-scheduler.ts` | Check if scheduler TS overrides `setOption`, `addEventSource`, or has its own option-passing paths that bypass the base class. If so, wire `evaluateCallbacks` there too. Specifically check: `updateResource` (passes resource data), any scheduler-specific `setOption` override, and `restoreStateFromServer` if overridden. |
| `docs/Samples.md` | Update all setCallbackOption examples to setOption + JsCallback |
| `docs/Features.md` | Update callback documentation |
| `docs/Migration-guides.md` | Update migration tables |
| `docs/release-notes-detail/Release-notes-7.1.md` | Rewrite callback section |
| Tests | Adapt all callback-related tests |

---

## Decisions on open questions

1. **Record vs. class** — Use a **class**, not a record. `JsCallback` needs a private constructor
   and null-returning static factory methods. Records don't support `of(null) → null` naturally
   (compact constructor can't return null). The class is simple enough that the boilerplate
   difference is negligible.

2. **Marker format** — Use `{ "__jsCallback": "...", "__injectCustomProperties": false }` with the
   dunder-prefix convention. Simple, unlikely to collide with any FC option property name.

3. **`JsCallback.of(null)` returns null** — Yes. This enables clean "clear callback" patterns:
   `setOption(Option.ENTRY_DID_MOUNT, JsCallback.of(userInput))` safely clears when `userInput` is null.

4. **No server-side JS validation** — The constructor does NOT validate JS syntax. A regex-based
   `looksLikeFunction()` guard was considered but rejected: it gives false security (can't prevent
   runtime SyntaxErrors), rejects valid patterns (multi-line IIFEs, destructuring arrows), and
   validates at the wrong layer (Java checking JS). Invalid JS produces a clear error in the
   browser console at evaluation time. The developer is responsible for the JS they write.

5. **Custom property injection** — Both key-based and flag-based, coexisting. Key-based is automatic
   for `KNOWN_INJECTION_KEYS`. Flag-based via `JsCallback.of(fn, true)` is the escape hatch.

6. **`Option.FIXED_MIRROR_PARENT` semantics change** — Previously accepted a raw JS expression
   (`"document.body"`) that was evaluated once via `new Function("return " + expr)()` in a dedicated
   TS method. Now accepts a `JsCallback` with a function returning a DOM element:
   `JsCallback.of("function() { return document.body; }")`. The dedicated TS method is removed.
   Update Javadoc to document the new usage.

7. **CSP compatibility** — `new Function()` requires `unsafe-eval` in the Content Security Policy.
   This is compatible with Vaadin's default CSP (which already requires `unsafe-eval`) but
   **incompatible with Vaadin's experimental strict CSP mode**. This is not a new limitation —
   the previous `setCallbackOption` API had the same requirement. Document in `JsCallback` Javadoc.
