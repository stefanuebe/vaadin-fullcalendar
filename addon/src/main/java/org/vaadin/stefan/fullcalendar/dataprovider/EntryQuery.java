package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.*;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.option.ExtendedLocalTime;
import org.vaadin.stefan.fullcalendar.option.Timezone;

import java.time.Duration;
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
        this(start != null ? LocalDateTime.ofInstant(start, Timezone.ZONE_ID_UTC) : null,
                end != null ? LocalDateTime.ofInstant(end, Timezone.ZONE_ID_UTC) : null,
                allDay);
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
                    LocalDateTime lastRecurrence = lastRecurrence(e);

                    // recurring entries that have no end may go indefinitely into the future, so we return
                    // them always
                    return lastRecurrence == null || lastRecurrence.isAfter(start);
                }

                LocalDateTime entryEnd = e.getEnd();
                if (entryEnd == null) {
                    // No explicit end (e.g. all-day single-day entry): defer to the
                    // upper-bound check below; do not exclude here.
                    return e.getStart() != null;
                }
                return entryEnd.isAfter(start);
            });
        }

        if (end != null) {
            stream = stream.filter(e -> {
                if (e.isRecurring()) {
                    LocalDateTime firstRecurrence = firstRecurrence(e);

                    // recurring entries that have no start may go indefinitely into the past, so we return
                    // them always
                    return firstRecurrence == null || firstRecurrence.isBefore(end);
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

    /**
     * The first point in time the recurrence of the entry may cover, used by the filter as its lower boundary. It is
     * the recurring start date at the recurring start time, or at the start of that day when there is no start time.
     * {@code null} when there is no start date, so the recurrence has no lower boundary.
     */
    private static LocalDateTime firstRecurrence(Entry e) {
        LocalDate startDate = e.getRecurringStartDate();
        if (startDate == null) {
            return null;
        }

        ExtendedLocalTime startTime = e.getRecurringStartTime();
        if (startTime == null) {
            return startDate.atStartOfDay();
        }

        return startDate.atStartOfDay().plusHours(startTime.getHour()).plusMinutes(startTime.getMinute());
    }

    /**
     * The last point in time the recurrence of the entry may cover, used by the filter as its upper boundary. It is
     * the recurring end date at the recurring end time, or the last second of that day when there is no end time.
     * A positive recurring duration is added, so an occurrence that lasts beyond the end date is found.
     * {@code null} when there is no end date, so the recurrence has no upper boundary. The client treats the end date
     * as exclusive, so the filter may let through a recurring entry that ends the day before the range. The client
     * does not show it.
     */
    private static LocalDateTime lastRecurrence(Entry e) {
        LocalDate endDate = e.getRecurringEndDate();
        if (endDate == null) {
            return null;
        }

        ExtendedLocalTime endTime = e.getRecurringEndTime();
        LocalDateTime last = endTime == null
                ? endDate.plusDays(1).atStartOfDay().minusSeconds(1)
                : endDate.atStartOfDay().plusHours(endTime.getHour()).plusMinutes(endTime.getMinute());

        Duration duration = e.getRecurringDuration();
        return duration != null && duration.isPositive() ? last.plus(duration) : last;
    }

    public enum AllDay {
        BOTH,
        ALL_DAY_ONLY,
        TIMED_ONLY;
    }
}
