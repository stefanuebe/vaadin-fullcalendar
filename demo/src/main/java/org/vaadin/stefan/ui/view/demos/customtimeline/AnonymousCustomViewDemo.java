package org.vaadin.stefan.ui.view.demos.customtimeline;

import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.view.AbstractSchedulerView;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

// not a registered route since it shall only provide some sample code for the "old" way of adding custom views
public class AnonymousCustomViewDemo extends AbstractSchedulerView {

    private SomeCalendarView calendarView;

    @Override
    protected FullCalendar createCalendar(ObjectNode defaultInitialOptions) {
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

        public ObjectNode getInitialOptions() {
            ObjectNode initialOptions = JsonFactory.createObject();

            ObjectNode durationHolder = JsonFactory.createObject();
            durationHolder.set("days", JsonFactory.create(numberOfDays));

            ObjectNode customViewHolder = JsonFactory.createObject();
            customViewHolder.set("type", JsonFactory.create("resourceTimeline"));
            customViewHolder.set("duration", durationHolder);

            ObjectNode viewsHolder = JsonFactory.createObject();
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