package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.AbstractEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.ui.MainLayout;
import org.vaadin.stefan.util.EntryManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
@Route(value = "entry-provider", layout = MainLayout.class)
public class EntryProviderDemo extends VerticalLayout {
    public static final List<Timezone> SOME_TIMEZONES = Arrays.asList(Timezone.UTC, Timezone.getSystem(), new Timezone(ZoneId.of("America/Los_Angeles")), new Timezone(ZoneId.of("Japan")));

    private final EntryService service;
    private final BackendEntryProvider entryProvider;

    private MenuBar toolbar;
    private FullCalendar calendar;
    private ComboBox<CalendarView> comboBoxView;
    private ComboBox<Timezone> timezoneComboBox;
    private Button buttonDatePicker;


    public EntryProviderDemo(EntryService service) {
        this.service = service;
        createToolbar();
        //        toolbar.addItem("Add entry", event -> )

        calendar = new FullCalendar();
        calendar.setHeightByParent();
        calendar.addDatesRenderedListener(event -> {
            updateIntervalLabel(buttonDatePicker, comboBoxView.getValue(), event.getIntervalStart());
            System.out.println("dates rendered: " + event.getStart() + " " + event.getEnd());
        });

        add(toolbar, calendar);
        setFlexGrow(1, calendar);

        entryProvider = new BackendEntryProvider(service);
        calendar.setEntryProvider(entryProvider);

        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
    }

    private void createToolbar() {
        toolbar = new MenuBar();
        toolbar.setOpenOnHover(true);
        toolbar.setWidthFull();

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

        toolbar.addItem("Today", e -> calendar.today());

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


    private static class BackendEntryProvider extends AbstractEntryProvider<Entry> {
        private final EntryService service;

        public BackendEntryProvider(EntryService service) {
            this.service = service;
        }

        @Override
        public Stream<Entry> fetch(@NonNull EntryQuery query) {
            return service.streamEntries(query);
        }

        @Override
        public Optional<Entry> fetchById(@NonNull String id) {
            return service.getEntry(id);
        }
    }

}
