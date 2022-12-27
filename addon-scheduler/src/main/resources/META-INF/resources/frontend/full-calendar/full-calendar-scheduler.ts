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

import {FullCalendar} from "@vaadin/flow-frontend/full-calendar/full-calendar";
import resourceTimelinePlugin from '@fullcalendar/resource-timeline';
import resourceTimeGridPlugin from '@fullcalendar/resource-timegrid';

@customElement("full-calendar-scheduler")
export class FullCalendarScheduler extends FullCalendar {

    protected createInitOptions(initialOptions: any) {
        const options = super.createInitOptions(initialOptions);

        options.resources = (fetchInfo: any, successCallback: (p: any) => void, failureCallback: any) => {
            (this as any).$server.fetchResourcesFromServer().then((array: any | any[]) => {
                if (Array.isArray(array)) {
                    successCallback(array);
                } else {
                    failureCallback("could not fetch");
                }
            })
        };
        options.plugins.push(resourceTimeGridPlugin, resourceTimelinePlugin);

        return options;
    }

    refreshAllResources() {
        this.calendar?.refetchEvents();
    }

    setResourceLabelClassNamesCallback(s: string) {
        this.calendar?.setOption('resourceLabelClassNames', new Function("return " + s)());
    }

    setResourceLabelContentCallback(s: string) {
        this.calendar?.setOption('resourceLabelContent', new Function("return " + s)());
    }

    setResourceLabelDidMountCallback(s: string) {
        this.calendar?.setOption('resourceLabelDidMount', new Function("return " + s)());
    }

    setResourceLablelWillUnmountCallback(s: string) {
        this.calendar?.setOption('resourceLabelWillUnmount', new Function("return " + s)());
    }

    setResourceLaneClassNamesCallback(s: string) {
        this.calendar?.setOption('resourceLaneClassNames', new Function("return " + s)());
    }

    setResourceLaneContentCallback(s: string) {
        this.calendar?.setOption('resourceLaneContent', new Function("return " + s)());
    }

    setResourceLaneDidMountCallback(s: string) {
        this.calendar?.setOption('resourceLaneDidMount', new Function("return " + s)());
    }

    setResourceLaneWillUnmountCallback(s: string) {
        this.calendar?.setOption('resourceLaneWillUnmount', new Function("return " + s)());
    }
}