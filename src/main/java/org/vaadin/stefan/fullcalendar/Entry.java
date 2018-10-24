package org.vaadin.stefan.fullcalendar;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a event / item in the full calendar. It is named Entry here to prevent name conflicts with
 * event handling mechanisms (e.g. a component event fired by clicking something).
 * <p/>
 * <i><b>Note: </b>Creation of an entry might be exported to a builder later.</i>
 *
 */
public class Entry {
    private boolean editable;
    private final String id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean fullDayEvent;

    public Entry(String title, LocalDate date) {
        this(null, title, date.atStartOfDay(), date.atStartOfDay(), true);
    }

    public Entry(String title, LocalDate start, LocalDate end) {
        this(null, title, start.atStartOfDay(), end.atTime(23, 59, 59, 999), true);
    }

    public Entry(String title, LocalDateTime start, LocalDateTime end) {
        this(null, title, start, end, false);
    }

    public Entry(String id, String title, LocalDate date) {
        this(id, title, date.atStartOfDay(), date.atStartOfDay(), true);
    }

    public Entry(String id, String title, LocalDate start, LocalDate end) {
        this(id, title, start.atStartOfDay(), end.atTime(23, 59, 59, 999), true);
    }

    public Entry(String id, String title, LocalDateTime start, LocalDateTime end) {
        this(id, title, start, end, false);
    }


    private Entry(String id, @Nonnull String title, @Nonnull LocalDateTime start, LocalDateTime end, boolean fullDayEvent) {
        Objects.requireNonNull(title);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

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

    public LocalDateTime getEnd() {
        return end;
    }

    public boolean isFullDayEvent() {
        return fullDayEvent;
    }

    public boolean isEditable() {
        return editable;
    }

    void setEditable(boolean editable) {
        this.editable = editable;
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setStart(LocalDateTime start) {
        this.start = start;
    }

    void setEnd(LocalDateTime end) {
        this.end = end;
    }

    void setFullDayEvent(boolean fullDayEvent) {
        this.fullDayEvent = fullDayEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entry event = (Entry) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
