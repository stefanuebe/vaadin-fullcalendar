package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.option.BusinessHours;
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
        calendar.options().setBusinessHours(
                BusinessHours.businessWeek().start(LocalTime.of(9, 0)).end(LocalTime.of(17, 0)));

        // Multiple instances
        calendar.options().setBusinessHours(
                BusinessHours.businessWeek().start(9).end(17),
                BusinessHours.of(DayOfWeek.SATURDAY).start(10).end(14));

        // Single instance for "each day from 9am to midnight"
        calendar.options().setBusinessHours(BusinessHours.allDays().start(9));
    }
}
