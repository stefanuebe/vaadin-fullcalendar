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
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.Lumo;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.stefan.fullcalendar.ClientSideValue;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.util.Locale;

@SuppressWarnings("rawtypes")
public abstract class AbstractLayout extends AppLayout implements AfterNavigationObserver {
    public static final String ADDON_VERSION = "7.1.3-SNAPSHOT";
    private static final long serialVersionUID = -7479612679602267287L;
    private Registration currentStyleSheetRegistration;
    private Select<Theme> themeSelect;
    private SideNav sideNav;
    private Select<String> colorSchemeSelector;

    @SuppressWarnings("unchecked")
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

        addToNavbar(toggle, title, initThemeSelector(), initColorSchemeSelector());
    }

    protected void addDrawerContent() {
        H4 header = new H4("Samples");
        header.addClassName("header");

        VerticalLayout footer = new VerticalLayout();

        footer.addClassName("footer");
        footer.add(new Span("Version " + ADDON_VERSION));
        footer.add(new Html("<span>Using the FullCalendar " + FullCalendar.FC_CLIENT_VERSION + " and Vaadin 24.<br> " +
                " More information can be found <a href=\"https://vaadin.com/directory/component/full-calendar-flow\" target=\"_blank\">here</a>.</span>"));

        sideNav = new SideNav();
        createMenuEntries(sideNav);

        addToDrawer(header, new Scroller(sideNav), footer);

    }

    protected abstract void createMenuEntries(SideNav menuBuilder);

    protected void addMenu(SideNav navigation, Class<? extends Component> clazz) {
        MenuItem item = clazz.getAnnotation(MenuItem.class);
        String caption = item != null ? item.label() : String.join(" ", StringUtils.splitByCharacterTypeCamelCase(clazz.getSimpleName()));

        navigation.addItem(new SideNavItem(caption, clazz));
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        UI.getCurrent().getPage().setTitle("::: FullCalendar Demo :::");

        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        sideNav.getItems().forEach(item -> item.setQueryParameters(queryParameters));

        Theme theme = queryParameters.getSingleParameter("theme")
                .map(String::toUpperCase)
                .map(Theme::valueOf)
                .orElse(Theme.LUMO);

        themeSelect.setValue(theme);

        String scheme = queryParameters.getSingleParameter("scheme")
                .orElse("system");

        colorSchemeSelector.setValue(scheme);
    }

    private Select<String> initColorSchemeSelector() {
        colorSchemeSelector = new Select<>("", "system", "light", "dark");
        colorSchemeSelector.addValueChangeListener(event -> {
            UI ui = UI.getCurrent();

            String scheme = event.getValue();
            applyColorScheme(ui, scheme);

            if (event.isFromClient()) {
                Location activeViewLocation = ui.getActiveViewLocation();
                QueryParameters parameters = activeViewLocation.getQueryParameters();
                if (!"system".equals(scheme)) {
                    parameters = parameters.merging("scheme", scheme);
                } else {
                    parameters = parameters.excluding("scheme");
                }

                ui.navigate(activeViewLocation.getPath(), parameters);
            }
        });
        colorSchemeSelector.setValue("system");

        return colorSchemeSelector;
    }

    private void applyColorScheme(UI ui, String scheme) {
        if ("dark".equals(scheme)) {
            ui.getPage().executeJs("document.documentElement.setAttribute('theme', 'dark')");
        } else if ("light".equals(scheme)) {
            ui.getPage().executeJs("document.documentElement.removeAttribute('theme')");
        } else {
            // system: remove explicit override, let browser/OS decide
            ui.getPage().executeJs(
                    "if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {" +
                    "  document.documentElement.setAttribute('theme', 'dark');" +
                    "} else {" +
                    "  document.documentElement.removeAttribute('theme');" +
                    "}");
        }
    }

    private Select<Theme> initThemeSelector() {
        themeSelect = new Select<>("", Theme.values());
        themeSelect.addValueChangeListener(event -> {
            if(currentStyleSheetRegistration != null) {
                currentStyleSheetRegistration.remove();
                currentStyleSheetRegistration = null;
            }

            Theme theme = event.getValue();

            UI ui = UI.getCurrent();
            currentStyleSheetRegistration = ui.getPage().addStyleSheet(theme.getClientSideValue());

            if (event.isFromClient()) {
                Location activeViewLocation = ui.getActiveViewLocation();
                QueryParameters parameters = activeViewLocation.getQueryParameters();
                if (theme != Theme.LUMO) {
                    parameters = parameters.merging("theme", theme.name().toLowerCase());
                } else {
                    parameters = parameters.excluding("theme");
                }

                ui.navigate(activeViewLocation.getPath(), parameters);
            }

        });
        themeSelect.getStyle().setMarginLeft("auto");

        return themeSelect;
    }

    private enum Theme implements ClientSideValue {
        LUMO(Lumo.STYLESHEET);

        private final String value;

        Theme(String value) {
            this.value = value;
        }

        @Override
        public String getClientSideValue() {
            return value;
        }
    }
}
