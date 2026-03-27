package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.dialogs.DemoDialog;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractSchedulerView;
import org.vaadin.stefan.ui.view.CalendarViewToolbar;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This demo shows signal binding for a scheduler with resources.
 * Both entries and resources are managed via {@link ListSignal}.
 * A sidebar with resource cards (each listing their entries) provides
 * a two-way integration: changes in the sidebar update the calendar,
 * and changes in the calendar are visible in the sidebar.
 */
@Route(value = "signal-scheduler", layout = MainLayout.class)
@MenuItem(label = "Signal Scheduler")
public class SignalsSchedulerDemo extends AbstractSchedulerView {

    private static final String[] COLORS = {"#3788d8", "#e53935", "#43a047", "#fb8c00", "#8e24aa", "#00897b"};
    private ListSignal<Entry> entriesSignal;
    private ListSignal<Resource> resourcesSignal;

    private DateTimeFormatter dtFormatter;

    @Override
    protected FullCalendar createCalendar(ObjectNode defaultInitialOptions) {
        entriesSignal = new ListSignal<>();
        resourcesSignal = new ListSignal<>();

        dtFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(UI.getCurrent().getLocale());

        // Pre-populate resources
        Resource roomA = new Resource(null, "Room A", "#3788d8");
        Resource roomB = new Resource(null, "Room B", "#e53935");
        Resource roomC = new Resource(null, "Room C", "#43a047");
        resourcesSignal.insertLast(roomA);
        resourcesSignal.insertLast(roomB);
        resourcesSignal.insertLast(roomC);

        // Pre-populate entries: 2-3 per resource per month (last, current, next month)
        // Entries are all-day with varying durations and staggered start days per resource
        Resource[] rooms = {roomA, roomB, roomC};
        String[][] titles = {
                {"Standup", "Sprint Review", "Retro"},           // Room A
                {"Workshop", "1:1 Meeting", "Tech Talk"},        // Room B
                {"Planning", "Demo", "All-Hands"},               // Room C
        };
        // day offsets per resource to avoid alignment (relative to month start)
        int[][] dayOffsets = {
                {1, 10, 20},  // Room A
                {4, 14, 23},  // Room B
                {7, 17, 26},  // Room C
        };
        // duration in days per entry (varies per title)
        int[][] durations = {
                {1, 3, 2},  // Room A
                {2, 1, 4},  // Room B
                {3, 2, 1},  // Room C
        };
        LocalDate now = LocalDate.now();
        for (int monthOffset = -1; monthOffset <= 1; monthOffset++) {
            LocalDate month = now.plusMonths(monthOffset).withDayOfMonth(1);
            for (int i = 0; i < rooms.length; i++) {
                for (int j = 0; j < titles[i].length; j++) {
                    LocalDate startDay = month.plusDays(dayOffsets[i][j]);
                    LocalDate endDay = startDay.plusDays(durations[i][j]);
                    ResourceEntry entry = new ResourceEntry();
                    entry.setTitle(titles[i][j]);
                    entry.setAllDay(true);
                    entry.setStart(startDay);
                    entry.setEnd(endDay);
                    entry.setColor(rooms[i].getColor());
                    entry.addResources(rooms[i]);
                    entriesSignal.insertLast(entry);
                }
            }
        }

        FullCalendarScheduler scheduler = (FullCalendarScheduler) FullCalendarBuilder.create()
                .withScheduler(Scheduler.GPL_V3_LICENSE_KEY)
                .withInitialOptions(defaultInitialOptions)
                .withEntryLimit(3)
                .withInitialView(SchedulerView.RESOURCE_TIMELINE_MONTH)
                .build();

        scheduler.setOption(FullCalendarScheduler.SchedulerOption.ENTRY_RESOURCES_EDITABLE, true);

        scheduler.bindResources(resourcesSignal);
        scheduler.bindEntries(entriesSignal);

        return scheduler;
    }

    @Override
    protected void postConstruct(FullCalendar calendar) {
        // Statistics bar
        Card resourceCount = new Card();
        resourceCount.setSubtitle("Resources");
        Card entryCount = new Card();
        entryCount.setSubtitle("Entries");
        Card entriesPerResource = new Card();
        entriesPerResource.setSubtitle("Most entries");
        Card entriesPerMonth = new Card();
        entriesPerMonth.setSubtitle("Entries/month");

        Signal.effect(this, () -> {
            List<ValueSignal<Resource>> resources = resourcesSignal.get();
            List<ValueSignal<Entry>> entries = entriesSignal.get();

            resourceCount.setTitle("" + resources.size());
            entryCount.setTitle("" + entries.size());

            // Resource with most entries
            Map<Resource, Long> countByResource = resources.stream()
                    .map(ValueSignal::get)
                    .collect(Collectors.toMap(r -> r, r -> entries.stream()
                            .map(ValueSignal::get)
                            .filter(e -> e instanceof ResourceEntry re && re.getResourcesOrEmpty().contains(r))
                            .count()));
            long maxCount = countByResource.values().stream().mapToLong(Long::longValue).max().orElse(0);
            boolean allEqual = countByResource.values().stream().distinct().count() <= 1;
            String maxResourceName = allEqual ? "-even-" : countByResource.entrySet().stream()
                    .filter(en -> en.getValue() == maxCount)
                    .map(en -> en.getKey().getTitle())
                    .findFirst().orElse("-");
            entriesPerResource.setTitle(maxResourceName + " (" + maxCount + ")");

            // Average entries per month
            Map<YearMonth, Integer> perMonth = new TreeMap<>();
            for (ValueSignal<Entry> es : entries) {
                Entry e = es.get();
                if (e.getStart() != null) {
                    perMonth.merge(YearMonth.from(e.getStartAsLocalDate()), 1, Integer::sum);
                }
            }
            double avgPerMonth = perMonth.isEmpty() ? 0 : perMonth.values().stream().mapToInt(Integer::intValue).average().orElse(0);
            entriesPerMonth.setTitle(String.format("%.1f", avgPerMonth));
        });

        HorizontalLayout statsBar = new HorizontalLayout(resourceCount, entryCount, entriesPerResource, entriesPerMonth);
        statsBar.addToEnd(getToolbar());

        VerticalLayout sidebar = createSidebar();

        Scroller sidebarScroller = new Scroller(sidebar);
        sidebarScroller.setWidth("300px");
        sidebarScroller.setMinWidth("300px");
        sidebarScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

        HorizontalLayout content = new HorizontalLayout(sidebarScroller, calendar);
        content.setFlexGrow(1, calendar);
        content.setFlexGrow(0, sidebarScroller);
        content.setAlignItems(Alignment.STRETCH);
        content.getStyle().setOverflow(Style.Overflow.HIDDEN);

        add(statsBar, content);
        setFlexGrow(1, content);
    }

    private VerticalLayout createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setPadding(false);
        sidebar.setSpacing(true);

        // "Add Resource" button
        Button addResourceBtn = new Button("Add Resource", VaadinIcon.PLUS.create(), e -> {
            openResourceDialog(null);
        });
        addResourceBtn.setWidthFull();
        sidebar.add(addResourceBtn);

        // Resource cards — reactively rendered via bindChildren
        VerticalLayout resourceList = new VerticalLayout();
        resourceList.setPadding(false);
        resourceList.setSpacing(true);
        resourceList.bindChildren(resourcesSignal, this::createResourceCard);

        sidebar.add(resourceList);
        return sidebar;
    }

    private Details createResourceCard(ValueSignal<Resource> resourceSignal) {
        // Summary: resource title + action buttons
        Span title = new Span();
        Signal.effect(title, () -> {
            Resource r = resourceSignal.get();
            title.setText(r.getTitle());
            if (r.getColor() != null) {
                title.getStyle().set("color", r.getColor());
                title.getStyle().set("font-weight", "bold");
            }
        });

        // Action buttons — stop propagation so they don't toggle the details
        Button editBtn = new Button(VaadinIcon.EDIT.create(), e ->
                openResourceDialog(resourceSignal));
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        Button addEntryBtn = new Button(VaadinIcon.PLUS.create(), e -> {
            Resource r = resourceSignal.peek();
            if (r != null) openNewEntryDialog(r);
        });
        addEntryBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e ->
                resourcesSignal.remove(resourceSignal));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout buttons = new HorizontalLayout(editBtn, addEntryBtn, deleteBtn);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        // Prevent button clicks from toggling the details
        buttons.getElement().executeJs("this.addEventListener('click', function(e) { e.stopPropagation(); })");

        HorizontalLayout summary = new HorizontalLayout(title, buttons);
        summary.setAlignItems(FlexComponent.Alignment.CENTER);
        summary.setFlexGrow(1, title);
        summary.setWidthFull();

        // Entry list — rebuilt via effect
        VerticalLayout entryList = new VerticalLayout();
        entryList.setPadding(false);
        entryList.setSpacing(false);
        entryList.setWidthFull();
        entryList.setAlignItems(Alignment.STRETCH);

        Details details = new Details(summary, entryList);
        details.setWidthFull();
        details.setOpened(false);

        // This effect rebuilds the entry list when resources, entries, or entry properties change.
        Signal.effect(details, () -> {
            Resource resource = resourceSignal.get();
            List<ValueSignal<Entry>> allEntries = entriesSignal.get();

            entryList.removeAll();

            for (ValueSignal<Entry> entrySignal : allEntries) {
                Entry entry = entrySignal.get();
                if (entry instanceof ResourceEntry re && re.getResourcesOrEmpty().contains(resource)) {
                    entryList.add(createEntryRow(entrySignal));
                }
            }

            if (entryList.getComponentCount() == 0) {
                Span empty = new Span("No entries");
                empty.getStyle().set("color", "var(--lumo-secondary-text-color)")
                        .set("font-size", "var(--lumo-font-size-s)");
                entryList.add(empty);
            }
        });

        return details;
    }

    private Card createEntryRow(ValueSignal<Entry> entrySignal) {
        Card card = new Card();

        Signal.effect(card, () -> {
            Entry entry = entrySignal.get();
            card.setTitle(entry.getTitle());
            card.setSubtitle(dtFormatter.format(entry.getStartAsLocalDate()) + " - " + dtFormatter.format(entry.getEndAsLocalDate()));
        });

        Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> {
            Entry entry = entrySignal.peek();
            DemoDialog dialog = new DemoDialog(entry, false);
            dialog.setSaveConsumer(saved -> onEntryChanged(saved));
            dialog.setDeleteConsumer(deleted -> onEntriesRemoved(Collections.singletonList(deleted)));
            dialog.open();
        });
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
            Entry target = entrySignal.peek();
            if (target == null) return;
            entriesSignal.peek().stream()
                    .filter(vs -> { Entry v = vs.peek(); return v != null && target.getId().equals(v.getId()); })
                    .findFirst()
                    .ifPresent(entriesSignal::remove);
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);

        card.setHeaderSuffix(new HorizontalLayout(editBtn, deleteBtn));
        return card;
    }

    // ---- Dialogs ----

    private void openResourceDialog(ValueSignal<Resource> resourceSignal) {
        boolean isNew = resourceSignal == null;
        Resource resource = isNew ? new Resource() : resourceSignal.peek();

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "New Resource" : "Edit Resource");
        dialog.setWidth("350px");

        TextField nameField = new TextField("Name");
        nameField.setValue(resource.getTitle() != null ? resource.getTitle() : "");
        nameField.setWidthFull();

        ComboBox<String> colorField = new ComboBox<>("Color", COLORS);
        colorField.setAllowCustomValue(true);
        colorField.addCustomValueSetListener(e -> colorField.setValue(e.getDetail()));
        colorField.setValue(resource.getColor() != null ? resource.getColor() : COLORS[0]);
        colorField.setWidthFull();

        VerticalLayout content = new VerticalLayout(nameField, colorField);
        content.setPadding(false);
        dialog.add(content);

        Button saveBtn = new Button("Save", e -> {
            String name = nameField.getValue();
            if (name == null || name.isBlank()) {
                nameField.setInvalid(true);
                nameField.setErrorMessage("Name is required");
                return;
            }
            if (isNew) {
                Resource newResource = new Resource(null, name, colorField.getValue());
                resourcesSignal.insertLast(newResource);
            } else {
                resourceSignal.modify(r -> {
                    r.setTitle(name);
                    r.setColor(colorField.getValue());
                });
            }
            dialog.close();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelBtn, saveBtn);

        dialog.open();
        nameField.focus();
    }

    private void openNewEntryDialog(Resource resource) {
        ResourceEntry entry = new ResourceEntry();
        entry.setStart(LocalDate.now().atTime(9, 0));
        entry.setEnd(entry.getStart().plusHours(1));
        entry.setColor("green");
        entry.addResources(resource);

        DemoDialog dialog = new DemoDialog(entry, true);
        dialog.setSaveConsumer(e -> onEntriesCreated(Collections.singletonList(e)));
        dialog.open();
    }

    // ---- Toolbar ----

    @Override
    protected CalendarViewToolbar createToolbar(CalendarViewToolbar.CalendarViewToolbarBuilder toolbarBuilder) {
        return super.createToolbar(toolbarBuilder.allowAddingRandomItemsInitially(false));
    }

    // ---- Calendar event handlers ----

    @Override
    protected void onTimeslotsSelectedScheduler(TimeslotsSelectedSchedulerEvent event) {
        ResourceEntry entry = new ResourceEntry();
        entry.setStart(event.getStart());
        entry.setEnd(event.getEnd());
        entry.setAllDay(event.isAllDay());
        entry.setColor("green");
        event.getResource().ifPresent(r -> entry.addResources(r));

        DemoDialog dialog = new DemoDialog(entry, true);
        dialog.setSaveConsumer(e -> onEntriesCreated(Collections.singletonList(e)));
        dialog.open();
    }

    @Override
    protected void onEntryClick(EntryClickedEvent event) {
        DemoDialog dialog = new DemoDialog(event.getEntry(), false);
        dialog.setSaveConsumer(this::onEntryChanged);
        dialog.setDeleteConsumer(e -> onEntriesRemoved(Collections.singletonList(e)));
        dialog.open();
    }

    @Override
    protected void onEntryDroppedScheduler(EntryDroppedSchedulerEvent event) {
        event.applyChangesOnEntry();
        Notification.show("Dropped entry " + event.getEntry().getTitle());
    }

    @Override
    protected void onEntryResized(EntryResizedEvent event) {
        event.applyChangesOnEntry();
        Notification.show("Resized entry " + event.getEntry().getTitle());
    }

    @Override
    protected void onEntriesCreated(Collection<Entry> entries) {
        entries.forEach(entriesSignal::insertLast);
    }

    @Override
    protected void onEntriesRemoved(Collection<Entry> entries) {
        List<ValueSignal<Entry>> signals = entriesSignal.peek();
        for (Entry entry : entries) {
            signals.stream()
                    .filter(vs -> { Entry e = vs.peek(); return e != null && entry.getId().equals(e.getId()); })
                    .findFirst()
                    .ifPresent(entriesSignal::remove);
        }
    }

    @Override
    protected void onEntryChanged(Entry entry) {
        entriesSignal.peek().stream()
                .filter(vs -> { Entry e = vs.peek(); return e != null && entry.getId().equals(e.getId()); })
                .findFirst()
                .ifPresent(vs -> vs.modify(e -> {
                    e.setTitle(entry.getTitle());
                    e.setStart(entry.getStart());
                    e.setEnd(entry.getEnd());
                    e.setAllDay(entry.isAllDay());
                    e.setColor(entry.getColor());
                    e.setDescription(entry.getDescription());
                }));
    }

    @Override
    protected String createDescription() {
        return "Demonstrates bindEntries() and bindResources() together on a FullCalendarScheduler " +
                "(requires Vaadin 25.1+, experimental). Both the calendar and the sidebar are reactively " +
                "driven by ListSignal — the sidebar uses Vaadin's bindChildren() to render resource cards. " +
                "Try adding/editing/deleting resources or entries in the sidebar and watch the calendar update, " +
                "or drag entries between resources in the calendar and watch the sidebar reflect the change.";
    }
}
