# Fix brief (ticket 67)

You fix the Javadoc (and only the Javadoc) of the option fields of ONE part of the review. Repo root is your working
directory (a git worktree on its own branch). Write English. Chat rules do not apply here.

## Rules (critical)

- Edit only Javadoc comments of fields and their setters in `addon/src/main/java/org/vaadin/stefan/fullcalendar/ViewOptions.java`,
  `CalendarOptions.java`, `addon-scheduler/src/main/java/org/vaadin/stefan/fullcalendar/SchedulerViewOptions.java`,
  `SchedulerCalendarOptions.java`, and value types under `…/fullcalendar/option/`. No code changes, no other files.
  These code changes are done by someone else, do not touch them: `Order` comma check, `hiddenDays` seven-day check,
  removal of `ListDayFormatValue.NONE` (for `listDayFormat`/`listDayAltFormat` write the Javadoc as if `NONE` did not
  exist and add: "The library docs also name `false` to hide the text, which the used library version ignores.").
- The library docs and code are git-ignored, so they exist only at the absolute paths given here.
- Never invent library behaviour. Every new fact needs a source: the reports/verify files below, `browser-checks.md`,
  the library docs `/home/suebe/workspace/directory/vaadin-fullcalendar/fc-docs/_docs-v7/`, or the library code `/home/suebe/workspace/directory/vaadin-fullcalendar/e2e-test-app/node_modules/fullcalendar/chunks/`,
  `/home/suebe/workspace/directory/vaadin-fullcalendar/e2e-test-app/node_modules/fullcalendar-scheduler/chunks/`. If unsure, leave the text as it is and list it in your
  final reply as "unsure".
- Never name a FullCalendar version number in Javadoc. Say "the library docs" and "the used library version".
- Do not run Maven, npm or a server. Do not switch branches. When done: `git add` the files you changed (explicit
  paths) and commit with message "Fix the Javadoc of the option fields, part N (ticket 67)" followed by an empty
  line and `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. No other trailer lines.
- Lines max 120 characters. Keep each `{@link …}` and `{@code …}` on one line. Follow the Javadoc conventions in
  `.scratch/option-fields/issues/README.md` (line 45 ff.) and the writing rules in `STYLEGUIDE.md`. Plain, short
  sentences. Do not reword correct text.

## What to fix, for every option in your part file

Inputs in `.scratch/option-fields/67-review/`: your `partN.md` (the options), `reportN.md` (findings),
`verify-A.md`, `verify-B.md`, `verify-C.md` (the verifiers' corrections: where they say a proposed fix is wrong,
follow the verifier), `browser-checks.md` (settled facts, they override the reports).

1. Every finding of `reportN.md` the verifiers confirmed: fix the Javadoc. Also the findings the verifiers add for
   your options (search the verify files for each option name of your part).
2. Library docs versus library code: where they disagree, the Javadoc names both ("The library docs say …; the used
   library version …"). Format callbacks get an info object, not a date (see `slotHeaderFormat` for the existing note).
3. True facts the old Javadoc had (the `Option` / `SchedulerOption` constant in `addon/.../Option.java`,
   `addon-scheduler/.../SchedulerOption.java`) that got lost: bring them back. This includes the note that the
   library docs describe the option only partly or not at all, where the old constant had it, and `@see` links whose
   page does not exist in `/home/suebe/workspace/directory/vaadin-fullcalendar/fc-docs/_docs-v7/index.md` (then link the page that names the option, or drop the link
   and say the docs do not describe it).
4. Library default (decision D1): EVERY field of your part names the library default in one sentence, "The library
   default is …." Take it from the library code (`BASE_OPTION_DEFAULTS` and the scheduler defaults; the reports and
   verifiers cite the lines) or the docs page; where the view, the theme or the locale decides, say that instead.
   Where the library has no default (the option is unset), say what happens without a value. If the add-on itself
   sets the option by default, say that too (check `FullCalendar.java` / `full-calendar.ts`). Keep the existing
   sentence "The getter returns null while the option is not set; then the library default applies." where present.
5. Info lists of callbacks name what the library passes; where the library passes `el`, include it (decision D9).

## Final reply

Under 150 words: options changed (count), findings applied, findings skipped with reason, "unsure" items.
