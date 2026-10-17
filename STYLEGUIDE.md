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
  the part on when it is applied.
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
- **The README and the wiki are written for developers who know Vaadin and
  FullCalendar.** They document what the add-on adds or changes, not how Vaadin
  works. Where they state a rule or a failure case, a short example follows.
- **A narrowly scoped feature gets a short example and a link to the page that
  covers it in detail**, wherever it is introduced, release notes included. Not
  only "See Features for an example", but two lines of code that set a custom
  toolbar button, followed by the link.

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
- **A note that stands apart from the text is a quote with a label**, as in
  `> **Note:** …`.
