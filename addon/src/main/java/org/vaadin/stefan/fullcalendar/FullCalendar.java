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
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

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
    private Map<Option, Serializable> options = new HashMap<>();

    // used to keep the amount of timeslot selected listeners. when 0, then selectable option is auto removed
    private int timeslotsSelectedListenerCount;

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
     * Sets a option for this instance.
     * @param option option
     * @param value value
     */
    protected void setOption(Option option, Serializable value) {
        options.put(option, value);
        getElement().callFunction("setOption", option.getOptionKey(), value);
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
        setOption(Option.FIRST_DAY, value);
    }

    /**
     * Sets the calendar's height to a fixed amount of pixels.
     * @param heightInPixels height in pixels (e.g. 300)
     */
    public void setHeight(int heightInPixels) {
        setOption(Option.HEIGHT, heightInPixels);
    }

    /**
     * Sets the calendar's height to be calculated from parents height. Please be aware, that a block parent with
     * relative height (e. g. 100%) might not work properly. In this case use flex layout or set a fixed height for
     * the parent or the calendar.
     */
    public void setHeightByParent() {
        setOption(Option.HEIGHT, "parent");
    }

    /**
     * Sets the calendar's height to be calculated automatically. In current implementation this means by the calendars
     * width-height-ratio.
     */
    public void setHeightAuto() {
        setOption(Option.HEIGHT, "auto");
    }

    /**
     * Set if timeslots might be selected by the user. Please see also documentation of {@link #addTimeslotsSelectedEventListener(ComponentEventListener)}.
     *
     * @param selectable activate selectable
     */
    public void setTimeslotsSelectable(boolean selectable) {
        setOption(Option.SELECTABLE, selectable);
    }

    /**
     * Should the calendar show week numbers (when available for the current view)?
     *
     * @param weekNumbersVisible week numbers visible
     */
    public void setWeekNumbersVisible(boolean weekNumbersVisible) {
        setOption(Option.WEEK_NUMBERS, weekNumbersVisible);
    }

    /**
     * Sets the locale to be used. If invoked for the first time it will load additional language scripts.
     * @param locale locale
     */
    public void setLocale(Locale locale) {
        setOption(Option.LOCALE, locale.toString());
    }

    /**
     * Force the client side instance to re-render it's content.
     */
    public void render() {
        getElement().callFunction("render");
    }

    /**
     * Registers a listener to be informed when a timeslot click event occurred.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addTimeslotClickedListener(@Nonnull ComponentEventListener<TimeslotClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotClickedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when an entry click event occurred.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryClickedListener(@Nonnull ComponentEventListener<EntryClickedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryClickedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when an entry resized event occurred.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryResizedListener(@Nonnull ComponentEventListener<EntryResizedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryResizedEvent.class, listener);
    }

    /**
     * Registers a listener to be informed when an entry dropped event occurred.
     * @param listener listener
     * @return registration to remove the listener
     * @throws NullPointerException when null is passed
     */
    public Registration addEntryDroppedListener(@Nonnull ComponentEventListener<EntryDroppedEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(EntryDroppedEvent.class, listener);
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

    /**
     * Registers a listener to be informed when the user selected a range of timeslots.
     * <p/>
     * Adding the first listener
     * to this component will activate the selectable option. Removing the last instance will automatically disable it.
     * You may override that via setTimestlotsSelectable().
     * <p/>
     * You should also deactivate timeslot clicked listeners since both events will get fired when the user only selects
     * one timeslot / day.
     * @param listener listener
     * @return registration to remove the listener
     */
    public Registration addTimeslotsSelectedEventListener(@Nonnull ComponentEventListener<TimeslotsSelectedEvent> listener) {
        Objects.requireNonNull(listener);

        Registration registration = addListener(TimeslotsSelectedEvent.class, listener);

        if(timeslotsSelectedListenerCount++ == 0) {
            setTimeslotsSelectable(true);
        }

        return () -> {
            registration.remove();
            if (--timeslotsSelectedListenerCount == 0) {
                setTimeslotsSelectable(false);
            }
        };
    }

    enum Option {
        DEFAULT_TIMES_EVENT_DURATION("defaultTimedEventDuration"),
        FIRST_DAY("firstDay"),
        HEIGHT("height"),
        LOCALE("locale"),
        SELECTABLE("selectable"),
        WEEK_NUMBERS("weekNumbers"),
        ;

        private final String optionKey;

        Option(String optionKey) {
            this.optionKey = optionKey;
        }

        String getOptionKey() {
            return optionKey;
        }
    }

}
