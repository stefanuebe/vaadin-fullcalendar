# FullCalendar v6 Feature Coverage

Legend:
- **Present** — Fully covered with a typed Java API
- **Partial** — Accessible via raw `setOption(String, Object)` but no typed method
- **Missing** — No equivalent at all in the addon

---

## event-model

| FC Option/Feature | Status | Notes |
|---|---|---|
| `id` | Present | `Entry.id` (final, set in constructor) |
| `groupId` | Present | `Entry.setGroupId(String)` |
| `allDay` | Present | `Entry.setAllDay(boolean)` |
| `start` | Present | `Entry.setStart(...)` (multiple overloads) |
| `end` | Present | `Entry.setEnd(...)` (multiple overloads) |
| `title` | Present | `Entry.setTitle(String)` |
| `url` | Missing | No `Entry.setUrl(String)` — FC opens URL on event click |
| `classNames` | Present | `Entry.getOrCreateClassNames()` / `Entry.addClassNames(...)` |
| `editable` | Present | `Entry.setEditable(boolean)` |
| `startEditable` | Present | `Entry.setStartEditable(Boolean)` |
| `durationEditable` | Present | `Entry.setDurationEditable(Boolean)` |
| `resourceEditable` | Present | `ResourceEntry.setResourceEditable(boolean)` |
| `display` | Present | `Entry.setDisplayMode(DisplayMode)` |
| `overlap` | Present | `Entry.setOverlapAllowed(boolean)` |
| `constraint` | Partial | `Entry.setConstraint(String)` — accepts string/groupId only, no typed BusinessHours object |
| `backgroundColor` | Present | `Entry.setBackgroundColor(String)` |
| `borderColor` | Present | `Entry.setBorderColor(String)` |
| `textColor` | Present | `Entry.setTextColor(String)` |
| `color` | Present | `Entry.setColor(String)` |
| `extendedProps` | Present | `Entry.setCustomProperty(String, Object)` / `getCustomProperty(String)` |
| `interactive` | Missing | No `Entry.setInteractive(boolean)` — makes event tabbable/focusable even without a URL |
| `daysOfWeek` (recurring) | Present | `Entry.setRecurringDaysOfWeek(...)` |
| `startTime` (recurring) | Present | `Entry.setRecurringStartTime(...)` |
| `endTime` (recurring) | Present | `Entry.setRecurringEndTime(...)` |
| `startRecur` | Present | `Entry.setRecurringStart(LocalDateTime)` |
| `endRecur` | Present | `Entry.setRecurringEnd(LocalDateTime)` |
| `rrule` (rrule-plugin) | Missing | RRule plugin recurring events not supported (`rrule`, `exdate`, `exrule` properties) |
| `duration` (recurring allday) | Missing | `Entry` has no `duration` field for multi-day all-day recurring events |
| `eventAdd` callback | Missing | No server-side event for "entry added to calendar" lifecycle |
| `eventChange` callback | Missing | No server-side event for "entry modified" lifecycle |
| `eventRemove` callback | Missing | No server-side event for "entry removed" lifecycle |
| `eventsSet` callback | Missing | No server-side event for "all events initialized/changed" |
| `eventDataTransform` | Missing | No hook for transforming raw event data from external sources |
| `defaultAllDay` | Partial | No typed setter; use `setOption("defaultAllDay", ...)` |
| `defaultAllDayEventDuration` | Present | `FullCalendar.DEFAULT_DAY_EVENT_DURATION` constant; no runtime setter |
| `defaultTimedEventDuration` | Present | `FullCalendar.DEFAULT_TIMED_EVENT_DURATION` constant; no runtime setter |
| `forceEventDuration` | Partial | No typed setter; use `setOption("forceEventDuration", ...)` |

---

## event-display

| FC Option/Feature | Status | Notes |
|---|---|---|
| `eventColor` (global) | Present | `Option.ENTRY_COLOR` / `setOption(Option.ENTRY_COLOR, ...)` |
| `eventBackgroundColor` (global) | Present | `Option.ENTRY_BACKGROUND_COLOR` |
| `eventBorderColor` (global) | Present | `Option.ENTRY_BORDER_COLOR` |
| `eventTextColor` (global) | Present | `Option.ENTRY_TEXT_COLOR` |
| `eventDisplay` (global) | Present | `FullCalendar.setEntryDisplay(DisplayMode)` |
| `eventOrder` | Present | `Option.ENTRY_ORDER` via `setOption(Option.ENTRY_ORDER, ...)` |
| `eventOrderStrict` | Present | `Option.ENTRY_ORDER_STRICT` |
| `nextDayThreshold` | Present | `Option.NEXT_DAY_THRESHOLD` |
| `eventTimeFormat` | Present | `Option.ENTRY_TIME_FORMAT` |
| `displayEventTime` | Partial | `Option.DISPLAY_ENTRY_TIME` exists but no typed `setDisplayEntryTime(boolean)` method |
| `displayEventEnd` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `progressiveEventRendering` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `eventClassNames` (render hook) | Present | `FullCalendar.setEntryClassNamesCallback(String)` — JS string only |
| `eventContent` (render hook) | Present | `FullCalendar.setEntryContentCallback(String)` — JS string only |
| `eventDidMount` (render hook) | Present | `FullCalendar.setEntryDidMountCallback(String)` — JS string only |
| `eventWillUnmount` (render hook) | Present | `FullCalendar.setEntryWillUnmountCallback(String)` — JS string only |

---

## event-clicking-hovering

| FC Option/Feature | Status | Notes |
|---|---|---|
| `eventClick` | Present | `addEntryClickedListener(ComponentEventListener<EntryClickedEvent>)` |
| `eventMouseEnter` | Present | `addEntryMouseEnterListener(ComponentEventListener<EntryMouseEnterEvent>)` |
| `eventMouseLeave` | Present | `addEntryMouseLeaveListener(ComponentEventListener<EntryMouseLeaveEvent>)` |

---

## event-dragging-resizing

| FC Option/Feature | Status | Notes |
|---|---|---|
| `editable` (global) | Present | `FullCalendar.setEditable(boolean)` |
| `eventStartEditable` (global) | Present | `FullCalendar.setEntryStartEditable(boolean)` |
| `eventDurationEditable` (global) | Present | `FullCalendar.setEntryDurationEditable(boolean)` |
| `eventResizableFromStart` (global) | Present | `FullCalendar.setEntryResizableFromStart(boolean)` |
| `eventResourceEditable` (global) | Present | `Scheduler.setEntryResourceEditable(boolean)` |
| `eventDrop` | Present | `addEntryDroppedListener(ComponentEventListener<EntryDroppedEvent>)` |
| `eventResize` | Present | `addEntryResizedListener(ComponentEventListener<EntryResizedEvent>)` |
| `eventDragStart` | Missing | No server-side event for drag-begin |
| `eventDragStop` | Missing | No server-side event for drag-end (before drop) |
| `eventResizeStart` | Missing | No server-side event for resize-begin |
| `eventResizeStop` | Missing | No server-side event for resize-end (before resize commit) |
| `eventOverlap` | Present | `Option.SELECT_OVERLAP` (boolean); function form not supported |
| `eventConstraint` | Present | `Option.SELECT_CONSTRAINT` (string/groupId); BusinessHours object form Partial |
| `eventAllow` | Missing | No JS callback for programmatic drag/drop control |
| `dragScroll` | Present | `FullCalendar.setDragScrollActive(boolean)` |
| `dragScrollEls` | Missing | No typed setter |
| `dragRevertDuration` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `snapDuration` | Present | `FullCalendar.setSnapDuration(String)` |
| `allDayMaintainDuration` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `eventDragMinDistance` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `fixedMirrorParent` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `droppable` | Missing | No support for external drag-drop from outside the calendar |
| `dropAccept` | Missing | No typed setter |
| `drop` (external drop callback) | Missing | No server-side event for external element drops |
| `eventReceive` | Missing | No server-side event for externally dragged event received |
| `eventLeave` | Missing | No server-side event for event leaving to another calendar |

---

## event-source

| FC Option/Feature | Status | Notes |
|---|---|---|
| In-memory array | Present | `InMemoryEntryProvider` |
| Function (lazy/callback) | Present | `CallbackEntryProvider` |
| JSON feed URL | Partial | Can be set via raw `setOption("events", url-string)` but no typed Java API; bypasses server-side data model |
| Google Calendar feed | Partial | Can be set via raw initial options; no typed `setGoogleCalendarApiKey(...)` etc. |
| iCalendar feed | Partial | Can be set via raw initial options; no typed API |
| Multiple event sources | Partial | `eventSources` array can be set via raw `setOption(String, Object)` |
| `startParam` / `endParam` | Partial | No typed setters; use raw `setOption(String, Object)` |
| `timeZoneParam` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `lazyFetching` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `loading` callback | Missing | No server-side event for async fetch start/stop |
| `eventSourceFailure` callback | Missing | No server-side event for fetch failure |
| `eventSourceSuccess` callback | Missing | No server-side transform hook for fetch success |
| `initialEvents` option | Partial | Use `InMemoryEntryProvider` instead |

---

## date-clicking-selecting

| FC Option/Feature | Status | Notes |
|---|---|---|
| `selectable` | Present | `FullCalendar.setTimeslotsSelectable(boolean)` |
| `dateClick` | Present | `addTimeslotClickedListener(...)` → `TimeslotClickedEvent` |
| `select` callback | Present | `addTimeslotsSelectedListener(...)` → `TimeslotsSelectedEvent` |
| `unselect` callback | Missing | No server-side event for user clearing a selection |
| `selectMirror` | Present | `Option.SELECT_MIRROR` |
| `selectMinDistance` | Present | `Option.SELECT_MIN_DISTANCE` |
| `selectOverlap` | Present | `Option.SELECT_OVERLAP` (boolean only; function not supported) |
| `selectConstraint` | Present | `Option.SELECT_CONSTRAINT` |
| `selectAllow` | Missing | No JS callback for programmatic selection control |
| `unselectAuto` | Present | `Option.UNSELECT_AUTO` |
| `unselectCancel` | Present | `Option.UNSELECT_CANCEL` |

---

## date-display

| FC Option/Feature | Status | Notes |
|---|---|---|
| `weekends` | Present | `FullCalendar.setWeekends(boolean)` |
| `hiddenDays` | Present | `Option.HIDDEN_DAYS` via `setOption(Option.HIDDEN_DAYS, ...)` |
| `dayHeaders` | Present | `FullCalendar.setColumnHeader(boolean)` (deprecated name) / `Option.DAY_HEADERS` |
| `dayHeaderFormat` | Present | `Option.DAY_HEADER_FORMAT` |
| `dayMinWidth` | Present | `Option.DAY_MIN_WIDTH` (premium feature) |
| `scrollTime` | Present | `Option.SCROLL_TIME` |
| `scrollTimeReset` | Present | `Option.SCROLL_TIME_RESET` |
| `scrollToTime` (method) | Present | `FullCalendar.scrollToTime(String)` / `scrollToTime(LocalTime)` |
| `slotDuration` | Present | `Option.SLOT_DURATION` |
| `slotLabelFormat` | Present | `Option.SLOT_LABEL_FORMAT` |
| `slotLabelInterval` | Present | `Option.SLOT_LABEL_INTERVAL` |
| `slotMinTime` | Present | `FullCalendar.setSlotMinTime(LocalTime)` |
| `slotMaxTime` | Present | `FullCalendar.setSlotMaxTime(LocalTime)` |
| `datesSet` callback | Present | `addDatesRenderedListener(...)` → `DatesRenderedEvent` |
| `dayCellClassNames` (render hook) | Missing | No Java API |
| `dayCellContent` (render hook) | Missing | No Java API |
| `dayCellDidMount` (render hook) | Missing | No Java API |
| `dayCellWillUnmount` (render hook) | Missing | No Java API |
| `dayHeaderClassNames` (render hook) | Missing | No Java API |
| `dayHeaderContent` (render hook) | Missing | No Java API |
| `dayHeaderDidMount` (render hook) | Missing | No Java API |
| `dayHeaderWillUnmount` (render hook) | Missing | No Java API |
| `slotLabelClassNames` (render hook) | Missing | No Java API |
| `slotLabelContent` (render hook) | Missing | No Java API |
| `slotLabelDidMount` (render hook) | Missing | No Java API |
| `slotLabelWillUnmount` (render hook) | Missing | No Java API |
| `slotLaneClassNames` (render hook) | Missing | No Java API |
| `slotLaneContent` (render hook) | Missing | No Java API |
| `slotLaneDidMount` (render hook) | Missing | No Java API |
| `slotLaneWillUnmount` (render hook) | Missing | No Java API |

---

## date-navigation

| FC Option/Feature | Status | Notes |
|---|---|---|
| `next()` | Present | `FullCalendar.next()` |
| `prev()` | Present | `FullCalendar.previous()` |
| `today()` | Present | `FullCalendar.today()` |
| `gotoDate()` | Present | `FullCalendar.gotoDate(LocalDate)` |
| `getDate()` | Partial | No direct Java equivalent; tracked via `currentIntervalStart` |
| `incrementDate()` | Missing | No direct Java equivalent; use `gotoDate` instead |
| `nextYear()` / `prevYear()` | Missing | No direct Java equivalents |
| `initialDate` | Partial | Can be set via initial options JSON; no typed setter |
| `validRange` | Present | `FullCalendar.setValidRange(LocalDate, LocalDate)` etc. |
| `dateAlignment` | Partial | No typed setter; relevant for custom views |
| `dateIncrement` | Partial | No typed setter; relevant for custom views |
| `navLinks` | Present | `FullCalendar.setNumberClickable(boolean)` |
| `navLinkDayClick` | Partial | No server-round-trip; JS-string only via raw `setOption(String, Object)` |
| `navLinkWeekClick` | Partial | No server-round-trip; JS-string only via raw `setOption(String, Object)` |

---

## toolbar

| FC Option/Feature | Status | Notes |
|---|---|---|
| `headerToolbar` | Present | `FullCalendar.setHeaderToolbar(Header)` |
| `footerToolbar` | Present | `FullCalendar.setFooterToolbar(Footer)` |
| `titleFormat` | Present | No typed setter but `Option` equivalent exists; use `setOption(String, Object)` |
| `titleRangeSeparator` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `buttonText` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `buttonIcons` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `customButtons` | Partial | No typed Java API; can be set via raw `setOption("customButtons", ...)` or initial options; click callbacks are JS only |

---

## sizing

| FC Option/Feature | Status | Notes |
|---|---|---|
| `height` | Present | `FullCalendar.setHeight(int)` / `setHeight(String)` / `setHeightAuto()` / `setHeightByParent()` |
| `contentHeight` | Present | `Option.CONTENT_HEIGHT` |
| `aspectRatio` | Present | `Option.ASPECT_RATIO` |
| `expandRows` | Present | `Option.EXPAND_ROWS` |
| `handleWindowResize` | Partial | Intentionally disabled (ResizeObserver used instead); no public typed setter |
| `windowResizeDelay` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `windowResize` callback | Missing | No server-side event for window resize |
| `stickyHeaderDates` | Present | `Option.STICKY_HEADER_DATES` |
| `stickyFooterScrollbar` | Present | `Option.STICKY_FOOTER_SCROLLBAR` |
| `updateSize()` (method) | Missing | No Java method to force size recalculation |

---

## time-zone

| FC Option/Feature | Status | Notes |
|---|---|---|
| `timeZone` | Present | `FullCalendar.setTimezone(Timezone)` |

---

## localization

| FC Option/Feature | Status | Notes |
|---|---|---|
| `locale` | Present | `FullCalendar.setLocale(Locale)` |
| `firstDay` | Present | `FullCalendar.setFirstDay(DayOfWeek)` |
| `direction` | Present | `Option.DIRECTION` |

---

## week-numbers

| FC Option/Feature | Status | Notes |
|---|---|---|
| `weekNumbers` | Present | `FullCalendar.setWeekNumbersVisible(boolean)` |
| `weekNumberCalculation` | Present | `Option.WEEK_NUMBER_CALCULATION` |
| `weekNumberFormat` | Present | `Option.WEEK_NUMBER_FORMAT` |
| `weekText` | Present | `Option.WEEK_TEXT` |
| `weekTextLong` | Present | `Option.WEEK_TEXT_LONG` |
| `weekNumberClassNames` (render hook) | Missing | No Java API |
| `weekNumberContent` (render hook) | Missing | No Java API |
| `weekNumberDidMount` (render hook) | Missing | No Java API |
| `weekNumberWillUnmount` (render hook) | Missing | No Java API |

---

## now-indicator

| FC Option/Feature | Status | Notes |
|---|---|---|
| `nowIndicator` | Present | `FullCalendar.setNowIndicatorShown(boolean)` |
| `now` option | Partial | No typed setter; use raw `setOption("now", ...)` |
| `nowIndicatorSnap` | Partial | No typed setter (new in v6.1.19); use raw `setOption(String, Object)` |
| `nowIndicatorClassNames` (render hook) | Missing | No Java API |
| `nowIndicatorContent` (render hook) | Missing | No Java API |
| `nowIndicatorDidMount` (render hook) | Missing | No Java API |
| `nowIndicatorWillUnmount` (render hook) | Missing | No Java API |

---

## business-hours

| FC Option/Feature | Status | Notes |
|---|---|---|
| `businessHours` | Present | `FullCalendar.setBusinessHours(BusinessHours...)` |
| `businessHours` per resource | Present | `Resource` constructor accepts `BusinessHours...` |

---

## event-popover (dayMaxEvents, more-link)

| FC Option/Feature | Status | Notes |
|---|---|---|
| `dayMaxEvents` | Present | `FullCalendar.setMaxEntriesPerDay(int)` etc. |
| `dayMaxEventRows` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `dayPopoverFormat` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `eventMaxStack` | Present | `Option.ENTRY_MAX_STACK` |
| `moreLinkClick` | Present | `FullCalendar.setMoreLinkClickAction(MoreLinkClickAction)` + `addMoreLinkClickedListener(...)` |
| `moreLinkClassNames` (render hook) | Missing | No Java API |
| `moreLinkContent` (render hook) | Missing | No Java API |
| `moreLinkDidMount` (render hook) | Missing | No Java API |
| `moreLinkWillUnmount` (render hook) | Missing | No Java API |

---

## list-view

| FC Option/Feature | Status | Notes |
|---|---|---|
| `listDayFormat` | Present | `Option.LIST_DAY_FORMAT` |
| `listDaySideFormat` | Present | `Option.LIST_DAY_SIDE_FORMAT` |
| `noEventsClassNames` (render hook) | Missing | No Java API |
| `noEventsContent` (render hook) | Missing | No Java API |
| `noEventsDidMount` (render hook) | Missing | No Java API |
| `noEventsWillUnmount` (render hook) | Missing | No Java API |

---

## multimonth-view

| FC Option/Feature | Status | Notes |
|---|---|---|
| `multiMonthMaxColumns` | Present | `Option.MULTI_MONTH_MAX_COLUMNS` |
| `multiMonthMinWidth` | Present | `Option.MULTI_MONTH_MIN_WIDTH` |
| `multiMonthTitleFormat` | Present | `Option.MULTI_MONTH_TITLE_FORMAT` |

---

## month-view (daygrid)

| FC Option/Feature | Status | Notes |
|---|---|---|
| `fixedWeekCount` | Present | `FullCalendar.setFixedWeekCount(boolean)` |
| `showNonCurrentDates` | Present | `Option.SHOW_NON_CURRENT_DATES` |
| `monthStartFormat` | Present | `Option.MONTH_START_FORMAT` |

---

## timegrid-view

| FC Option/Feature | Status | Notes |
|---|---|---|
| `allDaySlot` | Present | `Option.ALL_DAY_SLOT` |
| `slotEventOverlap` | Present | `Option.SLOT_ENTRY_OVERLAP` |
| `eventMinHeight` | Present | `Option.ENTRY_MIN_HEIGHT` |
| `eventShortHeight` | Present | `Option.ENTRY_SHORT_HEIGHT` |
| All-day render hooks | Missing | `allDayClassNames/Content/DidMount/WillUnmount` — no Java API |

---

## view-api

| FC Option/Feature | Status | Notes |
|---|---|---|
| `changeView()` | Present | `FullCalendar.changeView(CalendarView)` |
| `initialView` | Present | Set via initial options; `CalendarViewImpl` enum |
| `view` object | Present | Accessible via `DatesRenderedEvent.getCalendarView()` |
| `viewClassNames` (render hook) | Missing | No Java API |
| `viewDidMount` (render hook) | Present | `addViewSkeletonRenderedListener(...)` → `ViewSkeletonRenderedEvent` |
| `viewWillUnmount` (render hook) | Missing | No Java API |

---

## custom-views

| FC Option/Feature | Status | Notes |
|---|---|---|
| Custom view with JS | Present | `CustomCalendarView` / `FullCalendarBuilder.withCustomCalendarViews(...)` |
| `visibleRange` | Partial | For custom views; no typed setter |
| `duration` (custom view) | Partial | No typed setter |
| `dayCount` | Partial | No typed setter |
| `dateAlignment` | Partial | No typed setter |
| `dateIncrement` | Partial | No typed setter |

---

## intro / general

| FC Option/Feature | Status | Notes |
|---|---|---|
| `rerenderDelay` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `themeSystem` | Partial | No typed setter; use raw `setOption(String, Object)` |

---

## date-nav-links

| FC Option/Feature | Status | Notes |
|---|---|---|
| `navLinks` | Present | `FullCalendar.setNumberClickable(boolean)` |
| `navLinkDayClick` | Partial | JS string only (raw `setOption`); no server round-trip |
| `navLinkWeekClick` | Partial | JS string only (raw `setOption`); no server round-trip |

---

## date-library (formatting)

| FC Option/Feature | Status | Notes |
|---|---|---|
| `defaultRangeSeparator` | Partial | No typed setter; use raw `setOption(String, Object)` |

---

## accessibility

| FC Option/Feature | Status | Notes |
|---|---|---|
| `eventInteractive` | Missing | No typed setter (`Entry.setInteractive(boolean)` per-event; global option) |
| `buttonHints` | Missing | No typed setter |
| `viewHint` | Missing | No typed setter |
| `navLinkHint` | Missing | No typed setter |
| `moreLinkHint` | Missing | No typed setter |
| `closeHint` | Missing | No typed setter |
| `timeHint` | Missing | No typed setter |
| `eventHint` | Missing | No typed setter |

---

## touch

| FC Option/Feature | Status | Notes |
|---|---|---|
| `longPressDelay` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `eventLongPressDelay` | Partial | No typed setter; use raw `setOption(String, Object)` |
| `selectLongPressDelay` | Partial | No typed setter; use raw `setOption(String, Object)` |

---

## print

| FC Option/Feature | Status | Notes |
|---|---|---|
| Print support | Partial | FC renders a print-friendly view automatically via CSS; no specific Java API needed |

---

## resource-data (scheduler)

| FC Option/Feature | Status | Notes |
|---|---|---|
| `resources` (array) | Present | `Scheduler.addResources(...)` |
| `resources` (function) | Missing | No callback-based resource provider |
| `resources` (JSON feed) | Missing | No URL-based resource source |
| `initialResources` | Present | Via constructor / `addResources` before attach |
| `addResource()` | Present | `Scheduler.addResource(Resource)` |
| `getResourceById()` | Present | `Scheduler.getResourceById(String)` |
| `getResources()` | Present | `Scheduler.getResources()` |
| `getTopLevelResources()` | Present | `Scheduler.getTopLevelResources()` |
| `refetchResources()` | Missing | No method to trigger resource re-fetch from a function/feed |
| `refetchResourcesOnNavigate` | Partial | `SchedulerOption.REFETCH_RESOURCES_ON_NAVIGATE` in enum but no typed setter |
| `resourceAdd` callback | Missing | No server-side event for resource added |
| `resourceChange` callback | Missing | No server-side event for resource modified |
| `resourceRemove` callback | Missing | No server-side event for resource removed |
| `resourcesSet` callback | Missing | No server-side event for resources initialized/changed |
| `Resource.getParent()` | Present | `Resource.getParent()` |
| `Resource.getChildren()` | Present | `Resource.getChildren()` |
| `Resource.getEvents()` | Missing | No Java equivalent |
| `Resource.remove()` | Present | `Scheduler.removeResource(Resource)` |
| `Resource.setProp()` | Missing | Resource is mostly immutable (final fields); no setProp/setExtendedProp-style update that propagates to client |
| `Resource.setExtendedProp()` | Partial | `Resource.addExtendedProps(String, Object)` exists but does not push updates to client |
| Resource `eventConstraint` property | Missing | Resource-level eventConstraint not supported |
| Resource `eventOverlap` property | Missing | Resource-level eventOverlap not supported |
| Resource `eventAllow` property | Missing | Resource-level eventAllow not supported |
| Resource `eventBackgroundColor` property | Partial | Resource uses `eventColor` (shorthand) only |
| Resource `eventBorderColor` property | Missing | No per-resource border color |
| Resource `eventTextColor` property | Missing | No per-resource text color |
| Resource `eventClassNames` property | Missing | No per-resource event class names |

---

## resource-display (scheduler)

| FC Option/Feature | Status | Notes |
|---|---|---|
| `resourceOrder` | Present | `Scheduler.setResourceOrder(String)` |
| `filterResourcesWithEvents` | Present | `Scheduler.setFilterResourcesWithEvents(boolean)` |
| `resourceLabelClassNames` | Present | `Scheduler.setResourceLabelClassNamesCallback(String)` |
| `resourceLabelContent` | Present | `Scheduler.setResourceLabelContentCallback(String)` |
| `resourceLabelDidMount` | Present | `Scheduler.setResourceLabelDidMountCallback(String)` |
| `resourceLabelWillUnmount` | Present | `Scheduler.setResourceLablelWillUnmountCallback(String)` (note typo in method name) |
| `resourceLaneClassNames` | Present | `Scheduler.setResourceLaneClassNamesCallback(String)` |
| `resourceLaneContent` | Present | `Scheduler.setResourceLaneContentCallback(String)` |
| `resourceLaneDidMount` | Present | `Scheduler.setResourceLaneDidMountCallback(String)` |
| `resourceLaneWillUnmount` | Present | `Scheduler.setResourceLaneWillUnmountCallback(String)` |

---

## timeline-view (scheduler)

| FC Option/Feature | Status | Notes |
|---|---|---|
| `resourceAreaWidth` | Present | `Scheduler.setResourceAreaWidth(String)` |
| `resourceAreaHeaderContent` | Present | `Scheduler.setResourceAreaHeaderContent(String)` |
| `resourceAreaColumns` | Missing | No typed Java API for multi-column resource area |
| `resourceGroupField` | Missing | No typed setter for resource grouping by field |
| `resourcesInitiallyExpanded` | Present | `Scheduler.setResourcesInitiallyExpanded(boolean)` |
| `slotMinWidth` | Present | `Scheduler.setSlotMinWidth(String)` |
| `eventMinWidth` | Partial | No typed setter; use raw `setOption(String, Object)` |
| Resource area header render hooks | Partial | `resourceAreaHeaderContent` set as JS string only |
| Resource group render hooks (`resourceGroupClassNames` etc.) | Missing | No Java API for resource group render hooks |

---

## vertical-resource-view (scheduler)

| FC Option/Feature | Status | Notes |
|---|---|---|
| `datesAboveResources` | Missing | No typed setter |
| `groupByDateAndResource` | Present | `Scheduler.setGroupEntriesBy(GroupEntriesBy.DATE_AND_RESOURCE)` |
| `groupByResource` | Present | `Scheduler.setGroupEntriesBy(GroupEntriesBy.RESOURCE)` |
