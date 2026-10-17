## navLinkClass
- field: navLinkClass: String (ViewOptions.java)
- setters: ['String']
- d.ts: ['typeof refineClassName']
- old NAV_LINK_CLASS Type: class name {@code String} (space separated). No callback.
- old @see: ['https://fullcalendar.io/docs/navLinkClass']

## navLinkDayClick
- field: navLinkDayClick: JsCallback (CalendarOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<string | ((this: CalendarApi, date: Date, jsEvent: UIEvent) => void)>']
- old NAV_LINK_DAY_CLICK Type: view name {@code String} | {@link JsCallback}
- old @see: ['https://fullcalendar.io/docs/navLinkDayClick']

## navLinkHint
- field: navLinkHint: TextValue (ViewOptions.java)
- setters: ['String', 'TextValue']
- d.ts: ['Identity<string | ((dateText: string, date: Date) => string)>']
- old NAV_LINK_HINT Type: {@code String} (use {@code $0} as placeholder for the localized date text, e.g. {@code "Go to $0"} results in "Go to January 1, 2022") | {@link JsCallback}
- old @see: ['https://fullcalendar.io/docs/hints']

## navLinkWeekClick
- field: navLinkWeekClick: JsCallback (CalendarOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<string | ((this: CalendarApi, weekStart: Date, jsEvent: UIEvent) => void)>']
- old NAV_LINK_WEEK_CLICK Type: view name {@code String} | {@link JsCallback}
- old @see: ['https://fullcalendar.io/docs/navLinkWeekClick']

## navLinks
- field: navLinks: Boolean (ViewOptions.java)
- setters: ['Boolean']
- d.ts: ['BooleanConstructor']
- old NAV_LINKS Type: {@code boolean}
- old @see: ['https://fullcalendar.io/docs/navLinks']

## nextDayThreshold
- field: nextDayThreshold: DurationValue (ViewOptions.java)
- setters: ['Duration', 'DurationValue', 'Period', 'String']
- d.ts: ['Duration', 'typeof createDuration']
- old NEXT_DAY_THRESHOLD Type: {@link Duration} | {@link LocalTime} | duration string (e.g. {@code "HH:MM:SS"})
- old @see: ['https://fullcalendar.io/docs/nextDayThreshold']

## nextHint
- field: nextHint: TextValue (CalendarOptions.java)
- setters: ['String', 'TextValue']
- d.ts: ['Identity<string | ((currentUnitText: string, currentUnit: string) => string)>']
- old NEXT_HINT Type: {@code String} (use {@code $0} as placeholder for the unit text, e.g. "week") | {@link JsCallback}
- old @see: ['https://fullcalendar.io/docs/locale']

## nextText
- field: nextText: String (CalendarOptions.java)
- setters: ['String']
- d.ts: ['StringConstructor']
- old NEXT_TEXT Type: {@code String}
- old @see: ['https://fullcalendar.io/docs/locale']

## nextYearText
- field: nextYearText: String (CalendarOptions.java)
- setters: ['String']
- d.ts: ['StringConstructor']
- old NEXT_YEAR_TEXT Type: {@code String}
- old @see: ['https://fullcalendar.io/docs/headerToolbar']

## noEventsClass
- field: noEntriesClass: ClassNameValue (ViewOptions.java)
- setters: ['ClassNameValue', 'String']
- d.ts: ['Identity<ClassNameGenerator<NoEventsInfo>>']
- old NO_ENTRIES_CLASS Type: class name {@code String} (space separated) | {@link JsCallback} returning one
- old @see: ['https://fullcalendar.io/docs/no-events-render-hooks']

## noEventsContent
- field: noEntriesContent: ContentValue (ViewOptions.java)
- setters: ['ContentValue', 'String']
- d.ts: ['Identity<ContentGenerator<NoEventsInfo>>']
- old NO_ENTRIES_CONTENT Type: text, or a {@link JsCallback} returning content
- old @see: ['https://fullcalendar.io/docs/no-events-render-hooks']

## noEventsDidMount
- field: noEntriesDidMount: JsCallback (ViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<DidMountHandler<NoEventsInfo>>']
- old NO_ENTRIES_DID_MOUNT Type: -
- old @see: ['https://fullcalendar.io/docs/no-events-render-hooks']

## noEventsInnerClass
- field: noEntriesInnerClass: ClassNameValue (ViewOptions.java)
- setters: ['ClassNameValue', 'String']
- d.ts: ['Identity<ClassNameGenerator<NoEventsInfo>>']
- old NO_ENTRIES_INNER_CLASS Type: class name {@code String} (space separated) | {@link JsCallback} returning one
- old @see: ['https://fullcalendar.io/docs/no-events-render-hooks']

## noEventsText
- field: noEntriesText: String (ViewOptions.java)
- setters: ['String']
- d.ts: ['StringConstructor']
- old NO_ENTRIES_TEXT Type: {@code String}
- old @see: ['https://fullcalendar.io/docs/no-events-render-hooks']

## noEventsWillUnmount
- field: noEntriesWillUnmount: JsCallback (ViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<WillUnmountHandler<NoEventsInfo>>']
- old NO_ENTRIES_WILL_UNMOUNT Type: -
- old @see: ['https://fullcalendar.io/docs/no-events-render-hooks']

## nonBusinessHoursClass
- field: nonBusinessHoursClass: String (ViewOptions.java)
- setters: ['String']
- d.ts: ['typeof refineClassName']
- old NON_BUSINESS_HOURS_CLASS Type: class name {@code String} (space separated). No callback.
- old @see: ['https://fullcalendar.io/docs/business-hours-render-hooks']

## now
- field: now: NowValue (ViewOptions.java)
- setters: ['LocalDate', 'LocalDateTime', 'NowValue']
- d.ts: ['Identity<DateInput | ((this: CalendarApi) => DateInput)>']
- old NOW Type: {@link LocalDate} | {@link LocalDateTime} | ISO 8601 date {@code String}, e.g. {@code "2013-12-01T00:00:00"} | {@link JsCallback} returning a date
- old @see: ['https://fullcalendar.io/docs/now']

## nowIndicator
- field: nowIndicator: Boolean (ViewOptions.java)
- setters: ['Boolean']
- d.ts: ['BooleanConstructor']
- old NOW_INDICATOR Type: {@code boolean}
- old @see: ['https://fullcalendar.io/docs/nowIndicator']

## nowIndicatorDotClass
- field: nowIndicatorDotClass: String (ViewOptions.java)
- setters: ['String']
- d.ts: ['typeof refineClassName']
- old NOW_INDICATOR_DOT_CLASS Type: class name {@code String} (space separated). No callback.
- old @see: ['https://fullcalendar.io/docs/now-indicator-dot-render-hooks']

## nowIndicatorHeaderClass
- field: nowIndicatorHeaderClass: ClassNameValue (ViewOptions.java)
- setters: ['ClassNameValue', 'String']
- d.ts: ['Identity<ClassNameGenerator<NowIndicatorHeaderInfo>>']
- old NOW_INDICATOR_HEADER_CLASS Type: class name {@code String} (space separated) | {@link JsCallback} returning one
- old @see: ['https://fullcalendar.io/docs/now-indicator-header-render-hooks']

## nowIndicatorHeaderContent
- field: nowIndicatorHeaderContent: ContentValue (ViewOptions.java)
- setters: ['ContentValue', 'String']
- d.ts: ['Identity<ContentGenerator<NowIndicatorHeaderInfo>>']
- old NOW_INDICATOR_HEADER_CONTENT Type: text, or a {@link JsCallback} returning content
- old @see: ['https://fullcalendar.io/docs/now-indicator-header-render-hooks']

## nowIndicatorHeaderDidMount
- field: nowIndicatorHeaderDidMount: JsCallback (ViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<DidMountHandler<NowIndicatorHeaderInfo>>']
- old NOW_INDICATOR_HEADER_DID_MOUNT Type: -
- old @see: ['https://fullcalendar.io/docs/now-indicator-header-render-hooks']

## nowIndicatorHeaderWillUnmount
- field: nowIndicatorHeaderWillUnmount: JsCallback (ViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<WillUnmountHandler<NowIndicatorHeaderInfo>>']
- old NOW_INDICATOR_HEADER_WILL_UNMOUNT Type: -
- old @see: ['https://fullcalendar.io/docs/now-indicator-header-render-hooks']

## nowIndicatorLineClass
- field: nowIndicatorLineClass: ClassNameValue (ViewOptions.java)
- setters: ['ClassNameValue', 'String']
- d.ts: ['Identity<ClassNameGenerator<NowIndicatorLineInfo>>']
- old NOW_INDICATOR_LINE_CLASS Type: class name {@code String} (space separated) | {@link JsCallback} returning one
- old @see: ['https://fullcalendar.io/docs/now-indicator-line-render-hooks']

## nowIndicatorLineContent
- field: nowIndicatorLineContent: ContentValue (ViewOptions.java)
- setters: ['ContentValue', 'String']
- d.ts: ['Identity<ContentGenerator<NowIndicatorLineInfo>>']
- old NOW_INDICATOR_LINE_CONTENT Type: text, or a {@link JsCallback} returning content
- old @see: ['https://fullcalendar.io/docs/now-indicator-line-render-hooks']

## nowIndicatorLineDidMount
- field: nowIndicatorLineDidMount: JsCallback (ViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<DidMountHandler<NowIndicatorLineInfo>>']
- old NOW_INDICATOR_LINE_DID_MOUNT Type: -
- old @see: ['https://fullcalendar.io/docs/now-indicator-line-render-hooks']

## nowIndicatorLineWillUnmount
- field: nowIndicatorLineWillUnmount: JsCallback (ViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<WillUnmountHandler<NowIndicatorLineInfo>>']
- old NOW_INDICATOR_LINE_WILL_UNMOUNT Type: -
- old @see: ['https://fullcalendar.io/docs/now-indicator-line-render-hooks']

## nowIndicatorSnap
- field: nowIndicatorSnap: AutoBoolean (ViewOptions.java)
- setters: ['AutoBoolean']
- d.ts: ['Identity<boolean | "auto">']
- old NOW_INDICATOR_SNAP Type: {@code boolean} | {@code "auto"}
- old @see: ['https://fullcalendar.io/docs/nowIndicatorSnap']

## popoverClass
- field: popoverClass: String (ViewOptions.java)
- setters: ['String']
- d.ts: ['typeof refineClassName']
- old POPOVER_CLASS Type: class name {@code String} (space separated). No callback.
- old @see: ['https://fullcalendar.io/docs/popover-render-hooks']

## popoverCloseClass
- field: popoverCloseClass: String (ViewOptions.java)
- setters: ['String']
- d.ts: ['typeof refineClassName']
- old POPOVER_CLOSE_CLASS Type: class name {@code String} (space separated). No callback.
- old @see: ['https://fullcalendar.io/docs/popover-render-hooks']

## popoverCloseContent
- field: popoverCloseContent: ContentValue (ViewOptions.java)
- setters: ['ContentValue', 'String']
- d.ts: ['Identity<ContentGenerator<{}>>']
- old POPOVER_CLOSE_CONTENT Type: text, or a {@link JsCallback} returning content
- old @see: ['https://fullcalendar.io/docs/popover-render-hooks']

## popoverFormat
- field: popoverFormat: DateFormatValue (ViewOptions.java)
- setters: ['DateFormatValue', 'Map']
- d.ts: ['typeof createFormatter']
- old POPOVER_FORMAT Type: format object, e.g. a {@code Map} with {@code month}, {@code day}, {@code year}, and other properties
- old @see: ['https://fullcalendar.io/docs/popoverFormat']

## prevHint
- field: prevHint: TextValue (CalendarOptions.java)
- setters: ['String', 'TextValue']
- d.ts: ['Identity<string | ((currentUnitText: string, currentUnit: string) => string)>']
- old PREV_HINT Type: {@code String} (use {@code $0} as placeholder for the unit text, e.g. "week") | {@link JsCallback}
- old @see: ['https://fullcalendar.io/docs/locale']

## prevText
- field: prevText: String (CalendarOptions.java)
- setters: ['String']
- d.ts: ['StringConstructor']
- old PREV_TEXT Type: {@code String}
- old @see: ['https://fullcalendar.io/docs/locale']

## prevYearText
- field: prevYearText: String (CalendarOptions.java)
- setters: ['String']
- d.ts: ['StringConstructor']
- old PREV_YEAR_TEXT Type: {@code String}
- old @see: ['https://fullcalendar.io/docs/headerToolbar']

## printMaxRows
- field: printMaxRows: Integer (SchedulerViewOptions.java)
- setters: ['Integer']
- d.ts: ['NumberConstructor']
- old PRINT_MAX_ROWS Type: {@code number}
- old @see: ['https://fullcalendar.io/docs/printMaxRows']

## progressiveEventRendering
- field: progressiveEntryRendering: Boolean (ViewOptions.java)
- setters: ['Boolean']
- d.ts: ['BooleanConstructor']
- old PROGRESSIVE_ENTRY_RENDERING Type: {@code boolean}
- old @see: ['https://fullcalendar.io/docs/progressiveEventRendering']

## refetchResourcesOnNavigate
- field: refetchResourcesOnNavigate: Boolean (SchedulerCalendarOptions.java)
- setters: ['Boolean']
- d.ts: ['BooleanConstructor']
- old REFETCH_RESOURCES_ON_NAVIGATE Type: {@code boolean}
- old @see: ['https://fullcalendar.io/docs/refetchResourcesOnNavigate']

## rerenderDelay
- field: rerenderDelay: Integer (CalendarOptions.java)
- setters: ['Integer']
- d.ts: ['NumberConstructor']
- old RERENDER_DELAY Type: {@code int} | {@code null} (turned off)
- old @see: ['https://fullcalendar.io/docs/rerenderDelay']

## resourceAdd
- field: resourceAdd: JsCallback (SchedulerViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<(info: ResourceAddInfo) => void>']
- old RESOURCE_ADD Type: {@link JsCallback}
- old @see: ['https://fullcalendar.io/docs/resourceAdd']

## resourceCellClass
- field: resourceCellClass: ClassNameValue (SchedulerViewOptions.java)
- setters: ['ClassNameValue', 'String']
- d.ts: ['Identity<ClassNameGenerator<ResourceCellInfo>>']
- old RESOURCE_CELL_CLASS Type: class name {@code String} (space separated) | {@link JsCallback} returning one
- old @see: ['https://fullcalendar.io/docs/resource-cell-render-hooks']

## resourceCellContent
- field: resourceCellContent: ContentValue (SchedulerViewOptions.java)
- setters: ['ContentValue', 'String']
- d.ts: ['Identity<ContentGenerator<ResourceCellInfo>>']
- old RESOURCE_CELL_CONTENT Type: text, or a {@link JsCallback} returning content
- old @see: ['https://fullcalendar.io/docs/resource-cell-render-hooks']

## resourceCellDidMount
- field: resourceCellDidMount: JsCallback (SchedulerViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<DidMountHandler<ResourceCellInfo>>']
- old RESOURCE_CELL_DID_MOUNT Type: {@link JsCallback}
- old @see: ['https://fullcalendar.io/docs/resource-cell-render-hooks']

## resourceCellInnerClass
- field: resourceCellInnerClass: ClassNameValue (SchedulerViewOptions.java)
- setters: ['ClassNameValue', 'String']
- d.ts: ['Identity<ClassNameGenerator<ResourceCellInfo>>']
- old RESOURCE_CELL_INNER_CLASS Type: class name {@code String} (space separated) | {@link JsCallback} returning one
- old @see: ['https://fullcalendar.io/docs/resource-cell-render-hooks']

## resourceCellWillUnmount
- field: resourceCellWillUnmount: JsCallback (SchedulerViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<WillUnmountHandler<ResourceCellInfo>>']
- old RESOURCE_CELL_WILL_UNMOUNT Type: {@link JsCallback}
- old @see: ['https://fullcalendar.io/docs/resource-cell-render-hooks']

## resourceChange
- field: resourceChange: JsCallback (SchedulerViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<(info: ResourceChangeInfo) => void>']
- old RESOURCE_CHANGE Type: {@link JsCallback}
- old @see: ['https://fullcalendar.io/docs/resourceChange']

## resourceColumnDividerClass
- field: resourceColumnDividerClass: String (SchedulerViewOptions.java)
- setters: ['String']
- d.ts: ['typeof refineClassName']
- old RESOURCE_COLUMN_DIVIDER_CLASS Type: class name {@code String} (space separated). A callback is not supported for this option.
- old @see: ['https://fullcalendar.io/docs/resource-column-divider-render-hooks']

## resourceColumnHeaderClass
- field: resourceColumnHeaderClass: ClassNameValue (SchedulerViewOptions.java)
- setters: ['ClassNameValue', 'String']
- d.ts: ['Identity<ClassNameGenerator<ResourceColumnHeaderInfo>>']
- old RESOURCE_COLUMN_HEADER_CLASS Type: class name {@code String} (space separated) | {@link JsCallback} returning one
- old @see: ['https://fullcalendar.io/docs/resource-column-header-render-hooks']

## resourceColumnHeaderContent
- field: resourceColumnHeaderContent: ContentValue (SchedulerViewOptions.java)
- setters: ['ContentValue', 'String']
- d.ts: ['Identity<ContentGenerator<ResourceColumnHeaderInfo>>']
- old RESOURCE_COLUMN_HEADER_CONTENT Type: text, or a {@link JsCallback} returning content
- old @see: ['https://fullcalendar.io/docs/resource-column-header-render-hooks']

## resourceColumnHeaderDidMount
- field: resourceColumnHeaderDidMount: JsCallback (SchedulerViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<DidMountHandler<ResourceColumnHeaderInfo>>']
- old RESOURCE_COLUMN_HEADER_DID_MOUNT Type: {@link JsCallback}
- old @see: ['https://fullcalendar.io/docs/resource-column-header-render-hooks']

## resourceColumnHeaderInnerClass
- field: resourceColumnHeaderInnerClass: ClassNameValue (SchedulerViewOptions.java)
- setters: ['ClassNameValue', 'String']
- d.ts: ['Identity<ClassNameGenerator<ResourceColumnHeaderInfo>>']
- old RESOURCE_COLUMN_HEADER_INNER_CLASS Type: class name {@code String} (space separated) | {@link JsCallback} returning one
- old @see: ['https://fullcalendar.io/docs/resource-column-header-render-hooks']

## resourceColumnHeaderWillUnmount
- field: resourceColumnHeaderWillUnmount: JsCallback (SchedulerViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<WillUnmountHandler<ResourceColumnHeaderInfo>>']
- old RESOURCE_COLUMN_HEADER_WILL_UNMOUNT Type: {@link JsCallback}
- old @see: ['https://fullcalendar.io/docs/resource-column-header-render-hooks']

## resourceColumnResizerClass
- field: resourceColumnResizerClass: ClassNameValue (SchedulerViewOptions.java)
- setters: ['ClassNameValue', 'String']
- d.ts: ['Identity<ClassNameGenerator<ResourceColumnHeaderInfo>>']
- old RESOURCE_COLUMN_RESIZER_CLASS Type: class name {@code String} (space separated) | {@link JsCallback} returning one
- old @see: ['https://fullcalendar.io/docs/resource-column-header-render-hooks']

## resourceColumns
- field: resourceColumns: List<ResourceColumn> (SchedulerCalendarOptions.java)
- setters: ['List']
- d.ts: ['Identity<ColSpec[]>']
- old RESOURCE_COLUMNS Type: {@link List} of {@link ResourceColumn}
- old @see: ['https://fullcalendar.io/docs/resourceColumns']

## resourceColumnsWidth
- field: resourceColumnsWidth: WidthValue (SchedulerViewOptions.java)
- setters: ['WidthValue', 'int']
- d.ts: ['Identity<CssDimValue>']
- old RESOURCE_COLUMNS_WIDTH Type: number of pixels, or CSS width string (e.g. {@code "200px"}, {@code "25%"})
- old @see: ['https://fullcalendar.io/docs/resourceColumnsWidth']

## resourceDayHeaderAlign
- field: resourceDayHeaderAlign: HeaderAlignValue (SchedulerViewOptions.java)
- setters: ['HeaderAlignValue']
- d.ts: ['Identity<"start" | "center" | "end" | ((info: {\n        level: number']
- old RESOURCE_DAY_HEADER_ALIGN Type: {@link HeaderAlign} | {@link JsCallback} returning {@code 'start'}, {@code 'center'} or {@code 'end'}
- old @see: ['https://fullcalendar.io/docs/resource-day-header-render-hooks']

## resourceDayHeaderClass
- field: resourceDayHeaderClass: ClassNameValue (SchedulerViewOptions.java)
- setters: ['ClassNameValue', 'String']
- d.ts: ['Identity<ClassNameGenerator<ResourceDayHeaderInfo>>']
- old RESOURCE_DAY_HEADER_CLASS Type: class name {@code String} (space separated) | {@link JsCallback} returning one
- old @see: ['https://fullcalendar.io/docs/resource-day-header-render-hooks']

## resourceDayHeaderContent
- field: resourceDayHeaderContent: ContentValue (SchedulerViewOptions.java)
- setters: ['ContentValue', 'String']
- d.ts: ['Identity<ContentGenerator<ResourceDayHeaderInfo>>']
- old RESOURCE_DAY_HEADER_CONTENT Type: text, or a {@link JsCallback} returning content
- old @see: ['https://fullcalendar.io/docs/resource-day-header-render-hooks']

## resourceDayHeaderDidMount
- field: resourceDayHeaderDidMount: JsCallback (SchedulerViewOptions.java)
- setters: ['JsCallback']
- d.ts: ['Identity<DidMountHandler<ResourceDayHeaderInfo>>']
- old RESOURCE_DAY_HEADER_DID_MOUNT Type: {@link JsCallback}
- old @see: ['https://fullcalendar.io/docs/resource-day-header-render-hooks']

## resourceDayHeaderInnerClass
- field: resourceDayHeaderInnerClass: ClassNameValue (SchedulerViewOptions.java)
- setters: ['ClassNameValue', 'String']
- d.ts: ['Identity<ClassNameGenerator<ResourceDayHeaderInfo>>']
- old RESOURCE_DAY_HEADER_INNER_CLASS Type: class name {@code String} (space separated) | {@link JsCallback} returning one
- old @see: ['https://fullcalendar.io/docs/resource-day-header-render-hooks']
