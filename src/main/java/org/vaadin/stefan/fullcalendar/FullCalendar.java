package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

import java.time.LocalDateTime;
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

    /**
     * This is the default duration of an event in hours. Will be dynamic settable in a later version.
     */
    public static final int DEFAULT_TIMED_EVENT_DURATION = 1;
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

    public void updateEntry(Entry entry) {
        String id = entry.getId();
        boolean containsKey = entries.containsKey(id);
        if (containsKey) {
            getElement().callFunction("updateEvent", entryToJson(entry));
        }
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

        boolean fullDayEvent = entry.isAllDay();
        jsonObject.put("fullDay", toJsonValue(fullDayEvent));

        LocalDateTime start = entry.getStart();
        LocalDateTime end = entry.getEnd();
        jsonObject.put("start", toJsonValue(fullDayEvent ? start.toLocalDate() : start));
        jsonObject.put("end", toJsonValue(fullDayEvent ? end.toLocalDate() : end));
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

    public Registration addEntryDropListener(ComponentEventListener<EntryDropEvent> listener) {
        return addListener(EntryDropEvent.class, listener);
    }


}
