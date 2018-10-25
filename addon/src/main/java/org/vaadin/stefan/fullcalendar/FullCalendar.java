package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A Designer generated component for the full-calendar.html template.
 * <p>
 * Designer will add and remove fields with @Id mappings but
 * does not overwrite or otherwise change this file.
 */
@Tag("full-calendar")
@HtmlImport("bower_components/fullcalendar/full-calendar.html")
public class FullCalendar extends PolymerTemplate<TemplateModel> implements HasStyle, HasSize {

    /**
     * This is the default duration of an event in hours. Will be dynamic settable in a later version.
     */
    public static final int DEFAULT_TIMED_EVENT_DURATION = 1;
    public static final int DEFAULT_DAY_EVENT_DURATION = 1;

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
            getElement().callFunction("addEvent", entry.toJson());
        }

        return !containsKey;
    }

    public void updateEntry(Entry entry) {
        String id = entry.getId();
        boolean containsKey = entries.containsKey(id);
        if (containsKey) {
            getElement().callFunction("updateEvent", entry.toJson());
        }
    }

    public void removeEntry(Entry entry) {
        String id = entry.getId();
        if (entries.containsKey(id)) {
            entries.remove(id);
            getElement().callFunction("removeEvent", entry.toJson());
        }
    }

    public void removeAllEntries() {
        entries.clear();
        getElement().callFunction("removeAllEvents");
    }

    public void changeView(CalendarView view) {
        Objects.requireNonNull(view);
        getElement().callFunction("changeView", view.getClientSideName());
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
