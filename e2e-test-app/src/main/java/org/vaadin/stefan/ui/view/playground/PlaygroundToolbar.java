package org.vaadin.stefan.ui.view.playground;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.vaadin.stefan.fullcalendar.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;

/**
 * Toolbar for playground and entry-provider views.
 * Provides navigation, entries menu, settings menu, and view selector.
 */
public class PlaygroundToolbar extends HorizontalLayout {

    private final FullCalendar calendar;
    private MenuItem viewSelector;
    private CalendarView selectedView = CalendarViewImpl.DAY_GRID_MONTH;

    public PlaygroundToolbar(FullCalendar calendar,
                             Consumer<Collection<Entry>> onEntriesCreated,
                             Consumer<Collection<Entry>> onEntriesRemoved,
                             boolean showRandomEntries,
                             boolean show1kEntries) {
        this.calendar = calendar;
        setWrap(true);
        setAlignItems(Alignment.CENTER);

        initNavigation();
        initViewSelector();
        initEntriesMenu(onEntriesCreated, onEntriesRemoved, showRandomEntries, show1kEntries);
        initSettingsMenu();
    }

    private void initNavigation() {
        Button btnPrev = new Button(VaadinIcon.ANGLE_LEFT.create(), e -> calendar.previous());
        Button btnNext = new Button(VaadinIcon.ANGLE_RIGHT.create(), e -> calendar.next());
        Button btnToday = new Button("Today", e -> calendar.today());

        add(btnPrev, btnNext, btnToday);
    }

    private void initViewSelector() {
        MenuBar viewBar = new MenuBar();
        viewSelector = viewBar.addItem("View: Day Grid Month");
        SubMenu viewSubMenu = viewSelector.getSubMenu();

        Map<String, CalendarView> views = new LinkedHashMap<>();
        views.put("Day Grid Month", CalendarViewImpl.DAY_GRID_MONTH);
        views.put("Day Grid Week", CalendarViewImpl.DAY_GRID_WEEK);
        views.put("Time Grid Day", CalendarViewImpl.TIME_GRID_DAY);
        views.put("Time Grid Week", CalendarViewImpl.TIME_GRID_WEEK);
        views.put("List Week", CalendarViewImpl.LIST_WEEK);
        views.put("List Month", CalendarViewImpl.LIST_MONTH);
        views.put("Multi Month", CalendarViewImpl.MULTI_MONTH);

        views.forEach((label, view) -> {
            viewSubMenu.addItem(label, e -> {
                calendar.changeView(view);
                selectedView = view;
                viewSelector.setText("View: " + label);
            });
        });

        add(viewBar);
    }

    private void initEntriesMenu(Consumer<Collection<Entry>> onCreated,
                                  Consumer<Collection<Entry>> onRemoved,
                                  boolean showRandom,
                                  boolean show1k) {
        MenuBar entriesBar = new MenuBar();
        MenuItem entriesItem = entriesBar.addItem("Entries");
        SubMenu entriesSubMenu = entriesItem.getSubMenu();

        // Add single entry
        MenuItem addSingle = entriesSubMenu.addItem("Add single entry", e -> {
            Entry entry = new Entry();
            entry.setTitle("Single entry");
            entry.setStart(LocalDate.now().atTime(10, 0));
            entry.setEnd(LocalDate.now().atTime(11, 0));
            onCreated.accept(Collections.singletonList(entry));
            Notification.show("Added single entry");
            e.getSource().setEnabled(false);
        });

        // Add recurring entries
        MenuItem addRecurring = entriesSubMenu.addItem("Add recurring entries", e -> {
            Entry entry = new Entry();
            entry.setTitle("Weekly");
            entry.setRecurringDaysOfWeek(DayOfWeek.MONDAY);
            entry.setRecurringStartDate(LocalDate.now().withDayOfYear(1));
            entry.setRecurringEndDate(LocalDate.now().withDayOfYear(1).plusYears(1));
            entry.setRecurringStartTime(LocalTime.of(10, 0));
            entry.setRecurringEndTime(LocalTime.of(11, 0));
            entry.setColor("lightblue");
            entry.setBorderColor("blue");
            onCreated.accept(Collections.singletonList(entry));
            Notification.show("Added recurring entries");
            e.getSource().setEnabled(false);
        });

        // Add random entries (only on PlaygroundView)
        if (showRandom) {
            entriesSubMenu.addItem("Add random entries", e -> {
                List<Entry> entries = new ArrayList<>();
                Random random = new Random(42); // deterministic seed
                LocalDate base = LocalDate.now().withDayOfMonth(1);
                for (int i = 0; i < 20; i++) {
                    Entry entry = new Entry();
                    entry.setTitle("Random " + (i + 1));
                    int day = random.nextInt(28) + 1;
                    if (random.nextBoolean()) {
                        entry.setStart(base.withDayOfMonth(day));
                        entry.setAllDay(true);
                    } else {
                        entry.setStart(base.withDayOfMonth(day).atTime(9 + random.nextInt(8), 0));
                        entry.setEnd(entry.getStartWithOffset().plusHours(1));
                    }
                    entries.add(entry);
                }
                onCreated.accept(entries);
                Notification.show("Added random entries");
            });
        }

        // Add 1k entries (only on entry-provider views)
        if (show1k) {
            entriesSubMenu.addItem("Add 1k entries", e -> {
                List<Entry> entries = new ArrayList<>(1000);
                for (int i = 1; i <= 1000; i++) {
                    Entry entry = new Entry();
                    entry.setTitle("Generated " + i);
                    entry.setStart(LocalDate.now());
                    entry.setAllDay(true);
                    entries.add(entry);
                }
                onCreated.accept(entries);
                Notification.show("Added 1k entries");
            });
        }

        // Remove all entries
        entriesSubMenu.addItem("Remove all entries", e -> {
            onRemoved.accept(Collections.emptyList());
            addSingle.setEnabled(true);
            addRecurring.setEnabled(true);
        });

        add(entriesBar);
    }

    private void initSettingsMenu() {
        MenuBar settingsBar = new MenuBar();
        MenuItem settingsItem = settingsBar.addItem("Settings");
        SubMenu settingsSubMenu = settingsItem.getSubMenu();

        // Wrap checkbox in a MenuItem — use component item
        Checkbox lumoCheckbox = new Checkbox("Lumo");
        lumoCheckbox.setValue(true); // Vaadin theme is on by default
        lumoCheckbox.addValueChangeListener(e -> {
            if (e.getValue()) {
                calendar.addThemeVariants(FullCalendarVariant.VAADIN);
            } else {
                calendar.removeThemeVariants(FullCalendarVariant.VAADIN);
            }
        });

        VerticalLayout settingsLayout = new VerticalLayout(lumoCheckbox);
        settingsLayout.setPadding(true);
        settingsLayout.setSpacing(false);
        settingsSubMenu.addItem(settingsLayout);

        add(settingsBar);
    }

    public void updateSelectedView(CalendarView view) {
        selectedView = view;
        // Find human-readable name
        String name = view.getClientSideValue();
        for (CalendarViewImpl v : CalendarViewImpl.values()) {
            if (v == view) {
                name = v.name().replace("_", " ");
                // Convert to title case
                String[] words = name.toLowerCase().split(" ");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (!sb.isEmpty()) sb.append(" ");
                    sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
                }
                name = sb.toString();
                break;
            }
        }
        viewSelector.setText("View: " + name);
    }
}
