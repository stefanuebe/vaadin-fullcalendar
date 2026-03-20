# Fix 103: Cleanup ENTRY_DID_MOUNT / native event listener merge code

## Context

`setCallbackOption(ENTRY_DID_MOUNT, ...)` bypasses the native event listener merge
because the merge logic (`updateEntryDidMountCallbackOnAttach`) reads from a dedicated
`eventDidMountCallback` field, which is only written by the deprecated
`setEntryDidMountCallback`. The generic `setCallbackOption` path goes directly to JS.

This creates several problems:
- Samples.md example combining `setCallbackOption(ENTRY_DID_MOUNT)` + `addEntryNativeEventListener` is silently broken
- `setEntryDidMountCallback` cannot be fully deprecated (users with native listeners are stuck with it)
- `addEntryNativeEventListener` only takes effect on next attach, not immediately
- The `eventDidMountCallback` field and `updateEntryDidMountCallbackOnAttach()` are special-case workaround code

## Goal

Remove the special-case field/method. Route all ENTRY_DID_MOUNT writes through a common
merge path so that both `setCallbackOption(ENTRY_DID_MOUNT, ...)` and
`setEntryDidMountCallback` participate in native listener merging identically.

**Critical constraint: native event listeners via `addEntryNativeEventListener` must continue
to work in all scenarios.**

## Implementation Steps

### Step 1: Add `Map<String, String> callbackOptionStrings`

Add a new private field near `customNativeEventsMap`:

```java
private final Map<String, String> callbackOptionStrings = new LinkedHashMap<>();
```

Stores the raw user-provided JS string for each callback option key.
Only holds the *raw* user callback — never the merged result — so re-merging
after `addEntryNativeEventListener` always reads the original function.

---

### Step 2: Update `setCallbackOption(String, String)` to populate the map

```java
public void setCallbackOption(String optionKey, String jsFunction) {
    if (StringUtils.isNotBlank(jsFunction)) {
        callbackOptionStrings.put(optionKey, jsFunction);
    } else {
        callbackOptionStrings.remove(optionKey);
    }
    // special case: ENTRY_DID_MOUNT must go through merge
    if ("eventDidMount".equals(optionKey)) {
        applyEntryDidMountMerge();
        return;
    }
    if (StringUtils.isNotBlank(jsFunction)) {
        getElement().callJsFunction("setCallbackOption", optionKey, jsFunction);
    } else {
        setOption(optionKey, null);
    }
}
```

---

### Step 3: New private `applyEntryDidMountMerge()`

Replaces `updateEntryDidMountCallbackOnAttach()`. Reads from `callbackOptionStrings`
instead of the old `eventDidMountCallback` field. Safe to call at any time.

```java
private void applyEntryDidMountMerge() {
    if (!isAttached()) {
        return; // onAttach will call this
    }
    String userCallback = callbackOptionStrings.get("eventDidMount");

    StringBuilder events = null;
    if (!customNativeEventsMap.isEmpty()) {
        events = new StringBuilder();
        for (Map.Entry<String, String> entry : customNativeEventsMap.entrySet()) {
            events.append("arguments[0].el.addEventListener('")
                    .append(entry.getKey()).append("', ")
                    .append(entry.getValue()).append(")\n");
        }
    }

    String merged = null;
    if (StringUtils.isNotBlank(userCallback)) {
        if (events != null) {
            int index = userCallback.lastIndexOf("}");
            merged = userCallback.substring(0, index) + events + userCallback.substring(index);
        } else {
            merged = userCallback;
        }
    } else if (events != null) {
        merged = "function(info) {\n" + events + "}";
    }

    if (merged != null) {
        getElement().callJsFunction("setCallbackOption", "eventDidMount", merged);
    }
}
```

---

### Step 4: Update `onAttach`

Replace the call to `updateEntryDidMountCallbackOnAttach()` with `applyEntryDidMountMerge()`.
No structural changes to the `onAttach` method needed.

---

### Step 5: Update `addEntryNativeEventListener` to re-merge immediately when attached

```java
public void addEntryNativeEventListener(String eventName, String eventCallback) {
    customNativeEventsMap.put(eventName, eventCallback);
    applyEntryDidMountMerge(); // no-op if not attached
}
```

Previously, adding a native listener after attach had no immediate effect.

---

### Step 6: Update `setEntryDidMountCallback` to delegate

```java
@Deprecated(forRemoval = true)
public void setEntryDidMountCallback(String s) {
    setCallbackOption(CallbackOption.ENTRY_DID_MOUNT, s);
}
```

Remove the direct `eventDidMountCallback = s` field assignment.

---

### Step 7: Remove dead code

- Remove field: `private String eventDidMountCallback;`
- Remove method: `private void updateEntryDidMountCallbackOnAttach()`

---

### Step 8: Update Javadocs

- `setEntryDidMountCallback`: remove "must be called before attach" restriction; remove warning about native listener bypass; update deprecation note (delegation now works correctly in all cases)
- `addEntryNativeEventListener`: remove reference to `setEntryDidMountCallback` being required; state that native listeners work with `setCallbackOption(ENTRY_DID_MOUNT, ...)` too and take effect immediately even after attach
- `setCallbackOption(CallbackOption, String)`: add note that `ENTRY_DID_MOUNT` is automatically merged with any native event listeners

---

### Step 9: Update docs

- `docs/Samples.md`: fix the "combining ENTRY_DID_MOUNT + addEntryNativeEventListener" example — replace `setEntryDidMountCallback` with `setCallbackOption(FullCalendar.CallbackOption.ENTRY_DID_MOUNT, ...)`
- `docs/Migration-guides.md`: remove the note "continue using `setEntryDidMountCallback` if you also use native event listeners" — the limitation no longer exists

---

### Step 10: Unit tests (`CallbackOptionTest`)

Add test cases:

| Test | Verifies |
|---|---|
| `setCallbackOption(ENTRY_DID_MOUNT, fn)` stores in `callbackOptionStrings` | Step 2 |
| `setEntryDidMountCallback(fn)` delegates to merge path | Step 6 |
| Merge: only native listeners → generates wrapper `function(info){...}` | Step 3 |
| Merge: user callback + native listeners → native code injected before last `}` | Step 3 |
| `setCallbackOption(ENTRY_DID_MOUNT, null)` → removes from map | Step 2 |
| `setEntryDidMountCallback(null)` → removes from map | Steps 6+2 |

---

### Step 11: E2E test (new view + spec)

New test view in `e2e-test-app`:
- One entry on the calendar
- `setCallbackOption(ENTRY_DID_MOUNT, fn)` sets `data-did-mount="true"` on the entry element
- `addEntryNativeEventListener("click", fn)` increments a visible counter on click

New Playwright spec verifies:
- Entry has `data-did-mount="true"` (user callback fired)
- Clicking the entry increments the counter (native listener fired)

---

## Edge Cases

| Scenario | Behavior |
|---|---|
| Null callback + native listeners | Generates `function(info){ addEventListener... }` wrapper |
| Non-null callback + no native listeners | Raw string pushed to JS unchanged |
| `setCallbackOption(ENTRY_DID_MOUNT, null)` | Removes from map; no JS call; native listeners still work |
| Called before attach | `applyEntryDidMountMerge` returns early; `onAttach` fires it |
| Multiple `addEntryNativeEventListener` calls | Merge reads full `customNativeEventsMap` each time — no duplicates |
| `setEntryDidMountCallback(null)` | Delegates → clears map entry → re-merge skips JS |
