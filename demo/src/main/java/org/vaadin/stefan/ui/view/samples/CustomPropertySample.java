package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.JsCallback;

import static org.vaadin.stefan.fullcalendar.FullCalendar.Option.*;

/**
 * @author Stefan Uebe
 */
public class CustomPropertySample extends AbstractSample{

    private Entry entry;

    @Override
    protected void buildSample(FullCalendar calendar) {
        // set the custom property beforehand
                entry.setCustomProperty(Entry.EntryCustomProperties.DESCRIPTION, "some description");

        // use the custom property in the entryContent callback
        calendar.setOption(ENTRY_CONTENT,
                JsCallback.of("function(info) {" +
                        "   let entry = info.event;" +
                        "   console.log(entry.title);" + // standard property
                        "   console.log(entry.getCustomProperty('" + Entry.EntryCustomProperties.DESCRIPTION+ "'));" + // custom property
                        "   /* ... do something with the event content ...*/" +
                        "   return info.el; " +
                        "}")
        );
    }
}
