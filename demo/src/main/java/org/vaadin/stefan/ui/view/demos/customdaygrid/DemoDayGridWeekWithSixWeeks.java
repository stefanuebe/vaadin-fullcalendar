package org.vaadin.stefan.ui.view.demos.customdaygrid;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import elemental.json.Json;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;
import elemental.json.impl.JreJsonObject;
import org.vaadin.stefan.AbstractCalendarView;
import org.vaadin.stefan.CalendarViewToolbar;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.model.Header;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterItem;
import org.vaadin.stefan.fullcalendar.model.HeaderFooterPart;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

/**
 * Shows the integration of a customized calendar view.
 */
@Route(value = "demodaygridsixweeks", layout = MainLayout.class)
@PageTitle("FC with Six Weeks Grid")
@MenuItem(label = "Six Weeks Grid")
public class DemoDayGridWeekWithSixWeeks extends AbstractCalendarView {
    private static final long serialVersionUID = 1L;
    public static final int NUMBER_OF_WEEKS = 6;

    private FullCalendar calendar;

    // we create a new view with a fixed number of weeks by using the initial options
    // https://fullcalendar.io/docs/custom-views
    private static final CustomDayGridWeekCalendarView CUSTOM_VIEW = new CustomDayGridWeekCalendarView(NUMBER_OF_WEEKS);

    @Override
    protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
        // extend the initial options with the necessary client side settings to add the custom view
        CUSTOM_VIEW.extendInitialOptions(defaultInitialOptions);

        FullCalendar calendar = FullCalendarBuilder.create()
                .withInitialOptions(defaultInitialOptions)
                .withInitialEntries(EntryService.createRandomInstance().getEntries())
                .build();

        calendar.changeView(CUSTOM_VIEW);
        return calendar;
    }

    @Override
    protected CalendarViewToolbar createToolbar(CalendarViewToolbar.CalendarViewToolbarBuilder toolbarBuilder) {
        toolbarBuilder.customCalendarViews(Collections.singletonList(CUSTOM_VIEW));
        return super.createToolbar(toolbarBuilder);
    }

    @Override
    protected String createDescription() {
        return "This demo shows the integration of a customized calendar view using initial options. For additional details " +
                "on how to create custom views, please visit: https://fullcalendar.io/docs/custom-views";
    }
}