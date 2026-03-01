package org.vaadin.stefan.fullcalendar;

import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Typed wrapper around the raw JSON delta sent by the client on drag/drop/resize.
 * Provides typed accessors for the most common changed properties.
 * <p>
 * For custom or scheduler-specific properties, use {@link #getRawJson()} to access
 * the underlying JSON object directly.
 *
 * @author Stefan Uebe
 */
public class CalendarItemChanges {

    private final ObjectNode jsonDelta;

    /**
     * Creates a new instance wrapping the given JSON delta.
     *
     * @param jsonDelta the JSON object containing changed properties
     * @throws NullPointerException if jsonDelta is null
     */
    public CalendarItemChanges(ObjectNode jsonDelta) {
        this.jsonDelta = Objects.requireNonNull(jsonDelta, "jsonDelta");
    }

    /**
     * Returns the changed start date/time, if present in the delta.
     *
     * @return optional containing the new start, or empty if start was not changed
     */
    public Optional<LocalDateTime> getChangedStart() {
        return parseDateTime("start");
    }

    /**
     * Returns the changed end date/time, if present in the delta.
     *
     * @return optional containing the new end, or empty if end was not changed
     */
    public Optional<LocalDateTime> getChangedEnd() {
        return parseDateTime("end");
    }

    /**
     * Returns the changed allDay flag, if present in the delta.
     *
     * @return optional containing the new allDay value, or empty if allDay was not changed
     */
    public Optional<Boolean> getChangedAllDay() {
        if (jsonDelta.hasNonNull("allDay")) {
            return Optional.of(jsonDelta.get("allDay").asBoolean());
        }
        return Optional.empty();
    }

    /**
     * Returns the raw JSON delta object for accessing custom or extension-specific properties.
     *
     * @return the original JSON delta
     */
    public ObjectNode getRawJson() {
        return jsonDelta;
    }

    private Optional<LocalDateTime> parseDateTime(String property) {
        if (jsonDelta.hasNonNull(property)) {
            return Optional.of(JsonUtils.parseClientSideDateTime(jsonDelta.get(property).asString()));
        }
        return Optional.empty();
    }
}
