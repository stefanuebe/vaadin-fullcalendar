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
import {FullCalendarScheduler} from 'Frontend/generated/jar-resources/vaadin-full-calendar/full-calendar-scheduler';
import tippy from 'tippy.js';


export class FullCalendarWithTooltip extends FullCalendarScheduler {
    initCalendar() {
        super.initCalendar();

        this.calendar!.setOption("eventDidMount", e => {
            this.initTooltip(e);
        });
    }

    initTooltip(e: any) {
        if (e.event.title && !e.isMirror) {
            // Prevent adding duplicate listeners by checking if already initialized
            if (e.el._tooltipInitialized) {
                return;
            }
            e.el._tooltipInitialized = true;

            const mouseEnterHandler = () => {
                // Destroy existing tippy instance if any to prevent duplicates
                if (e.el._tippy) {
                    e.el._tippy.destroy();
                }

                let tooltip = e.event.getCustomProperty("description", e.event.title);

                e.el._tippy = tippy(e.el, {
                    theme: 'light',
                    content: tooltip,
                    trigger: 'manual'
                });

                e.el._tippy.show();
            };

            const mouseLeaveHandler = () => {
                if (e.el._tippy) {
                    e.el._tippy.destroy();
                    e.el._tippy = null;
                }
            };

            e.el.addEventListener("mouseenter", mouseEnterHandler);
            e.el.addEventListener("mouseleave", mouseLeaveHandler);

            // Store handlers for potential cleanup
            e.el._tooltipHandlers = { mouseEnterHandler, mouseLeaveHandler };
        }
    }
}

customElements.define("full-calendar-with-tooltip", FullCalendarWithTooltip);