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
    private boolean allDay;

    public Entry(String title, LocalDate date) {
        this(null, title, date);
    }

    public Entry(String id, String title, LocalDate date) {
        this(id, title, date, date);
    }

    public Entry(String title, LocalDate start, LocalDate end) {
        this(null, title, start, end);
    }

    public Entry(String id, String title, LocalDate start, LocalDate end) {
        this(id, title, start.atStartOfDay(), end.plusDays(1).atStartOfDay(), true);
    }

    public Entry(String title, LocalDateTime start, LocalDateTime end) {
        this(null, title, start, end);
    }

    public Entry(String id, String title, LocalDateTime start, LocalDateTime end) {
        this(id, title, start, end, false);
    }


    private Entry(String id, @Nonnull String title, @Nonnull LocalDateTime start, LocalDateTime end, boolean allDay) {
        Objects.requireNonNull(title);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.start = start;
        this.end = end;
        this.allDay = allDay;
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

    public boolean isAllDay() {
        return allDay;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
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
