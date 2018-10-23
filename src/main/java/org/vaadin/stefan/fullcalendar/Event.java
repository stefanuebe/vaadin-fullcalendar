package org.vaadin.stefan.fullcalendar;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a event / item in the full calendar.
 * <p/>
 * <i><b>Note: </b>Creation of an event might be exported to a builder later.</i>
 *
 */
public class Event {
    private final boolean editable;
    private final String id;
    private final String title;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final boolean fullDayEvent;

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

        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.start = start;
        this.end = end;
        this.fullDayEvent = fullDayEvent;
        this.editable = true;
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

    public boolean isEditable() {
        return editable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
