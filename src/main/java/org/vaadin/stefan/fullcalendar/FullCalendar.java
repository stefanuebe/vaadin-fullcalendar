package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A Designer generated component for the full-calendar.html template.
 * <p>
 * Designer will add and remove fields with @Id mappings but
 * does not overwrite or otherwise change this file.
 */
@Tag("full-calendar")
@HtmlImport("fullcalendar/full-calendar.html")
public class FullCalendar extends PolymerTemplate<TemplateModel> {

    private Map<String, Entry> entries = new HashMap<>();

    /**
     * Creates a new FullCalendar.
     */
    public FullCalendar() {
    }

    public void next() {
        getElement().callFunction("next");
    }

    public void previous() {
        getElement().callFunction("previous");
    }

    public void today() {
        getElement().callFunction("today");
    }

    public Optional<Entry> getEntryById(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    public boolean addEntry(Entry entry) {
        String id = entry.getId();
        boolean containsKey = entries.containsKey(id);
        if (!containsKey) {
            entries.put(id, entry);
            getElement().callFunction("addEvent", entryToJson(entry));
        }

        return !containsKey;
    }

    public void removeEntry(Entry entry) {
        String id = entry.getId();
        if (entries.containsKey(id)) {
            entries.remove(id);
            getElement().callFunction("removeEvent", entryToJson(entry));
        }
    }

    public void removeAllEntries() {
        entries.clear();
        getElement().callFunction("removeAllEvents");
    }

    private JsonObject entryToJson(Entry entry) {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", toJsonValue(entry.getId()));
        jsonObject.put("title", toJsonValue(entry.getTitle()));

        boolean fullDayEvent = entry.isFullDayEvent();
        jsonObject.put("fullDay", toJsonValue(fullDayEvent));

        LocalDateTime start = entry.getStart();
        LocalDateTime end = entry.getEnd().orElse(null);
        jsonObject.put("start", toJsonValue(fullDayEvent ? start.toLocalDate() : start));
        jsonObject.put("end", toJsonValue(fullDayEvent && end != null ? end.toLocalDate() : end));
        jsonObject.put("editable", entry.isEditable());

        return jsonObject;
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

    public Registration addDayClickListener(ComponentEventListener<DayClickEvent> listener) {
        return addListener(DayClickEvent.class, listener);
    }

    public Registration addEntryClickListener(ComponentEventListener<EntryClickEvent> listener) {
        return addListener(EntryClickEvent.class, listener);
    }

    public Registration addEntryResizeListener(ComponentEventListener<EntryResizeEvent> listener) {
        return addListener(EntryResizeEvent.class, listener);
    }

    @DomEvent("dayClick")
    public static class DayClickEvent extends ComponentEvent<FullCalendar> {

        private LocalDateTime clickedDateTime;
        private LocalDate clickedDate;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public DayClickEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.date") String date) {
            super(source, fromClient);

            try {
                clickedDateTime = LocalDateTime.parse(date);
            } catch (DateTimeParseException e) {
                clickedDate = LocalDate.parse(date);
            }
        }

        public Optional<LocalDate> getClickedDate() {
            return Optional.ofNullable(clickedDate);
        }

        public Optional<LocalDateTime> getClickedDateTime() {
            return Optional.ofNullable(clickedDateTime);
        }

        public boolean isTimeSlotEvent() {
            return clickedDateTime != null;
        }
    }

    @DomEvent("eventClick")
    public static class EntryClickEvent extends ComponentEvent<FullCalendar> {

        private final Entry entry;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public EntryClickEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.id") String id) {
            super(source, fromClient);
            this.entry = source.getEntryById(id).orElseThrow(IllegalArgumentException::new);
        }

        public Entry getEntry() {
            return entry;
        }
    }

    @DomEvent("eventResize")
    public static class EntryResizeEvent extends ComponentEvent<FullCalendar> {

        private final Entry entry;
        private final Delta delta;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public EntryResizeEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.id") String id, @EventData("event.detail.delta") JsonObject delta) {
            super(source, fromClient);
            this.entry = source.getEntryById(id).orElseThrow(IllegalArgumentException::new);

            this.delta = Delta.fromJson(delta);
            entry.setEnd(this.delta.applyOn(entry.getEnd().orElseGet(entry::getStart)));
        }


        /**
         * Returns the modified event. The end date of this event has already been updated by the delta.
         * @return event
         */
        public Entry getEntry() {
            return entry;
        }

        public Delta getDelta() {
            return delta;
        }
    }


}
