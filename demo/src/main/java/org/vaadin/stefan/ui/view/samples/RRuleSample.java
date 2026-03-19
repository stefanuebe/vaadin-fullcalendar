package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.RRule;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Demonstrates RFC 5545 recurrence rules via the {@link RRule} fluent builder.
 * <p>
 * Shows weekly, monthly (last-Friday-of-month), bi-weekly with exclusion dates,
 * and a raw RFC 5545 string pattern.
 */
public class RRuleSample extends AbstractSample {

    @Override
    protected void buildSample(FullCalendar calendar) {
        // Weekly on Monday, Wednesday, and Friday (all-day: dtstart uses LocalDate, so no time component)
        Entry standup = new Entry();
        standup.setTitle("Weekly Standup");
        standup.setRrule(RRule.weekly()
                .byWeekday(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
                .dtstart(LocalDate.now().withDayOfMonth(1))
                .until(LocalDate.now().plusMonths(3)));

        // Last Friday of each month.
        // byWeekday("-1fr") uses RFC 5545 notation: "-1fr" = last Friday,
        // "1mo" = first Monday, "-2tu" = second-to-last Tuesday.
        Entry review = new Entry();
        review.setTitle("Monthly Review");
        review.setRrule(RRule.monthly().byWeekday("-1fr")
                .dtstart(LocalDate.now().withDayOfMonth(1)));

        // Bi-weekly on Tuesday — exclude one occurrence
        Entry planning = new Entry();
        planning.setTitle("Bi-weekly Planning");
        planning.setRrule(RRule.weekly()
                .byWeekday(DayOfWeek.TUESDAY)
                .interval(2)
                .dtstart(LocalDate.now().with(DayOfWeek.TUESDAY)));
        // Exclude one specific occurrence by date (comma-separated ISO dates for multiple exclusions)
        planning.setExdate(LocalDate.now().plusWeeks(4).with(DayOfWeek.TUESDAY).toString());

        calendar.getEntryProvider().asInMemory().addEntries(standup, review, planning);
    }
}
