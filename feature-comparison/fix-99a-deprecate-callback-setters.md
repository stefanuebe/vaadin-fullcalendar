# Fix 99a: Deprecate pure-delegation callback setters

**Voraussetzung:** Fix 99 (setOptionFunction) muss abgeschlossen sein.

## Context

Nach Fix 99 existiert `setOptionFunction(Option/String, String)` als generische Methode, um eine FC-Option mit einer JS-Funktion zu belegen. Damit werden die dedizierten Callback-Setter überflüssig, sofern sie keine Zusatzlogik haben.

## Deprecate — Reine Delegationen

Diese Methoden tun nichts außer `callJsFunction("setXxxCallback", s)` (Pattern A) oder `setOption("xxx", s)` (Pattern B). Nach Fix 99 können Nutzer stattdessen `setOptionFunction(Option.XXX, ...)` verwenden.

### Pattern A — callJsFunction-Wrapper (werden zu setOptionFunction)

| Methode | Ersetzt durch |
|---|---|
| `setSelectAllowCallback(String)` | `setOptionFunction(Option.SELECT_ALLOW, ...)` |
| `setEventAllowCallback(String)` | `setOptionFunction(Option.EVENT_ALLOW, ...)` |
| `setEventOverlapCallback(String)` | `setOptionFunction(Option.EVENT_OVERLAP, ...)` |
| `setLoadingCallback(String)` | `setOptionFunction("loading", ...)` |
| `setEventDataTransformCallback(String)` | `setOptionFunction("eventDataTransform", ...)` |
| `setEventSourceSuccessCallback(String)` | `setOptionFunction("eventSourceSuccess", ...)` |
| `setDropAcceptCallback(String)` | `setOptionFunction(Option.DROP_ACCEPT, ...)` (aus Fix 01/A5) |
| `setEntryOrderCallback(String)` | `setOptionFunction(Option.ENTRY_ORDER, ...)` (aus Fix 01/A5) |

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

### Entry-Hooks — Sonderfall

| Methode | Pattern | Anmerkung |
|---|---|---|
| `setEntryClassNamesCallback` | callJsFunction | Hat TS-Methode mit `new Function()` — OK |
| `setEntryDidMountCallback` | callJsFunction | Hat TS-Methode mit `new Function()` — OK |
| `setEntryWillUnmountCallback` | callJsFunction | Hat TS-Methode mit `new Function()` — OK |
| `setEntryContentCallback` | `setOption("eventContent", s)` | **Bug** — siehe unten |

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

### Fix (als Teil von 99/99a)

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

## Verification

1. E2E-Test für mindestens einen Render-Hook (z.B. `dayCellClassNames`) — prüfen dass die Funktion wirklich ausgeführt wird
2. `mvn test -pl addon`
3. Alle deprecated-Methoden haben `@Deprecated(forRemoval = true)` + `@see setOptionFunction`
