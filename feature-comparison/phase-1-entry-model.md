# Phase 1: Missing Entry/Event Model Properties

## Goal

Add missing properties to the `Entry` Java model that correspond to FC event object fields. These are per-event properties (not calendar-level options).

---

## Existing Entry field patterns (context for all additions)

`Entry.java` uses Lombok `@Getter` and `@lombok.Setter` at class level, meaning all fields automatically get getters/setters unless overridden. Additional annotations control JSON serialization:
- `@JsonName("fcFieldName")` — maps the Java field to a different FC JSON key (e.g., `recurringStartDate` → `"startRecur"`)
- `@JsonConverter(SomeConverter.class)` — transforms the value during serialization (e.g., `LocalDate` → ISO string)
- `@JsonUpdateAllowed` — marks the field as updatable from client-side events (not relevant for new fields in this phase)
- `@JsonIgnore` — prevents the field from being sent to FC at all

Fields without any annotation are serialized using the Java field name as-is, which must match the FC event object property name exactly.

---

## Features Covered

### 1.1 `url` property on Entry

FC event objects support a `url` property. When the user clicks the event, **the browser navigates to this URL** (default browser navigation, i.e., `window.location.href = url` or `window.open(url, '_blank')` depending on configuration). This is a fundamental FC feature used for event linking to external resources, detail pages, etc.

**FC option:** `url` on event object (string)

**Java change:** Add `private String url` field to `Entry.java`. No annotation needed — the field name `url` matches FC's property name exactly.

```java
// In Entry.java — after existing fields like textColor, borderColor:
private String url;
// Lombok @Getter/@Setter generates getUrl() / setUrl(String) automatically
```

**JSON serialization:** Serialized as `"url": "https://..."` in the event JSON sent to FC. No `@JsonName` needed. Null value omits the field entirely (standard Jackson behavior; confirm the addon's serializer does the same for null fields — it should since other nullable fields like `color` are omitted when null).

**Interaction with other settings:**

- **`setEditable` / `Entry.setEditable(boolean)`:** The `url` field is independent of editability. An event with a URL can still be draggable. However, FC will navigate on click even if the event is editable; the developer cannot separately disable URL navigation while keeping drag enabled via a Java API — they would need a JS override for the `eventClick` callback.
- **`addEntryClickedListener`:** If both a URL and a server-side `entryClickedListener` are registered, **FC navigates to the URL first** (synchronously, in the same click handler), and then the server-side `entryClickedListener` fires via the normal `@ClientCallable` mechanism. In practice, the browser navigation may interrupt the server event from completing. To prevent URL navigation while still reacting to clicks server-side, set `url = null` and handle navigation in the click listener instead. Do not rely on both firing cleanly.

**Caveat:** FC only navigates to the URL when the user clicks the rendered event. There is no way to suppress this navigation from Java code alone once the URL is set — use JS `eventClick` callback override if needed.

---

### 1.2 `interactive` property on Entry

FC v6 introduced `eventInteractive` as a calendar-level option and `interactive` as a per-event override. These control whether an event is focusable and tabbable via keyboard, independently of drag/drop.

**The two distinct concepts:**
1. **`url`-based interactivity:** FC automatically makes an event keyboard-focusable (tabbable) if it has a `url`. Pressing Enter on the focused event navigates to the URL.
2. **`interactive: true` without URL:** Makes the event focusable/tabbable **without** a URL. This is essential for accessibility when the developer handles `entryClickedListener` server-side but the event has no URL. Without `interactive: true`, a non-URL event is not reachable by keyboard at all.
3. **`editable` (drag/drop):** Controls whether the event can be dragged and resized — completely independent of `interactive`. An event can be `interactive: true` (keyboard focusable) but `editable: false` (not draggable).

**FC option:** `interactive` on event object (boolean or null; null means inherit from `eventInteractive` calendar option)

**Java change:**
```java
// In Entry.java:
private Boolean interactive;
// Boolean (boxed) not boolean (primitive), so null = "not set, inherit calendar default"
// setInteractive(Boolean interactive) / isInteractive()
// Note: isInteractive() returns Boolean (boxed), not boolean
```

**JSON serialization:** Serialized as `"interactive": true/false`. Null is omitted. No `@JsonName` needed.

**Cross-reference:** The calendar-level `eventInteractive` option is covered in Phase 6.1. Do not duplicate. The per-entry `interactive` is the per-event override.

---

### 1.3 `duration` property for multi-day all-day recurring events

For recurring all-day events (events with `daysOfWeek`, `startRecur`, `endRecur`), FC supports a `duration` property to make each occurrence span multiple days. Without `duration`, each occurrence spans exactly one day.

**Existing recurrence fields in `Entry` (for context):**

```java
@JsonName("startRecur")    private LocalDate recurringStartDate;
@JsonName("endRecur")      private LocalDate recurringEndDate;
@JsonName("startTime")     private RecurringTime recurringStartTime;
@JsonName("endTime")       private RecurringTime recurringEndTime;
@JsonName("daysOfWeek")    private Set<DayOfWeek> recurringDaysOfWeek;
```

The `duration` field **completes the picture** by allowing each occurrence to last more than one day. Example: a weekly Monday event that lasts 3 days (Mon-Wed) would use `daysOfWeek = [MONDAY]` and `duration = "P3D"` (or `"72:00:00"`).

**FC option:** `duration` on recurring event object (FC duration string)

**Java change:**
```java
// In Entry.java:
@JsonName("duration")
private String recurringDuration;
// setRecurringDuration(String) / getRecurringDuration()
```

**JSON serialization:** Uses `@JsonName("duration")` to map `recurringDuration` to FC's `"duration"` key. The value is a FC duration string (ISO 8601 period or `"HH:MM:SS"` format). Examples: `"P2D"`, `"P1W"`, `"48:00:00"`.

**Caveat:** Only meaningful when other recurrence fields are set. Setting `recurringDuration` without any `daysOfWeek`/`startRecur` has no effect. The Java API should document this. No validation is enforced server-side.

**Caveat 2:** This `duration` field is specifically for recurring events. It is **not** the same as the event duration derived from `start`/`end`. Non-recurring events use `start` + `end` to define duration.

---

### 1.4 RRule plugin support

The `@fullcalendar/rrule` plugin enables rich recurrence (daily, weekly with byweekday, monthly, yearly, etc.) beyond the simple built-in recurrence (`daysOfWeek`/`startTime`/`endTime`). This is a larger feature requiring both Java model changes and frontend changes.

**Two rrule input formats FC accepts:**

**Format 1 — RRULE string (iCalendar format):**
```
DTSTART:20230101T090000Z
RRULE:FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=20231231T235959Z
```
- Pros: Standard iCalendar format, can be imported from calendar apps
- Cons: String manipulation in Java is error-prone; no type safety

**Format 2 — JSON object:**
```json
{
  "freq": "weekly",
  "byweekday": ["mo", "we"],
  "dtstart": "2023-01-01T09:00:00",
  "until": "2023-12-31"
}
```
- Pros: Easier to construct programmatically in Java; maps naturally to a builder class
- **Recommendation: use the JSON object approach**

**Proposed `RRule` builder class:**
```java
public class RRule {
    public enum Frequency { YEARLY, MONTHLY, WEEKLY, DAILY }

    private Frequency freq;           // required
    private String dtstart;           // ISO datetime string; start of first occurrence
    private String until;             // ISO date/datetime string; end date of recurrence (exclusive with count)
    private Integer count;            // number of occurrences (exclusive with until)
    private Integer interval;         // interval between occurrences (default 1)
    private List<String> byweekday;  // e.g., ["mo", "tu", "we"] or ["mo", "-1fr"] for last Friday
    private List<Integer> byyearday; // day numbers within the year (1-366)
    private List<Integer> bymonth;   // month numbers (1-12)
    private List<Integer> bymonthday;// day-of-month numbers (1-31, negative for from-end)
    private List<Integer> byhour;    // hour numbers (0-23)
    private List<Integer> byminute;  // minute numbers (0-59)
    private String wkst;             // week start day: "mo", "su", etc.

    // fluent builder:
    public static RRule weekly() { ... }
    public static RRule monthly() { ... }
    public RRule byWeekday(String... days) { ... }
    public RRule until(LocalDate date) { ... }
    public RRule count(int n) { ... }
    public RRule interval(int n) { ... }

    // serialization:
    public ObjectNode toJson() { ... }
}
```

**Java changes to `Entry`:**
```java
// In Entry.java:
@JsonIgnore  // handled by custom serialization
private RRule rrule;

@JsonName("exdate")
private String exdate;     // ISO date string or comma-separated dates to exclude

@JsonName("exrule")
private String exrule;     // RRULE string for excluded dates (less common)
```

The `rrule` field requires custom serialization: when non-null, serialize as either the JSON object form (`rrule.toJson()`) or as a raw RRULE string depending on which form was set. Recommend supporting both:
- `setRrule(RRule rrule)` — typed, uses JSON object form
- `setRrule(String rruleString)` — raw string form for import scenarios

**Frontend TypeScript impact (REQUIRED):**
1. Add `@fullcalendar/rrule` npm dependency to `package.json` (or the Vaadin npm config in `FullCalendar.java`'s `@NpmPackage` annotations)
2. In the TS companion file (`full-calendar.ts`), import `rrulePlugin` from `@fullcalendar/rrule` and add it to the plugins array in the calendar initialization

```typescript
// In full-calendar.ts:
import rrulePlugin from '@fullcalendar/rrule';
// In calendar init options:
plugins: [...existingPlugins, rrulePlugin],
```

**Caveat:** The rrule plugin and the built-in recurrence system (`daysOfWeek` etc.) are **mutually exclusive** on a per-event basis. Do not set both `rrule` and `recurringDaysOfWeek` on the same entry; FC behavior is undefined.

**Caveat 2:** `exdate` format: FC's rrule plugin accepts ISO 8601 date strings. A list of exclusion dates should be comma-separated or an array. The Java API should accept `List<String>` or `List<LocalDate>`.

---

### 1.5 `Entry.overlap` field (per-entry overlap control)

FC's per-event `overlap` property controls whether this specific event allows other events to overlap with it (and whether it can be dropped onto another event). This is different from the global `slotEntryOverlap` calendar option.

**FC option:** `overlap` on event object (boolean)

The `Entry` class **already has this field:**
```java
private boolean overlap = true;
```

However, it defaults to `true` (primitive boolean), meaning overlap cannot be left "unset" to inherit the calendar-level default. If you want an entry to inherit the calendar's `slotEntryOverlap` setting, you would need `Boolean` (nullable) rather than `boolean`.

**Recommendation:** Change `overlap` to `Boolean` (boxed) so that `null` means "inherit from calendar setting". This is a behavior change: currently `overlap = true` is always serialized. With `Boolean`, `null` would be omitted and FC would use the global setting.

**JSON serialization:** Serialized as `"overlap": true/false`. Currently uses the Java field name — no `@JsonName` needed.

**Cross-reference:** Global `setSlotEntryOverlap(boolean)` is covered in Phase 0.1 as a missing typed setter. The two interact: per-entry `overlap` takes precedence over the global `slotEntryOverlap` when set.

---

### 1.6 `Entry.constraint` typed improvement

`Entry.setConstraint(String)` currently accepts only a string. For the same reasons as Phase 0.4 (global constraint options), a `BusinessHours` overload is needed.

**Cross-reference:** This is the same problem as Phase 0.4 (`Entry.setConstraint` typed overloads). Phase 0.4 covers the full design including `BusinessHours` overload and the JSON serialization challenge. Do not duplicate the implementation design here — implement it once as described in 0.4.

The per-entry `constraint` and the calendar-level `eventConstraint` / `selectConstraint` all share the same three-form value type (string groupId / `"businessHours"` keyword / BusinessHours object). The serialization logic for the object form can be extracted into a shared utility.

---

## Implementation Notes

- `url` and `interactive` are straightforward JSON fields — follow existing patterns like `textColor`, `borderColor`. Both are nullable: use `String` for `url` (null = no URL) and `Boolean` (boxed) for `interactive` (null = inherit calendar default).
- RRule support requires frontend changes: the `@fullcalendar/rrule` package must be loaded and added to the calendar plugins list in the TypeScript companion file. This is the only item in Phase 1 requiring TS changes.
- The `duration` field for recurring events is a duration string — can reuse existing `SnapDuration` string pattern or define a typed `Duration` wrapper. A typed `RecurringDuration` class wrapping Java `java.time.Duration` is the cleanest approach if the addon wants to avoid raw duration strings.
- No new event classes are needed for this phase (these are model properties, not interaction callbacks).
- The `overlap` field type change from `boolean` to `Boolean` is technically a breaking behavior change — consider a migration path.

---

## Testing

### JUnit tests
Add a test class `Phase1EntryModelTest.java` in `addon/src/test/java/org/vaadin/stefan/fullcalendar/`.

Cover:
- `url` field: verify serialization to `"url"` key in JSON, null handling, storage/retrieval
- `interactive` field: verify `Boolean` (boxed) type, null vs true/false serialization, JSON key is `"interactive"`
- `recurringDuration` field: verify serialization to `"duration"` key (via `@JsonName`), string format, null handling
- `RRule` class: verify `toJson()` output matches FC plugin format, builder methods work, `exdate`/`exrule` fields serialize correctly
- `overlap` type change (if implemented): verify `Boolean` null serialization vs previous `boolean` default behavior

### Playwright tests (client-side effects)
Add demo view at `demo/src/main/java/org/vaadin/stefan/ui/view/testviews/Phase1EntryModelTestView.java` using `@Route(value = "phase1-entry-model", layout = TestLayout.class)`.

The view creates entries with `url`, `interactive`, recurring entries with `duration`, and entries with `rrule`. Add data-testid markers to verify:
- Entries with `url` are navigable (click test shows the URL was triggered)
- Entries with `interactive: true` receive keyboard focus and respond to Enter key
- Recurring entries with `duration` render with the correct multi-day span
- RRule-based recurring entries render occurrences correctly

Add Playwright spec at `e2e-tests/tests/phase1-entry-model.spec.js` to verify DOM output and interaction.

---

## Files to Modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/Entry.java`
  - Add `url` (String), `interactive` (Boolean) fields
  - Add `recurringDuration` (String with `@JsonName("duration")`) field
  - Add `rrule` field with custom serialization, `exdate` (String), `exrule` (String)
  - Consider `overlap` type change from `boolean` to `Boolean`
- `addon/src/main/resources/.../full-calendar.ts` (frontend TypeScript companion file)
  - Add `rrulePlugin` import and add to plugins array — **required for 1.4 only**
- `addon/src/main/java/org/vaadin/stefan/fullcalendar/` (new class)
  - `RRule.java` — typed rrule builder class with `toJson()` method
