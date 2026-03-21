# UC-009: Entry Providers (Data Management)

**As a** Vaadin application developer, **I want to** manage calendar entries using a data provider pattern **so that** I can efficiently populate the calendar from different data sources.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.LAZY_FETCHING`
**Related Events:** —

---

## User-Facing Behavior

- Entries appear on the calendar based on the configured `EntryProvider`
- With `InMemoryEntryProvider`: all entries are sent to the client; the client filters by visible date range
- With `CallbackEntryProvider`: only entries for the current visible range are fetched from the server
- Adding/removing/updating entries through the provider automatically refreshes the calendar display
- **Loading state**: While a `CallbackEntryProvider` is fetching, FullCalendar renders the view grid with no entries (no built-in loading indicator). Developers who need a loading spinner should listen for `DatesRenderedEvent` and toggle their own indicator.
- **Empty state**: When no entries exist in the visible range, the calendar shows empty grid cells (no built-in "no events" message). List views show a `noEventsText` message (configurable).

---

## Java API Usage

```java
// In-memory (small datasets)
InMemoryEntryProvider<Entry> provider = calendar.getEntryProvider().asInMemory();
provider.addEntry(entry1);
provider.addEntry(entry2);
provider.removeEntry(entry1);
provider.refreshAll(); // force full refresh

// Callback (large/database-backed datasets)
// Requires two lambdas: fetch-by-range and fetch-by-id
CallbackEntryProvider<Entry> provider = EntryProvider.fromCallbacks(
    query -> myService.findEntries(query.getStart(), query.getEnd()).stream(),
    id -> myService.findEntryById(id)
);
calendar.setEntryProvider(provider);

// Refresh single entry
provider.refreshItem(entry);

// Refresh all
provider.refreshAll();
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Default provider is `InMemoryEntryProvider` |
| BR-02 | `CallbackEntryProvider` queries are triggered on view change and navigation |
| BR-03 | `LAZY_FETCHING = true` (default) prevents re-fetching when navigating within already-fetched ranges |
| BR-04 | `refreshAll()` sends all entries to the client (in-memory) or re-triggers the callback |
| BR-05 | `refreshItem()` sends a single entry update to the client |
| BR-06 | Entry cache is bounded to 10,000 entries (LRU eviction) |
| BR-07 | `asInMemory()` is a cast helper — it throws `ClassCastException` if the provider is not `InMemoryEntryProvider`. Only use when the default provider is active or after explicitly setting an in-memory provider. |

---

## Acceptance Criteria

- [ ] Entries added via InMemoryEntryProvider appear on the calendar
- [ ] Removing entries from the provider removes them from display
- [ ] CallbackEntryProvider fetches entries per visible range
- [ ] `refreshAll()` updates the display
- [ ] `refreshItem()` updates a single entry without full refresh
- [ ] Large datasets (> 1000 entries) work with CallbackEntryProvider

---

## Tests

### Unit Tests
- [ ] `InMemoryEntryProviderTest` — CRUD operations

### E2E Tests
- [ ] `entry-provider.spec.js` — provider behavior

---

## Related FullCalendar Docs

- [Event Sources](https://fullcalendar.io/docs/event-source-object)
- [lazyFetching](https://fullcalendar.io/docs/lazyFetching)
