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
package org.vaadin.stefan.ui.layouts;

import com.vaadin.flow.component.sidenav.SideNav;
import org.vaadin.stefan.ui.view.demos.basic.BusinessHoursDemo;
import org.vaadin.stefan.ui.view.demos.basic.EventsViewDemo;
import org.vaadin.stefan.ui.view.demos.basic.I18nDemo;
import org.vaadin.stefan.ui.view.demos.basic.NativeEventListenersDemo;
import org.vaadin.stefan.ui.view.demos.basic.RecurringAndBgItemsDemo;
import org.vaadin.stefan.ui.view.demos.callbacks.CustomPropertiesDemo;
import org.vaadin.stefan.ui.view.demos.callbacks.ItemClassNamesDemo;
import org.vaadin.stefan.ui.view.demos.callbacks.ItemContentDemo;
import org.vaadin.stefan.ui.view.demos.callbacks.ItemDidMountDemo;
import org.vaadin.stefan.ui.view.demos.callbacks.ItemWillUnmountDemo;
import org.vaadin.stefan.ui.view.demos.callbacks.ResourceLabelCallbacksDemo;
import org.vaadin.stefan.ui.view.demos.callbacks.ResourceLaneCallbacksDemo;
import org.vaadin.stefan.ui.view.demos.calendaritemprovider.BackendCipDemo;
import org.vaadin.stefan.ui.view.demos.calendaritemprovider.CallbackCipDemo;
import org.vaadin.stefan.ui.view.demos.calendaritemprovider.InMemoryCipDemo;
import org.vaadin.stefan.ui.view.demos.entryproviders.BackendEntryProviderDemo;
import org.vaadin.stefan.ui.view.demos.entryproviders.CallbackEntryProviderDemo;
import org.vaadin.stefan.ui.view.demos.entryproviders.InMemoryEntryProviderDemo;
import org.vaadin.stefan.ui.view.demos.full.FullDemo;
import org.vaadin.stefan.ui.view.demos.multimonthselection.MultiMonthCrossMonthSelectionDemo;


public class MainLayout extends AbstractLayout {
    @Override
    protected void createMenuEntries(SideNav nav) {
        addSeparator(nav, "Playground");
        addMenu(nav, FullDemo.class);

        addSeparator(nav, "Basic Features");
        addMenu(nav, EventsViewDemo.class);
        addMenu(nav, RecurringAndBgItemsDemo.class);
        addMenu(nav, BusinessHoursDemo.class);
        addMenu(nav, NativeEventListenersDemo.class);
        addMenu(nav, I18nDemo.class);

        addSeparator(nav, "Calendar Item Provider");
        addMenu(nav, InMemoryCipDemo.class);
        addMenu(nav, CallbackCipDemo.class);
        addMenu(nav, BackendCipDemo.class);

        addSeparator(nav, "Entry Provider");
        addMenu(nav, InMemoryEntryProviderDemo.class);
        addMenu(nav, CallbackEntryProviderDemo.class);
        addMenu(nav, BackendEntryProviderDemo.class);

        addSeparator(nav, "Multi Month");
        addMenu(nav, MultiMonthCrossMonthSelectionDemo.class);

        addSeparator(nav, "Callbacks");
        addMenu(nav, CustomPropertiesDemo.class);
        addMenu(nav, ItemClassNamesDemo.class);
        addMenu(nav, ItemContentDemo.class);
        addMenu(nav, ItemDidMountDemo.class);
        addMenu(nav, ItemWillUnmountDemo.class);
        addMenu(nav, ResourceLabelCallbacksDemo.class);
        addMenu(nav, ResourceLaneCallbacksDemo.class);
    }
}
