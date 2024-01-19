package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.FullCalendar;

/**
 * @author Stefan Uebe
 */
public class ContentInjectionSample extends AbstractSample {
    @Override
    protected void buildSample(FullCalendar calendar) {
        calendar.setEntryDidMountCallback(
                "function(info) {" +
                "   info.el.style.color = 'red';" +
                "   return info.el; " +
                "}"
        );
    }
}
