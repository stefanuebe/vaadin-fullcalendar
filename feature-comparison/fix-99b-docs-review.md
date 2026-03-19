# Fix 99b: Full documentation review and update

**Voraussetzung:** Alle anderen Fixes (01–12, 99, 99a) müssen abgeschlossen sein.

## Context

Die Dokumentation in `docs/` wurde bei früheren Fixes nur punktuell angepasst. Durch Fix 01 (Remove typed setters), Fix 02 (Event→Entry), und die CustomButton/buttonIcons-Entfernung sind viele Referenzen veraltet. Ziel ist eine vollständige Durchsicht aller Docs gegen den tatsächlichen Stand der API.

## Scope

Alle Dateien unter `docs/`:
- `Features.md`
- `Samples.md`
- `Release-notes.md`
- `Migration-guides.md`
- `FAQ.md`
- `Known-issues.md`
- `Home.md`

## Was zu prüfen ist

### 1. Entfernte Methoden (Fix 01)

Jede dieser Methoden wurde entfernt — Doku-Referenzen müssen auf `setOption(Option.XYZ, value)` umgestellt werden:

- `setViewHint(String)` → `setOption(Option.NATIVE_TOOLBAR_VIEW_HINT, ...)`
- `setNavLinkHint(String)` → `setOption(Option.NAV_LINK_HINT, ...)`
- `setMoreLinkHint(String)` → `setOption(Option.MORE_LINK_HINT, ...)`
- `setCloseHint(String)` → `setOption(Option.CLOSE_HINT, ...)`
- `setTimeHint(String)` → `setOption(Option.TIME_HINT, ...)`
- `setDateIncrement(String)` → `setOption(Option.DATE_INCREMENT, ...)`
- `setDateAlignment(String)` → `setOption(Option.DATE_ALIGNMENT, ...)`
- `setUnselectCancel(String)` → `setOption(Option.UNSELECT_CANCEL, ...)`
- `setDropAccept(String)` (CSS-selector variant) → `setOption(Option.DROP_ACCEPT, ...)`
- `setProgressiveEventRendering(boolean)` → `setOption(Option.PROGRESSIVE_EVENT_RENDERING, ...)`
- `setThemeSystem(ThemeSystem)` → removed entirely (ThemeSystem enum deleted)
- `setEventInteractive(boolean)` → `setOption(Option.ENTRY_INTERACTIVE, ...)`
- `setEventHint(String)` → `setOption(Option.ENTRY_HINT, ...)`
- `setStartParam` / `setEndParam` / `setTimeZoneParam` → `setOption(Option.EXTERNAL_EVENT_SOURCE_START_PARAM, ...)` etc.
- `incrementDate(String)` / `previousYear()` / `nextYear()` / `updateSize()` → verify if these still exist; if not, update docs

### 2. Umbenannte Methoden (Fix 02)

- `setEventOverlapCallback` → `setEntryOverlapCallback`
- `setEventAllowCallback` → `setEntryAllowCallback`
- `setEventConstraint(String/BusinessHours)` → `setEntryConstraint(...)`
- `setEventConstraintToBusinessHours()` → entfernt
- Resource: `setEventBackgroundColor/BorderColor/TextColor/Overlap/Constraint/ClassNames/Allow` → `setEntry*`

### 3. Entfernte Klassen (CustomButton-Entfernung)

- `CustomButton` — Klasse gelöscht
- `CustomButtonClickedEvent` — Klasse gelöscht
- `buttonIcons` Option — entfernt
- `TITLE_FORMAT` Option — entfernt

### 4. Neue API (sicherstellen, dass sie dokumentiert ist)

- `setOptionFunction(CallbackOption, String)` + `CallbackOption` enum (Fix 99)
- `setDropAcceptCallback(String)` — neu hinzugefügt
- `setEntryOrderCallback(String)` — neu hinzugefügt
- `addClientSideEventSource(...)` + Registration (Fix 03, falls implementiert)
- Resource entry override methods: `setEntryBackgroundColor`, `setEntryBorderColor`, `setEntryTextColor`, `setEntryConstraint`, `setEntryOverlap`, `setEntryClassNames`, `setEntryAllow`

### 5. Samples.md — Code-Beispiele

Alle Code-Beispiele gegen aktuelle API compilieren/prüfen:
- `setValidRange(...)` — non-callback variant prüfen ob noch vorhanden
- `setMoreLinkHint(...)` — entfernt
- `setNavLinkHint(...)` — entfernt
- Scheduler-Beispiele mit Resource-Methoden aktualisieren

### 6. Release-notes.md

Den gesamten Release-notes-Eintrag für v7 reviewen und sicherstellen, dass er den tatsächlichen API-Stand widerspiegelt (keine entfernten Methoden mehr nennen, neue Methoden ergänzen).

## Verification

1. Grep über `docs/` nach allen Methodennamen aus den obigen Listen — zero hits für entfernte Methoden
2. Spot-check: 5 zufällige Code-Beispiele aus Samples.md gegen tatsächliche API verifizieren
3. Peer-review: Jemand anderes liest Features.md und prüft, ob die beschriebenen Features wirklich so funktionieren
