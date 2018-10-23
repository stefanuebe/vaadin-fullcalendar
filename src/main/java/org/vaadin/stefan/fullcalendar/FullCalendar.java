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
import java.util.LinkedHashSet;
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

    private LinkedHashSet<Event> events = new LinkedHashSet<>();

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

    public void addEvent(Event event) {
        boolean added = events.add(event);
        if (added) {
            getElement().callFunction("addEvent", eventToJson(event));
        }
    }

    public void removeEvent(Event event) {
        if (events.contains(event)) {
            events.remove(event);
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
}
