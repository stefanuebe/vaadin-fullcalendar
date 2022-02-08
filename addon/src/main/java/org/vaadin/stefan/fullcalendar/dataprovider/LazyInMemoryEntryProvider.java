package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JsonUtils;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.Timezone;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Lazy loading {@link InMemoryEntryProvider}. At this point it uses the {@link AbstractInMemoryEntryProvider} implementation
 * as it is, but as a dedicated class for easier distinction between eager and lazy loading.
 * @author Stefan Uebe
 */
public class LazyInMemoryEntryProvider<T extends Entry> extends AbstractInMemoryEntryProvider<T> {

    public LazyInMemoryEntryProvider() {
    }

    public LazyInMemoryEntryProvider(Iterable<T> entries) {
        super(entries);
    }
}
