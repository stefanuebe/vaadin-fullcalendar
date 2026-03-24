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
import {FullCalendar, evaluateCallbacks} from "@vaadin/flow-frontend/vaadin-full-calendar/full-calendar";
import resourceTimelinePlugin from '@fullcalendar/resource-timeline';
import resourceTimeGridPlugin from '@fullcalendar/resource-timegrid';
import resourceDayGridPlugin from '@fullcalendar/resource-daygrid';
import scrollgridPlugin from '@fullcalendar/scrollgrid';

export class FullCalendarScheduler extends FullCalendar {

    private _componentContainer: HTMLElement | null = null;

    connectedCallback() {
        super.connectedCallback();
        // Ensure the component container exists after FC has rendered.
        // FC's Calendar(this) wipes all light DOM children during init,
        // so any server-appended container is lost. We re-create it here.
        this.ensureComponentContainer();
    }

    private ensureComponentContainer(): HTMLElement {
        if (!this._componentContainer || !this.contains(this._componentContainer)) {
            this._componentContainer = document.createElement('div');
            this._componentContainer.setAttribute('data-fc-component-container', '');
            this._componentContainer.style.display = 'none';
            this.appendChild(this._componentContainer);
        }
        return this._componentContainer;
    }

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
                calendar.addResource(evaluateCallbacks(array[i]), scrollToLast);
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

    updateResource(jsonStr: string) {
        const data = JSON.parse(jsonStr);
        const resource = this.calendar.getResourceById(data.id);
        if (resource) {
            if (data.title !== undefined) resource.setProp('title', data.title);
            if (data.eventColor !== undefined) resource.setProp('eventColor', data.eventColor);
            if (data.eventBackgroundColor !== undefined) resource.setProp('eventBackgroundColor', data.eventBackgroundColor);
            if (data.eventBorderColor !== undefined) resource.setProp('eventBorderColor', data.eventBorderColor);
            if (data.eventTextColor !== undefined) resource.setProp('eventTextColor', data.eventTextColor);
            if (data.eventConstraint !== undefined) resource.setProp('eventConstraint', data.eventConstraint);
            if (data.eventOverlap !== undefined) resource.setProp('eventOverlap', evaluateCallbacks(data.eventOverlap));
            if (data.eventAllow !== undefined) resource.setProp('eventAllow', evaluateCallbacks(data.eventAllow));
            if (data.eventClassNames !== undefined) resource.setProp('eventClassNames', evaluateCallbacks(data.eventClassNames));
        }
    }

    // ---- Component Resource Column support ----

    returnComponentToContainer(resourceId: string, columnKey: string) {
        const container = this.querySelector('[data-fc-component-container]');
        if (!container) return;
        const escapedId = CSS.escape(resourceId);
        const component = this.querySelector(
            `[data-rc-resource-id="${escapedId}"][data-rc-column-key="${columnKey}"]`
        );
        if (component) {
            (component as HTMLElement).style.display = 'none';
            container.appendChild(component);
        }
    }

    returnAllComponentsToContainer() {
        const container = this.querySelector('[data-fc-component-container]');
        if (!container) return;
        const components = this.querySelectorAll('[data-rc-resource-id]');
        components.forEach((comp) => {
            (comp as HTMLElement).style.display = 'none';
            container.appendChild(comp);
        });
    }

    rerenderResources() {
        this.calendar.render();
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