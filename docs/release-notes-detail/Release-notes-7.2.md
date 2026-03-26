This page gives you an overview of the major changes that came with the release of
[FullCalendar for Flow, version 7.2](https://vaadin.com/directory/component/full-calendar-flow).

Version 7.2 requires **Vaadin 25.1** (up from 25.0 in 7.1).

## Auto-Revert for Unapplied Entry Changes (#225)

When a user drags or resizes an entry, FullCalendar JS immediately moves the entry to its new position on the client. The server receives an `EntryDroppedEvent` or `EntryResizedEvent`. Previously, if the developer chose not to call `event.applyChangesOnEntry()` (e.g., after a validation failure), the client would keep showing the entry at the new position while the server still had the old data — an inconsistent state.

Starting with 7.2, the calendar **automatically reverts** the client-side entry to its original position when `applyChangesOnEntry()` is not called. This is controlled by `setAutoRevertUnappliedEntryChanges(boolean)`, which defaults to `true`.

### Usage

```java
// Auto-revert is enabled by default — no configuration needed.
// Entries revert automatically when applyChangesOnEntry() is not called.

calendar.addEntryDroppedListener(event -> {
    if (isDropAllowed(event)) {
        event.applyChangesOnEntry();       // entry stays at new position
        entryProvider.refreshItem(event.getEntry());
    }
    // If not called: entry reverts to original position on the client
});
```

To restore the previous behavior (client keeps the new position regardless):

```java
calendar.setAutoRevertUnappliedEntryChanges(false);
```

### How It Works

1. When a drop or resize occurs, the FullCalendar JS `revert()` function is captured on the client side.
2. On the server, after all event listeners have been executed, a `beforeClientResponse` callback checks whether `applyChangesOnEntry()` was called.
3. If not called (and auto-revert is enabled), the server sends a `revertEntry` command to the client, which triggers FullCalendar's native revert animation.
4. If `applyChangesOnEntry()` was called, the pending revert is cleared — the entry stays at the new position.

### Notes

- Auto-revert works for `EntryDroppedEvent`, `EntryResizedEvent`, and `EntryDroppedSchedulerEvent`.
- The revert uses FullCalendar's native revert mechanism, which provides a smooth animation back to the original position.
- The `isChangesApplied()` method on `EntryDataEvent` indicates whether `applyChangesOnEntry()` was called.

## Signal Binding — Reactive Entry Management (Experimental)

Entries can now be bound to the calendar via Vaadin 25.1 Signals. When entries are added, removed, or modified through the signal, the calendar updates automatically — no manual `refreshItem()` or `refreshAll()` calls needed.

**Note:** This feature depends on Vaadin's experimental Signals API (25.1). The API may change in future Vaadin versions.

### Usage

```java
ListSignal<Entry> entries = new ListSignal<>();

// Bind — calendar reactively updates from the signal
calendar.bindEntries(entries);

// Add entry — appears on calendar automatically
ValueSignal<Entry> meeting = entries.insertLast(myEntry);

// Modify entry — calendar updates automatically
meeting.modify(e -> e.setTitle("Updated Title"));

// Remove entry — disappears from calendar automatically
entries.remove(meeting);

// Unbind — restores default InMemoryEntryProvider
calendar.bindEntries(null);
```

### Builder Support

```java
ListSignal<Entry> entries = new ListSignal<>();
FullCalendar calendar = FullCalendarBuilder.create()
    .withSignalBinding(entries)
    .build();
```

### Rules

- `bindEntries()` and `setEntryProvider()` are mutually exclusive. Calling one while the other is active throws `BindingActiveException`.
- `bindEntries()` requires `autoRevertUnappliedEntryChanges` to be `true` (the default).
- `applyChangesOnEntry()` in drop/resize listeners automatically routes through `signal.modify()` when a signal binding is active, ensuring all effects observe the change.
- Direct mutation of Entry objects (e.g., `entry.setTitle(...)`) does **not** trigger reactive updates. Always use `ValueSignal.modify()`.
- `withSignalBinding()` and `withEntryProvider()` are mutually exclusive in the builder.

### Resource Binding (Scheduler)

Resources on `FullCalendarScheduler` can also be bound via Signals:

```java
ListSignal<Resource> resources = new ListSignal<>();
ListSignal<Entry> entries = new ListSignal<>();

scheduler.bindResources(resources);
scheduler.bindEntries(entries);

// Add resource — appears in timeline automatically
ValueSignal<Resource> room = resources.insertLast(
    new Resource(null, "Room A", "#3788d8"));

// Add entry with resource reference
ResourceEntry entry = new ResourceEntry();
entry.setTitle("Meeting");
entry.addResources(room.peek());
entries.insertLast(entry);

// Modify resource — display updates automatically
room.modify(r -> r.setTitle("Room A (renovated)"));
```

- `bindResources()` and manual resource management (`addResource`, `removeResource`, `removeAllResources`) are mutually exclusive — throws `BindingActiveException`.
- `bindResources(null)` unbinds and clears all resources.
- `bindResources()` and `bindEntries()` are independent — you can use one or both.
