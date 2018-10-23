package org.vaadin.stefan.fullcalendar;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a event / item in the full calendar.
 */
public class Event {
    private String id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean fullDayEvent;

    public Event(String title, LocalDate date) {
        this(null, title, date.atStartOfDay(), null, true);
    }

    public Event(String title, LocalDate start, LocalDate end) {
        this(null, title, start.atStartOfDay(), end.atTime(23, 59, 59, 999), true);
    }

    public Event(String title, LocalDateTime start, LocalDateTime end) {
        this(null, title, start, end, false);
    }

    private Event(String id, @Nonnull String title, @Nonnull LocalDateTime start, LocalDateTime end, boolean fullDayEvent) {
        Objects.requireNonNull(title);
        Objects.requireNonNull(start);
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
        this.fullDayEvent = fullDayEvent;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public Optional<LocalDateTime> getEnd() {
        return Optional.ofNullable(end);
    }

    public boolean isFullDayEvent() {
        return fullDayEvent;
    }
}
