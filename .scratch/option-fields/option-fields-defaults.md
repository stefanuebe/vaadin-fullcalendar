# Drop the add-on's option defaults

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement |
| Status | decided (maintainer, 2026-10-15), merged into `option-fields-spec.md` (newer: headerToolbar false moves to the server, client fallback goes away) |
| Blocked by | `option-fields-design.md` |

---

## Why

With options as fields, `null` means the library default. The add-on still overrides some library defaults on the client (`createInitOptions` in `full-calendar.ts`) and on the server (`FullCalendar.postConstruct`). Whoever wants another value sets the field (maintainer, 2026-10-15).

## What goes

| Option | Add-on today | Library default | Visible effect |
|---|---|---|---|
| `timeZone` | `UTC` | `local` | times shown in the browser zone, see `option-fields-timezone.md` |
| `editable` | `true` | `false` | entries no longer draggable or resizable |
| `weekNumbers` | `true` | `false` | week numbers disappear |
| `tableHeaderSticky` | `true` | `'auto'` | with the add-on's full height effectively off |
| `footerScrollbarSticky` | `true` | `'auto'` | same |

Each is a breaking change for 8.0 and goes into the migration guide 8.0, with the line that restores the old value.

## What stays

- `headerToolbar: false`: the add-on is built for navigation with Vaadin components; FullCalendar's own toolbar navigates on the client without the server.
- The `locale` fallback `CalendarLocale.getDefaultLocale()`: it takes the application's locale, not a matter of taste.
- `setHeightFull()`: Vaadin sizing, not an option default.
- `resources: []` in the scheduler client: technically needed.

## Acceptance

- The client sets none of the dropped options, the server sets none on construction.
- The guard for `OptionFieldsPrototype*` in `postConstruct` and `addonDefaultOptions` for `editable` are gone.
- Migration guide 8.0 lists each dropped default with its restore line.
