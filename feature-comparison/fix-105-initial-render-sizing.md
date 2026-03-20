# Fix 105: Fix broken initial render / CSS layout until resize

## Symptom

The calendar renders with broken CSS/layout on first display. A manual browser resize
(or window resize) fixes it immediately. This happens because `updateSize()` is triggered
by the ResizeObserver, but the initial render fires before the browser has finished
calculating the element's final dimensions.

## Root cause

In `full-calendar.ts`, `initCalendar()` calls `this._calendar.render()` (line 122) and
then sets up a ResizeObserver. The ResizeObserver calls `updateSize()` via
`requestAnimationFrame` when the element's size changes.

Problem: on initial attach, the browser may not have finalized the element's layout
when `render()` runs. The ResizeObserver does not fire for the *initial* layout
(no previous size to compare to), so `updateSize()` is never called after the
browser finishes laying out the page. The broken state persists until the next
real resize event.

Common trigger scenarios:
- Calendar inside a Vaadin tab (tab content has no dimensions until tab is selected)
- Calendar inside a dialog or drawer (not visible until opened)
- Calendar inside a lazy-loaded view
- Any container that isn't fully laid out when the component is first connected to the DOM

## Fix

After `this._calendar.render()`, schedule a deferred `updateSize()` call so it runs
after the browser has finished the initial layout:

```typescript
this._calendar.render();

// Deferred updateSize to fix initial layout when the container dimensions
// are not yet finalized at render time (e.g. inside tabs, dialogs, lazy views).
requestAnimationFrame(() => {
    requestAnimationFrame(() => {
        this._calendar?.updateSize();
    });
});
```

Two nested `requestAnimationFrame` calls are used to ensure the call happens after
the browser has both committed the layout and painted the frame. A single
`requestAnimationFrame` sometimes still fires before layout is complete in certain
container configurations.

## Files to change

- `addon/src/main/resources/META-INF/resources/frontend/vaadin-full-calendar/full-calendar.ts`
  — add the deferred `updateSize()` after `this._calendar.render()` in `initCalendar()`

## Testing

After the fix, verify:
- Calendar renders correctly on first display without needing a resize
- Calendar inside a Vaadin tab renders correctly when the tab is selected
- Existing resize behavior (ResizeObserver) is unaffected
- No double-render flicker visible to the user (the deferred call should be invisible)

## Note

This is a frontend-only change. No Java changes required.
