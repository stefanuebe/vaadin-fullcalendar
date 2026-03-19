package org.vaadin.stefan.ui.view.samples;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.time.LocalDate;

/**
 * Demonstrates entry URL navigation and keyboard-accessible interactive entries.
 * <p>
 * URL entries are rendered as {@code <a>} tags and navigate the browser on click.
 * Interactive entries gain {@code tabindex="0"} and can be activated via keyboard.
 */
public class EntryUrlInteractiveSample extends AbstractSample {

    @Override
    protected void buildSample(FullCalendar calendar) {
        // Make all entries keyboard-accessible globally
        calendar.setOption(FullCalendar.Option.ENTRY_INTERACTIVE, true);

        // URL entry — FullCalendar wraps this in an <a> tag
        Entry link = new Entry();
        link.setTitle("Visit Documentation");
        link.setStart(LocalDate.now());
        link.setAllDay(true);
        link.setUrl("https://vaadin.com/docs");

        // Interactive entry — keyboard-focusable, no URL
        Entry keyboardEntry = new Entry();
        keyboardEntry.setTitle("Press Enter to confirm");
        keyboardEntry.setStart(LocalDate.now().plusDays(1));
        keyboardEntry.setAllDay(true);
        // interactive is inherited from the calendar-level setEventInteractive(true) above;
        // explicit per-entry override is only needed to opt OUT of the global setting:
        // keyboardEntry.setInteractive(false);

        calendar.addEntryClickedListener(e ->
                System.out.println("Entry clicked: " + e.getEntry().getTitle()));

        calendar.getEntryProvider().asInMemory().addEntries(link, keyboardEntry);
    }
}
