# Next Step 3: Extend E2E Tests — RRule / exdate

## Goal
The existing RRule E2E test only checks that "weekly occurrences render".
Extend it to cover `exdate`, monthly patterns, and `ofRaw` — the cases where
we are uncertain whether our implementation matches what FC actually expects.

## Status
✅ DONE — commit b09e2de

## Background / risk
- `exdate` is sent as a comma-separated string (`"2025-03-10,2025-03-17"`).
  FC docs show it as an array (`['2025-03-10']`). We need an E2E test to confirm
  the string form is accepted.
- `byWeekday("-1fr")` = last Friday of the month — needs to verify the event
  appears on the correct day, not just any Friday.
- `ofRaw` string — needs to verify occurrences actually render.

## Test view additions (`EntryModelTestView.java`)
Add these entries (all fixed to March 2025 so the calendar's initial date covers them):

```java
// exdate: weekly on Monday, but skip March 10
Entry exdateEntry = new Entry();
exdateEntry.setTitle("Exdate Test");
exdateEntry.setAllDay(true);
exdateEntry.setRrule(RRule.weekly()
    .dtstart(LocalDate.of(2025, 3, 3))
    .until(LocalDate.of(2025, 3, 31))
    .byWeekday(DayOfWeek.MONDAY));
exdateEntry.setExdate("2025-03-10");  // skip second Monday

// monthly last-Friday
Entry lastFridayEntry = new Entry();
lastFridayEntry.setTitle("Last Friday");
lastFridayEntry.setAllDay(true);
lastFridayEntry.setRrule(RRule.monthly()
    .dtstart(LocalDate.of(2025, 1, 1))
    .until(LocalDate.of(2025, 3, 31))
    .byWeekday("-1fr"));

// ofRaw
Entry rawEntry = new Entry();
rawEntry.setTitle("Raw RRule");
rawEntry.setAllDay(true);
rawEntry.setRrule(RRule.ofRaw("FREQ=WEEKLY;BYDAY=WE;DTSTART=20250305;UNTIL=20250331"));
```

## Playwright test additions (`entry-model.spec.js`)

```js
test('exdate: skipped Monday does not render', async ({ page }) => {
    // Mondays in March 2025: 3, 10, 17, 24, 31 = 5 total, minus skipped March 10 = 4
    const occurrences = page.locator('.fc-event:has-text("Exdate Test")');
    await expect(occurrences).toHaveCount(4);
});

test('exdate: non-skipped Mondays still render', async ({ page }) => {
    // March 3, 17, 24, 31 should appear; March 10 should not
    // Check by locating events on specific day cells
    const march10Cell = page.locator('[data-date="2025-03-10"]');
    await expect(march10Cell.locator('.fc-event:has-text("Exdate Test")')).toHaveCount(0);
    const march17Cell = page.locator('[data-date="2025-03-17"]');
    await expect(march17Cell.locator('.fc-event:has-text("Exdate Test")')).toHaveCount(1);
});

test('monthly last-friday renders on correct day', async ({ page }) => {
    // Last Friday of March 2025 = March 28
    const march28Cell = page.locator('[data-date="2025-03-28"]');
    await expect(march28Cell.locator('.fc-event:has-text("Last Friday")')).toHaveCount(1);
    // Should NOT appear on March 21 (not last Friday)
    const march21Cell = page.locator('[data-date="2025-03-21"]');
    await expect(march21Cell.locator('.fc-event:has-text("Last Friday")')).toHaveCount(0);
});

test('ofRaw renders occurrences', async ({ page }) => {
    // BYDAY=WE in March 2025 = Wednesdays: 5, 12, 19, 26 = 4 occurrences
    const occurrences = page.locator('.fc-event:has-text("Raw RRule")');
    await expect(occurrences).toHaveCount(4);
});
```

## Notes
- If `exdate` as a string fails (0 skipped), it confirms a bug — we need to fix
  the implementation to send an array instead of a comma-separated string.
- Run tests with the app at `2025-03-01` initial date so all events are visible.

## Docs / Javadoc updates required alongside any fixes
Any bug fix found here must be reflected consistently in:
- `Entry.java` Javadoc for `exdate` field (currently says "comma-separated ISO date strings")
- `docs/Release-notes.md` — line describing `Entry.setExdate(String)` in 7.1.x section
- `docs/Features.md` — entry model table row for "Exclusion dates"
- `docs/Samples.md` — RRule section `planning.setExdate(...)` example
- `demo/.../RRuleSample.java` — sample class `setExdate(...)` call
If the type changes from `String` to e.g. `List<LocalDate>` or the serialization
changes to a JSON array, all of the above must be updated in the same PR.

## Mandatory: Code Review
Run the `code-reviewer` agent on all changed files before marking this step complete.
This is non-negotiable — no step is done without a code review.

## Mandatory: End-User Review
Run the `end-user-reviewer` agent on any changed docs or Javadoc.
Fix all findings and re-run until the reviewer gives a clean pass.
No step is done without end-user sign-off on documentation.
