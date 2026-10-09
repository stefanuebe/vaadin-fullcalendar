# FullCalendar for Flow

A Vaadin Flow integration of the FullCalendar JavaScript library. Java components
(`FullCalendar`, `FullCalendarScheduler`) wrap FullCalendar so Vaadin developers
configure and drive it from the server.

Current state, what is next and what the add-on guarantees live in the issue
tracker, see `docs/agents/issue-tracker.md`. Decisions with their reasons are in
`docs/adr/`. Feature specs are in `specs/`.

## How I work here

- Code and document for humans. They have to understand and maintain this
  add-on after you are gone from the conversation.
- Don't guess, confirm with docs / sources / research results / MCP. Being
  uncertain is fine and saying so is fine. Stating an assumption as fact is not.
- If there is no solution or answer, say it. Acknowledging failure is better than
  trying to hide it.
- Answer the question you were asked before you edit anything.
- Never silently revert or tidy away something in the workspace you cannot
  explain. Ask, or leave it.
- Never self-dispatch after a question. If you ask something, wait for the
  answer before acting.
- Use plain and clear language, don't try to sound creative.
- Review before the gate and before the commit, see *Review before the gate,
  review before the commit*. That order is not yours to reorder.
- Pick the cheapest model that fits a subagent. Always pass `model` explicitly,
  because the inherited default is Opus or better. Restate the critical rules in
  each subagent's prompt. When unsure, start cheaper and escalate only if the
  output is shallow.

  | Subagent role | Model |
  |---|---|
  | Mechanical implementer (plan specifies the exact code) | Haiku |
  | Explore / search ("where is X defined") | Haiku |
  | Multi-file integration / pattern matching | Sonnet |
  | Per-phase code-quality or spec-compliance review | Sonnet |
  | Final whole-branch / holistic / deep design review | Opus or better |

## Working conventions

- **Commits:** one commit per logical phase or feature. Run the tests before
  committing, and don't commit on the user's behalf unless asked.
- **Docs:** a change that users of the add-on notice (new or changed API, changed
  behaviour, a new limitation) updates the wiki in the same piece of work, see
  *Documentation*.
- **Never push.** Pushing, opening pull requests and anything else that leaves this
  machine is the maintainer's step, always. This includes the wiki repo.
- **Branches and pull requests:** ticket work happens on a branch, one per ticket,
  or one integration branch for tickets that only turn green together. It is not
  merged into `master` locally. It reaches `master` through a GitHub pull request,
  where the maintainer reviews the code. Pushing the branch and opening the pull
  request are the maintainer's step (see *Never push*). Releases are not ticket
  work and follow `docs/agents/release.md`.
- **Tests & long-running ops:** run new/changed tests first, and only run the full
  gate once those pass. Don't wrap waits in `until … done` sleep loops. Poll
  periodically and check whether a background job has died. A change that only
  touches `demo/` needs `mvn -pl demo verify`, not the full gate, because the demo
  has no tests.

## Stack

- **Vaadin** 25.x (Core), **Java** 21, **Spring Boot** 4.x (demo and e2e test app only)
- FullCalendar JS client version: `FullCalendar.FC_CLIENT_VERSION` (currently 7.1.0).
  When raising it, search the add-on for `FC-UPDATE:` comments and check each place. They name
  what a new FullCalendar version may change.
- Lombok, Jackson 3 (since 7.0, replacing elemental.json), Vite, Maven multi-module
- Base package: `org.vaadin.stefan.fullcalendar` (core and scheduler share it)

Spring Boot is the version `com.vaadin:flow-project:<flow version>` names in its
`spring.boot.version` property, not the newest Spring Boot. Derive it from the
Vaadin release when bumping.

Before raising the Vaadin version, build `e2e-test-app` with an empty `user.home`
and check that it still passes. A Vaadin release can start asking for a license key
in a production build even with core components only.

**Version lines:** `master` carries the current major, `v7_master` the 7.x line
(Vaadin 25), `v6_master` the 6.x line (Vaadin 24). Fixes go to `master` through a
pull request and are ported back where they apply. A backport that applies about
1:1 is committed on the version line directly. One that deviates substantially goes
through its own pull request. Whether a feature goes back is decided per feature.
After switching branches, delete the generated frontend files in `demo/` and
`e2e-test-app/` (`node_modules`, `package.json`, `package-lock.json`,
`src/main/frontend/generated`), or the Vite build fails on the other line's
half-installed packages.

## Module structure

- **`addon/`** (`org.vaadin.stefan:fullcalendar2`) is the core component, published
  to the Vaadin Directory. Holds the unit and browserless tests. Spring-free, tests
  included. TypeScript client: `full-calendar.ts` in its frontend resources.
- **`addon-scheduler/`** (`fullcalendar2-scheduler`) adds resource views. The
  FullCalendar Scheduler library needs its own license, and the addon's MIT license
  covers only the addon code.
- **`demo/`** is the Spring Boot demo app. Holds **no tests at all**. Free to change.
- **`e2e-test-app/`** is the Vaadin app the browser tests run against. It owns its
  **Test views** and depends on the addons only, never on `demo/`.
- **`e2e-tests/`** is the Playwright suite (`tests/*.spec.js`, npm, not a Maven
  module). `mvn verify -Pit` in `e2e-test-app/` starts the app and runs it.
- **`fc-docs/`** holds local copies of the FullCalendar JS docs, `_docs-v6` and
  `_docs-v7` (Markdown, `index.md` lists every page, `CHANGELOG.md` beside it).
  Prefer them over web fetches. The folder is git-ignored. The v7 copy is built from
  `https://fullcalendar.io/docs/llms.txt`, which links every page as Markdown, plus
  the `CHANGELOG.md` of `fullcalendar/fullcalendar`.

## Testing

```bash
mvn clean install                               # build all modules, unit tests
mvn test -pl addon -Dtest=EntryTest#someMethod  # one class or method
cd e2e-test-app && mvn clean verify -Pit        # Playwright E2E against the test app
mvn test -pl addon -Ppit                        # mutation testing (PIT)
```

- **JUnit 5 + Mockito** for unit tests, in `addon/` and `addon-scheduler/`.
- **Browserless** tests (Vaadin `browserless-test`, free on 25.1+) for server-side
  behaviour that needs a Vaadin context but no browser. Spring-free, in the addon
  modules. They execute no JavaScript and assert server-side state and element
  properties only.
- **Playwright** in `e2e-tests/` for every behaviour that needs the client-side
  JavaScript to run.
- Manual mutation scripts `mutation-test-a.sh` / `mutation-test-b.sh`, see
  `specs/verification.md` §3.

**No test lives in `demo/`, ever.** Browser tests drive Test views they own,
browserless tests build the component directly. A test that reads the demo breaks
on a label change.

**Test our wiring, not FullCalendar.** The add-on's job is the connection between
Flow and FullCalendar: does the option we pass reach the client, does the entry we
change arrive, does the client event come back. Whether FullCalendar itself behaves
correctly is its scope.

- **Bug reports:** first check whether an existing test covers the case. If one
  exists but missed the bug, fix the test. Otherwise create one.
- **Every bug fix includes a test** that reproduces the bug. Every new feature
  includes the verification defined in its spec.
- Never commit test code without running it.

Driving Vaadin from Playwright has traps that let a test pass with the bug put
back. They are under *Testing standards* in `STYLEGUIDE.md`. Read them before
writing a browser test. When an addon's Java or frontend changes, rebuild it
(`mvn install -DskipTests -pl addon,addon-scheduler`) before running the demo or
the E2E tests so they don't run against a stale jar.

## Review before the gate, review before the commit

The order is fixed:

1. the tests covering what changed are green (the targeted run, not the gate)
2. the review: **always three agents**, in parallel. `/code-review` (from
   `mattpocock-skills`) brings two (Standards, Spec). The third takes an axis from
   the change itself, picked so it can disagree with the other two: Vaadin API
   usage, the FullCalendar integration, or test quality (do the tests prove the
   behaviour, or pass around it?). For a document: claims against source, framing
   and language. Without a spec `/code-review` skips Spec, so add a second axis of
   that kind. The review loops. Fix every major and run the next round on the whole
   scope under review. The agents judge what is major. Pass each round the
   findings already rejected, with the reason. Once a round finds no major, report
   the minors and ask whether to fix them and review again. Minors in test code
   and obvious minor errors (a stale name, a wrong doc row) are fixed without
   asking. If round 5 still finds a major, stop and ask how to
   go on.
3. the full gate: `mvn clean install`, then the E2E run in `e2e-test-app/`
4. commit

Reviewing after the gate pays for the long build twice. Committing before the
review means the commit is not the reviewed state. A commit without code (docs, a
status capture) goes in directly. This order overrides any skill that orders it
differently.

`/code-review` diffs `<fixed-point>...HEAD`, which before the commit still shows the
old state. Have it diff the working tree (`git diff <fixed-point>`) and name the
fixed point yourself: `HEAD` for one change, the branch point for several commits.

## Dev server

Vaadin dev loop (25.3+): `demo/.vaadin/vaadin-dev status | start | apply | restart |
stop`, serving on port 8080. The `vaadin-devloop` skill describes the cycle. Never
start the app with `spring-boot:run` beside it. Run `demo/.vaadin/vaadin-dev
shutdown` before the gate, because `mvn clean` deletes `demo/target` under the
running app. The dev loop does not run Lombok, so edits to Lombok-annotated classes
need a Maven build (`mvn clean install -DskipTests -pl addon,addon-scheduler`).

The dev loop runs only in the devcontainer. The maintainer opens the app from the
host at the container's IP, which the status line shows. The dev server is for
looking at the app, never a substitute for the gate. Stop it when you are done.

## Release

Only when the maintainer asks, and the push is always the maintainer's. Follow
`docs/agents/release.md` step by step, because the order of tag, `v-herd-demo` merge
and snapshot bump matters for the demo deployment.

## Documentation

User-facing documentation lives in the **GitHub wiki**, the single source of truth.
There is no user doc folder in this repo. The wiki is its own git repo, checked out
at `wiki/` (remote `origin-wiki`). Key pages: Home, Getting Started, Samples,
Features, Release notes, Migration guides, FAQ, Known Issues, Scheduler license.

- Wiki-internal links use full GitHub URLs
  (`https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Page-Name`), because the
  Vaadin Directory does not resolve relative wiki links.
- One release-notes page per minor (`Release-notes-<major>.<minor>`) and one
  migration guide per version jump (`Migration-guide-<from>-to-<to>`). Index pages
  stay one line per entry.
- Docs always describe the final state. Wiki work for an unreleased version happens
  on a local branch of the wiki repo (e.g. `8.0`) and is merged at release, so the
  live wiki keeps describing the released version. No "work in progress" pages or
  markers.
- One wiki for all major versions. Call out version-specific API differences inline
  where relevant, don't maintain parallel page sets.
- The former `mcp-server/` module was removed for its dependency churn. Don't
  reintroduce it. If tooling is missed, prefer something that reads the wiki directly.

## Architecture notes

- Component state needs no locking or `volatile`, because the `VaadinSession` lock
  serializes all component access. The addon must stay `Serializable` (see
  `SerializationTest` / `SchedulerSerializationTest`).
- `lastFetchedEntries` holds one viewport worth and is repopulated on every client
  fetch. It is not a long-lived cache.

## Who owns which file

| File | Owner | Rule |
|---|---|---|
| `docs/adr/` | Claude | Decisions with their reasons. Sparingly: hard to reverse, surprising, a real trade-off. |
| `specs/` | shared | Working basis for features. Keep it in line with the code. |
| `CLAUDE.md`, `STYLEGUIDE.md`, `CONTEXT.md` | shared | Standing rules. Add here only what outlives the piece of work that raised it. |

## Conventions

- The MCP `vaadin` server (`https://mcp.vaadin.com/docs`) is the source of truth
  for Vaadin API. Prefer it over memory. As a last resort for web component DOM, the
  per-element pages under `https://cdn.vaadin.com/vaadin-web-components/<version>/`.
- Don't pin dependency versions to a guessed "latest". Resolve the current release
  first.
- Code style: read and follow `STYLEGUIDE.md`.
- Domain language: use the terms in `CONTEXT.md`. A calendar item is an **Entry**
  (`ENTRY_` / `Entry` prefixes), never an event, to avoid clashing with Vaadin's
  event system.

## Agent skills

- Issue tracker: GitHub Issues via `gh`, see `docs/agents/issue-tracker.md`.
- Triage labels: default vocabulary, except `needs-info` → `waiting for author`, see
  `docs/agents/triage-labels.md`.
- Domain docs: single-context (`CONTEXT.md`, `docs/adr/`, plus `specs/`), see
  `docs/agents/domain.md`.
