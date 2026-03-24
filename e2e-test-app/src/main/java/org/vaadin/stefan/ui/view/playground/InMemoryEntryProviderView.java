package org.vaadin.stefan.ui.view.playground;

import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import java.util.Collection;

/**
 * In-Memory Entry Provider test view at /in-memory-entry-provider.
 */
@Route("in-memory-entry-provider")
public class InMemoryEntryProviderView extends AbstractEntryProviderView {

    @Override
    protected EntryProvider<Entry> createEntryProvider(EntryService entryService) {
        return EntryProvider.inMemoryFrom(entryService.getEntries());
    }

    @Override
    protected void onEntriesCreated(Collection<Entry> entries) {
        InMemoryEntryProvider<Entry> provider = (InMemoryEntryProvider<Entry>) getEntryProvider();
        provider.addEntries(entries);
        provider.refreshAll();
    }

    @Override
    protected void onEntriesRemoved(Collection<Entry> entries) {
        InMemoryEntryProvider<Entry> provider = (InMemoryEntryProvider<Entry>) getEntryProvider();
        if (entries.isEmpty()) {
            // "Remove all entries" — clear everything
            provider.removeAllEntries();
        } else {
            provider.removeEntries(entries);
        }
        provider.refreshAll();
    }

    @Override
    protected void onEntryChanged(Entry entry) {
        getEntryProvider().refreshItem(entry);
    }
}
