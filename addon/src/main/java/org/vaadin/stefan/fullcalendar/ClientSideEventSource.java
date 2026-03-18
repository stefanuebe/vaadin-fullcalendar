/*
 * Copyright 2026, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar;

import lombok.Getter;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.UUID;

/**
 * Abstract base class for all client-managed event sources. Client-managed sources (JSON feed, Google Calendar, iCal)
 * fetch events directly in the browser — they do NOT go through the server-side {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider}.
 * <br><br>
 * Entries fetched by these sources are NOT accessible as Java {@link Entry} objects on the server side.
 * If editable drag/drop is enabled (opt-in via {@link #withEditable(boolean)}), dropped or resized entries
 * from these sources fire {@link ExternalEntryDroppedEvent} / {@link ExternalEntryResizedEvent} instead of
 * the normal entry events.
 * <br><br>
 * All client-managed sources default to {@code editable = false} (read-only). Call {@link #withEditable(boolean) withEditable(true)}
 * to opt in to drag/drop, then handle persistence yourself in the corresponding server-side events.
 *
 * @param <S> concrete subtype for fluent chaining
 */
@Getter
public abstract class ClientSideEventSource<S extends ClientSideEventSource<S>> {

    /**
     * Developer-assigned ID. Auto-generated UUID if not set.
     * Used server-side to identify which source a dropped/resized entry came from.
     */
    private String id = UUID.randomUUID().toString();

    /** Background + border color shorthand. */
    private String color;

    /** Background color override. */
    private String backgroundColor;

    /** Border color override. */
    private String borderColor;

    /** Text color override. */
    private String textColor;

    /** CSS class names applied to all entries from this source. */
    private List<String> classNames;

    /**
     * Whether entries from this source can be dragged and resized.
     * {@code null} means "use the calendar default", which the addon overrides to {@code false}
     * for all client-side sources. Set to {@code true} explicitly to opt in to drag/drop.
     */
    private Boolean editable;

    /** Whether the start time of entries can be dragged. */
    private Boolean startEditable;

    /** Whether the duration of entries can be resized. */
    private Boolean durationEditable;

    /**
     * Constraint: either the string {@code "businessHours"} or an event group id that restricts
     * when entries from this source can be dropped.
     */
    private String constraint;

    /** Whether entries from this source can overlap with other entries. */
    private Boolean overlap;

    /**
     * Display mode: {@code "block"}, {@code "list-item"}, {@code "background"},
     * {@code "inverse-background"}, or {@code "none"}.
     */
    private String display;

    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) this;
    }

    /**
     * Sets the developer-assigned id for this source.
     * Use a meaningful id if you plan to handle {@link ExternalEntryDroppedEvent} / {@link ExternalEntryResizedEvent}
     * for entries from this source.
     *
     * @param id id
     * @return this
     */
    public S withId(String id) {
        this.id = id;
        return self();
    }

    /**
     * Sets the color (background + border shorthand) for all entries from this source.
     * @param color color string (CSS color value or FullCalendar named color)
     * @return this
     */
    public S withColor(String color) {
        this.color = color;
        return self();
    }

    /**
     * Sets the background color for all entries from this source.
     * @param backgroundColor background color
     * @return this
     */
    public S withBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        return self();
    }

    /**
     * Sets the border color for all entries from this source.
     * @param borderColor border color
     * @return this
     */
    public S withBorderColor(String borderColor) {
        this.borderColor = borderColor;
        return self();
    }

    /**
     * Sets the text color for all entries from this source.
     * @param textColor text color
     * @return this
     */
    public S withTextColor(String textColor) {
        this.textColor = textColor;
        return self();
    }

    /**
     * Sets CSS class names applied to all entries from this source.
     * @param classNames list of CSS class names
     * @return this
     */
    public S withClassNames(List<String> classNames) {
        this.classNames = classNames;
        return self();
    }

    /**
     * Sets whether entries from this source can be dragged and resized. Defaults to {@code false} (read-only).
     * Set to {@code true} to opt in to drag/drop, then handle persistence in
     * {@link FullCalendar#addExternalEntryDroppedListener} / {@link FullCalendar#addExternalEntryResizedListener}.
     *
     * @param editable editable flag
     * @return this
     */
    public S withEditable(boolean editable) {
        this.editable = editable;
        return self();
    }

    /**
     * Sets whether the start time of entries can be dragged.
     * @param startEditable startEditable flag
     * @return this
     */
    public S withStartEditable(boolean startEditable) {
        this.startEditable = startEditable;
        return self();
    }

    /**
     * Sets whether the duration of entries can be resized.
     * @param durationEditable durationEditable flag
     * @return this
     */
    public S withDurationEditable(boolean durationEditable) {
        this.durationEditable = durationEditable;
        return self();
    }

    /**
     * Sets the constraint for drag/drop of entries from this source.
     * @param constraint constraint string (e.g. {@code "businessHours"} or an event group id)
     * @return this
     */
    public S withConstraint(String constraint) {
        this.constraint = constraint;
        return self();
    }

    /**
     * Sets whether entries from this source can overlap with other entries.
     * @param overlap overlap flag
     * @return this
     */
    public S withOverlap(boolean overlap) {
        this.overlap = overlap;
        return self();
    }

    /**
     * Sets the display mode for entries from this source.
     * @param display display mode string
     * @return this
     */
    public S withDisplay(String display) {
        this.display = display;
        return self();
    }

    /**
     * Serializes common properties shared by all client-side event sources into the given JSON object.
     * Subclasses should call this and then add their own properties.
     *
     * @param json target JSON object
     */
    protected void addCommonToJson(ObjectNode json) {
        json.put("id", id);
        if (color != null) json.put("color", color);
        if (backgroundColor != null) json.put("backgroundColor", backgroundColor);
        if (borderColor != null) json.put("borderColor", borderColor);
        if (textColor != null) json.put("textColor", textColor);
        if (editable != null) json.put("editable", editable);
        if (startEditable != null) json.put("startEditable", startEditable);
        if (durationEditable != null) json.put("durationEditable", durationEditable);
        if (constraint != null) json.put("constraint", constraint);
        if (overlap != null) json.put("overlap", overlap);
        if (display != null) json.put("display", display);
        if (classNames != null && !classNames.isEmpty()) {
            ArrayNode namesNode = JsonFactory.createArray();
            classNames.forEach(namesNode::add);
            json.set("classNames", namesNode);
        }
    }

    /**
     * Serializes this event source to a JSON object suitable for passing to FullCalendar's {@code eventSources} config.
     *
     * @return JSON object
     */
    public abstract ObjectNode toJson();
}
