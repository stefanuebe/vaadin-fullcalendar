/*
 * Copyright 2018, Stefan Uebe
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
package org.vaadin.stefan;

import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route(value = "", layout = MainView.class)
@CssImport("./styles.css")
@CssImport("./styles-scheduler.css")
@CssImport("./demo-styles.css")
public class Demo extends VerticalLayout {

    private static final String[] COLORS = {"tomato", "orange", "dodgerblue", "mediumseagreen", "gray", "slateblue", "violet"};
    private FullCalendar calendar;
    private ComboBox<CalendarView> comboBoxView;
    private Button buttonDatePicker;
    private HorizontalLayout toolbar;
    private ComboBox<Timezone> timezoneComboBox;

    private boolean recurringCreated = false;

    public Demo() {
        setPadding(false);
        setMargin(false);
        setSpacing(false);
        getStyle().set("flex-grow", "1");

        createCalendarInstance();

        createToolbar();

        add(calendar);
        setFlexGrow(1, calendar);
        setHorizontalComponentAlignment(Alignment.STRETCH, calendar);

        createTestEntries(calendar);
    }

    private void createToolbar() {
        Button buttonToday = new Button("Today", VaadinIcon.HOME.create(), e -> calendar.today());
        Button buttonPrevious = new Button("Previous", VaadinIcon.ANGLE_LEFT.create(), e -> calendar.previous());
        Button buttonNext = new Button("Next", VaadinIcon.ANGLE_RIGHT.create(), e -> calendar.next());
        buttonNext.setIconAfterText(true);

        // simulate the date picker light that we can use in polymer
        DatePicker gotoDate = new DatePicker();
        gotoDate.addValueChangeListener(event1 -> calendar.gotoDate(event1.getValue()));
        gotoDate.getElement().getStyle().set("visibility", "hidden");
        gotoDate.getElement().getStyle().set("position", "fixed");
        gotoDate.setWidth("0px");
        gotoDate.setHeight("0px");
        gotoDate.setWeekNumbersVisible(true);
        buttonDatePicker = new Button(VaadinIcon.CALENDAR.create());
        buttonDatePicker.getElement().appendChild(gotoDate.getElement());
        buttonDatePicker.addClickListener(event -> gotoDate.open());

        List<CalendarView> calendarViews = new ArrayList<>(Arrays.asList(CalendarViewImpl.values()));
        calendarViews.addAll(Arrays.asList(SchedulerView.values()));
        calendarViews.sort(Comparator.comparing(CalendarView::getName));

        comboBoxView = new ComboBox<>("", calendarViews);
        comboBoxView.setValue(CalendarViewImpl.DAY_GRID_MONTH);
        comboBoxView.setWidth("300px");
        comboBoxView.addValueChangeListener(e -> {
            CalendarView value = e.getValue();
            calendar.changeView(value == null ? CalendarViewImpl.DAY_GRID_MONTH : value);
        });

        List<Locale> items = Arrays.asList(CalendarLocale.getAvailableLocales());
        ComboBox<Locale> comboBoxLocales = new ComboBox<>();
        comboBoxLocales.setItems(items);
        comboBoxLocales.setValue(CalendarLocale.getDefault());
        comboBoxLocales.addValueChangeListener(event -> calendar.setLocale(event.getValue()));
        comboBoxLocales.setRequired(true);
        comboBoxLocales.setPreventInvalidInput(true);

        timezoneComboBox = new ComboBox<>("");
        timezoneComboBox.setItemLabelGenerator(Timezone::getClientSideValue);
        timezoneComboBox.setItems(Timezone.getAvailableZones());
        timezoneComboBox.setValue(Timezone.UTC);
        timezoneComboBox.addValueChangeListener(event -> {
            Timezone value = event.getValue();
            calendar.setTimezone(value != null ? value : Timezone.UTC);
        });

        Button addThousand = new Button("Add 1000 entries", event -> {
            Button source = event.getSource();
            source.setEnabled(false);
            source.setText("Creating...");
            Optional<UI> optionalUI = getUI();
            optionalUI.ifPresent(ui -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Timezone timezone = new Timezone(ZoneId.systemDefault());
                    Instant start = Instant.now();
                    Instant end = Instant.now().plus(1, ChronoUnit.DAYS);
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
                        source.setVisible(false);
                        Notification.show("Added 1,000 entries for today");
                    });
                });
            });
        });

        Button removeAllEntries = new Button("All Entries", VaadinIcon.TRASH.create(), event -> calendar.removeAllEntries());
        Button removeAllResources = new Button("All Resources", VaadinIcon.TRASH.create(), event -> ((FullCalendarScheduler) calendar).removeAllResources());

        toolbar = new HorizontalLayout(buttonToday, buttonPrevious, buttonNext, buttonDatePicker, gotoDate, comboBoxView, comboBoxLocales);

        Optional.ofNullable(timezoneComboBox).ifPresent(toolbar::add);

        toolbar.add(addThousand, removeAllEntries, removeAllResources);

        add(toolbar);
    }


    private void createCalendarInstance() {
        calendar = FullCalendarBuilder.create().withAutoBrowserTimezone().withEntryLimit(3).withScheduler().build();

        ((FullCalendarScheduler) calendar).setSchedulerLicenseKey("GPL-My-Project-Is-Open-Source");

        calendar.addDatesRenderedListener(event -> {
            updateIntervalLabel(buttonDatePicker, comboBoxView.getValue(), event.getIntervalStart());
            System.out.println("dates rendered: " + event.getStart() + " " + event.getEnd());
        });

        calendar.setFirstDay(DayOfWeek.MONDAY);
        calendar.setNowIndicatorShown(true);
        calendar.setNumberClickable(true);
        calendar.setTimeslotsSelectable(true);

        calendar.setBusinessHours(
                new BusinessHours(LocalTime.of(9, 0), LocalTime.of(17, 0), BusinessHours.DEFAULT_BUSINESS_WEEK),
                new BusinessHours(LocalTime.of(12, 0), LocalTime.of(15, 0), DayOfWeek.SATURDAY),
                new BusinessHours(LocalTime.of(12, 0), LocalTime.of(13, 0), DayOfWeek.SUNDAY)
        );

        calendar.addWeekNumberClickedListener(event -> System.out.println("week number clicked: " + event.getDate()));
        calendar.addTimeslotClickedListener(event -> System.out.println("timeslot clicked: " + event.getDateTime() + " " + event.isAllDay()));
        calendar.addDayNumberClickedListener(event -> System.out.println("day number clicked: " + event.getDate()));
        calendar.addTimeslotsSelectedListener(event -> System.out.println("timeslots selected: " + event.getStartDateTime() + " -> " + event.getEndDateTime() + " " + event.isAllDay()));

        calendar.addEntryDroppedListener(event -> System.out.println(event.applyChangesOnEntry()));
        ((FullCalendarScheduler) calendar).addEntryDroppedSchedulerListener(event -> {
            System.out.println("Old resource: " + event.getOldResource());
            System.out.println("New resource: " + event.getNewResource());

            System.out.println(event.applyChangesOnEntry());
        });
        calendar.addEntryResizedListener(event -> System.out.println(event.applyChangesOnEntry()));

        calendar.addEntryClickedListener(event -> new DemoDialog(calendar, (ResourceEntry) event.getEntry(), false).open());

        ((FullCalendarScheduler) calendar).addTimeslotsSelectedSchedulerListener((event) -> {
            ResourceEntry entry = new ResourceEntry();

            entry.setStart(event.getStartDateTimeUTC());
            entry.setEnd(event.getEndDateTimeUTC());
            entry.setAllDay(event.isAllDay());

            entry.setColor("dodgerblue");
            new DemoDialog(calendar, entry, true).open();
        });

        calendar.addLimitedEntriesClickedListener(event -> {
            Collection<Entry> entries = calendar.getEntries(event.getClickedDate());
            if (!entries.isEmpty()) {
                Dialog dialog = new Dialog();
                VerticalLayout dialogLayout = new VerticalLayout();
                dialogLayout.setSpacing(false);
                dialogLayout.setPadding(false);
                dialogLayout.setMargin(false);
                dialogLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);

                dialogLayout.add(new Span("Entries of " + event.getClickedDate()));
                entries.stream()
                        .sorted(Comparator.comparing(Entry::getTitle))
                        .map(entry -> {
                            NativeButton button = new NativeButton(entry.getTitle(), clickEvent -> new DemoDialog(calendar, (ResourceEntry) entry, false).open());
                            Style style = button.getStyle();
                            style.set("background-color", Optional.ofNullable(entry.getColor()).orElse("rgb(58, 135, 173)"));
                            style.set("color", "white");
                            style.set("border", "0 none black");
                            style.set("border-radius", "3px");
                            style.set("text-align", "left");
                            style.set("margin", "1px");
                            return button;
                        }).forEach(dialogLayout::add);

                dialog.add(dialogLayout);
                dialog.open();
            }
        });

//        calendar.setOption("rerenderDelay", "null");
    }

    private void createTestEntries(FullCalendar calendar) {
        LocalDate now = LocalDate.now();

        Resource meetingRoomRed = createResource((Scheduler) calendar, "Meetingroom Red", "#ff0000");
        Resource meetingRoomGreen = createResource((Scheduler) calendar, "Meetingroom Green", "green");
        Resource meetingRoomBlue = createResource((Scheduler) calendar, "Meetingroom Blue", "blue");
        Resource computer1A = createResource((Scheduler) calendar, "Computer 1A", "lightbrown");
        Resource computer1B = createResource((Scheduler) calendar, "Computer 1B", "lightbrown");
        Resource computer1C = createResource((Scheduler) calendar, "Computer 1C", "lightbrown");

        createResource((Scheduler) calendar, "Computer room 1", "brown", Arrays.asList(computer1A, computer1B, computer1C));

        Resource computerRoom2 = createResource((Scheduler) calendar, "Computer room 2", "brown");
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

        createTimedEntry(calendar, "Kickoff meeting with customer #1", now.withDayOfMonth(3).atTime(10, 0), 120, null, meetingRoomBlue, meetingRoomGreen, meetingRoomRed);
        createTimedBackgroundEntry(calendar, now.withDayOfMonth(3).atTime(10, 0), 120, null, meetingRoomBlue, meetingRoomGreen, meetingRoomRed);
        createTimedEntry(calendar, "Kickoff meeting with customer #2", now.withDayOfMonth(7).atTime(11, 30), 120, "mediumseagreen", meetingRoomRed);
        createTimedEntry(calendar, "Kickoff meeting with customer #3", now.withDayOfMonth(12).atTime(9, 0), 120, "mediumseagreen", meetingRoomGreen);
        createTimedEntry(calendar, "Kickoff meeting with customer #4", now.withDayOfMonth(13).atTime(10, 0), 120, "mediumseagreen", meetingRoomGreen);
        createTimedEntry(calendar, "Kickoff meeting with customer #5", now.withDayOfMonth(17).atTime(11, 30), 120, "mediumseagreen", meetingRoomBlue);
        createTimedEntry(calendar, "Kickoff meeting with customer #6", now.withDayOfMonth(22).atTime(9, 0), 120, "mediumseagreen", meetingRoomRed);

        createTimedEntry(calendar, "Kickoff meeting with customer #1", now.withDayOfMonth(3).atTime(10, 0), 120, null);
        createTimedBackgroundEntry(calendar, now.withDayOfMonth(3).atTime(10, 0), 120, null);
        createTimedEntry(calendar, "Kickoff meeting with customer #2", now.withDayOfMonth(7).atTime(11, 30), 120, "mediumseagreen");
        createTimedEntry(calendar, "Kickoff meeting with customer #3", now.withDayOfMonth(12).atTime(9, 0), 120, "mediumseagreen");
        createTimedEntry(calendar, "Kickoff meeting with customer #4", now.withDayOfMonth(13).atTime(10, 0), 120, "mediumseagreen");
        createTimedEntry(calendar, "Kickoff meeting with customer #5", now.withDayOfMonth(17).atTime(11, 30), 120, "mediumseagreen");
        createTimedEntry(calendar, "Kickoff meeting with customer #6", now.withDayOfMonth(22).atTime(9, 0), 120, "mediumseagreen");

        createTimedEntry(calendar, "Grocery Store", now.withDayOfMonth(7).atTime(17, 30), 45, "violet");
        createTimedEntry(calendar, "Dentist", now.withDayOfMonth(20).atTime(11, 30), 60, "violet");
        createTimedEntry(calendar, "Cinema", now.withDayOfMonth(10).atTime(20, 30), 140, "dodgerblue");
        createDayEntry(calendar, "Short trip", now.withDayOfMonth(17), 2, "dodgerblue");
        createDayEntry(calendar, "John's Birthday", now.withDayOfMonth(23), 1, "gray");
        createDayEntry(calendar, "This special holiday", now.withDayOfMonth(4), 1, "gray");

        createDayEntry(calendar, "Multi 1", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 2", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 3", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 4", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 5", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 6", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 7", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 8", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 9", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 10", now.withDayOfMonth(12), 2, "tomato");

        createDayBackgroundEntry(calendar, now.withDayOfMonth(4), 6, "#B9FFC3");
        createDayBackgroundEntry(calendar, now.withDayOfMonth(19), 2, "#CEE3FF");
        createTimedBackgroundEntry(calendar, now.withDayOfMonth(20).atTime(11, 0), 150, "#FBC8FF");

        createRecurringEvents(calendar);
    }

    private void createRecurringEvents(FullCalendar calendar) {
        LocalDate now = LocalDate.now();

        ResourceEntry recurring = new ResourceEntry();
        recurring.setRecurring(true);
        recurring.setTitle(now.getYear() + "'s sunday event");
        recurring.setColor("lightgray");
        recurring.setRecurringDaysOfWeeks(Collections.singleton(DayOfWeek.SUNDAY));

        recurring.setRecurringStartDate(now.with(TemporalAdjusters.firstDayOfYear()), calendar.getTimezone());
        recurring.setRecurringEndDate(now.with(TemporalAdjusters.lastDayOfYear()), calendar.getTimezone());
        recurring.setRecurringStartTime(LocalTime.of(14, 0));
        recurring.setRecurringEndTime(LocalTime.of(17, 0));
        recurring.setResourceEditableOnClientSide(true);

        calendar.addEntry(recurring);
    }

    private void createDayEntry(FullCalendar calendar, String title, LocalDate start, int days, String color) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, title, start.atStartOfDay(), days, ChronoUnit.DAYS, color);
        entry.setResourceEditableOnClientSide(true);

        calendar.addEntry(entry);
    }

    private void createTimedEntry(FullCalendar calendar, String title, LocalDateTime start, int minutes, String color) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, title, start, minutes, ChronoUnit.MINUTES, color);
        entry.setResourceEditableOnClientSide(true);

        calendar.addEntry(entry);
    }

    private void createDayBackgroundEntry(FullCalendar calendar, LocalDate start, int days, String color) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, "BG", start.atStartOfDay(), days, ChronoUnit.DAYS, color);

        entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
        entry.setResourceEditableOnClientSide(true);

        calendar.addEntry(entry);
    }

    private void createTimedBackgroundEntry(FullCalendar calendar, LocalDateTime start, int minutes, String color) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, "BG", start, minutes, ChronoUnit.MINUTES, color);
        entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
        entry.setResourceEditableOnClientSide(true);

        calendar.addEntry(entry);
    }

    private void setValues(FullCalendar calendar, Entry entry, String title, LocalDateTime start, int amountToAdd, ChronoUnit unit, String color) {
        entry.setTitle(title);
        entry.setStart(start, calendar.getTimezone());
        entry.setEnd(entry.getStartUTC().plus(amountToAdd, unit));
        entry.setAllDay(unit == ChronoUnit.DAYS);
        entry.setColor(color);
    }

    private void setValues(FullCalendar calendar, ResourceEntry entry, String title, LocalDateTime start, int amountToAdd, ChronoUnit unit, String color) {
        entry.setTitle(title);
        entry.setStart(start, calendar.getTimezone());
        entry.setEnd(entry.getStartUTC().plus(amountToAdd, unit));
        entry.setAllDay(unit == ChronoUnit.DAYS);
        entry.setColor(color);
    }


    private Resource createResource(Scheduler calendar, String s, String color) {
        Resource resource = new Resource(null, s, color);
        calendar.addResource(resource);
        return resource;
    }

    private Resource createResource(Scheduler calendar, String s, String color, Collection<Resource> children) {
        Resource resource = new Resource(null, s, color, children);
        calendar.addResource(resource);
        return resource;
    }

    private void createTimedEntry(FullCalendar calendar, String title, LocalDateTime start, int minutes, String color, Resource... resources) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, title, start, minutes, ChronoUnit.MINUTES, color);
        if (resources != null && resources.length > 0) {
            entry.addResources(Arrays.asList(resources));
        }
        entry.setResourceEditableOnClientSide(true);
        calendar.addEntry(entry);
    }

    private void createTimedBackgroundEntry(FullCalendar calendar, LocalDateTime start, int minutes, String color, Resource... resources) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, "BG", start, minutes, ChronoUnit.MINUTES, color);
        entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
        if (resources != null && resources.length > 0) {
            entry.addResources(Arrays.asList(resources));
        }
        entry.setResourceEditableOnClientSide(true);

        calendar.addEntry(entry);
    }


    private void updateIntervalLabel(HasText intervalLabel, CalendarView view, LocalDate intervalStart) {
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
            switch((SchedulerView) view) {
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
