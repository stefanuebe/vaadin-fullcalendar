/*
 * Copyright 2018, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan;

import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Route(value = "")
public class Demo extends Div {
    private static final String[] COLORS = {"tomato", "orange", "dodgerblue", "mediumseagreen", "gray", "slateblue", "violet"};
    private FullCalendar calendar;
    private ComboBox<CalendarView> comboBoxView;
    private Button buttonDatePicker;
    private Div toolbar;
    private ComboBox<Timezone> timezoneComboBox;

    public Demo() {
        createCalendarInstance();
        add(calendar);

    }

    private void createCalendarInstance() {
        calendar = new FullCalendar(5);

        calendar.addDateClickedListener(event -> {
            System.out.println(event.getDateTime() + " " + event.isAllDay());
        });

        calendar.addLimitedEntriesClickedListener(event -> System.out.println(event.getClickedDate()));
        calendar.addDayNumberClickedListener(event -> System.out.println(event.getDateTime()));
        //        createTestEntries(calendar);
    }

    private void createTestEntries(FullCalendar calendar) {
        LocalDate now = LocalDate.now();


        createTimedEntry(calendar, "Kickoff meeting with customer #1", now.withDayOfMonth(3).atTime(10, 0), 120, null);
        createTimedBackgroundEntry(calendar, now.withDayOfMonth(3).atTime(10, 0), 120, null);
        createTimedEntry(calendar, "Kickoff meeting with customer #2", now.withDayOfMonth(7).atTime(11, 30), 120, "mediumseagreen");
        createTimedEntry(calendar, "Kickoff meeting with customer #3", now.withDayOfMonth(12).atTime(9, 0), 120, "mediumseagreen");
        createTimedEntry(calendar, "Kickoff meeting with customer #4", now.withDayOfMonth(13).atTime(10, 0), 120, "mediumseagreen");
        createTimedEntry(calendar, "Kickoff meeting with customer #5", now.withDayOfMonth(17).atTime(11, 30), 120, "mediumseagreen");
        createTimedEntry(calendar, "Kickoff meeting with customer #6", now.withDayOfMonth(22).atTime(9, 0), 120, "mediumseagreen");

        createTimedEntry(calendar, "Grocery Store", now.withDayOfMonth(7).atTime(17, 30), 45, "violet");
        createTimedEntry(calendar, "Dentist", now.withDayOfMonth(20).atTime(11, 30), 60, "violet");
        createTimedEntry(calendar, "Cinema", now.withDayOfMonth(10).atTime(20, 30), 140, "dodgerblue");
        createDayEntry(calendar, "Short trip", now.withDayOfMonth(17), 2, "dodgerblue");
        createDayEntry(calendar, "John's Birthday", now.withDayOfMonth(23), 1, "gray");
        createDayEntry(calendar, "This special holiday", now.withDayOfMonth(4), 1, "gray");

        createDayEntry(calendar, "Multi 1", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 2", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 3", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 4", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 5", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 6", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 7", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 8", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 9", now.withDayOfMonth(12), 2, "tomato");
        createDayEntry(calendar, "Multi 10", now.withDayOfMonth(12), 2, "tomato");

        createDayBackgroundEntry(calendar, now.withDayOfMonth(4), 6, "#B9FFC3");
        createDayBackgroundEntry(calendar, now.withDayOfMonth(19), 2, "#CEE3FF");
        createTimedBackgroundEntry(calendar, now.withDayOfMonth(20).atTime(11, 0), 150, "#FBC8FF");
    }

    private void createDayEntry(FullCalendar calendar, String title, LocalDate start, int days, String color) {
        Entry entry = new Entry();
        setValues(calendar, entry, title, start.atStartOfDay(), days, ChronoUnit.DAYS, color);
        calendar.addEntry(entry);
    }

    private void createTimedEntry(FullCalendar calendar, String title, LocalDateTime start, int minutes, String color) {
        Entry entry = new Entry();
        setValues(calendar, entry, title, start, minutes, ChronoUnit.MINUTES, color);
        calendar.addEntry(entry);
    }

    private void createDayBackgroundEntry(FullCalendar calendar, LocalDate start, int days, String color) {
        Entry entry = new Entry();
        setValues(calendar, entry, "BG", start.atStartOfDay(), days, ChronoUnit.DAYS, color);

        entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
        calendar.addEntry(entry);
    }

    private void createTimedBackgroundEntry(FullCalendar calendar, LocalDateTime start, int minutes, String color) {
        Entry entry = new Entry();
        setValues(calendar, entry, "BG", start, minutes, ChronoUnit.MINUTES, color);
        entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
        calendar.addEntry(entry);
    }

    private void setValues(FullCalendar calendar, Entry entry, String title, LocalDateTime start, int amountToAdd, ChronoUnit unit, String color) {
        entry.setTitle(title);
        entry.setStart(start, calendar.getTimezone());
        entry.setEnd(entry.getStartUTC().plus(amountToAdd, unit));
        entry.setAllDay(unit == ChronoUnit.DAYS);
        entry.setColor(color);
    }

    private void updateIntervalLabel(HasText intervalLabel, CalendarView view, LocalDate intervalStart) {
        String text = "--";
        Locale locale = calendar.getLocale();

        if (view == null) {
            text = intervalStart.format(DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(locale));
        } else if (view instanceof CalendarViewImpl) {
            switch ((CalendarViewImpl) view) {
                default:
                case MONTH:
                case LIST_MONTH:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(locale));
                    break;
                case AGENDA_DAY:
                case BASIC_DAY:
                case LIST_DAY:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(locale));
                    break;
                case AGENDA_WEEK:
                case BASIC_WEEK:
                case LIST_WEEK:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("dd.MM.yy").withLocale(locale)) + " - " + intervalStart.plusDays(6).format(DateTimeFormatter.ofPattern("dd.MM.yy").withLocale(locale)) + " (cw " + intervalStart.format(DateTimeFormatter.ofPattern("ww").withLocale(locale)) + ")";
                    break;
                case LIST_YEAR:
                    text = intervalStart.format(DateTimeFormatter.ofPattern("yyyy").withLocale(locale));
                    break;
            }
        }

        intervalLabel.setText(text);
    }




}
