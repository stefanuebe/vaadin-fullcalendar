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
import {Calendar, CalendarOptions, DateInput, DateRangeInput, DurationInput} from '@fullcalendar/core';
import interaction, {Draggable} from '@fullcalendar/interaction';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import listPlugin from '@fullcalendar/list';
import multiMonthPlugin from '@fullcalendar/multimonth';
import rrulePlugin from '@fullcalendar/rrule';
import {toMoment} from '@fullcalendar/moment'; // only for formatting
import momentTimezonePlugin from '@fullcalendar/moment-timezone';
import allLocales from '@fullcalendar/core/locales-all';
import googleCalendarPlugin from '@fullcalendar/google-calendar';
import iCalendarPlugin from '@fullcalendar/icalendar';

// Simple type, that allows JS object property access via ["xyz"]
export type IterableObject = {
    [key: string]: any,
    hasOwnProperty: (key: string) => boolean;
};

/**
 * Recursively walks a value and evaluates any JsCallback markers.
 * A JsCallback marker is an object with a {@code __jsCallback} string property.
 * <p>
 * Custom property injection (getCustomProperty on event objects) is NOT handled here —
 * it is applied by the monkey-patched calendar.setOption() and applyCustomPropertiesApi()
 * in initCalendar, based on a hard-coded set of well-known entry callback keys.
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

export class FullCalendar extends HTMLElement {

    private _calendar!: Calendar;
    private _resizeObserver: ResizeObserver | null = null;
    private _draggables: Map<HTMLElement, Draggable> = new Map();

    /** IDs of entries fetched from the server-side EntryProvider. Used to distinguish external-source entries. */
    private serverEntryIds: Set<string> = new Set();

    protected noDatesRenderEvent = false;
    protected noDatesRenderEventOnOptionSetting = true;
    protected moreLinkClickAction = "popover"
    protected prefetchEnabled = false;

    /** Pending revert functions from eventDrop/eventResize, keyed by entry ID. */
    private _pendingReverts: Map<string, () => void> = new Map();

    /**
     * Maximum wall-clock time (ms) to keep retrying updateSize after initial render
     * while the scrollgrid is stuck at width: 0px. See issue #231.
     * Override from Java with {@code getElement().setProperty("sizingRetryMaxWaitMs", 1000)}.
     */
    sizingRetryMaxWaitMs = 500;

    /**
     * Retry interval (ms) used while waiting for the scrollgrid to settle. See issue #231.
     * Override from Java with {@code getElement().setProperty("sizingRetryIntervalMs", 20)}.
     */
    sizingRetryIntervalMs = 10;

    // contains any json based initial options (not the ones set via setOption). might be empty in most cases
    protected initialJsonOptions = {};
    protected initialOptions = {};
    protected customViews: any = {};

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

            this._calendar = new Calendar(this, options);

            this.initEventProviderCallbacks();

            // override set option to allow a combination of internal and custom eventDidMount events
            // hacky and needs to be maintained on updates, but currently there seems to be no other way
            let _setOption = this._calendar.setOption;

            // This function is to be used for callback options, where a function is provided to
            // modify the event. The event will be extended with some custom api. Currently there is no
            // other way then hook into e.g. eventDidMount or eventContent to do this.
            let _setOptionCallbackWithCustomApi = (key: any, value: any) => {
                let callback = (info: any) => {
                    this.addCustomAPI(info.event);
                    return value.call(this._calendar, info);
                };

                _setOption.call(this._calendar, key, callback);
            };

            // TODO this is somehow double to the initial options variant, might be reduced to one variant?
            this._calendar.setOption = (key: any, value: any) => {
                // Null/undefined values pass through directly to clear the option — no wrapping
                if (value == null) {
                    _setOption.call(this._calendar, key, value);
                    return;
                }

                // Entry render hooks: inject getCustomProperty via info.event
                const entryInfoHooks = ['eventClassNames', 'eventContent', 'eventDidMount', 'eventWillUnmount'];
                if (entryInfoHooks.includes(key)) {
                    // in these cases add custom api to the event to allow for instance accessing custom properties
                    _setOptionCallbackWithCustomApi.call(this._calendar, key, value);
                // eventOverlap(stillEvent, movingEvent) — two direct event args
                } else if (key === 'eventOverlap') {
                    _setOption.call(this._calendar, key, (stillEvent: any, movingEvent: any) => {
                        this.addCustomAPI(stillEvent);
                        this.addCustomAPI(movingEvent);
                        return value(stillEvent, movingEvent);
                    });
                // eventAllow(dropInfo, draggedEvent) — event is second arg
                } else if (key === 'eventAllow') {
                    _setOption.call(this._calendar, key, (dropInfo: any, draggedEvent: any) => {
                        this.addCustomAPI(draggedEvent);
                        return value(dropInfo, draggedEvent);
                    });
                // selectOverlap(event) — event is first arg
                } else if (key === 'selectOverlap') {
                    _setOption.call(this._calendar, key, (event: any) => {
                        this.addCustomAPI(event);
                        return value(event);
                    });
                } else {
                    _setOption.call(this._calendar, key, value);
                }
            }

            this._calendar.render(); // needed for method calls, that somehow access the calendar's internals.

            // Deferred updateSize to fix broken initial layout when the container dimensions
            // are not yet finalized at render time (e.g. inside tabs, dialogs, lazy-loaded views).
            // Two nested rAFs ensure the call happens after both layout and paint are complete.
            //
            // Additional retry loop for issue #231: in dense timeline layouts (many resources)
            // the inner Preact tree can take longer to settle than two rAFs. computeScrollerDims()
            // then writes width: 0px onto scrollgrid chunk tables, and nothing re-fires because
            // the outer element never resizes. Retry updateSize in wall-clock intervals until the
            // scrollgrid stabilises (width no longer 0px) or we hit the max wait time.
            requestAnimationFrame(() => {
                requestAnimationFrame(() => {
                    this._calendar?.updateSize();

                    const startedAt = performance.now();
                    const retryUpdateSize = () => {
                        // Check ALL scrollgrid sync tables: the scheduler timeline has separate
                        // header and body tables, and the race can leave a later table stuck
                        // while the first one is already settled. Treat "no tables yet" as
                        // still-settling — Preact may not have mounted them at the first tick
                        // with many resources.
                        const tables = Array.from(
                            this.querySelectorAll('.fc-scrollgrid-sync-table')
                        ) as HTMLElement[];
                        const stillSettling = tables.length === 0
                                || tables.some(t => t.style.width === '0px');
                        if (stillSettling &&
                                performance.now() - startedAt < this.sizingRetryMaxWaitMs) {
                            this._calendar?.updateSize();
                            setTimeout(retryUpdateSize, this.sizingRetryIntervalMs);
                        }
                    };
                    setTimeout(retryUpdateSize, this.sizingRetryIntervalMs);
                });
            });

            // Fix for https://github.com/stefanuebe/vaadin_fullcalendar/issues/97
            // calling updateSize or render inside the resize observer leads to an error in combination
            // with the Vaadin AppLayout. To prevent having errors on every collapse/expand of the app layout's
            // sidebar, this error handler will catch this error and ignore it. the error seem to come up due to
            // the transition / animation. Normal resizes should not bring it up.
            // Using addEventListener instead of replacing window.onerror to avoid conflicts with other error handlers.
            window.addEventListener('error', (event: ErrorEvent) => {
                if (event.message && event.message.startsWith('ResizeObserver loop')) {
                    console.debug('Ignored: ResizeObserver loop limit exceeded');
                    event.stopImmediatePropagation();
                }
            });

            // Store ResizeObserver reference for cleanup in disconnectedCallback
            // @ts-ignore - webpack has problems with the resize observer type
            this._resizeObserver = new ResizeObserver((entries: any) => {
                if (!Array.isArray(entries) || !entries.length) {
                    return;
                }
                requestAnimationFrame(() => {
                    this.calendar?.updateSize();
                });
            });
            this._resizeObserver.observe(this);
        }
    }

    /**
     * Called when the element is removed from the DOM. Cleans up resources.
     */
    disconnectedCallback() {
        if (this._resizeObserver) {
            this._resizeObserver.disconnect();
            this._resizeObserver = null;
        }

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
            stickyHeaderDates: true,
            stickyFooterScrollbar: true,
            ...initialOptions,
        };

        if(this.customViews) {
            // extend the options with the custom views and override any "anonymous" views
            options.views = {...options.views, ...this.customViews};
        }

        this.addEventHandlersToOptions(options, events);

        // @ts-ignore
        options['locales'] = allLocales;
        // @ts-ignore
        options['plugins'] = [
            interaction,
            dayGridPlugin,
            timeGridPlugin,
            listPlugin,
            multiMonthPlugin,
            momentTimezonePlugin,
            rrulePlugin,
            googleCalendarPlugin,
            iCalendarPlugin
        ];

        // Evaluate any JsCallback markers in initial options before passing to FC
        for (const key of Object.keys(options)) {
            (options as Record<string, any>)[key] = evaluateCallbacks((options as Record<string, any>)[key]);
        }

        this.applyCustomPropertiesApi(options);

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
                    color: event.backgroundColor || event.borderColor || '',
                    display: event.display || '',
                };

                // Include extendedProps if present
                if (event.extendedProps && Object.keys(event.extendedProps).length > 0) {
                    data.customProperty = event.extendedProps;
                }

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
     * Formats the given date as an iso string. Setting asDay to true will cut of any time information. Also ignores
     * potential timezone offsets. Should be used for events where the server side works with a LocalDate instance.
     * @param date date
     * @param asDay format as day iso string (optional)
     * @returns {*}
     * @private
     */
    protected formatDate(date: string | Date, asDay = false) {
        if (!(date instanceof Date)) {
            date = new Date(date);
        }

        let moment = toMoment(date, this.calendar!);
        if (asDay) {
            // maybe also utc necessary?
            return moment.startOf('day').format().substring(0, 10);
        }

        return moment.utc().format();
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
                                return this.moreLinkClickAction;
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

    private applyCustomPropertiesApi(options: any) {
        // if the calendar is options to modify the event appearance, we extend the custom api here
        // see _initCalendar for details

        // Entry render hooks: inject getCustomProperty via info.event
        const entryInfoHooks = ['eventClassNames', 'eventContent', 'eventDidMount', 'eventWillUnmount'];
        for (const hookKey of entryInfoHooks) {
            if (typeof options[hookKey] === "function") {
                const initHook = options[hookKey];
                options[hookKey] = (info: any) => {
                    this.addCustomAPI(info.event);
                    return initHook.call(this._calendar, info);
                };
            }
        }

        // eventOverlap(stillEvent, movingEvent) — two direct event args
        if (typeof options.eventOverlap === "function") {
            const initOverlap = options.eventOverlap;
            options.eventOverlap = (stillEvent: any, movingEvent: any) => {
                this.addCustomAPI(stillEvent);
                this.addCustomAPI(movingEvent);
                return initOverlap.call(this._calendar, stillEvent, movingEvent);
            };
        }

        // eventAllow(dropInfo, draggedEvent) — event is second arg
        if (typeof options.eventAllow === "function") {
            const initAllow = options.eventAllow;
            options.eventAllow = (dropInfo: any, draggedEvent: any) => {
                this.addCustomAPI(draggedEvent);
                return initAllow.call(this._calendar, dropInfo, draggedEvent);
            };
        }

        // selectOverlap(event) — event is first arg
        if (typeof options.selectOverlap === "function") {
            const initSelectOverlap = options.selectOverlap;
            options.selectOverlap = (event: any) => {
                this.addCustomAPI(event);
                return initSelectOverlap.call(this._calendar, event);
            };
        }
    }


    private addCustomAPI = (event: any) => {
        if (!event.getCustomProperty) {
            // @ts-ignore
            event.getCustomProperty = (key, defaultValue = undefined) => {
                return FullCalendar.getCustomProperty(event, key, defaultValue);
            }
        }
    };

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
            let value: any = evaluateCallbacks(options[key]);
            this.handleTimeZoneChange(calendar, /*key, */value);
            // @ts-ignore
            calendar.setOption(key, value);
        }
        this.noDatesRenderEvent = false;
    }

    setOption(key: string, value: any) {
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

    /**
     * Reads a custom property from an event. Please use this method for the case, that the access behavior changes in future.
     * @param event event to read from
     * @param key property key to read
     * @param defaultValue
     * @return {*} property value
     */
    static getCustomProperty(event: any, key: string, defaultValue: any = undefined) {
        if (event.extendedProps && event.extendedProps.customProperties && event.extendedProps.customProperties[key]) {
            return event.extendedProps.customProperties[key];
        }

        return defaultValue;
    }

    /**
     * Writes a custom property to an event. Please use this method for the case, that the access behavior changes in future.
     * @param event event to write to
     * @param key property key to write
     * @param value value to write
     */
    static setCustomProperty(event: any, key: string, value: any) {
        if (!event.extendedProps.customProperties) {
            event.setExtendedProp("customProperties", {});
        }
        event.extendedProps.customProperties[key] = value;
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

    updateSize() {
        this.calendar?.updateSize();
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
        // Remove all existing client-managed sources (but not the server-side events function)
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
