package org.vaadin.stefan;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import org.vaadin.stefan.fullcalendar.FullCalendarScheduler;

@Tag("my-full-calendar")
@HtmlImport("frontend://my-full-calendar.html")
public class MyFullCalendar extends FullCalendarScheduler {
    MyFullCalendar(int entryLimit) {
        super(entryLimit);
    }
}
