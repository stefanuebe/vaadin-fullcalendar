This page gives you an overview of the major changes that came with the release of
[FullCalendar for Flow, version 7.2](https://vaadin.com/directory/component/full-calendar-flow).

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
