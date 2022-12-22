package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This demo shows the usage of the InMemoryEntryProvider. It stores all its data on the server side
 * while the client only receives the necessary data. This allows a mixture of easy in memory editing
 * via the CRUD API without a heavy memory impact on the client.
 *
 * @author Stefan Uebe
 */
@Route(value = "in-memory-entry-provider", layout = MainLayout.class)
@org.vaadin.stefan.ui.menu.MenuItem(label = "In Memory Entry Provider")
public class InMemoryEntryProviderDemo extends AbstractEntryProviderDemo {


    @Override
    protected EntryProvider<Entry> createEntryProvider(EntryService<Entry> entryService) {
        List<Entry> entries = entryService.streamEntries().collect(Collectors.toList());

        // The list is used to initialize the in memory provider, but different to the ListDataProvider it is
        // not used as the backing collection.
        return EntryProvider.inMemoryFrom(entries);
    }

    @Override
    protected Entry createNewEntry() {
        return new Entry();
    }

    @Override
    protected void onEntriesCreated(Collection<Entry> entries) {
        // The lazy in memory provider provides API to modify its internal cache. To inform the client about
        // the change a refresh call is necessary.
        InMemoryEntryProvider<Entry> provider = getEntryProvider();
        provider.addEntries(entries);
        provider.refreshAll();
    }

    @Override
    protected void onEntriesRemoved(Collection<Entry> entries) {
        // The lazy in memory provider provides API to modify its internal cache. To inform the client about
        // the change a refresh call is necessary.
        InMemoryEntryProvider<Entry> provider = getEntryProvider();
        provider.removeEntries(entries);
        provider.refreshAll();
    }

    @Override
    protected void onEntryChanged(Entry entry) {
        // To inform the client about the change a refresh call is necessary.
        getEntryProvider().refreshItem(entry);
    }

    @Override
    protected InMemoryEntryProvider<Entry> getEntryProvider() {
        return (InMemoryEntryProvider<Entry>) super.getEntryProvider();
    }

    @Override
    protected String createDescription() {
        return "This demo shows the usage of the InMemoryEntryProvider. It stores all its data on the server side " +
                "while the client only receives the necessary data. This allows a mixture of easy in memory editing " +
                "via the CRUD API without a heavy memory impact on the client.";
    }
}
