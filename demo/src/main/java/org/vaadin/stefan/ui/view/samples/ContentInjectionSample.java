package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.JsCallback;

import static org.vaadin.stefan.fullcalendar.FullCalendar.Option.*;

/**
 * @author Stefan Uebe
 */
public class ContentInjectionSample extends AbstractSample {
    @Override
    protected void buildSample(FullCalendar calendar) {
        calendar.setOption(ENTRY_DID_MOUNT,
                JsCallback.of("function(info) {" +
                "   info.el.style.color = 'red';" +
                "   return info.el; " +
                "}")
        );
    }
}
