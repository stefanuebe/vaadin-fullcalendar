package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.dataprovider.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * Internal {@link EntryProvider} implementation backed by a {@link ListSignal}.
 * <p>
 * This provider uses two levels of reactive effects:
 * <ul>
 *   <li>A <b>list-level effect</b> that observes structural changes (add/remove) on the ListSignal</li>
 *   <li><b>Per-entry effects</b> for each ValueSignal&lt;Entry&gt; in the list, observing property changes via modify()</li>
 * </ul>
 * <p>
 * This class is package-private — it is only instantiated by {@link FullCalendar#bindEntries(ListSignal)}.
 *
 * @param <T> entry type
 */
class SignalEntryProvider<T extends Entry> extends AbstractEntryProvider<T> {

    private final ListSignal<T> listSignal;

    private Registration listEffectRegistration;

    private final Map<ValueSignal<T>, Registration> entryEffectRegistrations = new IdentityHashMap<>();

    private final Set<ValueSignal<T>> trackedSignals = Collections.newSetFromMap(new IdentityHashMap<>());

    SignalEntryProvider(ListSignal<T> listSignal) {
        this.listSignal = Objects.requireNonNull(listSignal);
    }

    /**
     * Starts observing the ListSignal. Must be called after the calendar is set.
     */
    void startObserving(FullCalendar calendar) {
        listEffectRegistration = Signal.effect(calendar, () -> {
            List<ValueSignal<T>> currentSignals = listSignal.get();
            Set<ValueSignal<T>> currentSet = Collections.newSetFromMap(new IdentityHashMap<>());
            currentSet.addAll(currentSignals);

            // Register effects for newly added signals
            for (ValueSignal<T> vs : currentSignals) {
                if (!trackedSignals.contains(vs)) {
                    registerEntryEffect(calendar, vs);
                }
            }

            // Clean up effects for removed signals
            Iterator<ValueSignal<T>> it = trackedSignals.iterator();
            while (it.hasNext()) {
                ValueSignal<T> vs = it.next();
                if (!currentSet.contains(vs)) {
                    Registration reg = entryEffectRegistrations.remove(vs);
                    if (reg != null) {
                        reg.remove();
                    }
                    it.remove();
                }
            }

            fireEvent(new EntriesChangeEvent<>(this));
        });
    }

    private void registerEntryEffect(FullCalendar calendar, ValueSignal<T> entrySignal) {
        trackedSignals.add(entrySignal);

        Registration reg = Signal.effect(calendar, () -> {
            T entry = entrySignal.get();
            if (entry != null) {
                fireEvent(new EntryRefreshEvent<>(this, entry));
            }
        });

        entryEffectRegistrations.put(entrySignal, reg);
    }

    void stopObserving() {
        if (listEffectRegistration != null) {
            listEffectRegistration.remove();
            listEffectRegistration = null;
        }

        entryEffectRegistrations.values().forEach(Registration::remove);
        entryEffectRegistrations.clear();
        trackedSignals.clear();
    }

    @Override
    public Stream<T> fetch(@NonNull EntryQuery query) {
        return listSignal.peek().stream()
                .map(ValueSignal::peek)
                .filter(Objects::nonNull);
    }

    @Override
    public Optional<T> fetchById(@NonNull String id) {
        return listSignal.peek().stream()
                .map(ValueSignal::peek)
                .filter(Objects::nonNull)
                .filter(entry -> id.equals(entry.getId()))
                .findFirst();
    }

    Optional<ValueSignal<T>> findSignalForEntry(String entryId) {
        return listSignal.peek().stream()
                .filter(vs -> {
                    T entry = vs.peek();
                    return entry != null && entryId.equals(entry.getId());
                })
                .findFirst();
    }

    ListSignal<T> getListSignal() {
        return listSignal;
    }
}
