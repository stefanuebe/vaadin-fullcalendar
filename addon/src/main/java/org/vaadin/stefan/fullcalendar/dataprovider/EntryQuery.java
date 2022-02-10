package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.*;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.Timezone;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A class to provide filter parameters for an {@link EntryProvider} fetch query.
 * @author Stefan Uebe
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class EntryQuery {

//    private final FullCalendar source; // needed?
    private LocalDateTime start;
    private LocalDateTime end;

    @NonNull
    private AllDay allDay = AllDay.BOTH;

    public EntryQuery(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public EntryQuery(Instant start, Instant end) {
        this(start, end, AllDay.BOTH);
    }

    public EntryQuery(Instant start, Instant end, AllDay allDay) {
        this(start != null ? LocalDateTime.ofInstant(start, Timezone.ZONE_ID_UTC) : null, end != null ? LocalDateTime.ofInstant(end, Timezone.ZONE_ID_UTC) : null, allDay);
    }

    /**
     * Convenience implementation to filter a stream based on this query.
     * <p></p>
     * Simply applies the filter to the given stream and returns a stream containing only entries matching it.
     * Entries, that are "crossing" the time range border will be included in the stream.
     * <p></p>
     * Returns the same stream, when this filter is empty.
     *
     * @param stream stream
     * @param <T>    type
     * @return filtered stream
     */
    public <T extends Entry> Stream<T> applyFilter(Stream<T> stream) {
        if (start == null && end == null && allDay == AllDay.BOTH) {
            return stream;
        }

        if (start != null) {
            stream = stream.filter(e -> {
                if (e.isRecurring()) {
                    LocalDateTime recurringEnd = e.getRecurringEnd();

                    // recurring events, that have no end may go indefinitely to the future. So we return
                    // them always
                    return recurringEnd == null || recurringEnd.isAfter(start);
                }

                return e.getEnd() != null && e.getEnd().isAfter(start);
            });
        }

        if (end != null) {
            stream = stream.filter(e -> {
                if (e.isRecurring()) {
                    LocalDateTime recurringStart = e.getRecurringStart();

                    // recurring events, that have no start may go indefinitely to the past. So we return
                    // them always
                    return recurringStart == null || recurringStart.isBefore(end);
                }

                return e.getStart() != null && e.getStart().isBefore(end);
            });
        }


        if (allDay != AllDay.BOTH) {
            Predicate<T> allDayFilter = Entry::isAllDay;
            if (allDay == AllDay.TIMED_ONLY) {
                allDayFilter = allDayFilter.negate();
            }

            stream = stream.filter(allDayFilter);
        }

        return stream;
    }

    public enum AllDay {
        BOTH,
        ALL_DAY_ONLY,
        TIMED_ONLY;
    }
}
