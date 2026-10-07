/*
   Copyright 2023, Stefan Uebe

   Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
   documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
   rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
   permit persons to whom the Software is furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in all copies or substantial portions
   of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
   COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
   OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

   Exception of this license is the separately licensed part of the styles.
*/
import {Calendar, CalendarOptions, DateInput, DateRangeInput, DurationInput, PluginInput} from 'fullcalendar';
import interaction, {Draggable} from 'fullcalendar/interaction';
import dayGridPlugin from 'fullcalendar/daygrid';
import timeGridPlugin from 'fullcalendar/timegrid';
import listPlugin from 'fullcalendar/list';
import multiMonthPlugin from 'fullcalendar/multimonth';
import allLocales from 'fullcalendar/locales-all';
import rrulePlugin from '@fullcalendar/rrule';
import googleCalendarPlugin from '@fullcalendar/google-calendar';
import iCalendarPlugin from '@fullcalendar/icalendar';
import legacyClassNamesPlugin from './legacy-class-names';

// @ts-ignore TypeScript knows no type of a plain CSS import, Vite loads it
import 'fullcalendar/skeleton.css';

// Simple type, that allows JS object property access via ["xyz"]
export type IterableObject = {
    [key: string]: any,
    hasOwnProperty: (key: string) => boolean;
};

/**
 * Recursively walks a value and evaluates any JsCallback markers.
 * A JsCallback marker is an object with a {@code __jsCallback} string property.
 * <p>
 * IMPORTANT: This function must only be called on option/config objects, never on
 * entry/event data arrays, to prevent accidental code execution from user data.
 */
export function evaluateCallbacks(value: any): any {
    if (value == null || typeof value !== 'object') return value;

    // JsCallback marker object — evaluate the function string
    if (typeof value.__jsCallback === 'string') {
        return new Function("return " + value.__jsCallback)();
    }

    // Array — recurse into elements
    if (Array.isArray(value)) {
        return value.map((item) => evaluateCallbacks(item));
    }

    // Plain object — recurse into properties
    const result: any = {};
    for (const key of Object.keys(value)) {
        result[key] = evaluateCallbacks(value[key]);
    }
    return result;
}

/**
 * Loads an FC theme and resolves to its theme plugin, or to a module whose default export is the plugin. The loader
 * also loads the theme's CSS.
 */
export type ThemeLoader = () => Promise<PluginInput | { default: PluginInput }>;

/**
 * Loads a stock theme: its plugin, its stylesheet and its default palette. The palette goes into the cascade layer
 * "fc-palette", so that any unlayered palette or color variable of the application wins, regardless of load order
 * (ADR 0002). The CSS is imported as text and wrapped here, so the layer does not depend on the frontend build.
 */
async function loadStockTheme(name: string, plugin: Promise<any>, themeCss: Promise<any>, paletteCss: Promise<any>) {
    const [pluginModule, theme, palette] = await Promise.all([plugin, themeCss, paletteCss]);

    // Vaadin's build turns "?inline" CSS into a Lit CSSResult, plain Vite into a string. Both stringify to the CSS.
    // "vaadin" loads classic too until #266, so the stylesheet may already be there
    if (!document.head.querySelector(`style[data-fc-theme="${name}"]`)) {
        const style = document.createElement('style');
        style.dataset.fcTheme = name;
        style.textContent = `${String(theme.default)}\n@layer fc-palette {\n${String(palette.default)}\n}`;
        document.head.append(style);
    }

    return pluginModule.default;
}

const stockThemes: Record<string, ThemeLoader> = {
    classic: () => loadStockTheme('classic', import('fullcalendar/themes/classic'),
        import('fullcalendar/themes/classic/theme.css?inline'), import('fullcalendar/themes/classic/palette.css?inline')),
    monarch: () => loadStockTheme('monarch', import('fullcalendar/themes/monarch'),
        import('fullcalendar/themes/monarch/theme.css?inline'),
        import('fullcalendar/themes/monarch/palettes/purple.css?inline')),
    breezy: () => loadStockTheme('breezy', import('fullcalendar/themes/breezy'),
        import('fullcalendar/themes/breezy/theme.css?inline'),
        import('fullcalendar/themes/breezy/palettes/indigo.css?inline')),
    forma: () => loadStockTheme('forma', import('fullcalendar/themes/forma'),
        import('fullcalendar/themes/forma/theme.css?inline'), import('fullcalendar/themes/forma/palettes/blue.css?inline')),
    pulse: () => loadStockTheme('pulse', import('fullcalendar/themes/pulse'),
        import('fullcalendar/themes/pulse/theme.css?inline'), import('fullcalendar/themes/pulse/palettes/red.css?inline')),
};

// The Vaadin FC theme is built in #266. Until then "vaadin" renders as classic.
const themeLoaders = new Map<string, ThemeLoader>([...Object.entries(stockThemes), ['vaadin', stockThemes.classic]]);

/** Loaded theme plugins by name. Each theme is loaded once per page, however many calendars use it. */
const loadedThemes = new Map<string, Promise<PluginInput>>();
/** Plugins of themes that finished loading, so a calendar created later gets its theme without a second render. */
const resolvedThemes = new Map<string, PluginInput>();

export class FullCalendar extends HTMLElement {

    /**
     * Registers an FC theme under the given name, so that the server can select it with
     * {@code FullCalendar#setTheme(String)}. The loader runs when a calendar first uses the theme. Register before
     * the calendar selects the theme, e.g. at the top level of a module loaded with {@code @JsModule}.
     * <pre>
     * FullCalendar.registerTheme('corporate', () => import('./corporate-theme'));
     * </pre>
     * A name that is already registered, also a stock theme name, is replaced. Calendars that already show the
     * replaced theme keep it until they select a theme again.
     */
    static registerTheme(name: string, loader: ThemeLoader) {
        themeLoaders.set(name, loader);
        loadedThemes.delete(name);
        resolvedThemes.delete(name);
    }

    private _calendar!: Calendar;
    private _draggables: Map<HTMLElement, Draggable> = new Map();

    /** IDs of entries fetched from the server-side EntryProvider. Used to distinguish external-source entries. */
    private serverEntryIds: Set<string> = new Set();

    protected noDatesRenderEvent = false;
    protected noDatesRenderEventOnOptionSetting = true;
    /**
     * Value of the moreLinkClick option: a string like "popover" or "day", or a function. The option is not passed
     * to FullCalendar, our own handler stays registered so the server keeps receiving the click.
     */
    protected moreLinkClickAction: any = "popover";
    protected prefetchEnabled = false;

    /** Pending revert functions from eventDrop/eventResize, keyed by entry ID. */
    private _pendingReverts: Map<string, () => void> = new Map();

    // contains any json based initial options (not the ones set via setOption). might be empty in most cases
    protected initialJsonOptions = {};
    protected initialOptions = {};
    protected customViews: any = {};

    /** Theme selected by the server, see {@link fcTheme}. */
    private _fcTheme: string | undefined;
    /** Plugins of the calendar without the theme plugin. */
    private _basePlugins: PluginInput[] = [];

    /**
     * Name of the FC theme, set by the server. Until the initial theme is loaded, the calendar is rendered but kept
     * invisible, so it never shows unstyled. A later theme change keeps the previous theme until the new one is loaded.
     */
    set fcTheme(name: string | undefined) {
        this._fcTheme = name;
        if (!name || resolvedThemes.has(name)) {
            this.applyTheme();
            return;
        }

        const loader = themeLoaders.get(name);
        if (!loader) {
            console.error(`FullCalendar: unknown theme "${name}". Registered themes: ${[...themeLoaders.keys()].join(', ')}`);
            this.style.removeProperty('visibility');
            return;
        }

        if (!loadedThemes.has(name)) {
            const started: Promise<PluginInput> = loader().then(module => {
                const plugin = (module as any).default ?? module;
                // registerTheme may have replaced the loader while this one ran, then its result is outdated
                if (loadedThemes.get(name) === started) {
                    resolvedThemes.set(name, plugin);
                }
                return plugin;
            });
            loadedThemes.set(name, started);
        }

        const loading = loadedThemes.get(name)!;
        // Only the theme still selected counts, a calendar switched to another theme meanwhile ignores this one.
        // Errors of applyTheme are not caught here, so they are not reported as a failed load.
        loading.then(() => {
            if (this._fcTheme !== name) {
                return;
            }
            if (resolvedThemes.has(name)) {
                this.applyTheme();
            } else {
                this.fcTheme = name; // the loader was replaced while loading, load with the new one
            }
        }, error => {
            if (themeLoaders.get(name) !== loader) {
                // the loader was replaced while loading, its failure does not count. The new loader is a different
                // function, so this re-run cannot end up here again for it.
                if (this._fcTheme === name) {
                    this.fcTheme = name;
                }
                return;
            }
            // the first of the calendars waiting for this load removes it, so the next selection tries again
            if (loadedThemes.get(name) === loading) {
                loadedThemes.delete(name);
            }
            console.error(`FullCalendar: could not load theme "${name}"`, error);
            if (this._fcTheme === name) {
                this.style.removeProperty('visibility');
            }
        });
    }

    get fcTheme(): string | undefined {
        return this._fcTheme;
    }

    /**
     * Gives the calendar the plugin of the current theme. Before the calendar exists, createInitOptions picks the
     * theme up. A theme that finishes loading after another one was selected is not applied.
     */
    private applyTheme() {
        const plugin = this._fcTheme ? resolvedThemes.get(this._fcTheme) : undefined;
        if (!this._calendar || (this._fcTheme && !plugin)) {
            return;
        }

        // plugins are read only at creation and by resetOptions, setOption ignores them
        this.noDatesRenderEvent = this.noDatesRenderEventOnOptionSetting;
        this._calendar.resetOptions({plugins: plugin ? [...this._basePlugins, plugin] : this._basePlugins}, ['plugins']);
        this.noDatesRenderEvent = false;
        this.style.removeProperty('visibility');
    }

    connectedCallback() {
        if (!this._calendar) {
            this.initCalendar();
        }
        try {
            (this as any).$server.setBrowserTimezone(Intl.DateTimeFormat().resolvedOptions().timeZone);
        } catch (e) {
            console.log("Could not obtain browsers time zone", e);
        }
    }

    protected initCalendar() {
        if (!this._calendar) {
            let options = this.createInitOptions({...this.initialOptions, ...this.initialJsonOptions});
            // the final list, including the plugins subclasses added in their createInitOptions
            this._basePlugins = options.plugins;
            const themePlugin = this._fcTheme ? resolvedThemes.get(this._fcTheme) : undefined;
            if (themePlugin) {
                options.plugins = [...this._basePlugins, themePlugin];
            }

            this._calendar = new Calendar(this, options);

            this.initEventProviderCallbacks();

            // needed for method calls, that somehow access the calendar's internals.
            // FullCalendar resizes itself when the element changes size.
            this._calendar.render();

            // hidden, not unrendered, so the server's calls on the calendar work while the theme loads
            if (!themePlugin && this._fcTheme && loadedThemes.has(this._fcTheme)) {
                this.style.visibility = 'hidden';
            }
        }
    }

    /**
     * Called when the element is removed from the DOM. Cleans up resources.
     */
    disconnectedCallback() {
        // Clean up draggable instances to prevent listener leaks
        this._draggables.forEach(d => d.destroy());
        this._draggables.clear();

        // Clean up pending revert functions
        this._pendingReverts.clear();
    }

    protected createInitOptions(initialOptions = {}): any {
        let events = this.createEventHandlers();

        let options: CalendarOptions = {
            timeZone: 'UTC',
            // // no native control elements
            headerToolbar: false,
            weekNumbers: true,
            tableHeaderSticky: true,
            footerScrollbarSticky: true,
            ...initialOptions,
        };

        if(this.customViews) {
            // extend the options with the custom views and override any "anonymous" views
            options.views = {...options.views, ...this.customViews};
        }

        if ("moreLinkClick" in options) {
            this.setMoreLinkClickAction(options.moreLinkClick);
        }

        this.addEventHandlersToOptions(options, events);

        // @ts-ignore
        options['locales'] = allLocales;
        // Subclasses push their own plugins into options.plugins. initCalendar keeps the final list for theme changes
        // and adds the theme plugin.
        options.plugins = [
            interaction,
            dayGridPlugin,
            timeGridPlugin,
            listPlugin,
            multiMonthPlugin,
            rrulePlugin,
            googleCalendarPlugin,
            iCalendarPlugin as PluginInput, // its typings do not match PluginInput
            legacyClassNamesPlugin
        ];

        // Evaluate any JsCallback markers in initial options before passing to FC
        for (const key of Object.keys(options)) {
            (options as Record<string, any>)[key] = evaluateCallbacks((options as Record<string, any>)[key]);
        }

        return options;
    }

    /**
     * Creates an object that maps client side event information to server side information.
     * The returned object contains keys, that will be interpreted as client and server side event names.
     * Each key is assigned a function that takes the event info object as parameter and returns the
     * set of information as an object.
     * <br><br>
     * Does also include navLinkDayClick, navLinkWeekClick, but here the parameters are different (date for day
     * and weekStart moment for week). See FC doc for details about these functions. Same for moreLinkClick.
     * <br><br>
     * Example of the returned object.
     * <pre>
     {
     select: (eventInfo) => {
     return {
     start: eventInfo.startStr,
     end: eventInfo.endStr,
     allDay: eventInfo.allDay,
     resource: eventInfo.resource ? eventInfo.resource.id : null
     }
     },
     eventClick: (eventInfo) => {
     return {
     id: eventInfo.event.id
     }
     }
     }
     * </pre>
     * @returns an eventhandler definition object
     */
    protected createEventHandlers() {
        // definition of the client to server event mapping
        return {
            dateClick: (eventInfo: any) => {
                return {
                    date: this.formatDate(eventInfo.date, eventInfo.allDay),
                    allDay: eventInfo.allDay,
                    resource: eventInfo.resource ? eventInfo.resource.id : null
                }
            },
            select: (eventInfo: any) => {
                return {
                    start: this.formatDate(eventInfo.start, eventInfo.allDay),
                    end: this.formatDate(eventInfo.end, eventInfo.allDay),
                    allDay: eventInfo.allDay,
                    resource: eventInfo.resource ? eventInfo.resource.id : null
                }
            },
            eventClick: (eventInfo: any) => {
                let event = eventInfo.event;
                return {
                    id: event.id, // we keep this for backward compatibility, but not used by us on server side
                    data: this.convertToEventData(event)
                }
            },
            eventMouseEnter: (eventInfo: any) => {
                let event = eventInfo.event;
                return {
                    data: this.convertToEventData(event)
                }
            },
            eventMouseLeave: (eventInfo: any) => {
                let event = eventInfo.event;
                return {
                    data: this.convertToEventData(event)
                }
            },
            eventResize: (eventInfo: any) => {
                return {
                    data: this.convertToEventData(eventInfo.event),
                    delta: eventInfo.endDelta,
                    sourceId: eventInfo.event?.source?.id ?? null,
                }
            },
            eventDrop: (eventInfo: any) => {
                return {
                    data: this.convertToEventData(eventInfo.event, eventInfo.oldResource, eventInfo.newResource),
                    delta: eventInfo.delta,
                    sourceId: eventInfo.event?.source?.id ?? null,
                }
            },
            datesSet: (eventInfo: any) => {
                if (!this.noDatesRenderEvent) {
                    let view = eventInfo.view;
                    return {
                        name: view.type,
                        intervalStart: this.formatDate(view.currentStart, true),
                        intervalEnd: this.formatDate(view.currentEnd, true),
                        start: this.formatDate(view.activeStart, true),
                        end: this.formatDate(view.activeEnd, true)
                    }
                }

                return false;
            },
            navLinkDayClick: (date: any) => {
                return {
                    date: this.formatDate(date, true),
                    allDay: true
                }
            },
            navLinkWeekClick: (weekStart: any) => {
                return {
                    date: this.formatDate(weekStart, true),
                    allDay: true
                }
            },
            moreLinkClick: (eventInfo: any) => {
                const allSegs = eventInfo.allSegs || [];
                let events = allSegs.map((seg: { event: any; }) => {
                    return this.convertToEventData(seg.event);
                });
                return {
                    date: this.formatDate(eventInfo.date, true),
                    allSegs: events
                }
            },
            viewDidMount: (eventInfo: any) => {
                let view = eventInfo.view;
                return {
                    name: view.type,
                    intervalStart: this.formatDate(view.currentStart, true),
                    intervalEnd: this.formatDate(view.currentEnd, true),
                    start: this.formatDate(view.activeStart, true),
                    end: this.formatDate(view.activeEnd, true)
                }
            },

            // Drag/resize lifecycle events
            eventDragStart: (eventInfo: any) => {
                return {data: this.convertToEventData(eventInfo.event)};
            },
            eventDragStop: (eventInfo: any) => {
                return {data: this.convertToEventData(eventInfo.event)};
            },
            eventResizeStart: (eventInfo: any) => {
                return {data: this.convertToEventData(eventInfo.event)};
            },
            eventResizeStop: (eventInfo: any) => {
                return {data: this.convertToEventData(eventInfo.event)};
            },

            // Unselect
            unselect: (_eventInfo: any) => {
                return {};
            },

            // External drag-drop
            drop: (eventInfo: any) => {
                return {
                    date: this.formatDate(eventInfo.date, eventInfo.allDay),
                    allDay: eventInfo.allDay,
                    draggedElData: eventInfo.draggedEl ? eventInfo.draggedEl.getAttribute('data-event') : null,
                    draggableId: eventInfo.draggedEl ? eventInfo.draggedEl.getAttribute('data-draggable-id') : null
                };
            },
            eventReceive: (eventInfo: any) => {
                const event = eventInfo.event;

                // Send full event data for received entries (not just id/start/end/allDay)
                let data: any = {
                    ...this.convertToEventData(event),
                    title: event.title || '',
                    color: event.color || '',
                    display: event.display || '',
                    extendedProps: event.extendedProps,
                };

                // Remove the client-side phantom entry — the server will manage persistence
                event.remove();

                return {
                    data,
                    draggableId: eventInfo.draggedEl ? eventInfo.draggedEl.getAttribute('data-draggable-id') : null
                };
            },
            eventLeave: (eventInfo: any) => {
                return {data: this.convertToEventData(eventInfo.event)};
            }

        };
    }

    protected convertToEventData(event: any, oldResourceInfo: any = undefined, newResourceInfo: any = undefined) {
        let allDay = event.allDay;

        let start = this.formatDate(event.start, allDay);
        let end = event.end;

        // TODO add allDay parameters?
        if (end != null) {
            end = this.formatDate(end, allDay);
        } else if (event.allDay) { // when moved from time slotted to all day
            end = this.formatDate(new Date(event.start.valueOf() + 86400000), allDay); // + 1 day
        } else { // when moved from all day to time slotted
            end = this.formatDate(new Date(event.start.valueOf() + 3600000), allDay); // + 1 hour
        }

        let data = {
            id: event.id,
            start,
            end,
            allDay,
            // editable: event.extendedProps.editable // FIXME necessary? editable state should not be defined on client, but server only
        };

        if (oldResourceInfo) {
            // @ts-ignore
            data.oldResource = oldResourceInfo.id;
        }

        if (newResourceInfo) {
            // @ts-ignore
            data.newResource = newResourceInfo.id;
        }

        return data;
    }

    /**
     * Formats the given date as an iso string. Setting asDay to true returns the day in the calendar's time zone
     * (yyyy-MM-dd), for values the server side reads as a LocalDate. Otherwise the instant is
     * returned as UTC (yyyy-MM-ddTHH:mm:ssZ).
     * @param date date
     * @param asDay format as day iso string (optional)
     */
    protected formatDate(date: string | Date, asDay = false) {
        if (!(date instanceof Date)) {
            date = new Date(date);
        }

        if (asDay) {
            // formatIso drops the time only when it is exactly midnight. Otherwise (e.g. 23:00 at the end of a
            // 25-hour DST day) it appends the time, which is cut off here. The wall date comes first.
            return this.calendar.formatIso(date, true).substring(0, 10);
        }

        return date.toISOString().replace(/\.\d{3}Z$/, 'Z');
    }

    /**
     * Takes care of registering the events in the options object. Can be overriden for custom handling
     * of special events.
     * @see createInitOptions
     * @see createEventHandlers
     *
     * @param options options
     * @param events events
     */
    protected addEventHandlersToOptions(options: any, events: any) {
        for (let eventName in events) {
            if (events.hasOwnProperty(eventName)) {
                if (eventName === "eventDrop" || eventName === "eventResize") {
                    options[eventName] = (eventInfo: any) => {
                        const eventDetails = events[eventName](eventInfo);
                        if (eventDetails) {
                            const entryId: string = eventInfo.event?.id;
                            const isExternal = entryId != null && !this.serverEntryIds.has(entryId);
                            // Only store the revert for internal entries — auto-revert semantics
                            // apply to drag/resize of existing entries, not to external-source drops.
                            // Storing them for external entries would leak into _pendingReverts.
                            if (!isExternal && entryId != null && typeof eventInfo.revert === 'function') {
                                this._pendingReverts.set(entryId, eventInfo.revert);
                            }
                            const domEventName = isExternal
                                ? (eventName === "eventDrop" ? "externalEntryDrop" : "externalEntryResize")
                                : eventName;
                            this.dispatchEvent(new CustomEvent(domEventName, {detail: eventDetails}));
                        }
                    };
                } else {
                    options[eventName] = (eventInfo: any) => {
                        const eventDetails = events[eventName](eventInfo);
                        if (eventDetails) {
                            this.dispatchEvent(new CustomEvent(eventName, {
                                detail: eventDetails
                            }));

                            if (eventName === "moreLinkClick") {
                                return typeof this.moreLinkClickAction === "function"
                                    ? this.moreLinkClickAction(eventInfo)
                                    : this.moreLinkClickAction;
                            }
                        }

                        return undefined;
                    }
                }
            }
        }
    }

    /**
     * Sets the events callback (usage of server side event provider) to the calendar. Must be called after
     * this.calendar has been initialized, as it is necessary for the format date.
     * @private
     */
    protected initEventProviderCallbacks() {
        const callback = (info: any, successCallback: any, failureCallback: any) => {

            if (this.prefetchEnabled) {
                let rangeUnit = (this.calendar?.view as any)?.getCurrentData()?.dateProfile?.currentRangeUnit;
                if (!rangeUnit) {
                    console.warn("Could not prefetch, as the range unit could not be determined. If you " +
                        "see this warning, please create an issue about it at " +
                        "https://github.com/stefanuebe/vaadin_fullcalendar/issues")
                } else if (!(info.start instanceof Date) || !(info.end instanceof Date)) {
                    console.warn("View range is not of type Date. If you " +
                        "see this warning, please create an issue about it at " +
                        "https://github.com/stefanuebe/vaadin_fullcalendar/issues")

                } else {
                    let getter: (() => number) | undefined = undefined;
                    let setter: ((amount: number) => any) | undefined = undefined;

                    switch (rangeUnit) {
                        case 'day':
                            getter = Date.prototype.getDate;
                            setter = Date.prototype.setDate;
                            break;
                        case 'month':
                            getter = Date.prototype.getMonth;
                            setter = Date.prototype.setMonth;
                            break;
                        case 'year':
                            getter = Date.prototype.getFullYear;
                            setter = Date.prototype.setFullYear;
                            break;
                    }

                    if (setter && getter) {
                        setter.call(info.start, getter.call(info.start) - 1);
                        setter.call(info.end, getter.call(info.end) + 1);
                    }
                }
            }

            // @ts-ignore
            this.$server.fetchEntriesFromServer({
                start: this.formatDate(info.start),
                end: this.formatDate(info.end)
            }).then((array: any | any[]) => {
                if (Array.isArray(array)) {
                    this.serverEntryIds = new Set(array.map((e: any) => e.id));
                    successCallback(array);
                } else {
                    failureCallback("could not fetch");
                }
            }).catch((error: any) => {
                console.error("Failed to fetch entries from server:", error);
                failureCallback(error?.message || "Failed to fetch entries");
            })
        };
        this.calendar?.setOption("events", callback);
    }

    /**
     * Restores the state from the server. All values are optional and might be undefined.
     * @param options options to set
     * @param view view name to set
     * @param date date to go to
     * @private
     */
    protected restoreStateFromServer(options: {} = {}, view: any, date: any) {
        this.calendar?.batchRendering(() => {
            this.setOptions(options);

            // TODO necessary to restore the data fetching?

            if (view) {
                this.changeView(view, date);
            }
            if (date) {
                this.gotoDate(date);
            }
        });
    }

    /**
     * Allows to set a bunch of options at a time.
     * @param options options to set
     */
    setOptions(options: any = {}) {
        let calendar = this.calendar;
        this.noDatesRenderEvent = this.noDatesRenderEventOnOptionSetting;

        for (let key in options) {
            if (key === "moreLinkClick") {
                this.setMoreLinkClickAction(options[key]);
                continue;
            }
            let value: any = evaluateCallbacks(options[key]);
            this.handleTimeZoneChange(calendar, /*key, */value);
            // @ts-ignore
            calendar.setOption(key, value);
        }
        this.noDatesRenderEvent = false;
    }

    setOption(key: string, value: any) {
        if (key === "moreLinkClick") {
            this.setMoreLinkClickAction(value);
            return;
        }

        let calendar = this.calendar;

        value = evaluateCallbacks(value);

        // @ts-ignore
        let oldValue = calendar.getOption(key);

        if (oldValue !== value) {
            this.noDatesRenderEvent = this.noDatesRenderEventOnOptionSetting;

            // @ts-ignore
            calendar.setOption(key, value);
            this.noDatesRenderEvent = false;

            if (key === "timeZone") {
                this.handleTimeZoneChange(calendar, value);
            }
        }
    }

    /**
     * Stores the value of the moreLinkClick option for our own handler. Null restores FullCalendar's default.
     * @param value string, function or JsCallback marker
     */
    protected setMoreLinkClickAction(value: any) {
        this.moreLinkClickAction = evaluateCallbacks(value) ?? "popover";
    }

    /**
     * Special executions for the case that the timezone had changed.
     * @param calendar calendar
     * @param value value
     * @private
     */
    protected handleTimeZoneChange(calendar: Calendar, value: string) {
        calendar.refetchEvents();

        this.dispatchEvent(new CustomEvent("timezone-changed", {
            detail: {
                timezone: value
            }
        }));
    }

    /**
     * Calls the getOption method of the calendar.
     * @param key key
     */
    getOption(key: string): unknown {
        // @ts-ignore
        return this.calendar ? this.calendar.getOption(key) : this.initialJsonOptions[key];
    }

    next() {
        this.calendar.next();
    }

    previous() {
        this.calendar.prev();
    }

    today() {
        this.calendar.today();
    }

    gotoDate(date: DateInput) {
        this.calendar.gotoDate(date);
    }

    scrollToTime(duration: DurationInput) {
        this.calendar.scrollToTime(duration);
    }

    refreshAllEvents() {
        this.calendar.refetchEvents();
    }

    refreshSingleEvent(id: string) {
        console.debug(`refetch all events due to unsupported refresh single event ${id}`);
        this.calendar.refetchEvents();
    }

    /**
     * Reverts a pending drop/resize for the given entry ID.
     * Called from the server when applyChangesOnEntry() was not called.
     */
    revertEntry(entryId: string) {
        const revert = this._pendingReverts.get(entryId);
        if (revert) {
            revert();
            this._pendingReverts.delete(entryId);
        }
    }

    /**
     * Clears a pending revert for the given entry ID (changes were applied).
     */
    clearPendingRevert(entryId: string) {
        this._pendingReverts.delete(entryId);
    }

    changeView(viewName: string, date: DateInput | DateRangeInput | undefined) {
        this.calendar.changeView(viewName, date);
    }

    renderCalendar() {
        if (this.calendar) {
            this.calendar.render();
        }
    }

    // Navigation, size, and JS-only callback setters

    incrementDate(duration: string) {
        this.calendar.incrementDate(duration);
    }

    prevYear() {
        this.calendar.prevYear();
    }

    nextYear() {
        this.calendar.nextYear();
    }

    // --- Draggable management ---

    initDraggable(el: HTMLElement, itemSelector?: string, eventDataCallback?: any) {
        // Destroy existing if present (reattach or re-registration)
        this.destroyDraggable(el);

        const options: any = {};

        if (itemSelector) {
            options.itemSelector = itemSelector;
        }

        if (eventDataCallback) {
            // eventDataCallback is a JsCallback marker — evaluate it
            options.eventData = evaluateCallbacks(eventDataCallback);
        } else {
            // Default: read data-event attribute from the dragged element
            options.eventData = function(dragEl: HTMLElement) {
                const data = dragEl.getAttribute('data-event');
                return data ? JSON.parse(data) : {};
            };
        }

        const d = new Draggable(el, options);
        this._draggables.set(el, d);
    }

    destroyDraggable(el: HTMLElement) {
        const existing = this._draggables.get(el);
        if (existing) {
            existing.destroy();
            this._draggables.delete(el);
        }
    }

    // Event source management

    addEventSource(sourceJson: any) {
        const srcId = sourceJson.id;
        const config = evaluateCallbacks({...sourceJson});
        config.failure = (error: any) => {
            this.dispatchEvent(new CustomEvent("eventSourceFailure", {
                detail: {
                    sourceId: srcId,
                    message: (error && error.message) ? error.message : String(error)
                }
            }));
        };
        this.calendar?.addEventSource(config);
    }

    removeEventSource(id: string) {
        const sources = this.calendar?.getEventSources();
        if (sources) {
            const source = sources.find((s: any) => s.id === id);
            if (source) source.remove();
        }
    }

    setEventSources(sourcesJson: any[]) {
        // Remove all existing remote entry sources (but not the server-side events function)
        const sources = this.calendar?.getEventSources();
        if (sources) {
            sources.forEach((s: any) => { if (s.id !== undefined) s.remove(); });
        }
        sourcesJson.forEach((s: any) => this.addEventSource(s));
    }

    restoreEventSources(sourcesJson: any[]) {
        sourcesJson.forEach((s: any) => this.addEventSource(s));
    }

    refetchEvents() {
        this.calendar?.refetchEvents();
    }

    get calendar(): Calendar{
        if (!this._calendar) {
            this.initCalendar();
        }

        return this._calendar;
    }

}

customElements.define("vaadin-full-calendar", FullCalendar);
