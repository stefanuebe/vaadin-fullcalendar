package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.option.Timezone;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Stefan Uebe
 */
public class TimezonesSample extends AbstractSample {

    private Entry entry;

    @Override
    protected void buildSample(FullCalendar calendar) {
        // FC allows to show entries in a specifc timezone. Setting a timezone only affects the client side
        // and might be interesting, when editing those entries in some kind of edit form

        Timezone tzBerlinGermany = new Timezone(ZoneId.of("Europe/Berlin"));
        calendar.options().setTimeZone(tzBerlinGermany); // will rerender the client side and show all times 1-2 hours "later".

        // We can also set it back to UTC.
        calendar.options().setTimeZone(Timezone.UTC);

        // We can also read the browsers timezone, after the component has been attached to the client side.
        // There are other ways to obtain the browser's timezone, so you are not obliged to use the listener.
        calendar.addBrowserTimezoneObtainedListener(event -> calendar.options().setTimeZone(event.getTimezone()));

        // Or let the calendar follow the browser's time zone in one line.
        calendar.options().setTimeZone(Timezone.LOCAL);

        // Entries use internally utc to define times. The LocalDateTime and Instant methods setStart/End have the same effect.
        entry.setStart(Instant.now()); // UTC
        entry.setEnd(LocalDateTime.now()); // UTC

        // Entry provides some additional convenience methods to handle a timezone's offset, e.g. to allow easy
        // integration into edit forms. Pass the calendar's time zone to work with the times as the calendar shows them.
        calendar.options().setTimeZone(tzBerlinGermany); // times are now 1-2 hours "ahead" (depending on daylight saving)
        entry.setStart(LocalDate.of(2000, 1, 1).atStartOfDay());

        LocalDateTime utcStart = entry.getStart(); // will be 2000-01-01, 00:00
        LocalDateTime offsetStart = entry.getStartWithOffset(calendar.options().getTimeZone()); // will be 2000-01-01, 01:00

        // ... modify the offset start, for instance in a date picker
        // e.g. modifiedOffsetStart = offsetStart.plusHours(5);
        LocalDateTime modifiedOffsetStart = offsetStart.plusHours(5);

        entry.setStartWithOffset(modifiedOffsetStart, calendar.options().getTimeZone()); // takes care of the conversion back to utc
        utcStart = entry.getStart(); // will be 2000-01-01, 04:00
        offsetStart = entry.getStartWithOffset(calendar.options().getTimeZone()); // will be 2000-01-01, 05:00
    }
}
