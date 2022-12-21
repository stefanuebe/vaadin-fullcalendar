package org.vaadin.stefan.ui.view.demos.customproperties;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.vaadin.flow.router.PageTitle;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "demoextendedprops", layout = MainLayout.class)
@MenuItem(label = "Custom Properties")
public class DemoCustomProperties extends VerticalLayout {
	private static final long serialVersionUID = -117988331031719049L;

	private FullCalendar calendar;
	
	private Entry selected;

    public DemoCustomProperties() {
    	createCalendarInstance();
    	addDemoEntrys();

        setSizeFull();
        add(calendar);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        setFlexGrow(1, calendar);
    }
    
    private void createCalendarInstance() {
    	calendar = FullCalendarBuilder.create()
                .build();
    	
    	calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);

        calendar.setEntryDidMountCallback("function (info) {" +
                "info.el.style.backgroundColor = info.event.getCustomProperty('selected', false) ? 'lightblue' : 'lightgreen';" +
                "}");

        calendar.addEntryClickedListener(e -> {
            Entry oldSelected = this.selected;
            if (oldSelected != null) {
                oldSelected.setCustomProperty("selected", false);
            }

            var entryProvider = calendar.getEntryProvider().asInMemory();

            this.selected = e.getEntry();
            this.selected.setCustomProperty("selected", true);
            if (oldSelected != null) {
                entryProvider.removeEntries(oldSelected, this.selected);
                entryProvider.addEntries(oldSelected, this.selected);

            } else {
                entryProvider.removeEntries(this.selected);
                entryProvider.addEntries(this.selected);
            }
        });

        calendar.setHeightByParent();
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
            calendar.getEntryProvider().asInMemory().addEntry(entry);
        }
    }
    

}
