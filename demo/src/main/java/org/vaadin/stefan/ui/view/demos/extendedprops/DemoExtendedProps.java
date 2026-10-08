package org.vaadin.stefan.ui.view.demos.extendedprops;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.JsCallback;
import org.vaadin.stefan.ui.layouts.MainLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import static org.vaadin.stefan.fullcalendar.Option.*;

@Route(value = "demoextendedprops", layout = MainLayout.class)
@MenuItem(label = "Extended Props")
public class DemoExtendedProps extends VerticalLayout {
	private static final long serialVersionUID = -117988331031719049L;

	private FullCalendar calendar;
	
	private Entry selected;

    public DemoExtendedProps() {
    	createCalendarInstance();
    	addDemoEntrys();

        setSizeFull();
        add(calendar);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        setFlexGrow(1, calendar);
    }
    
    private void createCalendarInstance() {
    	calendar = new FullCalendar();
    	
    	calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);

        calendar.setOption(ENTRY_DID_MOUNT,
                JsCallback.of("function (info) {" +
                "info.el.style.backgroundColor = info.event.extendedProps.selected ? 'lightblue' : 'lightgreen';" +
                "}"));

        calendar.addEntryClickedListener(e -> {
            Entry oldSelected = this.selected;
            if (oldSelected != null) {
                oldSelected.setExtendedProp("selected", false);
            }

            var entryProvider = calendar.getEntryProvider().asInMemory();

            this.selected = e.getEntry();
            this.selected.setExtendedProp("selected", true);
            if (oldSelected != null) {
                entryProvider.removeEntries(oldSelected, this.selected);
                entryProvider.addEntries(oldSelected, this.selected);

            } else {
                entryProvider.removeEntries(this.selected);
                entryProvider.addEntries(this.selected);
            }
        });

        calendar.setOption(HEIGHT, "100%");
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
                entry.setExtendedProp("selected", true);
                selected = entry;
            }
            calendar.getEntryProvider().asInMemory().addEntry(entry);
        }
    }

}
