package org.vaadin.stefan.ui.view.demos.entryproviders;

import com.vaadin.flow.spring.annotation.UIScope;
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
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
@Service
@UIScope
public class EntryService {

    private final Map<String, EntryData> database = new HashMap<>();

    public Optional<Entry> getEntry(String id) {
        return Optional.ofNullable(database.get(id)).map(this::toEntry);
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

    public EntryService() {
        initDatabase();
    }

    private void initDatabase() {
        LocalDate date = LocalDate.now().withDayOfYear(1);
        LocalDate end = date.plusYears(1);

        int ids = 0;
        while (date.isBefore(end)) {
            LocalDate start = date.withDayOfMonth(10);

            EntryData day = new EntryData("" + ids++, "Day event " + date.getMonth(), start.atStartOfDay(), start.plusDays(1).atStartOfDay(), true);
            EntryData time = new EntryData("" + ids++, "Time event " + date.getMonth(), start.atStartOfDay(), start.atTime(10,0), false);

            database.put(day.getId(), day);
            database.put(time.getId(), time);

            date = date.plusMonths(1);
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
}
