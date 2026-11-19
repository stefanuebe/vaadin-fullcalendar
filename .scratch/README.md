# Local tickets (archive, state 2026-10-18)

Tickets live on GitHub. Nothing new is written here.

These files were the tracker while the old bot account was flagged (2026-10-15). On 2026-10-18 every ticket that was not done moved to GitHub as its own issue. The issue carries the status from then on; the file stays as background only.

## Moved to GitHub

| File | Issue |
|---|---|
| `option-fields/option-fields-7.3.md` | #405 |
| `option-fields/issues/52-wiki-calendar-configuration.md` | #406 |
| `option-fields/issues/53-wiki-examples.md` | #407 |
| `option-fields/issues/54-wiki-release-notes.md` | #408 |
| `option-fields/issues/55-wiki-migration-option-mapping.md` | #409 |
| `option-fields/issues/56-wiki-migration-methods-defaults.md` | #410 |
| `wiki-docs/320-writing-rules.md` | #411 |
| `wiki-docs/321-legacy-pages.md` | #412 |
| `wiki-docs/readme-8.0.md` | #413 |
| `wiki-docs/319-topic-pages-spec.md` | #414 |
| `wiki-docs/topics/01-calendar-configuration.md` | #415 |
| `wiki-docs/topics/02-views-and-navigation.md` | #416 |
| `wiki-docs/topics/03-entries.md` | #417 |
| `wiki-docs/topics/04-entry-providers.md` | #418 |
| `wiki-docs/topics/05-event-listeners.md` | #419 |
| `wiki-docs/topics/06-time-zones-and-locale.md` | #420 |
| `wiki-docs/topics/07-recurrence.md` | #421 |
| `wiki-docs/topics/08-theming.md` | #422 |
| `wiki-docs/topics/09-scheduler.md` | #423 |
| `wiki-docs/topics/10-drag-and-drop.md` | #424 |
| `wiki-docs/topics/11-scheduler-resource-columns.md` | #425 |
| `wiki-docs/topics/12-callbacks-and-render-hooks.md` | #426 |
| `wiki-docs/topics/13-remote-entry-sources.md` | #427 |
| `wiki-docs/topics/14-native-toolbar.md` | #428 |
| `wiki-docs/topics/15-native-js-events-for-entries.md` | #429 |
| `wiki-docs/topics/16-subclassing-fullcalendar.md` | #430 |
| `wiki-docs/topics/17-tooltips-with-tippy-js.md` | #431 |
| `wiki-docs/topics/18-converters.md` | #432 |
| `wiki-docs/340-getting-started.md` | #433 |
| `wiki-docs/341-faq.md` | #434 |
| `wiki-docs/342-features-overview.md` | #435 |
| `wiki-docs/344-documentation-sidebar.md` | #436 |
| `release/343-release.md` | #437 |

## Done here

- `option-fields/issues/01…51`, `57…78`: done, merged into `master` (the issue numbers, where one exists, are in each file).
- `option-fields/option-fields-spec.md`, `option-fields-defaults.md`, `option-fields-timezone.md`, `option-fields-timezone-client.md`: decided and implemented with the option fields.

## Obsolete

- `option-fields/option-fields-design.md`, `option-fields/option-type-check.md`: replaced by the option fields.
- `option-fields/initial-view-javadoc.md`: `Option.INITIAL_VIEW` no longer exists on `master`.

## Doc review server

`.doc-review/server.py 8090` serves the previews (http://172.17.0.2:8090/). `.doc-review/build.sh` rebuilds them; its `PAGES` list holds the pages under review. Comments are saved per page into `.doc-review/comments.json`; handled ones go to `.doc-review/comments-done-*.json`.
