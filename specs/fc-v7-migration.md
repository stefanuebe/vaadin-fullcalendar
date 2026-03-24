# Migration Plan: FullCalendar JS v6 → v7

> **Status:** Draft — awaiting FC v7 stable release
> **Addon version:** 7.x (FC v6) → 8.0.0 (FC v7)
> **Date:** 2026-03-21

---

## Executive Summary

The migration from FullCalendar JS v6 to v7 touches four layers:

1. **NPM packages and TypeScript imports** — Package names stay as `@fullcalendar/*` (confirmed via npm RC), only version bump needed. `fullcalendar` / `fullcalendar-scheduler` are metapackages. TS imports unchanged.
2. **Option name mapping** — ~30 FC option wire names change across `Option` and `SchedulerOption` enums
3. **License key** — GPLv3 → AGPLv3 for the scheduler
4. **CSS/Theme** — fc-* classes and --fc-* variables appear unchanged in v7 source (confirmed via GitHub), but the changelog claims "CSS variables refactored/renamed" — **must be verified against stable release before implementing**

The JS Calendar API (`setOption`, `changeView`, `prev`, `next`, etc.) is **unchanged**.

All phases ship together as **8.0.0**.

---

## Rollback Strategy

- Maintain a `7.x-maintenance` branch from the current `fc7` HEAD before starting Phase 1
- All Phase 1 work happens on a feature branch (`fc-v7-upgrade`) — only merge after full E2E green
- If FC v7 stable API differs from beta docs, abort and stay on v6 until docs are updated
- The `(verify)` markers in this plan are **blocking prerequisites** — each must be confirmed before that section is implemented

---

## npm Package Research Results (confirmed 2026-03-21 against v7.0.0-rc.0)

**Key finding:** All `@fullcalendar/*` packages **still exist as separate npm packages** in v7 RC. The `fullcalendar` and `fullcalendar-scheduler` packages are **metapackages** that bundle them as dependencies. This means:
- **No `@NpmPackage` name changes needed** — only version bumps
- **No TypeScript import path changes needed** — `'@fullcalendar/core'`, `'@fullcalendar/daygrid'`, etc. still work
- `@fullcalendar/core/locales-all` export path confirmed unchanged
- `@fullcalendar/moment-timezone` still exists at v7 RC (changelog says removed — contradicts npm)
- No `temporal-polyfill` peer dependency in v7 RC (changelog mentions it — may be added in stable)
- `@fullcalendar/scrollgrid` still exists at v7 RC (changelog says merged into core — contradicts npm)

**This dramatically simplifies Phase 1** — it becomes a pure version bump.

---

## Prerequisites (before starting Phase 1)

### Package Verification
- [x] ~~Package names~~ — `@fullcalendar/*` confirmed unchanged in v7 RC
- [x] ~~Scheduler packages~~ — `@fullcalendar/resource-*` confirmed at v7 RC
- [x] ~~locales-all path~~ — `@fullcalendar/core/locales-all` confirmed
- [ ] FC v7 stable release published to npm
- [ ] Re-verify all above against stable release (RC findings may change)
- [ ] Confirm `temporal-polyfill` status in stable (not a peer dep in RC)
- [ ] Confirm `@fullcalendar/moment-timezone` is still supported in stable (exists in RC, changelog says removed)
- [ ] Confirm `@fullcalendar/scrollgrid` status in stable (exists in RC, changelog says merged)

### Option Wire Name Verification
- [ ] Confirm `buttons` option structure (replacement for `buttonText`)
- [ ] Confirm exact v7 wire names for ALL renames in Phase 2 tables
- [ ] Confirm `eventContent`, `eventDidMount`, `eventWillUnmount` are unchanged in v7 (or renamed)
- [ ] Confirm `slotLabelDidMount`/`slotLabelWillUnmount` → `slotHeaderDidMount`/`slotHeaderWillUnmount` (by pattern, unconfirmed in changelog)

### CSS/Theme Verification (CRITICAL)
- [ ] Verify `fc-*` class names are still present and usable in v7 Classic theme (not obfuscated)
- [ ] Diff `--fc-*` CSS variables between v6 and v7 source — confirm no renames
- [ ] Confirm whether v7 requires explicit theme plugin import (CSS no longer bundled with JS)
- [ ] Identify the correct `@NpmPackage` / import for the Classic theme plugin (if needed)

### Hardcoded TS Option Names
- [ ] Confirm these hardcoded FC option names in `full-calendar.ts` are unchanged in v7: `eventOverlap`, `eventAllow`, `selectOverlap`, `events`, `timeZone`
- [ ] Confirm `batchRendering` API still exists in v7

---

## Phase 1 — Core JS Upgrade: Version Bump + Optional Moment Removal

**Risk:** Medium (downgraded from High — no package renames needed)

### 1a. @NpmPackage Version Bump — `FullCalendar.java`

Update `FC_CLIENT_VERSION` from `"6.1.20"` to v7 stable version. **No package name changes** — all `@fullcalendar/*` packages confirmed to exist in v7.

Optional: attempt to remove moment and replace `formatDate()` with native JS (see 1c). If this fails, keep moment packages at their current versions.

| Package | Action |
|---|---|
| All 11 `@fullcalendar/*` packages | **Version bump only** |
| `moment` (2.30.1) | Keep (or remove if 1c succeeds) |
| `moment-timezone` (0.6.0) | Keep (or remove if 1c succeeds) |
| `ical.js` (2.0.1) | Keep |
| `temporal-polyfill` | Add **only if** v7 stable requires it as peer dep (not required in RC) |

### 1b. @NpmPackage Version Bump — `FullCalendarScheduler.java`

Update `FC_SCHEDULER_CLIENT_VERSION` from `"6.1.9"` to v7 stable version. No package name changes.

### 1c. Optional: `formatDate()` Native Replacement — `full-calendar.ts`

If we want to drop the moment dependency, replace the moment-based `formatDate()`:

```typescript
// Current (moment-based):
protected formatDate(date: string | Date, asDay = false) {
    let moment = toMoment(date, this.calendar!);
    if (asDay) {
        return moment.startOf('day').format().substring(0, 10);
    }
    return moment.utc().format();
}

// Replacement (native JS, no moment dependency):
protected formatDate(date: string | Date, asDay = false): string {
    const d = (date instanceof Date) ? date : new Date(date);
    if (asDay) {
        return d.toISOString().substring(0, 10);
    }
    // Strip milliseconds to match moment's output format
    return d.toISOString().replace(/\.\d{3}Z$/, 'Z');
}
```

**Format difference:** `moment.utc().format()` → `2024-03-15T14:30:00+00:00` (no millis). `Date.toISOString()` → `2024-03-15T14:30:00.000Z` (with millis). The `.replace()` strips millis. Java's `Instant.parse()` accepts both.

If the native replacement works and all E2E tests pass, remove: `@fullcalendar/moment`, `@fullcalendar/moment-timezone`, `moment`, `moment-timezone` from @NpmPackage and the corresponding TS imports + plugin registrations.

**Assumption:** Calendar is always initialized with `timeZone: 'UTC'`.

### 1d. Hardcoded Option Names in TypeScript — Audit

These FC option names are hardcoded in `full-calendar.ts` (not routed through the Java Option enum). Verify each is unchanged in v7:

| Option Name | Location | v7 Status |
|---|---|---|
| `eventOverlap` | lines 137, 600 | verify unchanged |
| `eventAllow` | lines 144, 610 | verify unchanged |
| `selectOverlap` | lines 150, 619 | verify unchanged |
| `events` | line 580 | verify unchanged |
| `timeZone` | line 692 | verify unchanged |
| `batchRendering` | line 646 | verify still exists |

### 1e. headerToolbar Default

FC v7 disables `headerToolbar` by default. The addon already sets `headerToolbar: false` explicitly in `createInitOptions()`. **No change needed.**

### 1f. Vaadin Build Cleanup (IMPORTANT)

After changing @NpmPackage versions, Vaadin invalidates its frontend bundle. **Before verification:**
```bash
# In demo/ and e2e-test-app/:
rm -rf node_modules package-lock.json target/
# Also delete pre-compiled bundle if it exists:
rm -rf src/main/bundles/
```

### Files Modified

| File | Changes |
|------|---------|
| `addon/.../FullCalendar.java` | FC_CLIENT_VERSION bump (+ optional: remove 4 moment @NpmPackage) |
| `addon/.../full-calendar.ts` | Optional: formatDate() replacement, remove moment imports/plugins |
| `addon-scheduler/.../FullCalendarScheduler.java` | FC_SCHEDULER_CLIENT_VERSION bump |

### Verification

1. Clean stale frontend: `rm -rf node_modules package-lock.json target/` in demo/ and e2e-test-app/
2. `mvn clean install` — compiles without errors
3. `cd demo && mvn spring-boot:run -Pproduction` — all views render (DayGrid, TimeGrid, List, MultiMonth)
4. `cd e2e-test-app && mvn clean verify -Pit` — full E2E suite passes
5. Focus on: `calendar-views.spec.js`, `event-sources.spec.js`, `scheduler-features.spec.js`, `entry-model.spec.js` (RRule), `interaction-callbacks.spec.js`
6. Manual timezone test: configure calendar with `new Timezone(ZoneId.of("Europe/Helsinki"))`, add entry at known UTC time, verify displayed time shows UTC+2 offset

### New E2E Test: Timezone Rendering

Write a test that:
- Configures the calendar with a non-UTC timezone
- Adds an entry at a specific UTC time
- Verifies the displayed entry time reflects the timezone offset
- This catches any `formatDate()` behavioral regression

---

## Phase 2 — Option Name Renames in Java Enums

**Risk:** Medium

### 2a. Option Enum Wire Name Changes — `FullCalendar.java`

The Java constant names stay the same (addon public API). Only the FC wire name string changes.

| Java Constant | v6 Wire Name | v7 Wire Name |
|---|---|---|
| `ENTRY_BACKGROUND_COLOR` | `"eventBackgroundColor"` | `"eventColor"` (verify — merged with border) |
| `ENTRY_BORDER_COLOR` | `"eventBorderColor"` | `"eventColor"` (verify — merged with background) |
| `ENTRY_TEXT_COLOR` | `"eventTextColor"` | `"eventContrastColor"` |
| `ENTRY_CLASS_NAMES` | `"eventClassNames"` | `"eventClass"` |
| `SLOT_LABEL_FORMAT` | `"slotLabelFormat"` | `"slotHeaderFormat"` |
| `SLOT_LABEL_INTERVAL` | `"slotLabelInterval"` | `"slotHeaderInterval"` |
| `SLOT_LABEL_CLASS_NAMES` | `"slotLabelClassNames"` | `"slotHeaderClass"` |
| `SLOT_LABEL_CONTENT` | `"slotLabelContent"` | `"slotHeaderContent"` |
| `SLOT_LABEL_DID_MOUNT` | `"slotLabelDidMount"` | `"slotHeaderDidMount"` (verify) |
| `SLOT_LABEL_WILL_UNMOUNT` | `"slotLabelWillUnmount"` | `"slotHeaderWillUnmount"` (verify) |
| `SLOT_LANE_CLASS_NAMES` | `"slotLaneClassNames"` | `"slotLaneClass"` |
| `VIEW_CLASS_NAMES` | `"viewClassNames"` | `"viewClass"` |
| `MORE_LINK_CLASS_NAMES` | `"moreLinkClassNames"` | `"moreLinkClass"` |
| `DAY_CELL_CLASS_NAMES` | `"dayCellClassNames"` | `"dayCellClass"` |
| `DAY_CELL_CONTENT` | `"dayCellContent"` | `"dayCellTopContent"` |
| `DAY_HEADER_CLASS_NAMES` | `"dayHeaderClassNames"` | `"dayHeaderClass"` |
| `NO_ENTRIES_CLASS_NAMES` | `"noEventsClassNames"` | `"noEventsClass"` |
| `MULTI_MONTH_MIN_WIDTH` | `"multiMonthMinWidth"` | `"singleMonthMinWidth"` |
| `MULTI_MONTH_TITLE_FORMAT` | `"multiMonthTitleFormat"` | `"singleMonthTitleFormat"` |
| `TIME_HINT` | `"timeHint"` | `"timedText"` |
| `ENTRY_HINT` | `"eventHint"` | `"eventsHint"` |
| `NATIVE_TOOLBAR_BUTTON_TEXT` | `"buttonText"` | Investigate: v7 uses `buttons` map structure |

**Note on `ENTRY_BACKGROUND_COLOR` / `ENTRY_BORDER_COLOR` merger:** v7 combines these into a single `eventColor`. If a user sets both, only one can be sent. Investigate whether v7 still accepts the old names as aliases, or if we need to deprecate one and merge in the converter.

### 2b. SchedulerOption Wire Name Changes — `FullCalendarScheduler.java`

| Java Constant | v6 Wire Name | v7 Wire Name |
|---|---|---|
| `RESOURCE_AREA_COLUMNS` | `"resourceAreaColumns"` | `"resourceColumns"` |
| `RESOURCE_LABEL_CLASS_NAMES` | `"resourceLabelClassNames"` | `"resourceCellClassNames"` (verify) |
| `RESOURCE_LABEL_CONTENT` | `"resourceLabelContent"` | `"resourceCellContent"` |
| `RESOURCE_LABEL_DID_MOUNT` | `"resourceLabelDidMount"` | `"resourceCellDidMount"` |
| `RESOURCE_LABEL_WILL_UNMOUNT` | `"resourceLabelWillUnmount"` | `"resourceCellWillUnmount"` |
| `RESOURCE_LANE_CLASS_NAMES` | `"resourceLaneClassNames"` | `"resourceLaneClass"` |
| `RESOURCE_LANE_CONTENT` | `"resourceLaneContent"` | `"resourceLaneTopContent"` (verify — may also need `resourceLaneBottomContent`) |
| `RESOURCE_GROUP_CLASS_NAMES` | `"resourceGroupClassNames"` | renamed per `resourceGroupLabel*` → `resourceGroupHeader*` pattern (verify) |
| `RESOURCE_GROUP_CONTENT` | `"resourceGroupContent"` | renamed (verify) |
| `RESOURCE_LANE_DID_MOUNT` | `"resourceLaneDidMount"` | renamed per pattern (verify) |
| `RESOURCE_LANE_WILL_UNMOUNT` | `"resourceLaneWillUnmount"` | renamed per pattern (verify) |
| `RESOURCE_GROUP_DID_MOUNT` | `"resourceGroupDidMount"` | renamed per `resourceGroupLabel*` → `resourceGroupHeader*` pattern (verify) |
| `RESOURCE_GROUP_WILL_UNMOUNT` | `"resourceGroupWillUnmount"` | renamed per pattern (verify) |
| `RESOURCE_GROUP_LANE_CLASS_NAMES` | `"resourceGroupLaneClassNames"` | `"resourceGroupLaneClass"` (verify) |
| `RESOURCE_GROUP_LANE_CONTENT` | `"resourceGroupLaneContent"` | renamed per pattern (verify) |
| `RESOURCE_GROUP_LANE_DID_MOUNT` | `"resourceGroupLaneDidMount"` | renamed per pattern (verify) |
| `RESOURCE_GROUP_LANE_WILL_UNMOUNT` | `"resourceGroupLaneWillUnmount"` | renamed per pattern (verify) |

### 2c. TypeScript Hook Name Lists — `full-calendar.ts`

Update the hardcoded hook name arrays (2 occurrences at lines ~132 and ~588):
```typescript
// v6:
const entryInfoHooks = ['eventClassNames', 'eventContent', 'eventDidMount', 'eventWillUnmount'];
// v7 (verify eventContent/eventDidMount/eventWillUnmount are unchanged):
const entryInfoHooks = ['eventClass', 'eventContent', 'eventDidMount', 'eventWillUnmount'];
```

### 2d. Deprecated / Removed Options

Mark as `@Deprecated(forRemoval = true)`:

| Constant | Reason |
|---|---|
| `FIXED_MIRROR_PARENT` | Removed in FC v7 — mirror always attaches to `<body>` |
| `NOW_INDICATOR_SNAP` | Removed in FC v7 |
| `SLOT_LANE_CONTENT` | Removed in FC v7 (not renamed) |

Setting these via `setOption()` will silently have no effect.

### Files Modified

| File | Changes |
|------|---------|
| `addon/.../FullCalendar.java` | ~22 Option wire names, 3 @Deprecated |
| `addon-scheduler/.../FullCalendarScheduler.java` | ~17 SchedulerOption wire names |
| `addon/.../full-calendar.ts` | entryInfoHooks array (2 occurrences) |

### Verification

1. `mvn clean install`
2. Full E2E suite: `cd e2e-test-app && mvn clean verify -Pit`
3. Focus on: `display-options.spec.js`, `advanced-options.spec.js`, `scheduler-features.spec.js`, `native-event-listener.spec.js`, `interaction-callbacks.spec.js`
4. Manual verification for renamed options not covered by E2E:
   - `SLOT_LABEL_FORMAT` / `SLOT_LABEL_INTERVAL` — set custom values, verify slot headers render correctly
   - `ENTRY_CLASS_NAMES` — set a callback, verify CSS class appears on entries
   - `DAY_CELL_CONTENT` — set custom content, verify it renders
   - `MULTI_MONTH_MIN_WIDTH` — set value, verify multimonth column width
   - `ENTRY_BACKGROUND_COLOR` / `ENTRY_TEXT_COLOR` — set colors, verify rendering
   - `RESOURCE_AREA_COLUMNS` — set columns, verify resource area
   - `RESOURCE_LANE_CONTENT` — set content, verify it renders in timeline
   - `TIME_HINT` / `ENTRY_HINT` — set values, verify ARIA attributes

### New E2E Test: Option Wire Name Smoke Test

Write a test view that sets every renamed option that produces visible DOM output and asserts the expected CSS class or rendered text appears. This is the only reliable way to catch wrong wire name renames.

---

## Phase 3 — Scheduler License Key + Demo Cleanup

**Risk:** Low

### 3a. License Key Constants — `Scheduler.java`

```java
// Deprecate old constant — value changes to new AGPL string for source compat:
// (callers using the constant automatically get the correct v7 string)
@Deprecated
public static final String GPL_V3_LICENSE_KEY = "AGPL-My-Frontend-And-Backend-Are-Open-Source";

// Add new constant:
public static final String AGPL_LICENSE_KEY = "AGPL-My-Frontend-And-Backend-Are-Open-Source";
```

**Note:** This changes the value of `GPL_V3_LICENSE_KEY` from `"GPL-My-Project-Is-Open-Source"` to the AGPL string. Callers using the constant (not the literal string) automatically get the correct v7 key. Callers using the old string literal directly will need to update.

### 3b. Demo + E2E Files

Update references from `GPL_V3_LICENSE_KEY` to `AGPL_LICENSE_KEY`:
- `demo/.../DemoCalendarWithBackgroundEvent.java`
- `demo/.../AnonymousCustomViewDemo.java`
- `demo/.../CustomViewDemo.java`
- `demo/.../FullDemo.java`
- `e2e-test-app/.../SchedulerFeaturesTestView.java` (uses `DEVELOPER_LICENSE_KEY` — no change needed)

### Verification

1. `mvn clean install`
2. Demo with scheduler views — no license warning in browser console
3. E2E: `scheduler-features.spec.js`

---

## Phase 4 — CSS/Theme Audit + New v7 Features

**Risk:** Medium (upgraded from Low based on review feedback)

### 4a. CSS/Theme Verification (CRITICAL)

**Status from v7 source investigation:** The v7 GitHub source (packages/core/src/styles/) confirms `fc-*` class names and `--fc-*` CSS variables are present. However, the v7 changelog states "CSS modules with obfuscated classNames" and "CSS variables refactored/renamed". This appears to affect internal utility classes only, not the public `fc-*` selectors, but **must be verified against the stable release**.

**Step 1: Verify CSS compatibility**
- Install FC v7 stable, render a calendar, inspect DOM for `fc-*` class presence
- Diff all `--fc-*` variables from v6 vs v7 CSS source
- Confirm the Classic theme preserves stable `fc-*` selectors

**Step 2: Theme plugin import**
FC v7 no longer bundles CSS with JS. Determine:
- Does the Classic theme require an explicit import/plugin registration?
- If so, add the appropriate `@NpmPackage` and TS import
- How does this interact with our `@CssImport` for the Vaadin theme?
- **Vaadin note:** If FC v7 requires a CSS import, add it in `full-calendar.ts` (processed by Vite), NOT as a `@CssImport` annotation. The `@CssImport` on the Java class is for the addon's own CSS files.
- **Vaadin note:** If the CSS import must happen before the addon's CSS, ensure the TS `import` comes before the `@CssImport` in load order.

**Step 3: Audit selectors**
Selectors in `full-calendar-styles.css` that may break with flexbox DOM:
- `thead tr.fc-scrollgrid-section-header th` — **will break** (table-based, v7 uses flexbox)
- `.fc-scrollgrid-section-sticky > *` — verify
- `.fc-scroller-harness` — verify

Selectors in `full-calendar-theme-vaadin.css` to verify:
- `.fc-list th .fc-list-day-cushion` — table-based selector
- All 17 `.fc-*` class selectors (listed in design-system.md CSS token table)

**Step 4: Dark mode evaluation**
- Does FC v7's dark mode (`prefers-color-scheme` or `dark` attribute) conflict with Vaadin's `[theme="dark"]` mechanism?
- Can both coexist, or does one override the other?
- Document the recommended approach for addon users

### 4b. New Option Constants (additive)

| New Java Constant | FC v7 Option | Purpose |
|---|---|---|
| `HEADING_LEVEL` | `headingLevel` | Accessibility: heading level (h1-h6) for view headings |
| `EVENT_SLICING` | `eventSlicing` | Multi-day event fragmentation with +more links |
| `VIEW_CHANGE_HINT` | `viewChangeHint` | Accessibility hint for view changes |

### 4c. Theme System Exposure

Evaluate whether FC v7 stock themes (Monarch, Forma, Breezy, Pulse) should be exposed as `FullCalendarVariant` enum values or documented as "apply via `addClassName()`". Dark mode support evaluation.

### 4d. Remove Deprecated Constants

Remove from enum (deprecated in Phase 2):
- `Option.FIXED_MIRROR_PARENT`
- `Option.NOW_INDICATOR_SNAP`
- `Option.SLOT_LANE_CONTENT`

### Verification

1. Full E2E suite passes
2. Visual regression check: all 6 view types + scheduler timeline view
3. Specific visual checks: today badge, entry hover filter, selection highlight, scrollgrid border, slot headers
4. No console warnings about unknown FC options
5. Dark mode toggle: verify no visual conflict between FC and Vaadin dark modes

---

## Documentation Updates (all part of 8.0.0)

- `docs/Migration-guides.md` — add "7.x → 8.0" section covering all 4 phases
- `docs/Release-notes.md` — FC v7 upgrade notes
- `docs/Scheduler-license.md` — AGPL note
- `specs/architecture.md` — update "FullCalendar JS v6.1.x" to v7
- `specs/project-context.md` — update version references
- Migration note for renamed options (Java constant names unchanged, wire names changed)
- Deprecation notice for `FIXED_MIRROR_PARENT`, `NOW_INDICATOR_SNAP`, `SLOT_LANE_CONTENT`, `GPL_V3_LICENSE_KEY`
- New options documentation
- CSS migration notes

---

## Phase Summary

All phases ship together as **8.0.0**. Phases are implementation order, not separate releases.

| Phase | Risk | Scope | Effort |
|-------|------|-------|--------|
| 1 | High | NPM packages, TS imports, formatDate(), moment removal | Significant |
| 2 | Medium | ~30 option wire names, 3 deprecated, TS hook names | Moderate |
| 3 | Low | AGPL license key | Trivial |
| 4 | Medium | CSS/theme audit, new options, dark mode, cleanup | Moderate–Significant |
