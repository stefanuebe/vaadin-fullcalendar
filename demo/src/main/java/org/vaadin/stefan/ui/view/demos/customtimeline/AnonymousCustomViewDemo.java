package org.vaadin.stefan.ui.view.demos.customtimeline;

import com.vaadin.flow.component.UI;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.view.AbstractSchedulerView;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;
import elemental.json.JsonObject;

import java.util.List;

// not a registered route since it shall only provide some sample code for the "old" way of adding custom views
public class AnonymousCustomViewDemo extends AbstractSchedulerView {

    private SomeCalendarView calendarView;

    @Override
    protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
        calendarView = new SomeCalendarView(28);
        FullCalendarScheduler calendar = new FullCalendarScheduler(calendarView.getInitialOptions());
        calendar.setOption(FullCalendarScheduler.SchedulerOption.LICENSE_KEY, Scheduler.GPL_V3_LICENSE_KEY);
        calendar.setLocale(UI.getCurrent().getLocale());

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

        public JsonObject getInitialOptions() {
            JsonObject initialOptions = JsonFactory.createObject();

            JsonObject durationHolder = JsonFactory.createObject();
            durationHolder.put("days", JsonFactory.create(numberOfDays));

            JsonObject customViewHolder = JsonFactory.createObject();
            customViewHolder.put("type", JsonFactory.create("resourceTimeline"));
            customViewHolder.put("duration", durationHolder);

            JsonObject viewsHolder = JsonFactory.createObject();
            viewsHolder.put(getClientSideValue(), customViewHolder);

            initialOptions.put("views", viewsHolder);

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