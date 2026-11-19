# 76: The theme drag test depends on today's date

| | |
|---|---|
| GitHub | #404 |
| Labels | bug |
| Status | done (2026-10-17, 818813dd) |
| Blocked by | – |
| Spec | – |

---

**What to build:** `vaadin-theme.spec.js`, "an entry dragged out of a dark calendar keeps the theme's colors", fails when today is not in the first week row of the month. `VaadinThemeTestView` puts its entries on today and squeezes seven calendars into the page (`setSizeFull`, flex grow), so each calendar shows only its first row and scrolls the rest. The test takes the entry's bounding box and drives the mouse without scrolling it into view, so on 2026-10-17 (third row) no drag starts and no mirror appears. It fails on `master` too (checked 2026-10-17 during ticket 70). The popover test beside it passes, because `click()` scrolls into view.

**Blocked by:** none

**Status:** done

- [x] The drag test passes whatever today's date is, for example by scrolling the entry into view before reading its bounding box, or by a fixed initial date in the view.
- [x] The test still fails with the bug it guards put back (the mirror without the theme class).
