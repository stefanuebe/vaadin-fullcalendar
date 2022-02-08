package org.vaadin.stefan.ui.view.demos.entryproviders;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
@Service
public class EntryServiceWithReadOnlyDatabase {

    private final Map<String, EntryData> database = new HashMap<>();


    public EntryServiceWithReadOnlyDatabase() {
        System.out.println("Initialize entry service readonly database");
        LocalDate date = LocalDate.of(2010, 1, 1).withDayOfYear(1);
        LocalDate end = LocalDate.now().plusYears(10);

        Random random = new Random();

        while (date.isBefore(end)) {
            int maxDays = date.lengthOfMonth();
            for (int i = 0; i < 8; i++) {
                LocalDate start = date.withDayOfMonth(random.nextInt(maxDays) + 1);
                createAt(start, random.nextBoolean());
            }

            date = date.plusMonths(1);
        }

        System.out.println("Created " + count() + " dates");
    }

    public Optional<Entry> getEntry(String id) {
        return Optional.ofNullable(database.get(id)).map(this::toEntry);
    }

    public int count() {
        return database.size();
    }

    private void createAt(LocalDate date, boolean allDay) {
        if (allDay) {
            EntryData day = new EntryData("" + database.size(), "Day event " + database.size(), date.atStartOfDay(), date.plusDays(1).atStartOfDay(), true);
            database.put(day.getId(), day);
        } else {
            EntryData time = new EntryData("" + database.size(), "Time event " + database.size(), date.atStartOfDay(), date.atTime(10, 0), false);
            database.put(time.getId(), time);
        }

    }

    public Stream<Entry> streamEntries(EntryQuery query) {
        Stream<EntryData> stream = database.values().stream();
        return applyFilter(stream, query).map(this::toEntry)/*.peek(e -> e.setCalendar(query.getSource()))*/;
    }

    public Stream<EntryData> applyFilter(Stream<EntryData> stream, EntryQuery query) {
        LocalDateTime start = query.getStart();
        LocalDateTime end = query.getEnd();
        EntryQuery.AllDay allDay = query.getAllDay();

        if (start == null && end == null && allDay == EntryQuery.AllDay.BOTH) {
            return stream;
        }

        stream = stream.filter(item -> {
            if (start != null && item.getStart().isAfter(end)) {
                return false;
            }

            return !(end != null && item.getEnd().isBefore(start));
        });

        if (allDay != EntryQuery.AllDay.BOTH) {
            Predicate<EntryData> allDayFilter = EntryData::isAllDay;
            if (allDay == EntryQuery.AllDay.TIMED_ONLY) {
                allDayFilter = allDayFilter.negate();
            }

            stream = stream.filter(allDayFilter);
        }

        return stream;
    }

    public Entry toEntry(EntryData entryData) {
        Entry entry = new Entry();
        entry.setTitle(entryData.getTitle());
        entry.setStart(entryData.getStart());
        entry.setEnd(entryData.getEnd());
        entry.setAllDay(entryData.isAllDay());
        return entry;
    }

    @Data
    @AllArgsConstructor
    private static class EntryData {
        private final String id;
        private String title;
        private LocalDateTime start;
        private LocalDateTime end;
        private boolean allDay;
    }
}
