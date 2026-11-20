# Style Guide

The documented coding standards for this project. `/code-review` reads this file as
the repo's standards source for its Standards axis, so keep every rule concrete and
quotable. Extend it with project-specific conventions as they emerge.

> **Relationship to other files**
> - This file is the **single source of truth** for code standards. Keep the
>   normative rules in this one place.
> - This project has no formatter or linter in the build (no Spotless, no
>   Checkstyle), so every rule here is checked by review. If tooling is added
>   later, its config becomes the source for what it enforces.
> - Decisions about *how the system is built*, rather than how code is written, are
>   ADRs under `docs/adr/`. Cite them here where they constrain the code.
> - Which layer a test belongs in, and what may not be asserted against the demo,
>   is in `CLAUDE.md`. This file holds the craft rules.

---

## 1. Audience

Code is written for **human Java developers**, not for the machine and not for the
AI that wrote it. It must read top-to-bottom like prose: easy to scan, easy to
change. Optimise for the next reader.

- Follow **clean code** and **YAGNI**. An abstraction earns its place by a need
  that exists today, not one that might.
- **Don't invent or work around something that already has a solution.** Reach for
  the libraries already on the classpath before adding a new one, and for the
  framework's own API before hand-rolling around it.

## 2. Formatting & whitespace

These are mechanical and non-negotiable:

- **Always use `{}` blocks** for one-line `if`/`else`/`for`/`while`. Never use the
  braceless form.
- A **guard clause / early-return `if`** is followed by exactly one blank line
  before the main logic, *unless* the next line closes the method (`}`).
- The **terminating `return`** that produces the method's result is preceded by
  exactly one blank line, separating it from the code that built the value,
  *unless* it is the method's only statement, or it sits immediately after the
  opening `{`.
- **Multiline comments** are always preceded by one blank line.
- **At most one blank line in a row**, in code and in comments.

The blank-line rules are easy to miss by reading: working through a long file
by eye finds only a fraction of the real misses. *(Optional)* A small scanner script
(e.g. `tools/scan-blank-lines.py <paths>`) that favours recall over precision does
better; its output is adjudicated, not applied.

## 3. Method shape (judgment)

Read a method as a sequence of phases — **guard → setup → work → result** — and
separate those phases with a single blank line. Do not clutch everything into one
dense block.

Put a blank line *between* phases such as:

- an object initialised and configured over multiple lines,
- a larger block (e.g. a loop) preceded by its initialisers,
- a long call chain (e.g. a stream) spanning multiple lines.

**Do not over-separate.** A blank line goes *between* phases, not between every
statement. Tightly related one-liners stay together.

## 4. Naming & imports

- **No fully-qualified names** in code unless genuinely unavoidable (name clash).
  Use `import` instead.
- Names state intent. A reader should understand a variable/method from its name
  without reading the body.
- Use the terms in `CONTEXT.md` for classes, methods and tests.

## 5. Comments

- Less is more. Keep comments **short and focused on the WHY**.
- The WHAT must be obvious from the code itself. If it isn't, rewrite the code
  rather than explaining it in a comment.
- No commented-out code without a stated reason.
- **The code is not your logbook.** A comment records why the code is the way it
  is, never what it used to be, what was tried first, or what changed in this
  round. That belongs in the commit message.

## 6. Structure & visibility

- **Nested types** (inner classes) belong at the **bottom** of the owning class.
- **Member order in a TypeScript module:** types, then constants, then functions,
  then the class. A type never stands between functions or methods.
- **Member order in a class**, Java and TypeScript alike:
  1. fields
  2. constructors
  3. static methods
  4. lifecycle callbacks, such as `connectedCallback` and `disconnectedCallback`
  5. the other methods. Here a method stands above the methods it calls. A getter
     and setter pair stands right above the private methods it calls.
- **Never widen production-API visibility** (package-private → public) just to
  make something testable. Test through the real public interface or restructure.
- Prefer **deep modules**: a small, stable interface over a substantial
  implementation. A class whose interface is nearly as complex as its body is a
  smell, see `/improve-codebase-architecture`.
- **The add-on stays framework-agnostic.** `addon/` and `addon-scheduler/` depend
  on Vaadin Flow only. Spring, configuration lookup and other application concerns belong to the
  consuming app (and to the demo), not to the add-on.

---

## Project-specific standards *(extend here)*

Anything written here becomes a rule a reviewer can cite.

### Architecture & package layout

- Public API lives in `org.vaadin.stefan.fullcalendar` (core and scheduler share the
  package). Sub-packages: `dataprovider` (entry providers), `converters` (option and
  property converters), `json` (annotations for entry serialization), `model`
  (toolbar model), `option` (every type passed as an option value, in core and
  scheduler; the options classes themselves stay in the root package).
- Entries are extensible by subclassing. The `json` annotations together with the
  converters are the supported way to send own properties to the client, so they
  stay public and stable.
- The option API (`calendar.options()` with its typed fields, and `setRawOption`
  for the rest) is the central way to configure a calendar. Don't add a setter on
  the calendar for something an option covers. Type safety comes from the field's
  value type and its converter.
- Stable styling hooks: see `docs/adr/0001-stable-v6-class-names.md`.
- `set…` or `with…`:
  - Options classes and domain classes (`Entry`, `Resource`, `BusinessHours`) have
    `set…` setters that return nothing. Chaining them is not possible: the options
    objects reach their setters through `@Delegate`, so a chained setter would return
    the inner data class, and Lombok cannot generate a setter returning another type.
  - Small configuration objects handed to the calendar (entry sources, `Draggable`,
    resource columns) have `with…` methods that change the object and return it.
  - A type meant for inheritance chains only through a self type (`RemoteEntrySource<S>`,
    the resource column base), so the chain keeps the subtype without covariant
    overrides in each subclass. Otherwise it has `set…` setters returning nothing.
  - `RRule` keeps its prefix-less chaining methods, named after the iCalendar RRULE
    properties (`dtstart`, `count`, `interval`).

### Option Javadoc

An option field whose type is a value type (a `*Value` interface in the `option`
package) carries a definition list in its Javadoc, so every option reads the same.
The list goes into the field's Javadoc, because Lombok copies it into the getter and
the setters point to the getter. Prose on what the option does stays above the
list, a sample goes below it. What the list says is not repeated in the prose.

A hand-written setter of an option stays short: what it sets, `see {@link #getX()}`,
then the placeholder line, then its tags:

```java
/**
 * Sets own business hours, see {@link #getBusinessHours()}.
 * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
 *
 * @param hours the business hours, at least one, or null to remove the option
 */
```

The IDE hides the HTML comment. For the sources jar, `build-tools/SourcesJavadoc.java`
replaces it with the getter's description and drops the reference, so the setter
shows the whole option without Lombok. The build fails when the getter has no
Javadoc or a placeholder stays.

The rows, in this order. A row that does not apply is left out.

| Row | Content |
|---|---|
| `Forms` | Every form the value-type setter accepts, as `{@link}` to the constant, factory method or class, followed by a colon and a short description: `{@link ClassName}: class names.` `JsCallback` where it is one ("a function, see JsCallback"). Nested forms name the parts in the description. A form that takes effect with the scheduler only says "scheduler only". |
| `Shortcuts` | Every other setter of the field, each with the form it produces. A restriction goes with its shortcut. Overlapping shortcuts are all listed. |
| `Default` | Who sets it and what it is: "library: …", and "add-on: …" where the add-on sets a value of its own or another option changes the effect. |
| `JsCallback` | Only with `JsCallback` as a form: the signature, what the arguments hold and, as its last sentence, "It returns …". A row of its own for the return value would read like the `Returns` section of the method in the IDE. |

The row names are bold, `<dt><b>Forms</b></dt>`, so they stand out from the text.

Inside a `<dd>`, every entry (a form, a shortcut, the library and the add-on
default) starts its own paragraph with `<p>`, except the first. Every sentence ends
with a period, and every sentence but the last of an entry ends with `<br>`, so each
sentence starts on its own line. A long list reads at a glance that way.

The sample shows the simplest way for the common case, a shortcut where there is
one, plus each form only the value-type setter reaches, such as
`HeightValue.AUTO` or a `JsCallback`. No sample when every form is obvious.

```java
/**
 * Whether dragged or resized entries may overlap other entries.
 * <dl>
 *   <dt><b>Forms</b></dt>      <dd>{@link Overlap#ALLOWED}: entries may overlap.
 *                                  <p>{@link Overlap#FORBIDDEN}: entries may not overlap.
 *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
 *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setEntryOverlap(boolean)}: {@code true} = {@link Overlap#ALLOWED},
 *                                  {@code false} = {@link Overlap#FORBIDDEN}.</dd>
 *   <dt><b>Default</b></dt>    <dd>library: {@code true}.</dd>
 *   <dt><b>JsCallback</b></dt> <dd>{@code function(stillEvent, movingEvent)}, the entry underneath and the
 *                                  entry being moved.<br>
 *                                  It returns {@code true} to allow the overlap.</dd>
 * </dl>
 * <pre>{@code
 * calendar.options().setEntryOverlap(false);
 * calendar.options().setEntryOverlap(JsCallback.of("(still, moving) => still.allDay && moving.allDay"));
 * }</pre>
 */
```

### Vaadin / UI conventions

Prefer official Vaadin API over custom workarounds or DOM manipulation. Verify
against the Vaadin MCP before rolling your own.

- The client part is a light-DOM web component in TypeScript, loaded with
  `@JsModule`. FullCalendar packages are declared with `@NpmPackage`, and their
  version comes from the client version constant in `FullCalendar`, never a literal.
- Server-defined JS callbacks are evaluated with `new Function()` on purpose.
- The add-on must stay Java-`Serializable`. Mark genuinely transient fields
  `transient` (see `SerializationTest`).

### Testing standards

**A test that passes with the bug put back is not a test.** Before claiming a test
guards something, reinstate the defect and watch it fail. If it stays green either
way, fix it or delete it, and say which.

**Traps when driving Vaadin from Playwright.** Each one lets a test pass with the
bug put back.

- **`page.clock().runFor()`, never `fastForward()`.** `fastForward` fires each due
  timer at most once and never the ones scheduled while it jumps. A client-side
  debounce that schedules the next timer is exactly such a chain, so the jump
  silently breaks it.
- **`locator.click()` returns when the click is dispatched, not when the server has
  answered.** Reading state right after it is a race. Give the test view's control
  a visible effect to wait for (e.g. a button that disables itself) and assert that
  first.
- **"Nothing has been sent yet" cannot be asserted on the UI.** A value reaches the
  page through a round trip in real time, so an empty element only means *not
  yet*. Count the client's own dispatches instead, where they happen synchronously.
- Plain Playwright does not wait for Vaadin's round trips on its own; use
  auto-waiting `assertThat` rather than reading values directly.

### Writing

Applies to everything a person reads: the README, the wiki, Javadoc, comments,
commit messages and issues.

- **No semicolons, colons or dashes joining two sentences**, and no comma splices
  either. Write two sentences, or join them with a word such as "because", "so" or
  "but". A colon is fine before an example, a list or a table. Chained clauses are
  hard to read and mark a text as machine-written.
- **One kind of information per paragraph.** When a text moves on to a new kind
  of information, such as what a value does, how it is shown, a limitation, or
  when it loads, start a new paragraph: a `<p>` in Javadoc, a blank line
  elsewhere. A summary sentence followed by one long block is hard to scan. Not
  one block on what `entryClass` does and when it is applied, but a `<p>` before
  the part on when it is applied. A sentence on what the page or section covers
  ("This page explains …") is a paragraph of its own, not the tail of the
  paragraph that explains the topic.
- **No lists of constants or methods inside a sentence.** Up to three names fit
  in a sentence. More go into a bullet list or a table, or are left out with a
  pointer to the Javadoc when no reader needs the full list. Not "hooks for
  `nowIndicatorHeader*`, `nowIndicatorLine*`, `inlineWeekNumber*`,
  `allDayHeader*` and `dayLane*`", but "hooks for the parts of a view, such as
  the now indicator and the week numbers. The options classes list them."
- **A heading names its topic.** A reader scanning the headings must find where a
  topic is described. Not a catch-all such as "Behavior changes" or "Options"
  beside more specific sections, but "Removing options" or "Entry providers".
- **The add-on is written "add-on" in prose.** Not "the addon sends the option",
  but "the add-on sends the option". Module, artifact and package names such as
  `addon/` keep their spelling. Release notes and migration guides of released
  versions stay as they were published.
- **A version names what it belongs to.** A bare version number means the add-on.
  A version of the wrapped library always says library. Not "FullCalendar 7 renders
  different HTML", but "the FullCalendar library 7.1 renders different HTML". The
  terms are defined in `CONTEXT.md`.
- **The README and the wiki explain the add-on.** They are written for developers
  who know Java and Vaadin and do not explain how Vaadin works. The FullCalendar
  library may be new to the reader, so a library concept gets a sentence of
  context where it first appears. Details of the library are left to its
  documentation, with a link. Where the docs state a rule or a failure case, a
  short example follows.
- **A narrowly scoped feature gets a short example and a link to the page that
  covers it in detail**, wherever it is introduced, release notes included. Not
  only "See Features for an example", but two lines of code that set a custom
  toolbar button, followed by the link.
- **No sentence starts with "Because".** Put the reason after the statement, or
  in its own sentence that starts differently.
- **No counts and no marketing.** Not "more than 100 new constants", but what the
  reader can now do.
- **Docs describe the final state.** No "still open", "planned" or "work in
  progress" remarks. Open work lives in the tickets.
- **Don't point readers at misuse.** Leave out ways an option could be abused.
  Not "options that only exist inside a custom view, or to break the add-on",
  but "options that only exist inside a custom view".
- **A changed text is reread as a whole.** After an edit, read the whole block or
  section it sits in, and the sections that refer to it. Every sentence has to
  connect to the one before it, and a pronoun such as "it" has to point to the
  right thing. Adding a piece without rereading the whole leaves a text that
  reads puzzled together.
- **Javadoc names no FullCalendar library version.** Not "since FullCalendar 7"
  or "the FullCalendar version used by 8.0", but "the used FullCalendar library
  version", "newer FullCalendar library versions" or "an older FullCalendar
  library version". The add-on and library versions are not in sync. An add-on
  version is fine where something starts at it ("since 7.2.5").
- **A claim about behaviour is checked in the code before it is written down.**
  Copying a Javadoc sentence is not a check. Where the code does not do what the
  text says, decide whether the code or the text is wrong, and fix that one. The
  Javadoc of `setEntryProvider` said it refreshes the client. It did not, which
  was a bug.
- **A limitation is questioned before it is documented.** Check why it exists and
  whether the reason still holds. "One entry provider per calendar" rested on a
  field nothing read any more. Removing the field removed the limitation.

### Wiki topic pages

Applies to the wiki pages that explain a topic, such as Entries or Calendar
configuration.

- **A section opens with a sentence of context.** It says what the topic is and
  why a reader cares before the code comes. Not only "Create an `Entry`, set its
  title and add it:" followed by code.
- **The sample comes before the details.** A section goes: one or two sentences
  of context, the sample, then the details. Return values, side effects and
  special cases follow the sample instead of piling up before it. Not "fires an
  `EntryDroppedEvent`. It carries the new times, but the entry still has the old
  values. A listener applies them …" before the drop listener, but "When the user
  drops an entry on another timeslot, the calendar fires an `EntryDroppedEvent`:"
  and the sample, then what `applyChangesOnEntry()` does.
- **Each feature the reader uses gets its own subsection and sample.** On a page
  that presents several events or features, each gets a `###` with a small
  sample. Not a bullet list of events that each end in "[X] shows an example",
  and not "as shown in [Adding a listener]" in place of a sample of its own.
- **No run of paragraphs on unrelated options.** When a section goes on with one
  option per paragraph (one for the time grid, the next for the timeline, then a
  pointer elsewhere), group them under `###` headings by what they belong to,
  such as the view, or put them in a table. Not "In the resource time grid … /
  By default, the area with the resource titles takes 30% … / The area can show
  more columns …" in a row under "Scheduler views", but a `### Resource time grid`
  and a `### Resource area of the timeline`. The whole-page pass checks for such
  runs.
- **Requirements from another topic are a line in the sample.** The option that
  allows dragging is `options().setEditable(true)` in the drop listener sample,
  not a paragraph on options at the start of a section about listeners.
- **Name the Vaadin pattern instead of explaining Vaadin.** Not "Each event is a
  `ComponentEvent` of the calendar. The calendar has a listener method for each",
  but "Like most other Vaadin components, the calendar allows listening to
  events. Adding event listeners follows the same Vaadin scheme."
- **Say what a method gives the reader**, not only what it returns. Not
  "`getChangesAsEntry()` returns a copy of the entry with the new times", but
  that it lets the reader check the change with the `Entry` API instead of
  parsing raw JSON.
- **Say when something happens.** Make clear whether a sentence is about the
  setup or about a calendar that is already shown. Not "An option change that
  moves the shown dates fires no event", which reads like initial setup, but "An
  option change on a calendar that is already shown …". Avoid words with two
  readings: "drops an entry on another timeslot", not "at another time".
- **Write as a person explains it.** Not "`addViewSkeletonRenderedListener` is
  the same method under the name of the event", but "`addViewSkeletonRenderedListener`
  does the same and is named after the event."
- **Bullets list items of one kind**, such as getters, options or steps.
  Sentences that explain and build on each other stay a paragraph.
- **A mechanism is described in one place.** Other pages show a sample at most
  and point there. Two pages that each explain part of it and point to each
  other leave the reader without the whole. Applying a change and auto-revert
  are described on Event listeners. Entries shows the listener samples and
  links there.
- **A section keeps one thread.** Every sentence serves what the section
  explains. Facts picked up along the way, such as how several listeners
  interact or that drops and resizes are counted apart, do not get sprinkled
  in. They go to the Javadoc.
- **A sample is realistic and simple.** It shows something a user can actually
  do at that moment. Not a delete button disabled during a drag, when nobody
  can click during a drag. The simpler variant wins over a more exact but
  nested one, such as comparing with today in UTC rather than in the
  calendar's time zone.
- **Where the add-on differs from the library, name the library's default.**
  Not "The calendar does not move to the clicked day on its own", but "By
  default, the FullCalendar library moves to a day view. The add-on disables
  this default behavior and fires events instead."
- **A rarely needed topic stands next to the topic it belongs to**, after the
  common parts, not in a catch-all section such as "Advanced entry events".
  A topic that does not belong to any section on the page goes to its end.
- **Explain the concept, then name the method.** A reader cannot be expected to
  know what a method stands for. Not "Without one, the entry follows
  `options().setEntryDisplay(…)`", but "follows the display mode the calendar sets
  for all entries (`options().setEntryDisplay(…)`)". Not "`setBusinessHours`
  sets …", but "Business hours mark the time span in which …".
- **The what and the how stand in separate paragraphs.** A blank line separates
  what a thing is from how to use it. Not "An all-day entry covers whole days.
  To create one, call `setAllDay(true)`." in one paragraph, but a new paragraph
  before "To create one …".
- **A section stays on its topic.** It does not explain the API of a neighbouring
  topic. A section on updating entries says what the refresh methods do, not every
  method of the in-memory entry provider. It points to the page or the Javadoc
  that covers the rest.
- **Where there are alternatives, say how they differ.** "After a change, call
  `refreshItem`" reads as if only that method works. Say what each one does and
  when to pick which.
- **A topic page describes the current version only.** What was different in an
  earlier version belongs to the release notes and the migration guide.
- **Rare material goes last or into the Javadoc.** A topic page covers what most
  readers need first. Material for few readers goes to the end of the page.
  Whether it moves to one of the global Advanced pages instead is the
  maintainer's call, case by case. Complete lists of properties are left to the
  Javadoc, with a pointer. A feature that seems hardly used is not dropped on
  the writer's own judgement. The writer lists such features for the maintainer
  with a short summary each, and the maintainer decides.
- **No sample for a single method call.** A sample shows how parts work together.
  A single setter is explained in the text or in a table.
- **A sample shows one case.** When a variant adds steps, the plain case comes
  first and the variant follows as a second sample. Not one drop listener that
  applies the change, limits the date and refreshes the entry, but one that only
  applies the change, then one that also limits the date.
- **How to set something up comes before how to work with it.** A section on
  refreshing the entry provider needs a section before it on how a calendar gets
  its entry provider.
- **Where Vaadin has a counterpart, follow its pattern.** A reader who knows the
  Vaadin API recognizes the add-on's API in it. The in-memory entry provider is
  documented like `ListDataProvider`: `setItems` first, "list" rather than
  "collection".
- **No package names in the text.** The IDE resolves a class name, and the import
  of a sample shows the package where it matters. Not "`EntryProvider.fromCallbacks`
  (package `org.vaadin.stefan.fullcalendar.dataprovider`) creates", but
  "`EntryProvider.fromCallbacks` creates".
- **Every sentence gives the reader something.** No technical reason the reader
  does not need ("because the add-on listens to entry clicks"), no remark about
  the page itself ("The other examples reuse `calendar`"), no statement of the
  obvious ("A setter sets an option"). A sentence connects to the one before it.
  One that seems puzzled in from elsewhere goes.
- **A friendly, neutral tone.** Not "An entry is all-day only after
  `setAllDay(true)`. A `LocalDate` is not enough", but "To create one, call
  `setAllDay(true)`. Setting a `LocalDate` as start alone does not make an entry
  all-day."
- **Related sections stand together.** Order the sections by content. How the
  user changes entries follows how the code updates them.
- **A page is one line of thought.** Read top to bottom, each section follows
  from the one before, and each goes from the simple, common case to the detail
  and the special case. A fact stands where the reader first needs it: a
  requirement before or in the sample that depends on it (`setEditable(true)` in
  the drop listener sample), a term before the table that uses it, an id rule before the
  sample that looks entries up by id. Facts on one topic stay in one place
  instead of spread over the page, and none is said twice. A forward link
  ("see X below") that stands in for an explanation the reader needs here means
  the fact is in the wrong place. Move it, don't link to it.
- **A note on terms stands at the end of the introduction**, as a
  `> **Note:**` quote, not in the first sentence. A term is noted once, on the
  page that introduces it, and not repeated on every page that uses it. That the
  library calls entries "events" is noted on Entries only. What follows from
  the term on another page stays where the reader needs it, such as `info.event`
  in a JavaScript callback or `fc-event` in a class name.
- **What only the scheduler has goes to the Scheduler page.** A topic page has
  no scheduler section. The Scheduler page explains the scheduler variants of
  events, options and class names, and the topic page may link there.

### Release notes and migration guides

Applies to the wiki pages `Release-notes-<version>` and `Migration-guide-<from>-to-<to>`.

- **A version knows no later version.** The pages of a version read as if no later
  version exists, and so does the Javadoc of its version line. A later version may
  refer back to an earlier one, never the other way round. Deprecated API says
  "removed in a future major version" at most. Not "removed in 8.0" or "works the
  same way in 8.0" on a 7.x page.
- **Release notes say what changed.** New features come first. Each gets a short
  intro that says what it is and why a reader cares, then an example, then the
  details. Deprecations and fixes follow. Where a reader has to act, the release
  notes link to the migration guide instead of repeating the steps.
- **Say honestly what is new.** A rename is presented as a rename, with the old
  name beside the new one ("`JsonFeedEntrySource`, formerly
  `JsonFeedEventSource`"), not as a new feature. API that existed before is not
  presented as new. A deprecation gives its reason, also when the reason is that
  the add-on does not want to maintain the old API.
- **The migration guide says what to do.** It holds the complete table of
  deprecated API and its replacement, which no other page repeats. Names are
  written out in full, so a reader can search their code for them. Not
  "`_END_PARAM`", but "`EXTERNAL_EVENT_SOURCE_END_PARAM`". Each area gets a
  before-and-after example, with the old code in a `// was:` comment.
- **Every paragraph of a migration guide leads to an action.** The action is one the
  reader has to take, or one we strongly advise. Check each paragraph with three
  questions:
  1. Does a reader have to act because of it? If not, it is a changelog item and
     leaves the guide. It belongs in the release notes if it matters to readers.
  2. Is the action clear? Say what to do and who is affected ("if you set an option
     to your own object, …"), not only what changed.
  3. Does a before-and-after example show it? Most actions need one. An action that
     is a single obvious step, such as removing a call, may go without.
  4. Is it unclear whether a reader is affected? Then say it both ways: who has
     to act, and that everyone else needs no change ("Your code needs no change for
     this.").
- **Call the version before "the previous version", not by its line.** "7.x" in
  prose confuses readers, even with the glossary. Each page says once which version
  "the previous version" means, for example "it means 7.3". "Previously … now"
  works as a variant. A version line keeps its name where it is the subject ("The
  7.x line stays available"), and so do page names and specific versions.
- **Sentences don't start with the version one after another.** Not "7.3 adds …
  7.3 also … 7.3 deprecates …". Name the version once and vary the subject.
- **Removed and deprecated API stays with its area.** Each area's section lists
  what was removed or deprecated there. A global section only holds what belongs
  to no area, such as the `FullCalendar` class itself.
- **Say whether the reader has to act**, also when nothing is to be done ("Your
  code needs no change").
- **A note that stands apart from the text is a quote with a label**, as in
  `> **Note:** …`.
