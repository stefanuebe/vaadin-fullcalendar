package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.vaadin.stefan.AbstractCalendarView;
import org.vaadin.stefan.CalendarViewToolbar;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;

import java.util.*;

/**
 * @author Stefan Uebe
 */
@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
public abstract class AbstractEntryProviderDemo extends AbstractCalendarView {
    public static final int MAX_ITEMS_PER_UI = 3000;

    private final String description;

    @Override
    protected String createDescription() {
        return description;
    }

    @Override
    protected CalendarViewToolbar.CalendarViewToolbarBuilder initToolbarBuilder(CalendarViewToolbar.CalendarViewToolbarBuilder toolbarBuilder) {
        return toolbarBuilder.editable(true);
    }

    @Override
    protected void onEntryClick(EntryClickedEvent event) {
        Notification.show("Entry clicked " + event.getEntry().getId());
    }

    @Override
    protected void onEntryDropped(EntryDroppedEvent event) {
        applyChanges(event);
        Notification.show("Dropped entry " + event.getEntry().getId());
    }

    @Override
    protected void onEntryResized(EntryResizedEvent event) {
        applyChanges(event);
        Notification.show("Resized entry " + event.getEntry().getId());
    }

    private void applyChanges(EntryDataEvent event) {
        event.applyChangesOnEntry();
        onSampleChanged(event.getEntry());
    }

    protected abstract EntryProvider<Entry> createEntryProvider(EntryService service);

    protected abstract void onSamplesCreated(Set<Entry> entries);

    protected abstract void onSampleChanged(Entry entry);

    protected abstract void onSamplesRemoved(Set<Entry> entries);






}
