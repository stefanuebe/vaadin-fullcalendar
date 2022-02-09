package org.vaadin.stefan.ui.view.tests;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.AbstractCalendarView;
import org.vaadin.stefan.CalendarViewToolbar;
import org.vaadin.stefan.fullcalendar.Delta;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.view.demos.entryproviders.EntryService;

import java.time.LocalDate;

/**
 * @author Stefan Uebe
 */
@Route(value = "test-remove-add-in-one-cycle", layout = TestLayout.class)
public class RemoveAddInOneCycleTestView extends AbstractCalendarView {

    private Entry entry2;
    private Entry entry1;

    @Override
    protected CalendarViewToolbar createToolbar(CalendarViewToolbar.CalendarViewToolbarBuilder toolbarBuilder) {
        CalendarViewToolbar toolbar = super.createToolbar(toolbarBuilder);

        toolbar.addItem("Remove and Add", event -> {
            TestProvider testProvider = (TestProvider) getEntryProvider();
            testProvider.startRecording();

            testProvider.removeAllEntries();

            Delta delta = Delta.builder().days(1).build();
            entry1.moveStartEnd(delta);
            entry2.moveStartEnd(delta);

            testProvider.addEntries(entry1, entry2);

            Notification.show("Remove and Add done. Entries should have moved one day up. Entry map and json arrays should have data");
        });

        return toolbar;
    }

    @Override
    protected EntryProvider<Entry> createEntryProvider(EntryService service) {
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

        testProvider.addEntries(entry1, entry2);
        return testProvider;
    }

}
