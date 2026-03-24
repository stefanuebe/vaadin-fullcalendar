# Phase H: Infrastruktur, Tests, Dokumentation, Demo

> **Priorität**: Mittel (teilweise parallel zu anderen Phasen)
> **Geschätzter Aufwand**: Mittel
> **Abhängigkeiten**: E2E-Tests brauchen Features aus C-F; Specs/Doku können früher starten

## Scope

### 1. Specs-Ordner (kann sofort starten)

Von master kopieren und für v6 adaptieren:
- `project-context.md` → Java 17, Vaadin 24.10, elemental.json
- `architecture.md` → Tech Stack, Spring Boot 3.x, elemental.json statt Jackson
- `design-system.md` → Aura entfernen, Lumo only
- `datamodel/datamodel.md` → Neue Felder dokumentieren
- `use-cases/` → Direkt übernehmen
- `verification.md` → Direkt übernehmen

### 2. Unit Tests (phasenweise mit Features)

| Test-Klasse | Phase | Beschreibung |
|---|---|---|
| `ConverterTest.java` | A | Converter Unit Tests |
| `EntryModelTest.java` | B | Entry Property + RRule Tests |
| `JsCallbackTest.java` | C1 | JsCallback Tests |
| `AdvancedOptionsTest.java` | C1 | Option System Tests |
| `InteractionCallbacksTest.java` | C1 | Callback Tests |
| `DisplayOptionsTest.java` | C1 | Display Option Tests |
| `AccessibilityTouchTest.java` | C1 | Accessibility Tests |
| `EventSourcesTest.java` | E | Event Source Tests |
| `ComponentResourceAreaColumnTest.java` | F | Scheduler Column Tests |
| `SchedulerFeaturesTest.java` | F | Scheduler Tests |
| `ResourceEntryCopyTest.java` | B/F | Resource Entry Copy Tests |

### 3. E2E-Test-Infrastruktur (nach Phase C)

**e2e-test-app/** von master kopieren und adaptieren:
- `pom.xml`: Vaadin 24.10, Java 17, Spring Boot 3.5
- Test Views: Jackson→elemental.json in Java-Dateien
- Nur Views für implementierte Features aktivieren

**e2e-tests/** von master kopieren:
- Playwright-Suite (kein Vaadin-Bezug im JS)
- `playwright.config.js` — Port/URL prüfen

### 4. Demo-Modul aktualisieren

Für jedes neue Feature Demo-Content hinzufügen:
- RRule-Beispiel (Phase B)
- Event Source Beispiele (Phase E)
- ComponentResourceAreaColumn Demo (Phase F)
- JsCallback Beispiele (Phase C1)

### 5. Dokumentation

- **Release Notes 6.4.0**: Plattformwechsel + alle neuen Features
- **Migration Guide 6.3 → 6.4**: Breaking Changes (V14 dropped), API-Deprecations, overlap-Änderung
- **Feature-Docs**: Neue Features dokumentieren
- **README** aktualisieren
- **Wiki synchronisieren**: `wiki/` muss mit `docs/` inhaltlich synchron bleiben (siehe MEMORY.md Regel)

### 6. Frontend-Event-Verifikation (Phase D)

DOM-Event-Dispatching kann nicht mit Unit Tests verifiziert werden. Strategie:
- **E2E-Tests**: `interaction-callbacks.spec.js` und `calendar-interactions.spec.js` aus v7 decken DragStart/Stop, ResizeStart/Stop ab
- **Manuelle Verifikation**: Demo mit Event-Listener starten, Browser DevTools → Event Listeners prüfen

## Verifikation

- [ ] Specs konsistent mit v6
- [ ] Alle Unit Tests grün
- [ ] E2E-Test-App startet auf V24.10
- [ ] E2E-Tests laufen (soweit Features implementiert)
- [ ] Demo zeigt alle neuen Features
- [ ] Dokumentation vollständig
