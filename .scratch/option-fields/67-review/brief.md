# Option-by-option review (ticket 67)

Repo: /home/suebe/workspace/directory/vaadin-fullcalendar (branch master, do not switch).
READ-ONLY: never edit repo files, never run git checkout/stash/reset/switch, never run Maven or npm. Write only your
report file (path given in your prompt). Answer from sources only; never invent library behaviour. If unsure, say
"unsure" and why. Stay inside the wrapper's scope: no speculative use cases, no "a user might want…" findings.

## Task

For each option in your part file, compare the old constant (`Option` in
`addon/src/main/java/org/vaadin/stefan/fullcalendar/Option.java`, `SchedulerOption` in
`addon-scheduler/src/main/java/org/vaadin/stefan/fullcalendar/SchedulerOption.java`) with its successor field and the
field's setters (`CalendarOptions.java`, `ViewOptions.java` in addon, `SchedulerCalendarOptions.java`,
`SchedulerViewOptions.java` in addon-scheduler; value types in `…/fullcalendar/option/*.java` of both modules).
The part file names the field, its type, setters, the library `.d.ts` type and the old Type block per option.

Three axes, Javadoc first:

1. **Javadoc (main focus).** The old Javadoc is the language frame; the facts come from the library.
   - Check every factual statement of the field's Javadoc (and of its setters) against the library docs
     `fc-docs/_docs-v7/` (`index.md` lists every page) and, where they disagree or the docs are silent, against the
     library code: `e2e-test-app/node_modules/fullcalendar/chunks/*.js|*.d.ts`,
     `e2e-test-app/node_modules/fullcalendar-scheduler/chunks/*`, `e2e-test-app/node_modules/@full-ui/headless-calendar/index.js`.
   - A statement about the add-on itself is checked against the add-on: `addon/src/main/resources/META-INF/frontend/vaadin-full-calendar/full-calendar.ts`
     `FullCalendar.java`, `FullCalendarScheduler.java`.
   - Where docs and library code disagree, the code wins (it is what runs). Then the field's Javadoc must say so,
     naming what the docs say and what the used library version does. Report missing such notes.
   - Parts of the old Javadoc were written for an older library version. Known examples: `moreLinkHint` sets
     `title`, not `aria-label`; a newline in `allDayText` is no line break. Report any carried-over statement that is
     wrong for the used library.
   - Also report: a fact the old Javadoc had that is still true and useful but got lost in the new one; the
     default value stated wrongly; a wrong @see link (page must exist in fc-docs index for that option).
   - Wording style is NOT a finding unless it misleads.
2. **Values.** Does the field take every value and form the library takes for the option (its `.d.ts` type, its
   refiner in the chunks, its docs page), and nothing the library rejects? Does the converter send the value as the
   library reads it (converters in `addon/src/main/java/org/vaadin/stefan/fullcalendar/converters/`)?
3. **Mapping.** Where a library form is not mapped 1:1, is the reason concrete and written down in the Javadoc and in
   a ticket under `.scratch/option-fields/issues/` (grep for the option name)?

## Do not report

- Options the add-on handles through Vaadin itself, covered by their own tickets: `navLinkDayClick`,
  `navLinkWeekClick`, `moreLinkClick`, `viewDidMount`, `height`, and `class`/`className` aliasing.
- Known gaps with their own tickets: weeks in durations and the whole Javadoc of the duration fields
  (`dateIncrement`, `defaultAllDayEntryDuration`, `defaultTimedEntryDuration`, `duration`, `nextDayThreshold`,
  `scrollTime`, `slotDuration`, `slotHeaderInterval`, `slotMaxTime`, `slotMinTime`, `snapDuration`, ticket 70: skip
  these options entirely); `resourceIds` in constraints (71); the missing `ResourceColumn` properties (72);
  `locale` as array or `LocaleInput` (73); `initialDate` with a time (74).
- Wording polish, Javadoc formatting, naming.

## Report

Write your report as Markdown to the file named in your prompt. List only findings, each:
`### <option>` then: **Axis** (Javadoc / Values / Mapping), **Where** (repo-relative file:line of the field or
setter Javadoc), **Claim** (quote the wrong or missing statement), **Source** (file:line or docs page + quote that
proves it), **Fix** (proposed English sentence(s) for the Javadoc, or what the field/converter must change),
**Confidence** (high / medium / unsure). End with "Checked N options, M findings." Then reply with only that
last line.
