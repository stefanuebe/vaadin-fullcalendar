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
        calendar.setBusinessHours(BusinessHours.businessWeek().start(LocalTime.of(9, 0)).end(LocalTime.of(17, 0)));

        // Multiple instances
        calendar.setBusinessHours(
                BusinessHours.businessWeek().start(9).end(17),
                BusinessHours.of(DayOfWeek.SATURDAY).start(10).end(14)
        );

        // Single instance for "each day from 9am to midnight"
        calendar.setBusinessHours(BusinessHours.allDays().start(9));
    }
}
