package org.vaadin.stefan.ui.view.playground;

import com.vaadin.flow.router.Route;
import jakarta.annotation.Nonnull;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.dataprovider.AbstractEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Backend Entry Provider test view at /backend-entry-provider.
 * Uses a custom AbstractEntryProvider implementation.
 */
@Route("backend-entry-provider")
public class BackendEntryProviderView extends AbstractEntryProviderView {

    @Override
    protected EntryProvider<Entry> createEntryProvider(EntryService entryService) {
        return new BackendEntryProvider(entryService);
    }

    @Override
    protected void onEntriesCreated(Collection<Entry> entries) {
        getEntryService().addEntries(entries);
        getEntryProvider().refreshAll();
    }

    @Override
    protected void onEntriesRemoved(Collection<Entry> entries) {
        if (entries.isEmpty()) {
            getEntryService().removeAll();
        } else {
            getEntryService().removeEntries(entries);
        }
        getEntryProvider().refreshAll();
    }

    @Override
    protected void onEntryChanged(Entry entry) {
        getEntryService().updateEntry(entry);
        getEntryProvider().refreshItem(entry);
    }

    /**
     * Custom entry provider that delegates to EntryService.
     */
    private static class BackendEntryProvider extends AbstractEntryProvider<Entry> {

        private final EntryService service;

        public BackendEntryProvider(EntryService service) {
            this.service = service;
        }

        @Override
        public Stream<Entry> fetch(@Nonnull EntryQuery query) {
            return service.streamEntries(query);
        }

        @Override
        public Optional<Entry> fetchById(@Nonnull String id) {
            return service.getEntry(id);
        }
    }
}
