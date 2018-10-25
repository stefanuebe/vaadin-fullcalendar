package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

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

    JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", toJsonValue(getId()));
        jsonObject.put("title", toJsonValue(getTitle()));

        boolean fullDayEvent = isAllDay();
        jsonObject.put("allDay", toJsonValue(fullDayEvent));

        LocalDateTime start = getStart();
        LocalDateTime end = getEnd();
        jsonObject.put("start", toJsonValue(fullDayEvent ? start.toLocalDate() : start));
        jsonObject.put("end", toJsonValue(fullDayEvent ? end.toLocalDate() : end));
        jsonObject.put("editable", isEditable());

        return jsonObject;
    }

    /**
     * Updates this instance with the content of the given object. Properties, that are not part of the object will
     * be unmodified. Same for the id. Properties in the object, that do not match with this instance will be
     * ignored.
     * @param object json object / change set
     */
    void update(JsonObject object) {
        String id = object.getString("id");
        if (!this.id.equals(id)) {
            throw new IllegalArgumentException("IDs are not matching.");
        }

        updateString(object, "title", this::setTitle);
        updateBoolean(object, "editable", this::setEditable);
        updateBoolean(object, "allDay", this::setAllDay);
        updateDateTime(object, "start", this::setStart);
        updateDateTime(object, "end", this::setEnd);
    }

    private void updateString(JsonObject object, String key, Consumer<String> setter) {
        if (object.hasKey(key)) {
            setter.accept(object.getString(key));
        }
    }

    private void updateBoolean(JsonObject object, String key, Consumer<Boolean> setter) {
        if (object.hasKey(key)) {
            setter.accept(object.getBoolean(key));
        }
    }


    private void updateDateTime(JsonObject object, String key, Consumer<LocalDateTime> setter) {
        if (object.hasKey(key)) {
            String string = object.getString(key);

            LocalDateTime dateTime;
            try {
                dateTime = LocalDateTime.parse(string);
            } catch (DateTimeParseException e) {
                dateTime = LocalDate.parse(string).atStartOfDay();
            }

            setter.accept(dateTime);
        }
    }

    private JsonValue toJsonValue(Object value) {
        if (value == null) {
            return Json.createNull();
        }
        if (value instanceof Boolean) {
            return Json.create((Boolean) value);
        }
        return Json.create(String.valueOf(value));
    }

    @Override
    public String toString() {
        return "Entry{" +
                "title='" + title + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", allDay=" + allDay +
                ", editable=" + editable +
                ", id='" + id + '\'' +
                '}';
    }
}
