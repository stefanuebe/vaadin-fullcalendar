package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Test view for signal resource binding (UC-025, Phase 2).
 * <p>
 * Route: /test/signal-resource-binding
 */
@Route(value = "signal-resource-binding", layout = TestLayout.class)
@MenuItem(label = "Signal Resource Binding")
public class SignalResourceBindingTestView extends VerticalLayout {

    private final ListSignal<Resource> resourcesSignal = new ListSignal<>();
    private final ListSignal<Entry> entriesSignal = new ListSignal<>();
    private int resourceCounter = 0;
    private int entryCounter = 0;

    private final Span resourceCount = new Span("0");
    private final Span lastAction = new Span("none");
    private final Span childCount = new Span("0");
    private int childCounter = 0;

    public SignalResourceBindingTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Signal Resource Binding (Phase 2)"));

        resourceCount.setId("resource-count");
        lastAction.setId("last-action");
        childCount.setId("child-count");

        Div status = new Div(
                label("resources: "), resourceCount,
                label(" | children: "), childCount,
                label(" | action: "), lastAction
        );
        status.getStyle().set("font-size", "12px");
        add(status);

        FullCalendarScheduler scheduler = (FullCalendarScheduler) FullCalendarBuilder.create()
                .withScheduler(Scheduler.DEVELOPER_LICENSE_KEY)
                .build();
        scheduler.addThemeVariants(FullCalendarVariant.VAADIN);
        scheduler.setLocale(Locale.UK);
        scheduler.setOption("initialDate", LocalDate.of(2025, 3, 3).toString());
        scheduler.setOption("initialView", SchedulerView.RESOURCE_TIMELINE_WEEK.getClientSideValue());
        scheduler.setOption(FullCalendar.Option.EDITABLE, true);

        // Bind both signals
        scheduler.bindResources(resourcesSignal);
        scheduler.bindEntries(entriesSignal);

        // Buttons
        Button addResourceBtn = new Button("Add Resource", e -> {
            resourceCounter++;
            Resource resource = new Resource(null, "Room " + resourceCounter, "#3788d8");
            resourcesSignal.insertLast(resource);
            updateCount("resource-added");
        });
        addResourceBtn.setId("add-resource-btn");

        Button removeFirstResourceBtn = new Button("Remove First Resource", e -> {
            List<ValueSignal<Resource>> items = resourcesSignal.peek();
            if (!items.isEmpty()) {
                resourcesSignal.remove(items.get(0));
                updateCount("resource-removed");
            }
        });
        removeFirstResourceBtn.setId("remove-first-resource-btn");

        Button modifyFirstResourceBtn = new Button("Modify First Resource Title", e -> {
            List<ValueSignal<Resource>> items = resourcesSignal.peek();
            if (!items.isEmpty()) {
                items.get(0).modify(r -> r.setTitle("Modified-" + System.currentTimeMillis()));
                updateCount("resource-modified");
            }
        });
        modifyFirstResourceBtn.setId("modify-first-resource-btn");

        Button addEntryBtn = new Button("Add Entry to First Resource", e -> {
            List<ValueSignal<Resource>> items = resourcesSignal.peek();
            if (!items.isEmpty()) {
                entryCounter++;
                ResourceEntry entry = new ResourceEntry();
                entry.setTitle("Meeting " + entryCounter);
                entry.setStart(LocalDateTime.of(2025, 3, 3, 9, 0).plusDays(entryCounter - 1));
                entry.setEnd(entry.getStart().plusHours(2));
                entry.addResources(items.get(0).peek());
                entriesSignal.insertLast(entry);
                updateCount("entry-added");
            }
        });
        addEntryBtn.setId("add-entry-btn");

        Button addChildBtn = new Button("Add Child to First Resource", e -> {
            List<ValueSignal<Resource>> items = resourcesSignal.peek();
            if (!items.isEmpty()) {
                childCounter++;
                Resource child = new Resource(null, "Child " + childCounter, "#fb8c00");
                items.get(0).modify(r -> r.addChild(child));
                updateCount("child-added");
            }
        });
        addChildBtn.setId("add-child-btn");

        Button unbindBtn = new Button("Unbind Resources", e -> {
            scheduler.bindResources(null);
            updateCount("unbound");
        });
        unbindBtn.setId("unbind-btn");

        add(addResourceBtn, removeFirstResourceBtn, modifyFirstResourceBtn, addEntryBtn, addChildBtn, unbindBtn);
        add(scheduler);
        setFlexGrow(1, scheduler);
    }

    private void updateCount(String action) {
        resourceCount.setText(String.valueOf(resourcesSignal.peek().size()));
        // Count total children across all resources
        long children = resourcesSignal.peek().stream()
                .map(ValueSignal::peek)
                .filter(java.util.Objects::nonNull)
                .mapToLong(r -> r.getChildren().size())
                .sum();
        childCount.setText(String.valueOf(children));
        lastAction.setText(action);
    }

    private static Span label(String text) {
        return new Span(text);
    }
}
