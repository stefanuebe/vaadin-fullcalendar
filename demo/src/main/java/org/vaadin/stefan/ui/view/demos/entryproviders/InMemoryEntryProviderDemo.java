package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.dialogs.DemoDialog;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarViewToolbar;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Demonstrates InMemoryEntryProvider — entries are stored server-side and the client only
 * receives visible data. Supports CRUD operations via the provider's add/remove API.
 *
 * @author Stefan Uebe
 */
@Route(value = "in-memory-entry-provider", layout = MainLayout.class)
@org.vaadin.stefan.ui.menu.MenuItem(label = "In Memory Entry Provider")
public class InMemoryEntryProviderDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    @SuppressWarnings("unchecked")
    protected FullCalendar<?> createCalendar() {
        EntryService<Entry> service = EntryService.createSimpleInstance();
        List<Entry> entries = service.streamEntries().collect(Collectors.toList());

        // InMemoryEntryProvider stores all entries server-side;
        // the client only receives data for the visible date range.
        InMemoryEntryProvider<Entry> provider = (InMemoryEntryProvider<Entry>) EntryProvider.inMemoryFrom(entries);

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
                provider.refreshItem(changed);
            });
            dialog.setDeleteConsumer(removed -> {
                provider.removeEntries(Collections.singletonList(removed));
                provider.refreshAll();
            });
            dialog.open();
        });

        // Drop: apply position change and refresh
        calendar.addCalendarItemDroppedListener(event -> {
            event.applyChangesOnItem();
            provider.refreshItem(event.getItem());
            Notification.show("Moved entry: " + event.getItem().getTitle());
        });

        // Resize: apply duration change and refresh
        calendar.addCalendarItemResizedListener(event -> {
            event.applyChangesOnItem();
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
        return "Demonstrates InMemoryEntryProvider — entries are stored server-side and the client only receives visible data. Supports CRUD operations via the provider's add/remove API.";
    }
}
