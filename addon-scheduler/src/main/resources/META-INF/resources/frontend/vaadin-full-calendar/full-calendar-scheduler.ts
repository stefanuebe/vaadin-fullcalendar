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
import resourceTimelinePlugin from 'fullcalendar-scheduler/resource-timeline';
import resourceTimeGridPlugin from 'fullcalendar-scheduler/resource-timegrid';
import resourceDayGridPlugin from 'fullcalendar-scheduler/resource-daygrid';
import clsx from 'clsx';

export class FullCalendarScheduler extends FullCalendar {

    private _componentContainer: HTMLElement | null = null;
    // Cells waiting for their Vaadin component: key = "resourceId::columnKey"
    _pendingCells = new Map<string, HTMLElement>();
    private _componentObserver: MutationObserver | null = null;

    connectedCallback() {
        // Rescue Vaadin component elements before FC's Calendar(this) wipes all DOM children.
        // FC wipes this.children synchronously inside super.connectedCallback().
        const rescued = Array.from(this.querySelectorAll('[data-rc-resource-id]')) as HTMLElement[];

        // Clear pending cells here — they will be re-populated by cellDidMount during render().
        // Do NOT clear inside _setupComponentObserver: that runs after render() and would erase
        // the cells that cellDidMount just registered.
        this._pendingCells.clear();

        super.connectedCallback();

        // FC has now initialized and rendered. Cells are in _pendingCells.
        const container = this.ensureComponentContainer();

        // Set up the observer BEFORE processing rescued elements so any components that Vaadin
        // adds asynchronously (after connectedCallback returns) are caught automatically.
        this._setupComponentObserver();

        // Place rescued elements directly and synchronously — no MutationObserver round-trip needed.
        for (const el of rescued) {
            const resourceId = el.getAttribute('data-rc-resource-id')!;
            const columnKey = el.getAttribute('data-rc-column-key')!;
            const cellKey = `${resourceId}::${columnKey}`;
            const cellEl = this._pendingCells.get(cellKey);
            if (cellEl) {
                cellEl.appendChild(el);
                el.style.display = '';
                this._pendingCells.delete(cellKey);
            } else {
                // Cell not yet known — park in hidden container; cellDidMount will pick it up.
                el.style.display = 'none';
                container.appendChild(el);
            }
        }
    }

    disconnectedCallback() {
        this._componentObserver?.disconnect();
        this._componentObserver = null;
        this._pendingCells.clear();
        super.disconnectedCallback();
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

    private _setupComponentObserver() {
        this._componentObserver?.disconnect();
        // _pendingCells is intentionally NOT cleared here. It is cleared at the start of
        // connectedCallback() before super.connectedCallback() runs, so that cellDidMount
        // can populate it freshly. Clearing here would erase those registrations.
        this._componentObserver = new MutationObserver((mutations) => {
            for (const mutation of mutations) {
                for (const node of mutation.addedNodes) {
                    this._tryPlaceComponent(node as HTMLElement);
                }
            }
        });
        // Observe the document body with subtree so we catch virtual children rendered via
        // <flow-component-renderer> which FC's Calendar(el) wipe does not affect.
        this._componentObserver.observe(document.body, { childList: true, subtree: true });
    }

    private _tryPlaceComponent(el: HTMLElement) {
        if (el.nodeType !== 1 || !el.hasAttribute) return;
        if (el.hasAttribute('data-rc-resource-id')) {
            this._placeOrDefer(el);
        }
        // Also check children (in case Vaadin wraps the element)
        if (el.querySelectorAll) {
            el.querySelectorAll('[data-rc-resource-id]').forEach((child) => {
                this._placeOrDefer(child as HTMLElement);
            });
        }
    }

    private _placeOrDefer(el: HTMLElement) {
        const resourceId = el.getAttribute('data-rc-resource-id')!;
        const columnKey = el.getAttribute('data-rc-column-key')!;
        const cellKey = `${resourceId}::${columnKey}`;
        const cellEl = this._pendingCells.get(cellKey);
        if (cellEl) {
            cellEl.appendChild(el);
            el.style.display = '';
            this._pendingCells.delete(cellKey);
        } else {
            // No pending cell yet — move to hidden container so cellDidMount can find it later.
            // Do NOT move if already inside this element (e.g. as a direct child after Vaadin appended
            // it, or inside a cell after cellDidMount placed it) — cellDidMount will find it there via
            // document.querySelector. Moving it back would undo placement done by cellDidMount.
            const container = this.querySelector('[data-fc-component-container]');
            if (container && !this.contains(el)) {
                el.style.display = 'none';
                container.appendChild(el);
            }
        }
    }

    protected createInitOptions(initialOptions: any) {
        const options = super.createInitOptions(initialOptions);

        options.resources = options.resources ?? [];

        // scrollgridPlugin removed in v7 (merged into core)
        options.plugins.push(resourceTimeGridPlugin, resourceDayGridPlugin, resourceTimelinePlugin);

        // --- v7 class API: inject scheduler-specific CSS class names ---
        const serverResourceLaneClass = options.resourceLaneClass;
        options.resourceLaneClass = clsx('vfc-resource-lane', serverResourceLaneClass);

        const serverResourceCellClass = options.resourceCellClass;
        options.resourceCellClass = clsx('vfc-resource-cell', serverResourceCellClass);

        const serverResourceColumnHeaderClass = options.resourceColumnHeaderClass;
        options.resourceColumnHeaderClass = clsx('vfc-resource-col-header', serverResourceColumnHeaderClass);

        const serverResourceGroupHeaderClass = options.resourceGroupHeaderClass;
        const userResourceGroupHeaderClass = typeof serverResourceGroupHeaderClass === 'function' ? serverResourceGroupHeaderClass : null;
        options.resourceGroupHeaderClass = (data: any) => {
            // v7 renamed data.groupValue -> data.fieldValue
            const base = 'vfc-resource-group-header';
            return userResourceGroupHeaderClass ? clsx(base, userResourceGroupHeaderClass(data)) : base;
        };

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
            // v7: eventColor is the unified color prop (replaces eventBackgroundColor + eventBorderColor)
            if (data.eventColor !== undefined) resource.setProp('eventColor', data.eventColor);
            // v7: eventBackgroundColor / eventBorderColor are obsolete — map both into eventColor
            if (data.eventBackgroundColor !== undefined) resource.setProp('eventColor', data.eventBackgroundColor);
            if (data.eventBorderColor !== undefined) resource.setProp('eventColor', data.eventBorderColor);
            // v7: eventTextColor -> eventContrastColor
            if (data.eventTextColor !== undefined) resource.setProp('eventContrastColor', data.eventTextColor);
            if (data.eventContrastColor !== undefined) resource.setProp('eventContrastColor', data.eventContrastColor);
            if (data.eventConstraint !== undefined) resource.setProp('eventConstraint', data.eventConstraint);
            if (data.eventOverlap !== undefined) resource.setProp('eventOverlap', evaluateCallbacks(data.eventOverlap));
            if (data.eventAllow !== undefined) resource.setProp('eventAllow', evaluateCallbacks(data.eventAllow));
            // v7: eventClassNames -> eventClass
            if (data.eventClassNames !== undefined) resource.setProp('eventClass', evaluateCallbacks(data.eventClassNames));
            if (data.eventClass !== undefined) resource.setProp('eventClass', evaluateCallbacks(data.eventClass));

            // Extended props: any top-level JSON key not covered above is treated as an extended prop.
            // Resource.toJson() serializes extended props flat at the top level (the FC Resource
            // constructor accepts them that way), so we mirror that shape here on update.
            const handled = new Set([
                'id', 'title', 'parentId', 'children', 'businessHours',
                'eventColor', 'eventBackgroundColor', 'eventBorderColor',
                'eventTextColor', 'eventContrastColor',
                'eventConstraint', 'eventOverlap', 'eventAllow',
                'eventClassNames', 'eventClass'
            ]);
            for (const key of Object.keys(data)) {
                if (!handled.has(key)) {
                    resource.setExtendedProp(key, data[key]);
                }
            }
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
}

customElements.define("vaadin-full-calendar-scheduler", FullCalendarScheduler);