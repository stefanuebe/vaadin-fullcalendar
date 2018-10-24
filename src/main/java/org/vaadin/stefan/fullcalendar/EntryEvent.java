package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;

public class EntryEvent extends ComponentEvent<FullCalendar> {
    private final Entry entry;

    public EntryEvent(FullCalendar source, boolean fromClient, String id) {
        super(source, fromClient);
        this.entry = source.getEntryById(id).orElseThrow(IllegalArgumentException::new);
    }

    public Entry getEntry() {
        return entry;
    }
}
