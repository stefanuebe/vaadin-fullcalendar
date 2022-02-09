package org.vaadin.stefan;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import lombok.Builder;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.util.EntryManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Stefan Uebe
 */
public class CalendarViewToolbar extends MenuBar {
    public static final List<Timezone> SOME_TIMEZONES = Arrays.asList(Timezone.UTC, Timezone.getSystem(), new Timezone(ZoneId.of("America/Los_Angeles")), new Timezone(ZoneId.of("Japan")));

    private final FullCalendar calendar;
    private final boolean allTimezones;
    private final boolean allLocales;
    private final boolean editable;
    private final boolean viewChangeable;
    private final boolean dateChangeable;
    private final Consumer<Collection<Entry>> onSamplesCreated;
    private final Consumer<Collection<Entry>> onSamplesRemoved;
    private final List<CalendarView> customCalendarViews;

    private Button buttonDatePicker;
    private Select<CalendarView> viewSelector;
    private Select<Timezone> timezoneSelector;
    private HasComponents calendarParent;

    @Builder
    private CalendarViewToolbar(FullCalendar calendar, boolean allTimezones, boolean allLocales, boolean editable, boolean viewChangeable, boolean dateChangeable, Consumer<Collection<Entry>> onSamplesCreated, Consumer<Collection<Entry>> onSamplesRemoved, List<CalendarView> customCalendarViews) {
        this.calendar = calendar;
        this.onSamplesCreated = onSamplesCreated;
        this.onSamplesRemoved = onSamplesRemoved;
        this.customCalendarViews = customCalendarViews;
        if (calendar == null) {
            throw new IllegalArgumentException("Calendar instance is required");
        }

        this.allTimezones = allTimezones;
        this.allLocales = allLocales;
        this.editable = editable;
        this.viewChangeable = viewChangeable;
        this.dateChangeable = dateChangeable;

        initMenuBar();
    }

    protected void initMenuBar() {
        if (dateChangeable) {
            initDateItems();
        }

        if (editable) {
            initEditItems();
        }

        initGeneralSettings();
    }

    private void initDateItems() {
        addItem(VaadinIcon.ANGLE_LEFT.create(), e -> calendar.previous());

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
        addItem(buttonDatePicker);
        addItem(VaadinIcon.ANGLE_RIGHT.create(), e -> calendar.next());
        addItem("Today", e -> calendar.today());
    }

    protected SubMenu initEditItems() {
        SubMenu calendarItems = addItem("Entries").getSubMenu();

        MenuItem addDailyItems;
        MenuItem addSingleItem;
        MenuItem addThousandItems;
        if (onSamplesCreated != null) {
            addSingleItem = calendarItems.addItem("Add single entry", event -> {
                event.getSource().setEnabled(false);
                Entry entry = new Entry();
                entry.setStart(LocalDate.now().atTime(10, 0));
                entry.setEnd(LocalDate.now().atTime(11, 0));
                entry.setTitle("Single entry");
                onSamplesCreated.accept(Collections.singletonList(entry));
            });

            addDailyItems = calendarItems.addItem("Add sample entries", event -> {
                event.getSource().setEnabled(false);
                Optional<UI> optionalUI = getUI();
                optionalUI.ifPresent(ui -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        LocalDateTime max = LocalDate.of(2022, Month.DECEMBER, 31).atStartOfDay();
                        LocalDateTime date = LocalDate.now().atTime(10, 0);

                        Set<Entry> entries = new HashSet<>();
                        while (!date.isAfter(max)) {
                            Entry entry = new Entry();
                            EntryManager.setValues(calendar, entry, "DAILY", date, 60, ChronoUnit.MINUTES, null);
                            entries.add(entry);
                            date = date.plusDays(1);
                        }
                        ui.access(() -> {
                            onSamplesCreated.accept(entries);
                            Notification.show("Added " + entries.size() + " entries, one per day at 10:00 UTC");
                        });
                    });
                });
            });

            addThousandItems = calendarItems.addItem("Add 1k entries", event -> {
                MenuItem source = event.getSource();
                source.setEnabled(false);
                Optional<UI> optionalUI = getUI();
                optionalUI.ifPresent(ui -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        LocalDateTime start = LocalDateTime.now();
                        LocalDateTime end = LocalDateTime.now().plus(1, ChronoUnit.DAYS);
                        Set<Entry> list = IntStream.range(0, 1000).mapToObj(i -> {
                            Entry entry = new Entry();
                            entry.setStart(start);
                            entry.setEnd(end);
                            entry.setAllDay(true);
                            entry.setTitle("Generated " + (i + 1));
                            return entry;
                        }).collect(Collectors.toSet());

                        ui.access(() -> {
                            onSamplesCreated.accept(list);
                            Notification.show("Added 1,000 entries for today");
                        });
                    });
                });
            });

        } else {
            addSingleItem = null;
            addDailyItems = null;
            addThousandItems = null;
        }

        if (onSamplesRemoved != null) {
            calendarItems.addItem("Remove all entries", e -> {
                onSamplesRemoved.accept(calendar.getEntryProvider().fetchAll().collect(Collectors.toSet()));
                if (addDailyItems != null) {
                    addDailyItems.setEnabled(true);
                }
                if (addThousandItems != null) {
                    addThousandItems.setEnabled(true);
                }
                if (addSingleItem != null) {
                    addSingleItem.setEnabled(true);
                }
                Notification.show("All entries removed. Reload this page to create a new set of samples or use the Add sample entries buttons.");
            });
        }

        return calendarItems;
    }

    private SubMenu initGeneralSettings() {
        SubMenu subMenu = addItem("Settings").getSubMenu();

        if (viewChangeable) {
            List<CalendarView> calendarViews;
            CalendarView initialView = CalendarViewImpl.DAY_GRID_MONTH;
            if (customCalendarViews != null && !customCalendarViews.isEmpty()) {
                calendarViews = customCalendarViews;
                if (!customCalendarViews.contains(initialView)) {
                    // TODO extend calendar to get the current view and use it here instead
                    initialView = customCalendarViews.get(0);
                }
            } else {
                calendarViews = new ArrayList<>(Arrays.asList(CalendarViewImpl.values()));
                calendarViews.addAll(Arrays.asList(SchedulerView.values()));
            }

            calendarViews.sort(Comparator.comparing(CalendarView::getName));

            viewSelector = new Select<>();
            viewSelector.setLabel("View");
            viewSelector.setItems(calendarViews);
            viewSelector.setValue(initialView);
            viewSelector.setWidthFull();
            CalendarView finalInitialView = initialView;
            viewSelector.addValueChangeListener(e -> {
                CalendarView value = e.getValue();
                calendar.changeView(value == null ? finalInitialView : value);
            });
            viewSelector.setWidthFull();
            subMenu.add(viewSelector);
        }

        List<Locale> items = Arrays.asList(CalendarLocale.getAvailableLocales());
        ComboBox<Locale> localeSelector = new ComboBox<>("Locale");
        localeSelector.setClearButtonVisible(true);
        localeSelector.setItems(items);
        localeSelector.setValue(CalendarLocale.getDefault());
        localeSelector.addValueChangeListener(event -> {
            Locale value = event.getValue();
            calendar.setLocale(value != null ? value : CalendarLocale.getDefault());
            Notification.show("Locale changed to " + calendar.getLocale().toLanguageTag());
        });
        localeSelector.setPreventInvalidInput(true);

        timezoneSelector = new Select<>();
        timezoneSelector.setLabel("Timezone");
        timezoneSelector.setItemLabelGenerator(Timezone::getClientSideValue);
        if (allTimezones) {
            timezoneSelector.setItems(Timezone.getAvailableZones());
        } else {
            timezoneSelector.setItems(SOME_TIMEZONES);
        }

        timezoneSelector.setValue(Timezone.UTC);
        timezoneSelector.addValueChangeListener(event -> {
            if (!Objects.equals(calendar.getTimezone(), event.getValue())) {
                Timezone value = event.getValue();
                calendar.setTimezone(value != null ? value : Timezone.UTC);
                Notification.show("Timezone changed to " + calendar.getTimezone());
            }
        });

        subMenu.add(localeSelector, timezoneSelector);

        subMenu.addItem("Detach/Attach Calendar", event -> {
            if (calendar.getParent().isPresent()) {
                calendarParent = (HasComponents) calendar.getParent().get();
                calendarParent.remove(calendar);
            } else if (calendarParent != null) {
                calendarParent.add(calendar);
            }
        });

        return subMenu;
    }

    public void updateInterval(LocalDate intervalStart) {
        if (buttonDatePicker != null && viewSelector != null) {
            updateIntervalLabel(buttonDatePicker, viewSelector.getValue(), intervalStart);
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
