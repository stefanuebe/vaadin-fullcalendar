package org.vaadin.stefan.util;

import java.util.Collection;

import org.vaadin.stefan.fullcalendar.BusinessHours;
import org.vaadin.stefan.fullcalendar.Resource;
import org.vaadin.stefan.fullcalendar.Scheduler;

public class ResourceManager {
	public static Resource createResource(Scheduler calendar, String s, String color) {
        Resource resource = new Resource(null, s, color);
        calendar.addResource(resource);
        return resource;
    }

	public static Resource createResource(Scheduler calendar, String s, String color, Collection<Resource> children) {
        Resource resource = new Resource(null, s, color, children);
        calendar.addResource(resource);
        return resource;
    }
    
	public static Resource createResource(Scheduler calendar, String s, String color, Collection<Resource> children, BusinessHours businessHours) {
        Resource resource = new Resource(null, s, color, children, businessHours);
        calendar.addResource(resource);
        return resource;
    }
}
