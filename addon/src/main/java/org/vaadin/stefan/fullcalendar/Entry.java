package org.vaadin.stefan.fullcalendar;

import elemental.json.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
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
    private String color;
    private String description;
    private Rendering rendering = Rendering.NORMAL;

    private FullCalendar calendar;

    public Entry(String id, String title, LocalDateTime start, LocalDateTime end, boolean allDay, boolean editable, String color, String description) {
        this(id);

        this.title = title;
        this.start = start;
        this.end = end;
        this.allDay = allDay;
        this.editable = editable;
        this.description = description;

        setColor(color);
    }

    public Entry(String id, String title, LocalDateTime start, LocalDateTime end, boolean allDay, boolean editable, String color, String description, Rendering rendering) {
        this(id);

        this.title = title;
        this.start = start;
        this.end = end;
        this.allDay = allDay;
        this.editable = editable;
        this.description = description;
        this.rendering = rendering;

        setColor(color);
    }

    /**
     * Empty instance.
     */
    public Entry() {
        this(null);
        this.editable = true;
    }

    protected Entry(String id) {
        this.id = id != null ? id : UUID.randomUUID().toString();
    }

    /**
     * Sets the calendar instance to be used internally. There is NO automatic removal or add when the calendar changes.
     * @param calendar calendar instance
     */
    protected void setCalendar(FullCalendar calendar) {
        this.calendar = calendar;
    }

    /**
     * Returns the calendar instance of this entry. Is empty when not yet added to a calendar.
     *
     * @return calendar instance
     */
    public Optional<FullCalendar> getCalendar() {
        return Optional.ofNullable(calendar);
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color == null || color.trim().isEmpty() ? null : color;
    }

    public Rendering getRendering() {
        return rendering;
    }

    public void setRendering(Rendering rendering) {
        this.rendering = rendering;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Entry event = (Entry) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    protected JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", JsonUtils.toJsonValue(getId()));
        jsonObject.put("title", JsonUtils.toJsonValue(getTitle()));

        boolean fullDayEvent = isAllDay();
        jsonObject.put("allDay", JsonUtils.toJsonValue(fullDayEvent));

        LocalDateTime start = getStart();
        LocalDateTime end = getEnd();
        jsonObject.put("start", JsonUtils.toJsonValue(fullDayEvent ? start.toLocalDate() : start));
        jsonObject.put("end", JsonUtils.toJsonValue(fullDayEvent ? end.toLocalDate() : end));
        jsonObject.put("editable", isEditable());
        jsonObject.put("color", JsonUtils.toJsonValue(getColor()));

        return jsonObject;
    }

    /**
     * Updates this instance with the content of the given object. Properties, that are not part of the object or are
     * of an invalid type will be unmodified. Same for the id. Properties in the object, that do not match with this
     * instance will be ignored.
     * @param object json object / change set
     */
    protected void update(JsonObject object) {
        String id = object.getString("id");
        if (!this.id.equals(id)) {
            throw new IllegalArgumentException("IDs are not matching.");
        }

        JsonUtils.updateString(object, "title", this::setTitle);
        JsonUtils.updateBoolean(object, "editable", this::setEditable);
        JsonUtils.updateBoolean(object, "allDay", this::setAllDay);
        JsonUtils.updateDateTime(object, "start", this::setStart);
        JsonUtils.updateDateTime(object, "end", this::setEnd);
        JsonUtils.updateString(object, "color", this::setColor);
    }

    @Override
    public String toString() {
        return "Entry{" +
                "title='" + title + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", allDay=" + allDay +
                ", color='" + color + '\'' +
                ", description='" + description + '\'' +
                ", editable=" + editable +
                ", id='" + id + '\'' +
                ", calendar=" + calendar +
                '}';
    }

    /**
     * Gets the description of an event.
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of an event.
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Constants for rendering of an event.
     */
    public enum Rendering {
        /**
         * Renders as normal entry.
         */
        NORMAL(null),

        /**
         * Renders as background entry (marks the area of the entry interval).
         */
        BACKGROUND("background"),

        /**
         * Renders as inversed background entry (marks everything except the entry interval).
         */
        INVERSE_BACKGROUND("inverse-background");

        private final String clientSideName;

        Rendering(String clientSideName) {
            this.clientSideName = clientSideName;
        }

        public String getClientSideName() {
            return clientSideName;
        }
    }
}
