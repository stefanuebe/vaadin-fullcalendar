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

import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import elemental.json.JsonObject;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.Entry.RenderingMode;
import org.vaadin.stefan.ui.MainLayout;
import org.vaadin.stefan.ui.view.DemoDialog;
import org.vaadin.stefan.util.EntryManager;
import org.vaadin.stefan.util.ResourceManager;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route(value = "full-demo", layout = MainLayout.class)
@CssImport("./styles.css")
@CssImport("./styles-scheduler.css")
@PageTitle("Full Demo")
@org.vaadin.stefan.ui.menu.MenuItem(label = "Full Demo")
public class FullDemo extends VerticalLayout {

    public static final List<Timezone> SOME_TIMEZONES = Arrays.asList(Timezone.UTC, Timezone.getSystem(), new Timezone(ZoneId.of("America/Los_Angeles")), new Timezone(ZoneId.of("Japan")));

    private static final long serialVersionUID = 1L;

    private FullCalendar calendar;
    private ComboBox<CalendarView> comboBoxView;
    private Button buttonDatePicker;
    private MenuBar toolbar;

    private Timezone timezone;
    private ComboBox<Timezone> timezoneComboBox;

    public FullDemo() {
        initView();


        createToolbar();
        add(toolbar);

        createCalendarInstance();
        addAndExpand(calendar);

        createTestEntries();
    }

    private void initView() {
        setSizeFull();
    }

    private void createToolbar() {
        toolbar = new MenuBar();
        toolbar.setOpenOnHover(true);
        toolbar.setWidthFull();

        toolbar.addItem("Today", e -> calendar.today());
        toolbar.addItem(VaadinIcon.ANGLE_LEFT.create(), e -> calendar.previous());

        // simulate the date picker light that we can use in polymer
        DatePicker gotoDate = new DatePicker();
        gotoDate.addValueChangeListener(event1 -> calendar.gotoDate(event1.getValue()));
        gotoDate.getElement().getStyle().set("visibility", "hidden");
        gotoDate.getElement().getStyle().set("position", "fixed");
        gotoDate.setWidth("0px");
        gotoDate.setHeight("0px");
        gotoDate.setWeekNumbersVisible(true);
        buttonDatePicker = new Button(VaadinIcon.CALENDAR.create());
        buttonDatePicker.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        buttonDatePicker.getElement().appendChild(gotoDate.getElement());
        buttonDatePicker.addClickListener(event -> gotoDate.open());
        buttonDatePicker.setWidthFull();
        toolbar.addItem(buttonDatePicker);

        toolbar.addItem(VaadinIcon.ANGLE_RIGHT.create(), e -> calendar.next());

        SubMenu calendarItems = toolbar.addItem("Calendar Items").getSubMenu();
        calendarItems.addItem("Add 1k entries", event -> {
            MenuItem source = event.getSource();
            source.setEnabled(false);
            source.setText("Creating...");
            Optional<UI> optionalUI = getUI();
            optionalUI.ifPresent(ui -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    LocalDateTime start = LocalDateTime.now();
                    LocalDateTime end = LocalDateTime.now().plus(1, ChronoUnit.DAYS);
                    List<Entry> list = IntStream.range(0, 1000).mapToObj(i -> {
                        Entry entry = new ResourceEntry();
                        entry.setStart(start);
                        entry.setEnd(end);
                        entry.setAllDay(true);
                        entry.setTitle("Generated " + (i + 1));
                        return entry;
                    }).collect(Collectors.toList());

                    ui.access(() -> {
                        calendar.addEntries(list);
                        source.setEnabled(true);
                        source.setText("Add 1k entries");
                        Notification.show("Added 1,000 entries for today");
                    });
                });
            });
        });

        calendarItems.addItem("Add daily items up to year's end", event -> {

            LocalDateTime max = LocalDate.of(2022, Month.DECEMBER, 31).atStartOfDay();
            LocalDateTime date = LocalDate.now().atTime(10, 0);

            List<Entry> entries = new LinkedList<>();
            while (!date.isAfter(max)) {
                ResourceEntry entry = new ResourceEntry();
                EntryManager.setValues(calendar, entry, "DAILY", date, 60, ChronoUnit.MINUTES, "red");
                entries.add(entry);
                date = date.plusDays(1);
            }
            calendar.addEntries(entries);
            Notification.show("Added " + entries.size() + " entries, one per day at 10:00 UTC");
        });

        calendarItems.addItem("Remove all entries", e -> calendar.removeAllEntries());
        calendarItems.addItem("Remove all resources", e -> ((FullCalendarScheduler) calendar).removeAllResources());

        createSettingsSubMenu(toolbar);
    }

    private void createSettingsSubMenu(MenuBar menuBar) {
        SubMenu subMenu = menuBar.addItem("Settings").getSubMenu();

        List<CalendarView> calendarViews = new ArrayList<>(Arrays.asList(CalendarViewImpl.values()));
        calendarViews.addAll(Arrays.asList(SchedulerView.values()));
        calendarViews.sort(Comparator.comparing(CalendarView::getName));

        comboBoxView = new ComboBox<>("Calendar View", calendarViews);
        comboBoxView.setValue(CalendarViewImpl.DAY_GRID_MONTH);
        comboBoxView.setWidthFull();
        comboBoxView.addValueChangeListener(e -> {
            CalendarView value = e.getValue();
            calendar.changeView(value == null ? CalendarViewImpl.DAY_GRID_MONTH : value);
        });
        comboBoxView.setWidthFull();
        subMenu.add(comboBoxView);

//        Button toogleFixedWeekCount = new Button("Toggle fixed week count", event -> {
//            calendar.setFixedWeekCount(!calendar.getFixedWeekCount());
//            Notification.show("Updated fixedWeekCount value from " + Boolean.toString(!calendar.getFixedWeekCount()) + " to " + Boolean.toString(calendar.getFixedWeekCount()));
//        });

        List<Locale> items = Arrays.asList(CalendarLocale.getAvailableLocales());
        ComboBox<Locale> comboBoxLocales = new ComboBox<>("Locale");
        comboBoxLocales.setClearButtonVisible(true);
        comboBoxLocales.setItems(items);
        comboBoxLocales.setValue(CalendarLocale.getDefault());
        comboBoxLocales.addValueChangeListener(event -> {
            Locale value = event.getValue();
            calendar.setLocale(value != null ? value : CalendarLocale.getDefault());
            Notification.show("Locale changed to " + calendar.getLocale().toLanguageTag());
        });
        comboBoxLocales.setPreventInvalidInput(true);

        Checkbox showOnlySomeTimezones = new Checkbox("Show only some timezones", true);

        timezoneComboBox = new ComboBox<>("Timezone");
        timezoneComboBox.setClearButtonVisible(true);
        timezoneComboBox.setItemLabelGenerator(Timezone::getClientSideValue);
        timezoneComboBox.setPreventInvalidInput(true);
        timezoneComboBox.setItems(SOME_TIMEZONES);
        timezoneComboBox.setValue(Timezone.UTC);
        timezoneComboBox.addValueChangeListener(event -> {
            if (!Objects.equals(calendar.getTimezone(), event.getValue())) {

                Timezone value = event.getValue();
                calendar.setTimezone(value != null ? value : Timezone.UTC);
                Notification.show("Timezone changed to " + calendar.getTimezone());
            }
        });
        showOnlySomeTimezones.addValueChangeListener(event -> updateTimezonesComboBox(calendar, timezoneComboBox, event.getValue()));

        subMenu.add(/*toogleFixedWeekCount, */comboBoxLocales, timezoneComboBox, showOnlySomeTimezones);
    }

    private void updateTimezonesComboBox(FullCalendar calendar, ComboBox<Timezone> timezoneComboBox, boolean showOnlySome) {
        if (showOnlySome) {
            timezoneComboBox.setItems(SOME_TIMEZONES);
        } else {
            timezoneComboBox.setItems(Timezone.getAvailableZones());
        }

        if (!SOME_TIMEZONES.contains(calendar.getTimezone())) {
            timezoneComboBox.setValue(Timezone.UTC);
        } else {
            timezoneComboBox.setValue(calendar.getTimezone());
        }
    }

    private void createCalendarInstance() {
        JsonObject initialOptions = Json.createObject();
        JsonObject eventTimeFormat = Json.createObject();
        //{ hour: 'numeric', minute: '2-digit', timeZoneName: 'short' }
        eventTimeFormat.put("hour", "2-digit");
        eventTimeFormat.put("minute", "2-digit");
        eventTimeFormat.put("timeZoneName", "short");
        eventTimeFormat.put("meridiem",false);
        initialOptions.put("eventTimeFormat", eventTimeFormat);

        calendar = FullCalendarBuilder.create()
                .withAutoBrowserTimezone()
                .withInitialOptions(initialOptions)
                .withEntryLimit(3)
                .withScheduler("GPL-My-Project-Is-Open-Source")
                .build();

        calendar.setSizeFull();
        ((FullCalendarScheduler) calendar).setResourceAreaWidth("15%");
        ((FullCalendarScheduler) calendar).setSlotMinWidth("100");
        ((FullCalendarScheduler) calendar).setResourcesInitiallyExpanded(false);

        calendar.setFirstDay(DayOfWeek.MONDAY);
        calendar.setNowIndicatorShown(true);
        calendar.setNumberClickable(true);
        calendar.setTimeslotsSelectable(true);

        calendar.setSlotMinTime(LocalTime.of(7, 0));
        calendar.setSlotMaxTime(LocalTime.of(17, 0));

        calendar.setBusinessHours(
                new BusinessHours(LocalTime.of(9, 0), LocalTime.of(17, 0), BusinessHours.DEFAULT_BUSINESS_WEEK),
                new BusinessHours(LocalTime.of(12, 0), LocalTime.of(15, 0), DayOfWeek.SATURDAY),
                new BusinessHours(LocalTime.of(12, 0), LocalTime.of(13, 0), DayOfWeek.SUNDAY)
        );

        calendar.addDatesRenderedListener(event -> {
            updateIntervalLabel(buttonDatePicker, comboBoxView.getValue(), event.getIntervalStart());
            System.out.println("dates rendered: " + event.getStart() + " " + event.getEnd());
        });

        calendar.addViewSkeletonRenderedListener(event -> {
            System.out.println("View skeleton rendered: " + event);
        });

        calendar.addWeekNumberClickedListener(event -> System.out.println("week number clicked: " + event.getDate()));
        calendar.addTimeslotClickedListener(event -> System.out.println("timeslot clicked: " + event.getDateTime() + " " + event.isAllDay()));
        calendar.addDayNumberClickedListener(event -> System.out.println("day number clicked: " + event.getDate()));
        calendar.addTimeslotsSelectedListener(event -> System.out.println("timeslots selected: " + event.getStartDateTime() + " -> " + event.getEndDateTime() + " " + event.isAllDay()));

        ((FullCalendarScheduler) calendar).addEntryDroppedSchedulerListener(event -> {
            System.out.println("Old resource: " + event.getOldResource());
            System.out.println("New resource: " + event.getNewResource());
            Entry entry = event.getEntry();
            System.out.println(entry.getStart() + " / " + entry.getStart());
            System.out.println(event.applyChangesOnEntry());
            System.out.println(entry.getStart() + " / " + entry.getStart());

        });
        calendar.addEntryResizedListener(event -> System.out.println(event.applyChangesOnEntry()));

        calendar.addEntryClickedListener(event -> {
            if (event.getEntry().getRenderingMode() != RenderingMode.BACKGROUND && event.getEntry().getRenderingMode() != RenderingMode.INVERSE_BACKGROUND)
                new DemoDialog(calendar, (ResourceEntry) event.getEntry(), false).open();
        });

        ((FullCalendarScheduler) calendar).addTimeslotsSelectedSchedulerListener((event) -> {
            ResourceEntry entry = new ResourceEntry();

            entry.setStart(event.getStartDateTime());
            entry.setEnd(event.getEndDateTime());
            entry.setAllDay(event.isAllDay());

            entry.setColor("dodgerblue");
            new DemoDialog(calendar, entry, true).open();
        });

        calendar.addBrowserTimezoneObtainedListener(event -> {
            timezoneComboBox.setValue(event.getTimezone());
        });

        ((FullCalendarScheduler) calendar).setEntryResourceEditable(false);
        // this following code is an exapmle on how to create a server side dialog showing all entries of the day
//        calendar.setMoreLinkClickAction(FullCalendar.MoreLinkClickAction.NOTHING);
//        calendar.addMoreLinkClickedListener(event -> {
//            Collection<Entry> entries = event.getEntries();
//            if (!entries.isEmpty()) {
//                Dialog dialog = new Dialog();
//                VerticalLayout dialogLayout = new VerticalLayout();
//                dialogLayout.setSpacing(false);
//                dialogLayout.setPadding(false);
//                dialogLayout.setMargin(false);
//                dialogLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
//
//                dialogLayout.add(new Span("Entries of " + event.getClickedDate()));
//                entries.stream()
//                        .sorted(Comparator.comparing(Entry::getTitle))
//                        .map(entry -> {
//                            NativeButton button = new NativeButton(entry.getTitle(), clickEvent -> new DemoDialog(calendar, (ResourceEntry) entry, false).open());
//                            Style style = button.getStyle();
//                            style.set("background-color", Optional.ofNullable(entry.getColor()).orElse("rgb(58, 135, 173)"));
//                            style.set("color", "white");
//                            style.set("border", "0 none black");
//                            style.set("border-radius", "3px");
//                            style.set("text-align", "left");
//                            style.set("margin", "1px");
//                            return button;
//                        }).forEach(dialogLayout::add);
//
//                dialog.add(dialogLayout);
//                dialog.open();
//            }
//        });

        calendar.addBrowserTimezoneObtainedListener(event -> {
            System.out.println("Use browser's timezone: " + event.getTimezone().toString());
            timezone = event.getTimezone();
        });

        calendar.setEntryDidMountCallback(
                "function(info) { "
                        + "    if(info.event.extendedProps.cursors != undefined) { "
                        + "        if(!info.event.startEditable) { "
                        + "            info.el.style.cursor = info.event.extendedProps.cursors.disabled;"
                        + "        } else { "
                        + "            info.el.style.cursor = info.event.extendedProps.cursors.enabled;"
                        + "        }"
                        + "    }"
                        + "}");
    }

    private void createTestEntries() {
        LocalDate now = LocalDate.now();

        Resource meetingRoomRed = ResourceManager.createResource((Scheduler) calendar, "Meetingroom Red", "#ff0000");
        Resource meetingRoomGreen = ResourceManager.createResource((Scheduler) calendar, "Meetingroom Green", "green");
        Resource meetingRoomBlue = ResourceManager.createResource((Scheduler) calendar, "Meetingroom Blue", "blue");
        Resource meetingRoomOrange = ResourceManager.createResource((Scheduler) calendar, "Meetingroom Orange", "orange", null,
                new BusinessHours(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

        Resource computer1A = ResourceManager.createResource((Scheduler) calendar, "Computer 1A", "lightbrown");
        Resource computer1B = ResourceManager.createResource((Scheduler) calendar, "Computer 1B", "lightbrown");
        Resource computer1C = ResourceManager.createResource((Scheduler) calendar, "Computer 1C", "lightbrown");

        ResourceManager.createResource((Scheduler) calendar, "Computer room 1", "brown", Arrays.asList(computer1A, computer1B, computer1C));

        Resource computerRoom2 = ResourceManager.createResource((Scheduler) calendar, "Computer room 2", "brown");
        // here we must NOT use createResource, since they are added to the calendar later
        Resource computer2A = new Resource(null, "Computer 2A", "lightbrown");
        Resource computer2B = new Resource(null, "Computer 2B", "lightbrown");
        Resource computer2C = new Resource(null, "Computer 2C", "lightbrown");

        // not realistic, just a demonstration of automatic recursive adding
        computer2A.addChild(new Resource(null, "Mouse", "orange"));
        computer2A.addChild(new Resource(null, "Screen", "orange"));
        computer2A.addChild(new Resource(null, "Keyboard", "orange"));

        List<Resource> computerRoom2Children = Arrays.asList(computer2A, computer2B, computer2C);
        computerRoom2.addChildren(computerRoom2Children);
        ((Scheduler) calendar).addResources(computerRoom2Children);

        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #1", now.withDayOfMonth(3).atTime(10, 0), 120, null, meetingRoomBlue, meetingRoomGreen, meetingRoomRed);

        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #7", now.withDayOfMonth(3).atTime(10, 0), 120, null, meetingRoomOrange);
        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #2", now.withDayOfMonth(7).atTime(11, 30), 120, "mediumseagreen", meetingRoomRed);

        HashMap<String, Object> extendedProps = new HashMap<String, Object>();
        HashMap<String, Object> cursors = new HashMap<String, Object>();
        cursors.put("enabled", "pointer");
        cursors.put("disabled", "not-allowed");
        extendedProps.put("cursors", cursors);

        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #3", now.withDayOfMonth(12).atTime(9, 0), 120, "mediumseagreen", extendedProps, meetingRoomGreen);
        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #4", now.withDayOfMonth(13).atTime(10, 0), 120, "mediumseagreen", meetingRoomGreen);
        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #5", now.withDayOfMonth(17).atTime(11, 30), 120, "mediumseagreen", meetingRoomBlue);
        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #6", now.withDayOfMonth(22).atTime(9, 0), 120, "mediumseagreen", meetingRoomRed);

        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #1", now.withDayOfMonth(3).atTime(10, 0), 120, null);
        EntryManager.createTimedBackgroundEntry(calendar, now.withDayOfMonth(3).atTime(10, 0), 120, null);
        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #2", now.withDayOfMonth(7).atTime(11, 30), 120, "mediumseagreen");
        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #3", now.withDayOfMonth(12).atTime(9, 0), 120, "mediumseagreen");
        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #4", now.withDayOfMonth(13).atTime(10, 0), 120, "mediumseagreen");
        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #5", now.withDayOfMonth(17).atTime(11, 30), 120, "mediumseagreen");
        EntryManager.createTimedEntry(calendar, "Kickoff meeting with customer #6", now.withDayOfMonth(22).atTime(9, 0), 120, "mediumseagreen");

        EntryManager.createTimedEntry(calendar, "Grocery Store", now.withDayOfMonth(7).atTime(17, 30), 45, "violet");
        EntryManager.createTimedEntry(calendar, "Dentist", now.withDayOfMonth(20).atTime(11, 30), 60, "violet");
        EntryManager.createTimedEntry(calendar, "Cinema", now.withDayOfMonth(10).atTime(20, 30), 140, "dodgerblue");
        EntryManager.createDayEntry(calendar, "Short trip", now.withDayOfMonth(17), 2, "dodgerblue");
        EntryManager.createDayEntry(calendar, "John's Birthday", now.withDayOfMonth(23), 1, "gray");
        EntryManager.createDayEntry(calendar, "This special holiday", now.withDayOfMonth(4), 1, "gray");

        EntryManager.createDayEntry(calendar, "Multi 1", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 2", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 3", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 4", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 5", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 6", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 7", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 8", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 9", now.withDayOfMonth(12), 2, "tomato");
        EntryManager.createDayEntry(calendar, "Multi 10", now.withDayOfMonth(12), 2, "tomato");


        EntryManager.createDayBackgroundEntry(calendar, now.withDayOfMonth(4), 6, "#B9FFC3");
        EntryManager.createDayBackgroundEntry(calendar, now.withDayOfMonth(19), 2, "#CEE3FF");
        EntryManager.createTimedBackgroundEntry(calendar, now.withDayOfMonth(20).atTime(11, 0), 150, "#ff0000");

        EntryManager.createRecurringEvents(calendar);
    }

    void updateIntervalLabel(HasText intervalLabel, CalendarView view, LocalDate intervalStart) {
        String text = "--";
        Locale locale = calendar.getLocale();

        if (view == null) {
            text = intervalStart.format(DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(locale));
        } else if (view instanceof CalendarViewImpl) {
            switch ((CalendarViewImpl) view) {
                default:
                case DAY_GRID_MONTH:
                case LIST_MONTH:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(locale));
                    break;
                case TIME_GRID_DAY:
                case DAY_GRID_DAY:
                case LIST_DAY:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(locale));
                    break;
                case TIME_GRID_WEEK:
                case DAY_GRID_WEEK:
                case LIST_WEEK:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("dd.MM.yy").withLocale(locale)) + " - " + intervalStart.plusDays(6).format(DateTimeFormatter.ofPattern("dd.MM.yy").withLocale(locale)) + " (cw " + intervalStart.format(DateTimeFormatter.ofPattern("ww").withLocale(locale)) + ")";
                    break;
                case LIST_YEAR:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("yyyy").withLocale(locale));
                    break;
            }
        } else if (view instanceof SchedulerView) {
            switch ((SchedulerView) view) {
                case TIMELINE_DAY:
                case RESOURCE_TIMELINE_DAY:
                case RESOURCE_TIME_GRID_DAY:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(locale));
                    break;
                case TIMELINE_WEEK:
                case RESOURCE_TIMELINE_WEEK:
                case RESOURCE_TIME_GRID_WEEK:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("dd.MM.yy").withLocale(locale)) + " - " + intervalStart.plusDays(6).format(DateTimeFormatter.ofPattern("dd.MM.yy").withLocale(locale)) + " (cw " + intervalStart.format(DateTimeFormatter.ofPattern("ww").withLocale(locale)) + ")";
                    break;
                case TIMELINE_MONTH:
                case RESOURCE_TIMELINE_MONTH:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(locale));
                    break;
                case TIMELINE_YEAR:
                case RESOURCE_TIMELINE_YEAR:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("yyyy").withLocale(locale));
                    break;
            }
        }

        intervalLabel.setText(text);
    }

}