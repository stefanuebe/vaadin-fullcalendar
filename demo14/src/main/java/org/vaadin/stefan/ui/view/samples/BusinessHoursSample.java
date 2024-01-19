package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.BusinessHours;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * @author Stefan Uebe
 */
public class BusinessHoursSample extends AbstractSample {
    @Override
    protected void buildSample(FullCalendar calendar) {
        // Single instance for "normal" business week (mo-fr)
        calendar.setBusinessHours(new BusinessHours(LocalTime.of(9, 0), LocalTime.of(17, 0),BusinessHours.DEFAULT_BUSINESS_WEEK));

        // Multiple instances
        calendar.setBusinessHours(
                new BusinessHours(LocalTime.of(9, 0), LocalTime.of(17, 0),BusinessHours.DEFAULT_BUSINESS_WEEK),
                new BusinessHours(LocalTime.of(12, 0), LocalTime.of(15, 0), DayOfWeek.SATURDAY)
        );

        // Single instance for "each day from 9am to midnight"
        calendar.setBusinessHours(new BusinessHours(LocalTime.of(9, 0)));
    }
}
