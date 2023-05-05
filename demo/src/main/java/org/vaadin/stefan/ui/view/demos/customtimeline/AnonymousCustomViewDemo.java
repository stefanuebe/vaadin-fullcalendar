package org.vaadin.stefan.ui.view.demos.customtimeline;

import com.vaadin.flow.router.Route;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;
import elemental.json.impl.JreJsonObject;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractSchedulerView;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

import java.util.List;

@Route(value = "anonymous-custom-view", layout = MainLayout.class)
@MenuItem(label = "Anonymous Custom View")
public class AnonymousCustomViewDemo extends AbstractSchedulerView {

    private SomeCalendarView calendarView;

    @Override
    protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
        calendarView = new SomeCalendarView(28);
        FullCalendar calendar = FullCalendarBuilder.create()
                .withScheduler(Scheduler.GPL_V3_LICENSE_KEY)
                .withAutoBrowserLocale()
                .withInitialOptions(calendarView.getInitialOptions())
                .build();

        List<Entry> entries = EntryService.createRandomInstance().getEntries();
        calendar.getEntryProvider().asInMemory().addEntries(entries);

        return calendar;
    }

    @Override
    protected void postConstruct(FullCalendar calendar) {
        calendar.changeView(calendarView);
    }

    @Override
    protected String createDescription() {
        return "This demo shows how the calendar handles the \"old\" way of adding custom views. " +
                "The custom view is the same as in the Custom View Demo, but the view is created using the initial options.";
    }

    @Override
    protected String createTitle() {
        return "Anonymous Custom View Demo";
    }


    private static class SomeCalendarView implements CalendarView {
        private final int numberOfDays;

        public SomeCalendarView(int numberOfDays) {
            this.numberOfDays = numberOfDays;
        }

        @Override
        public String getClientSideValue() {
            return "fixedDaysResourceTimelineAnonymous";
        }

        public JreJsonObject getInitialOptions() {
            JsonFactory factory = new JreJsonFactory();
            JreJsonObject initialOptions = new JreJsonObject(factory);

            JreJsonObject durationHolder = new JreJsonObject(factory);
            durationHolder.set("days", factory.create(numberOfDays));

            JreJsonObject customViewHolder = new JreJsonObject(factory);
            customViewHolder.set("type", factory.create("resourceTimeline"));
            customViewHolder.set("duration", durationHolder);

            JreJsonObject viewsHolder = new JreJsonObject(factory);
            viewsHolder.set(getClientSideValue(), customViewHolder);

            initialOptions.set("views", viewsHolder);

            return initialOptions;
        }

        @Override
        public String getName() {
            return "Fixed Days Resource Timeline (anonymous)";
        }
    }

    @Override
    protected boolean isToolbarViewChangeable() {
        return false;
    }
}