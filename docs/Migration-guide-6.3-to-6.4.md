# Migrating from 6.3 to 6.4

Version 6.4 modernizes the addon for Vaadin 24.10 while backporting v7 features. This guide covers the upgrade path and breaking changes.

> For earlier versions (6.2 or 6.1), see the [v6.0 migration guide](migration) first.

## Prerequisites

Before upgrading to 6.4, ensure your project meets these requirements:

| Requirement | Minimum Version |
|-------------|-----------------|
| **Java** | 17 |
| **Vaadin** | 24.10.x |
| **Spring Boot** | 3.5+ (demo tested with this version) |
| **Maven** | 3.8+ |

**Note**: Vaadin 14 is no longer supported. If you're on Vaadin 14, you must upgrade to Vaadin 24.10 first.

## Step 1: Update Maven Dependencies

In your `pom.xml`, update the FullCalendar addon version:

```xml
<dependency>
    <groupId>org.vaadin.stefan</groupId>
    <artifactId>fullcalendar2</artifactId>
    <version>6.4.0</version>
</dependency>

<!-- For Scheduler support -->
<dependency>
    <groupId>org.vaadin.stefan</groupId>
    <artifactId>fullcalendar2-scheduler</artifactId>
    <version>6.4.0</version>
</dependency>
```

Run Maven to fetch the new version:

```bash
mvn clean dependency:tree
```

## Step 2: Verify Java and Vaadin Versions

Check your `pom.xml` or `gradle.properties` for the following:

```xml
<!-- Ensure Java 17+ -->
<maven.compiler.source>17</maven.compiler.source>
<maven.compiler.target>17</maven.compiler.target>

<!-- Ensure Vaadin 24.10+ -->
<vaadin.version>24.10.0</vaadin.version>
```

If you're still on Java 11 or Vaadin 23, upgrade them now:

```bash
mvn clean compile
```

Fix any compilation errors related to Java 17 syntax or Vaadin API changes.

## Step 3: Handle Breaking Changes

### Overlap Field Type Change

The `Entry.overlap` field has changed from `boolean` (primitive) to `Boolean` (nullable):

**Before (6.3)**:
```java
entry.setOverlap(true);
boolean overlap = entry.isOverlap();  // Returns primitive boolean
```

**After (6.4)**:
```java
entry.setOverlap(true);
Boolean overlap = entry.getOverlap();  // Returns nullable Boolean
```

**What changed**:
- `isOverlap()` is deprecated in 6.4; `getOverlap()` is the non-deprecated replacement
- `boolean` → `Boolean` (type changed to nullable)
- `null` is now a distinct state: when null, the entry inherits the calendar-level `eventOverlap` setting; when `false`, overlap is always denied for this entry

**Migration**:
- Replace `isOverlap()` calls with `getOverlap()`. `isOverlap()` still compiles but is deprecated and coalesces `null` to `true`; `getOverlap()` gives you the raw nullable value
- If you need a primitive, use: `Boolean overlap = entry.getOverlap(); boolean value = overlap != null && overlap;`
- Update any comparisons: `if (overlap != null && overlap)` instead of `if (overlap)`

The binary layout is compatible, but **recompilation is required**.

## Step 4: Update Deprecated Method Calls (Optional)

While deprecated methods still work, consider replacing them with the unified `setOption()` API for consistency.

### Common Deprecated Methods → Replacements

| Deprecated Method | Replacement |
|-------------------|-------------|
| `setFirstDay(DayOfWeek)` | `setOption(Option.FIRST_DAY, DayOfWeek.MONDAY)` |
| `setWeekends(boolean)` | `setOption(Option.WEEKENDS, true)` |
| `setEntryDidMountCallback(String)` | `setOption(Option.ENTRY_DID_MOUNT, JsCallback.of(...))` |

**Example**:

```java
// Before (6.3)
calendar.setFirstDay(DayOfWeek.MONDAY);
calendar.setWeekends(false);

// After (6.4) — preferred approach
calendar.setOption(Option.FIRST_DAY, DayOfWeek.MONDAY);
calendar.setOption(Option.WEEKENDS, false);
```

Deprecated methods are still functional and have `@Deprecated` annotations visible in your IDE. Replace them at your convenience — they remain supported in 6.4.

## Step 5: Optional — Adopt New v7 Features

### Use RRule for Recurring Entries

If you were using the legacy recurrence fields (`recurringDaysOfWeek`, `recurringStartDate`, `recurringEndDate`), consider switching to RRule:

**Before (6.3 — legacy approach)**:
```java
Entry entry = new Entry("Weekly Meeting");
entry.setStart(LocalDateTime.of(2025, 3, 24, 10, 0));
entry.setEnd(LocalDateTime.of(2025, 3, 24, 11, 0));
entry.setRecurringDaysOfWeek(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
entry.setRecurringStartDate(LocalDate.of(2025, 3, 1));
entry.setRecurringEndDate(LocalDate.of(2025, 12, 31));
calendar.getEntryProvider().asInMemory().addEntry(entry);
```

**After (6.4 — RRule approach)**:
```java
Entry entry = new Entry("Weekly Meeting");
entry.setStart(LocalDateTime.of(2025, 3, 24, 10, 0));
entry.setEnd(LocalDateTime.of(2025, 3, 24, 11, 0));

RRule rule = RRule.weekly()
    .byWeekday(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
    .until(LocalDate.of(2025, 12, 31));

entry.setRRule(rule);
calendar.getEntryProvider().asInMemory().addEntry(entry);
```

Both approaches work, but RRule is more powerful (supports exclusions, complex patterns, RFC 5545 import).

### Adopt New Entry Fields

The new fields (`url`, `interactive`) are optional but recommended:

```java
Entry event = new Entry("Conference Talk");
event.setUrl("https://conference.example.com/talks/opening-keynote");
event.setInteractive(true);  // Allows dragging and resizing
calendar.getEntryProvider().asInMemory().addEntry(event);
```

## Step 6: Handle New NPM Dependencies

The following npm packages are automatically loaded:

- `@fullcalendar/rrule`
- `@fullcalendar/google-calendar`
- `@fullcalendar/icalendar`
- `ical.js`

**You don't need to do anything** — they're bundled automatically in the addon JAR. If you have a custom npm build process, no changes are required.

## Step 7: Recompile and Test

```bash
mvn clean verify
```

If you're using Spring Boot, restart the development server:

```bash
mvn spring-boot:run
```

Test the following:

1. Calendar renders and loads entries
2. Drag and drop still work
3. Entry creation/editing dialogs function
4. Custom styling (if any) still applies
5. Entry provider (if used) still loads data correctly

## Troubleshooting

### Compilation Error: "cannot find symbol: method isOverlap()"

**Cause**: This error occurs if the method was removed in a future version or if the addon dependency is not correctly resolved. In 6.4, `isOverlap()` is deprecated but still available.

**Fix**:
```java
// Replace:
boolean value = entry.isOverlap();

// With:
Boolean value = entry.getOverlap();
```

### Compilation Error: "incompatible types: Boolean cannot be converted to boolean"

**Cause**: You're assigning the result of `getOverlap()` to a primitive `boolean`.

**Fix**:
```java
// Replace:
boolean value = entry.getOverlap();

// With:
Boolean value = entry.getOverlap();
// Or, to convert to primitive:
boolean value = entry.getOverlap() != null && entry.getOverlap();
```

### Calendar Not Rendering

**Cause**: Vaadin 24.10 or Java 17 not properly configured.

**Check**:
```bash
mvn compile
java -version  # Should be 17+
grep "vaadin.version" pom.xml
```

Ensure both are correct, then clear your browser cache and reload:
```bash
# Clear Vaadin frontend cache
rm -rf frontend
rm -rf target
mvn clean install
```

### RRule Entries Not Recurring

**Cause**: RRule entries may require the `@fullcalendar/rrule` plugin, which is included but may need a page reload.

**Fix**: Hard-refresh your browser (Ctrl+Shift+R / Cmd+Shift+R) to clear cached JavaScript.

## Important Notes

- **No automatic migration script**: Breaking changes are minimal, but review code for `isOverlap()` and deprecated method calls.
- **Backward compatibility**: Apart from the `overlap` field type change, v6.3 code should work with minimal changes.
- **Binary compatibility**: If you compiled v6.3 with `boolean` overlap, recompilation against v6.4 is required.

## Next Steps

- Review the [Release Notes](Release-notes-6.4) for new features
- Check the [demo module](https://github.com/stefanuebe/vaadin-fullcalendar) for usage examples
- File issues on [GitHub](https://github.com/stefanuebe/vaadin-fullcalendar/issues) if problems arise

---

*Revision: March 2026*
