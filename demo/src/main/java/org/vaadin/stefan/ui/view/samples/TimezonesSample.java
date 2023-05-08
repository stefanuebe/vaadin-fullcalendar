package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.Timezone;

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
        calendar.setTimezone(tzBerlinGermany); // will rerender the client side and show all times 1-2 hours "later".

        // We can also reset the timezone to default.
        calendar.setTimezone(Timezone.UTC);

        // We can also read the browsers timezone, after the component has been attached to the client side.
        // There are other ways to obtain the browser's timezone, so you are not obliged to use the listener.
        calendar.addBrowserTimezoneObtainedListener(event -> calendar.setTimezone(event.getTimezone()));

        // If you want to let the calendar obtain the browser time zone automatically, you may simply use the builder.
        // In that case as soon as the client connected, it will set it's timezone in the server side instance.
        FullCalendarBuilder.create().withAutoBrowserTimezone().build();

        // Entries use internally utc to define times. The LocalDateTime and Instant methods setStart/End have the same effect.
        entry.setStart(Instant.now()); // UTC
        entry.setEnd(LocalDateTime.now()); // UTC

        // Entry provides some additional convenience methods to handle the current calendar's timezone's offset, e.g. to allow easy
        // integration into edit forms.
        calendar.setTimezone(tzBerlinGermany); // times are now 1-2 hours "ahead" (depending on daylight saving)
        entry.setStart(LocalDate.of(2000, 1, 1).atStartOfDay());

        LocalDateTime utcStart = entry.getStart(); // will be 2000-01-01, 00:00
        LocalDateTime offsetStart = entry.getStartWithOffset(); // will be 2000-01-01, 01:00

        // ... modify the offset start, for instance in a date picker
        // e.g. modifiedOffsetStart = offsetStart.plusHours(5);
        LocalDateTime modifiedOffsetStart = offsetStart.plusHours(5);

        entry.setStartWithOffset(modifiedOffsetStart); // automatically takes care of conversion back to utc
        utcStart = entry.getStart(); // will be 2000-01-01, 04:00
        offsetStart = entry.getStartWithOffset(); // will be 2000-01-01, 05:00
    }
}
