package org.vaadin.stefan.fullcalendar;

public class FullCalendarBuilder {

    private boolean scheduler;
    private int entryLimit;

    public static FullCalendarBuilder create() {
        return new FullCalendarBuilder(false, -1);
    }

    private FullCalendarBuilder(boolean scheduler, int entryLimit) {
        this.scheduler = scheduler;
        this.entryLimit = entryLimit;
    }

    public FullCalendarBuilder withScheduler() {
        return new FullCalendarBuilder(true, entryLimit);
    }

    public FullCalendarBuilder withEntryLimit(int entryLimit) {
        return new FullCalendarBuilder(scheduler, entryLimit);
    }

    public FullCalendar build() {
        if (scheduler) {
            try {
                Class<?> loadClass = getClass().getClassLoader().loadClass("org.vaadin.stefan.fullcalendar.FullCalendarScheduler");
                return (FullCalendar) loadClass.getDeclaredConstructor(int.class).newInstance(entryLimit);
            } catch (ClassNotFoundException ce) {
                throw new RuntimeException("Could not find scheduler extension for FullCalendar on class path. Please check you libraries / dependencies.", ce);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return new FullCalendar(entryLimit);
        }
    }

}
