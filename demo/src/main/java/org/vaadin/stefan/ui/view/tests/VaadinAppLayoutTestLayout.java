package org.vaadin.stefan.ui.view.tests;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Span;

/**
 * @author Stefan Uebe
 */
public class VaadinAppLayoutTestLayout extends AppLayout {

    public VaadinAppLayoutTestLayout() {
        DrawerToggle toggle = new DrawerToggle();

        addToDrawer(new Span("Drawer"));
        addToNavbar(toggle, new Span("Navbar"));
        setPrimarySection(Section.NAVBAR);
    }
}
