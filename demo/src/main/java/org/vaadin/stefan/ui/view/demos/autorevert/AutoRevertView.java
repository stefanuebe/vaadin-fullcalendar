package org.vaadin.stefan.ui.view.demos.autorevert;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.EntryDataEvent;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.AbstractCalendarView;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;
import tools.jackson.databind.node.ObjectNode;

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

    // NOTE: not initialised inline — AbstractCalendarView's constructor calls
    // createCalendar + postConstruct before this subclass's field initialisers run,
    // so we defer instantiation to postConstruct. The drop/resize handler is registered
    // from createCalendar, which runs first; handleEvent therefore checks for null.
    private ValueSignal<Boolean> autoApply;

    @Override
    protected FullCalendar createCalendar(ObjectNode defaultInitialOptions) {
        FullCalendar calendar = new FullCalendar(defaultInitialOptions);
        ((InMemoryEntryProvider<Entry>) calendar.getEntryProvider())
                .addEntries(EntryService.createSimpleInstance().getEntries());
        calendar.setOption(FullCalendar.Option.MAX_ENTRIES_PER_DAY, 3);

        calendar.addEntryDroppedListener(this::handleEvent);
        calendar.addEntryResizedListener(this::handleEvent);
        return calendar;
    }

    @Override
    protected void postConstruct(FullCalendar calendar) {
        // Add the two auto-revert-specific checkboxes above the toolbar provided by
        // AbstractCalendarView. Both bind a ValueSignal to the checkbox value; a Signal.effect
        // wires the "Auto Revert" flag through to the FullCalendar option reactively.
        autoApply = new ValueSignal<>(false);
        ValueSignal<Boolean> autoRevert = new ValueSignal<>(calendar.isAutoRevertUnappliedEntryChanges());

        Checkbox autoRevertCB = new Checkbox("Auto Revert");
        autoRevertCB.bindValue(autoRevert, autoRevert::set);

        Checkbox autoApplyCB = new Checkbox("Accept drop/resize (applyChangesOnEntry)");
        autoApplyCB.bindValue(autoApply, autoApply::set);

        Signal.effect(calendar, () -> calendar.setAutoRevertUnappliedEntryChanges(autoRevert.get()));

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
        // autoApply is lazily initialised in postConstruct; drop/resize can only fire after
        // the component is attached and the user interacts, so this check is purely defensive.
        if (autoApply != null && autoApply.peek()) {
            event.applyChangesOnEntry();
        }
        ObjectNode jsonObject = event.getJsonObject();
        Entry entry = event.getEntry();
        Notification.show(
                "Client: " + jsonObject.get("start") + " - " + jsonObject.get("end")
                        + " | Server: " + entry.getStart().toLocalDate() + " - " + entry.getEnd().toLocalDate());
    }
}
