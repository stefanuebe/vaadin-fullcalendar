package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.AccessLevel;
import lombok.Getter;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.util.EntryManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author Stefan Uebe
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractEntryProviderDemo extends VerticalLayout {
    public static final List<Timezone> SOME_TIMEZONES = Arrays.asList(Timezone.UTC, Timezone.getSystem(), new Timezone(ZoneId.of("America/Los_Angeles")), new Timezone(ZoneId.of("Japan")));
    public static final int MAX_ITEMS_PER_UI = 3000;

    private final EntryService entryService;
    private final EntryProvider<Entry> entryProvider;

    private final MenuBar toolbar;
    private final FullCalendar calendar;
    private ComboBox<CalendarView> comboBoxView;
    private ComboBox<Timezone> timezoneComboBox;
    private Button buttonDatePicker;
    private MenuItem addDailyItems;

    public AbstractEntryProviderDemo(boolean editable, String description) {
        toolbar = createToolbar(editable);

        entryService = EntryService.createInstance();
        entryProvider = createEntryProvider(entryService);

        calendar = new FullCalendar();
        calendar.setHeightByParent();
        calendar.addDatesRenderedListener(event -> updateIntervalLabel(buttonDatePicker, comboBoxView.getValue(), event.getIntervalStart()));

        calendar.addDayNumberClickedListener(event -> Notification.show("Clicked day number " + event.getDate()));
        calendar.addEntryClickedListener(event -> Notification.show("Clicked entry " + event.getEntry().getId()));
        calendar.addEntryDroppedListener(event -> {
            applyChanges(event);
            Notification.show("Dropped entry " + event.getEntry().getId());
        });
        calendar.addEntryResizedListener(event -> {
            applyChanges(event);
            Notification.show("Resized entry " + event.getEntry().getId());
        });



        calendar.setEntryProvider(entryProvider);

        Span descriptionElement = new Span(description);
        add(descriptionElement, toolbar, calendar);
        setFlexGrow(1, calendar);
        setHorizontalComponentAlignment(Alignment.STRETCH, calendar, descriptionElement);
        setHorizontalComponentAlignment(Alignment.CENTER, toolbar);

        setSizeFull();
    }

    private void applyChanges(EntryDataEvent event) {
        event.applyChangesOnEntry();
        if (!event.getSource().isEagerLoadingEntryProvider()) {
            event.getSource().getEntryProvider().refreshAll();
        }
    }

    protected abstract EntryProvider<Entry> createEntryProvider(EntryService service);

    private MenuBar createToolbar(boolean editable) {
        MenuBar toolbar = new MenuBar();
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

        if (editable) {

            SubMenu calendarItems = toolbar.addItem("Calendar Items").getSubMenu();
            addDailyItems = calendarItems.addItem("Add sample entries", event -> {
                LocalDateTime max = LocalDate.of(2022, Month.DECEMBER, 31).atStartOfDay();
                LocalDateTime date = LocalDate.now().atTime(10, 0);

                List<Entry> entries = new LinkedList<>();
                while (!date.isAfter(max)) {
                    Entry entry = new Entry();
                    EntryManager.setValues(calendar, entry, "DAILY", date, 60, ChronoUnit.MINUTES, null);
                    entries.add(entry);
                    date = date.plusDays(1);
                }

                onSamplesCreated(entries);

                event.getSource().setEnabled(false);
                Notification.show("Added " + entries.size() + " entries, one per day at 10:00 UTC");
            });

            calendarItems.addItem("Remove all entries", e -> {
                onSamplesRemoved();

                addDailyItems.setEnabled(true);

                Notification.show("All entries removed. Reload this page to create a new set of samples or use the Add sample entries button.");
            });
        }

        createSettingsSubMenu(toolbar);
        return toolbar;
    }


    protected abstract void onSamplesCreated(List<Entry> entries);

    protected abstract void onSamplesRemoved();


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

        subMenu.add(comboBoxLocales, timezoneComboBox, showOnlySomeTimezones);

        subMenu.addItem("Detach/Attach Calendar", event -> {
            if (calendar.getParent().isPresent()) {
                remove(calendar);
            } else {
                add(calendar);
            }
        });
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


}
