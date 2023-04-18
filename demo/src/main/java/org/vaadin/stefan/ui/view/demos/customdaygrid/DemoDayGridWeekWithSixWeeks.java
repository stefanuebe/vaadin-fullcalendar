package org.vaadin.stefan.ui.view.demos.customdaygrid;

import com.vaadin.flow.router.Route;

import java.io.Serial;
import java.util.Collections;

import elemental.json.JsonObject;
import org.vaadin.stefan.ui.view.AbstractCalendarView;
import org.vaadin.stefan.ui.view.CalendarViewToolbar;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

/**
 * Shows the integration of a customized calendar view.
 * @deprecated <a href="https://fullcalendar.io/docs/fixedWeekCount">https://fullcalendar.io/docs/fixedWeekCount</a>
 */
@Deprecated
@Route(value = "demodaygridsixweeks", layout = MainLayout.class)
@MenuItem(label = "Six Weeks Grid")
public class DemoDayGridWeekWithSixWeeks extends AbstractCalendarView {
    @Serial
    private static final long serialVersionUID = 1L;
    public static final int NUMBER_OF_WEEKS = 6;

    private FullCalendar calendar;

    // we create a new view with a fixed number of weeks by using the initial options
    // https://fullcalendar.io/docs/custom-views
    private static final CustomFixedDayGridWeekCalendarView CUSTOM_VIEW = new CustomFixedDayGridWeekCalendarView(NUMBER_OF_WEEKS);

    @Override
    protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
        // extend the initial options with the necessary client side settings to add the custom view
        CUSTOM_VIEW.extendInitialOptions(defaultInitialOptions);

        FullCalendar calendar = FullCalendarBuilder.create()
            .withInitialOptions(defaultInitialOptions)
            .withInitialEntries(EntryService.createRandomInstance().getEntries())
            .build();
        
        calendar.addEntryMouseEnterListener(ev -> System.out.println("Entry mouse ENTER: " + ev.getEntry().toString()));

        calendar.addEntryMouseLeaveListener(ev -> System.out.println("Entry mouse LEAVE: " + ev.getEntry().toString()));

        calendar.changeView(CUSTOM_VIEW);
        return calendar;
    }

    @Override
    protected CalendarViewToolbar createToolbar(CalendarViewToolbar.CalendarViewToolbarBuilder toolbarBuilder) {
        return super.createToolbar(toolbarBuilder
                .customViews(Collections.singletonList(CUSTOM_VIEW))
                .allowAddingRandomItemsInitially(false)
        );
    }

    @Override
    protected String createDescription() {
        return "This demo shows the integration of a customized calendar view using initial options. In this case, the monthly view is" +
                "fixed on showing six weeks per month. For additional details " +
                "on how to create custom views, please visit: https://fullcalendar.io/docs/custom-views";
    }
}