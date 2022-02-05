package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.*;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class FullCalendarQuery {

    private LocalDateTime from;
    private LocalDateTime to;

    @NonNull
    private AllDay allDay = AllDay.BOTH;

    /**
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
        if (from == null && to == null && (allDay == null || allDay == AllDay.BOTH)) {
            return stream;
        }

        return stream.filter(item -> {
                    if (from != null && item.getStart().isAfter(from)) {
                        return false;
                    }

                    if (to != null && item.getEnd().isBefore(to)) {
                        return false;
                    }

//                    if (allDay) {
//
//                    }

                    return true;
                }
        );
    }

    public enum AllDay {
        BOTH,
        ALL_DAY_ONLY,
        TIMED_ONLY;
    }
}
