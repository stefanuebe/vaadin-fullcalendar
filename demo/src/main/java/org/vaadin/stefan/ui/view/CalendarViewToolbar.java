package org.vaadin.stefan.ui.view;

import com.vaadin.flow.component.HasComponents;
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
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

import java.time.*;
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
    public static final List<Timezone> SOME_TIMEZONES = Arrays.asList(Timezone.UTC, new Timezone(ZoneId.of("Europe/Berlin")), new Timezone(ZoneId.of("America/Los_Angeles")), new Timezone(ZoneId.of("Japan")));

    private final FullCalendar calendar;
    private final boolean allTimezones;
    private final boolean allLocales;
    private final boolean editable;
    private final boolean viewChangeable;
    private final boolean dateChangeable;
    private final boolean settingsAvailable;
    private final boolean allowAddingRandomItemsInitially;
    private final Consumer<Collection<Entry>> onSamplesCreated;
    private final Consumer<Collection<Entry>> onSamplesRemoved;
    private final List<CalendarView> customViews;

    private CalendarView selectedView = CalendarViewImpl.DAY_GRID_MONTH;
    private Button buttonDatePicker;
    private MenuItem viewSelector;
    private Select<Timezone> timezoneSelector;
    private HasComponents calendarParent;

    @Builder
    private CalendarViewToolbar(FullCalendar calendar, boolean allTimezones, boolean allLocales, boolean editable, boolean viewChangeable, boolean dateChangeable, boolean settingsAvailable, boolean allowAddingRandomItemsInitially, Consumer<Collection<Entry>> onSamplesCreated, Consumer<Collection<Entry>> onSamplesRemoved, List<CalendarView> customViews) {

        this.calendar = calendar;
        this.settingsAvailable = settingsAvailable;
        this.allowAddingRandomItemsInitially = allowAddingRandomItemsInitially;
        this.onSamplesCreated = onSamplesCreated;
        this.onSamplesRemoved = onSamplesRemoved;
        this.customViews = customViews;
        if (calendar == null) {
            throw new IllegalArgumentException("Calendar instance is required");
        }

        this.allTimezones = allTimezones;
        this.allLocales = allLocales;
        this.editable = editable;
        this.viewChangeable = viewChangeable;
        this.dateChangeable = dateChangeable;

        addThemeVariants(MenuBarVariant.LUMO_SMALL);

        initMenuBar();
    }

    protected void initMenuBar() {
        if (dateChangeable) {
            initDateItems();
        }

        if (viewChangeable) {
            initViewSelector();
        }

        if (editable) {
            initEditItems();
        }

        if (settingsAvailable) {
            initGeneralSettings();
        }

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

        MenuItem addRandomItems;
        MenuItem addRecurringItems;
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
                Notification.show("Added a single entry for today");
            });

            addRecurringItems = calendarItems.addItem("Add recurring entries", event -> {
                event.getSource().setEnabled(false);
                Optional<UI> optionalUI = getUI();
                optionalUI.ifPresent(ui -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        LocalDate endDate = LocalDate.now().withDayOfYear(1).plusYears(1);

                        Entry entry = new Entry();
                        entry.setTitle("Weekly");
                        entry.setRecurringStartDate(LocalDate.now().withDayOfYear(1));
                        entry.setRecurringStartTime(LocalTime.of(10, 0));
                        entry.setRecurringEndDate(entry.getRecurringStartDate().plusYears(1));
                        entry.setRecurringEndTime(entry.getRecurringStartTime().plusMinutes(60));
                        entry.setRecurringDaysOfWeek(DayOfWeek.MONDAY);
                        entry.setDescription("Our weekly meeting at 10");
                        entry.setColor("lightblue");
                        entry.setBorderColor("blue");
                        entry.setCalendar(calendar);

                        ui.access(() -> {
                            onSamplesCreated.accept(Collections.singletonList(entry));
                            Notification.show("Added a recurring entry for this year, each monday at 10:00 UTC");
                        });
                    });
                });
            });

            addRandomItems = calendarItems.addItem("Add random entries", event -> {
                event.getSource().setEnabled(false);
                Optional<UI> optionalUI = getUI();
                optionalUI.ifPresent(ui -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        List<Entry> entries = EntryService.createRandomInstance().getEntries();
                        ui.access(() -> {
                            onSamplesCreated.accept(entries);
                            Notification.show("Added " + entries.size() + " random entries");
                        });
                    });
                });
            });
            if (!allowAddingRandomItemsInitially) {
                addRandomItems.setEnabled(false);
            }

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
            addRandomItems = null;
            addThousandItems = null;
            addRecurringItems = null;
        }

        if (onSamplesRemoved != null) {
            calendarItems.addItem("Remove all entries", e -> {
                onSamplesRemoved.accept(calendar.getEntryProvider().fetchAll().collect(Collectors.toSet()));
                if (addRandomItems != null) {
                    addRandomItems.setEnabled(true);
                }
                if (addRecurringItems != null) {
                    addRecurringItems.setEnabled(true);
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

        List<Locale> items = Arrays.asList(CalendarLocale.getAvailableLocales());
        ComboBox<Locale> localeSelector = new ComboBox<>("Locale");
        localeSelector.setClearButtonVisible(true);
        localeSelector.setItems(items);
        localeSelector.setValue(CalendarLocale.getDefaultLocale());
        localeSelector.addValueChangeListener(event -> {
            Locale value = event.getValue();
            calendar.setLocale(value != null ? value : CalendarLocale.getDefaultLocale());
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

        Checkbox enablePrefetching = new Checkbox("Enable Prefetching", calendar.isPrefetchEnabled());
        enablePrefetching.addValueChangeListener(event -> calendar.setPrefetchEnabled(event.getValue()));

        VerticalLayout verticalLayout = new VerticalLayout(localeSelector, timezoneSelector, enablePrefetching);
        verticalLayout.setSpacing(false);
        verticalLayout.setPadding(false);
        verticalLayout.setMargin(true);
        verticalLayout.setSizeUndefined();
        subMenu.add(verticalLayout);


//        subMenu.addItem("Detach/Attach Calendar", event -> {
//            if (calendar.getParent().isPresent()) {
//                calendarParent = (HasComponents) calendar.getParent().get();
//                calendarParent.remove(calendar);
//            } else if (calendarParent != null) {
//                calendarParent.add(calendar);
//            }
//        });

        return subMenu;
    }

    private void initViewSelector() {
        List<CalendarView> calendarViews;
        if (customViews != null && !customViews.isEmpty()) {
            calendarViews = customViews;
            if (!customViews.contains(selectedView)) {
                selectedView = customViews.get(0);
            }
        } else {
            calendarViews = new ArrayList<>(Arrays.asList(CalendarViewImpl.values()));
            if (calendar instanceof Scheduler) {
                calendarViews.addAll(Arrays.asList(SchedulerView.values()));
            }
        }

        calendarViews.sort(Comparator.comparing(CalendarView::getName));

        viewSelector = addItem("View: " + getViewName(selectedView));
        SubMenu subMenu = viewSelector.getSubMenu();
        calendarViews.stream()
                .sorted(Comparator.comparing(this::getViewName))
                .forEach(view -> {
                    String viewName = getViewName(view);
                    subMenu.addItem(viewName, event -> {
                        calendar.changeView(view);
                        viewSelector.setText("View: " + viewName);
                        selectedView = view;
                    });
                });
    }

    private String getViewName(CalendarView view) {
        String name = null /*customViewNames.get(view)*/;
        if (name == null) {
            name = StringUtils.capitalize(String.join(" ", StringUtils.splitByCharacterTypeCamelCase(view.getClientSideValue())));
        }

        return name;
    }

    public void updateInterval(LocalDate intervalStart) {
        if (buttonDatePicker != null && selectedView != null) {
            updateIntervalLabel(buttonDatePicker, selectedView, intervalStart);
        }
    }

    void updateIntervalLabel(HasText intervalLabel, CalendarView view, LocalDate intervalStart) {
        String text = "--";
        Locale locale = calendar.getLocale();

        if (view instanceof CalendarViewImpl) {
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
        } else {
            String pattern = view != null && view.getDateTimeFormatPattern() != null ? view.getDateTimeFormatPattern() : "MMMM yyyy";
            text = intervalStart.format(DateTimeFormatter.ofPattern(pattern).withLocale(locale));

        }

        intervalLabel.setText(text);
    }

    /**
     * Sets the timezone in the timezone selector. May lead to client side updates.
     * @param timezone timezone
     */
    public void setTimezone(Timezone timezone) {
        if (timezoneSelector != null) {
            timezoneSelector.setValue(timezone);
        }
    }
}
