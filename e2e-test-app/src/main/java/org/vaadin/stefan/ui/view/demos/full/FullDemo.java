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

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.dialogs.DemoDialog;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.view.AbstractSchedulerView;
import org.vaadin.stefan.util.EntryManager;
import org.vaadin.stefan.util.ResourceManager;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

@Route(value = "", layout = MainLayout.class)
@org.vaadin.stefan.ui.menu.MenuItem(label = "Playground")
public class FullDemo extends AbstractSchedulerView {

    private Popover popup;

    @Override
    protected FullCalendar<Entry> createCalendar(ObjectNode initialOptions) {
//        initialOptions.put("eventContent",
//                "function(arg, createElement) {" +
//                " console.warn('hello');" +
//                "  return 'WORLD';" +
//                "}");

        FullCalendar<Entry> calendar = FullCalendarBuilder.create()
                .withAutoBrowserTimezone()
                .withAutoBrowserLocale()
//                .withCalendarItemContent("""
//                        function(arg) {
//                            let italicEl = document.createElement('i');
//                            if (arg.event.getCustomProperty('isUrgent', false)) {
//                                italicEl.innerHTML = 'urgent event';
//                            } else {
//                                italicEl.innerHTML = 'normal event';
//                            }
//                            let arrayOfDomNodes = [ italicEl ];
//                            return { domNodes: arrayOfDomNodes }
//                        }""")
                .withInitialOptions(initialOptions)
                .withCalendarItemLimit(3)
                .withScheduler(Scheduler.GPL_V3_LICENSE_KEY)
                .build();

        FullCalendarScheduler<Entry> scheduler = (FullCalendarScheduler<Entry>) calendar;
        scheduler.setResourceAreaWidth("15%");
        scheduler.setSlotMinWidth("100");
        scheduler.setResourcesInitiallyExpanded(false);

        calendar.setNowIndicatorShown(true);
        calendar.setTimeslotsSelectable(true);

        calendar.setNumberClickable(true);
        calendar.addDayNumberClickedListener(event -> {
            calendar.changeView(CalendarViewImpl.TIME_GRID_DAY);
            calendar.gotoDate(event.getDate());
        });

        calendar.addWeekNumberClickedListener(event -> {
            calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);
            calendar.gotoDate(event.getDate());
        });

        // initally change the view and go to a specific date - attention: this will not fire listeners as the client side is not initialized yet
//        calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);
//        calendar.gotoDate(LocalDate.of(2023, Month.JUNE, 1));

        calendar.setSlotMinTime(LocalTime.of(7, 0));
        calendar.setSlotMaxTime(LocalTime.of(17, 0));


        calendar.addBrowserTimezoneObtainedListener(event -> {
            getToolbar().setTimezone(event.getTimezone());
        });

        scheduler.setItemResourceEditable(false);

//        calendar.setEntryClassNamesCallback("function(arg) {\n" +
//                "    return [ 'hello','world' ]\n" +
//                "}");
//        calendar.setItemContentCallback("" +
//                "function(arg, createElement) {" +
//                " console.warn('hello');" +
//                "  return 'WORLD';" +
//                "}");



//        calendar.addItemNativeEventListener("mouseover", "e => info.el.style.opacity = '0.5'");
//        calendar.addItemNativeEventListener("mouseout", "e => info.el.style.opacity = ''");
//        calendar.addItemNativeEventListener("contextmenu", "e => console.warn('just a context menu event')");

        calendar.addItemNativeEventListener("contextmenu",
                "e => {" +
                "   e.preventDefault(); " +
                "   this.el.parentElement.$server.openContextMenu(info.event.id);" +
                "}");
//        calendar.setItemDidMountCallback("""
//                function(info) {
//                    info.el.id = "entry-" + info.event.id;
//                }""");

//
//        scheduler.setResourceLabelContentCallback(
//                "function(arg, createElement) {" +
//                " console.warn('hello');" +
//                "  return 'Hello';" +
//                "}");
//
//        scheduler.setResourceLaneContentCallback(
//                "function(arg, createElement) {" +
//                " console.warn('world');" +
//                "  return 'World';" +
//                "}");

        createTestEntries(calendar);

//        calendar.changeView(CalendarViewImpl.MULTI_MONTH);
//        calendar.gotoDate(LocalDate.now().plusYears(1));


        return calendar;
    }

    private void initPopup() {
        if (popup == null) {
            popup = new Popover();
//            popup.setFocusTrap(true);
            add(popup);
        }
    }

    @ClientCallable
    public void openContextMenu(String id) {
        initPopup();

        popup.removeAll();

        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Option A", "Option B", "Option C");
        listBox.addValueChangeListener(event -> {
            Notification.show("Selected " + event.getValue());
            popup.close();
        });

        popup.add(listBox);
        popup.setFor("entry-" + id);

        popup.open();
    }

    private void createTestEntries(FullCalendar<Entry> calendar) {
        LocalDate now = LocalDate.now();

        // Resources — keep a small set for Scheduler views
        Resource roomRed = ResourceManager.createResource((Scheduler) calendar, "Room Red", "#ff0000");
        Resource roomBlue = ResourceManager.createResource((Scheduler) calendar, "Room Blue", "blue");
        Resource roomGreen = ResourceManager.createResource((Scheduler) calendar, "Room Green", "green");

        // Timed entries
        EntryManager.createTimedEntry(calendar, "Meeting", now.withDayOfMonth(15).atTime(10, 0), 120, null, roomRed);
        EntryManager.createTimedEntry(calendar, "Meeting 8", now.withDayOfMonth(20).atTime(10, 0), 120, null);
        EntryManager.createTimedEntry(calendar, "Timed Today", now.atTime(10, 0), 120, "mediumseagreen");

        // All-day entries
        EntryManager.createDayEntry(calendar, "Short trip", now.withDayOfMonth(17), 2, "dodgerblue");
        EntryManager.createDayEntry(calendar, "This special holiday", now.withDayOfMonth(25), 1, "gray");
        EntryManager.createDayEntry(calendar, "All Day Today", now, 2, "lightgreen");

        // Multi entries on day 22 — 4 entries with limit=3 triggers "+more" link
        EntryManager.createDayEntry(calendar, "Multi 1", now.withDayOfMonth(22), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 2", now.withDayOfMonth(22), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 3", now.withDayOfMonth(22), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 4", now.withDayOfMonth(22), 2, "tomato");

        // Recurring sunday event (all year)
        EntryManager.createRecurringEvents(calendar);
    }

    @Override
    protected String createDescription() {
        return "Welcome to the FullCalendar demo playground. In this instance you see a basic set of different calendar entry types to play around with. " +
                "You may also create new ones or delete them. Have fun :)";
    }

    @Override
    protected void onTimeslotsSelected(TimeslotsSelectedEvent<Entry> event) {
//        super.onTimeslotsSelected(event); // this is handled by onTimeslotSelectedScheduler
    }

    @Override
    protected void onDayNumberClicked(DayNumberClickedEvent<Entry> event) {
        super.onDayNumberClicked(event);
    }

    @Override
    protected void onWeekNumberClicked(WeekNumberClickedEvent<Entry> event) {
        super.onWeekNumberClicked(event);
    }

    @Override
    protected void onViewSkeletonRendered(ViewSkeletonRenderedEvent<Entry> event) {
        super.onViewSkeletonRendered(event);
    }

    @Override
    protected void onEntryResized(CalendarItemResizedEvent<Entry> event) {
        super.onEntryResized(event);
    }

    @Override
    protected void onEntryDropped(CalendarItemDroppedEvent<Entry> event) {
//        super.onEntryDropped(event); this is handled by onEntryDroppedScheduler
    }

    @Override
    protected void onEntryClick(CalendarItemClickedEvent<Entry> event) {
        if (event.getItem().getDisplayMode() != DisplayMode.BACKGROUND && event.getItem().getDisplayMode() != DisplayMode.INVERSE_BACKGROUND) {
            DemoDialog dialog = new DemoDialog(event.getItem(), false);
            dialog.setSaveConsumer(this::onEntryChanged);
            dialog.setDeleteConsumer(e -> onEntriesRemoved(Collections.singletonList(e)));
            dialog.open();
        }
    }

    @Override
    protected void onBrowserTimezoneObtained(BrowserTimezoneObtainedEvent<Entry> event) {
        super.onBrowserTimezoneObtained(event);
    }

    @Override
    protected void onDatesRendered(DatesRenderedEvent<Entry> event) {
        super.onDatesRendered(event);
    }

    @Override
    protected void onMoreLinkClicked(MoreLinkClickedEvent event) {
        super.onMoreLinkClicked(event);
    }

    @Override
    protected void onTimeslotClicked(TimeslotClickedEvent<Entry> event) {
//        super.onTimeslotClicked(event); // this is handled by onTimeslotClickedScheduler
    }

    @Override
    protected void onEntryDroppedScheduler(CalendarItemDroppedSchedulerEvent<Entry> event) {
        super.onEntryDroppedScheduler(event);
    }

    @Override
    protected void onTimeslotClickedScheduler(TimeslotClickedSchedulerEvent event) {
        super.onTimeslotClickedScheduler(event);

        ResourceEntry entry = new ResourceEntry();
        entry.setStart(event.getDateTime());
        entry.setEnd(event.getDateTime().plusHours(1));
        entry.setAllDay(event.isAllDay());
        @SuppressWarnings("unchecked")
        FullCalendar<Entry> calendar = (FullCalendar<Entry>) event.getSource();
        entry.setCalendar(calendar);

        DemoDialog dialog = new DemoDialog(entry, true);
        dialog.setSaveConsumer(e -> onEntriesCreated(Collections.singletonList(e)));
        dialog.setDeleteConsumer(e -> onEntriesRemoved(Collections.singletonList(e)));
        dialog.open();
    }

    @Override
    protected void onTimeslotsSelectedScheduler(TimeslotsSelectedSchedulerEvent event) {
        super.onTimeslotsSelectedScheduler(event);

        @SuppressWarnings("unchecked")
        FullCalendar<Entry> calendar = (FullCalendar<Entry>) event.getSource();

        ResourceEntry entry = new ResourceEntry();

        entry.setStart(event.getStart());
        entry.setEnd(event.getEnd());
        entry.setAllDay(event.isAllDay());

        entry.setCalendar(calendar);

        DemoDialog dialog = new DemoDialog(entry, true);
        dialog.setSaveConsumer(e -> onEntriesCreated(Collections.singletonList(e)));
        dialog.setDeleteConsumer(e -> onEntriesRemoved(Collections.singletonList(e)));
        dialog.open();
    }
}