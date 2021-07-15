package org.vaadin.stefan.ui.view.demos.extendedprops;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.vaadin.flow.router.PageTitle;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.ui.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "demoextendedprops", layout = MainLayout.class)
@PageTitle("FC with Extended Properties")
@MenuItem(label = "Extended Properties")
public class DemoExtendedProps extends VerticalLayout {
	private static final long serialVersionUID = -117988331031719049L;

	private FullCalendar calendar;
	
	private Entry selected;

    public DemoExtendedProps() {
    	createCalendarInstance();
    	addDemoEntrys();

        setSizeFull();
        addAndExpand(calendar);
    }
    
    private void createCalendarInstance() {
    	calendar = FullCalendarBuilder.create().build();
    	
    	calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);

        calendar.setEntryContentCallback("" +
            "function (info) {" +
                "debugger;" +
                "console.warn(info.event.title + ': ' + info.event.extendedProps?.customProperties?.selected);" +
            "	if (info.event.extendedProps?.customProperties?.selected)" +
            "		info.backgroundColor = 'lightblue';" +
                "else " +
            "		info.backgroundColor = 'lightgreen';" +
            "	" +
            "}");

        calendar.addEntryClickedListener(e -> {
            Entry oldSelected = this.selected;
            oldSelected.setCustomProperty("selected", false);
            this.selected = e.getEntry();
            this.selected.setCustomProperty("selected", true);
            calendar.updateEntries(oldSelected, this.selected);
        });
        
        calendar.setSizeFull();
    }
    
    private void addDemoEntrys() {
    	for (int i = 1; i < 10; i++) {
            LocalDate now = LocalDate.now();
            LocalDate start = now.withDayOfMonth((int)(Math.random() * 28) + 1);
            LocalDate end = start.plusDays(1);
            Entry entry = new Entry(UUID.randomUUID().toString());
            entry.setColor("lightgreen");
            entry.setTitle("Entry " + i);
            entry.setStart(start.atStartOfDay());
            entry.setEnd(end.atTime(LocalTime.MAX));
            if (i == 1) {
                entry.setCustomProperty("selected", true);
                selected = entry;
            }
            calendar.addEntry(entry);
        }
    }
    

}
