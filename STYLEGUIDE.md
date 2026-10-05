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

The three blank-line rules are easy to miss by reading: working through a long file
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
  (toolbar model).
- Entries are extensible by subclassing. The `json` annotations together with the
  converters are the supported way to send own properties to the client, so they
  stay public and stable.
- The option API (`setOption` / `getOption` with the option enums) is the central
  way to configure a calendar. Don't add a typed setter for something an option
  covers. Type safety comes from a converter on the option.
- Stable styling hooks: see `docs/adr/0001-stable-v6-class-names.md`.

### Vaadin / UI conventions

Prefer official Vaadin API over custom workarounds or DOM manipulation. Verify
against the Vaadin MCP before rolling your own.

- The client part is a light-DOM web component in TypeScript, loaded with
  `@JsModule`. FullCalendar packages are declared with `@NpmPackage`, and their
  version comes from the client version constant in `FullCalendar`, never a literal.
- Server-defined JS callbacks are evaluated with `new Function()` on purpose.
- The addon must stay Java-`Serializable`. Mark genuinely transient fields
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

Applies to everything a person reads: the README, Javadoc, comments, commit
messages and issues.

- **No semicolons, colons or dashes joining two sentences**, and no comma splices
  either. Write two sentences, or join them with a word such as "because", "so" or
  "but". A colon is fine before an example, a list or a table. Chained clauses are
  hard to read and mark a text as machine-written.
- **The README and the wiki are written for developers who know Vaadin and
  FullCalendar.** It documents what the add-on adds or changes, not how Vaadin works.
  Where it states a rule or a failure case, a short example follows.
