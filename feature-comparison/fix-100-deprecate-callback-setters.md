# Fix 100: Deprecate/remove pure-delegation callback setters

**Voraussetzung:** Fix 99 (setOptionFunction + CallbackOption) und Fix 02 (Event→Entry) müssen abgeschlossen sein.

## Context

Nach Fix 99 existiert `setOptionFunction(CallbackOption, String)` als generische Methode, um eine FC-Option mit einer JS-Funktion zu belegen. Damit werden die dedizierten Callback-Setter überflüssig, sofern sie keine Zusatzlogik haben.

Methodennamen folgen Fix 02 (Event→Entry-Naming): z.B. `setEventAllowCallback` → `setEntryAllowCallback`.

## Deprecate — Reine Delegationen

Diese Methoden tun nichts außer `callJsFunction("setXxxCallback", s)` (Pattern A) oder `setOption("xxx", s)` (Pattern B). Nach Fix 99 können Nutzer stattdessen `setOptionFunction(Option.XXX, ...)` verwenden.

### Pattern A — callJsFunction-Wrapper (werden zu setOptionFunction)

| Methode (nach Fix 02) | Ersetzt durch | Aktion |
|---|---|---|
| `setSelectAllowCallback(String)` | `setOptionFunction(CallbackOption.SELECT_ALLOW, ...)` | Neu in Branch → entfernen |
| `setEntryAllowCallback(String)` | `setOptionFunction(CallbackOption.EVENT_ALLOW, ...)` | Neu → entfernen |
| `setEntryOverlapCallback(String)` | `setOptionFunction(CallbackOption.EVENT_OVERLAP, ...)` | Neu → entfernen |
| `setLoadingCallback(String)` | `setOptionFunction(CallbackOption.LOADING, ...)` | Neu → entfernen |
| `setEntryDataTransformCallback(String)` | `setOptionFunction(CallbackOption.EVENT_DATA_TRANSFORM, ...)` | Neu → entfernen |
| `setEventSourceSuccessCallback(String)` | `setOptionFunction(CallbackOption.EVENT_SOURCE_SUCCESS, ...)` | Neu → entfernen |
| `setDropAcceptCallback(String)` | `setOptionFunction(CallbackOption.DROP_ACCEPT, ...)` | Neu → entfernen |
| `setEntryOrderCallback(String)` | `setOptionFunction(CallbackOption.ENTRY_ORDER, ...)` | Neu → entfernen |
| `setNavLinkDayClickCallback(String)` | `setOptionFunction(CallbackOption.NAV_LINK_DAY_CLICK, ...)` | Neu → entfernen |
| `setNavLinkWeekClickCallback(String)` | `setOptionFunction(CallbackOption.NAV_LINK_WEEK_CLICK, ...)` | Neu → entfernen |

### Pattern B — setOption mit String (Render-Hooks, siehe Bug unten)

Alle 40+ Render-Hook-Callbacks:
- `setDayCellClassNamesCallback`, `setDayCellContentCallback`, `setDayCellDidMountCallback`, `setDayCellWillUnmountCallback`
- `setDayHeaderClassNamesCallback`, ... (4 Methoden)
- `setSlotLabelClassNamesCallback`, ... (4 Methoden)
- `setSlotLaneClassNamesCallback`, ... (4 Methoden)
- `setViewClassNamesCallback`, `setViewDidMountCallback`, `setViewWillUnmountCallback`
- `setNowIndicatorClassNamesCallback`, ... (4 Methoden)
- `setWeekNumberClassNamesCallback`, ... (4 Methoden)
- `setMoreLinkClassNamesCallback`, ... (4 Methoden)
- `setNoEventsClassNamesCallback`, ... (4 Methoden)
- `setAllDayClassNamesCallback`, ... (4 Methoden)
- `setNavLinkDayClickCallback`, `setNavLinkWeekClickCallback`

### Entry-Hooks — Existieren auf master → deprecaten

| Methode | Pattern | Aktion |
|---|---|---|
| `setEntryClassNamesCallback` | callJsFunction | `@Deprecated` → `setOptionFunction(CallbackOption.ENTRY_CLASS_NAMES, ...)` |
| `setEntryDidMountCallback` | callJsFunction | `@Deprecated` → `setOptionFunction(CallbackOption.ENTRY_DID_MOUNT, ...)` |
| `setEntryWillUnmountCallback` | callJsFunction | `@Deprecated` → `setOptionFunction(CallbackOption.ENTRY_WILL_UNMOUNT, ...)` |
| `setEntryContentCallback` | `setOption("eventContent", s)` | `@Deprecated` → `setOptionFunction(CallbackOption.ENTRY_CONTENT, ...)` (**Bug-Fix** — siehe unten) |

## KEEP — Haben Zusatzlogik

| Methode | Grund |
|---|---|
| `setSelectOverlapCallback(String)` | Null-Handling: `null` → `setOption(null)`, sonst `callJsFunction` |
| `setValidRangeCallback(String)` | Null-Handling: `null` → `setOption(null)`, sonst `callJsFunction` |
| `setFixedMirrorParent(String)` | Null-Handling in TS-Methode |

---

## BUG: Render-Hook-Callbacks (Pattern B) funktionieren nicht korrekt

### Problem

Die Render-Hook-Callbacks ab `dayCellClassNames` aufwärts nutzen auf Java-Seite:
```java
setOption("dayCellClassNames", jsFunction);  // sends raw string
```

Die TS-Seite `setOption()` leitet den Wert **ohne** `new Function()` an FC weiter. FC erwartet aber eine **echte JavaScript-Funktion**, keinen String. Das heißt:
- Der String wird als FC-Option gespeichert
- FC versucht den String als Funktion aufzurufen → **TypeError zur Laufzeit**

### Ausnahme: Entry-Hooks

Die Entry-Hooks (`eventClassNames`, `eventDidMount`, `eventWillUnmount`) nutzen `callJsFunction` mit eigener TS-Methode, die `new Function()` korrekt anwendet. `eventContent` hat speziellen TS-Intercept-Code.

### Fix (als Teil von 99/100)

Alle Render-Hook-Callbacks müssen auf das `setOptionFunction`-Pattern umgestellt werden:

**Vorher** (broken):
```java
public void setDayCellClassNamesCallback(String jsFunction) {
    setOption("dayCellClassNames", jsFunction);  // sends raw string
}
```

**Nachher** (via setOptionFunction):
```java
@Deprecated
public void setDayCellClassNamesCallback(String jsFunction) {
    setOptionFunction("dayCellClassNames", jsFunction);  // wraps in new Function()
}
```

Oder direkt: Nutzer ruft `setOptionFunction("dayCellClassNames", ...)` auf, Methode wird deprecated.

### Konsequenz für Fix 99

Fix 99 muss sicherstellen, dass `setOptionFunction` **vor** Fix 99a implementiert ist, damit die deprecaten Callback-Setter auf `setOptionFunction` umgestellt werden können und der Bug gleichzeitig gefixt wird.

## TS-Aufräumen

Nach Einführung von `setOptionFunction` auf TS-Seite (eine generische Methode) werden alle individuellen TS-Callback-Methoden überflüssig:

**Entfernen aus `full-calendar.ts`:**
- `setEventClassNamesCallback(s)` — ersetzt durch generisches `setOptionFunction`
- `setEventDidMountCallback(s)` — dito
- `setEventWillUnmountCallback(s)` — dito
- `setSelectAllowCallback(s)` — dito
- `setEventAllowCallback(s)` — dito
- `setEventOverlapCallback(s)` — dito
- `setValidRangeCallback(s)` — dito
- `setSelectOverlapCallback(s)` — dito
- `setFixedMirrorParent(s)` — Sonderfall wegen null-Handling, prüfen

Die Java-seitigen deprecated Methoden (`setEntryClassNamesCallback` etc.) werden intern auf `setOptionFunction(CallbackOption.XXX, ...)` umgestellt, das dann `callJsFunction("setOptionFunction", key, fn)` aufruft → nur noch eine TS-Methode nötig.

**Beachten:** Der `eventContent`-Sonderfall im TS-Code (Intercept in `setOption` + custom API wrapping) muss in die generische `setOptionFunction` TS-Methode integriert werden, oder separat behandelt bleiben.

---

## Verification

1. E2E-Test für mindestens einen Render-Hook (z.B. `dayCellClassNames`) — prüfen dass die Funktion wirklich ausgeführt wird
2. `mvn test -pl addon`
3. Alle deprecated-Methoden haben `@Deprecated(forRemoval = true)` + `@see setOptionFunction`
