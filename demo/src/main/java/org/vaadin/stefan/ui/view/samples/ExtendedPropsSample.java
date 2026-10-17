package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.option.JsCallback;

/**
 * @author Stefan Uebe
 */
public class ExtendedPropsSample extends AbstractSample{

    private Entry entry;

    @Override
    protected void buildSample(FullCalendar calendar) {
        // set the extended prop beforehand
                entry.setExtendedProp("description", "some description");

        // use the extended prop in the entryContent callback
        calendar.options().setEntryContent(
                JsCallback.of("function(info) {" +
                        "   let entry = info.event;" +
                        "   console.log(entry.title);" + // standard property
                        "   console.log(entry.extendedProps.description);" + // extended prop
                        "   /* ... do something with the event content ...*/" +
                        "   return info.el; " +
                        "}")
        );
    }
}
