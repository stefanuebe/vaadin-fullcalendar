package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EagerInMemoryEntryProvider;
import org.vaadin.stefan.ui.MainLayout;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Stefan Uebe
 */
@Route(value = "in-memory-entry-provider-with-eager-loading", layout = MainLayout.class)
@org.vaadin.stefan.ui.menu.MenuItem(label = "In Memory Entry Provider (eager)")
public class InMemoryEntryProviderWithEagerLoadingDemo extends AbstractEntryProviderDemo {

    public InMemoryEntryProviderWithEagerLoadingDemo() {
        super(true, "TBD: eager in memory provider");
    }

    @Override
    protected EntryProvider<Entry> createEntryProvider(EntryService service) {
        return new EagerInMemoryEntryProvider<>(service.streamEntries().collect(Collectors.toList()));
    }

    @Override
    protected void onSamplesCreated(List<Entry> entries) {
        EagerInMemoryEntryProvider<Entry> provider = getEntryProvider();
        provider.addEntries(entries);
    }

    @Override
    protected void onSamplesRemoved() {
        EagerInMemoryEntryProvider<Entry> provider = getEntryProvider();
        provider.removeAllEntries();
    }

    @Override
    protected EagerInMemoryEntryProvider<Entry> getEntryProvider() {
        return (EagerInMemoryEntryProvider<Entry>) super.getEntryProvider();
    }
}
