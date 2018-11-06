package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Flow implementation for the FullCalendar.
 * <p>
 * Please visit <a href="https://fullcalendar.io/">https://fullcalendar.io/</a> for details about the client side
 * component, API, functionality, etc.
 */
@Tag("full-calendar-scheduler")
@HtmlImport("bower_components/fullcalendar/full-calendar-scheduler.html")
public class FullCalendarScheduler extends FullCalendar implements Scheduler {

    private Map<String, Resource> resources = new HashMap<>();

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

    @Override
    public boolean addResource(Resource resource) {
        String id = resource.getId();
        boolean containsKey = resources.containsKey(id);
        if (!containsKey) {
            resources.put(id, resource);
            getElement().callFunction("addResource", resource.toJson());
        }

        return !containsKey;
    }

    @Override
    public void removeResource(Resource resource) {
        String id = resource.getId();
        if (resources.containsKey(id)) {
            resources.remove(id);
            getElement().callFunction("removeResource", resource.toJson());
        }
    }

    @Override
    public Optional<Resource> getResourceById(@Nonnull String id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(resources.get(id));
    }

    @Override
    public Set<Resource> getResources() {
        return new HashSet<>(resources.values());
    }

    @Override
    public void removeAllResources() {
        for (Resource value : resources.values()) {
            getElement().callFunction("removeResource", value.toJson());
        }

        resources.clear();
    }

    /**
     * Set a grouping option for entries based on their assigned resource(s) and date.
     * @param groupEntriesBy group entries by option
     */
    @Override
    public void setGroupEntriesBy(GroupEntriesBy groupEntriesBy) {
        switch (groupEntriesBy) {
            default:
            case NONE:
                setOption("groupByResource", false);
                setOption("groupByDateAndResource", false);
                break;
            case RESOURCE_DATE:
                setOption("groupByDateAndResource", false);
                setOption("groupByResource", true);
                break;
            case DATE_RESOURCE:
                setOption("groupByResource", false);
                setOption("groupByDateAndResource", true);
                break;
        }
    }
}
