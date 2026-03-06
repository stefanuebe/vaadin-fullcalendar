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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
    protected void createMenuEntries(VerticalLayout container) {
        SideNav playground = addSection(container, "Playground", true);
        addMenu(playground, FullDemo.class);

        SideNav basic = addSection(container, "Basic Features");
        addMenu(basic, EventsViewDemo.class);
        addMenu(basic, RecurringAndBgItemsDemo.class);
        addMenu(basic, BusinessHoursDemo.class);
        addMenu(basic, NativeEventListenersDemo.class);
        addMenu(basic, I18nDemo.class);

        SideNav cip = addSection(container, "Calendar Item Provider");
        addMenu(cip, InMemoryCipDemo.class);
        addMenu(cip, CallbackCipDemo.class);
        addMenu(cip, BackendCipDemo.class);

        SideNav ep = addSection(container, "Entry Provider");
        addMenu(ep, InMemoryEntryProviderDemo.class);
        addMenu(ep, CallbackEntryProviderDemo.class);
        addMenu(ep, BackendEntryProviderDemo.class);

        SideNav multiMonth = addSection(container, "Multi Month");
        addMenu(multiMonth, MultiMonthCrossMonthSelectionDemo.class);

        SideNav callbacks = addSection(container, "Callbacks");
        addMenu(callbacks, CustomPropertiesDemo.class);
        addMenu(callbacks, ItemClassNamesDemo.class);
        addMenu(callbacks, ItemContentDemo.class);
        addMenu(callbacks, ItemDidMountDemo.class);
        addMenu(callbacks, ItemWillUnmountDemo.class);
        addMenu(callbacks, ResourceLabelCallbacksDemo.class);
        addMenu(callbacks, ResourceLaneCallbacksDemo.class);
    }
}
