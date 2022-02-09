package org.vaadin.stefan.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Stefan Uebe
 */
public class LazyInMemoryEntryProviderSample extends AbstractSample {

    private EntryService backend = EntryService.createInstance();

    @Override
    protected void buildSample(FullCalendar calendar) {
// load items from backend
List<Entry> entryList = backend.streamEntries().collect(Collectors.toList());

// init lazy loading provider based on given collection - does NOT use the collection as backend as ListDataProvider does
InMemoryEntryProvider<Entry> entryProvider = EntryProvider.lazyInMemoryFromItems(entryList);

// set entry provider
calendar.setEntryProvider(entryProvider);

// at some later point, modify content by using CRUD operations
Entry newEntry = new Entry();
// ... init entry
entryProvider.addEntries(newEntry);

// call refresh to inform the client about the data change and trigger a refetch
entryProvider.refreshAll();
    }
}