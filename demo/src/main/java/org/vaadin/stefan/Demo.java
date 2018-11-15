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
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Route(value = "", layout = MainView.class)
@HtmlImport("frontend://styles.html")
@HtmlImport("frontend://styles_scheduler.html")
public class Demo extends Div {
    private static final String[] COLORS = {"tomato", "orange", "dodgerblue", "mediumseagreen", "gray", "slateblue", "violet"};
    private FullCalendar calendar;
    private ComboBox<CalendarView> comboBoxView;
    private Button buttonDatePicker;
    private HorizontalLayout toolbar;
    private ComboBox<Timezone> timezoneComboBox;

    public Demo() {
        createToolbar();
        add(toolbar);

        add(new Hr());

        createCalendarInstance();
        add(calendar);

        // height by parent and flex container
        initBaseLayoutSettings();
    }

    private void createToolbar() {
        Button buttonToday = new Button("Today", VaadinIcon.HOME.create(), e -> calendar.today());
        Button buttonPrevious = new Button("Previous", VaadinIcon.ANGLE_LEFT.create(), e -> calendar.previous());
        Button buttonNext = new Button("Next", VaadinIcon.ANGLE_RIGHT.create(), e -> calendar.next());
        buttonNext.setIconAfterText(true);

        List<CalendarView> calendarViews = new ArrayList<>();
        calendarViews.addAll(Arrays.asList(CalendarViewImpl.values()));
        calendarViews.addAll(Arrays.asList(SchedulerView.values()));
        comboBoxView = new ComboBox<>("", calendarViews);
        comboBoxView.setValue(CalendarViewImpl.MONTH);
        comboBoxView.addValueChangeListener(e -> {
            CalendarView value = e.getValue();
            calendar.changeView(value == null ? CalendarViewImpl.MONTH : value);
        });

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

        Button buttonHeight = new Button("Calendar height", event -> new HeightDialog().open());

        Checkbox cbWeekNumbers = new Checkbox("Week numbers", event -> calendar.setWeekNumbersVisible(event.getValue()));

        ComboBox<Locale> comboBoxLocales = new ComboBox<>();

        List<Locale> items = Arrays.asList(CalendarLocale.getAvailableLocales());
        comboBoxLocales.setItems(items);
        comboBoxLocales.setValue(CalendarLocale.getDefault());
        comboBoxLocales.addValueChangeListener(event -> calendar.setLocale(event.getValue()));
        comboBoxLocales.setRequired(true);
        comboBoxLocales.setPreventInvalidInput(true);

        ComboBox<GroupEntriesBy> comboBoxGroupBy = new ComboBox<>("");
        comboBoxGroupBy.setPlaceholder("Group by...");
        comboBoxGroupBy.setItems(GroupEntriesBy.values());
        comboBoxGroupBy.setItemLabelGenerator(item -> {
            switch (item) {
                default:
                case NONE:
                    return "none";
                case RESOURCE_DATE:
                    return "group by resource / date";
                case DATE_RESOURCE:
                    return "group by date / resource";
            }
        });
        comboBoxGroupBy.addValueChangeListener(event -> ((Scheduler) calendar).setGroupEntriesBy(event.getValue()));

        timezoneComboBox = new ComboBox<>("");
        timezoneComboBox.setItemLabelGenerator(Timezone::getClientSideValue);
        timezoneComboBox.setItems(Timezone.getAvailableZones());
        timezoneComboBox.setValue(Timezone.UTC);
        timezoneComboBox.addValueChangeListener(event -> {
            Timezone value = event.getValue();
            calendar.setTimezone(value != null ? value : Timezone.UTC);
        });

        toolbar = new HorizontalLayout(buttonToday, buttonPrevious, buttonDatePicker, buttonNext, comboBoxView, buttonHeight, cbWeekNumbers, comboBoxLocales, comboBoxGroupBy, timezoneComboBox);
    }

    private void setFlexStyles(boolean flexStyles) {
        if (flexStyles) {
            calendar.getElement().getStyle().set("flex-grow", "1");
            getElement().getStyle().set("display", "flex");
            getElement().getStyle().set("flex-direction", "column");
        } else {
            calendar.getElement().getStyle().remove("flex-grow");
            getElement().getStyle().remove("display");
            getElement().getStyle().remove("flex-direction");
        }
    }

    private void createCalendarInstance() {
//        calendar = FullCalendarBuilder.create().withEntryLimit(5).withScheduler().build();
        calendar = new MyFullCalendar(5);
        calendar.changeView(CalendarViewImpl.MONTH);
        calendar.setNowIndicatorShown(true);
        calendar.setNumberClickable(true);
        calendar.setTimeslotsSelectable(true);
        calendar.setBusinessHours(
                new BusinessHours(LocalTime.of(9, 0), LocalTime.of(17, 0),BusinessHours.DEFAULT_BUSINESS_WEEK),
                new BusinessHours(LocalTime.of(12, 0), LocalTime.of(15, 0), DayOfWeek.SATURDAY)
        );
        calendar.addBrowserTimezoneObtainedListener(event -> {
            timezoneComboBox.setValue(event.getTimezone());
        });

//        calendar.setEntryRenderCallback("" +
//                "function(event, element) {" +
//                "   console.log(event.title + 'X');" +
//                "   element.css('color', 'red');" +
//                "   return element; " +
//                "}");

        // scheduler options
        ((Scheduler) calendar).setSchedulerLicenseKey(Scheduler.GPL_V3_LICENSE_KEY);

        // This event listener is deactivated to prevent conflicts with selected event listener, who is also called on a
        // one day selection.
        //        calendar.addTimeslotClickedListener(event -> {
        //            Entry entry = new Entry();
        //
        //            LocalDateTime start = event.getClickedDateTime();
        //            entry.setStart(start);
        //
        //            boolean allDay = event.isAllDay();
        //            entry.setAllDay(allDay);
        //            entry.setEnd(allDay ? start.plusDays(FullCalendar.DEFAULT_DAY_EVENT_DURATION) : start.plusHours(FullCalendar.DEFAULT_TIMED_EVENT_DURATION));
        //
        //            entry.setColor("dodgerblue");
        //            new DemoDialog(calendar, entry, true).open();
        //        });

        calendar.addEntryClickedListener(event -> new DemoDialog(calendar, event.getEntry(), false).open());
        calendar.addEntryResizedListener(event -> {
            event.applyChangesOnEntry();

            Entry entry = event.getEntry();

            Notification.show(entry.getTitle() + " resized to " + entry.getStart() + " - " + entry.getEnd() + " " + calendar.getTimezone().getClientSideValue() + " by " + event.getDelta());
        });
        calendar.addEntryDroppedListener(event -> {
            event.applyChangesOnEntry();

            Entry entry = event.getEntry();
            LocalDateTime start = entry.getStart();
            LocalDateTime end = entry.getEnd();

            String text = entry.getTitle() + " moved to " + start + " - " + end + " " + calendar.getTimezone().getClientSideValue()+ " by " + event.getDelta();

            if(entry instanceof ResourceEntry) {
                Set<Resource> resources = ((ResourceEntry) entry).getResources();
                if(!resources.isEmpty()) {
                    text += text + " - rooms are " + resources;
                }
            }


            Notification.show(text);
        });
        calendar.addViewRenderedListener(event -> updateIntervalLabel(buttonDatePicker, comboBoxView.getValue(), event.getIntervalStart()));

        calendar.addTimeslotsSelectedListener(event -> {
            Entry entry = new Entry();

            entry.setStart(calendar.getTimezone().convertToUTC(event.getStartDateTime()));
            entry.setEnd(calendar.getTimezone().convertToUTC(event.getEndDateTime()));
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
                            NativeButton button = new NativeButton(entry.getTitle(), clickEvent -> new DemoDialog(calendar, entry, false).open());
                            Style style = button.getStyle();
                            style.set("background-color", entry.getColor());
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

        calendar.addDayNumberClickedListener(event -> {
            comboBoxView.setValue(CalendarViewImpl.LIST_DAY);
            calendar.gotoDate(event.getDateTime().toLocalDate());
        });
        calendar.addWeekNumberClickedListener(event -> {
            comboBoxView.setValue(CalendarViewImpl.LIST_WEEK);
            calendar.gotoDate(event.getDateTime().toLocalDate());
        });

        createTestEntries(calendar);
    }

    private void initBaseLayoutSettings() {
        setSizeFull();
        calendar.setHeightByParent();
        setFlexStyles(true);
    }

    private void createTestEntries(FullCalendar calendar) {
        LocalDate now = LocalDate.now();

        Resource meetingRoomRed = createResource((Scheduler) calendar, "Meetingroom Red", "red");
        Resource meetingRoomGreen = createResource((Scheduler) calendar, "Meetingroom Green", "green");
        Resource meetingRoomBlue = createResource((Scheduler) calendar, "Meetingroom Blue", "blue");


        createTimedEntry(calendar, "Kickoff meeting with customer #1", now.withDayOfMonth(3).atTime(10, 0), 120, null, meetingRoomBlue, meetingRoomGreen, meetingRoomRed);
        createTimedBackgroundEntry(calendar, now.withDayOfMonth(3).atTime(10, 0), 120, null, meetingRoomBlue, meetingRoomGreen, meetingRoomRed);
        createTimedEntry(calendar, "Kickoff meeting with customer #2", now.withDayOfMonth(7).atTime(11, 30), 120, "mediumseagreen", meetingRoomRed);
        createTimedEntry(calendar, "Kickoff meeting with customer #3", now.withDayOfMonth(12).atTime(9, 0), 120, "mediumseagreen", meetingRoomGreen);
        createTimedEntry(calendar, "Kickoff meeting with customer #4", now.withDayOfMonth(13).atTime(10, 0), 120, "mediumseagreen", meetingRoomGreen);
        createTimedEntry(calendar, "Kickoff meeting with customer #5", now.withDayOfMonth(17).atTime(11, 30), 120, "mediumseagreen", meetingRoomBlue);
        createTimedEntry(calendar, "Kickoff meeting with customer #6", now.withDayOfMonth(22).atTime(9, 0), 120, "mediumseagreen", meetingRoomRed);

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
    }

    private Resource createResource(Scheduler calendar, String s, String color) {
        Resource resource = new Resource(null, s, color);
        calendar.addResource(resource);
        return resource;
    }

    private void createDayEntry(FullCalendar calendar, String title, LocalDate start, int days, String color) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, title, start.atStartOfDay(), days, ChronoUnit.DAYS, color);
        calendar.addEntry(entry);
    }

    private void createTimedEntry(FullCalendar calendar, String title, LocalDateTime start, int minutes, String color) {
        createTimedEntry(calendar, title, start, minutes, color, (Resource[]) null);
    }

    private void createTimedEntry(FullCalendar calendar, String title, LocalDateTime start, int minutes, String color, Resource... resources) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, title, start, minutes, ChronoUnit.MINUTES, color);
        if (resources != null && resources.length > 0) {
            entry.addResources(Arrays.asList(resources));
        }
        calendar.addEntry(entry);
    }
    private void createDayBackgroundEntry(FullCalendar calendar, LocalDate start, int days, String color) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, "BG", start.atStartOfDay(), days, ChronoUnit.DAYS, color);

        entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
        calendar.addEntry(entry);
    }

    private void createTimedBackgroundEntry(FullCalendar calendar, LocalDateTime start, int minutes, String color) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, "BG", start, minutes, ChronoUnit.MINUTES, color);

        entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
        calendar.addEntry(entry);
    }

    private void createTimedBackgroundEntry(FullCalendar calendar, LocalDateTime start, int minutes, String color, Resource... resources) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, "BG", start, minutes, ChronoUnit.MINUTES, color);
        entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
        if (resources != null && resources.length > 0) {
            entry.addResources(Arrays.asList(resources));
        }
        calendar.addEntry(entry);
    }

    private void setValues(FullCalendar calendar, ResourceEntry entry, String title, LocalDateTime start, int amountToAdd, ChronoUnit unit, String color) {
        entry.setTitle(title);
        entry.setStart(start, calendar.getTimezone());
        entry.setEnd(entry.getStartUTC().plus(amountToAdd, unit));
        entry.setAllDay(unit == ChronoUnit.DAYS);
        entry.setColor(color);
    }

    private void updateIntervalLabel(HasText intervalLabel, CalendarView view, LocalDate intervalStart) {
        String text = "--";
        Locale locale = calendar.getLocale();

        if (view == null) {
            text = intervalStart.format(DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(locale));
        } else if (view instanceof CalendarViewImpl) {
            switch ((CalendarViewImpl) view) {
                default:
                case MONTH:
                case LIST_MONTH:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(locale));
                    break;
                case AGENDA_DAY:
                case BASIC_DAY:
                case LIST_DAY:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(locale));
                    break;
                case AGENDA_WEEK:
                case BASIC_WEEK:
                case LIST_WEEK:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("dd.MM.yy").withLocale(locale)) + " - " + intervalStart.plusDays(6).format(DateTimeFormatter.ofPattern("dd.MM.yy").withLocale(locale)) + " (cw " + intervalStart.format(DateTimeFormatter.ofPattern("ww").withLocale(locale)) + ")";
                    break;
                case LIST_YEAR:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("yyyy").withLocale(locale));
                    break;
            }
        } else if (view instanceof SchedulerView) {
            switch ((SchedulerView) view) {
                default:
                case TIMELINE_MONTH:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(locale));
                    break;
                case TIMELINE_DAY:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(locale));
                    break;
                case TIMELINE_WEEK:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("dd.MM.yy").withLocale(locale)) + " - " + intervalStart.plusDays(6).format(DateTimeFormatter.ofPattern("dd.MM.yy").withLocale(locale)) + " (cw " + intervalStart.format(DateTimeFormatter.ofPattern("ww").withLocale(locale)) + ")";
                    break;
                case TIMELINE_YEAR:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("yyyy").withLocale(locale));
                    break;
            }
        }

        intervalLabel.setText(text);
    }

    public static class DemoDialog extends Dialog {

        DemoDialog(FullCalendar calendar, Entry entry, boolean newInstance) {
            setCloseOnEsc(true);
            setCloseOnOutsideClick(true);

            VerticalLayout layout = new VerticalLayout();
            layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
            layout.setSizeFull();

            TextField fieldTitle = new TextField("Title");
            fieldTitle.focus();

            ComboBox<String> fieldColor = new ComboBox<>("Color", COLORS);
            TextArea fieldDescription = new TextArea("Description");

            layout.add(fieldTitle, fieldColor, fieldDescription);

            TextField fieldStart = new TextField("Start");
            fieldStart.setEnabled(false);

            TextField fieldEnd = new TextField("End");
            fieldEnd.setEnabled(false);

            fieldStart.setValue(calendar.getTimezone().formatWithZoneId(entry.getStartUTC()));
            fieldEnd.setValue(calendar.getTimezone().formatWithZoneId(entry.getEndUTC()));

            Checkbox fieldAllDay = new Checkbox("All day event");
            fieldAllDay.setValue(entry.isAllDay());
            fieldAllDay.setEnabled(false);

            layout.add(fieldStart, fieldEnd, fieldAllDay);

            Binder<Entry> binder = new Binder<>(Entry.class);
            binder.forField(fieldTitle)
                    .asRequired()
                    .bind(Entry::getTitle, Entry::setTitle);

            binder.bind(fieldColor, Entry::getColor, Entry::setColor);
            binder.bind(fieldDescription, Entry::getDescription, Entry::setDescription);
            binder.setBean(entry);

            HorizontalLayout buttons = new HorizontalLayout();
            Button buttonSave;
            if (newInstance) {
                buttonSave = new Button("Create", e -> {
                    if (binder.validate().isOk()) {
                        calendar.addEntry(entry);
                    }
                });
            } else {
                buttonSave = new Button("Save", e -> {
                    if (binder.validate().isOk()) {
                        calendar.updateEntry(entry);
                    }
                });
            }
            buttonSave.addClickListener(e -> close());
            buttons.add(buttonSave);

            Button buttonCancel = new Button("Cancel", e -> close());
            buttonCancel.getElement().getThemeList().add("tertiary");
            buttons.add(buttonCancel);

            if (!newInstance) {
                Button buttonRemove = new Button("Remove", e -> {
                    calendar.removeEntry(entry);
                    close();
                });
                ThemeList themeList = buttonRemove.getElement().getThemeList();
                themeList.add("error");
                themeList.add("tertiary");
                buttons.add(buttonRemove);
            }

            add(layout, buttons);
        }
    }

    public class HeightDialog extends Dialog {
        HeightDialog() {
            VerticalLayout dialogContainer = new VerticalLayout();
            add(dialogContainer);

            TextField heightInput = new TextField("", "500", "e. g. 300");
            Button byPixels = new Button("Set by pixels", e -> {
                calendar.setHeight(Integer.valueOf(heightInput.getValue()));

                Demo.this.setSizeUndefined();
                setFlexStyles(false);
            });
            byPixels.getElement().setProperty("title", "Calendar height is fixed by pixels.");
            dialogContainer.add(new HorizontalLayout(heightInput, byPixels));

            Button autoHeight = new Button("Auto height", e -> {
                calendar.setHeightAuto();

                Demo.this.setSizeUndefined();
                setFlexStyles(false);
            });
            autoHeight.getElement().setProperty("title", "Calendar height is set to auto.");
            dialogContainer.add(autoHeight);

            Button heightByBlockParent = new Button("Height by block parent", e -> {
                calendar.setHeightByParent();
                calendar.setSizeFull();

                Demo.this.setSizeFull();
                setFlexStyles(false);
            });
            heightByBlockParent.getElement().setProperty("title", "Container is display:block + setSizeFull(). Calendar height is set to parent + setSizeFull(). Body element kept unchanged.");
            dialogContainer.add(heightByBlockParent);

            Button heightByBlockParentAndCalc = new Button("Height by block parent + calc()", e -> {
                calendar.setHeightByParent();
                calendar.getElement().getStyle().set("height", "calc(100vh - 450px)");

                Demo.this.setSizeFull();
                setFlexStyles(false);
            });
            heightByBlockParentAndCalc.getElement().setProperty("title", "Container is display:block + setSizeFull(). Calendar height is set to parent + css height is calculated by calc(100vh - 450px) as example. Body element kept unchanged.");
            dialogContainer.add(heightByBlockParentAndCalc);

            Button heightByFlexParent = new Button("Height by flex parent", e -> {
                calendar.setHeightByParent();

                Demo.this.setSizeFull();
                setFlexStyles(true);
            });
            heightByFlexParent.getElement().setProperty("title", "Container is display:flex + setSizeFull(). Calendar height is set to parent + flex-grow: 1. Body element kept unchanged.");
            dialogContainer.add(heightByFlexParent);

            Button heightByFlexParentAndBody = new Button("Height by flex parent and flex body", e -> {
                calendar.setHeightByParent();

                Demo.this.setSizeUndefined();
                setFlexStyles(true);

                UI.getCurrent().getElement().getStyle().set("display", "flex");
            });
            heightByFlexParentAndBody.getElement().setProperty("title", "Container is display:flex. Calendar height is set to parent + flex-grow: 1. Body element is set to display: flex.");
            dialogContainer.add(heightByFlexParentAndBody);
        }
    }

}
