package org.vaadin.stefan;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.vaadin.flow.router.PageTitle;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "demoextendedprops", layout = MainView.class)
@PageTitle("FC with Extended Properties")
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

//        calendar.setEntryContentCallback("" +
//            "function (info) {" +
//            "   console.warn(info.event.title + ': ' + info.event.extendedProps['selected']);" +
//            "	if (info.event.extendedProps['selected'])" +
//            "		info.el.style.setProperty('border', '2px solid red');" +
//            "	return info.el;" +
//            "}");
         /*calendar.setEntryRenderCallback("" +
            "function (info) {" +
            "   console.warn(info.event.title + ': ' + info.event.extendedProps['selected']);" +
            "	if (info.event.extendedProps['selected'])" +
            "		info.el.style.setProperty('border', '2px solid red');" +
            "	return info.el;" +
            "}");*/

        calendar.addEntryClickedListener(e -> {
            Entry oldSelected = this.selected;
            oldSelected.removeExtendedProps("selected");
            this.selected = e.getEntry();
            this.selected.addExtendedProps("selected", true);
            calendar.updateEntries(oldSelected, this.selected);
        });
        
        calendar.setSizeFull();
    }
    
    private void addDemoEntrys() {
    	for (int i = 1; i < 10; i++) {
            LocalDate start = LocalDate.of(2021, 6, (int)(Math.random() * 29) + 1);
            LocalDate end = start.plusDays(1);
            Entry entry = new Entry(UUID.randomUUID().toString());
            entry.setColor("lightgreen");
            entry.setTitle("Entry " + i);
            entry.setStart(start.atStartOfDay());
            entry.setEnd(end.atTime(LocalTime.MAX));
            if (i == 1) {
                entry.addExtendedProps("selected", true);
                selected = entry;
            }
            calendar.addEntry(entry);
        }
    }
    

}
