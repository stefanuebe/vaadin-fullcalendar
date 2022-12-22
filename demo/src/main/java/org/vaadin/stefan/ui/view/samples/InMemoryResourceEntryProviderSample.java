package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.ResourceEntry;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Stefan Uebe
 */
public class InMemoryResourceEntryProviderSample extends AbstractSchedulerSample {

    private EntryService<ResourceEntry> backend = EntryService.createResourceInstance();

    @Override
    protected void buildSample(FullCalendar calendar) {
        // load items from backend
        List<ResourceEntry> entryList = backend.streamEntries().collect(Collectors.toList());

        // init lazy loading provider based on given collection - does NOT use the collection as backend as ListDataProvider does
        InMemoryEntryProvider<ResourceEntry> entryProvider = EntryProvider.inMemoryFrom(entryList);

        // set entry provider
        calendar.setEntryProvider(entryProvider);

        // CRUD operations
        // to add
        ResourceEntry entry = new ResourceEntry();       // ... plus some init
        entryProvider.addEntries(entry); // register in data provider
        entryProvider.refreshAll();         // call refresh to inform the client about the data change and trigger a refetch

        // after some change
        entryProvider.refreshItem(entry); // call refresh to inform the client about the data change and trigger a refetch

        // to remove
        entryProvider.removeEntry(entry);
        entryProvider.refreshAll(); // call refresh to inform the client about the data change and trigger a refetch
    }
}