package org.vaadin.stefan.ui.view.demos.calendaritemprovider;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Sample POJO used by CIP demo views to show arbitrary domain objects on the calendar.
 */
public class Meeting {
    private String id;
    private String subject;
    private LocalDateTime begin;
    private LocalDateTime finish;
    private boolean allDay;
    private String color;
    private Set<String> resourceIds;

    public Meeting() {
        this.id = UUID.randomUUID().toString();
    }

    public Meeting(String id, String subject, LocalDateTime begin, LocalDateTime finish, String color) {
        this.id = id;
        this.subject = subject;
        this.begin = begin;
        this.finish = finish;
        this.color = color;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public LocalDateTime getBegin() { return begin; }
    public void setBegin(LocalDateTime begin) { this.begin = begin; }

    public LocalDateTime getFinish() { return finish; }
    public void setFinish(LocalDateTime finish) { this.finish = finish; }

    public boolean isAllDay() { return allDay; }
    public void setAllDay(boolean allDay) { this.allDay = allDay; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Set<String> getResourceIds() { return resourceIds; }
    public void setResourceIds(Set<String> resourceIds) { this.resourceIds = resourceIds; }
}
