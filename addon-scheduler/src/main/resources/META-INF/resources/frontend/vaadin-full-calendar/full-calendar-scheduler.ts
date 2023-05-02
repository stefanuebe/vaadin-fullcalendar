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
import {FullCalendar} from "@vaadin/flow-frontend/vaadin-full-calendar/full-calendar";
import resourceTimelinePlugin from '@fullcalendar/resource-timeline';
import resourceTimeGridPlugin from '@fullcalendar/resource-timegrid';

export class FullCalendarScheduler extends FullCalendar {

    // stores any options, that are set before the calendar is attached using "setOption"

    protected createInitOptions(initialOptions: any) {
        const options = super.createInitOptions(initialOptions);

        options.resources = options.resources ?? [];

        options.plugins.push(resourceTimeGridPlugin, resourceTimelinePlugin);

        return options;
    }

    addResources(array: any[], scrollToLast: boolean) {
        let calendar = this.calendar;
        calendar.batchRendering(function () {
            for (let i = 0; i < array.length; i++) {
                calendar.addResource(array[i], scrollToLast);
            }
        });
    }

    removeResources(array: any[]) {
        let calendar = this.calendar;
        calendar.batchRendering(function () {
            for (let i = 0; i < array.length; i++) {
                const resource = calendar.getResourceById(array[i].id);
                if (resource != null) {
                    resource.remove();
                }
            }
        });
    }

    removeAllResources() {
        let calendar = this.calendar;
        calendar.batchRendering(function () {
            calendar.getResources().forEach(r => r.remove());
        });
    }

    setResourceLabelClassNamesCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceLabelClassNames', new Function("return " + s)());
    }

    setResourceLabelContentCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceLabelContent', new Function("return " + s)());
    }

    setResourceLabelDidMountCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceLabelDidMount', new Function("return " + s)());
    }

    setResourceLablelWillUnmountCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceLabelWillUnmount', new Function("return " + s)());
    }

    setResourceLaneClassNamesCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceLaneClassNames', new Function("return " + s)());
    }

    setResourceLaneContentCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceLaneContent', new Function("return " + s)());
    }

    setResourceLaneDidMountCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceLaneDidMount', new Function("return " + s)());
    }

    setResourceLaneWillUnmountCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceLaneWillUnmount', new Function("return " + s)());
    }
}

customElements.define("vaadin-full-calendar-scheduler", FullCalendarScheduler);