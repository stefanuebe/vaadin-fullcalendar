package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.CallbackEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.dialogs.DemoDialog;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarViewToolbar;

import java.util.Collections;

/**
 * Demonstrates CallbackEntryProvider — entries are fetched lazily via callbacks for each date range,
 * ideal for large datasets or backend integration.
 *
 * @author Stefan Uebe
 */
@Route(value = "callback-entry-provider", layout = MainLayout.class)
@org.vaadin.stefan.ui.menu.MenuItem(label = "Callback Entry Provider")
public class CallbackEntryProviderDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        EntryService<Entry> service = EntryService.createSimpleInstance();

        // CallbackEntryProvider fetches entries on demand for the visible date range.
        // Variant A: the service filters entries by the query range (preferred for large datasets).
        CallbackEntryProvider<Entry> provider = EntryProvider.fromCallbacks(
                query -> service.streamEntries(query),
                id -> service.getEntry(id).orElse(null)
        );

        // Variant B: fetch all entries and let the query do the filtering (simpler but less efficient):
        // CallbackEntryProvider<Entry> provider = EntryProvider.fromCallbacks(
        //     query -> {
        //         Stream<Entry> stream = service.streamEntries();
        //         return query.applyFilter(stream);
        //     },
        //     id -> service.getEntry(id).orElse(null)
        // );

        FullCalendar<Entry> calendar = FullCalendarBuilder.<Entry>create()
                .withEntryProvider(provider)
                .withCalendarItemLimit(3)
                .build();

        calendar.addThemeVariants(FullCalendarVariant.VAADIN);
        calendar.setSizeFull();
        calendar.setEditable(true);

        // Click: open edit dialog
        calendar.addCalendarItemClickedListener(event -> {
            Entry entry = event.getItem();
            DemoDialog dialog = new DemoDialog(entry, false);
            dialog.setSaveConsumer(changed -> {
                service.updateEntry(changed);
                provider.refreshItem(changed);
            });
            dialog.setDeleteConsumer(removed -> {
                service.removeEntries(Collections.singletonList(removed));
                provider.refreshAll();
            });
            dialog.open();
        });

        // Drop: apply position change and persist
        calendar.addCalendarItemDroppedListener(event -> {
            event.applyChangesOnItem();
            service.updateEntry(event.getItem());
            provider.refreshItem(event.getItem());
            Notification.show("Moved entry: " + event.getItem().getTitle());
        });

        // Resize: apply duration change and persist
        calendar.addCalendarItemResizedListener(event -> {
            event.applyChangesOnItem();
            service.updateEntry(event.getItem());
            provider.refreshItem(event.getItem());
            Notification.show("Resized entry: " + event.getItem().getTitle());
        });

        return calendar;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Component createToolbar() {
        return CalendarViewToolbar.builder()
                .calendar((FullCalendar<Entry>) (FullCalendar) getCalendar())
                .dateChangeable(true)
                .viewChangeable(true)
                .editable(true)
                .settingsAvailable(true)
                .build();
    }
    // DEMO-END

    @Override
    protected String createDescription() {
        return "Demonstrates CallbackEntryProvider — entries are fetched lazily via callbacks for each date range, ideal for large datasets or backend integration.";
    }
}
