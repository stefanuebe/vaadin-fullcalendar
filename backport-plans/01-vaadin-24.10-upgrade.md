# Backport Plan 1: Upgrade v6 auf Vaadin 24.10

## Ausgangslage

| Eigenschaft | v6_master (IST) | Ziel |
|---|---|---|
| Addon Vaadin BOM | 14.11.10 | **24.10.0** |
| Demo Vaadin BOM | 24.8.0 | **24.10.0** |
| Java (Addon) | 1.8 | **17** |
| Java (Demo) | 17 | 17 (bleibt) |
| Projekt-Version | 6.3.0 | **6.4.0-SNAPSHOT** |
| Parent Module | addon, addon-scheduler, demo | + **e2e-test-app** |
| Spring Boot (Demo) | 3.5.0 | **3.5.x** (bleibt kompatibel) |
| JSON-Serialisierung | elemental.json | elemental.json (bleibt) |
| Theming | Lumo | Lumo (bleibt, kein Aura) |

## Zielsetzung & Scope

**Version 6.4.0** stellt einen bewussten Plattformwechsel dar: Die Mindestanforderung wird von Vaadin 14 auf **Vaadin 24.10** angehoben. Vaadin 14-Kompatibilität wird damit aufgegeben.

Hintergrund: Die aktuelle Hauptentwicklung (v7) setzt Vaadin 25 und Java 21 voraus. Viele bestehende Anwendungen laufen jedoch noch auf Vaadin 24 und können nicht kurzfristig auf Vaadin 25 migrieren. Version 6.4 schließt diese Lücke, indem sie die neuen Features der v7-Linie auch für Vaadin 24-Anwendungen verfügbar macht — als stabiler Migrationspfad für Teams, die den Sprung auf Vaadin 25 noch nicht vollziehen können.

**Breaking Change**: Projekte, die noch Vaadin 14 einsetzen, müssen auf der bisherigen Version 6.3.x verbleiben.

## Warum 24.10?

- 24.10.0 ist das aktuelle Release der 24er-Linie (veröffentlicht 18. März 2025)
- Vaadin 24 ist die letzte Major-Version mit **Lumo-only** Theming (kein Aura)
- Erlaubt Java 17 (nicht Java 21 wie V25)
- `elemental.json` API bleibt verfügbar → **kein Jackson-Umbau nötig** (Jackson-Migration erst in V25)
- Bestehende `@CssImport`, `@JsModule`, `@NpmPackage`, `@Tag` Annotationen funktionieren unverändert
- `@DomEvent` / `ComponentEvent` / `@EventData` API ist identisch zu V14
- Vaadin 24 nutzt **Vite** statt Webpack (seit V23.2) — TS-Dateien kompatibel
- Vaadin 24.10 erfordert **Spring Boot 3.5+** — Demo nutzt bereits 3.5.0

## Abgrenzung zu v7 (master)

| Eigenschaft | v7 (master) | v6 Backport (dieses Projekt) |
|---|---|---|
| Vaadin | 25.x | **24.10.x** |
| Java | 21 | **17** |
| JSON | Jackson 3 (`tools.jackson`) | **elemental.json** (Vaadin Flow) |
| Theme | Lumo + Aura Fallback | **Lumo only** |
| FullCalendar JS | 6.1.20 | **6.1.9** (wird ggf. in Plan 2 aktualisiert) |
| E2E Tests | Playwright | Playwright (wird übernommen) |
| Specs | `specs/` Ordner | Wird adaptiert übernommen |

---

## Wichtiger Hinweis: Parent-POM-Struktur

Die Child-Module (addon, addon-scheduler, demo) deklarieren **keinen `<parent>`** — der Parent POM ist ein reiner Aggregator. Properties, die im Parent definiert werden, haben **keine Wirkung** auf die Child-Module. Jedes Modul muss seine Versionen und Properties eigenständig pflegen. Diese Struktur wird beibehalten (Änderung wäre out of scope).

---

## Schritt-für-Schritt Plan

### Phase 1: POM-Anpassungen (Addon + Addon-Scheduler)

#### 1.1 `addon/pom.xml`

- [ ] `vaadin.version` von `14.11.10` auf `24.10.0` ändern
- [ ] Java Compiler von `1.8` auf `17` ändern:
  ```xml
  <!-- ALT -->
  <maven.compiler.source>1.8</maven.compiler.source>
  <maven.compiler.target>1.8</maven.compiler.target>

  <!-- NEU -->
  <maven.compiler.release>17</maven.compiler.release>
  ```
  (Die alten `source`/`target` Properties entfernen)
- [ ] Webjar-Exclusions aus `vaadin-core` Dependency entfernen (V13-Kompatibilität irrelevant):
  ```xml
  <!-- Diese 6 Exclusion-Blöcke komplett entfernen: -->
  <!-- com.vaadin.webjar, org.webjars.bowergithub.insites, .polymer, -->
  <!-- .polymerelements, .vaadin, .webcomponents -->
  ```
- [ ] Plugin-Versionen aktualisieren:
  - `maven-compiler-plugin`: 3.10.1 → 3.14.1
  - `maven-surefire-plugin`: 2.22.0 → 3.5.4
  - `maven-failsafe-plugin`: 2.22.0 → 3.5.4
  - `nexus-staging-maven-plugin`: 1.6.7 → **1.7.0** (1.6.7 hat Probleme mit Java 17 Module System)
  - `maven-gpg-plugin`: 1.6 → **3.2.7** (optional, aber empfohlen)
- [ ] Test-Dependency-Versionen aktualisieren:
  - JUnit: 5.5.2 → 5.10.0
  - Mockito: 4.3.1 → 5.0.0
  - Lombok: 1.18.26 → 1.18.42
- [ ] `commons-text`: 1.10.0 → 1.14.0
- [ ] Projekt-Version: `6.3.0` → `6.4.0-SNAPSHOT`
- [ ] `vaadin-maven-plugin` Version in Profilen: **muss exakt `${vaadin.version}` entsprechen** (Mismatch führt zu Frontend-Build-Fehlern)
- [ ] `pnpmEnable` in vaadin-maven-plugin Konfiguration prüfen: V24 nutzt npm als Default. Entweder `<pnpmEnable>true</pnpmEnable>` beibehalten (pnpm weiterhin unterstützt) oder entfernen um den V24-Default (npm) zu verwenden

#### 1.2 `addon-scheduler/pom.xml`

- [ ] Gleiche Änderungen wie addon (vaadin.version, Java, Plugin-Versionen)
- [ ] Dependency auf `fullcalendar2` Version anpassen: `6.4.0-SNAPSHOT`
- [ ] **Hinweis**: addon-scheduler hat kein `maven-central` Profil (GPG, Nexus, Javadoc/Source). Für Maven-Central-Publishing muss das nachgerüstet werden — ggf. als separater Task
- [ ] **Hinweis**: Mockito ist in addon-scheduler auskommentiert. Falls Scheduler-Tests Mockito benötigen, Dependency aktivieren

#### 1.3 `pom.xml` (Parent — reiner Aggregator)

- [ ] Version: `6.3.0` → `6.4.0-SNAPSHOT`
- [ ] Properties dienen nur als Dokumentation (werden nicht an Children vererbt):
  ```xml
  <properties>
      <maven.compiler.release>17</maven.compiler.release>
      <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
  </properties>
  ```
- [ ] Modul `e2e-test-app` hinzufügen (wird in Phase 5 erstellt)
- [ ] `e2e-tests/` ist kein Maven-Modul (npm-Projekt) — wird NICHT in `<modules>` aufgenommen

### Phase 2: Demo-Anpassung

#### 2.1 `demo/pom.xml`

- [ ] `vaadin-bom` von `24.8.0` auf `24.10.0`
- [ ] `fullcalendar2` und `fullcalendar2-scheduler` Version: `6.4.0-SNAPSHOT`
- [ ] Spring Boot BOM: 3.5.0 bleibt (V24.10 erfordert Spring Boot 3.5+, passt)
- [ ] **vaadin-core Exclusion entfernen**: Die Demo excludiert `vaadin-core` aus `vaadin-spring-boot-starter` als V13-Workaround für Webjars. In V24 unnötig und fragil (Demo hängt sonst transitiv am Addon für vaadin-core). Exclusion-Block entfernen.
- [ ] `popup` Addon-Version auf V24.10-Kompatibilität prüfen (optional entfernen wenn inkompatibel)

### Phase 3: Source-Code Anpassungen

#### 3.1 javax → Jakarta Namespace

Vaadin 24 basiert auf Jakarta EE 10. Alle `javax.*` Imports müssen zu `jakarta.*` werden.

**Ist-Zustand** (verifiziert): Nur eine einzige Stelle betroffen:
- `addon/src/.../NotNull.java` — importiert `javax.validation.Constraint` (wird aber nicht als Annotation verwendet)
- Diese Klasse wird in v7 komplett gelöscht. → **Datei löschen** (wird in v7 durch die Standard-Annotationen ersetzt)

Addon-scheduler und Demo: **keine** `javax.*` Imports gefunden.

#### 3.2 Vaadin API-Migrationen

**Verifiziert**: Der Code nutzt bereits die V24-kompatiblen APIs:
- `callJsFunction()` (16 Vorkommen in FullCalendar.java, 12 in FullCalendarScheduler.java) — **kein `executeJavaScript()` vorhanden**
- `setPropertyJson()` — bereits V24-kompatibel
- Keine `HtmlImport`-Nutzung gefunden
- Keine `BootstrapListener`- oder `PageConfigurator`-Nutzung

→ **Keine Java-API-Migrationen nötig** (außer NotNull.java Löschung)

#### 3.3 Frontend-Kompatibilität

- [ ] **Frontend-Resource-Pfad verifizieren**: Die Addon-Dateien liegen unter `META-INF/resources/frontend/`. V24 unterstützt diesen Pfad weiterhin per Kompatibilitäts-Fallback (neuer Pfad wäre `META-INF/frontend/`). Muss nach dem Build verifiziert werden — falls Vite die Dateien nicht findet, Pfad migrieren.
- [ ] `full-calendar.ts` — nutzt standard `HTMLElement` mit `connectedCallback()`, kein Polymer, kein LitElement → **V24-kompatibel**
- [ ] TypeScript nutzt ESM `import`-Syntax (kein CommonJS `require()`) → **Vite-kompatibel** (verifiziert: kein `require()` im Code)
- [ ] `full-calendar-styles.css` — `@CssImport` funktioniert weiterhin in V24 (der `themeFor` Parameter ist deprecated, wird hier aber nicht verwendet)
- [ ] `full-calendar-theme-lumo.css` — Lumo ist Standard-Theme in V24, bleibt
  - **Kein** `full-calendar-theme-vaadin.css` übernehmen (Aura-Fallbacks nicht verfügbar in V24)
- [ ] `@NpmPackage` Annotationen — API identisch zu V14

#### 3.4 Kommunikationsmuster (laut specs/architecture.md — bestätigt stabil)

| Pattern | API | V24 Status |
|---|---|---|
| Java → JS (imperativ) | `getElement().callJsFunction(name, args)` | Stabil |
| Java → JS (State) | `getElement().setPropertyJson(name, json)` | Stabil |
| JS → Java | `@DomEvent` + `ComponentEvent` | Stabil |
| Entry Sync | `Entry.toJson()` (elemental.json) | Stabil |
| Option System | `callJsFunction("setOption", key, value)` | Stabil |

#### 3.5 Optionale Java-17-Modernisierung

**Nicht zwingend für das Upgrade**, kann aber gemacht werden:
- Text Blocks, `var`, `instanceof` Pattern Matching, `List.of()` / `Map.of()` / `Set.of()`

**Empfehlung**: Nur in Dateien modernisieren die sowieso angefasst werden.

### Phase 4: Build & Test

#### 4.1 Kompilierung

- [ ] `mvn clean compile -pl addon,addon-scheduler` → Kompilierungsfehler beheben
- [ ] Erwartung: **wenig bis keine Fehler** (kein executeJavaScript, kein javax außer NotNull)

#### 4.2 Frontend-Resource-Pfad verifizieren

- [ ] Demo starten und prüfen ob Vite die TS/CSS-Dateien aus `META-INF/resources/frontend/` findet
- [ ] Falls nicht: Dateien nach `META-INF/frontend/` verschieben (in addon/ und addon-scheduler/)
- [ ] Bei Problemen: `rm -rf node_modules package-lock.json target/` und neu bauen

#### 4.3 Unit Tests

- [ ] `mvn test -pl addon` → alle Tests grün
- [ ] `mvn test -pl addon-scheduler` → alle Tests grün

#### 4.4 Demo verifizieren

- [ ] `cd demo && mvn spring-boot:run` — Applikation startet
- [ ] Frontend-Build läuft durch (Vite)
- [ ] Manuelle Prüfung:
  - Calendar rendert in DayGrid, TimeGrid, List, MultiMonth Views
  - Entries werden angezeigt (Farben, Titel, Positionen)
  - Drag & Drop funktioniert
  - Resize funktioniert
  - Scheduler Views (Timeline, Resource) funktionieren
  - Entry Click Events kommen am Server an
  - Keine JS-Konsolen-Fehler
  - Custom Element `<vaadin-full-calendar>` registriert sich korrekt

### Phase 5: Specs, E2E-Infrastruktur & Dokumentation

#### 5.1 Specs-Ordner erstellen

- [ ] `specs/` Verzeichnis von master kopieren
- [ ] `specs/project-context.md` anpassen:
  - Java 21 → **Java 17**
  - Vaadin 25 → **Vaadin 24.10**
  - Jackson 3 → **elemental.json**
  - Abschnitt "Constraints" um Plattformwechsel-Info ergänzen
- [ ] `specs/architecture.md` anpassen:
  - Tech Stack: Java 17, Vaadin 24.10, elemental.json, Spring Boot 3.x
  - Jackson-Referenzen entfernen, elemental.json dokumentieren
  - `BeanProperties` Reflection-basierte JSON Beschreibung beibehalten
- [ ] `specs/design-system.md` anpassen:
  - Aura-Fallbacks (`--aura-*`) entfernen
  - `full-calendar-theme-vaadin.css` → `full-calendar-theme-lumo.css`
  - Lumo als einziges Theme dokumentieren
- [ ] `specs/verification.md` — direkt übernehmen
- [ ] `specs/use-cases/` — direkt übernehmen (Feature-Specs sind Vaadin-unabhängig)
- [ ] `specs/fc-v7-migration.md` — beibehalten als Referenz

#### 5.2 E2E-Test-Infrastruktur vorbereiten

- [ ] `e2e-test-app/` von master kopieren und anpassen:
  - `pom.xml`: Vaadin BOM → **24.10.0**, Java → **17**, Spring Boot → **3.5.x**
  - Jackson-basierte Imports durch elemental.json ersetzen (falls in Test Views vorhanden)
- [ ] `e2e-tests/` von master kopieren (Playwright-Suite, kein Vaadin-Bezug im JS)
  - `playwright.config.js` — Port und Basis-URL prüfen
  - `package.json` — Playwright-Version prüfen

**Hinweis**: E2E-Tests werden erst vollständig lauffähig wenn die Features aus Plan 2 backportiert sind.

#### 5.3 Dokumentation aktualisieren

- [ ] **Release Notes** für 6.4.0 erstellen:
  - Plattformwechsel dokumentieren: Vaadin 14 → Vaadin 24.10 Mindestanforderung
  - Java 8 → Java 17 Mindestanforderung
  - Klare Migrationsinformation: "Projekte auf Vaadin 14 bleiben auf 6.3.x"
  - Zielgruppe: Vaadin 24-Anwendungen die noch nicht auf V25 migrieren können
- [ ] **Getting-Started / README** aktualisieren:
  - Voraussetzungen: Vaadin 24.10+, Java 17+, Spring Boot 3.5+
  - Maven-Dependency Snippet mit neuer Version
- [ ] **Migration Guide** 6.3 → 6.4 erstellen:
  - Breaking: Vaadin 14 nicht mehr unterstützt
  - Keine API-Änderungen am Addon selbst (nur Plattform-Upgrade)
  - Hinweis dass 6.4 Brücke zu v7-Features bildet

---

## Risiken & Mitigationen

| Risiko | Wahrscheinlichkeit | Mitigation |
|---|---|---|
| Frontend-Resource-Pfad (`META-INF/resources/frontend/`) nicht erkannt | Mittel | Nach Build verifizieren. Fallback: Dateien nach `META-INF/frontend/` verschieben |
| Frontend-Build-Probleme (Vite) | Niedrig | TS nutzt ESM, kein `require()`. Bei Problemen: `node_modules` löschen und neu bauen |
| `pnpmEnable` Flag verhält sich anders in V24 | Niedrig | Flag beibehalten oder entfernen (npm ist Default in V24, beides funktioniert) |
| Demo `popup` Addon inkompatibel | Niedrig | Popup ist optional, kann entfernt werden |
| E2E-Test-Views nutzen v7-Features | Erwartet | Test Views werden nur strukturell übernommen; fehlende Features in Plan 2 |
| `nexus-staging-maven-plugin` 1.6.7 + Java 17 | Mittel | Auf 1.7.0 aktualisieren |

**Nicht mehr als Risiko eingestuft** (verifiziert):
- ~~`executeJavaScript` Renames~~ → Code nutzt bereits `callJsFunction()`
- ~~`javax.*` Namespace~~ → Nur NotNull.java betroffen, wird gelöscht
- ~~CommonJS `require()` im TS~~ → Nicht vorhanden, rein ESM
- ~~Mockito 5 + Java 17~~ → Kein Risiko
- ~~elemental.json Verfügbarkeit~~ → Bestätigt verfügbar in V24

---

## Verifikations-Checkliste (nach Abschluss)

### Build
- [ ] `mvn clean install` kompiliert fehlerfrei (addon, addon-scheduler, demo)
- [ ] Keine Compiler-Warnings zu deprecated Vaadin APIs
- [ ] Frontend-Build (Vite) läuft ohne Fehler

### Unit Tests
- [ ] `mvn test -pl addon` — alle Tests grün
- [ ] `mvn test -pl addon-scheduler` — alle Tests grün

### Funktional (Demo)
- [ ] Demo startet und rendert Calendar
- [ ] DayGrid, TimeGrid, List, MultiMonth Views funktionieren
- [ ] Entries anzeigen, Farben, Titel, Positionen korrekt
- [ ] Drag & Drop funktioniert
- [ ] Resize funktioniert
- [ ] Scheduler Timeline/Resource Views funktionieren
- [ ] Entry Click Events kommen am Server an

### Frontend
- [ ] TS/CSS-Dateien werden von Vite aus `META-INF/resources/frontend/` geladen
- [ ] Keine JS-Konsolen-Fehler im Browser
- [ ] FullCalendar JS Library wird korrekt geladen
- [ ] Custom Element `<vaadin-full-calendar>` registriert sich korrekt

### Dokumentation
- [ ] Release Notes vorhanden
- [ ] README/Getting-Started aktualisiert
- [ ] Migration Guide 6.3 → 6.4 vorhanden
- [ ] Specs für v6 adaptiert
