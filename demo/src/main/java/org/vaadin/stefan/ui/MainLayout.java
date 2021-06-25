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
package org.vaadin.stefan;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.*;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarScheduler;

import java.util.Optional;

@Push
@PageTitle("FullCalendar Demo")
@Viewport("width=100vw, height=100vh")
public class MainView extends AppLayout implements HasUrlParameter<String> {
    public static final String ADDON_VERSION = "3.0.0";
    private final Tabs vMenu;
    private H5 vViewTitle;

    public MainView() {
        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        vMenu = createMenu();
        addToDrawer(createDrawerContent(vMenu));
    }

    private static Tab createTab(String pText, Class<? extends Component> pNavigationTarget) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(pText, pNavigationTarget));
        ComponentUtil.setData(tab, Class.class, pNavigationTarget);
        return tab;
    }

    private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setId("header");
        layout.getThemeList().set("dark", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(new DrawerToggle());
        vViewTitle = new H5();
        layout.add(vViewTitle);
        return layout;
    }

    private Component createDrawerContent(Tabs pMenu) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        VerticalLayout logoLayout = new VerticalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        Image logo = new Image("images/logo.png", "FC Flow Logo");
        logo.setWidth("50%");
        logoLayout.add(logo);
        logoLayout.add(new H4("FullCalendar Demo"));

        String text = "Version " + ADDON_VERSION + " on FC " + FullCalendar.FC_CLIENT_VERSION + " and Vaadin 14.6.3";


        Span span = new Span(text);
        span.getStyle().set("font-size", "0.7em");
        logoLayout.add(span);
        layout.add(logoLayout, pMenu);
        return layout;
    }

    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(createMenuItems());
        return tabs;
    }

    private Component[] createMenuItems() {
        return new Component[]{
                createTab("Basic Demo", Demo.class),
                createTab("Simple Demo", Demo2.class),
                createTab("Background Events", DemoCalendarWithBackgroundEvent.class),
                createTab("Tooltips", DemoWithTooltip.class),
                createTab("Extended Properties", DemoExtendedProps.class),
                createTab("28 Days Timeline", DemoTimelineWith28Days.class),
                createTab("Six Weeks Grid", DemoDayGridWeekWithSixWeeks.class)
        };
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        Component content = getContent();
        if (content instanceof HasSize) {
            ((HasSize) content).setSizeFull();
        }
        getTabForComponent(content).ifPresent(vMenu::setSelectedTab);
//        vViewTitle.setText(getCurrentPageTitle());

    }

    private Optional<Tab> getTabForComponent(Component pComponent) {
        return vMenu.getChildren().filter(tab -> ComponentUtil.getData(tab, Class.class).equals(pComponent.getClass()))
                .findFirst().map(Tab.class::cast);
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    @Override
    public void setParameter(BeforeEvent pEvent, String pParameter) {
        addToDrawer(new Label(pParameter));
    }

//    public MainView() {
//        setSpacing(false);
//        setPadding(false);
//        setMargin(false);
//        setSizeFull();
//
//        HorizontalLayout title = new HorizontalLayout();
//
//        String text = "<b>FullCalendar Demo</b> (Vaadin 14.6.3, FullCalendar addon: " + ADDON_VERSION + " " +
//                "(uses FC " + FullCalendar.FC_CLIENT_VERSION + "), " +
//        "FullCalendar Scheduler extension: " + ADDON_VERSION + " " +
//                "(uses scheduler extension libs " + FullCalendarScheduler.FC_SCHEDULER_CLIENT_VERSION + ")";
//
//        Span span = new Span();
//        span.getElement().setProperty("innerHTML", text);
//        span.getElement().getStyle().set("text-align", "center");
//        span.setWidthFull();
//
//        title.add(span);
//        title.setSpacing(false);
//        title.setWidthFull();
//
//        add(title);
//    }
}
