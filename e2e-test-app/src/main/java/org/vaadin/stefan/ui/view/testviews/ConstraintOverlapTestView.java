package org.vaadin.stefan.ui.view.testviews;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import org.vaadin.stefan.ui.layouts.TestLayout;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Test view for verifying constraint, overlap, editable flags, validRange, and selectAllow.
 * <p>
 * Uses timeGridWeek to test drag/resize behavior with constraints.
 * <p>
 * Route: /test/constraint-overlap
 */
@Route(value = "constraint-overlap", layout = TestLayout.class)
@MenuItem(label = "Constraint/Overlap")
public class ConstraintOverlapTestView extends VerticalLayout {

    public ConstraintOverlapTestView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Constraint / Overlap / Editable"));
        add(new Paragraph("Tests per-entry editable flags, overlap, constraint, validRange, selectAllow."));

        // --- Badges ---
        Span dropCount = badge("drop-count", "0");
        Span dropTitle = badge("drop-title", "");

        Div badges = new Div(
                label("dropCount: "), dropCount,
                label(" | dropTitle: "), dropTitle
        );
        badges.getStyle().set("font-size", "12px");
        add(badges);

        // --- Calendar ---
        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.addThemeVariants(FullCalendarVariant.LUMO);
        calendar.setLocale(Locale.UK); // Monday-start
        calendar.setOption("initialDate", LocalDate.of(2025, 3, 3).toString());
        calendar.setOption("initialView", CalendarViewImpl.TIME_GRID_WEEK.getClientSideValue());
        calendar.setOption(FullCalendar.Option.EDITABLE, true);
        calendar.setOption(FullCalendar.Option.SELECTABLE, true);

        // validRange: prevent navigation before 2025-03-01 and after 2025-04-30
        calendar.setValidRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 4, 30));

        // --- Entries ---
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();

        // 1. Normal editable entry
        Entry normalEntry = new Entry();
        normalEntry.setTitle("Normal Entry");
        normalEntry.setStart(LocalDateTime.of(2025, 3, 4, 9, 0));
        normalEntry.setEnd(LocalDateTime.of(2025, 3, 4, 10, 0));
        provider.addEntry(normalEntry);

        // 2. Per-entry editable=false (should NOT be draggable despite calendar editable=true)
        Entry notEditable = new Entry();
        notEditable.setTitle("Locked Entry");
        notEditable.setStart(LocalDateTime.of(2025, 3, 5, 9, 0));
        notEditable.setEnd(LocalDateTime.of(2025, 3, 5, 10, 0));
        // Note: Entry.setEditable() is V7 API — not available in V6.
        provider.addEntry(notEditable);

        // 3. Per-entry startEditable=false (should NOT be draggable but CAN be resized)
        Entry noStartEdit = new Entry();
        noStartEdit.setTitle("No Start Edit");
        noStartEdit.setStart(LocalDateTime.of(2025, 3, 6, 9, 0));
        noStartEdit.setEnd(LocalDateTime.of(2025, 3, 6, 10, 0));
        // Note: Entry.setStartEditable() is V7 API — not available in V6.
        provider.addEntry(noStartEdit);

        // 4. Entry with overlap=false
        Entry noOverlap = new Entry();
        noOverlap.setTitle("No Overlap");
        noOverlap.setStart(LocalDateTime.of(2025, 3, 3, 11, 0));
        noOverlap.setEnd(LocalDateTime.of(2025, 3, 3, 12, 0));
        noOverlap.setOverlap(false);
        provider.addEntry(noOverlap);

        // 5. Blocking entry on same day as noOverlap — occupies 12:00-13:00
        Entry blocker = new Entry();
        blocker.setTitle("Blocker");
        blocker.setStart(LocalDateTime.of(2025, 3, 3, 12, 0));
        blocker.setEnd(LocalDateTime.of(2025, 3, 3, 13, 0));
        provider.addEntry(blocker);

        // 6. Entry with constraint=businessHours
        Entry constrained = new Entry();
        constrained.setTitle("BH Constrained");
        constrained.setStart(LocalDateTime.of(2025, 3, 7, 10, 0));
        constrained.setEnd(LocalDateTime.of(2025, 3, 7, 11, 0));
        constrained.setConstraint("businessHours");
        provider.addEntry(constrained);

        calendar.setEntryProvider(provider);

        // businessHours option for constraint testing
        calendar.setOption(FullCalendar.Option.BUSINESS_HOURS, true);

        // --- Listeners ---
        calendar.addEntryDroppedListener(e -> {
            e.applyChangesOnEntry();
            provider.refreshItem(e.getEntry());
            int count = Integer.parseInt(dropCount.getText()) + 1;
            dropCount.setText(String.valueOf(count));
            dropTitle.setText(e.getEntry().getTitle());
        });

        add(calendar);
        setFlexGrow(1, calendar);
    }

    private static Span badge(String id, String text) {
        Span s = new Span(text);
        s.setId(id);
        return s;
    }

    private static Span label(String text) {
        return new Span(text);
    }
}
