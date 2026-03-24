package org.vaadin.stefan.ui.layouts;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RoutePrefix;

/**
 * Minimal layout for E2E test views.
 * Provides the {@code /test/} route prefix so all test views are served under
 * {@code /test/<view-route>}. No navigation UI is needed — Playwright accesses
 * views directly by URL.
 */
@RoutePrefix("test")
public class TestLayout extends VerticalLayout implements RouterLayout {
    public TestLayout() {
        addClassName("test-layout");
        setSizeFull();
    }
}
