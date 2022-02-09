package org.vaadin.stefan.ui.view.tests;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import elemental.json.JsonObject;
import org.vaadin.stefan.ui.view.AbstractCalendarView;
import org.vaadin.stefan.ui.view.CalendarViewToolbar;
import org.vaadin.stefan.fullcalendar.Delta;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.ui.layouts.TestLayout;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * @author Stefan Uebe
 */
@Route(value = "test-add-remove-in-one-cycle", layout = TestLayout.class)
@RouteAlias(value = "", layout = TestLayout.class)
public class AddRemoveInOneCycleTestView extends AbstractCalendarView {

    private Entry entry1;
    private Entry entry2;
    private List<Entry> entries;

    @Override
    protected FullCalendar createCalendar(JsonObject defaultInitialOptions) {
        LocalDate now = LocalDate.now();

        entry1 = new Entry("1");
        entry1.setTitle("Entry 1");
        entry1.setStart(now);
        entry1.setEnd(now.plusDays(1));
        entry1.setAllDay(true);

        entry2 = new Entry("2");
        entry2.setTitle("Entry 2");
        entry2.setAllDay(false);
        entry2.setStart(now.atTime(10, 0));
        entry2.setEnd(now.atTime(11, 0));

        entries = Arrays.asList(entry1, entry2);

        TestProvider testProvider = new TestProvider(thisInstance -> {
            if (thisInstance.isRecording()) {
                System.out.println();
                System.out.println("::: RECORDED DATA ::: ");
                System.out.println("::: Entry map");
                System.out.println(thisInstance.getTmpItemSnapshots());
                System.out.println("::: Json arrays ");
                System.out.println(thisInstance.getCreatedJsonArraysAsSets());
                System.out.println();
            }
        });

        return FullCalendarBuilder.create()
                .withInitialOptions(defaultInitialOptions)
                .withEntryProvider(testProvider)
                .build();
    }

    @Override
    protected CalendarViewToolbar createToolbar(CalendarViewToolbar.CalendarViewToolbarBuilder toolbarBuilder) {
        CalendarViewToolbar toolbar = CalendarViewToolbar.builder().calendar(getCalendar()).build();

        toolbar.addItem("Add and Remove", event -> {
            TestProvider testProvider = (TestProvider) getEntryProvider();
            testProvider.startRecording();
            testProvider.addEntries(entries);

            Delta delta = Delta.builder().days(1).build();
            entry1.moveStartEnd(delta);
            entry2.moveStartEnd(delta);

            testProvider.removeEntries(entries);

            Notification.show("Add and Remove done. Calendar should still be empty. Entry map should have data, json arrays should be empty.");
        });

        return toolbar;
    }

}
