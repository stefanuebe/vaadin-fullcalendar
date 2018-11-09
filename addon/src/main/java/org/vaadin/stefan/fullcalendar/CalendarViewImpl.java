package org.vaadin.stefan.fullcalendar;

/**
 * Basic enumeration of possible calendar views.
 */
public enum CalendarViewImpl implements CalendarView {
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

    CalendarViewImpl(String clientSideName) {
        this.clientSideName = clientSideName;
    }

    @Override
    public String getClientSideValue() {
        return clientSideName;
    }
}
