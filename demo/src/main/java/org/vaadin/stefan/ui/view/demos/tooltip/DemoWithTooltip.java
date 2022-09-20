/*
 * Copyright 2020, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.ui.view.demos.tooltip;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.JsonObject;
import org.vaadin.stefan.ui.view.AbstractCalendarView;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.dataprovider.LazyInMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

@Route(value = "tooltip", layout = MainLayout.class)
@CssImport("./styles.css")
@CssImport("./styles-scheduler.css")
@PageTitle("FC with Tooltips")
@MenuItem(label = "Tooltips")
public class DemoWithTooltip extends AbstractCalendarView {
    private static final long serialVersionUID = 1L;
    private LazyInMemoryEntryProvider<Entry> entryProvider;

    @Override
    protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
        EntryService entryService = EntryService.createSimpleInstance();

        FullCalendar calendar = FullCalendarBuilder.create()
                .withCustomType(FullCalendarWithTooltip.class) // create a new instance with a custom type
                .withInitialOptions(defaultInitialOptions)
                .withEntryLimit(3)
                .withInitialEntries(entryService.getEntries()) // init with some sample data
                .build();
        
        calendar.setEntryDidMountCallback("" +
        		"function(info) {" +
        		" info.el.style.color = 'red';" +
        		" return info.el; " +
        		"}"
        		);

        return calendar;
    }

    @Override
    protected String createDescription() {
        return "This demo shows the integration of the Tippy tooltip library. Every calendar entry will show a tooltip based on the entry's description. The integration " +
                "is done in a simple client side extension of the base javascript class of the FullCalendar.";
    }
}
