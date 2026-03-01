package org.vaadin.stefan.fullcalendar.spike;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Sample POJO for spike testing. Represents a typical backend meeting model
 * that a developer would want to display on a calendar without translating to Entry.
 */
public class SamplePojo {

    private String id;
    private String name;
    private LocalDateTime from;
    private LocalDateTime to;
    private boolean fullDay;
    private String categoryColor;
    private String room;
    private boolean canEdit;
    private Set<DayOfWeek> weekDays;
    private LocalDate repeatStart;
    private Map<String, Object> metadata;
    private Set<String> tags;

    public SamplePojo() {
        this.id = UUID.randomUUID().toString();
    }

    public SamplePojo(String id, String name, LocalDateTime from, LocalDateTime to) {
        this.id = id;
        this.name = name;
        this.from = from;
        this.to = to;
    }

    // Getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getFrom() { return from; }
    public void setFrom(LocalDateTime from) { this.from = from; }

    public LocalDateTime getTo() { return to; }
    public void setTo(LocalDateTime to) { this.to = to; }

    public boolean isFullDay() { return fullDay; }
    public void setFullDay(boolean fullDay) { this.fullDay = fullDay; }

    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public boolean isCanEdit() { return canEdit; }
    public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }

    public Set<DayOfWeek> getWeekDays() { return weekDays; }
    public void setWeekDays(Set<DayOfWeek> weekDays) { this.weekDays = weekDays; }

    public LocalDate getRepeatStart() { return repeatStart; }
    public void setRepeatStart(LocalDate repeatStart) { this.repeatStart = repeatStart; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
}
