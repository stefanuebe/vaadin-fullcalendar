package org.vaadin.stefan.ui.view.playground;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import java.util.Locale;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;

/**
 * Root playground view at "/" for E2E tests.
 * Provides a full calendar with pre-loaded entries, toolbar, and entry dialog.
 */
@Route("")
public class PlaygroundView extends VerticalLayout {

    private final FullCalendar calendar;
    private final InMemoryEntryProvider<Entry> entryProvider;

    public PlaygroundView() {
        setSizeFull();

        // --- Theme / Mode selects ---
        HorizontalLayout headerBar = new HorizontalLayout();
        headerBar.setAlignItems(Alignment.CENTER);

        Select<String> themeSelect = new Select<>();
        themeSelect.setItems("AURA", "LUMO", "MATERIAL");
        themeSelect.setValue("AURA");
        themeSelect.setLabel("Theme");
        headerBar.add(themeSelect);

        Select<String> modeSelect = new Select<>();
        modeSelect.setItems("SYSTEM", "DARK", "LIGHT");
        modeSelect.setValue("SYSTEM");
        modeSelect.setLabel("Mode");
        modeSelect.addValueChangeListener(e -> {
            String val = e.getValue();
            if (val != null && UI.getCurrent() != null) {
                switch (val) {
                    case "DARK" -> UI.getCurrent().getPage().executeJs("document.documentElement.setAttribute('theme', 'dark')");
                    case "LIGHT" -> UI.getCurrent().getPage().executeJs("document.documentElement.setAttribute('theme', 'light')");
                    default -> UI.getCurrent().getPage().executeJs("document.documentElement.removeAttribute('theme')");
                }
            }
        });
        headerBar.add(modeSelect);

        add(headerBar);

        // --- Calendar ---
        calendar = FullCalendarBuilder.create()
                .withEntryLimit(3)
                .build();
        calendar.addThemeVariants(FullCalendarVariant.LUMO);
        calendar.setOption(FullCalendar.Option.WEEK_NUMBERS, true);
        calendar.setOption(FullCalendar.Option.SELECTABLE, true);
        calendar.setLocale(Locale.ENGLISH);

        // --- Entry provider with pre-loaded entries ---
        entryProvider = new InMemoryEntryProvider<>();
        createInitialEntries();
        calendar.setEntryProvider(entryProvider);

        // --- Toolbar ---
        PlaygroundToolbar toolbar = new PlaygroundToolbar(
                calendar,
                this::onEntriesCreated,
                this::onEntriesRemoved,
                true,   // showRandomEntries
                false   // show1kEntries
        );
        add(toolbar);
        setHorizontalComponentAlignment(Alignment.CENTER, toolbar);

        // --- Event listeners ---
        calendar.addEntryClickedListener(this::onEntryClick);
        calendar.addTimeslotsSelectedListener(this::onTimeslotsSelected);
        calendar.addEntryDroppedListener(this::onEntryDropped);
        calendar.addEntryResizedListener(this::onEntryResized);

        add(calendar);
        setFlexGrow(1, calendar);
        setHorizontalComponentAlignment(Alignment.STRETCH, calendar);
    }

    private void createInitialEntries() {
        // "Meeting" — timed entry for today
        Entry meeting = new Entry();
        meeting.setTitle("Meeting");
        meeting.setStart(LocalDate.now().atTime(10, 0));
        meeting.setEnd(LocalDate.now().atTime(11, 0));
        entryProvider.addEntry(meeting);

        // "Short trip" — all-day, multi-day
        Entry shortTrip = new Entry();
        shortTrip.setTitle("Short trip");
        shortTrip.setStart(LocalDate.now().plusDays(2));
        shortTrip.setEnd(LocalDate.now().plusDays(5));
        shortTrip.setAllDay(true);
        shortTrip.setColor("green");
        entryProvider.addEntry(shortTrip);

        // "This special holiday" — all-day, 1 day
        Entry holiday = new Entry();
        holiday.setTitle("This special holiday");
        holiday.setStart(LocalDate.now().plusDays(4));
        holiday.setAllDay(true);
        holiday.setColor("gray");
        entryProvider.addEntry(holiday);

        // "sunday event" — recurring weekly on Sundays (exact title, no year prefix!)
        Entry sundayEvent = new Entry();
        sundayEvent.setTitle("sunday event");
        sundayEvent.setRecurringDaysOfWeek(DayOfWeek.SUNDAY);
        sundayEvent.setRecurringStartDate(LocalDate.now().withDayOfYear(1));
        sundayEvent.setRecurringEndDate(LocalDate.now().withDayOfYear(1).plusYears(1));
        sundayEvent.setRecurringStartTime(LocalTime.of(9, 0));
        sundayEvent.setRecurringEndTime(LocalTime.of(10, 0));
        entryProvider.addEntry(sundayEvent);

        // "Multi 1" through "Multi 5" — ALL on day 12 for "+more" link
        for (int i = 1; i <= 5; i++) {
            Entry multi = new Entry();
            multi.setTitle("Multi " + i);
            multi.setStart(LocalDate.now().withDayOfMonth(12));
            multi.setEnd(LocalDate.now().withDayOfMonth(12).plusDays(3));
            multi.setAllDay(true);
            entryProvider.addEntry(multi);
        }
    }

    private void onEntryClick(EntryClickedEvent event) {
        PlaygroundDialog dialog = new PlaygroundDialog(event.getEntry(), false);
        dialog.setSaveConsumer(this::onEntryChanged);
        dialog.setDeleteConsumer(e -> onEntriesRemoved(Collections.singletonList(e)));
        dialog.open();
    }

    private void onTimeslotsSelected(TimeslotsSelectedEvent event) {
        Entry entry = new Entry();
        entry.setStart(event.getStart());
        entry.setEnd(event.getEnd());
        entry.setAllDay(event.isAllDay());
        entry.setCalendar(event.getSource());

        PlaygroundDialog dialog = new PlaygroundDialog(entry, true);
        dialog.setSaveConsumer(e -> onEntriesCreated(Collections.singletonList(e)));
        dialog.open();
    }

    private void onEntryDropped(EntryDroppedEvent event) {
        event.applyChangesOnEntry();
        entryProvider.refreshItem(event.getEntry());
    }

    private void onEntryResized(EntryResizedEvent event) {
        event.applyChangesOnEntry();
        entryProvider.refreshItem(event.getEntry());
    }

    private void onEntriesCreated(Collection<Entry> entries) {
        entryProvider.addEntries(entries);
        entryProvider.refreshAll();
    }

    private void onEntriesRemoved(Collection<Entry> entries) {
        if (entries.isEmpty()) {
            // "Remove all entries" — clear everything
            entryProvider.removeAllEntries();
        } else {
            entryProvider.removeEntries(entries);
        }
        entryProvider.refreshAll();
    }

    private void onEntryChanged(Entry entry) {
        entryProvider.refreshItem(entry);
    }
}
