package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.CallbackEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;

import java.util.Collection;

/**
 * This sample shows the usage of the CallbackEntryProvider. This variant can be instantiated on the
 * fly providing a single callback to stream entries for the requested timespan.
 * @author Stefan Uebe
 */
@Route(value = "callback-entry-provider", layout = MainLayout.class)
@org.vaadin.stefan.ui.menu.MenuItem(label = "Callback Entry Provider")
public class CallbackEntryProviderDemo extends AbstractEntryProviderDemo {

    @Override
    protected EntryProvider<Entry> createEntryProvider(EntryService entryService) {
        // Variant A - the backend service takes care of filtering the entries before returning them
        CallbackEntryProvider<Entry> entryProvider = EntryProvider.fromCallbacks(
                query -> entryService.streamEntries(query),
                entryId -> entryService.getEntry(entryId).orElse(null)
        );
        // Variant B - the backend service returns a plain stream an the callback takes care of filtering the returned entries (may be less performant)
        // entryProvider = EntryProvider.fromCallbacks(query -> {
        //     Stream<Entry> stream = entryService.streamEntries();
        //     stream = query.applyFilter(stream); // a query built in method to filter entry streams based on the query
        //     return stream;
        // }, entryId -> entryService.getEntry(entryId).orElse(null));

        return entryProvider;
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

    @Override
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
        return "This sample shows the usage of the CallbackEntryProvider. This variant can be instantiated on the " +
                "fly providing a single callback to stream entries for the requested timespan.";
    }
}
