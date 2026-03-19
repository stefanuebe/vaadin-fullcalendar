# Machbarkeitsanalyse: Scheduler-Ansicht mit FullCalendar v6

**Erstellt:** 2026-03-19
**Grundlage:** Screenshot `customer-requirement.png`, FullCalendar v6 Dokumentation (`fc-docs/_docs-v6`), Vaadin-Addon-Quellcode (`addon/`, `addon-scheduler/`)

---

## 1. Was zeigt der Screenshot?

Der Screenshot zeigt eine **horizontale Ressourcen-Timeline** (Gantt-ähnlich) mit folgenden Elementen:

| Bereich | Beschreibung |
|---|---|
| **X-Achse (Kopfzeile)** | Mehrzeilig: Wochentag (Thu/Fri/…), absolutes Datum (19/20/…), relative Tageszahl (–11/–10/…/0/1/…), farbige Phasenleiste |
| **Y-Achse (links)** | Ressourcentabelle mit 4 Spalten: Quantity, Duration, Code, Rollenbezeichnung |
| **Inhalt** | Grüne und blaue Pill-förmige Balken = Einsatzzeiträume der Ressourcen |
| **Markierung** | Roter Rahmen um eine Zeile (Hervorhebung) |

---

## 2. FC-Grundlage: resourceTimeline View

Die gesamte Ansicht entspricht dem **`resourceTimeline`-View** von FullCalendar — dieser ist das Kernstück des Scheduler-Plugins:

```
@fullcalendar/resource-timeline   (Premium / Scheduler-Lizenz erforderlich)
```

> **Wichtig:** Diese View ist kein freies Feature. Sie erfordert eine [FullCalendar Scheduler-Lizenz](https://fullcalendar.io/pricing) (kommerziell ca. $300/Jahr). Die nicht-kommerzielle Nutzung ist unter GPL möglich.

---

## 3. Analyse der einzelnen Anforderungen

### 3.1 Horizontale Timeline mit Ressourcenzeilen — ✅ Nativ

Der `resourceTimeline`-View liefert exakt diese Grundstruktur: Ressourcen als Zeilen, Zeit als horizontale Achse.

```js
initialView: 'resourceTimeline',
resources: [
  { id: '1', quantity: 1, duration: 40, code: 'ABC', title: 'CT Technical Site Leader' },
  // ...
]
```

---

### 3.2 Beliebiger Zeitraum / Sichtfenster — ✅ Nativ

Der sichtbare Zeitraum lässt sich exakt definieren:

```js
visibleRange: { start: '2024-11-08', end: '2025-05-06' }
// oder dynamisch über eine Funktion
```

Für einen langen Projektzeitraum (wie im Screenshot, ~3–4 Monate) ist das direkt konfigurierbar.

---

### 3.3 Mehrzeilige Zeitachsen-Kopfzeile (Wochentag + Datum) — ✅ Nativ

`slotLabelFormat` als Array erlaubt **mehrere Kopfzeilen-Ebenen**:

```js
slotLabelFormat: [
  { weekday: 'short' },   // Ebene 0: "Thu", "Fri", ...
  { day: 'numeric' }      // Ebene 1: "19", "20", ...
]
```

Über den `slotLabelContent`-Callback kann jede Ebene (`arg.level`) individuell gerendert werden — inklusive der **relativen Tageszahl** (Offset ab einem Referenzdatum, selbst berechnet).

---

### 3.4 Relative Tageszahlen in der Kopfzeile (–11, –10, … 0, 1, …) — ⚠️ Machbar, aber nicht nativ

Diese Zahlen sind nicht Teil der Standardformatierung. Sie lassen sich jedoch über `slotLabelContent` realisieren:

```js
slotLabelContent: function(arg) {
  if (arg.level === 2) {
    const refDate = new Date('2024-11-30'); // Referenz = Tag 0
    const diff = Math.round((arg.date - refDate) / 86400000);
    return { html: String(diff) };
  }
}
```

→ **Umsetzbar** mit moderatem Aufwand (eine zusätzliche Kopfzeilen-Ebene).

---

### 3.5 Farbige Phasenleiste in der Kopfzeile (Blau/Lila/Gelb-Blöcke) — ⚠️ Kein natives Feature, aber realisierbar

Die farbigen Balken über den Ressourcenzeilen (z.B. "Woche 1 = blau, Woche 2 = lila") sind **keine eingebaute Funktion**. Es gibt zwei praktikable Lösungsansätze:

**Option A: Dedizierte Kopfzeilen-Ressource (empfohlen)**
Eine nicht-navigierbare "Phantomzeile" als erste Ressource, gefüllt mit `display: 'background'`-Events in den gewünschten Farben. Die Zeilenbeschriftung bleibt leer oder zeigt "Phases".

**Option B: DOM-Injection via `datesSet`-Callback**
Nach dem Rendern wird ein farbiger HTML-Streifen per JavaScript direkt in den Timeline-Header-DOM eingefügt. Weniger sauber, aber präziser positionierbar.

→ **Umsetzbar**, Option A ist wartbarer.

---

### 3.6 Mehrere Ressourcen-Spalten links (Quantity, Duration, Code, Title) — ✅ Nativ

`resourceAreaColumns` liefert genau das:

```js
resourceAreaColumns: [
  { field: 'quantity',    headerContent: 'Qty',      width: 40  },
  { field: 'duration',    headerContent: 'Duration', width: 50  },
  { field: 'code',        headerContent: 'Code',     width: 60  },
  { field: 'title',       headerContent: 'Description'          }
]
```

Jede Spalte unterstützt eigene `cellContent`-Callbacks (z.B. für Fettschrift bei bestimmten Werten, wie im Screenshot "bold italic" für Kundenpersonal).

---

### 3.7 Ereignisbalken (grün/blau, mehrtägig) — ✅ Nativ

Standard-FC-Events mit `resourceId`-Verknüpfung. Farbe über `backgroundColor` oder `className` pro Event oder pro Ressource steuerbar:

```js
events: [
  { resourceId: '1', start: '2024-11-19', end: '2025-04-10', backgroundColor: '#7bc47f' },
  // ...
]
```

Mehrere übereinanderliegende Events pro Ressource (wie im Screenshot sichtbar, grüne + blaue Balken in derselben Zeile) werden durch FC automatisch gestapelt oder können über `eventDisplay: 'background'` für Hintergrundmarkierungen genutzt werden.

---

### 3.8 Zeilenmarkierung / Selektion (roter Rahmen im Screenshot) — ⚠️ Nicht nativ, aber einfach

FC hat keine eingebaute "Zeilenmarkierung". Umsetzung über:
- `resourceLaneClassNames`-Callback → CSS-Klasse auf die gewünschte Ressourcenzeile
- `resourceLabelClassNames` → Klasse auch auf die linke Beschriftungszelle

→ **Einfach umsetzbar** mit wenigen Zeilen JavaScript + CSS.

---

## 4. Gesamtbewertung

| Anforderung | Umsetzbarkeit | Aufwand |
|---|---|---|
| Horizontale Resource-Timeline | ✅ Nativ | — |
| Beliebiger Projektzeitraum | ✅ Nativ | — |
| Mehrzeilige Zeitachse (Tag, Datum) | ✅ Nativ | Gering |
| Relative Tageszahlen in Kopfzeile | ✅ Machbar | Gering |
| Farbige Phasenleiste (Kopfbereich) | ✅ Machbar | Mittel |
| Mehrere Ressourcenspalten links | ✅ Nativ | Gering |
| Mehrtägige farbige Ereignisbalken | ✅ Nativ | — |
| Zeilenmarkierung / Hervorhebung | ✅ Machbar | Gering |

**Fazit:** Die gezeigte Ansicht ist mit FullCalendar v6 vollständig realisierbar. Alle Kernanforderungen werden entweder nativ unterstützt oder lassen sich mit geringem bis mittlerem Aufwand über die vorhandenen Render-Hooks (Callbacks) umsetzen. Es gibt keine Anforderung, die einen grundlegenden Umbau oder das Umgehen von FC-Mechanismen erfordern würde.

---

## 5. Voraussetzungen / Einschränkungen (FC nativ)

- **Scheduler-Lizenz erforderlich:** Der `resourceTimeline`-View gehört zum kostenpflichtigen FullCalendar Scheduler-Paket.
- **Keine Out-of-the-box Phasenleiste:** Die farbige Kopfleiste erfordert eigenen Code (ca. 20–40 Zeilen).
- **Relative Tageszahlen:** Müssen applikationsseitig berechnet und über `slotLabelContent` injiziert werden — kein Konfigurationsparameter.

---

## 6. Umsetzbarkeit mit dem Vaadin FullCalendar Addon

Untersucht wurden: `addon/` (Core) und `addon-scheduler/` (Scheduler-Erweiterung), aktueller Stand des Branches `missing-features-check`.

### 6.1 Verfügbare Java-API im Scheduler-Addon

Das Addon stellt `FullCalendarScheduler extends FullCalendar` bereit und implementiert das `Scheduler`-Interface. Zusätzlich relevant: `Resource`, `ResourceEntry`, `ResourceAreaColumn`, `SchedulerView`.

### 6.2 Mapping Screenshot → Addon-API

#### resourceTimeline View — ✅ Vollständig unterstützt

```java
calendar.changeView(SchedulerView.RESOURCE_TIMELINE_WEEK);
// oder MONTH, YEAR, DAY
```

Für einen **benutzerdefinierten Zeitraum** (z.B. 120 Tage wie im Screenshot) gibt es keinen typisierten Setter. Das geht nur über den Raw-String-Setter:

```java
calendar.setOption("views", Map.of("resourceTimelineCustom", Map.of(
    "type", "resourceTimeline",
    "duration", Map.of("days", 120)
)));
calendar.setOption("initialView", "resourceTimelineCustom");
```

→ Funktioniert, ist aber kein "schöner" Java-API-Aufruf.

---

#### Zeitraum fixieren (visibleRange) — ⚠️ Nur via Raw-Setter

`visibleRange` ist **nicht als typisierter Setter** vorhanden und fehlt auch im `Option`-Enum. Umsetzung:

```java
calendar.setOption("visibleRange", Map.of(
    "start", "2024-11-08",
    "end",   "2025-05-06"
));
```

→ Technisch möglich, aber undokumentiert und ohne IDE-Unterstützung.

---

#### Mehrzeilige Zeitachsen-Kopfzeile (slotLabelFormat) — ⚠️ Nur via Option-Enum

`SLOT_LABEL_FORMAT` existiert als `Option`-Enum-Wert, aber **kein typisierter Convenience-Setter**. Die mehrzeilige Array-Variante muss als JSON-Struktur übergeben werden:

```java
ObjectMapper mapper = new ObjectMapper();
ArrayNode formats = mapper.createArrayNode();
formats.add(mapper.createObjectNode().put("weekday", "short"));
formats.add(mapper.createObjectNode().put("day", "numeric"));
calendar.setOption(FullCalendar.Option.SLOT_LABEL_FORMAT, formats);
```

→ Möglich, aber umständlich ohne typisierte API.

---

#### Relative Tageszahlen (slotLabelContent Callback) — ✅ Unterstützt

`setSlotLabelContentCallback(String jsFunction)` existiert im Core und ist über `FullCalendarScheduler` per Vererbung verfügbar:

```java
calendar.setSlotLabelContentCallback("""
    function(arg) {
        if (arg.level === 2) {
            var ref = new Date('2024-11-30');
            var diff = Math.round((arg.date - ref) / 86400000);
            return { domNodes: [Object.assign(document.createElement('span'), {textContent: String(diff)})] };
        }
    }
""");
```

→ Vollständig nutzbar.

---

#### Mehrere Ressourcenspalten links (resourceAreaColumns) — ✅ Unterstützt, mit Einschränkung

`setResourceAreaColumns(List<ResourceAreaColumn>)` ist vorhanden:

```java
calendar.setResourceAreaColumns(
    new ResourceAreaColumn("quantity",    "Qty").withWidth("50px"),
    new ResourceAreaColumn("duration",    "Duration").withWidth("60px"),
    new ResourceAreaColumn("code",        "Code").withWidth("70px"),
    new ResourceAreaColumn("title",       "Description")
);
```

**Einschränkung:** `ResourceAreaColumn` unterstützt aktuell nur Header-seitige Callbacks (`headerClassNames`, `headerDidMount`, `headerWillUnmount`). **`cellContent` und `cellClassNames` sind nicht implementiert.** Für die im Screenshot gezeigte fett-kursiv-Formatierung von Kundenpersonal-Zeilen müsste ein `cellContent`-Callback ergänzt werden — das ist eine **fehlende Funktion im Addon** (FC selbst unterstützt es).

---

#### Ressourcen mit extendedProps (Qty, Duration, Code) — ✅ Unterstützt

```java
Resource res = new Resource("1", "CT Technical Site Leader", null);
res.setExtendedProps(Map.of(
    "quantity", 1,
    "duration", 40,
    "code", "ABC123"
));
```

---

#### Ereignisbalken (grün/blau, mehrtägig) — ✅ Vollständig unterstützt

```java
ResourceEntry entry = new ResourceEntry();
entry.setStart(LocalDate.of(2024, 11, 19).atStartOfDay());
entry.setEnd(LocalDate.of(2025, 4, 10).atStartOfDay());
entry.setColor("#7bc47f"); // grün
entry.addResources(Set.of(resource));
```

---

#### Farbige Phasenleiste — ✅ Machbar (via Background-Events auf Phantom-Ressource)

```java
Resource phaseRow = new Resource("phases", "", null);
calendar.addResource(phaseRow);

ResourceEntry phase1 = new ResourceEntry();
phase1.setStart(LocalDate.of(2024, 11, 1).atStartOfDay());
phase1.setEnd(LocalDate.of(2024, 11, 30).atStartOfDay());
phase1.setColor("#4a90d9"); // blau
phase1.setDisplay("background");
phase1.addResources(Set.of(phaseRow));
```

→ Identisches Vorgehen wie bei nativem FC.

---

#### Zeilenmarkierung (roter Rahmen) — ✅ Unterstützt

```java
calendar.setResourceLaneClassNamesCallback("""
    function(arg) {
        if (arg.resource.id === '1') return ['highlighted-row'];
    }
""");
calendar.setResourceLabelClassNamesCallback("""
    function(arg) {
        if (arg.resource.id === '1') return ['highlighted-row'];
    }
""");
```

---

### 6.3 Gesamtbewertung Vaadin-Addon

| Anforderung | Addon-Unterstützung | Aufwand |
|---|---|---|
| resourceTimeline View | ✅ Typisiert (Enum) | — |
| Custom-Dauer-View | ⚠️ Nur via Raw-Setter | Gering |
| visibleRange | ⚠️ Nur via Raw-Setter | Gering |
| Mehrzeilige Kopfzeile (Format) | ⚠️ Nur via Option-Enum + JSON | Mittel |
| Relative Tageszahlen (Callback) | ✅ Typisierter Setter | Gering |
| Mehrere Ressourcenspalten | ✅ Typisiert | — |
| cellContent in Ressourcenspalten | ❌ Nicht implementiert | Erweiterung nötig |
| Ressourcen mit custom Properties | ✅ extendedProps | — |
| Mehrtägige Ereignisbalken | ✅ ResourceEntry | — |
| Farbige Phasenleiste | ✅ Background-Events | Gering |
| Zeilenmarkierung | ✅ Callbacks | Gering |

**Fazit:** Die Ansicht ist auch mit dem Vaadin-Addon umsetzbar. Die meisten Anforderungen werden durch typisierte Java-API abgedeckt. Drei Punkte erfordern Workarounds oder eine Addon-Erweiterung:

1. **`visibleRange` und Custom-View-Dauer**: Über `setOption(String, Object)` lösbar — unintuitive API, aber funktional.
2. **`slotLabelFormat` als Array**: Erfordert manuelles JSON-Bauen — wäre ein sinnvoller Kandidat für einen typisierten Setter im Addon.
3. **`ResourceAreaColumn.cellContent`**: Fehlt aktuell komplett. Für dynamische Zellinhalte (z.B. Fettschrift für bestimmte Ressourcen) ist eine Erweiterung der `ResourceAreaColumn`-Klasse um `withCellContent()` und `withCellClassNames()` notwendig — analog zu den bereits vorhandenen Header-Callbacks.
