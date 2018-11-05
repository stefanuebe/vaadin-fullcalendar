package org.vaadin.stefan.fullcalendar;

/**
 * Enumeration of possible scheduler views.
 */
public enum SchedulerView implements CalendarView {
    TIMELINE_DAY("timelineDay"),
    TIMELINE_WEEK("timelineWeek"),
    TIMELINE_MONTH("timelineMonth"),
    TIMELINE_YEAR("timelineYear"),
    ;

    private final String clientSideName;

    SchedulerView(String clientSideName) {
        this.clientSideName = clientSideName;
    }

    @Override
    public String getClientSideName() {
        return clientSideName;
    }
}
