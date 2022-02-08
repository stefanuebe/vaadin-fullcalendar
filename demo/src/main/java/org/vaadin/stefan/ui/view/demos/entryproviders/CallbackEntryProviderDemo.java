package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.router.Route;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.dataprovider.AbstractEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.CallbackEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.ui.MainLayout;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
@Route(value = "callback-entry-provider", layout = MainLayout.class)
@org.vaadin.stefan.ui.menu.MenuItem(label = "Callback Entry Provider")
public class CallbackEntryProviderDemo extends AbstractEntryProviderDemo {

    public CallbackEntryProviderDemo() {
        super(true, "TBD: callback entry provider");
    }

    @Override
    protected EntryProvider<Entry> createEntryProvider(EntryService service) {
        return new CallbackEntryProvider<>(service::streamEntries, service::getEntryOrNull);
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


}
