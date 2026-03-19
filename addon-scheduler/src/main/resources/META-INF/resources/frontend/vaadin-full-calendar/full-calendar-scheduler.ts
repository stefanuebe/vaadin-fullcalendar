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
import {FullCalendar} from "@vaadin/flow-frontend/vaadin-full-calendar/full-calendar";
import resourceTimelinePlugin from '@fullcalendar/resource-timeline';
import resourceTimeGridPlugin from '@fullcalendar/resource-timegrid';
import resourceDayGridPlugin from '@fullcalendar/resource-daygrid';
import scrollgridPlugin from '@fullcalendar/scrollgrid';

export class FullCalendarScheduler extends FullCalendar {

    // stores any options, that are set before the calendar is attached using "setOption"

    protected createInitOptions(initialOptions: any) {
        const options = super.createInitOptions(initialOptions);

        options.resources = options.resources ?? [];

        options.plugins.push(scrollgridPlugin, resourceTimeGridPlugin, resourceDayGridPlugin, resourceTimelinePlugin);

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

    setResourceLabelWillUnmountCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceLabelWillUnmount', new Function("return " + s)());
    }

    setResourceLablelWillUnmountCallback(s: string) {
        this.setResourceLabelWillUnmountCallback(s);
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

    setResourceGroupClassNamesCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceGroupClassNames', new Function("return " + s)());
    }

    setResourceGroupContentCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceGroupContent', new Function("return " + s)());
    }

    setResourceGroupDidMountCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceGroupDidMount', new Function("return " + s)());
    }

    setResourceGroupWillUnmountCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceGroupWillUnmount', new Function("return " + s)());
    }

    setResourceGroupLaneClassNamesCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceGroupLaneClassNames', new Function("return " + s)());
    }

    setResourceGroupLaneContentCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceGroupLaneContent', new Function("return " + s)());
    }

    setResourceGroupLaneDidMountCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceGroupLaneDidMount', new Function("return " + s)());
    }

    setResourceGroupLaneWillUnmountCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceGroupLaneWillUnmount', new Function("return " + s)());
    }

    setResourceAreaHeaderClassNamesCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceAreaHeaderClassNames', new Function("return " + s)());
    }

    setResourceAreaHeaderDidMountCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceAreaHeaderDidMount', new Function("return " + s)());
    }

    setResourceAreaHeaderWillUnmountCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceAreaHeaderWillUnmount', new Function("return " + s)());
    }

    setResourceAddCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceAdd', new Function("return " + s)());
    }

    setResourceChangeCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceChange', new Function("return " + s)());
    }

    setResourceRemoveCallback(s: string) {
        // @ts-ignore
        this.setOption('resourceRemove', new Function("return " + s)());
    }

    setResourcesSetCallback(s: string) {
        // @ts-ignore
        this.setOption('resourcesSet', new Function("return " + s)());
    }

    updateResource(jsonStr: string) {
        const data = JSON.parse(jsonStr);
        const resource = this.calendar.getResourceById(data.id);
        if (resource) {
            if (data.title !== undefined) resource.setProp('title', data.title);
            if (data.eventColor !== undefined) resource.setProp('eventColor', data.eventColor);
            if (data.eventBackgroundColor !== undefined) resource.setProp('eventBackgroundColor', data.eventBackgroundColor);
            if (data.eventBorderColor !== undefined) resource.setProp('eventBorderColor', data.eventBorderColor);
            if (data.eventTextColor !== undefined) resource.setProp('eventTextColor', data.eventTextColor);
            if (data.eventConstraint !== undefined) resource.setExtendedProp('eventConstraint', data.eventConstraint);
            if (data.eventOverlap !== undefined) resource.setExtendedProp('eventOverlap', data.eventOverlap);
            if (data.eventClassNames !== undefined) resource.setProp('eventClassNames', data.eventClassNames);
        }
    }
}

customElements.define("vaadin-full-calendar-scheduler", FullCalendarScheduler);