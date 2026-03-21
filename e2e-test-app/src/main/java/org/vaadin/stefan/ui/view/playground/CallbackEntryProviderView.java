package org.vaadin.stefan.ui.view.playground;

import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;

import java.util.Collection;

/**
 * Callback Entry Provider test view at /callback-entry-provider.
 */
@Route("callback-entry-provider")
public class CallbackEntryProviderView extends AbstractEntryProviderView {

    @Override
    protected EntryProvider<Entry> createEntryProvider(EntryService entryService) {
        return EntryProvider.fromCallbacks(
                query -> entryService.streamEntries(query),
                entryId -> entryService.getEntry(entryId).orElse(null)
        );
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
}
