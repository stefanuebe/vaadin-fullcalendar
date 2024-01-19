package org.vaadin.stefan.ui.view.demos.multimonthselection;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractCalendarView;
import org.vaadin.stefan.ui.view.demos.entryproviders.CallbackEntryProviderDemo;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Stefan Uebe
 */
@Route(value = "inline-calendar", layout = MainLayout.class)
@MenuItem(label = "Inline Calendar")
@JsModule("./multi-month-selection-utils.js")
@CssImport("./inline-calendar.css")
public class InlineCalendarDemo extends VerticalLayout {

    // basic constructor with full sized items
    public InlineCalendarDemo() {
        setSizeFull();
        setAlignItems(Alignment.STRETCH);

        FullCalendar calendar = FullCalendarBuilder.create()
                .withAutoBrowserLocale()
                .build();
        calendar.addClassName("inline-calendar");

        // activate cross month selection
        calendar.setTimeslotsSelectable(false); // important
        calendar.addAttachListener(event -> // refire things, when reattached
            calendar.getElement()
                .executeJs("window.Vaadin.Flow.multiMonthCrossSelectionUtils.register(this.calendar)")
                .then(jsonValue -> calendar.changeView(CalendarViewImpl.MULTI_MONTH))
        );

        addAndExpand(calendar);

        InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();
        calendar.addTimeslotsSelectedListener(event -> {
            // fetch entries from the entryprovider using the event start and end date and map them to their start date
            LocalDateTime start = event.getStart();
            LocalDateTime end = event.getEnd();

            System.out.println("start: " + start);
            System.out.println("end: " + end);

            Map<LocalDate, Entry> map = entryProvider
                    .fetch(start, end)
                    .collect(Collectors.toMap(entry -> entry.getStartAsLocalDate(), entry -> entry));

            // calculate amount of days between event start and end
            int days = Period.between(start.toLocalDate(), end.toLocalDate()).getDays();
            if (days == map.size()) {
                // unmark all days that are covered by the event period
                entryProvider.removeEntries(map.values());
                entryProvider.refreshAll();
            } else {
                // iterate over the event period and create an entry for each day, that is not in the map
                LocalDate startDate = start.toLocalDate();
                LocalDate endDate = end.toLocalDate();
                LocalDate date = startDate;

                while (date.isBefore(endDate)) {
                    if (!map.containsKey(date)) {
                        Entry entry = new Entry();
                        entry.setStart(date.atStartOfDay());
                        entry.setEnd(date.plusDays(1).atStartOfDay());
                        entry.setAllDay(true);
                        entry.setDisplayMode(DisplayMode.BACKGROUND);
                        entryProvider.addEntry(entry);
                    }

                    date = date.plusDays(1);
                }

                entryProvider.refreshAll();
            }

            calendar.clearSelection();
        });

    }

}
