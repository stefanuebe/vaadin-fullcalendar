package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonObject;

import javax.annotation.Nonnull;
import java.time.Instant;
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
    private Instant start;
    private Instant end;
    private boolean allDay;
    private String color;
    private String description;
    private RenderingMode renderingMode = RenderingMode.NORMAL;

    private FullCalendar calendar;

    public Entry(String id, String title, Instant start, Instant end, boolean allDay, boolean editable, String color, String description) {
        this(id);

        this.title = title;
        this.start = start;
        this.end = end;
        this.allDay = allDay;
        this.editable = editable;
        this.description = description;

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

    protected JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", JsonUtils.toJsonValue(getId()));
        jsonObject.put("title", JsonUtils.toJsonValue(getTitle()));

        boolean fullDayEvent = isAllDay();
        jsonObject.put("allDay", JsonUtils.toJsonValue(fullDayEvent));

        Timezone timezone = calendar != null ? calendar.getTimezone() : Timezone.UTC;

        jsonObject.put("start", JsonUtils.toJsonValue(timezone.formatWithZoneId(getStart())));
        jsonObject.put("end", JsonUtils.toJsonValue(timezone.formatWithZoneId(getEnd())));
        jsonObject.put("editable", isEditable());
        jsonObject.put("color", JsonUtils.toJsonValue(getColor()));
        jsonObject.put("rendering", JsonUtils.toJsonValue(getRenderingMode()));

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
        JsonUtils.updateDateTime(object, "start", this::setStart, calendar.getTimezone().getZoneId());
        JsonUtils.updateDateTime(object, "end", this::setEnd, calendar.getTimezone().getZoneId());
        JsonUtils.updateString(object, "color", this::setColor);
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

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
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

    public void setStart(Instant start) {
        this.start = start;
    }

    public void setEnd(Instant end) {
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

    /**
     * Returns the rendering mode of this entry. Never null.
     * @return rendering mode
     */
    public RenderingMode getRenderingMode() {
        return renderingMode;
    }

    /**
     * Sets the rendering of this entry. Default is {@link RenderingMode#NORMAL}
     * @param renderingMode rendering
     * @throws NullPointerException when passing null
     */
    public void setRenderingMode(@Nonnull RenderingMode renderingMode) {
        Objects.requireNonNull(renderingMode);
        this.renderingMode = renderingMode;
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
                ", rendering=" + renderingMode +
                '}';
    }

    /**
     * Constants for rendering of an event.
     */
    public enum RenderingMode implements ClientSideValue {
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

        RenderingMode(String clientSideName) {
            this.clientSideName = clientSideName;
        }

        @Override
        public String getClientSideValue() {
            return clientSideName;
        }

    }
}
