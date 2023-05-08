package org.vaadin.stefan.ui.view.samples;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

/**
 * @author Stefan Uebe
 */
public class CrudSample extends AbstractSample {
    private boolean newInstance;
    private Binder<?> binder;
    private Entry entry;

    @Override
    protected void buildSample(FullCalendar calendar) {
        // ... create a form and binder to provide editable components to the user

        InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();

        HorizontalLayout buttons = new HorizontalLayout();
        Button buttonSave;
        if (newInstance) {
            buttonSave = new Button("Create", e -> {
                if (binder.validate().isOk()) {
                    // add the entry to the calendar instance
                    entryProvider.addEntry(entry);
                    entryProvider.refreshAll();
                }
            });
        } else {
            buttonSave = new Button("Save", e -> {
                if (binder.validate().isOk()) {
                    // update an existing entry in the client side
                    // this will only send changed data
                    entryProvider.refreshItem(entry);
                }
            });
        }
        buttons.add(buttonSave);

        if (!newInstance) {
            Button buttonRemove = new Button("Remove", e -> {
                entryProvider.removeEntry(entry);
                entryProvider.refreshAll();
            });
            buttons.add(buttonRemove);
        }
    }
}
