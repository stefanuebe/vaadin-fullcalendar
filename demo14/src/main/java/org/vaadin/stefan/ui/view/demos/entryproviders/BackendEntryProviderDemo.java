package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.router.Route;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.AbstractEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.ui.layouts.MainLayout;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This demo shows an EntryProvider implementation, that fetches its data from
 * a simulated database. Items are created temporary based on the respective backend entities.
 * @author Stefan Uebe
 */
@Route(value = "backend-entry-provider", layout = MainLayout.class)
@org.vaadin.stefan.ui.menu.MenuItem(label = "Backend Entry Provider")
public class BackendEntryProviderDemo extends AbstractEntryProviderDemo {

    @Override
    protected EntryProvider<Entry> createEntryProvider(EntryService<Entry> entryService) {
        return new BackendEntryProvider(entryService);
    }

    @Override
    protected Entry createNewEntry() {
        return getEntryService().createNewInstance();
    }

    @Override
    protected void onEntriesCreated(Collection<Entry> entries) {
        getEntryService().addEntries(entries);
        getEntryProvider().refreshAll();
    }

    protected void onEntryChanged(Entry entry) {
        getEntryService().updateEntry(entry);
        getEntryProvider().refreshItem(entry);
    }

    @Override
    protected void onEntriesRemoved(Collection<Entry> entries) {
        getEntryService().removeEntries(entries);
        getEntryProvider().refreshAll();
    }

    @Override
    protected String createDescription() {
        return "This demo shows an EntryProvider implementation, that fetches its data from " +
                "a simulated database. Items are created temporary based on the respective backend entities.";
    }

    private static class BackendEntryProvider extends AbstractEntryProvider<Entry> {

        private final EntryService service;

        public BackendEntryProvider(EntryService service) {
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
