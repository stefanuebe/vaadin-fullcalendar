package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.combobox.ComboBox;
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
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    @Override
    protected FullCalendar createCalendar(ObjectNode defaultInitialOptions) {
        entriesSignal = new ListSignal<>();
        resourcesSignal = new ListSignal<>();

        // Pre-populate resources
        Resource roomA = new Resource(null, "Room A", "#3788d8");
        Resource roomB = new Resource(null, "Room B", "#e53935");
        Resource roomC = new Resource(null, "Room C", "#43a047");
        resourcesSignal.insertLast(roomA);
        resourcesSignal.insertLast(roomB);
        resourcesSignal.insertLast(roomC);

        // Pre-populate entries with resource assignments
        EntryService<ResourceEntry> entryService = EntryService.createRandomResourceInstance();
        Resource[] rooms = {roomA, roomB, roomC};
        int i = 0;
        for (ResourceEntry entry : (Iterable<ResourceEntry>) entryService.streamEntries()::iterator) {
            entry.addResources(rooms[i % rooms.length]);
            entriesSignal.insertLast(entry);
            i++;
        }

        FullCalendarScheduler scheduler = (FullCalendarScheduler) FullCalendarBuilder.create()
                .withScheduler(Scheduler.GPL_V3_LICENSE_KEY)
                .withInitialOptions(defaultInitialOptions)
                .withEntryLimit(3)
                .build();

        scheduler.setOption("initialView", SchedulerView.RESOURCE_TIMELINE_WEEK.getClientSideValue());
        scheduler.setOption(FullCalendarScheduler.SchedulerOption.ENTRY_RESOURCES_EDITABLE, true);

        scheduler.bindResources(resourcesSignal);
        scheduler.bindEntries(entriesSignal);

        return scheduler;
    }

    @Override
    protected void postConstruct(FullCalendar calendar) {
        // Remove calendar from the VerticalLayout (added by AbstractCalendarView)
        // and wrap it in a HorizontalLayout with the sidebar
        remove(calendar);

        VerticalLayout sidebar = createSidebar();
        sidebar.setWidth("300px");
        sidebar.setMinWidth("300px");

        Scroller sidebarScroller = new Scroller(sidebar);
        sidebarScroller.setWidth("300px");
        sidebarScroller.setMinWidth("300px");
        sidebarScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

        HorizontalLayout content = new HorizontalLayout(sidebarScroller, calendar);
        content.setSizeFull();
        content.setFlexGrow(1, calendar);
        content.setFlexGrow(0, sidebarScroller);

        add(content);
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

    private Card createResourceCard(ValueSignal<Resource> resourceSignal) {
        Card card = new Card();
        card.setWidthFull();

        // Title bound to resource name
        Div title = new Div();
        Signal.effect(title, () -> {
            Resource r = resourceSignal.get();
            title.setText(r.getTitle());
            title.getStyle().set("font-weight", "bold");
            if (r.getColor() != null) {
                title.getStyle().set("color", r.getColor());
            }
        });

        // Action buttons
        Button editBtn = new Button(VaadinIcon.EDIT.create(), e ->
                openResourceDialog(resourceSignal));
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
            resourcesSignal.remove(resourceSignal);
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        Button addEntryBtn = new Button(VaadinIcon.PLUS.create(), e ->
                openNewEntryDialog(resourceSignal.peek()));
        addEntryBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(title, editBtn, addEntryBtn, deleteBtn);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setFlexGrow(1, title);
        header.setWidthFull();
        card.setHeader(header);

        // Entry list inside the card — rebuilt via effect
        VerticalLayout entryList = new VerticalLayout();
        entryList.setPadding(false);
        entryList.setSpacing(false);

        // This effect rebuilds the entry list when resources, entries, or entry properties change.
        // Note: using .get() on each entry signal registers dependencies on ALL entries,
        // so any entry change rebuilds all resource cards. Acceptable for a demo.
        Signal.effect(card, () -> {
            Resource resource = resourceSignal.get();
            List<ValueSignal<Entry>> allEntries = entriesSignal.get();

            entryList.removeAll();

            for (ValueSignal<Entry> entrySignal : allEntries) {
                Entry entry = entrySignal.get(); // .get() establishes dependency — effect re-fires on entry property changes
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

        card.add(entryList);
        return card;
    }

    private Div createEntryRow(ValueSignal<Entry> entrySignal) {
        Div row = new Div();
        row.getStyle().set("display", "flex").set("align-items", "center")
                .set("gap", "4px").set("padding", "2px 0");

        Span titleSpan = new Span();
        titleSpan.getStyle().set("flex-grow", "1").set("font-size", "var(--lumo-font-size-s)")
                .set("overflow", "hidden").set("text-overflow", "ellipsis").set("white-space", "nowrap");
        Signal.effect(titleSpan, () -> titleSpan.setText(entrySignal.get().getTitle()));

        Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> {
            Entry entry = entrySignal.peek();
            DemoDialog dialog = new DemoDialog(entry, false);
            dialog.setSaveConsumer(saved -> onEntryChanged(saved));
            dialog.setDeleteConsumer(deleted -> onEntriesRemoved(Collections.singletonList(deleted)));
            dialog.open();
        });
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
            List<ValueSignal<Entry>> signals = entriesSignal.peek();
            signals.stream()
                    .filter(vs -> entrySignal.peek().getId().equals(vs.peek().getId()))
                    .findFirst()
                    .ifPresent(entriesSignal::remove);
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);

        row.add(titleSpan, editBtn, deleteBtn);
        return row;
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
                    .filter(vs -> entry.getId().equals(vs.peek().getId()))
                    .findFirst()
                    .ifPresent(entriesSignal::remove);
        }
    }

    @Override
    protected void onEntryChanged(Entry entry) {
        entriesSignal.peek().stream()
                .filter(vs -> entry.getId().equals(vs.peek().getId()))
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
        return "Signal binding for a scheduler with resources. " +
                "The sidebar shows resource cards with their entries — all managed via ListSignal. " +
                "Changes in the sidebar update the calendar, and drag/drop in the calendar updates the sidebar.";
    }
}
