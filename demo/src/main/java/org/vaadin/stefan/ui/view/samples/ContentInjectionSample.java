package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.option.JsCallback;

/**
 * @author Stefan Uebe
 */
public class ContentInjectionSample extends AbstractSample {
    @Override
    protected void buildSample(FullCalendar calendar) {
        calendar.options().setEntryDidMount(
                JsCallback.of("function(info) {" +
                "   info.el.style.color = 'red';" +
                "   return info.el; " +
                "}")
        );
    }
}
