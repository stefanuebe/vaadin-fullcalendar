/*
   Copyright 2022, Stefan Uebe

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
import {customElement} from "lit/decorators.js";
import {html, LitElement, PropertyValues} from "lit";

import {Calendar, DateInput, DateRangeInput, DurationInput} from '@fullcalendar/core';
import interaction from '@fullcalendar/interaction';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import listPlugin from '@fullcalendar/list';
import {toMoment} from '@fullcalendar/moment'; // only for formatting
import momentTimezonePlugin from '@fullcalendar/moment-timezone';
import allLocales from '@fullcalendar/core/locales-all.js';
import {ThemableMixin} from "@vaadin/vaadin-themable-mixin";

@customElement("full-calendar")
export class FullCalendar extends ThemableMixin(LitElement) {

    private _calendar?: Calendar;

    dayMaxEvents = false;
    navLinks = true;
    editable = true;
    selectable = true;
    dragScroll = true;
    noDatesRenderEventOnOptionSetting = true;
    initialOptions = {};
    moreLinkClickAction = "popover"
    hasLazyLoadingEntryProvider = false;
    private noDatesRenderEvent = false;

    protected render() {
        return html`
            <slot></slot>
        `;
    }

    protected firstUpdated(_changedProperties: PropertyValues) {
        super.firstUpdated(_changedProperties);

        this.initCalendar();
    }

    connectedCallback() {
        super.connectedCallback();
        try {
            (this as any).$server.setBrowserTimezone(Intl.DateTimeFormat().resolvedOptions().timeZone);
        } catch (e) {
            console.log("Could not obtain browsers time zone", e);
        }
    }

    protected initCalendar() {
        if (!this._calendar) {
            let options = this.createInitOptions(this.initialOptions);

            this._calendar = new Calendar(this, options);


            // override set option to allow a combination of internal and custom eventDidMount events
            // hacky and needs to be maintained on updates, but currently there seems to be no other way
            let _setOption = this._calendar.setOption;

            // This function is to be used for callback options, where a function is provided to
            // modify the event. The event will be extended with some custom api. Currently there is no
            // other way then hook into e.g. eventDidMount or eventContent to do this.
            let _setOptionCallbackWithCustomApi = (key: any, value: any) => {
                let callback = (info: any) => {
                    this.addCustomAPI(info.event);
                    value.call(this._calendar, info);
                };

                _setOption.call(this._calendar, key, callback);
            };

            // TODO this is somehow double to the initial options variant, might be reduced to one variant?
            this._calendar.setOption = (key, value) => {
                if (key === "eventDidMount" || key === "eventContent") {
                    // in these cases add custom api to the event to allow for instance accessing custom properties
                    _setOptionCallbackWithCustomApi.call(this._calendar, key, value);
                } else {
                    _setOption.call(this._calendar, key, value);
                }
            }

            this._calendar.render(); // needed for method calls, that somehow access the calendar's internals.
        }
    }

    protected createInitOptions(initialOptions = {}): any {
        let events = this.createEventHandlers();

        let options = {
            height: '100%',
            timeZone: 'UTC',

            // // no native control elements
            headerToolbar: false,
            weekNumbers: true,
            dayMaxEvents: this.dayMaxEvents,
            navLinks: this.navLinks,
            editable: this.editable,
            selectable: this.selectable,
            dragScroll: this.dragScroll,
            stickyHeaderDates: true,
            stickyFooterScrollbar: true,
            // eventTimeFormat: { hour: 'numeric', minute: '2-digit', timeZoneName: 'short' },
            ...initialOptions,
        };

        this.addEventHandlersToOptions(options, events);
        this.addEventProvider(options);

        // @ts-ignore
        options['locales'] = allLocales;
        // @ts-ignore
        options['plugins'] = [interaction, dayGridPlugin, timeGridPlugin, listPlugin, momentTimezonePlugin];

        // be aware of never setting or passing in any harmful content from the serverside
        // @ts-ignore
        if (typeof options.eventContent === "string") {
            // @ts-ignore
            options.eventContent = new Function("return " + options.eventContent)();
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
                    delta: eventInfo.endDelta
                }
            },
            eventDrop: (eventInfo: any) => {
                return {
                    data: this.convertToEventData(eventInfo.event, eventInfo.oldResource, eventInfo.newResource),
                    delta: eventInfo.delta,
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
                let events = eventInfo.allSegs.map((seg: { event: any; }) => {
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
                options[eventName] = (eventInfo: any) => {
                    const eventDetails = events[eventName](eventInfo);
                    if (eventDetails) {
                        this.dispatchEvent(new CustomEvent(eventName, {
                            detail: eventDetails
                        }));

                        if (eventName === "moreLinkClick") {
                            return this.moreLinkClickAction; // necessary to prevent showing a popup
                        }
                    }

                    return undefined;
                }
            }
        }
    }

    /**
     * Sets the events callback (usage of server side event provider) to the options.
     * @param options
     * @private
     */
    protected addEventProvider(options: any) {
        options.events = (info: any, successCallback: any, failureCallback: any) => {
            // @ts-ignore
            this.$server.fetchFromServer({
                start: this.formatDate(info.start),
                end: this.formatDate(info.end)
            }).then((array: any | any[]) => {
                this.calendar?.removeAllEvents(); // this is necessary to also remove previously manually pushed events (e.g. refreshItem on lazy loading)
                if (Array.isArray(array)) {
                    successCallback(array);
                } else {
                    failureCallback("could not fetch");
                }
            })
        };
    }

    private applyCustomPropertiesApi(options: any) {
        // if the calendar is options to modify the event appearance, we extend the custom api here
        // see _initCalendar for details
        if (typeof options.eventDidMount === "function") {
            let initEventDidMount = options.eventDidMount;
            options.eventDidMount = (info: any) => {
                let event = info.event;
                this.addCustomAPI(event);

                return initEventDidMount.call(this._calendar, info);
            };
        }

        if (typeof options.eventContent === "function") {
            let initEventContent = options.eventContent;
            options.eventContent = (info: any, createElement: any) => {
                let event = info.event;
                this.addCustomAPI(event);

                return initEventContent.call(this._calendar, info, createElement);
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

        if (calendar) {
            this.noDatesRenderEvent = this.noDatesRenderEventOnOptionSetting;

            for (let key in options) {
                let value: any = options[key];
                this.handleTimeZoneChange(calendar, /*key, */value);
                // @ts-ignore
                calendar.setOption(key, value);
            }
            this.noDatesRenderEvent = false;
        } else {
            console.warn("setOptions called before calendar init - implement initialOptions update");
        }
    }

    setOption(key: string, value: any) {
        let calendar = this.calendar;

        if (calendar) {
            // @ts-ignore
            let oldValue = calendar.getOption(key);
            if (oldValue != value) {
                this.noDatesRenderEvent = this.noDatesRenderEventOnOptionSetting;

                // @ts-ignore
                calendar.setOption(key, value);
                this.noDatesRenderEvent = false;

                if (key === "timeZone") {
                    this.handleTimeZoneChange(calendar, value);
                }
            }
        } else {
            console.warn("setOption called before calendar init - implement initialOptions update");
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
     * @returns {*}
     */
    getOption(key: string) {
        // @ts-ignore
        return this.calendar.getOption(key);
    }

    next() {
        this.calendar?.next();
    }

    previous() {
        this.calendar?.prev();
    }

    today() {
        this.calendar?.today();
    }

    gotoDate(date: DateInput) {
        this.calendar?.gotoDate(date);
    }

    scrollToTime(duration: DurationInput) {
        this.calendar?.scrollToTime(duration);
    }

    refreshAllEvents() {
        this.calendar?.refetchEvents();
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
        this.calendar?.changeView(viewName, date);
    }

    renderCalendar() {
        this.calendar?.render();
    }

    setEventClassNamesCallback(s: string) {
        this.calendar?.setOption('eventClassNames', new Function("return " + s)());
    }

    setEventContentCallback(s: string | undefined = undefined) {
        if (this.calendar) {
            console.warn("DEPRECATED: Setting the event content callback after the calendar has" +
                " been initialized is no longer supported. Please use the initial options to set the 'eventContent'.");
        }
    }

    setEventDidMountCallback(s: string) {
        this.calendar?.setOption('eventDidMount', new Function("return " + s)());
    }

    setEventWillUnmountCallback(s: string) {
        this.calendar?.setOption('eventWillUnmount', new Function("return " + s)());
    }

    get calendar(): Calendar | undefined {
        if (!this._calendar) {
            console.warn("get calendar called before first updated. Needs a fix?");
        }

        return this._calendar;
    }

}
