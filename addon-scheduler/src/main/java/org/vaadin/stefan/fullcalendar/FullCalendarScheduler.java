package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

/**
 * Flow implementation for the FullCalendar.
 * <p>
 * Please visit <a href="https://fullcalendar.io/">https://fullcalendar.io/</a> for details about the client side
 * component, API, functionality, etc.
 */
@Tag("full-calendar-scheduler")
@HtmlImport("bower_components/fullcalendar/full-calendar-scheduler.html")
public class FullCalendarScheduler extends FullCalendar implements Scheduler {

    FullCalendarScheduler() {
        super();
    }

    FullCalendarScheduler(int entryLimit) {
        super(entryLimit);
    }

    @Override
    public void setSchedulerLicenseKey(String schedulerLicenseKey) {
        setOption("schedulerLicenseKey", schedulerLicenseKey);
    }
}
