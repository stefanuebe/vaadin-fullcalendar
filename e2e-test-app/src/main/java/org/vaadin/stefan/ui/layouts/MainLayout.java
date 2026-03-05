package org.vaadin.stefan.ui.layouts;

import com.vaadin.flow.component.sidenav.SideNav;
import org.vaadin.stefan.ui.view.demos.entryproviders.BackendEntryProviderDemo;
import org.vaadin.stefan.ui.view.demos.entryproviders.CallbackEntryProviderDemo;
import org.vaadin.stefan.ui.view.demos.entryproviders.InMemoryEntryProviderDemo;
import org.vaadin.stefan.ui.view.demos.full.FullDemo;

/**
 * Minimal layout for the E2E test application.
 * Only includes views that are required by the E2E test suite.
 */
public class MainLayout extends AbstractLayout {
    @Override
    protected void createMenuEntries(SideNav nav) {
        addMenu(nav, FullDemo.class);
        addMenu(nav, InMemoryEntryProviderDemo.class);
        addMenu(nav, CallbackEntryProviderDemo.class);
        addMenu(nav, BackendEntryProviderDemo.class);
    }
}
