import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {afterNextRender} from '@polymer/polymer/lib/utils/render-status.js';

import {Calendar} from '@fullcalendar/core';
import interaction from '@fullcalendar/interaction';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import listPlugin from '@fullcalendar/list';
import {toMoment} from '@fullcalendar/moment'; // only for formatting
import momentTimezonePlugin from '@fullcalendar/moment-timezone';
import allLocales from '@fullcalendar/core/locales-all.min';

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

export class FullCalendar extends PolymerElement {
    static get template() {
        return html`
            ${this.templateCalendarCss}
            ${this.templateElementCss}
            
            ${this.templateContainer}
        `;
    }

    static get templateCalendarCss() {
        return html`
            <style>.fc .fc-axis { padding: var(--fc_fc-axis-padding, 0 4px); }.fc .fc-axis { vertical-align: var(--fc_fc-axis-vertical-align, middle); }.fc .fc-axis { white-space: var(--fc_fc-axis-white-space, nowrap); }.fc .fc-list-table { table-layout: var(--fc_fc-list-table-table-layout, auto); }.fc .fc-row .fc-content-skeleton table { background: var(--fc_fc-row_fc-content-skeleton_table-background, none); }.fc .fc-row .fc-content-skeleton table { border-color: var(--fc_fc-row_fc-content-skeleton_table-border-color, transparent); }.fc .fc-row .fc-content-skeleton td { background: var(--fc_fc-row_fc-content-skeleton_td-background, none); }.fc .fc-row .fc-content-skeleton td { border-color: var(--fc_fc-row_fc-content-skeleton_td-border-color, transparent); }.fc .fc-row .fc-mirror-skeleton td { background: var(--fc_fc-row_fc-mirror-skeleton_td-background, none); }.fc .fc-row .fc-mirror-skeleton td { border-color: var(--fc_fc-row_fc-mirror-skeleton_td-border-color, transparent); }.fc .fc-row { border-style: var(--fc_fc-row-border-style, solid); }.fc .fc-row { border-width: var(--fc_fc-row-border-width, 0); }.fc table { border-collapse: var(--fc_table-border-collapse, collapse); }.fc table { border-spacing: var(--fc_table-border-spacing, 0); }.fc table { box-sizing: var(--fc_table-box-sizing, border-box); }.fc table { font-size: var(--fc_table-font-size, 1em); }.fc table { table-layout: var(--fc_table-table-layout, fixed); }.fc table { width: var(--fc_table-width, 100%); }.fc td { border-style: var(--fc_td-border-style, solid); }.fc td { border-width: var(--fc_td-border-width, 1px); }.fc td { padding: var(--fc_td-padding, 0); }.fc td { vertical-align: var(--fc_td-vertical-align, top); }.fc td.fc-today { border-style: var(--fc_tdfc-today-border-style, double); }.fc th { border-style: var(--fc_th-border-style, solid); }.fc th { border-width: var(--fc_th-border-width, 1px); }.fc th { padding: var(--fc_th-padding, 0); }.fc th { text-align: var(--fc_th-text-align, center); }.fc th { vertical-align: var(--fc_th-vertical-align, top); }.fc { direction: var(--fc-direction, ltr); }.fc { text-align: var(--fc-text-align, left); }.fc-bg table { height: var(--fc-bg_table-height, 100%); }.fc-bg { bottom: var(--fc-bg-bottom, 0); }.fc-bg { left: var(--fc-bg-left, 0); }.fc-bg { position: var(--fc-bg-position, absolute); }.fc-bg { right: var(--fc-bg-right, 0); }.fc-bg { top: var(--fc-bg-top, 0); }.fc-bgevent { background: var(--fc-bgevent-background, rgb(143, 223, 130)); }.fc-bgevent { opacity: var(--fc-bgevent-opacity, 0.3); }.fc-bgevent-skeleton { left: var(--fc-bgevent-skeleton-left, 0); }.fc-bgevent-skeleton { position: var(--fc-bgevent-skeleton-position, absolute); }.fc-bgevent-skeleton { right: var(--fc-bgevent-skeleton-right, 0); }.fc-bgevent-skeleton { top: var(--fc-bgevent-skeleton-top, 0); }.fc-button .fc-icon { font-size: var(--fc-button_fc-icon-font-size, 1.5em); }.fc-button .fc-icon { vertical-align: var(--fc-button_fc-icon-vertical-align, middle); }.fc-button { -moz-user-select: var(--fc-button--moz-user-select, none); }.fc-button { -ms-user-select: var(--fc-button--ms-user-select, none); }.fc-button { -webkit-appearance: var(--fc-button--webkit-appearance, button); }.fc-button { -webkit-user-select: var(--fc-button--webkit-user-select, none); }.fc-button { background-color: var(--fc-button-background-color, transparent); }.fc-button { border-radius: var(--fc-button-border-radius, 0); }.fc-button { border: var(--fc-button-border, 1px solid transparent); }.fc-button { color: var(--fc-button-color, rgb(33, 37, 41)); }.fc-button { display: var(--fc-button-display, inline-block); }.fc-button { font-family: var(--fc-button-font-family, inherit); }.fc-button { font-size: var(--fc-button-font-size, inherit); }.fc-button { font-weight: var(--fc-button-font-weight, 400); }.fc-button { line-height: var(--fc-button-line-height, inherit); }.fc-button { margin: var(--fc-button-margin, 0); }.fc-button { overflow: var(--fc-button-overflow, visible); }.fc-button { padding: var(--fc-button-padding, 0.4em 0.65em); }.fc-button { text-align: var(--fc-button-text-align, center); }.fc-button { text-transform: var(--fc-button-text-transform, none); }.fc-button { user-select: var(--fc-button-user-select, none); }.fc-button { vertical-align: var(--fc-button-vertical-align, middle); }.fc-button-group > .fc-button { -ms-flex: var(--fc-button-group__LACE_BRACE__fc-button--ms-flex, 1 1 auto); }.fc-button-group > .fc-button { -webkit-box-flex: var(--fc-button-group__LACE_BRACE__fc-button--webkit-box-flex, 1); }.fc-button-group > .fc-button { flex: var(--fc-button-group__LACE_BRACE__fc-button-flex, 1 1 auto); }.fc-button-group > .fc-button { position: var(--fc-button-group__LACE_BRACE__fc-button-position, relative); }.fc-button-group > .fc-button.fc-button-active { z-index: var(--fc-button-group__LACE_BRACE__fc-buttonfc-button-active-z-index, 1); }.fc-button-group > .fc-button:active { z-index: var(--fc-button-group__LACE_BRACE__fc-button_COLON_active-z-index, 1); }.fc-button-group > .fc-button:focus { z-index: var(--fc-button-group__LACE_BRACE__fc-button_COLON_focus-z-index, 1); }.fc-button-group > .fc-button:hover { z-index: var(--fc-button-group__LACE_BRACE__fc-button_COLON_hover-z-index, 1); }.fc-button-group > .fc-button:not(:first-child) { border-bottom-left-radius: var(--fc-button-group__LACE_BRACE__fc-button_COLON_not_R_BRACKET_OPEN__COLON_first-child_R_BRACKET_CLOSE_-border-bottom-left-radius, 0); }.fc-button-group > .fc-button:not(:first-child) { border-top-left-radius: var(--fc-button-group__LACE_BRACE__fc-button_COLON_not_R_BRACKET_OPEN__COLON_first-child_R_BRACKET_CLOSE_-border-top-left-radius, 0); }.fc-button-group > .fc-button:not(:first-child) { margin-left: var(--fc-button-group__LACE_BRACE__fc-button_COLON_not_R_BRACKET_OPEN__COLON_first-child_R_BRACKET_CLOSE_-margin-left, -1px); }.fc-button-group > .fc-button:not(:last-child) { border-bottom-right-radius: var(--fc-button-group__LACE_BRACE__fc-button_COLON_not_R_BRACKET_OPEN__COLON_last-child_R_BRACKET_CLOSE_-border-bottom-right-radius, 0); }.fc-button-group > .fc-button:not(:last-child) { border-top-right-radius: var(--fc-button-group__LACE_BRACE__fc-button_COLON_not_R_BRACKET_OPEN__COLON_last-child_R_BRACKET_CLOSE_-border-top-right-radius, 0); }.fc-button-group { display: var(--fc-button-group-display, inline-flex); }.fc-button-group { display: var(--fc-button-group-display--ms-inline-flexbox, -ms-inline-flexbox); }.fc-button-group { display: var(--fc-button-group-display--webkit-inline-box, -webkit-inline-box); }.fc-button-group { position: var(--fc-button-group-position, relative); }.fc-button-group { vertical-align: var(--fc-button-group-vertical-align, middle); }.fc-button-primary { background-color: var(--fc-button-primary-background-color, rgb(44, 62, 80)); }.fc-button-primary { border-color: var(--fc-button-primary-border-color, rgb(44, 62, 80)); }.fc-button-primary { color: var(--fc-button-primary-color, rgb(255, 255, 255)); }.fc-button-primary:disabled { background-color: var(--fc-button-primary_COLON_disabled-background-color, rgb(44, 62, 80)); }.fc-button-primary:disabled { border-color: var(--fc-button-primary_COLON_disabled-border-color, rgb(44, 62, 80)); }.fc-button-primary:disabled { color: var(--fc-button-primary_COLON_disabled-color, rgb(255, 255, 255)); }.fc-button-primary:focus { -webkit-box-shadow: var(--fc-button-primary_COLON_focus--webkit-box-shadow, 0 0 0 0.2rem rgba(76, 91, 106, 0.5)); }.fc-button-primary:focus { box-shadow: var(--fc-button-primary_COLON_focus-box-shadow, 0 0 0 0.2rem rgba(76, 91, 106, 0.5)); }.fc-button-primary:hover { background-color: var(--fc-button-primary_COLON_hover-background-color, rgb(30, 43, 55)); }.fc-button-primary:hover { border-color: var(--fc-button-primary_COLON_hover-border-color, rgb(26, 37, 47)); }.fc-button-primary:hover { color: var(--fc-button-primary_COLON_hover-color, rgb(255, 255, 255)); }.fc-button-primary:not(:disabled).fc-button-active { background-color: var(--fc-button-primary_COLON_not_R_BRACKET_OPEN__COLON_disabled_R_BRACKET_CLOSE_fc-button-active-background-color, rgb(26, 37, 47)); }.fc-button-primary:not(:disabled).fc-button-active { border-color: var(--fc-button-primary_COLON_not_R_BRACKET_OPEN__COLON_disabled_R_BRACKET_CLOSE_fc-button-active-border-color, rgb(21, 30, 39)); }.fc-button-primary:not(:disabled).fc-button-active { color: var(--fc-button-primary_COLON_not_R_BRACKET_OPEN__COLON_disabled_R_BRACKET_CLOSE_fc-button-active-color, rgb(255, 255, 255)); }.fc-button-primary:not(:disabled).fc-button-active:focus { -webkit-box-shadow: var(--fc-button-primary_COLON_not_R_BRACKET_OPEN__COLON_disabled_R_BRACKET_CLOSE_fc-button-active_COLON_focus--webkit-box-shadow, 0 0 0 0.2rem rgba(76, 91, 106, 0.5)); }.fc-button-primary:not(:disabled).fc-button-active:focus { box-shadow: var(--fc-button-primary_COLON_not_R_BRACKET_OPEN__COLON_disabled_R_BRACKET_CLOSE_fc-button-active_COLON_focus-box-shadow, 0 0 0 0.2rem rgba(76, 91, 106, 0.5)); }.fc-button-primary:not(:disabled):active { background-color: var(--fc-button-primary_COLON_not_R_BRACKET_OPEN__COLON_disabled_R_BRACKET_CLOSE__COLON_active-background-color, rgb(26, 37, 47)); }.fc-button-primary:not(:disabled):active { border-color: var(--fc-button-primary_COLON_not_R_BRACKET_OPEN__COLON_disabled_R_BRACKET_CLOSE__COLON_active-border-color, rgb(21, 30, 39)); }.fc-button-primary:not(:disabled):active { color: var(--fc-button-primary_COLON_not_R_BRACKET_OPEN__COLON_disabled_R_BRACKET_CLOSE__COLON_active-color, rgb(255, 255, 255)); }.fc-button-primary:not(:disabled):active:focus { -webkit-box-shadow: var(--fc-button-primary_COLON_not_R_BRACKET_OPEN__COLON_disabled_R_BRACKET_CLOSE__COLON_active_COLON_focus--webkit-box-shadow, 0 0 0 0.2rem rgba(76, 91, 106, 0.5)); }.fc-button-primary:not(:disabled):active:focus { box-shadow: var(--fc-button-primary_COLON_not_R_BRACKET_OPEN__COLON_disabled_R_BRACKET_CLOSE__COLON_active_COLON_focus-box-shadow, 0 0 0 0.2rem rgba(76, 91, 106, 0.5)); }.fc-button::-moz-focus-inner { border-style: var(--fc-button_COLON__COLON_-moz-focus-inner-border-style, none); }.fc-button::-moz-focus-inner { padding: var(--fc-button_COLON__COLON_-moz-focus-inner-padding, 0); }.fc-button:disabled { opacity: var(--fc-button_COLON_disabled-opacity, 0.65); }.fc-button:focus { -webkit-box-shadow: var(--fc-button_COLON_focus--webkit-box-shadow, 0 0 0 0.2rem rgba(44, 62, 80, 0.25)); }.fc-button:focus { box-shadow: var(--fc-button_COLON_focus-box-shadow, 0 0 0 0.2rem rgba(44, 62, 80, 0.25)); }.fc-button:focus { outline: var(--fc-button_COLON_focus-outline, 1px dotted); }.fc-button:hover { color: var(--fc-button_COLON_hover-color, rgb(33, 37, 41)); }.fc-button:hover { text-decoration: var(--fc-button_COLON_hover-text-decoration, none); }.fc-button:not(:disabled) { cursor: var(--fc-button_COLON_not_R_BRACKET_OPEN__COLON_disabled_R_BRACKET_CLOSE_-cursor, pointer); }.fc-day-grid .fc-row { z-index: var(--fc-day-grid_fc-row-z-index, 1); }.fc-day-grid-event .fc-content { overflow: var(--fc-day-grid-event_fc-content-overflow, hidden); }.fc-day-grid-event .fc-content { white-space: var(--fc-day-grid-event_fc-content-white-space, nowrap); }.fc-day-grid-event .fc-time { font-weight: var(--fc-day-grid-event_fc-time-font-weight, bold); }.fc-day-grid-event { margin: var(--fc-day-grid-event-margin, 1px 2px 0); }.fc-day-grid-event { padding: var(--fc-day-grid-event-padding, 0 1px); }.fc-day-top.fc-other-month { opacity: var(--fc-day-topfc-other-month-opacity, 0.3); }.fc-dayGrid-view .fc-body .fc-row { min-height: var(--fc-dayGrid-view_fc-body_fc-row-min-height, 4em); }.fc-dayGrid-view .fc-day-number { padding: var(--fc-dayGrid-view_fc-day-number-padding, 2px); }.fc-dayGrid-view .fc-day-top .fc-week-number { background-color: var(--fc-dayGrid-view_fc-day-top_fc-week-number-background-color, rgb(242, 242, 242)); }.fc-dayGrid-view .fc-day-top .fc-week-number { color: var(--fc-dayGrid-view_fc-day-top_fc-week-number-color, rgb(128, 128, 128)); }.fc-dayGrid-view .fc-day-top .fc-week-number { min-width: var(--fc-dayGrid-view_fc-day-top_fc-week-number-min-width, 1.5em); }.fc-dayGrid-view .fc-day-top .fc-week-number { text-align: var(--fc-dayGrid-view_fc-day-top_fc-week-number-text-align, center); }.fc-dayGrid-view .fc-week-number { padding: var(--fc-dayGrid-view_fc-week-number-padding, 2px); }.fc-dayGrid-view td.fc-week-number > * { display: var(--fc-dayGrid-view_tdfc-week-number__LACE_BRACE___ASTERISK_-display, inline-block); }.fc-dayGrid-view td.fc-week-number > * { min-width: var(--fc-dayGrid-view_tdfc-week-number__LACE_BRACE___ASTERISK_-min-width, 1.25em); }.fc-dayGrid-view td.fc-week-number { text-align: var(--fc-dayGrid-view_tdfc-week-number-text-align, center); }.fc-dayGrid-view th.fc-day-number { padding: var(--fc-dayGrid-view_thfc-day-number-padding, 0 2px); }.fc-dayGrid-view th.fc-week-number { padding: var(--fc-dayGrid-view_thfc-week-number-padding, 0 2px); }.fc-dayGridDay-view .fc-content-skeleton { padding-bottom: var(--fc-dayGridDay-view_fc-content-skeleton-padding-bottom, 1em); }.fc-dayGridWeek-view .fc-content-skeleton { padding-bottom: var(--fc-dayGridWeek-view_fc-content-skeleton-padding-bottom, 1em); }.fc-divider { border-style: var(--fc-divider-border-style, solid); }.fc-divider { border-width: var(--fc-divider-border-width, 1px); }.fc-event .fc-content { position: var(--fc-event_fc-content-position, relative); }.fc-event .fc-content { z-index: var(--fc-event_fc-content-z-index, 2); }.fc-event .fc-resizer { display: var(--fc-event_fc-resizer-display, none); }.fc-event .fc-resizer { position: var(--fc-event_fc-resizer-position, absolute); }.fc-event .fc-resizer { z-index: var(--fc-event_fc-resizer-z-index, 4); }.fc-event { background-color: var(--fc-event-background-color, rgb(55, 136, 216)); }.fc-event { border-radius: var(--fc-event-border-radius, 3px); }.fc-event { border: var(--fc-event-border, 1px solid rgb(55, 136, 216)); }.fc-event { color: var(--fc-event-color, rgb(255, 255, 255)); }.fc-event { display: var(--fc-event-display, block); }.fc-event { font-size: var(--fc-event-font-size, 0.85em); }.fc-event { line-height: var(--fc-event-line-height, 1.4); }.fc-event { position: var(--fc-event-position, relative); }.fc-event { text-decoration: var(--fc-event-text-decoration, none); }.fc-event-dot { background-color: var(--fc-event-dot-background-color, rgb(55, 136, 216)); }.fc-event-dot { border-radius: var(--fc-event-dot-border-radius, 5px); }.fc-event-dot { display: var(--fc-event-dot-display, inline-block); }.fc-event-dot { height: var(--fc-event-dot-height, 10px); }.fc-event-dot { width: var(--fc-event-dot-width, 10px); }.fc-event.fc-allow-mouse-resize .fc-resizer { display: var(--fc-eventfc-allow-mouse-resize_fc-resizer-display, block); }.fc-event.fc-draggable { cursor: var(--fc-eventfc-draggable-cursor, pointer); }.fc-event.fc-dragging.fc-selected { box-shadow: var(--fc-eventfc-draggingfc-selected-box-shadow, 0 2px 7px rgba(0, 0, 0, 0.3)); }.fc-event.fc-dragging:not(.fc-selected) { opacity: var(--fc-eventfc-dragging_COLON_not_R_BRACKET_OPEN_fc-selected_R_BRACKET_CLOSE_-opacity, 0.75); }.fc-event.fc-selected .fc-resizer { display: var(--fc-eventfc-selected_fc-resizer-display, block); }.fc-event.fc-selected .fc-resizer:before { content: var(--fc-eventfc-selected_fc-resizer_COLON_before-content, ""); }.fc-event.fc-selected .fc-resizer:before { height: var(--fc-eventfc-selected_fc-resizer_COLON_before-height, 40px); }.fc-event.fc-selected .fc-resizer:before { left: var(--fc-eventfc-selected_fc-resizer_COLON_before-left, 50%); }.fc-event.fc-selected .fc-resizer:before { margin-left: var(--fc-eventfc-selected_fc-resizer_COLON_before-margin-left, -20px); }.fc-event.fc-selected .fc-resizer:before { margin-top: var(--fc-eventfc-selected_fc-resizer_COLON_before-margin-top, -20px); }.fc-event.fc-selected .fc-resizer:before { position: var(--fc-eventfc-selected_fc-resizer_COLON_before-position, absolute); }.fc-event.fc-selected .fc-resizer:before { top: var(--fc-eventfc-selected_fc-resizer_COLON_before-top, 50%); }.fc-event.fc-selected .fc-resizer:before { width: var(--fc-eventfc-selected_fc-resizer_COLON_before-width, 40px); }.fc-event.fc-selected .fc-resizer:before { z-index: var(--fc-eventfc-selected_fc-resizer_COLON_before-z-index, 9999); }.fc-event.fc-selected { box-shadow: var(--fc-eventfc-selected-box-shadow, 0 2px 5px rgba(0, 0, 0, 0.2)); }.fc-event.fc-selected { z-index: var(--fc-eventfc-selected-z-index, 9999); }.fc-event.fc-selected:after { background: var(--fc-eventfc-selected_COLON_after-background, rgb(0, 0, 0)); }.fc-event.fc-selected:after { bottom: var(--fc-eventfc-selected_COLON_after-bottom, -1px); }.fc-event.fc-selected:after { content: var(--fc-eventfc-selected_COLON_after-content, ""); }.fc-event.fc-selected:after { left: var(--fc-eventfc-selected_COLON_after-left, -1px); }.fc-event.fc-selected:after { opacity: var(--fc-eventfc-selected_COLON_after-opacity, 0.25); }.fc-event.fc-selected:after { position: var(--fc-eventfc-selected_COLON_after-position, absolute); }.fc-event.fc-selected:after { right: var(--fc-eventfc-selected_COLON_after-right, -1px); }.fc-event.fc-selected:after { top: var(--fc-eventfc-selected_COLON_after-top, -1px); }.fc-event.fc-selected:after { z-index: var(--fc-eventfc-selected_COLON_after-z-index, 1); }.fc-event:hover { color: var(--fc-event_COLON_hover-color, rgb(255, 255, 255)); }.fc-event:hover { text-decoration: var(--fc-event_COLON_hover-text-decoration, none); }.fc-event[href] { cursor: var(--fc-event_SQUARE_BRACKET_OPEN_href_SQUARE_BRACKET_CLOSE_-cursor, pointer); }.fc-h-event.fc-allow-mouse-resize .fc-resizer { bottom: var(--fc-h-eventfc-allow-mouse-resize_fc-resizer-bottom, -1px); }.fc-h-event.fc-allow-mouse-resize .fc-resizer { top: var(--fc-h-eventfc-allow-mouse-resize_fc-resizer-top, -1px); }.fc-h-event.fc-allow-mouse-resize .fc-resizer { width: var(--fc-h-eventfc-allow-mouse-resize_fc-resizer-width, 7px); }.fc-h-event.fc-selected .fc-resizer { background: var(--fc-h-eventfc-selected_fc-resizer-background, rgb(255, 255, 255)); }.fc-h-event.fc-selected .fc-resizer { border-color: var(--fc-h-eventfc-selected_fc-resizer-border-color, inherit); }.fc-h-event.fc-selected .fc-resizer { border-radius: var(--fc-h-eventfc-selected_fc-resizer-border-radius, 4px); }.fc-h-event.fc-selected .fc-resizer { border-style: var(--fc-h-eventfc-selected_fc-resizer-border-style, solid); }.fc-h-event.fc-selected .fc-resizer { border-width: var(--fc-h-eventfc-selected_fc-resizer-border-width, 1px); }.fc-h-event.fc-selected .fc-resizer { height: var(--fc-h-eventfc-selected_fc-resizer-height, 6px); }.fc-h-event.fc-selected .fc-resizer { margin-top: var(--fc-h-eventfc-selected_fc-resizer-margin-top, -4px); }.fc-h-event.fc-selected .fc-resizer { top: var(--fc-h-eventfc-selected_fc-resizer-top, 50%); }.fc-h-event.fc-selected .fc-resizer { width: var(--fc-h-eventfc-selected_fc-resizer-width, 6px); }.fc-h-event.fc-selected:before { bottom: var(--fc-h-eventfc-selected_COLON_before-bottom, -10px); }.fc-h-event.fc-selected:before { content: var(--fc-h-eventfc-selected_COLON_before-content, ""); }.fc-h-event.fc-selected:before { left: var(--fc-h-eventfc-selected_COLON_before-left, 0); }.fc-h-event.fc-selected:before { position: var(--fc-h-eventfc-selected_COLON_before-position, absolute); }.fc-h-event.fc-selected:before { right: var(--fc-h-eventfc-selected_COLON_before-right, 0); }.fc-h-event.fc-selected:before { top: var(--fc-h-eventfc-selected_COLON_before-top, -10px); }.fc-h-event.fc-selected:before { z-index: var(--fc-h-eventfc-selected_COLON_before-z-index, 3); }.fc-highlight { background: var(--fc-highlight-background, rgb(188, 232, 241)); }.fc-highlight { opacity: var(--fc-highlight-opacity, 0.3); }.fc-highlight-skeleton { left: var(--fc-highlight-skeleton-left, 0); }.fc-highlight-skeleton { position: var(--fc-highlight-skeleton-position, absolute); }.fc-highlight-skeleton { right: var(--fc-highlight-skeleton-right, 0); }.fc-highlight-skeleton { top: var(--fc-highlight-skeleton-top, 0); }.fc-icon { -moz-osx-font-smoothing: var(--fc-icon--moz-osx-font-smoothing, grayscale); }.fc-icon { -webkit-font-smoothing: var(--fc-icon--webkit-font-smoothing, antialiased); }.fc-icon { display: var(--fc-icon-display, inline-block); }.fc-icon { font-family: var(--fc-icon-font-family, "fcicons"); }.fc-icon { font-style: var(--fc-icon-font-style, normal); }.fc-icon { font-variant: var(--fc-icon-font-variant, normal); }.fc-icon { font-weight: var(--fc-icon-font-weight, normal); }.fc-icon { height: var(--fc-icon-height, 1em); }.fc-icon { line-height: var(--fc-icon-line-height, 1); }.fc-icon { speak: var(--fc-icon-speak, none); }.fc-icon { text-align: var(--fc-icon-text-align, center); }.fc-icon { text-transform: var(--fc-icon-text-transform, none); }.fc-icon { width: var(--fc-icon-width, 1em); }.fc-icon-chevron-left:before { content: var(--fc-icon-chevron-left_COLON_before-content, ""); }.fc-icon-chevron-right:before { content: var(--fc-icon-chevron-right_COLON_before-content, ""); }.fc-icon-chevrons-left:before { content: var(--fc-icon-chevrons-left_COLON_before-content, ""); }.fc-icon-chevrons-right:before { content: var(--fc-icon-chevrons-right_COLON_before-content, ""); }.fc-icon-minus-square:before { content: var(--fc-icon-minus-square_COLON_before-content, ""); }.fc-icon-plus-square:before { content: var(--fc-icon-plus-square_COLON_before-content, ""); }.fc-icon-x:before { content: var(--fc-icon-x_COLON_before-content, ""); }.fc-limited { display: var(--fc-limited-display, none); }.fc-list-empty { display: var(--fc-list-empty-display, table-cell); }.fc-list-empty { text-align: var(--fc-list-empty-text-align, center); }.fc-list-empty { vertical-align: var(--fc-list-empty-vertical-align, middle); }.fc-list-empty-wrap1 { display: var(--fc-list-empty-wrap1-display, table); }.fc-list-empty-wrap1 { height: var(--fc-list-empty-wrap1-height, 100%); }.fc-list-empty-wrap1 { width: var(--fc-list-empty-wrap1-width, 100%); }.fc-list-empty-wrap2 { bottom: var(--fc-list-empty-wrap2-bottom, 0); }.fc-list-empty-wrap2 { left: var(--fc-list-empty-wrap2-left, 0); }.fc-list-empty-wrap2 { position: var(--fc-list-empty-wrap2-position, absolute); }.fc-list-empty-wrap2 { right: var(--fc-list-empty-wrap2-right, 0); }.fc-list-empty-wrap2 { top: var(--fc-list-empty-wrap2-top, 0); }.fc-list-heading td { font-weight: var(--fc-list-heading_td-font-weight, bold); }.fc-list-heading { border-bottom-width: var(--fc-list-heading-border-bottom-width, 1px); }.fc-list-item-marker { white-space: var(--fc-list-item-marker-white-space, nowrap); }.fc-list-item-marker { width: var(--fc-list-item-marker-width, 1px); }.fc-list-item-time { white-space: var(--fc-list-item-time-white-space, nowrap); }.fc-list-item-time { width: var(--fc-list-item-time-width, 1px); }.fc-list-item-title a { color: var(--fc-list-item-title_a-color, inherit); }.fc-list-item-title a { text-decoration: var(--fc-list-item-title_a-text-decoration, none); }.fc-list-item-title a[href]:hover { text-decoration: var(--fc-list-item-title_a_SQUARE_BRACKET_OPEN_href_SQUARE_BRACKET_CLOSE__COLON_hover-text-decoration, underline); }.fc-list-item.fc-has-url { cursor: var(--fc-list-itemfc-has-url-cursor, pointer); }.fc-list-table td { border-width: var(--fc-list-table_td-border-width, 1px 0 0); }.fc-list-table td { padding: var(--fc-list-table_td-padding, 8px 14px); }.fc-list-table tr:first-child td { border-top-width: var(--fc-list-table_tr_COLON_first-child_td-border-top-width, 0); }.fc-list-view { border-style: var(--fc-list-view-border-style, solid); }.fc-list-view { border-width: var(--fc-list-view-border-width, 1px); }.fc-ltr .fc-axis { text-align: var(--fc-ltr_fc-axis-text-align, right); }.fc-ltr .fc-day-grid-event.fc-allow-mouse-resize .fc-end-resizer { margin-right: var(--fc-ltr_fc-day-grid-eventfc-allow-mouse-resize_fc-end-resizer-margin-right, -2px); }.fc-ltr .fc-day-grid-event.fc-allow-mouse-resize .fc-start-resizer { margin-left: var(--fc-ltr_fc-day-grid-eventfc-allow-mouse-resize_fc-start-resizer-margin-left, -2px); }.fc-ltr .fc-dayGrid-view .fc-day-top .fc-day-number { float: var(--fc-ltr_fc-dayGrid-view_fc-day-top_fc-day-number-float, right); }.fc-ltr .fc-dayGrid-view .fc-day-top .fc-week-number { border-radius: var(--fc-ltr_fc-dayGrid-view_fc-day-top_fc-week-number-border-radius, 0 0 3px 0); }.fc-ltr .fc-dayGrid-view .fc-day-top .fc-week-number { float: var(--fc-ltr_fc-dayGrid-view_fc-day-top_fc-week-number-float, left); }.fc-ltr .fc-h-event .fc-end-resizer { cursor: var(--fc-ltr_fc-h-event_fc-end-resizer-cursor, e-resize); }.fc-ltr .fc-h-event .fc-end-resizer { right: var(--fc-ltr_fc-h-event_fc-end-resizer-right, -1px); }.fc-ltr .fc-h-event .fc-start-resizer { cursor: var(--fc-ltr_fc-h-event_fc-start-resizer-cursor, w-resize); }.fc-ltr .fc-h-event .fc-start-resizer { left: var(--fc-ltr_fc-h-event_fc-start-resizer-left, -1px); }.fc-ltr .fc-h-event.fc-not-end { border-bottom-right-radius: var(--fc-ltr_fc-h-eventfc-not-end-border-bottom-right-radius, 0); }.fc-ltr .fc-h-event.fc-not-end { border-right-width: var(--fc-ltr_fc-h-eventfc-not-end-border-right-width, 0); }.fc-ltr .fc-h-event.fc-not-end { border-top-right-radius: var(--fc-ltr_fc-h-eventfc-not-end-border-top-right-radius, 0); }.fc-ltr .fc-h-event.fc-not-end { margin-right: var(--fc-ltr_fc-h-eventfc-not-end-margin-right, 0); }.fc-ltr .fc-h-event.fc-not-end { padding-right: var(--fc-ltr_fc-h-eventfc-not-end-padding-right, 1px); }.fc-ltr .fc-h-event.fc-not-start { border-bottom-left-radius: var(--fc-ltr_fc-h-eventfc-not-start-border-bottom-left-radius, 0); }.fc-ltr .fc-h-event.fc-not-start { border-left-width: var(--fc-ltr_fc-h-eventfc-not-start-border-left-width, 0); }.fc-ltr .fc-h-event.fc-not-start { border-top-left-radius: var(--fc-ltr_fc-h-eventfc-not-start-border-top-left-radius, 0); }.fc-ltr .fc-h-event.fc-not-start { margin-left: var(--fc-ltr_fc-h-eventfc-not-start-margin-left, 0); }.fc-ltr .fc-h-event.fc-not-start { padding-left: var(--fc-ltr_fc-h-eventfc-not-start-padding-left, 1px); }.fc-ltr .fc-h-event.fc-selected .fc-end-resizer { margin-right: var(--fc-ltr_fc-h-eventfc-selected_fc-end-resizer-margin-right, -4px); }.fc-ltr .fc-h-event.fc-selected .fc-start-resizer { margin-left: var(--fc-ltr_fc-h-eventfc-selected_fc-start-resizer-margin-left, -4px); }.fc-ltr .fc-list-heading-alt { float: var(--fc-ltr_fc-list-heading-alt-float, right); }.fc-ltr .fc-list-heading-main { float: var(--fc-ltr_fc-list-heading-main-float, left); }.fc-ltr .fc-list-item-marker { padding-right: var(--fc-ltr_fc-list-item-marker-padding-right, 0); }.fc-ltr .fc-time-grid .fc-event-container { margin: var(--fc-ltr_fc-time-grid_fc-event-container-margin, 0 2.5% 0 2px); }.fc-ltr .fc-time-grid .fc-now-indicator-arrow { border-bottom-color: var(--fc-ltr_fc-time-grid_fc-now-indicator-arrow-border-bottom-color, transparent); }.fc-ltr .fc-time-grid .fc-now-indicator-arrow { border-top-color: var(--fc-ltr_fc-time-grid_fc-now-indicator-arrow-border-top-color, transparent); }.fc-ltr .fc-time-grid .fc-now-indicator-arrow { border-width: var(--fc-ltr_fc-time-grid_fc-now-indicator-arrow-border-width, 5px 0 5px 6px); }.fc-ltr .fc-time-grid .fc-now-indicator-arrow { left: var(--fc-ltr_fc-time-grid_fc-now-indicator-arrow-left, 0); }.fc-mirror-skeleton tr:first-child > td > .fc-day-grid-event { margin-top: var(--fc-mirror-skeleton_tr_COLON_first-child__LACE_BRACE__td__LACE_BRACE__fc-day-grid-event-margin-top, 0); }.fc-mirror-skeleton { left: var(--fc-mirror-skeleton-left, 0); }.fc-mirror-skeleton { position: var(--fc-mirror-skeleton-position, absolute); }.fc-mirror-skeleton { right: var(--fc-mirror-skeleton-right, 0); }.fc-mirror-skeleton { top: var(--fc-mirror-skeleton-top, 0); }.fc-more-popover .fc-event-container { padding: var(--fc-more-popover_fc-event-container-padding, 10px); }.fc-more-popover { width: var(--fc-more-popover-width, 220px); }.fc-more-popover { z-index: var(--fc-more-popover-z-index, 2); }.fc-nonbusiness { background: var(--fc-nonbusiness-background, rgb(215, 215, 215)); }.fc-not-allowed .fc-event { cursor: var(--fc-not-allowed_fc-event-cursor, not-allowed); }.fc-not-allowed { cursor: var(--fc-not-allowed-cursor, not-allowed); }.fc-now-indicator { border: var(--fc-now-indicator-border, 0 solid red); }.fc-now-indicator { position: var(--fc-now-indicator-position, absolute); }.fc-popover .fc-header .fc-close { cursor: var(--fc-popover_fc-header_fc-close-cursor, pointer); }.fc-popover .fc-header .fc-close { font-size: var(--fc-popover_fc-header_fc-close-font-size, 1.1em); }.fc-popover .fc-header .fc-close { opacity: var(--fc-popover_fc-header_fc-close-opacity, 0.65); }.fc-popover .fc-header .fc-title { margin: var(--fc-popover_fc-header_fc-title-margin, 0 2px); }.fc-popover .fc-header { align-items: var(--fc-popover_fc-header-align-items, center); }.fc-popover .fc-header { display: var(--fc-popover_fc-header-display, flex); }.fc-popover .fc-header { flex-direction: var(--fc-popover_fc-header-flex-direction, row); }.fc-popover .fc-header { justify-content: var(--fc-popover_fc-header-justify-content, space-between); }.fc-popover .fc-header { padding: var(--fc-popover_fc-header-padding, 2px 4px); }.fc-popover { box-shadow: var(--fc-popover-box-shadow, 0 2px 6px rgba(0, 0, 0, 0.15)); }.fc-popover { position: var(--fc-popover-position, absolute); }.fc-row .fc-bg { z-index: var(--fc-row_fc-bg-z-index, 1); }.fc-row .fc-bgevent-skeleton table { height: var(--fc-row_fc-bgevent-skeleton_table-height, 100%); }.fc-row .fc-bgevent-skeleton td { border-color: var(--fc-row_fc-bgevent-skeleton_td-border-color, transparent); }.fc-row .fc-bgevent-skeleton { bottom: var(--fc-row_fc-bgevent-skeleton-bottom, 0); }.fc-row .fc-bgevent-skeleton { z-index: var(--fc-row_fc-bgevent-skeleton-z-index, 2); }.fc-row .fc-content-skeleton tbody td { border-top: var(--fc-row_fc-content-skeleton_tbody_td-border-top, 0); }.fc-row .fc-content-skeleton td { border-bottom: var(--fc-row_fc-content-skeleton_td-border-bottom, 0); }.fc-row .fc-content-skeleton { padding-bottom: var(--fc-row_fc-content-skeleton-padding-bottom, 2px); }.fc-row .fc-content-skeleton { position: var(--fc-row_fc-content-skeleton-position, relative); }.fc-row .fc-content-skeleton { z-index: var(--fc-row_fc-content-skeleton-z-index, 4); }.fc-row .fc-highlight-skeleton table { height: var(--fc-row_fc-highlight-skeleton_table-height, 100%); }.fc-row .fc-highlight-skeleton td { border-color: var(--fc-row_fc-highlight-skeleton_td-border-color, transparent); }.fc-row .fc-highlight-skeleton { bottom: var(--fc-row_fc-highlight-skeleton-bottom, 0); }.fc-row .fc-highlight-skeleton { z-index: var(--fc-row_fc-highlight-skeleton-z-index, 3); }.fc-row .fc-mirror-skeleton tbody td { border-top: var(--fc-row_fc-mirror-skeleton_tbody_td-border-top, 0); }.fc-row .fc-mirror-skeleton td { border-bottom: var(--fc-row_fc-mirror-skeleton_td-border-bottom, 0); }.fc-row .fc-mirror-skeleton { z-index: var(--fc-row_fc-mirror-skeleton-z-index, 5); }.fc-row table { border-bottom: var(--fc-row_table-border-bottom, 0 hidden transparent); }.fc-row table { border-left: var(--fc-row_table-border-left, 0 hidden transparent); }.fc-row table { border-right: var(--fc-row_table-border-right, 0 hidden transparent); }.fc-row { position: var(--fc-row-position, relative); }.fc-row.fc-rigid .fc-content-skeleton { left: var(--fc-rowfc-rigid_fc-content-skeleton-left, 0); }.fc-row.fc-rigid .fc-content-skeleton { position: var(--fc-rowfc-rigid_fc-content-skeleton-position, absolute); }.fc-row.fc-rigid .fc-content-skeleton { right: var(--fc-rowfc-rigid_fc-content-skeleton-right, 0); }.fc-row.fc-rigid .fc-content-skeleton { top: var(--fc-rowfc-rigid_fc-content-skeleton-top, 0); }.fc-row.fc-rigid { overflow: var(--fc-rowfc-rigid-overflow, hidden); }.fc-row:first-child table { border-top: var(--fc-row_COLON_first-child_table-border-top, 0 hidden transparent); }.fc-rtl .fc-axis { text-align: var(--fc-rtl_fc-axis-text-align, left); }.fc-rtl .fc-day-grid-event.fc-allow-mouse-resize .fc-end-resizer { margin-left: var(--fc-rtl_fc-day-grid-eventfc-allow-mouse-resize_fc-end-resizer-margin-left, -2px); }.fc-rtl .fc-day-grid-event.fc-allow-mouse-resize .fc-start-resizer { margin-right: var(--fc-rtl_fc-day-grid-eventfc-allow-mouse-resize_fc-start-resizer-margin-right, -2px); }.fc-rtl .fc-dayGrid-view .fc-day-top .fc-day-number { float: var(--fc-rtl_fc-dayGrid-view_fc-day-top_fc-day-number-float, left); }.fc-rtl .fc-dayGrid-view .fc-day-top .fc-week-number { border-radius: var(--fc-rtl_fc-dayGrid-view_fc-day-top_fc-week-number-border-radius, 0 0 0 3px); }.fc-rtl .fc-dayGrid-view .fc-day-top .fc-week-number { float: var(--fc-rtl_fc-dayGrid-view_fc-day-top_fc-week-number-float, right); }.fc-rtl .fc-h-event .fc-end-resizer { cursor: var(--fc-rtl_fc-h-event_fc-end-resizer-cursor, w-resize); }.fc-rtl .fc-h-event .fc-end-resizer { left: var(--fc-rtl_fc-h-event_fc-end-resizer-left, -1px); }.fc-rtl .fc-h-event .fc-start-resizer { cursor: var(--fc-rtl_fc-h-event_fc-start-resizer-cursor, e-resize); }.fc-rtl .fc-h-event .fc-start-resizer { right: var(--fc-rtl_fc-h-event_fc-start-resizer-right, -1px); }.fc-rtl .fc-h-event.fc-not-end { border-bottom-left-radius: var(--fc-rtl_fc-h-eventfc-not-end-border-bottom-left-radius, 0); }.fc-rtl .fc-h-event.fc-not-end { border-left-width: var(--fc-rtl_fc-h-eventfc-not-end-border-left-width, 0); }.fc-rtl .fc-h-event.fc-not-end { border-top-left-radius: var(--fc-rtl_fc-h-eventfc-not-end-border-top-left-radius, 0); }.fc-rtl .fc-h-event.fc-not-end { margin-left: var(--fc-rtl_fc-h-eventfc-not-end-margin-left, 0); }.fc-rtl .fc-h-event.fc-not-end { padding-left: var(--fc-rtl_fc-h-eventfc-not-end-padding-left, 1px); }.fc-rtl .fc-h-event.fc-not-start { border-bottom-right-radius: var(--fc-rtl_fc-h-eventfc-not-start-border-bottom-right-radius, 0); }.fc-rtl .fc-h-event.fc-not-start { border-right-width: var(--fc-rtl_fc-h-eventfc-not-start-border-right-width, 0); }.fc-rtl .fc-h-event.fc-not-start { border-top-right-radius: var(--fc-rtl_fc-h-eventfc-not-start-border-top-right-radius, 0); }.fc-rtl .fc-h-event.fc-not-start { margin-right: var(--fc-rtl_fc-h-eventfc-not-start-margin-right, 0); }.fc-rtl .fc-h-event.fc-not-start { padding-right: var(--fc-rtl_fc-h-eventfc-not-start-padding-right, 1px); }.fc-rtl .fc-h-event.fc-selected .fc-end-resizer { margin-left: var(--fc-rtl_fc-h-eventfc-selected_fc-end-resizer-margin-left, -4px); }.fc-rtl .fc-h-event.fc-selected .fc-start-resizer { margin-right: var(--fc-rtl_fc-h-eventfc-selected_fc-start-resizer-margin-right, -4px); }.fc-rtl .fc-list-heading-alt { float: var(--fc-rtl_fc-list-heading-alt-float, left); }.fc-rtl .fc-list-heading-main { float: var(--fc-rtl_fc-list-heading-main-float, right); }.fc-rtl .fc-list-item-marker { padding-left: var(--fc-rtl_fc-list-item-marker-padding-left, 0); }.fc-rtl .fc-list-view { direction: var(--fc-rtl_fc-list-view-direction, rtl); }.fc-rtl .fc-popover .fc-header { flex-direction: var(--fc-rtl_fc-popover_fc-header-flex-direction, row-reverse); }.fc-rtl .fc-time-grid .fc-event-container { margin: var(--fc-rtl_fc-time-grid_fc-event-container-margin, 0 2px 0 2.5%); }.fc-rtl .fc-time-grid .fc-now-indicator-arrow { border-bottom-color: var(--fc-rtl_fc-time-grid_fc-now-indicator-arrow-border-bottom-color, transparent); }.fc-rtl .fc-time-grid .fc-now-indicator-arrow { border-top-color: var(--fc-rtl_fc-time-grid_fc-now-indicator-arrow-border-top-color, transparent); }.fc-rtl .fc-time-grid .fc-now-indicator-arrow { border-width: var(--fc-rtl_fc-time-grid_fc-now-indicator-arrow-border-width, 5px 6px 5px 0); }.fc-rtl .fc-time-grid .fc-now-indicator-arrow { right: var(--fc-rtl_fc-time-grid_fc-now-indicator-arrow-right, 0); }.fc-rtl { text-align: var(--fc-rtl-text-align, right); }.fc-scroller > .fc-day-grid { position: var(--fc-scroller__LACE_BRACE__fc-day-grid-position, relative); }.fc-scroller > .fc-day-grid { width: var(--fc-scroller__LACE_BRACE__fc-day-grid-width, 100%); }.fc-scroller > .fc-time-grid { position: var(--fc-scroller__LACE_BRACE__fc-time-grid-position, relative); }.fc-scroller > .fc-time-grid { width: var(--fc-scroller__LACE_BRACE__fc-time-grid-width, 100%); }.fc-scroller { -webkit-overflow-scrolling: var(--fc-scroller--webkit-overflow-scrolling, touch); }.fc-time-grid .fc-bgevent { left: var(--fc-time-grid_fc-bgevent-left, 0); }.fc-time-grid .fc-bgevent { position: var(--fc-time-grid_fc-bgevent-position, absolute); }.fc-time-grid .fc-bgevent { right: var(--fc-time-grid_fc-bgevent-right, 0); }.fc-time-grid .fc-bgevent { z-index: var(--fc-time-grid_fc-bgevent-z-index, 1); }.fc-time-grid .fc-bgevent-container { position: var(--fc-time-grid_fc-bgevent-container-position, relative); }.fc-time-grid .fc-bgevent-container { z-index: var(--fc-time-grid_fc-bgevent-container-z-index, 2); }.fc-time-grid .fc-business-container { position: var(--fc-time-grid_fc-business-container-position, relative); }.fc-time-grid .fc-business-container { z-index: var(--fc-time-grid_fc-business-container-z-index, 1); }.fc-time-grid .fc-content-col { position: var(--fc-time-grid_fc-content-col-position, relative); }.fc-time-grid .fc-content-skeleton { left: var(--fc-time-grid_fc-content-skeleton-left, 0); }.fc-time-grid .fc-content-skeleton { position: var(--fc-time-grid_fc-content-skeleton-position, absolute); }.fc-time-grid .fc-content-skeleton { right: var(--fc-time-grid_fc-content-skeleton-right, 0); }.fc-time-grid .fc-content-skeleton { top: var(--fc-time-grid_fc-content-skeleton-top, 0); }.fc-time-grid .fc-content-skeleton { z-index: var(--fc-time-grid_fc-content-skeleton-z-index, 3); }.fc-time-grid .fc-event { position: var(--fc-time-grid_fc-event-position, absolute); }.fc-time-grid .fc-event { z-index: var(--fc-time-grid_fc-event-z-index, 1); }.fc-time-grid .fc-event-container { position: var(--fc-time-grid_fc-event-container-position, relative); }.fc-time-grid .fc-event-container { z-index: var(--fc-time-grid_fc-event-container-z-index, 4); }.fc-time-grid .fc-highlight { left: var(--fc-time-grid_fc-highlight-left, 0); }.fc-time-grid .fc-highlight { position: var(--fc-time-grid_fc-highlight-position, absolute); }.fc-time-grid .fc-highlight { right: var(--fc-time-grid_fc-highlight-right, 0); }.fc-time-grid .fc-highlight-container { position: var(--fc-time-grid_fc-highlight-container-position, relative); }.fc-time-grid .fc-highlight-container { z-index: var(--fc-time-grid_fc-highlight-container-z-index, 3); }.fc-time-grid .fc-mirror-container { position: var(--fc-time-grid_fc-mirror-container-position, relative); }.fc-time-grid .fc-mirror-container { z-index: var(--fc-time-grid_fc-mirror-container-z-index, 6); }.fc-time-grid .fc-now-indicator-arrow { margin-top: var(--fc-time-grid_fc-now-indicator-arrow-margin-top, -5px); }.fc-time-grid .fc-now-indicator-line { border-top-width: var(--fc-time-grid_fc-now-indicator-line-border-top-width, 1px); }.fc-time-grid .fc-now-indicator-line { left: var(--fc-time-grid_fc-now-indicator-line-left, 0); }.fc-time-grid .fc-now-indicator-line { right: var(--fc-time-grid_fc-now-indicator-line-right, 0); }.fc-time-grid .fc-now-indicator-line { z-index: var(--fc-time-grid_fc-now-indicator-line-z-index, 5); }.fc-time-grid .fc-slats .fc-minor td { border-top-style: var(--fc-time-grid_fc-slats_fc-minor_td-border-top-style, dotted); }.fc-time-grid .fc-slats td { border-bottom: var(--fc-time-grid_fc-slats_td-border-bottom, 0); }.fc-time-grid .fc-slats td { height: var(--fc-time-grid_fc-slats_td-height, 1.5em); }.fc-time-grid .fc-slats { position: var(--fc-time-grid_fc-slats-position, relative); }.fc-time-grid .fc-slats { z-index: var(--fc-time-grid_fc-slats-z-index, 2); }.fc-time-grid > .fc-bg { z-index: var(--fc-time-grid__LACE_BRACE__fc-bg-z-index, 1); }.fc-time-grid > hr { position: var(--fc-time-grid__LACE_BRACE__hr-position, relative); }.fc-time-grid > hr { z-index: var(--fc-time-grid__LACE_BRACE__hr-z-index, 2); }.fc-time-grid table { border: var(--fc-time-grid_table-border, 0 hidden transparent); }.fc-time-grid { min-height: var(--fc-time-grid-min-height, 100%); }.fc-time-grid { position: var(--fc-time-grid-position, relative); }.fc-time-grid { z-index: var(--fc-time-grid-z-index, 1); }.fc-time-grid-container { position: var(--fc-time-grid-container-position, relative); }.fc-time-grid-container { z-index: var(--fc-time-grid-container-z-index, 1); }.fc-time-grid-event .fc-content { max-height: var(--fc-time-grid-event_fc-content-max-height, 100%); }.fc-time-grid-event .fc-content { overflow: var(--fc-time-grid-event_fc-content-overflow, hidden); }.fc-time-grid-event .fc-time { font-size: var(--fc-time-grid-event_fc-time-font-size, 0.85em); }.fc-time-grid-event .fc-time { padding: var(--fc-time-grid-event_fc-time-padding, 0 1px); }.fc-time-grid-event .fc-time { white-space: var(--fc-time-grid-event_fc-time-white-space, nowrap); }.fc-time-grid-event .fc-title { padding: var(--fc-time-grid-event_fc-title-padding, 0 1px); }.fc-time-grid-event { margin-bottom: var(--fc-time-grid-event-margin-bottom, 1px); }.fc-time-grid-event-inset { -webkit-box-shadow: var(--fc-time-grid-event-inset--webkit-box-shadow, 0px 0px 0px 1px rgb(255, 255, 255)); }.fc-time-grid-event-inset { box-shadow: var(--fc-time-grid-event-inset-box-shadow, 0px 0px 0px 1px rgb(255, 255, 255)); }.fc-time-grid-event.fc-allow-mouse-resize .fc-resizer { bottom: var(--fc-time-grid-eventfc-allow-mouse-resize_fc-resizer-bottom, 0); }.fc-time-grid-event.fc-allow-mouse-resize .fc-resizer { cursor: var(--fc-time-grid-eventfc-allow-mouse-resize_fc-resizer-cursor, s-resize); }.fc-time-grid-event.fc-allow-mouse-resize .fc-resizer { font-family: var(--fc-time-grid-eventfc-allow-mouse-resize_fc-resizer-font-family, monospace); }.fc-time-grid-event.fc-allow-mouse-resize .fc-resizer { font-size: var(--fc-time-grid-eventfc-allow-mouse-resize_fc-resizer-font-size, 11px); }.fc-time-grid-event.fc-allow-mouse-resize .fc-resizer { height: var(--fc-time-grid-eventfc-allow-mouse-resize_fc-resizer-height, 8px); }.fc-time-grid-event.fc-allow-mouse-resize .fc-resizer { left: var(--fc-time-grid-eventfc-allow-mouse-resize_fc-resizer-left, 0); }.fc-time-grid-event.fc-allow-mouse-resize .fc-resizer { line-height: var(--fc-time-grid-eventfc-allow-mouse-resize_fc-resizer-line-height, 8px); }.fc-time-grid-event.fc-allow-mouse-resize .fc-resizer { overflow: var(--fc-time-grid-eventfc-allow-mouse-resize_fc-resizer-overflow, hidden); }.fc-time-grid-event.fc-allow-mouse-resize .fc-resizer { right: var(--fc-time-grid-eventfc-allow-mouse-resize_fc-resizer-right, 0); }.fc-time-grid-event.fc-allow-mouse-resize .fc-resizer { text-align: var(--fc-time-grid-eventfc-allow-mouse-resize_fc-resizer-text-align, center); }.fc-time-grid-event.fc-allow-mouse-resize .fc-resizer:after { content: var(--fc-time-grid-eventfc-allow-mouse-resize_fc-resizer_COLON_after-content, "="); }.fc-time-grid-event.fc-not-end { border-bottom-left-radius: var(--fc-time-grid-eventfc-not-end-border-bottom-left-radius, 0); }.fc-time-grid-event.fc-not-end { border-bottom-right-radius: var(--fc-time-grid-eventfc-not-end-border-bottom-right-radius, 0); }.fc-time-grid-event.fc-not-end { border-bottom-width: var(--fc-time-grid-eventfc-not-end-border-bottom-width, 0); }.fc-time-grid-event.fc-not-end { padding-bottom: var(--fc-time-grid-eventfc-not-end-padding-bottom, 1px); }.fc-time-grid-event.fc-not-start { border-top-left-radius: var(--fc-time-grid-eventfc-not-start-border-top-left-radius, 0); }.fc-time-grid-event.fc-not-start { border-top-right-radius: var(--fc-time-grid-eventfc-not-start-border-top-right-radius, 0); }.fc-time-grid-event.fc-not-start { border-top-width: var(--fc-time-grid-eventfc-not-start-border-top-width, 0); }.fc-time-grid-event.fc-not-start { padding-top: var(--fc-time-grid-eventfc-not-start-padding-top, 1px); }.fc-time-grid-event.fc-selected .fc-resizer { background: var(--fc-time-grid-eventfc-selected_fc-resizer-background, rgb(255, 255, 255)); }.fc-time-grid-event.fc-selected .fc-resizer { border-color: var(--fc-time-grid-eventfc-selected_fc-resizer-border-color, inherit); }.fc-time-grid-event.fc-selected .fc-resizer { border-radius: var(--fc-time-grid-eventfc-selected_fc-resizer-border-radius, 5px); }.fc-time-grid-event.fc-selected .fc-resizer { border-style: var(--fc-time-grid-eventfc-selected_fc-resizer-border-style, solid); }.fc-time-grid-event.fc-selected .fc-resizer { border-width: var(--fc-time-grid-eventfc-selected_fc-resizer-border-width, 1px); }.fc-time-grid-event.fc-selected .fc-resizer { bottom: var(--fc-time-grid-eventfc-selected_fc-resizer-bottom, -5px); }.fc-time-grid-event.fc-selected .fc-resizer { height: var(--fc-time-grid-eventfc-selected_fc-resizer-height, 8px); }.fc-time-grid-event.fc-selected .fc-resizer { left: var(--fc-time-grid-eventfc-selected_fc-resizer-left, 50%); }.fc-time-grid-event.fc-selected .fc-resizer { margin-left: var(--fc-time-grid-eventfc-selected_fc-resizer-margin-left, -5px); }.fc-time-grid-event.fc-selected .fc-resizer { width: var(--fc-time-grid-eventfc-selected_fc-resizer-width, 8px); }.fc-time-grid-event.fc-short .fc-content { white-space: var(--fc-time-grid-eventfc-short_fc-content-white-space, nowrap); }.fc-time-grid-event.fc-short .fc-time span { display: var(--fc-time-grid-eventfc-short_fc-time_span-display, none); }.fc-time-grid-event.fc-short .fc-time { display: var(--fc-time-grid-eventfc-short_fc-time-display, inline-block); }.fc-time-grid-event.fc-short .fc-time { vertical-align: var(--fc-time-grid-eventfc-short_fc-time-vertical-align, top); }.fc-time-grid-event.fc-short .fc-time:after { content: var(--fc-time-grid-eventfc-short_fc-time_COLON_after-content, " - "); }.fc-time-grid-event.fc-short .fc-time:before { content: var(--fc-time-grid-eventfc-short_fc-time_COLON_before-content, attr(data-start)); }.fc-time-grid-event.fc-short .fc-title { display: var(--fc-time-grid-eventfc-short_fc-title-display, inline-block); }.fc-time-grid-event.fc-short .fc-title { font-size: var(--fc-time-grid-eventfc-short_fc-title-font-size, 0.85em); }.fc-time-grid-event.fc-short .fc-title { padding: var(--fc-time-grid-eventfc-short_fc-title-padding, 0); }.fc-time-grid-event.fc-short .fc-title { vertical-align: var(--fc-time-grid-eventfc-short_fc-title-vertical-align, top); }.fc-timeGrid-view .fc-day-grid .fc-row .fc-content-skeleton { padding-bottom: var(--fc-timeGrid-view_fc-day-grid_fc-row_fc-content-skeleton-padding-bottom, 1em); }.fc-timeGrid-view .fc-day-grid .fc-row { min-height: var(--fc-timeGrid-view_fc-day-grid_fc-row-min-height, 3em); }.fc-timeGrid-view .fc-day-grid { position: var(--fc-timeGrid-view_fc-day-grid-position, relative); }.fc-timeGrid-view .fc-day-grid { z-index: var(--fc-timeGrid-view_fc-day-grid-z-index, 2); }.fc-toolbar > * > :not(:first-child) { margin-left: var(--fc-toolbar__LACE_BRACE___ASTERISK___LACE_BRACE___COLON_not_R_BRACKET_OPEN__COLON_first-child_R_BRACKET_CLOSE_-margin-left, 0.75em); }.fc-toolbar h2 { font-size: var(--fc-toolbar_h2-font-size, 1.75em); }.fc-toolbar h2 { margin: var(--fc-toolbar_h2-margin, 0); }.fc-toolbar { align-items: var(--fc-toolbar-align-items, center); }.fc-toolbar { display: var(--fc-toolbar-display, flex); }.fc-toolbar { justify-content: var(--fc-toolbar-justify-content, space-between); }.fc-toolbar.fc-footer-toolbar { margin-top: var(--fc-toolbarfc-footer-toolbar-margin-top, 1.5em); }.fc-toolbar.fc-header-toolbar { margin-bottom: var(--fc-toolbarfc-header-toolbar-margin-bottom, 1.5em); }.fc-unselectable { -khtml-user-select: var(--fc-unselectable--khtml-user-select, none); }.fc-unselectable { -moz-user-select: var(--fc-unselectable--moz-user-select, none); }.fc-unselectable { -ms-user-select: var(--fc-unselectable--ms-user-select, none); }.fc-unselectable { -webkit-tap-highlight-color: var(--fc-unselectable--webkit-tap-highlight-color, rgba(0, 0, 0, 0)); }.fc-unselectable { -webkit-touch-callout: var(--fc-unselectable--webkit-touch-callout, none); }.fc-unselectable { -webkit-user-select: var(--fc-unselectable--webkit-user-select, none); }.fc-unselectable { user-select: var(--fc-unselectable-user-select, none); }.fc-unthemed .fc-content { border-color: var(--fc-unthemed_fc-content-border-color, rgb(221, 221, 221)); }.fc-unthemed .fc-disabled-day { background: var(--fc-unthemed_fc-disabled-day-background, rgb(215, 215, 215)); }.fc-unthemed .fc-disabled-day { opacity: var(--fc-unthemed_fc-disabled-day-opacity, 0.3); }.fc-unthemed .fc-divider { background: var(--fc-unthemed_fc-divider-background, rgb(238, 238, 238)); }.fc-unthemed .fc-divider { border-color: var(--fc-unthemed_fc-divider-border-color, rgb(221, 221, 221)); }.fc-unthemed .fc-list-empty { background-color: var(--fc-unthemed_fc-list-empty-background-color, rgb(238, 238, 238)); }.fc-unthemed .fc-list-heading td { background: var(--fc-unthemed_fc-list-heading_td-background, rgb(238, 238, 238)); }.fc-unthemed .fc-list-heading td { border-color: var(--fc-unthemed_fc-list-heading_td-border-color, rgb(221, 221, 221)); }.fc-unthemed .fc-list-item:hover td { background-color: var(--fc-unthemed_fc-list-item_COLON_hover_td-background-color, rgb(245, 245, 245)); }.fc-unthemed .fc-list-view { border-color: var(--fc-unthemed_fc-list-view-border-color, rgb(221, 221, 221)); }.fc-unthemed .fc-popover .fc-header { background: var(--fc-unthemed_fc-popover_fc-header-background, rgb(238, 238, 238)); }.fc-unthemed .fc-popover { background-color: var(--fc-unthemed_fc-popover-background-color, rgb(255, 255, 255)); }.fc-unthemed .fc-popover { border-color: var(--fc-unthemed_fc-popover-border-color, rgb(221, 221, 221)); }.fc-unthemed .fc-popover { border-style: var(--fc-unthemed_fc-popover-border-style, solid); }.fc-unthemed .fc-popover { border-width: var(--fc-unthemed_fc-popover-border-width, 1px); }.fc-unthemed .fc-row { border-color: var(--fc-unthemed_fc-row-border-color, rgb(221, 221, 221)); }.fc-unthemed tbody { border-color: var(--fc-unthemed_tbody-border-color, rgb(221, 221, 221)); }.fc-unthemed td { border-color: var(--fc-unthemed_td-border-color, rgb(221, 221, 221)); }.fc-unthemed td.fc-today { background: var(--fc-unthemed_tdfc-today-background, rgb(252, 248, 227)); }.fc-unthemed th { border-color: var(--fc-unthemed_th-border-color, rgb(221, 221, 221)); }.fc-unthemed thead { border-color: var(--fc-unthemed_thead-border-color, rgb(221, 221, 221)); }.fc-view > table { position: var(--fc-view__LACE_BRACE__table-position, relative); }.fc-view > table { z-index: var(--fc-view__LACE_BRACE__table-z-index, 1); }.fc-view { position: var(--fc-view-position, relative); }.fc-view { z-index: var(--fc-view-z-index, 1); }.fc-view-container * { -moz-box-sizing: var(--fc-view-container__ASTERISK_--moz-box-sizing, content-box); }.fc-view-container * { -webkit-box-sizing: var(--fc-view-container__ASTERISK_--webkit-box-sizing, content-box); }.fc-view-container * { box-sizing: var(--fc-view-container__ASTERISK_-box-sizing, content-box); }.fc-view-container *:after { -moz-box-sizing: var(--fc-view-container__ASTERISK__COLON_after--moz-box-sizing, content-box); }.fc-view-container *:after { -webkit-box-sizing: var(--fc-view-container__ASTERISK__COLON_after--webkit-box-sizing, content-box); }.fc-view-container *:after { box-sizing: var(--fc-view-container__ASTERISK__COLON_after-box-sizing, content-box); }.fc-view-container *:before { -moz-box-sizing: var(--fc-view-container__ASTERISK__COLON_before--moz-box-sizing, content-box); }.fc-view-container *:before { -webkit-box-sizing: var(--fc-view-container__ASTERISK__COLON_before--webkit-box-sizing, content-box); }.fc-view-container *:before { box-sizing: var(--fc-view-container__ASTERISK__COLON_before-box-sizing, content-box); }.fc-view-container { position: var(--fc-view-container-position, relative); }a.fc-more { cursor: var(--afc-more-cursor, pointer); }a.fc-more { font-size: var(--afc-more-font-size, 0.85em); }a.fc-more { margin: var(--afc-more-margin, 1px 3px); }a.fc-more { text-decoration: var(--afc-more-text-decoration, none); }a.fc-more:hover { text-decoration: var(--afc-more_COLON_hover-text-decoration, underline); }a[data-goto] { cursor: var(--a_SQUARE_BRACKET_OPEN_data-goto_SQUARE_BRACKET_CLOSE_-cursor, pointer); }a[data-goto]:hover { text-decoration: var(--a_SQUARE_BRACKET_OPEN_data-goto_SQUARE_BRACKET_CLOSE__COLON_hover-text-decoration, underline); }body .fc { font-size: var(--body_fc-font-size, 1em); }hr.fc-divider { border-width: var(--hrfc-divider-border-width, 1px 0); }hr.fc-divider { height: var(--hrfc-divider-height, 0); }hr.fc-divider { margin: var(--hrfc-divider-margin, 0); }hr.fc-divider { padding: var(--hrfc-divider-padding, 0 0 2px); }tr:first-child > td > .fc-day-grid-event { margin-top: var(--tr_COLON_first-child__LACE_BRACE__td__LACE_BRACE__fc-day-grid-event-margin-top, 2px); }</style>
`;
    }

    static get templateElementCss() {
        return html`
            <style>
                :host {
                        display: flex;
                    }
        
                    #calendar-container, #calendar {
                        display: flex;
                        flex-direction: column;
                        flex-grow: 1;
                    }
                    
                    </style>
`;
    }

    static get templateContainer() {
        return html`<div id='calendar-container'>
                <div id='calendar'></div>
            </div>`;
    }

    static get properties() {
        return {
            eventLimit: {
                type: Object,
                value: false
            },
            navLinks: {
                type: Boolean,
                value: true
            },
            selectable: {
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
            this._calendar = new Calendar(this.$.calendar, options);

            this._calendar.render(); // needed for method calls, that somehow access the calendar's internals.

            afterNextRender(this, function () {
                // used to assure correct initial size. It seems, that with V15 (currently 15.0.5) the lifecycle
                // can not guarantee, that the calendar container provides the correct height, since it might not
                // be rendered yet. This method call will upate the size correctly.
                this._calendar.updateSize();

            });
        }
    }

    /**
     * Creates an object that maps client side event information to server side information.
     * The returned object contains keys, that will be interpreted as client and server side event names.
     * Each key is assigned a function that takes the event info object as parameter and returns the
     * set of information as an object.
     * <br><br>
     * Does also include navLinkDayClick, navLinkWeekClick, but here the parameters are different (date for day
     * and weekStart moment for week). See FC doc for details about these functions. Same for eventLimitClick.
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
                    date: eventInfo.dateStr,
                    allDay: eventInfo.allDay,
                    resource: eventInfo.resource ? eventInfo.resource.id : null
                }
            },
            select: (eventInfo) => {
                return {
                    start: this._formatDate(eventInfo.start),
                    end: this._formatDate(eventInfo.end),
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
            eventResize: (eventInfo) => {
                return {
                    data: this._toEventData(eventInfo.event),
                    delta: eventInfo.endDelta
                }
            },
            eventDrop: (eventInfo) => {
                return {
                    data: this._toEventData(eventInfo.event, eventInfo.oldResource, eventInfo.newResource),
                    // data: this._toEventData(eventInfo.event),
                    delta: eventInfo.delta,
                    // oldResource: eventInfo.oldResource ? eventInfo.oldResource.id : null,
                    // newResource: eventInfo.newResource ? eventInfo.newResource.id : null
                }
            },
            datesRender: (eventInfo) => {
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
            eventLimitClick: (eventInfo) => {
                return {
                    date: this._formatDate(eventInfo.date, true)
                }
            },
            viewSkeletonRender: (eventInfo) => {
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
        let moment = toMoment(date, this.getCalendar());
        if (asDay) {
            moment = moment.startOf('day');
        }

        let dateString = moment.format();

        return asDay ? dateString.substr(0, dateString.indexOf('T')) : dateString;
    }

    // _formatDate(date, asDay) {
    //     let dateString = date.getFullYear()
    //         + "-" + this._withLeadingZero(date.getMonth())
    //         + "-" + this._withLeadingZero(date.getDay());
    //
    //     if (!asDay) {
    //         let offset = date.getTimezoneOffset();
    //
    //         let offsetString;
    //         if (offset === 0) {
    //             offsetString = "Z"
    //         } else {
    //             offsetString = offset < 0 ? "-" : "+";
    //
    //             offset = Math.abs(offset);
    //
    //             let offsetHours = Math.trunc(offset / 60);
    //             let offsetMinutes = offset - (offsetHours * 60);
    //
    //             offsetString += this._withLeadingZero(offsetHours) + ":" + this._withLeadingZero(offsetMinutes);
    //         }
    //
    //         dateString += "T" + this._withLeadingZero(date.getHours())
    //             + ":" + this._withLeadingZero(date.getMinutes())
    //             + ":" + this._withLeadingZero(date.getSeconds())
    //             + offsetString;
    //     }
    //
    //     return dateString
    // }

    // _withLeadingZero(number) {
    //     return (number < 10 ? "0" : "") + number;
    // }

    _createInitOptions(initialOptions) {
        let events = this._createEventHandlers();

        let options = initialOptions != null ? initialOptions : {
            height: 'parent',
            timeZone: 'UTC',

            // // no native control elements
            header: false,
            weekNumbers: true,
            eventLimit: this.eventLimit,
            navLinks: this.navLinks,
            selectable: this.selectable,
        };

        this._addEventHandlersToOptions(options, events);

        options['locales'] = allLocales;
        options['plugins'] = [interaction, dayGridPlugin, timeGridPlugin, listPlugin, momentTimezonePlugin];

        return options;
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
                    }
                }
            }
        }
    }


    setOption(key, value) {
        let calendar = this.getCalendar();
        if (key === "timezone" && calendar.getOption("timezone") !== value) {
            this.dispatchEvent(new CustomEvent("timezone-changed", {
                detail: {
                    timezone: value
                }
            }));
        }

        this.noDatesRenderEvent = this.noDatesRenderEventOnOptionSetting;
        calendar.setOption(key, value);
        this.noDatesRenderEvent = false;
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
        let end = event.end;
        if (end != null) {
            end = this._formatDate(end);
        } else if (event.allDay) { // when moved from time slotted to all day
            end = this._formatDate(new Date(event.start.valueOf() + 86400000)); // + 1 day
        } else { // when moved from all day to time slotted
            end = this._formatDate(new Date(event.start.valueOf() + 3600000)); // + 1 hour
        }

        let data = {
            id: event.id,
            start: this._formatDate(event.start),
            end: end,
            allDay: event.allDay,
            editable: event.editable
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

    addEvents(obj) {
        this.getCalendar().addEventSource(obj);
    }

    updateEvents(array) {
        const calendar = this.getCalendar();
        calendar.batchRendering(() => {

            for (let i = 0; i < array.length; i++) {
                let obj = array[i];

                let eventToUpdate = calendar.getEventById(obj.id);

                if (eventToUpdate != null) {
                    // TODO check for unchanged values and ignore them

                    // since currently recurring events can not be set by updating existing events, we circumcise that
                    // by simply re-adding the event.
                    // https://github.com/fullcalendar/fullcalendar/issues/4393

                    if (obj['_hardReset'] === true || this._isServerSideRecurring(obj) || this._isClientSideRecurring(eventToUpdate)) {
                        eventToUpdate.remove();
                        this.addEvents([obj]);
                    } else {
                        let start = obj['start'] != null ? calendar.formatIso(obj['start'], obj['allDay']) : null;
                        let end = obj['end'] != null ? calendar.formatIso(obj['end'], obj['allDay']) : null;

                        eventToUpdate.setDates(start, end, {allDay: obj['allDay']});

                        // setting all day is not working 100%, we workaround it here
                        if (obj['allDay']) {
                            eventToUpdate.moveEnd()
                        }

                        for (let property in obj) {
                            if (property !== 'id' && property !== "start" && property !== "end" && property !== "allDay") {
                                eventToUpdate.setProp(property, obj[property]);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Checks entries coming from the server side if they are recurring.
     * @param event
     * @returns {boolean}
     * @private
     */
    _isServerSideRecurring(event) {
        return event.daysOfWeek != null || event.startTime != null || event.endTime != null || event.startRecur != null || event.endRecur != null;
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
        for (var i = 0; i < array.length; i++) {
            let event = this.getCalendar().getEventById(array[i].id);
            if (event != null) {
                event.remove();
            }
        }
    }


    removeAllEvents() {
        //this.getCalendar().getEvents().forEach(e => e.remove());
        var calendar = this.getCalendar();
        this.getCalendar().batchRendering(function () {
            calendar.getEvents().forEach(e => e.remove());
        });
    }


    changeView(viewName) {
        this.getCalendar().changeView(viewName);
    }

    render() {
        this.getCalendar().render();
    }

    setEventRenderCallback(s) {
        let calendar = this.getCalendar();
        calendar.setOption('eventRender', new Function("return " + s)());
    }

}

customElements.define("full-calendar", FullCalendar);