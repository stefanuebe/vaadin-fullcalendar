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

import org.vaadin.stefan.ui.view.demos.customtimeline.AnonymousCustomViewDemo;
import org.vaadin.stefan.ui.view.demos.customtimeline.CustomViewDemo;
import org.vaadin.stefan.ui.view.demos.entryproviders.BackendEntryProviderDemo;
import org.vaadin.stefan.ui.view.demos.entryproviders.CallbackEntryProviderDemo;
import org.vaadin.stefan.ui.view.demos.entryproviders.InMemoryEntryProviderDemo;
import org.vaadin.stefan.ui.view.demos.full.FullDemo;
import org.vaadin.stefan.ui.view.demos.multimonthselection.MultiMonthCrossMonthSelectionDemo;
import org.vaadin.stefan.ui.view.demos.tooltip.DemoWithTooltip;


public class MainLayout extends AbstractLayout {
    @Override
    protected void createMenuEntries(AppNav nav) {
        addMenu(nav, FullDemo.class);
        addMenu(nav, InMemoryEntryProviderDemo.class);
        addMenu(nav, CallbackEntryProviderDemo.class);
        addMenu(nav, BackendEntryProviderDemo.class);
        addMenu(nav, DemoWithTooltip.class);
//        addMenu(nav, DemoCustomProperties.class); // TODO overhaul the demo first
//        addMenu(nav, DemoCalendarWithBackgroundEvent.class); // TODO overhaul the demo first
        addMenu(nav, CustomViewDemo.class);
        addMenu(nav, MultiMonthCrossMonthSelectionDemo.class);
//        addMenu(nav, InlineCalendarDemo.class);
    }
}

