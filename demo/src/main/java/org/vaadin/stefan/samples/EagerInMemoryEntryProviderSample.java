package org.vaadin.stefan.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.dataprovider.EagerInMemoryEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Stefan Uebe
 */
public class EagerInMemoryEntryProviderSample extends AbstractSample {

    private EntryService backend = EntryService.createInstance();

    @Override
    protected void buildSample(FullCalendar calendar) {
        // load items from backend
        List<Entry> entryList = backend.streamEntries().collect(Collectors.toList());

        // since the calendar is initialized with an eager in memory provider, the next two calls are optional
        EagerInMemoryEntryProvider<Entry> entryProvider = EntryProvider.eagerInMemoryFromItems(entryList);
        calendar.setEntryProvider(entryProvider);

        // CRUD operations - we should not call refreshAll, since that will send ALL data back to the client
        // The eager in memory provider takes care of that itself.

        // to add
        Entry entry = new Entry();       // ... plus some init
        entryProvider.addEntries(entry); // register in data provider

        // after some change
        entryProvider.updateEntry(entry);

        // to remove
        entryProvider.removeEntry(entry);
    }
}