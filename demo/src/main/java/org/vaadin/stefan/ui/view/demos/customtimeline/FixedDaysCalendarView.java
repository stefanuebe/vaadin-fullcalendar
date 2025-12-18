package org.vaadin.stefan.ui.view.demos.customtimeline;

import org.vaadin.stefan.fullcalendar.CalendarView;

import org.vaadin.stefan.fullcalendar.CustomCalendarView;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.ui.view.demos.HasIntervalLabel;
import tools.jackson.databind.node.ObjectNode;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FixedDaysCalendarView implements CustomCalendarView, HasIntervalLabel {
	private final int numberOfDays;

    public FixedDaysCalendarView(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    @Override
    public String getClientSideValue() {
        return "fixedDaysResourceTimeline";
    }

    @Override
    public String getName() {
        return "Fixed Days Resource Timeline";
    }

    @Override
    public ObjectNode getViewSettings() {
        ObjectNode days = JsonFactory.createObject();
        days.put("days", numberOfDays);

        ObjectNode baseSettings = JsonFactory.createObject();
        baseSettings.put("type", "resourceTimeline");
        baseSettings.set("duration", days);
        return baseSettings;
    }

    @Override
    public String formatIntervalLabel(LocalDate intervalStart, Locale locale) {
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return pattern.format(intervalStart) + " - " + pattern.format(intervalStart.plusDays(numberOfDays - 1));
    }
}
