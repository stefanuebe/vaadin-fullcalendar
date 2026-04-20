package org.vaadin.stefan.ui.view.demos.autorevert;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import elemental.json.JsonObject;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.EntryDataEvent;
import org.vaadin.stefan.fullcalendar.EntryDroppedEvent;
import org.vaadin.stefan.fullcalendar.EntryResizedEvent;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractCalendarView;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Demo view for the auto-revert feature ({@code autoRevertUnappliedEntryChanges}).
 * <p>
 * Provides two checkboxes: "Auto Revert" toggles whether the calendar reverts client-side
 * entry changes (drop/resize) that are not explicitly applied by the server. "Accept drop/resize
 * (applyChangesOnEntry)" controls whether {@code applyChangesOnEntry()} is called in the drop/resize
 * handler. Use this demo to explore how the calendar behaves under different combinations.
 */
@Route(value = "auto-revert", layout = MainLayout.class)
@MenuItem(label = "Auto Revert")
public class AutoRevertView extends AbstractCalendarView {

    private final AtomicBoolean autoApply = new AtomicBoolean(false);

    @Override
    protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
        FullCalendar calendar = new FullCalendar(defaultInitialOptions);
        ((InMemoryEntryProvider<Entry>) calendar.getEntryProvider())
                .addEntries(EntryService.createSimpleInstance().getEntries());
        calendar.setOption(FullCalendar.Option.MAX_ENTRIES_PER_DAY, 3);
        return calendar;
    }

    // Override ACV's defaults: they unconditionally call applyChangesOnEntry(), which would
    // short-circuit the revert mechanism. Delegate to handleEvent instead, which gates the
    // apply call on the "Accept drop/resize" checkbox.
    @Override
    protected void onEntryDropped(EntryDroppedEvent event) {
        handleEvent(event);
    }

    @Override
    protected void onEntryResized(EntryResizedEvent event) {
        handleEvent(event);
    }

    @Override
    protected void postConstruct(FullCalendar calendar) {
        // Add the two auto-revert-specific checkboxes above the toolbar provided by
        // AbstractCalendarView. Plain Checkbox value-change listeners update the mutable
        // state — no Signals API on the v6.x line.
        Checkbox autoRevertCB = new Checkbox("Auto Revert");
        autoRevertCB.setValue(calendar.isAutoRevertUnappliedEntryChanges());
        autoRevertCB.addValueChangeListener(e ->
                calendar.setAutoRevertUnappliedEntryChanges(Boolean.TRUE.equals(e.getValue())));

        Checkbox autoApplyCB = new Checkbox("Accept drop/resize (applyChangesOnEntry)");
        autoApplyCB.setValue(autoApply.get());
        autoApplyCB.addValueChangeListener(e -> autoApply.set(Boolean.TRUE.equals(e.getValue())));

        addComponentAtIndex(indexOf(getToolbar()), new HorizontalLayout(autoRevertCB, autoApplyCB));
    }

    @Override
    protected String createDescription() {
        return "Toggle the two checkboxes and then drag or resize an entry. 'Auto Revert' (default on) "
                + "controls whether the calendar reverts client-side changes when 'applyChangesOnEntry()' is "
                + "not called on the server. 'Accept drop/resize' controls whether the handler calls "
                + "applyChangesOnEntry(). A notification reports the client-side and server-side state "
                + "after each interaction.";
    }

    private void handleEvent(EntryDataEvent event) {
        if (autoApply.get()) {
            event.applyChangesOnEntry();
        }
        JsonObject jsonObject = event.getJsonObject();
        Entry entry = event.getEntry();
        Notification.show(
                "Client: " + jsonObject.get("start") + " - " + jsonObject.get("end")
                        + " | Server: " + entry.getStartAsLocalDate() + " - " + entry.getEndAsLocalDate());
    }
}
