/*
   Copyright 2020, Stefan Uebe

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

import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {afterNextRender} from '@polymer/polymer/lib/utils/render-status.js';

import '@fullcalendar/core/vdom'; // https://github.com/stefanuebe/vaadin_fullcalendar/issues/159
import '@fullcalendar/core'; // https://github.com/stefanuebe/vaadin_fullcalendar/issues/159import {Calendar} from '@fullcalendar/core';
import interaction from '@fullcalendar/interaction';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import listPlugin from '@fullcalendar/list';
import {toMoment} from '@fullcalendar/moment'; // only for formatting
import momentTimezonePlugin from '@fullcalendar/moment-timezone';
import allLocales from '@fullcalendar/core/locales-all.js';

export class FullCalendar extends PolymerElement {

    static get template() {
        return html`
            ${this.templateCalendarCss}
            ${this.templateElementCss}
            ${this.templateContainer}
        `;
    }

    static get templateContainer() {
        return html`
            <div id='calendar-container'>
                <div id='calendar'/>
            </div>`;
    }

    static get properties() {
        return {
            dayMaxEvents: {
                type: Object,
                value: false
            },
            navLinks: {
                type: Boolean,
                value: true
            },
            editable: {
                type: Boolean,
                value: true
            },
            selectable: {
                type: Boolean,
                value: true
            },
            dragScroll: {
                type: Boolean,
                value: true
            },
            noDatesRenderEventOnOptionSetting: {
                type: Boolean,
                value: true
            },
            initialOptions: {
                type: Object,
                value: null
            },
            moreLinkClickAction: {
                type: String,
                value: "popover"
            },
            hasLazyLoadingEntryProvider: {
                type: Boolean,
                value: false
            }
        };
    }

    ready() {
        this._initCalendar();
    }

    getCalendar() {
        if (this._calendar === undefined) {
            this._initCalendar();
        }

        return this._calendar;
    }

    _initCalendar() {
        if (this._calendar === undefined) {
            super.ready();

            try {
                this.$server.setBrowserTimezone(Intl.DateTimeFormat().resolvedOptions().timeZone);
            } catch (e) {
                console.log("Could not obtain browsers time zone", e);
            }

            let options = this._createInitOptions(this.initialOptions);

            // if the calendar is options to modify the event appearance, we extend the custom api here
            // see _initCalendar for details
            if (typeof options.eventDidMount === "function") {
                let initEventDidMount = options.eventDidMount;
                    options.eventDidMount = (info) => {
                        let event = info.event;
                        this._addCustomAPI(event);

                        return initEventDidMount.call(this._calendar, info);
                    };
                }

            if (typeof options.eventContent === "function") {
            let initEventContent = options.eventContent;
                options.eventContent = (info) => {
                    let event = info.event;
                    this._addCustomAPI(event);

                    return initEventContent.call(this._calendar, info);
                };
            }

            this._calendar = new Calendar(this.$.calendar, options);


            // override set option to allow a combination of internal and custom eventDidMount events
            // hacky and needs to be maintained on updates, but currently there seems to be no other way
            let _setOption = this._calendar.setOption;

            // This function is to be used for callback options, where a function is provided to
            // modify the event. The event will be extended with some custom api. Currently there is no
            // other way then hook into e.g. eventDidMount or eventContent to do this.
            let _setOptionCallbackWithCustomApi = (key, value) => {
                _setOption.call(this._calendar, key, info => {
                    this._addCustomAPI(info.event);
                    value.call(this._calendar, info);
                })
            };

            this._calendar.setOption = (key, value) => {
                if(key === "eventDidMount" || key === "eventContent") {
                    // in these cases add custom api to the event to allow for instance accessing custom properties
                    _setOptionCallbackWithCustomApi.call(this._calendar, key, value);
                } else {
                    _setOption.call(this._calendar, key, value);
                }
            }


            this._calendar.render(); // needed for method calls, that somehow access the calendar's internals.

            afterNextRender(this, function () {
                // used to assure correct initial size. It seems, that with V15 (currently 15.0.5) the lifecycle
                // can not guarantee, that the calendar container provides the correct height, since it might not
                // be rendered yet. This method call will upate the size correctly.
                this._calendar.updateSize();

                // WIP, not for production use yet
                // let selector = "#full-calendar-css";
                // const lightStyle = (this.getRootNode().querySelector(selector) || document.querySelector(selector);
                // if (lightStyle) {
                //     let node;
                //     if (lightStyle.tagName === "DOM-MODULE") {
                //         let style = lightStyle.children?.[0]?.content?.children?.[0]
                //         if (style?.tagName === "STYLE") {
                //             node = style;
                //         }
                //     } else if (lightStyle.tagName === "STYLE") {
                //         node = lightStyle;
                //     }
                //
                //     if (node) {
                //         this.shadowRoot.appendChild(node.cloneNode(true));
                //     }
                // }


            });
        }
    }

    /**
     * Restores the state from the server. All values are optional and might be undefined.
     * @param options options to set
     * @param lazyLoadingDataProvider indicates, if the server uses a lazy loading data provider
     * @param view view name to set
     * @param date date to go to
     * @private
     */
    _restoreStateFromServer(options = {}, lazyLoadingDataProvider, view, date) {
        const calendar = this.getCalendar();
        calendar.batchRendering(() => {
            this.setOptions(options);

            if (view) {
                this.changeView(view, date);
            }  if (date) {
                this.gotoDate(date);
            }

            this.setHasLazyLoadingEntryProvider(lazyLoadingDataProvider);
        });

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
    _createEventHandlers() {
        // definition of the client to server event mapping
        return {
            dateClick: (eventInfo) => {
                return {
                    date: this._formatDate(eventInfo.date, eventInfo.allDay),
                    allDay: eventInfo.allDay,
                    resource: eventInfo.resource ? eventInfo.resource.id : null
                }
            },
            select: (eventInfo) => {
                return {
                    start: this._formatDate(eventInfo.start, eventInfo.allDay),
                    end: this._formatDate(eventInfo.end, eventInfo.allDay),
                    allDay: eventInfo.allDay,
                    resource: eventInfo.resource ? eventInfo.resource.id : null
                }
            },
            eventClick: (eventInfo) => {
                let event = eventInfo.event;
                return {
                    id: event.id, // we keep this for backward compatibility, but not used by us on server side
                    data: this._toEventData(event)
                }
            },
            eventMouseEnter: (eventInfo) => {
                let event = eventInfo.event;
                return {
                    data: this._toEventData(event)
                }
            },
            eventMouseLeave: (eventInfo) => {
                let event = eventInfo.event;
                return {
                    data: this._toEventData(event)
                }
            },
            eventResize: (eventInfo) => {
                return {
                    data: this._toEventData(eventInfo.event),
                    delta: eventInfo.endDelta
                }
            },
            eventDrop: (eventInfo) => {
                return {
                    data: this._toEventData(eventInfo.event, eventInfo.oldResource, eventInfo.newResource),
                    delta: eventInfo.delta,
                }
            },
            datesSet: (eventInfo) => {
                if (!this.noDatesRenderEvent) {
                    let view = eventInfo.view;
                    return {
                        name: view.type,
                        intervalStart: this._formatDate(view.currentStart, true),
                        intervalEnd: this._formatDate(view.currentEnd, true),
                        start: this._formatDate(view.activeStart, true),
                        end: this._formatDate(view.activeEnd, true)
                    }
                }

                return false;
            },
            navLinkDayClick: (date) => {
                return {
                    date: this._formatDate(date, true),
                    allDay: true
                }
            },
            navLinkWeekClick: (weekStart) => {
                return {
                    date: this._formatDate(weekStart, true),
                    allDay: true
                }
            },
            moreLinkClick: (eventInfo) => {
                let events = eventInfo.allSegs.map(seg => this._toEventData(seg.event));
                return {
                    date: this._formatDate(eventInfo.date, true),
                    allSegs: events
                }
            },
            viewDidMount: (eventInfo) => {
                let view = eventInfo.view;
                return {
                    name: view.type,
                    intervalStart: this._formatDate(view.currentStart, true),
                    intervalEnd: this._formatDate(view.currentEnd, true),
                    start: this._formatDate(view.activeStart, true),
                    end: this._formatDate(view.activeEnd, true)
                }
            }

        };
    }

    /**
     * Formats the given date as an iso string. Setting asDay to true will cut of any time information. Also ignores
     * potential timezone offsets. Should be used for events where the server side works with a LocalDate instance.
     * @param date date
     * @param asDay format as day iso string (optional)
     * @returns {*}
     * @private
     */
    _formatDate(date, asDay) {
        if (!(date instanceof Date)) {
            date = new Date(date);
        }

        let moment = toMoment(date, this.getCalendar());
        if (asDay) {
            let dateString = moment.startOf('day').format().substr(0, 10);
            return dateString;
        }

        let dateString = moment.utc().format();
        return dateString;
    }

    _createInitOptions(initialOptions = {}) {
        let events = this._createEventHandlers();

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
            ...initialOptions
        };

        this._addEventHandlersToOptions(options, events);

        options['locales'] = allLocales;
        options['plugins'] = [interaction, dayGridPlugin, timeGridPlugin, listPlugin, momentTimezonePlugin];

        // be aware of never setting or passing in any harmful content from the serverside
        if (typeof options.eventContent === "string") {
            options.eventContent = new Function("return " + options.eventContent)();
        }

        return options;
    }

    _addCustomAPI(event) {
        if(!event.getCustomProperty) {
            event.getCustomProperty = (key, defaultValue = undefined) => {
                return FullCalendar.getCustomProperty(event, key, defaultValue);
            }
        }
    }

    /**
     * Takes care of registering the events in the options object. Can be overriden for custom handling
     * of special events.
     * @see _createInitOptions
     * @see _createEventHandlers
     *
     * @param options options
     * @param events events
     */
    _addEventHandlersToOptions(options, events) {
        for (let eventName in events) {
            if (events.hasOwnProperty(eventName)) {
                options[eventName] = (eventInfo) => {
                    const eventDetails = events[eventName](eventInfo);
                    if (eventDetails) {
                        this.dispatchEvent(new CustomEvent(eventName, {
                            detail: eventDetails
                        }));

                        if (eventName === "moreLinkClick") {
                            return this.moreLinkClickAction; // necessary to prevent showing a popup
                        }
                    }
                }
            }
        }
    }

    /**
     * Allows to set a bunch of options at a time.
     * @param options options to set
     */
    setOptions(options = {}) {
        let calendar = this.getCalendar();
        this.noDatesRenderEvent = this.noDatesRenderEventOnOptionSetting;
        for (let key in options) {
            let value = options[key];
            this._handleTimeZoneChange(calendar, key, value);
            calendar.setOption(key, value);
        }
        this.noDatesRenderEvent = false;
    }

    setOption(key, value) {
        let calendar = this.getCalendar();

        let oldValue = calendar.getOption(key);
        if (oldValue != value) {
            this.noDatesRenderEvent = this.noDatesRenderEventOnOptionSetting;

            calendar.setOption(key, value);
            this.noDatesRenderEvent = false;

            if (key === "timeZone") {
                this._handleTimeZoneChange(calendar, value);
            }
        }
    }

    /**
     * Special executions for the case that the timezone had changed.
     * @param calendar calendar
     * @param value value
     * @private
     */
    _handleTimeZoneChange(calendar, value) {
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
    getOption(key) {
        return this.getCalendar().getOption(key);
    }


    _toEventData(event, oldResourceInfo, newResourceInfo) {
        let allDay = event.allDay;

        let start = this._formatDate(event.start, allDay);
        let end = event.end;

        // TODO add allDay parameters?
        if (end != null) {
            end = this._formatDate(end, allDay);
        } else if (event.allDay) { // when moved from time slotted to all day
            end = this._formatDate(new Date(event.start.valueOf() + 86400000), allDay); // + 1 day
        } else { // when moved from all day to time slotted
            end = this._formatDate(new Date(event.start.valueOf() + 3600000), allDay); // + 1 hour
        }

        let data = {
            id: event.id,
            start,
            end,
            allDay,
            // editable: event.extendedProps.editable // FIXME necessary? editable state should not be defined on client, but server only
        };

        if (oldResourceInfo != null) {
            data.oldResource = oldResourceInfo.id;
        }

        if (newResourceInfo != null) {
            data.newResource = newResourceInfo.id;
        }

        return data;
    }

    next() {
        this.getCalendar().next();
    }

    previous() {
        this.getCalendar().prev();
    }

    today() {
        this.getCalendar().today();
    }

    gotoDate(date) {
        this.getCalendar().gotoDate(date);
    }
    
    scrollToTime(duration){
    	this.getCalendar().scrollToTime(duration);
    }

    setHasLazyLoadingEntryProvider(hasLazyLoadingEntryProvider) {
        if (hasLazyLoadingEntryProvider && !this.hasLazyLoadingEntryProvider) {
            let calendar = this.getCalendar();
            this.setOption("events", (info, successCallback, failureCallback)=> {
                this.$server.fetchFromServer({
                    start: this._formatDate(info.start),
                    end: this._formatDate(info.end)
                }).then(array => {
                    calendar.removeAllEvents(); // this is necessary to also remove previously manually pushed events (e.g. refreshItem on lazy loading)
                    if (Array.isArray(array)) {
                        successCallback(array);
                    } else {
                        failureCallback("could not fetch");
                    }
                })

                // failureCallback(err);
            });
        } else if(!hasLazyLoadingEntryProvider && this.hasLazyLoadingEntryProvider) {
            this.setOption("events", []);
        }
    }

    refreshAllEvents() {
        this.getCalendar().refetchEvents();
    }

    refreshSingleEvent(entryJson) {
        entryJson['_hardReset'] = true;
        this.updateEvents([entryJson]); // method expects array parameter
    }

    addEvents(eventsCreateInfo) {
        this.getCalendar().addEventSource(eventsCreateInfo);
    }

    updateEvents(eventsUpdateInfo) {
        const calendar = this.getCalendar();
        calendar.batchRendering(() => {

            for (let i = 0; i < eventsUpdateInfo.length; i++) {
                let obj = eventsUpdateInfo[i];

                let eventToUpdate = calendar.getEventById(obj.id);

                if (eventToUpdate != null) {
                    // since currently recurring events can not be set by updating existing events, we circumcise that
                    // by simply re-adding the event.

                    // Current issues with built in properties (therefore the special handlings of recurring and resources)
                    // - https://github.com/fullcalendar/fullcalendar/issues/4393
                    // - https://github.com/fullcalendar/fullcalendar/issues/5166
                    // - https://github.com/fullcalendar/fullcalendar/issues/5262
                    // Therefore this if will lead to a lot of "reset event", due to the fact, that resource editable
                    // etc. might be set often.

                    if (obj['_hardReset'] === true) { // also used by this.refreshEvent();
                        delete obj["_hardReset"];
                        eventToUpdate.remove();
                        this.addEvents([obj]);
                    } else {
                        for (let key in obj) {
                            switch (key) {
                                case "start":   // fallthrough
                                case "end":     // fallthrough
                                case "allDay":
                                    // NOOP daytime settings are handled separately
                                    break;

                                case "customProperties":
                                    let customProperties = obj[key];
                                    for (let cpKey in customProperties) {
                                        FullCalendar.setCustomProperty(eventToUpdate, cpKey, customProperties[cpKey]);
                                    }
                                    break;

                                default:
                                    eventToUpdate.setProp(key, obj[key]);
                                    break;
                            }
                        }

                        // handle time changes
                        if (obj.start || obj.end || obj.hasOwnProperty("allDay")) {

                            let allDay = obj.hasOwnProperty("allDay") ? obj.allDay : eventToUpdate.allDay;

                            // start must not be null
                            let start = this._toUpdateDate("start", obj, eventToUpdate, false, allDay);
                            let end = this._toUpdateDate("end", obj, eventToUpdate, true, allDay);

                            eventToUpdate.setDates(start, end, {allDay});
                        }

                        // setting all day is not working 100%, we workaround it here
                        if (obj.hasOwnProperty("allDay") && !obj.allDay) {
                            eventToUpdate.moveEnd();
                        }
                    }
                }
            }
        });
    }

    _toUpdateDate(key, obj, eventToUpdate, canBeNull, allDay) {
        let date = null;
        if (obj.hasOwnProperty(key)) {
            if(!canBeNull && obj[key] == null) {
                console.error(`${key} must not be null (obj: ${obj.id})`);
            }

            date = obj[key];
        } else {
            date = eventToUpdate[key];
        }

        if (date == null) {
            return null;
        }

        let formatted = this.getCalendar().formatIso(date, allDay); //somehow not working with the date returned from eventToUpdate

        if (allDay) {
            return formatted.substr(0, 10);
        }

        return formatted;
    }

    _isResourceRelated(obj, eventToUpdate) {
        return obj.hasOwnProperty("resourceIds") || obj.hasOwnProperty("resourceEditable");
    }

    /**
     * Reads a custom property from an event. Please use this method for the case, that the access behavior changes in future.
     * @param event event to read from
     * @param key property key to read
     * @return {*} property value
     */
    static getCustomProperty(event, key, defaultValue = undefined) {
        if(event.extendedProps && event.extendedProps.customProperties && event.extendedProps.customProperties[key]) {
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
    static setCustomProperty(event, key, value) {
        if (!event.extendedProps.customProperties) {
            event.setExtendedProp("customProperties", {}) ;
        }
        event.extendedProps.customProperties[key] = value;
    }

    /**
     * Checks entries coming from the server side if they are recurring.
     * @param event
     * @returns {boolean}
     * @private
     */
    _isServerSideRecurring(event) {
        return (event.daysOfWeek != null && Array.isArray(event.daysOfWeek) && event.daysOfWeek.length) || event.startTime != null || event.endTime != null || event.startRecur != null || event.endRecur != null || event.groupId != null;
    }

    /**
     * Checks entries existing on the client side if they are recurring (experimental)
     * @param event
     * @returns {boolean|boolean}
     * @private
     */
    _isClientSideRecurring(event) {
        return typeof event._def.recurringDef === "object" && event._def.recurringDef != null
    }

    removeEvents(array) {
        // TODO use batch rendering?
        let calendar = this.getCalendar();
        calendar.batchRendering(function () {
            for (var i = 0; i < array.length; i++) {
                let event = calendar.getEventById(array[i].id);
                if (event != null) {
                    event.remove();
                }
            }
        });
    }

    removeAllEvents() {
        //this.getCalendar().getEvents().forEach(e => e.remove());
        var calendar = this.getCalendar();
        this.getCalendar().batchRendering(function () {
            calendar.getEvents().forEach(e => e.remove());
        });
    }

    changeView(viewName, date) {
        this.getCalendar().changeView(viewName, date);
    }

    render() {
        this.getCalendar().render();
    }

    setEventClassNamesCallback(s) {
        let calendar = this.getCalendar();
        calendar.setOption('eventClassNames', new Function("return " + s)());
    }

    setEventContentCallback(s) {
        if (this.calendar) {
            console.warn("DEPRECATED: Setting the event content callback after the calendar has" +
                " been initialized is no longer supported. Please use the initial options to set the 'eventContent'.");
        }
    }

    setEventDidMountCallback(s) {
        let calendar = this.getCalendar();
        calendar.setOption('eventDidMount', new Function("return " + s)());
    }

    setEventWillUnmountCallback(s) {
        let calendar = this.getCalendar();
        calendar.setOption('eventWillUnmount', new Function("return " + s)());
    }

    addCustomStyles(customStylesString) {
        let customStylesElement = document.createElement('style');
        customStylesElement.innerHTML = customStylesString;
        this.shadowRoot.appendChild(customStylesElement);
    }

    static get templateCalendarCss() {
        return html`
            <style>
                .fc-icon, .fc-unselectable {
                    -moz-user-select: none;
                    -ms-user-select: none
                }

                .fc .fc-button, .fc-icon {
                    text-transform: none;
                    font-weight: 400
                }

                .fc-not-allowed, .fc-not-allowed .fc-event {
                    cursor: not-allowed
                }

                .fc .fc-button:not(:disabled), .fc a[data-navlink], .fc-event.fc-event-draggable, .fc-event[href] {
                    cursor: pointer
                }

                .fc-unselectable {
                    -webkit-user-select: none;
                    user-select: none;
                    -webkit-touch-callout: none;
                    -webkit-tap-highlight-color: transparent
                }

                .fc {
                    display: flex;
                    flex-direction: column;
                    font-size: 1em
                }

                .fc .fc-button, .fc-icon {
                    display: inline-block;
                    text-align: center
                }

                .fc, .fc *, .fc :after, .fc :before {
                    box-sizing: border-box
                }

                .fc table {
                    border-collapse: collapse;
                    border-spacing: 0;
                    font-size: 1em
                }

                .fc th {
                    text-align: center
                }

                .fc td, .fc th {
                    vertical-align: top;
                    padding: 0
                }

                .fc .fc-button, .fc .fc-button .fc-icon, .fc .fc-button-group, .fc .fc-timegrid-slot-label {
                    vertical-align: middle
                }

                .fc a[data-navlink]:hover {
                    text-decoration: underline
                }

                .fc .fc-button:hover, .fc .fc-list-event-title a, a.fc-event, a.fc-event:hover {
                    text-decoration: none
                }

                .fc-direction-ltr {
                    direction: ltr;
                    text-align: left
                }

                .fc-direction-rtl {
                    direction: rtl;
                    text-align: right
                }

                .fc-theme-standard td, .fc-theme-standard th {
                    border: 1px solid #ddd;
                    border: 1px solid var(--fc-border-color, #ddd)
                }

                .fc-liquid-hack td, .fc-liquid-hack th {
                    position: relative
                }

                @font-face {
                    font-family: fcicons;
                    src: url("data:application/x-font-ttf;charset=utf-8;base64,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") format('truetype');
                    font-weight: 400;
                    font-style: normal
                }

                .fc-icon {
                    width: 1em;
                    height: 1em;
                    -webkit-user-select: none;
                    user-select: none;
                    font-family: fcicons !important;
                    speak: none;
                    font-style: normal;
                    font-variant: normal;
                    line-height: 1;
                    -webkit-font-smoothing: antialiased;
                    -moz-osx-font-smoothing: grayscale
                }

                .fc-icon-chevron-left:before {
                    content: "\\e900"
                }

                .fc-icon-chevron-right:before {
                    content: "\\e901"
                }

                .fc-icon-chevrons-left:before {
                    content: "\\e902"
                }

                .fc-icon-chevrons-right:before {
                    content: "\\e903"
                }

                .fc-icon-minus-square:before {
                    content: "\\e904"
                }

                .fc-icon-plus-square:before {
                    content: "\\e905"
                }

                .fc-icon-x:before {
                    content: "\\e906"
                }

                .fc .fc-button {
                    overflow: visible;
                    text-transform: none;
                    margin: 0;
                    font-family: inherit
                }

                .fc .fc-button::-moz-focus-inner {
                    padding: 0;
                    border-style: none
                }

                .fc .fc-button {
                    -webkit-appearance: button;
                    -webkit-user-select: none;
                    -moz-user-select: none;
                    -ms-user-select: none;
                    user-select: none;
                    background-color: transparent;
                    border: 1px solid transparent;
                    padding: .4em .65em;
                    font-size: 1em;
                    line-height: 1.5;
                    border-radius: .25em
                }

                .fc .fc-button:focus {
                    outline: 0;
                    box-shadow: 0 0 0 .2rem rgba(44, 62, 80, .25)
                }

                .fc .fc-button-primary:focus, .fc .fc-button-primary:not(:disabled).fc-button-active:focus, .fc .fc-button-primary:not(:disabled):active:focus {
                    box-shadow: 0 0 0 .2rem rgba(76, 91, 106, .5)
                }

                .fc .fc-button:disabled {
                    opacity: .65
                }

                .fc .fc-button-primary {
                    color: #fff;
                    color: var(--fc-button-text-color, #fff);
                    background-color: #2C3E50;
                    background-color: var(--fc-button-bg-color, #2C3E50);
                    border-color: #2C3E50;
                    border-color: var(--fc-button-border-color, #2C3E50)
                }

                .fc .fc-button-primary:hover {
                    color: #fff;
                    color: var(--fc-button-text-color, #fff);
                    background-color: #1e2b37;
                    background-color: var(--fc-button-hover-bg-color, #1e2b37);
                    border-color: #1a252f;
                    border-color: var(--fc-button-hover-border-color, #1a252f)
                }

                .fc .fc-button-primary:disabled {
                    color: #fff;
                    color: var(--fc-button-text-color, #fff);
                    background-color: #2C3E50;
                    background-color: var(--fc-button-bg-color, #2C3E50);
                    border-color: #2C3E50;
                    border-color: var(--fc-button-border-color, #2C3E50)
                }

                .fc .fc-button-primary:not(:disabled).fc-button-active, .fc .fc-button-primary:not(:disabled):active {
                    color: #fff;
                    color: var(--fc-button-text-color, #fff);
                    background-color: #1a252f;
                    background-color: var(--fc-button-active-bg-color, #1a252f);
                    border-color: #151e27;
                    border-color: var(--fc-button-active-border-color, #151e27)
                }

                .fc .fc-button .fc-icon {
                    font-size: 1.5em
                }

                .fc .fc-button-group {
                    position: relative;
                    display: inline-flex
                }

                .fc .fc-button-group > .fc-button {
                    position: relative;
                    flex: 1 1 auto
                }

                .fc .fc-button-group > .fc-button.fc-button-active, .fc .fc-button-group > .fc-button:active, .fc .fc-button-group > .fc-button:focus, .fc .fc-button-group > .fc-button:hover {
                    z-index: 1
                }

                .fc-direction-ltr .fc-button-group > .fc-button:not(:first-child) {
                    margin-left: -1px;
                    border-top-left-radius: 0;
                    border-bottom-left-radius: 0
                }

                .fc-direction-ltr .fc-button-group > .fc-button:not(:last-child) {
                    border-top-right-radius: 0;
                    border-bottom-right-radius: 0
                }

                .fc-direction-rtl .fc-button-group > .fc-button:not(:first-child) {
                    margin-right: -1px;
                    border-top-right-radius: 0;
                    border-bottom-right-radius: 0
                }

                .fc-direction-rtl .fc-button-group > .fc-button:not(:last-child) {
                    border-top-left-radius: 0;
                    border-bottom-left-radius: 0
                }

                .fc .fc-toolbar {
                    display: flex;
                    justify-content: space-between;
                    align-items: center
                }

                .fc .fc-toolbar.fc-header-toolbar {
                    margin-bottom: 1.5em
                }

                .fc .fc-toolbar.fc-footer-toolbar {
                    margin-top: 1.5em
                }

                .fc .fc-toolbar-title {
                    font-size: 1.75em;
                    margin: 0
                }

                .fc-direction-ltr .fc-toolbar > * > :not(:first-child) {
                    margin-left: .75em
                }

                .fc-direction-rtl .fc-toolbar > * > :not(:first-child) {
                    margin-right: .75em
                }

                .fc-direction-rtl .fc-toolbar-ltr {
                    flex-direction: row-reverse
                }

                .fc .fc-scroller {
                    -webkit-overflow-scrolling: touch;
                    position: relative
                }

                .fc .fc-scroller-liquid {
                    height: 100%
                }

                .fc .fc-scroller-liquid-absolute {
                    position: absolute;
                    top: 0;
                    right: 0;
                    left: 0;
                    bottom: 0
                }

                .fc .fc-scroller-harness {
                    position: relative;
                    overflow: hidden;
                    direction: ltr
                }

                .fc .fc-scroller-harness-liquid {
                    height: 100%
                }

                .fc-direction-rtl .fc-scroller-harness > .fc-scroller {
                    direction: rtl
                }

                .fc-theme-standard .fc-scrollgrid {
                    border: 1px solid #ddd;
                    border: 1px solid var(--fc-border-color, #ddd)
                }

                .fc .fc-scrollgrid, .fc .fc-scrollgrid-section-footer > *, .fc .fc-scrollgrid-section-header > * {
                    border-bottom-width: 0
                }

                .fc .fc-scrollgrid, .fc .fc-scrollgrid table {
                    width: 100%;
                    table-layout: fixed
                }

                .fc .fc-scrollgrid table {
                    border-top-style: hidden;
                    border-left-style: hidden;
                    border-right-style: hidden
                }

                .fc .fc-scrollgrid {
                    border-collapse: separate;
                    border-right-width: 0
                }

                .fc .fc-scrollgrid-liquid {
                    height: 100%
                }

                .fc .fc-scrollgrid-section, .fc .fc-scrollgrid-section table, .fc .fc-scrollgrid-section > td {
                    height: 1px
                }

                .fc .fc-scrollgrid-section-liquid > td {
                    height: 100%
                }

                .fc .fc-scrollgrid-section > * {
                    border-top-width: 0;
                    border-left-width: 0
                }

                .fc .fc-scrollgrid-section-body table, .fc .fc-scrollgrid-section-footer table {
                    border-bottom-style: hidden
                }

                .fc .fc-scrollgrid-section-sticky > * {
                    background: var(--fc-page-bg-color, transparent);
                    position: sticky;
                    z-index: 3
                }

                .fc .fc-scrollgrid-section-header.fc-scrollgrid-section-sticky > * {
                    top: 0
                }

                .fc .fc-scrollgrid-section-footer.fc-scrollgrid-section-sticky > * {
                    bottom: 0
                }

                .fc .fc-scrollgrid-sticky-shim {
                    height: 1px;
                    margin-bottom: -1px
                }

                .fc-sticky {
                    position: sticky
                }

                .fc .fc-view-harness {
                    flex-grow: 1;
                    position: relative
                }

                .fc .fc-bg-event, .fc .fc-highlight, .fc .fc-non-business, .fc .fc-view-harness-active > .fc-view {
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0
                }

                .fc .fc-col-header-cell-cushion {
                    display: inline-block;
                    padding: 2px 4px
                }

                .fc .fc-non-business {
                    background: rgba(215, 215, 215, .3);
                    background: var(--fc-non-business-color, rgba(215, 215, 215, .3))
                }

                .fc .fc-bg-event {
                    background: var(--fc-bg-event-color, #8fdf82);
                    opacity: .3;
                    opacity: var(--fc-bg-event-opacity, .3)
                }

                .fc .fc-bg-event .fc-event-title {
                    margin: .5em;
                    font-size: .85em;
                    font-size: var(--fc-small-font-size, .85em);
                    font-style: italic
                }

                .fc .fc-highlight {
                    background: rgba(188, 232, 241, .3);
                    background: var(--fc-highlight-color, rgba(188, 232, 241, .3))
                }

                .fc .fc-cell-shaded, .fc .fc-day-disabled {
                    background: rgba(208, 208, 208, .3);
                    background: var(--fc-neutral-bg-color, rgba(208, 208, 208, .3))
                }

                .fc-event .fc-event-main {
                    position: relative;
                    z-index: 2
                }

                .fc-event-dragging:not(.fc-event-selected) {
                    opacity: .75
                }

                .fc-event-dragging.fc-event-selected {
                    box-shadow: 0 2px 7px rgba(0, 0, 0, .3)
                }

                .fc-event .fc-event-resizer {
                    display: none;
                    position: absolute;
                    z-index: 4
                }

                .fc-event-selected .fc-event-resizer, .fc-event:hover .fc-event-resizer, .fc-h-event {
                    display: block
                }

                .fc-event-selected .fc-event-resizer {
                    border-radius: 4px;
                    border-radius: calc(var(--fc-event-resizer-dot-total-width, 8px) / 2);
                    border-width: 1px;
                    border-width: var(--fc-event-resizer-dot-border-width, 1px);
                    width: 8px;
                    width: var(--fc-event-resizer-dot-total-width, 8px);
                    height: 8px;
                    height: var(--fc-event-resizer-dot-total-width, 8px);
                    border-style: solid;
                    border-color: inherit;
                    background: var(--fc-page-bg-color, #fff)
                }

                .fc-event-selected .fc-event-resizer:before {
                    content: '';
                    position: absolute;
                    top: -20px;
                    left: -20px;
                    right: -20px;
                    bottom: -20px
                }

                .fc-event-selected {
                    box-shadow: 0 2px 5px rgba(0, 0, 0, .2)
                }

                .fc-event-selected:before {
                    content: "";
                    position: absolute;
                    z-index: 3;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0
                }

                .fc-event-selected:after {
                    content: "";
                    background: rgba(0, 0, 0, .25);
                    background: var(--fc-event-selected-overlay-color, rgba(0, 0, 0, .25));
                    position: absolute;
                    z-index: 1;
                    top: -1px;
                    left: -1px;
                    right: -1px;
                    bottom: -1px
                }

                .fc-h-event {
                    border: 1px solid #3788d8;
                    border: 1px solid var(--fc-event-border-color, #3788d8);
                    background-color: #3788d8;
                    background-color: var(--fc-event-bg-color, #3788d8)
                }

                .fc-h-event .fc-event-main {
                    color: #fff;
                    color: var(--fc-event-text-color, #fff)
                }

                .fc-h-event .fc-event-main-frame {
                    display: flex
                }

                .fc-h-event .fc-event-time {
                    max-width: 100%;
                    overflow: hidden
                }

                .fc-h-event .fc-event-title-container {
                    flex-grow: 1;
                    flex-shrink: 1;
                    min-width: 0
                }

                .fc-h-event .fc-event-title {
                    display: inline-block;
                    vertical-align: top;
                    left: 0;
                    right: 0;
                    max-width: 100%;
                    overflow: hidden
                }

                .fc-h-event.fc-event-selected:before {
                    top: -10px;
                    bottom: -10px
                }

                .fc-direction-ltr .fc-daygrid-block-event:not(.fc-event-start), .fc-direction-rtl .fc-daygrid-block-event:not(.fc-event-end) {
                    border-top-left-radius: 0;
                    border-bottom-left-radius: 0;
                    border-left-width: 0
                }

                .fc-direction-ltr .fc-daygrid-block-event:not(.fc-event-end), .fc-direction-rtl .fc-daygrid-block-event:not(.fc-event-start) {
                    border-top-right-radius: 0;
                    border-bottom-right-radius: 0;
                    border-right-width: 0
                }

                .fc-h-event:not(.fc-event-selected) .fc-event-resizer {
                    top: 0;
                    bottom: 0;
                    width: 8px;
                    width: var(--fc-event-resizer-thickness, 8px)
                }

                .fc-direction-ltr .fc-h-event:not(.fc-event-selected) .fc-event-resizer-start, .fc-direction-rtl .fc-h-event:not(.fc-event-selected) .fc-event-resizer-end {
                    cursor: w-resize;
                    left: -4px;
                    left: calc(var(--fc-event-resizer-thickness, 8px) / -2)
                }

                .fc-direction-ltr .fc-h-event:not(.fc-event-selected) .fc-event-resizer-end, .fc-direction-rtl .fc-h-event:not(.fc-event-selected) .fc-event-resizer-start {
                    cursor: e-resize;
                    right: -4px;
                    right: calc(var(--fc-event-resizer-thickness, 8px) / -2)
                }

                .fc-h-event.fc-event-selected .fc-event-resizer {
                    top: 50%;
                    margin-top: -4px;
                    margin-top: calc(var(--fc-event-resizer-dot-total-width, 8px) / -2)
                }

                .fc-direction-ltr .fc-h-event.fc-event-selected .fc-event-resizer-start, .fc-direction-rtl .fc-h-event.fc-event-selected .fc-event-resizer-end {
                    left: -4px;
                    left: calc(var(--fc-event-resizer-dot-total-width, 8px) / -2)
                }

                .fc-direction-ltr .fc-h-event.fc-event-selected .fc-event-resizer-end, .fc-direction-rtl .fc-h-event.fc-event-selected .fc-event-resizer-start {
                    right: -4px;
                    right: calc(var(--fc-event-resizer-dot-total-width, 8px) / -2)
                }

                .fc .fc-popover {
                    position: absolute;
                    z-index: 190;
                    box-shadow: 0 2px 6px rgba(0, 0, 0, .15)
                }

                .fc .fc-popover-header {
                    display: flex;
                    flex-direction: row;
                    justify-content: space-between;
                    align-items: center;
                    padding: 3px 4px
                }

                .fc .fc-popover-title {
                    margin: 0 2px
                }

                .fc .fc-popover-close {
                    cursor: pointer;
                    opacity: .65;
                    font-size: 1.1em
                }

                .fc-theme-standard .fc-popover {
                    border: 1px solid #ddd;
                    border: 1px solid var(--fc-border-color, #ddd);
                    background: var(--fc-page-bg-color, #fff)
                }

                .fc-theme-standard .fc-popover-header {
                    background: rgba(208, 208, 208, .3);
                    background: var(--fc-neutral-bg-color, rgba(208, 208, 208, .3))
                }

                :root {
                    --fc-daygrid-event-dot-width: 8px;
                    --fc-list-event-dot-width: 10px;
                    --fc-list-event-hover-bg-color: #f5f5f5
                }

                .fc-daygrid-day-events:after, .fc-daygrid-day-events:before, .fc-daygrid-day-frame:after, .fc-daygrid-day-frame:before, .fc-daygrid-event-harness:after, .fc-daygrid-event-harness:before {
                    content: "";
                    clear: both;
                    display: table
                }

                .fc .fc-daygrid-body {
                    position: relative;
                    z-index: 1
                }

                .fc .fc-daygrid-day.fc-day-today {
                    background-color: rgba(255, 220, 40, .15);
                    background-color: var(--fc-today-bg-color, rgba(255, 220, 40, .15))
                }

                .fc .fc-daygrid-day-frame {
                    position: relative;
                    min-height: 100%
                }

                .fc .fc-daygrid-day-top {
                    display: flex;
                    flex-direction: row-reverse
                }

                .fc .fc-day-other .fc-daygrid-day-top {
                    opacity: .3
                }

                .fc .fc-daygrid-day-number {
                    position: relative;
                    z-index: 4;
                    padding: 4px
                }

                .fc .fc-daygrid-day-events {
                    margin-top: 1px
                }

                .fc .fc-daygrid-body-balanced .fc-daygrid-day-events {
                    position: absolute;
                    left: 0;
                    right: 0
                }

                .fc .fc-daygrid-body-unbalanced .fc-daygrid-day-events {
                    position: relative;
                    min-height: 2em
                }

                .fc .fc-daygrid-body-natural .fc-daygrid-day-events {
                    margin-bottom: 1em
                }

                .fc .fc-daygrid-event-harness {
                    position: relative
                }

                .fc .fc-daygrid-event-harness-abs {
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0
                }

                .fc .fc-daygrid-bg-harness {
                    position: absolute;
                    top: 0;
                    bottom: 0
                }

                .fc .fc-daygrid-day-bg .fc-non-business {
                    z-index: 1
                }

                .fc .fc-daygrid-day-bg .fc-bg-event {
                    z-index: 2
                }

                .fc .fc-daygrid-day-bg .fc-highlight {
                    z-index: 3
                }

                .fc .fc-daygrid-event {
                    z-index: 6;
                    margin-top: 1px
                }

                .fc .fc-daygrid-event.fc-event-mirror {
                    z-index: 7
                }

                .fc .fc-daygrid-day-bottom {
                    font-size: .85em;
                    padding: 2px 3px 0
                }

                .fc .fc-daygrid-day-bottom:before {
                    content: "";
                    clear: both;
                    display: table
                }

                .fc .fc-daygrid-more-link {
                    position: relative;
                    z-index: 4;
                    cursor: pointer
                }

                .fc .fc-daygrid-week-number {
                    position: absolute;
                    z-index: 5;
                    top: 0;
                    padding: 2px;
                    min-width: 1.5em;
                    text-align: center;
                    background-color: rgba(208, 208, 208, .3);
                    background-color: var(--fc-neutral-bg-color, rgba(208, 208, 208, .3));
                    color: grey;
                    color: var(--fc-neutral-text-color, grey)
                }

                .fc .fc-more-popover .fc-popover-body {
                    min-width: 220px;
                    padding: 10px
                }

                .fc-direction-ltr .fc-daygrid-event.fc-event-start, .fc-direction-rtl .fc-daygrid-event.fc-event-end {
                    margin-left: 2px
                }

                .fc-direction-ltr .fc-daygrid-event.fc-event-end, .fc-direction-rtl .fc-daygrid-event.fc-event-start {
                    margin-right: 2px
                }

                .fc-direction-ltr .fc-daygrid-week-number {
                    left: 0;
                    border-radius: 0 0 3px
                }

                .fc-direction-rtl .fc-daygrid-week-number {
                    right: 0;
                    border-radius: 0 0 0 3px
                }

                .fc-liquid-hack .fc-daygrid-day-frame {
                    position: static
                }

                .fc-daygrid-event {
                    position: relative;
                    white-space: nowrap;
                    border-radius: 3px;
                    font-size: .85em;
                    font-size: var(--fc-small-font-size, .85em)
                }

                .fc-daygrid-block-event .fc-event-time {
                    font-weight: 700
                }

                .fc-daygrid-block-event .fc-event-time, .fc-daygrid-block-event .fc-event-title {
                    padding: 1px
                }

                .fc-daygrid-dot-event {
                    display: flex;
                    align-items: center;
                    padding: 2px 0
                }

                .fc-daygrid-dot-event .fc-event-title {
                    flex-grow: 1;
                    flex-shrink: 1;
                    min-width: 0;
                    overflow: hidden;
                    font-weight: 700
                }

                .fc-daygrid-dot-event.fc-event-mirror, .fc-daygrid-dot-event:hover {
                    background: rgba(0, 0, 0, .1)
                }

                .fc-daygrid-dot-event.fc-event-selected:before {
                    top: -10px;
                    bottom: -10px
                }

                .fc-daygrid-event-dot {
                    margin: 0 4px;
                    box-sizing: content-box;
                    width: 0;
                    height: 0;
                    border: 4px solid #3788d8;
                    border: calc(var(--fc-daygrid-event-dot-width, 8px) / 2) solid var(--fc-event-border-color, #3788d8);
                    border-radius: 4px;
                    border-radius: calc(var(--fc-daygrid-event-dot-width, 8px) / 2)
                }

                .fc-direction-ltr .fc-daygrid-event .fc-event-time {
                    margin-right: 3px
                }

                .fc-direction-rtl .fc-daygrid-event .fc-event-time {
                    margin-left: 3px
                }

                .fc-v-event {
                    display: block;
                    border: 1px solid #3788d8;
                    border: 1px solid var(--fc-event-border-color, #3788d8);
                    background-color: #3788d8;
                    background-color: var(--fc-event-bg-color, #3788d8)
                }

                .fc-v-event .fc-event-main {
                    color: #fff;
                    color: var(--fc-event-text-color, #fff);
                    height: 100%
                }

                .fc-v-event .fc-event-main-frame {
                    height: 100%;
                    display: flex;
                    flex-direction: column
                }

                .fc-v-event .fc-event-time {
                    flex-grow: 0;
                    flex-shrink: 0;
                    max-height: 100%;
                    overflow: hidden
                }

                .fc-v-event .fc-event-title-container {
                    flex-grow: 1;
                    flex-shrink: 1;
                    min-height: 0
                }

                .fc-v-event .fc-event-title {
                    top: 0;
                    bottom: 0;
                    max-height: 100%;
                    overflow: hidden
                }

                .fc-v-event:not(.fc-event-start) {
                    border-top-width: 0;
                    border-top-left-radius: 0;
                    border-top-right-radius: 0
                }

                .fc-v-event:not(.fc-event-end) {
                    border-bottom-width: 0;
                    border-bottom-left-radius: 0;
                    border-bottom-right-radius: 0
                }

                .fc-v-event.fc-event-selected:before {
                    left: -10px;
                    right: -10px
                }

                .fc-v-event .fc-event-resizer-start {
                    cursor: n-resize
                }

                .fc-v-event .fc-event-resizer-end {
                    cursor: s-resize
                }

                .fc-v-event:not(.fc-event-selected) .fc-event-resizer {
                    height: 8px;
                    height: var(--fc-event-resizer-thickness, 8px);
                    left: 0;
                    right: 0
                }

                .fc-v-event:not(.fc-event-selected) .fc-event-resizer-start {
                    top: -4px;
                    top: calc(var(--fc-event-resizer-thickness, 8px) / -2)
                }

                .fc-v-event:not(.fc-event-selected) .fc-event-resizer-end {
                    bottom: -4px;
                    bottom: calc(var(--fc-event-resizer-thickness, 8px) / -2)
                }

                .fc-v-event.fc-event-selected .fc-event-resizer {
                    left: 50%;
                    margin-left: -4px;
                    margin-left: calc(var(--fc-event-resizer-dot-total-width, 8px) / -2)
                }

                .fc-v-event.fc-event-selected .fc-event-resizer-start {
                    top: -4px;
                    top: calc(var(--fc-event-resizer-dot-total-width, 8px) / -2)
                }

                .fc-v-event.fc-event-selected .fc-event-resizer-end {
                    bottom: -4px;
                    bottom: calc(var(--fc-event-resizer-dot-total-width, 8px) / -2)
                }

                .fc .fc-timegrid .fc-daygrid-body {
                    z-index: 2
                }

                .fc .fc-timegrid-axis-chunk > table, .fc .fc-timegrid-body, .fc .fc-timegrid-slots {
                    position: relative;
                    z-index: 1
                }

                .fc .fc-timegrid-divider {
                    padding: 0 0 2px
                }

                .fc .fc-timegrid-body {
                    min-height: 100%
                }

                .fc .fc-timegrid-axis-chunk {
                    position: relative
                }

                .fc .fc-timegrid-slot {
                    height: 1.5em;
                    border-bottom: 0
                }

                .fc .fc-timegrid-slot:empty:before {
                    content: '\\00a0'
                }

                .fc .fc-timegrid-slot-minor {
                    border-top-style: dotted
                }

                .fc .fc-timegrid-slot-label-cushion {
                    display: inline-block;
                    white-space: nowrap
                }

                .fc .fc-timegrid-axis-cushion, .fc .fc-timegrid-slot-label-cushion {
                    padding: 0 4px
                }

                .fc .fc-timegrid-axis-frame-liquid {
                    height: 100%
                }

                .fc .fc-timegrid-axis-frame {
                    overflow: hidden;
                    display: flex;
                    align-items: center;
                    justify-content: flex-end
                }

                .fc .fc-timegrid-axis-cushion {
                    max-width: 60px;
                    flex-shrink: 0
                }

                .fc-direction-ltr .fc-timegrid-slot-label-frame {
                    text-align: right
                }

                .fc-direction-rtl .fc-timegrid-slot-label-frame {
                    text-align: left
                }

                .fc-liquid-hack .fc-timegrid-axis-frame-liquid {
                    height: auto;
                    position: absolute;
                    top: 0;
                    right: 0;
                    bottom: 0;
                    left: 0
                }

                .fc .fc-timegrid-col.fc-day-today {
                    background-color: rgba(255, 220, 40, .15);
                    background-color: var(--fc-today-bg-color, rgba(255, 220, 40, .15))
                }

                .fc .fc-timegrid-col-frame {
                    min-height: 100%;
                    position: relative
                }

                .fc-liquid-hack .fc-timegrid-col-frame {
                    height: auto;
                    position: absolute;
                    top: 0;
                    right: 0;
                    bottom: 0;
                    left: 0
                }

                .fc-media-screen .fc-timegrid-cols {
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0
                }

                .fc-media-screen .fc-timegrid-cols > table {
                    height: 100%
                }

                .fc-media-screen .fc-timegrid-col-bg, .fc-media-screen .fc-timegrid-col-events, .fc-media-screen .fc-timegrid-now-indicator-container {
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0
                }

                .fc .fc-timegrid-col-bg {
                    z-index: 2
                }

                .fc .fc-timegrid-col-bg .fc-non-business {
                    z-index: 1
                }

                .fc .fc-timegrid-col-bg .fc-bg-event {
                    z-index: 2
                }

                .fc .fc-timegrid-col-bg .fc-highlight, .fc .fc-timegrid-col-events {
                    z-index: 3
                }

                .fc .fc-timegrid-bg-harness {
                    position: absolute;
                    left: 0;
                    right: 0
                }

                .fc .fc-timegrid-now-indicator-container {
                    bottom: 0;
                    overflow: hidden
                }

                .fc-direction-ltr .fc-timegrid-col-events {
                    margin: 0 2.5% 0 2px
                }

                .fc-direction-rtl .fc-timegrid-col-events {
                    margin: 0 2px 0 2.5%
                }

                .fc-timegrid-event-harness {
                    position: absolute
                }

                .fc-timegrid-event-harness > .fc-timegrid-event {
                    position: absolute;
                    top: 0;
                    bottom: 0;
                    left: 0;
                    right: 0
                }

                .fc-timegrid-event-harness-inset .fc-timegrid-event, .fc-timegrid-event.fc-event-mirror, .fc-timegrid-more-link {
                    box-shadow: 0 0 0 1px #fff;
                    box-shadow: 0 0 0 1px var(--fc-page-bg-color, #fff)
                }

                .fc-timegrid-event, .fc-timegrid-more-link {
                    font-size: .85em;
                    font-size: var(--fc-small-font-size, .85em);
                    border-radius: 3px
                }

                .fc-timegrid-event {
                    margin-bottom: 1px
                }

                .fc-timegrid-event .fc-event-main {
                    padding: 1px 1px 0
                }

                .fc-timegrid-event .fc-event-time {
                    white-space: nowrap;
                    font-size: .85em;
                    font-size: var(--fc-small-font-size, .85em);
                    margin-bottom: 1px
                }

                .fc-timegrid-event-short .fc-event-main-frame {
                    flex-direction: row;
                    overflow: hidden
                }

                .fc-timegrid-event-short .fc-event-time:after {
                    content: '\\00a0-\\00a0'
                }

                .fc-timegrid-event-short .fc-event-title {
                    font-size: .85em;
                    font-size: var(--fc-small-font-size, .85em)
                }

                .fc-timegrid-more-link {
                    position: absolute;
                    z-index: 9999;
                    color: inherit;
                    color: var(--fc-more-link-text-color, inherit);
                    background: var(--fc-more-link-bg-color, #d0d0d0);
                    cursor: pointer;
                    margin-bottom: 1px
                }

                .fc-timegrid-more-link-inner {
                    padding: 3px 2px;
                    top: 0
                }

                .fc-direction-ltr .fc-timegrid-more-link {
                    right: 0
                }

                .fc-direction-rtl .fc-timegrid-more-link {
                    left: 0
                }

                .fc .fc-timegrid-now-indicator-line {
                    position: absolute;
                    z-index: 4;
                    left: 0;
                    right: 0;
                    border-style: solid;
                    border-color: red;
                    border-color: var(--fc-now-indicator-color, red);
                    border-width: 1px 0 0
                }

                .fc .fc-timegrid-now-indicator-arrow {
                    position: absolute;
                    z-index: 4;
                    margin-top: -5px;
                    border-style: solid;
                    border-color: red;
                    border-color: var(--fc-now-indicator-color, red)
                }

                .fc-direction-ltr .fc-timegrid-now-indicator-arrow {
                    left: 0;
                    border-width: 5px 0 5px 6px;
                    border-top-color: transparent;
                    border-bottom-color: transparent
                }

                .fc-direction-rtl .fc-timegrid-now-indicator-arrow {
                    right: 0;
                    border-width: 5px 6px 5px 0;
                    border-top-color: transparent;
                    border-bottom-color: transparent
                }

                .fc-theme-standard .fc-list {
                    border: 1px solid #ddd;
                    border: 1px solid var(--fc-border-color, #ddd)
                }

                .fc .fc-list-empty {
                    background-color: rgba(208, 208, 208, .3);
                    background-color: var(--fc-neutral-bg-color, rgba(208, 208, 208, .3));
                    height: 100%;
                    display: flex;
                    justify-content: center;
                    align-items: center
                }

                .fc .fc-list-empty-cushion {
                    margin: 5em 0
                }

                .fc .fc-list-table {
                    width: 100%;
                    border-style: hidden
                }

                .fc .fc-list-table tr > * {
                    border-left: 0;
                    border-right: 0
                }

                .fc .fc-list-sticky .fc-list-day > * {
                    position: sticky;
                    top: 0;
                    background: var(--fc-page-bg-color, #fff)
                }

                .fc .fc-list-table th {
                    padding: 0
                }

                .fc .fc-list-day-cushion, .fc .fc-list-table td {
                    padding: 8px 14px
                }

                .fc .fc-list-day-cushion:after {
                    content: "";
                    clear: both;
                    display: table
                }

                .fc-theme-standard .fc-list-day-cushion {
                    background-color: rgba(208, 208, 208, .3);
                    background-color: var(--fc-neutral-bg-color, rgba(208, 208, 208, .3))
                }

                .fc-direction-ltr .fc-list-day-text, .fc-direction-rtl .fc-list-day-side-text {
                    float: left
                }

                .fc-direction-ltr .fc-list-day-side-text, .fc-direction-rtl .fc-list-day-text {
                    float: right
                }

                .fc-direction-ltr .fc-list-table .fc-list-event-graphic {
                    padding-right: 0
                }

                .fc-direction-rtl .fc-list-table .fc-list-event-graphic {
                    padding-left: 0
                }

                .fc .fc-list-event.fc-event-forced-url {
                    cursor: pointer
                }

                .fc .fc-list-event:hover td {
                    background-color: #f5f5f5;
                    background-color: var(--fc-list-event-hover-bg-color, #f5f5f5)
                }

                .fc .fc-list-event-graphic, .fc .fc-list-event-time {
                    white-space: nowrap;
                    width: 1px
                }

                .fc .fc-list-event-dot {
                    display: inline-block;
                    box-sizing: content-box;
                    width: 0;
                    height: 0;
                    border: 5px solid #3788d8;
                    border: calc(var(--fc-list-event-dot-width, 10px) / 2) solid var(--fc-event-border-color, #3788d8);
                    border-radius: 5px;
                    border-radius: calc(var(--fc-list-event-dot-width, 10px) / 2)
                }

                .fc .fc-list-event-title a {
                    color: inherit
                }

                .fc .fc-list-event.fc-event-forced-url:hover a {
                    text-decoration: underline
                }

                .fc-theme-bootstrap a:not([href]) {
                    color: inherit
                }
            </style>
        `;
    }

    static get templateElementCss() {
        return html`
            <style>
                #calendar-container, #calendar {
                    height: 100%;
                    display: flex;
                    flex-direction: column;
                    flex-grow: 1;
                }
            </style>
        `;
    }
}

customElements.define("full-calendar", FullCalendar);