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

    public Event(String id, String title, LocalDate date) {
        this(id, title, date.atStartOfDay(), null, true);
    }

    public Event(String id, String title, LocalDate start, LocalDate end) {
        this(id, title, start.atStartOfDay(), end.atTime(23, 59, 59, 999), true);
    }

    public Event(String id, String title, LocalDateTime start, LocalDateTime end) {
        this(id, title, start, end, false);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Event event = (Event) o;
        return fullDayEvent == event.fullDayEvent &&
                Objects.equals(id, event.id) &&
                Objects.equals(title, event.title) &&
                Objects.equals(start, event.start) &&
                Objects.equals(end, event.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, start, end, fullDayEvent);
    }
}
