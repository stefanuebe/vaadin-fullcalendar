package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.router.Route;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.AbstractEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.ui.MainLayout;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
@Route(value = "backend-entry-provider", layout = MainLayout.class)
@org.vaadin.stefan.ui.menu.MenuItem(label = "Backend Entry Provider")
public class BackendEntryProviderDemo extends AbstractEntryProviderDemo {

    public BackendEntryProviderDemo() {
        super(true, "This demo shows an EntryProvider implementation, that fetches its data from " +
                "a simulated database. Items are created temporary based on the respective backend entities.");
    }

    @Override
    protected EntryProvider<Entry> createEntryProvider(EntryService service) {
        return new BackendEntryProvider(service);
    }

    @Override
    protected void onSamplesCreated(List<Entry> entries) {
        getEntryService().addEntries(entries);
        getEntryProvider().refreshAll();
    }

    @Override
    protected void onSamplesRemoved() {
        getEntryService().removeAll();
        getEntryProvider().refreshAll();
    }

    @Override
    protected void onSampleChanged(Entry entry) {
        getEntryService().updateEntry(entry);
        getEntryProvider().refreshAll();
    }

    private static class BackendEntryProvider extends AbstractEntryProvider<Entry> {
        private final EntryService service;

        public BackendEntryProvider(EntryService service) {
            this.service = service;
        }

        @Override
        public Stream<Entry> fetch(@NonNull EntryQuery query) {
            return service.streamEntries(query);
        }
    }

}
