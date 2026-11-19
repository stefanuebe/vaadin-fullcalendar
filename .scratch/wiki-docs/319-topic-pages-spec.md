# Wiki 8.0: topic pages from Samples and Features (one sub-issue per page)

| | |
|---|---|
| GitHub | #319 |
| Labels | Docs, ready-for-agent |
| Status | open, spec approved by the maintainer (2026-10-15) |
| Blocked by | #314 (closed) |

---

## Parent

#255

## Problem Statement

A developer who wants to know how to do something with the add-on has two pages to search: `Samples` with about 50 sections in no particular order, and `Features`, which mixes a list of what the calendar can do with detailed examples. Many examples exist on both pages, in slightly different versions. Simple and rare use cases sit next to each other, so a newcomer cannot tell where to start, and an experienced user cannot find a topic again quickly.

## Solution

The wiki leads the reader in three steps, each answering its own question:

1. **Getting Started**: how do I get a first calendar running? (#340)
2. **Features**: what can the add-on do, and where do I start? (#342)
3. **Topic pages**: how exactly do I do it? One page per topic, listed on the `Documentation` page and in the sidebar (#344).

This ticket collects step 3. The content of `Samples` and the detailed examples of `Features` move to topic pages, ordered from general to specific. Rare and specialized use cases go to pages below **Advanced**. Each topic page is its own sub-issue, so each page is written and reviewed on its own.

## User Stories

1. As a developer new to the add-on, I want the topics ordered from general to specific, so that I learn the common things first.
2. As a developer, I want one page per topic, so that I do not search a long page for one example.
3. As a developer, I want to find a topic in the sidebar, so that I get to it in one click.
4. As a developer, I want each example to exist once, so that I do not have to decide which of two versions is current.
5. As a developer, I want to configure the calendar through options in one place, so that I learn the option API before anything else.
6. As a developer, I want to switch views and navigate in code, so that I can build my own navigation.
7. As a developer, I want to show the current interval with my own Vaadin components, so that my toolbar fits my application.
8. As a developer, I want to add, update and remove entries and see all properties of an entry, so that I can model my data.
9. As a developer, I want to choose the right entry provider, so that the calendar loads my entries efficiently.
10. As a developer, I want to react to clicks, selections, drags and resizes, so that my application responds to the user.
11. As a developer, I want to set the time zone and the locale, so that the calendar shows dates the way my users expect.
12. As a developer, I want to create recurring entries, simple or with an RRule, so that I do not create each occurrence myself.
13. As a developer, I want to style the calendar, so that it fits my application's look.
14. As a developer using the scheduler, I want to add resources and link entries to them, so that I can plan rooms or people.
15. As an experienced developer, I want the advanced topics on their own pages, so that they do not get in the way of the basics and I still find them.
16. As a developer, I want to drag Vaadin components onto the calendar and control overlap and constraints, so that drag and drop follows my rules.
17. As a developer using the scheduler, I want resource columns with Vaadin components, so that users can edit resource data in the timeline.
18. As a developer, I want to run JavaScript callbacks and render hooks, so that I can change what the library renders.
19. As a developer, I want to load entries from JSON feeds, Google Calendar or ICS, so that I can show data the server does not hold.
20. As a developer, I want to configure the native toolbar, including its screen-reader labels, so that I can use it instead of my own.
21. As a developer, I want to attach native JavaScript events to entries, so that I can react to events the add-on does not forward.
22. As a developer, I want to subclass `FullCalendar`, so that I can extend the client side, for example with tooltips.
23. As a developer on 7.x, I want to see where 7.x differs, so that the wiki serves my version too.
24. As a user of an old link to `Samples`, I want to land on a page that leads me on, so that the link does not end on a missing page.

## Implementation Decisions

**Topic pages, in this order:**

1. Calendar configuration (options)
2. Views and navigation
3. Entries
4. Entry providers
5. Event listeners
6. Time zones and locale
7. Recurrence
8. Theming: extends the existing `Themes` page, no new page. `Stable-class-names` stays its own page.
9. Scheduler
10. Advanced: an overview page, with one page per topic:
    - A. Drag and drop
    - B. Scheduler: resource columns
    - C. Callbacks and render hooks
    - D. Remote entry sources
    - E. Native toolbar
    - F. Native JS events for entries
    - G. Subclassing FullCalendar
    - H. Tooltips with tippy.js (builds on G)
    - I. Converters (`@JsonConverter` for own entry properties). New content. The sub-issue first checks whether converters are meant for users; if not, the page is dropped.

**Where today's sections go** (`S` = section of `Samples`, `F` = section of `Features`):

| Topic page | Content |
|---|---|
| Calendar configuration | S: Passing custom initial options, Setting the calendar's dimensions, View-specific options, Adding business hours, moreLinkClick (static value). F: View-specific options |
| Views and navigation | S: Show the current interval with Vaadin components, Restricting navigation with valid range. F: Navigation |
| Entries | S: Add, update or remove a calendar entry, Creating a background entry, Entry URL and keyboard accessibility, Entry data utilities. F: Entry model, Entry URL behaviour, Accessibility (`ENTRY_INTERACTIVE`) |
| Entry providers | S: Entry providers (in memory, callbacks, custom implementation, prefetch mode) |
| Event listeners | S: Calendar event handling, Drag and resize lifecycle events. F: Event handling |
| Time zones and locale | S: Using timezones. Locale: new content |
| Recurrence | S: RRule. F: RRule, the recurrence rows of the entry model |
| Theming (`Themes`) | S: Style the calendar |
| Scheduler | S: Using the Scheduler (activating, resources, hierarchical resources, drag between resources, timeline and vertical views, resource background entries), Resource-level display overrides. F: Scheduler features |
| A. Drag and drop | S: External drag and drop, Entry overlap control, Global event overlap control, Entry constraints with BusinessHours |
| B. Resource columns | S: Using component resource columns, Resource columns (Scheduler) with column render hooks. F: Component Resource Columns |
| C. Callbacks and render hooks | S: JsCallback usage, Render hook callbacks, Customize entry rendering, moreLinkClick (function callback). F: render hooks part of Options, toolbar buttons and render hooks |
| D. Remote entry sources | S: Remote entry sources. F: Remote entry sources |
| E. Native toolbar | S: Accessibility hints for toolbar buttons. F: Toolbar buttons, the hint options of Accessibility |
| F. Native JS events | S: Use native javascript events for entries |
| G. Subclassing | S: Creating a subclass of FullCalendar for custom mods |
| H. Tooltips | S: Using tippy.js for description tooltips |

**Content that goes elsewhere:**

- S: Creating a basic calendar instance → `Getting-Started` (#340).
- F: the list of what the calendar can do → dropped (#342).
- F: Developer Tools (MCP note) → decided in #321.

**Rules for every topic page:**

- The topic page starts with its most basic example. `Features` (#342) is written after the topic pages and shows only a short entry into the topic, so the topic page needs no change for it.
- Each example exists once in the wiki. Duplicates are merged into the best version.
- The page follows the *Writing* section of `STYLEGUIDE.md` and the terms of `CONTEXT.md`. Claims and code samples match 8.0 (`master`).
- The wiki serves all major versions. Notes on how 7.x differs stay with the content they belong to.
- The links of the other wiki pages point to the new pages. No wiki page links to an anchor that no longer exists.

**Order of work:** one sub-issue per topic page, worked in the order of the list. When all are done, `Samples` becomes a short page with links to the `Documentation` page and the topic pages. GitHub wikis have no redirects, so links to its anchors cannot be kept.

## Testing Decisions

For documentation, "tested" means:

- Every code sample compiles against 8.0. Samples that need the browser (callbacks, render hooks, native events) are checked against the library docs in `fc-docs/_docs-v7`.
- No link and no anchor in the wiki points to a missing target.
- Each page gets the review of the project (three agents in parallel, claims against source, framing and language) before it is committed, and the maintainer reviews it on the review pages.

## Out of Scope

- `Getting-Started` (#340), `FAQ` (#341), `Features` (#342), the `Documentation` page and the sidebar (#344).
- The legacy and unrelated pages (#321).
- The writing rules on the remaining pages (#320). The `Themes` page gets only the moved section here.

## Further Notes

The wiki is its own git repository. Work for 8.0 happens on its branch `8.0`, which is merged into the live wiki when 8.0 is released (#343). Wiki-internal links stay full URLs.

## Acceptance criteria

- [ ] One sub-issue per topic page exists, in the order above.
- [ ] Every section of `Samples` and every detailed example of `Features` ends up on exactly one topic page, or is dropped as a duplicate.
- [ ] All sub-issues are closed, and `Samples` is a short page with links to the topic pages.

## Blocked by

- #314
