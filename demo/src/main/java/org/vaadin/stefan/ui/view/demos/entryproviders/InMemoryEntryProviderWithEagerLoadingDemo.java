package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EagerInMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This demo shows the usage of the EagerInMemoryEntryProvider. This entry provider stores all
 * data on the server and the client side and thus might have a huge memory or performance impact.
 * It is basically the old behavior of the FullCalendar's entry management and therefore is the default
 * provider used by new calendar instances.
 *
 * @author Stefan Uebe
 */
@Route(value = "in-memory-entry-provider-with-eager-loading", layout = MainLayout.class)
@org.vaadin.stefan.ui.menu.MenuItem(label = "In Memory Entry Provider (eager)")
public class InMemoryEntryProviderWithEagerLoadingDemo extends AbstractEntryProviderDemo {

    @Override
    protected EntryProvider<Entry> createEntryProvider(EntryService entryService) {
        List<Entry> entriesFromBackend = entryService.streamEntries().collect(Collectors.toList());

        // The list is used to initialize the in memory provider, but different to the ListDataProvider it is
        // not used as the backing collection.
        return EntryProvider.eagerInMemoryFromItems(entriesFromBackend);
    }

    @Override
    protected Entry createNewEntry() {
        return new Entry();
    }

    @Override
    protected void onEntriesCreated(Collection<Entry> entries) {
        // The eager in memory provider provider provides API to modify its internal cache and takes care of pushing
        // the data to the client - no refresh call is needed (or even recommended here)
        getEntryProvider().addEntries(entries);
    }

    @Override
    protected void onEntriesRemoved(Collection<Entry> entries) {
        // The eager in memory provider provider provides API to modify its internal cache and takes care of pushing
        // the data to the client - no refresh call is needed (or even recommended here)
        getEntryProvider().removeEntries(entries);
    }

    @Override
    protected void onEntryChanged(Entry entry) {
        // The eager in memory provider provider provides API to modify its internal cache and takes care of pushing
        // the data to the client - no refresh call is needed (or even recommended here)
        getEntryProvider().updateEntry(entry);
    }

    @Override
    public EagerInMemoryEntryProvider<Entry> getEntryProvider() {
        return (EagerInMemoryEntryProvider<Entry>) super.getEntryProvider();
    }

    @Override
    protected String createDescription() {
        return "This demo shows the usage of the EagerInMemoryEntryProvider. This entry provider stores all " +
                "data on the server and the client side and thus might have a huge memory or performance impact. " +
                "It is basically the old behavior of the FullCalendar's entry management and therefore is the default " +
                "provider used by new calendar instances.";
    }
}
