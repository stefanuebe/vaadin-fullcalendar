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

    private Map<String, Event> events = new HashMap<>();

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

    public Optional<Event> getEventById(String id) {
        return Optional.ofNullable(events.get(id));
    }

    public boolean addEvent(Event event) {
        String id = event.getId();
        boolean containsKey = events.containsKey(id);
        if (!containsKey) {
            events.put(id, event);
            getElement().callFunction("addEvent", eventToJson(event));
        }

        return !containsKey;
    }

    public void removeEvent(Event event) {
        String id = event.getId();
        if (events.containsKey(id)) {
            events.remove(id);
            getElement().callFunction("removeEvent", eventToJson(event));
        }
    }

    public void removeAllEvents() {
        events.clear();
        getElement().callFunction("removeAllEvents");
    }

    private JsonObject eventToJson(Event event) {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", toJsonValue(event.getId()));
        jsonObject.put("title", toJsonValue(event.getTitle()));

        boolean fullDayEvent = event.isFullDayEvent();
        jsonObject.put("fullDay", toJsonValue(fullDayEvent));

        LocalDateTime start = event.getStart();
        LocalDateTime end = event.getEnd().orElse(null);
        jsonObject.put("start", toJsonValue(fullDayEvent ? start.toLocalDate() : start));
        jsonObject.put("end", toJsonValue(fullDayEvent && end != null ? end.toLocalDate() : end));
        jsonObject.put("editable", event.isEditable());

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

    public Registration addEventClickListener(ComponentEventListener<EventClickEvent> listener) {
        return addListener(EventClickEvent.class, listener);
    }

    public Registration addEventResizeListener(ComponentEventListener<EventResizeEvent> listener) {
        return addListener(EventResizeEvent.class, listener);
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
    public static class EventClickEvent extends ComponentEvent<FullCalendar> {

        private final Event event;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public EventClickEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.id") String id) {
            super(source, fromClient);
            this.event = source.getEventById(id).orElseThrow(IllegalArgumentException::new);
        }

        public Event getEvent() {
            return event;
        }
    }

    @DomEvent("eventResize")
    public static class EventResizeEvent extends ComponentEvent<FullCalendar> {

        private final Event event;
        private final Delta delta;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public EventResizeEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.id") String id, @EventData("event.detail.delta") JsonObject delta) {
            super(source, fromClient);
            this.event = source.getEventById(id).orElseThrow(IllegalArgumentException::new);

            int years = toInt(delta, "years");
            int months = toInt(delta, "months");
            int days = toInt(delta, "days");
            int hours = toInt(delta, "hours");
            int minutes = toInt(delta, "minutes");
            int seconds = toInt(delta, "seconds");

            this.delta = new Delta(years, months, days, hours, minutes, seconds);
        }

        private int toInt(JsonObject delta, String key) {
            return (int) delta.getNumber(key);
        }

        public Event getEvent() {
            return event;
        }

        public Delta getDelta() {
            return delta;
        }
    }

}
