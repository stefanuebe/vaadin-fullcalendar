# README describes 8.0

| | |
|---|---|
| GitHub | – (not created, bot account flagged) |
| Labels | Docs, ready-for-agent |
| Status | open |
| Blocked by | – |
| Blocks | #343 (release) |

---

## What to build

The README on `master` still describes 7.x. It says the add-on is based on version 6 of the FullCalendar library, and its version list has no version 8. The wiki Home page sends readers to the README for supported versions, so the README has to be right when 8.0 is released.

- The intro names the library line 8.0 wraps (library 7.1), following the version rules in `CONTEXT.md`.
- The version list adds "Version 8, based on Vaadin 25.3 / Java 21" and says which older versions are still supported.
- The README follows the *Writing* section of `STYLEGUIDE.md` (for example "add-on" instead of "addon").

## Acceptance criteria

- [ ] Intro and version list match 8.0 (`master`).
- [ ] The README follows the writing rules.
- [ ] Three-agent review ran, the maintainer reviewed the change.

## Where

Code repository, branch from `master`, merged through a PR. The 7.x README on `v7_master` stays as it is.
