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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.theme.lumo.Lumo;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.io.Serial;
import java.util.Locale;

@CssImport("./app-layout-styles.css")
public abstract class AbstractLayout extends AppLayout implements AfterNavigationObserver {
    public static final String ADDON_VERSION = "24.0.0-SNAPSHOT";
    @Serial
    private static final long serialVersionUID = -7479612679602267287L;

    public AbstractLayout() {
        selectCurrentLocale();

        setPrimarySection(Section.DRAWER);
        addHeaderContent();
        addDrawerContent();
    }


    protected void selectCurrentLocale() {
        Locale locale = (Locale) VaadinRequest.getCurrent().getWrappedSession().getAttribute("locale");
        if (locale == null) {
            locale = UI.getCurrent().getLocale();
            VaadinRequest.getCurrent().getWrappedSession().setAttribute("locale", locale);
        } else
            UI.getCurrent().setLocale(locale);
    }

    protected Component generateTitle(String title) {
        Span span = new Span(title);

        span.setWidthFull();
        span.getStyle()
                .set("margin-left", "var(--app-layout-menu-toggle-button-padding)")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("text-align", "center");

        return span;
    }

    protected void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        Component title = generateTitle("FullCalendar " + ADDON_VERSION + " for Vaadin Flow");

        addToNavbar(true, toggle, title);
    }

    protected void addDrawerContent() {
        H4 header = new H4("Samples");
        header.addClassName("header");

        VerticalLayout footer = new VerticalLayout();

        Button themeToggle = new Button("Toggle dark theme", click -> {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();

            if (themeList.contains(Lumo.DARK)) {
                themeList.remove(Lumo.DARK);
            } else {
                themeList.add(Lumo.DARK);
            }
        });
        themeToggle.setWidthFull();

        Div footerText = new Div(new Html("<span>Using the FullCalendar library " + FullCalendar.FC_CLIENT_VERSION + " and Vaadin 24.0.0.rc1. " +
                "More information can be found <a href=\"https://vaadin.com/directory/component/full-calendar-flow\" target=\"_blank\">here</a>.</span>"));

        footer.addClassName("footer");
        footer.add(themeToggle, footerText);

        AppNav nav = new AppNav();
        createMenuEntries(nav);

        addToDrawer(header, new Scroller(nav), footer);

    }

    protected abstract void createMenuEntries(AppNav menuBuilder);

    protected void addMenu(AppNav navigation, Class<? extends Component> clazz) {
        MenuItem item = clazz.getAnnotation(MenuItem.class);
        String caption = item != null ? item.label() : String.join(" ", StringUtils.splitByCharacterTypeCamelCase(clazz.getSimpleName()));

        navigation.addItem(new AppNavItem(caption, clazz, "la"));
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        UI.getCurrent().getPage().setTitle("::: FullCalendar Demo :::");
    }
}

