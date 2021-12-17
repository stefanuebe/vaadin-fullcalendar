package org.vaadin.stefan.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.Resource;
import org.vaadin.stefan.fullcalendar.ResourceEntry;

public class EntryManager {
	public static void createRecurringEvents(FullCalendar calendar) {
        LocalDate now = LocalDate.now();

        ResourceEntry recurring = new ResourceEntry();
        recurring.setTitle(now.getYear() + "'s sunday event");
        recurring.setColor("lightgray");
        recurring.setRecurringDaysOfWeek(Collections.singleton(DayOfWeek.SUNDAY));

        recurring.setRecurringStartDate(now.with(TemporalAdjusters.firstDayOfYear()), calendar.getTimezoneClient());
        recurring.setRecurringEndDate(now.with(TemporalAdjusters.lastDayOfYear()), calendar.getTimezoneClient());
        recurring.setRecurringStartTime(LocalTime.of(14, 0));
        recurring.setRecurringEndTime(LocalTime.of(17, 0));
        recurring.setResourceEditable(true);

        calendar.addEntry(recurring);
    }

	public static void createDayEntry(FullCalendar calendar, String title, LocalDate start, int days, String color) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, title, start.atStartOfDay(), days, ChronoUnit.DAYS, color);
        entry.setResourceEditable(true);

        calendar.addEntry(entry);
    }

	public static void createDayBackgroundEntry(FullCalendar calendar, LocalDate start, int days, String color) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, start.atStartOfDay(), days, ChronoUnit.DAYS, color);

        entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
        entry.setResourceEditable(true);

        calendar.addEntry(entry);
    }

	public static void createTimedBackgroundEntry(FullCalendar calendar, LocalDateTime start, int minutes, String color) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, start, minutes, ChronoUnit.MINUTES, color);
        entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
        entry.setResourceEditable(true);

        calendar.addEntry(entry);
    }
	
	public static void createTimedBackgroundEntry(FullCalendar calendar, LocalDateTime start, int minutes, String color, Resource... resources) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, start, minutes, ChronoUnit.MINUTES, color);
        entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
        if (resources != null && resources.length > 0) {
            entry.assignResources(Arrays.asList(resources));
        }
        entry.setResourceEditable(true);

        calendar.addEntry(entry);
    }
	
	public static void createTimedEntry(FullCalendar calendar, String title, LocalDateTime start, int minutes, String color) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, title, start, minutes, ChronoUnit.MINUTES, color);
        entry.setResourceEditable(true);

        calendar.addEntry(entry);
    }
	
	public static ResourceEntry createTimedEntry(FullCalendar calendar, String title, LocalDateTime start, int minutes, String color, Resource... resources) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, title, start, minutes, ChronoUnit.MINUTES, color);
        if (resources != null && resources.length > 0) {
            entry.assignResources(Arrays.asList(resources));
        }
        entry.setResourceEditable(true);
        calendar.addEntry(entry);
        return entry;
    }
    
    public static void createTimedEntry(FullCalendar calendar, String title, LocalDateTime start, int minutes, String color, HashMap<String, Object> extendedProps, Resource... resources) {
        ResourceEntry entry = new ResourceEntry();
        setValues(calendar, entry, title, start, minutes, ChronoUnit.MINUTES, color, extendedProps);
        if (resources != null && resources.length > 0) 
            entry.assignResources(Arrays.asList(resources));
        entry.setResourceEditable(true);
        entry.setEditable(false);
        calendar.addEntry(entry);
    }

	static void setValues(FullCalendar calendar, Entry entry, String title, LocalDateTime start, int amountToAdd, ChronoUnit unit, String color) {
        entry.setTitle(title);
        entry.setStart(start);
        entry.setEndUTC(entry.getStartUTC().plus(amountToAdd, unit));
        entry.setAllDay(unit == ChronoUnit.DAYS);
        entry.setColor(color);
    }

	static void setValues(FullCalendar calendar, ResourceEntry entry, String title, LocalDateTime start, int amountToAdd, ChronoUnit unit, String color) {
        entry.setTitle(title);
        entry.setStart(start);
        entry.setEndUTC(entry.getStartUTC().plus(amountToAdd, unit));
        entry.setAllDay(unit == ChronoUnit.DAYS);
        entry.setColor(color);
        entry.setCustomProperty("description", "Description of " + title);
    }
    
    static void setValues(FullCalendar calendar, ResourceEntry entry, String title, LocalDateTime start, int amountToAdd, ChronoUnit unit, String color, HashMap<String, Object> extendedProps) {
        entry.setTitle(title);
        entry.setStart(start);
        entry.setEndUTC(entry.getStartUTC().plus(amountToAdd, unit));
        entry.setAllDay(unit == ChronoUnit.DAYS);
        entry.setColor(color);
        entry.setCustomProperties(extendedProps);
    }
    
    static void setValues(FullCalendar calendar, ResourceEntry entry, LocalDateTime start, int amountToAdd, ChronoUnit unit, String color) {
    	entry.setTitle("");
        entry.setStart(start);
        entry.setEndUTC(entry.getStartUTC().plus(amountToAdd, unit));
        entry.setAllDay(unit == ChronoUnit.DAYS);
        entry.setColor(color);
    }
}
