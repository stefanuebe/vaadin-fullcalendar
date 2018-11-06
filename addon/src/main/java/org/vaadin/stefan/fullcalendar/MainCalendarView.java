package org.vaadin.stefan.fullcalendar;

/**
 * Basic enumeration of possible calendar views.
 */
public enum MainCalendarView implements CalendarView {
    MONTH("month"),
    AGENDA_DAY("agendaDay"),
    AGENDA_WEEK("agendaWeek"),
    BASIC_DAY("basicDay"),
    BASIC_WEEK("basicWeek"),
    LIST_WEEK("listWeek"),
    LIST_DAY("listDay"),
    LIST_MONTH("listMonth"),
    LIST_YEAR("listYear"),
    ;

    private final String clientSideName;

    MainCalendarView(String clientSideName) {
        this.clientSideName = clientSideName;
    }

    @Override
    public String getClientSideName() {
        return clientSideName;
    }
}
