package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;

/**
 * @author Stefan Uebe
 */
public class CustomPropertySample extends AbstractSample{

    private Entry entry;

    @Override
    protected void buildSample(FullCalendar calendar) {
        // set the custom property beforehand
                entry.setCustomProperty(Entry.EntryCustomProperties.DESCRIPTION, "some description");

        // use the custom property
                calendar = FullCalendarBuilder.create()
                        .withEntryContent(
                                "function(info) {" +
                                        "   let entry = info.event;" +
                                        "   console.log(entry.title);" + // standard property
                                        "   console.log(entry.getCustomProperty('" + Entry.EntryCustomProperties.DESCRIPTION+ "'));" + // custom property
                                        "   /* ... do something with the event content ...*/" +
                                        "   return info.el; " +
                                        "}"
                        )
                        // ... other settings
                        .build();

        // or use the custom property in the entryDidMountCallback
    }
}
