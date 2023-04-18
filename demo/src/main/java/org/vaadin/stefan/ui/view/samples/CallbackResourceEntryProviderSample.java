package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.ResourceEntry;
import org.vaadin.stefan.fullcalendar.dataprovider.CallbackEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

/**
 * @author Stefan Uebe
 */
public class CallbackResourceEntryProviderSample extends AbstractSchedulerSample {

    private final EntryService<ResourceEntry> backend = EntryService.createResourceInstance();

    @Override
    protected void buildSample(FullCalendar calendar) {
        // the callback provider uses the given callback to fetch entries when necessary
        CallbackEntryProvider<ResourceEntry> entryProvider = EntryProvider.fromCallbacks(
                backend::streamEntries,
                entryId -> backend.getEntry(entryId).orElse(null)
        );

        // set entry provider
        calendar.setEntryProvider(entryProvider);

        // CRUD operations
        // to add
        ResourceEntry entry = new ResourceEntry();          // ... plus some init
        backend.addEntries(entry);            // register in your backend
        entryProvider.refreshAll();         // call refresh to inform the client about the data change and trigger a refetch

        // after some change
        backend.updateEntry(entry);         // inform your backend
        entryProvider.refreshItem(entry);   // call refresh to inform the client about the data change and trigger a refetch

        // to remove
        backend.removeEntries(entry);         // remove from your backend
        entryProvider.refreshAll();   // call refresh to inform the client about the data change and trigger a refetch
    }
}