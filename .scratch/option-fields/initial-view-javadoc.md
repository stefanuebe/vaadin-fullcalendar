# Javadoc of `Option.INITIAL_VIEW` asks for `getClientSideValue()` without need

| | |
|---|---|
| GitHub | – (not created, bot account flagged) |
| Labels | Docs, ready-for-agent |
| Status | open (found 2026-10-15) |
| Blocked by | – |

---

## What to build

The Javadoc of `Option.INITIAL_VIEW` says to pass a view's client-side value, for example `CalendarViewImpl.TIME_GRID_WEEK.getClientSideValue()`. `setOption` unwraps every `ClientSideValue` itself (`JsonUtils.toJsonNode`, before and after attach), so `setOption(Option.INITIAL_VIEW, CalendarViewImpl.TIME_GRID_WEEK)` works. Getting-Started already uses that form (maintainer's wish). The Javadoc, and the demo / e2e views that call `getClientSideValue()`, should show the direct form.

## Acceptance criteria

- [ ] The Javadoc of `INITIAL_VIEW` shows passing a `CalendarView` directly.
- [ ] A unit test pins that the client gets the view key for a `CalendarView` value.

## Where

Code repository, branch from `master`, PR.
