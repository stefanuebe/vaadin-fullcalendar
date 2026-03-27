package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.vaadin.stefan.fullcalendar.Resource;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@SuperBuilder
public class ResourceEntryQuery extends EntryQuery {

    private final Resource resource;

    public ResourceEntryQuery(Resource resource) {
        this.resource = resource;
    }

    public ResourceEntryQuery(Resource resource, LocalDateTime start, LocalDateTime end, @NonNull AllDay allDay) {
        super(start, end, allDay);
        this.resource = resource;
    }

    public ResourceEntryQuery(Resource resource, LocalDateTime start, LocalDateTime end) {
        super(start, end);
        this.resource = resource;
    }

    public ResourceEntryQuery(Resource resource, Instant start, Instant end) {
        super(start, end);
        this.resource = resource;
    }

    public ResourceEntryQuery(Resource resource, Instant start, Instant end, AllDay allDay) {
        super(start, end, allDay);
        this.resource = resource;
    }

    public ResourceEntryQuery() {
        this.resource = null;
    }

    public ResourceEntryQuery(LocalDateTime start, LocalDateTime end, @NonNull AllDay allDay) {
        this(null, start, end, allDay);
    }

    public ResourceEntryQuery(LocalDateTime start, LocalDateTime end) {
        this(null, start, end);
    }

    public ResourceEntryQuery(Instant start, Instant end) {
        this(null, start, end);
    }

    public ResourceEntryQuery(Instant start, Instant end, AllDay allDay) {
        this(null, start, end, allDay);
    }
}