package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;

import javax.annotation.Nonnull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Flow implementation for the FullCalendar.
 *
 * Please visit <a href="https://fullcalendar.io/">https://fullcalendar.io/</a> for details about the client side
 * component, API, functionality, etc.
 */
@Tag("full-calendar")
@HtmlImport("bower_components/fullcalendar/full-calendar.html")
public class FullCalendar extends PolymerTemplate<TemplateModel> implements HasStyle, HasSize {

    /**
     * This is the default duration of an timeslot event in hours. Will be dynamic settable in a later version.
     */
    public static final int DEFAULT_TIMED_EVENT_DURATION = 1;

    /**
     * This is the default duration of an daily event in days. Will be dynamic settable in a later version.
     */
    public static final int DEFAULT_DAY_EVENT_DURATION = 1;

    private Map<String, Entry> entries = new HashMap<>();

    /**
     * Creates a new FullCalendar.
     */
    public FullCalendar() {
    }

    /**
     * Moves to the next interval (e. g. next month if current view is monthly based).
     */
    public void next() {
        getElement().callFunction("next");
    }

    /**
     * Moves to the previous interval (e. g. previous month if current view is monthly based).
     */
    public void previous() {
        getElement().callFunction("previous");
    }

    /**
     * Moves to the current interval (e. g. current month if current view is monthly based).
     */
    public void today() {
        getElement().callFunction("today");
    }

    /**
     * Sets the first day of a week to be shown by the calendar. Per default sunday.
     * <p/>
     * Might be extended / replaced later by a locale setting.
     *
     * @param firstDay first day to be shown
     * @throws NullPointerException when null is passed
     */
    public void setFirstDay(@Nonnull DayOfWeek firstDay) {
        int value = firstDay == DayOfWeek.SUNDAY ? 0 : firstDay.getValue();
        getElement().callFunction("setFirstDay", value);
    }

    // TODO active multi locales on client side
//    public void setLocale(String locale) {
//        getElement().callFunction("setLocale", locale);
//    }

    /**
     * Returns the entry with the given id. Is empty when the id is not registered.
     *
     * @param id id
     * @return entry or empty
     */
    public Optional<Entry> getEntryById(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    /**
     * Adds an entry to this calendar. Noop if the entry id is already registered
     *
     * @param entry entry
     * @return true if entry could be added
     * @throws NullPointerException when null is passed
     */
    public boolean addEntry(@Nonnull Entry entry) {
        String id = entry.getId();
        boolean containsKey = entries.containsKey(id);
        if (!containsKey) {
            entries.put(id, entry);
            getElement().callFunction("addEvent", entry.toJson());
        }

        return !containsKey;
    }

    /**
     * Updates the given entry on the client side. Will check if the id is already registered, otherwise a noop.
     *
     * @param entry entry to update
     * @throws NullPointerException when null is passed
     */
    public void updateEntry(@Nonnull Entry entry) {
        String id = entry.getId();
        boolean containsKey = entries.containsKey(id);
        if (containsKey) {
            getElement().callFunction("updateEvent", entry.toJson());
        }
    }

    /**
     * Removes the given entry. Noop if the id is not registered.
     *
     * @param entry entry
     * @throws NullPointerException when null is passed
     */
    public void removeEntry(@Nonnull Entry entry) {
        String id = entry.getId();
        if (entries.containsKey(id)) {
            entries.remove(id);
            getElement().callFunction("removeEvent", entry.toJson());
        }
    }

    /**
     * Remove all entries.
     */
    public void removeAllEntries() {
        entries.clear();
        getElement().callFunction("removeAllEvents");
    }

    /**
     * Change the view of the calendar (e. g. from monthly to weekly)
     *
     * @param view view to set
     * @throws NullPointerException when null is passed
     */
    public void changeView(@Nonnull CalendarView view) {
        Objects.requireNonNull(view);
        getElement().callFunction("changeView", view.getClientSideName());
    }

    /**
     * Switch to the intervall containing the given date (e. g. to month "October" if the "15th October ..." is passed).
     *
     * @param date date to goto
     * @throws NullPointerException when null is passed
     */
    public void gotoDate(@Nonnull LocalDate date) {
        getElement().callFunction("gotoDate", date.toString());
    }

    /**
     * Registers a listener to be informed when a day click event occurred.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addDayClickListener(@Nonnull ComponentEventListener<DayClickEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(DayClickEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when an entry click event occurred.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryClickListener(@Nonnull ComponentEventListener<EntryClickEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryClickEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when an entry resized event occurred.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryResizeListener(@Nonnull ComponentEventListener<EntryResizeEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryResizeEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when an entry dropped event occurred.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryDropListener(@Nonnull ComponentEventListener<EntryDropEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryDropEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when a view rendered event occurred.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addViewRenderedListener(@Nonnull ComponentEventListener<ViewRenderedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(ViewRenderedEvent.class, listener);
    }


}
