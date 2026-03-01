package org.vaadin.stefan.fullcalendar.dataprovider;

import java.time.LocalDateTime;

/**
 * A query object providing filter parameters for a {@link CalendarItemProvider} fetch.
 * <p>
 * Start and end are nullable — a no-arg construction creates a query that fetches all items
 * (equivalent to {@link CalendarItemProvider#fetchAll()}).
 *
 * @author Stefan Uebe
 */
public class CalendarQuery {

    private final LocalDateTime start;
    private final LocalDateTime end;

    /**
     * Creates a query that fetches all items (no time-range filter).
     */
    public CalendarQuery() {
        this(null, null);
    }

    /**
     * Creates a query with the given time range. Both parameters may be null.
     *
     * @param start the start of the time range (inclusive), or null for no lower bound
     * @param end   the end of the time range (exclusive), or null for no upper bound
     */
    public CalendarQuery(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the start of the time range, or null if no lower bound.
     *
     * @return start date/time or null
     */
    public LocalDateTime getStart() {
        return start;
    }

    /**
     * Returns the end of the time range, or null if no upper bound.
     *
     * @return end date/time or null
     */
    public LocalDateTime getEnd() {
        return end;
    }
}
