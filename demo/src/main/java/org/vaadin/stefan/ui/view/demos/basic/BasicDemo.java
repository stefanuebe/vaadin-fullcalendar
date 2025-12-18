package org.vaadin.stefan.ui.view.demos.basic;

import com.vaadin.flow.router.Route;
import org.vaadin.stefan.ui.dialogs.DemoDialog;
import org.vaadin.stefan.ui.view.AbstractCalendarView;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;
import tools.jackson.databind.node.ObjectNode;

import java.util.Collections;

@Route(value = "basic-demo", layout = MainLayout.class)
@MenuItem(label = "Basic Demo")
public class BasicDemo extends AbstractCalendarView {

    @Override
    protected FullCalendar createCalendar(ObjectNode defaultInitialOptions) {
        EntryService<Entry> simpleInstance = EntryService.createSimpleInstance();

        return FullCalendarBuilder.create()
                .withInitialOptions(defaultInitialOptions)
                .withInitialEntries(simpleInstance.getEntries())
                .withEntryLimit(3)
                .build();
    }

    @Override
    protected String createDescription() {
        return "A simple demo, showing the basic interaction events with the calendar and allow basic modification of entries.";
    }

    @Override
    protected void onEntryClick(EntryClickedEvent event) {
        System.out.println(event.getClass().getSimpleName() + ": " + event);

        if (event.getEntry().getDisplayMode() != DisplayMode.BACKGROUND && event.getEntry().getDisplayMode() != DisplayMode.INVERSE_BACKGROUND) {
            DemoDialog dialog = new DemoDialog(event.getEntry(), false);
            dialog.setSaveConsumer(this::onEntryChanged);
            dialog.setDeleteConsumer(e -> onEntriesRemoved(Collections.singletonList(e)));
            dialog.open();
        }
    }

    @Override
    protected void onTimeslotsSelected(TimeslotsSelectedEvent event) {
        super.onTimeslotsSelected(event);

        ResourceEntry entry = new ResourceEntry();

        entry.setStart(event.getStart());
        entry.setEnd(event.getEnd());
        entry.setAllDay(event.isAllDay());
        entry.setCalendar(event.getSource());

        DemoDialog dialog = new DemoDialog(entry, true);
        dialog.setSaveConsumer(e -> onEntriesCreated(Collections.singletonList(e)));
        dialog.setDeleteConsumer(e -> onEntriesRemoved(Collections.singletonList(e)));
        dialog.open();
    }
}
