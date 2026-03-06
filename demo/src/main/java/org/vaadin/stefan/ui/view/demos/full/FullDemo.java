/*
 * Copyright 2020, Stefan Uebe
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
package org.vaadin.stefan.ui.view.demos.full;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.dialogs.DemoDialog;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarViewToolbar;
import org.vaadin.stefan.util.EntryManager;
import org.vaadin.stefan.util.ResourceManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Route(value = "", layout = MainLayout.class)
@MenuItem(label = "Playground")
public class FullDemo extends AbstractDemoView {

    /**
     * Toolbar reference — populated by {@link #createToolbar()} and used from within
     * the browser-timezone listener registered in {@link #createCalendar()}.
     * The base class calls createCalendar() first, then createToolbar(), so we use a
     * field to bridge the two lifecycle methods.
     */
    private CalendarViewToolbar toolbar;

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected FullCalendar<?> createCalendar() {
        FullCalendar<Entry> calendar = FullCalendarBuilder.create()
                .withAutoBrowserTimezone()
                .withAutoBrowserLocale()
                .withCalendarItemLimit(3)
                .withScheduler(Scheduler.GPL_V3_LICENSE_KEY)
                .build();

        calendar.addThemeVariants(FullCalendarVariant.VAADIN);

        FullCalendarScheduler<Entry> scheduler = (FullCalendarScheduler<Entry>) calendar;
        scheduler.setResourceAreaWidth("15%");
        scheduler.setSlotMinWidth("100");
        scheduler.setResourcesInitiallyExpanded(false);
        scheduler.setItemResourceEditable(false);

        calendar.setNowIndicatorShown(true);
        calendar.setTimeslotsSelectable(true);
        calendar.setNumberClickable(true);

        calendar.setSlotMinTime(LocalTime.of(7, 0));
        calendar.setSlotMaxTime(LocalTime.of(17, 0));

        calendar.setBusinessHours(
                BusinessHours.businessWeek().start(9).end(17),
                BusinessHours.of(DayOfWeek.SATURDAY).start(12).end(15),
                BusinessHours.of(DayOfWeek.SUNDAY).start(12).end(13)
        );

        // Day/week number click navigation
        calendar.addDayNumberClickedListener(event -> {
            calendar.changeView(CalendarViewImpl.TIME_GRID_DAY);
            calendar.gotoDate(event.getDate());
        });

        calendar.addWeekNumberClickedListener(event -> {
            calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);
            calendar.gotoDate(event.getDate());
        });

        // Entry click: open edit dialog (skip background entries)
        calendar.addCalendarItemClickedListener(event -> {
            Entry item = event.getItem();
            if (item.getDisplayMode() != DisplayMode.BACKGROUND
                    && item.getDisplayMode() != DisplayMode.INVERSE_BACKGROUND) {
                DemoDialog dialog = new DemoDialog(item, false);
                dialog.setSaveConsumer(e -> refreshItem(e));
                dialog.setDeleteConsumer(e -> removeItem(e));
                dialog.open();
            }
        });

        // Drag/drop scheduler variant (includes resource assignment changes)
        FullCalendarScheduler rawScheduler = scheduler;
        rawScheduler.addCalendarItemDroppedSchedulerListener(event -> {
            CalendarItemDroppedSchedulerEvent<Entry> e = (CalendarItemDroppedSchedulerEvent<Entry>) event;
            e.applyChangesOnItem();
            refreshItem(e.getItem());
        });

        // Resize
        calendar.addCalendarItemResizedListener(event -> {
            event.applyChangesOnItem();
            refreshItem(event.getItem());
        });

        // Timeslots selected (scheduler variant): open create-entry dialog
        rawScheduler.addTimeslotsSelectedSchedulerListener(event -> {
            TimeslotsSelectedSchedulerEvent<Entry> e = (TimeslotsSelectedSchedulerEvent<Entry>) event;
            ResourceEntry entry = new ResourceEntry();
            entry.setStart(e.getStart());
            entry.setEnd(e.getEnd());
            entry.setAllDay(e.isAllDay());
            entry.setCalendar(calendar);

            DemoDialog dialog = new DemoDialog(entry, true);
            dialog.setSaveConsumer(saved -> addItem((Entry) saved));
            dialog.setDeleteConsumer(deleted -> removeItem(deleted));
            dialog.open();
        });

        // Browser timezone obtained: update toolbar timezone selector
        calendar.addBrowserTimezoneObtainedListener(event -> {
            if (toolbar != null) {
                toolbar.setTimezone(event.getTimezone());
            }
        });

        createSampleData(calendar);

        return calendar;
    }

    @Override
    protected Component createToolbar() {
        @SuppressWarnings("unchecked")
        FullCalendar<Entry> typedCalendar = (FullCalendar<Entry>) getCalendar();

        toolbar = CalendarViewToolbar.builder()
                .calendar(typedCalendar)
                .editable(true)
                .settingsAvailable(true)
                .viewChangeable(true)
                .dateChangeable(true)
                .allowAddingRandomItemsInitially(true)
                .onSamplesCreated(entries -> {
                    @SuppressWarnings("unchecked")
                    var provider = (InMemoryEntryProvider<Entry>) getCalendar().getCalendarItemProvider();
                    provider.addEntries(entries);
                    provider.refreshAll();
                })
                .onSamplesRemoved(entries -> {
                    @SuppressWarnings("unchecked")
                    var provider = (InMemoryEntryProvider<Entry>) getCalendar().getCalendarItemProvider();
                    provider.removeEntries(entries);
                    provider.refreshAll();
                })
                .build();

        return toolbar;
    }

    @Override
    protected boolean isCodeDisplayEnabled() {
        return false;
    }

    @Override
    protected String createDescription() {
        return "Welcome to the FullCalendar demo playground. In this instance you see a basic set of different calendar entry types to play around with. " +
                "You may also create new ones or delete them. Have fun :)";
    }

    // -------------------------------------------------------------------------
    // InMemoryEntryProvider helpers
    // -------------------------------------------------------------------------

    private void addItem(Entry entry) {
        @SuppressWarnings("unchecked")
        InMemoryEntryProvider<Entry> provider =
                (InMemoryEntryProvider<Entry>) getCalendar().getCalendarItemProvider();
        provider.addEntry(entry);
        provider.refreshAll();
    }

    private void removeItem(Entry entry) {
        @SuppressWarnings("unchecked")
        InMemoryEntryProvider<Entry> provider =
                (InMemoryEntryProvider<Entry>) getCalendar().getCalendarItemProvider();
        provider.removeEntry(entry);
        provider.refreshAll();
    }

    @SuppressWarnings("unchecked")
    private void refreshItem(Entry entry) {
        ((InMemoryEntryProvider<Entry>) getCalendar().getCalendarItemProvider()).refreshItem(entry);
    }

    // -------------------------------------------------------------------------
    // Sample data
    // -------------------------------------------------------------------------

    private void createSampleData(FullCalendar<Entry> calendar) {
        LocalDate now = LocalDate.now();

        // Resources: 4 meeting rooms
        Resource roomRed    = ResourceManager.createResource((Scheduler) calendar, "Meeting Room Red",    "#ff0000");
        Resource roomGreen  = ResourceManager.createResource((Scheduler) calendar, "Meeting Room Green",  "green");
        Resource roomBlue   = ResourceManager.createResource((Scheduler) calendar, "Meeting Room Blue",   "blue");
        Resource roomOrange = ResourceManager.createResource((Scheduler) calendar, "Meeting Room Orange", "orange", null,
                BusinessHours.businessWeek().start(9).end(17));

        // Timed meetings with resource assignments
        EntryManager.createTimedEntry(calendar, "Meeting 1", now.withDayOfMonth(3).atTime(10, 0),   120, null, roomBlue, roomGreen, roomRed);
        EntryManager.createTimedEntry(calendar, "Meeting 2", now.withDayOfMonth(3).atTime(10, 0),   120, null, roomOrange);
        EntryManager.createTimedEntry(calendar, "Meeting 3", now.withDayOfMonth(7).atTime(11, 30),  120, null, roomRed);
        EntryManager.createTimedEntry(calendar, "Meeting 4", now.withDayOfMonth(12).atTime(9, 0),   120, null, roomGreen);
        EntryManager.createTimedEntry(calendar, "Meeting 5", now.withDayOfMonth(13).atTime(10, 0),  120, null, roomGreen);
        EntryManager.createTimedEntry(calendar, "Meeting 6", now.withDayOfMonth(17).atTime(11, 30), 120, null, roomBlue);
        EntryManager.createTimedEntry(calendar, "Meeting 7", now.withDayOfMonth(22).atTime(9, 0),   120, null, roomRed);
        EntryManager.createTimedEntry(calendar, "Meeting 8", now.withDayOfMonth(4).atTime(10, 0),   120, null);

        // Personal / colored timed entries
        EntryManager.createTimedEntry(calendar, "Grocery Store", now.withDayOfMonth(7).atTime(17, 30),  45,  "violet");
        EntryManager.createTimedEntry(calendar, "Dentist",       now.withDayOfMonth(20).atTime(11, 30), 60,  "violet");
        EntryManager.createTimedEntry(calendar, "Cinema",        now.withDayOfMonth(10).atTime(20, 30), 140, "dodgerblue");

        // All-day / multi-day entries
        EntryManager.createDayEntry(calendar, "Short trip",           now.withDayOfMonth(17), 3, "dodgerblue");
        EntryManager.createDayEntry(calendar, "John's Birthday",      now.withDayOfMonth(23), 1, "gray");
        EntryManager.createDayEntry(calendar, "This special holiday", now.withDayOfMonth(4),  1, "gray");

        // Not-editable entry
        EntryManager.createTimedEntry(calendar, "Not editable", now.withDayOfMonth(5).atTime(10, 0), 60, "lightgray")
                .setEditable(false);

        // Multiple entries on day 12 to trigger the +more link (item limit is 3)
        EntryManager.createDayEntry(calendar, "Multi 1", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 2", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 3", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 4", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 5", now.withDayOfMonth(12), 2, "tomato");

        // Background entry
        EntryManager.createDayBackgroundEntry(calendar, now.withDayOfMonth(4), 6, "#B9FFC3");

        // Recurring entry (every Sunday throughout the year)
        EntryManager.createRecurringEvents(calendar);
    }
}
