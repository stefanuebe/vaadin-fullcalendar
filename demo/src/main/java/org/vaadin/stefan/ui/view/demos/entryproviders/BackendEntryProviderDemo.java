package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.AbstractEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.ui.dialogs.DemoDialog;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.view.AbstractDemoView;
import org.vaadin.stefan.ui.view.CalendarViewToolbar;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Demonstrates a custom EntryProvider backed by a simulated database service.
 * Entries are fetched and persisted through the service layer.
 *
 * @author Stefan Uebe
 */
@Route(value = "backend-entry-provider", layout = MainLayout.class)
@org.vaadin.stefan.ui.menu.MenuItem(label = "Backend Entry Provider")
public class BackendEntryProviderDemo extends AbstractDemoView {

    // DEMO-START
    @Override
    protected FullCalendar<?> createCalendar() {
        EntryService<Entry> service = EntryService.createSimpleInstance();

        // Custom AbstractEntryProvider that delegates fetch operations to the service layer.
        // Use this pattern when you need full control over how entries are loaded and refreshed.
        BackendEntryProvider provider = new BackendEntryProvider(service);

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
        return "Demonstrates a custom EntryProvider backed by a simulated database service. Entries are fetched and persisted through the service layer.";
    }

    /**
     * A custom entry provider that delegates to a service (simulated backend / database).
     * Extend {@link AbstractEntryProvider} when you need full control over entry loading,
     * caching, or update strategies.
     */
    private static class BackendEntryProvider extends AbstractEntryProvider<Entry> {

        private final EntryService<Entry> service;

        BackendEntryProvider(EntryService<Entry> service) {
            this.service = service;
        }

        @Override
        public Stream<Entry> fetch(@NonNull EntryQuery query) {
            return service.streamEntries(query);
        }

        @Override
        public Optional<Entry> fetchById(@NonNull String id) {
            return service.getEntry(id);
        }
    }
}
