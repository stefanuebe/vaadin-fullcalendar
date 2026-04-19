package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the #191 Delta deprecation: {@code getYears()} and {@code getMonths()}
 * are deprecated since 7.2.0 because FullCalendar JS normalises year/month portions
 * into {@code days} when emitting drag/drop deltas.
 * <p>
 * In {@link Delta}, Lombok {@code @Getter} is disabled for {@code years}/{@code months}
 * via {@code @Getter(AccessLevel.NONE)} and the two getters are written out manually,
 * carrying the explicit {@code @Deprecated(since = "7.2.0")}. These tests read the
 * annotation off the accessor methods.
 */
class DeltaDeprecationTest {

    @Test
    void getYears_isDeprecatedSince720() throws NoSuchMethodException {
        Method m = Delta.class.getMethod("getYears");
        Deprecated d = m.getAnnotation(Deprecated.class);
        assertNotNull(d, "getYears must be @Deprecated (Lombok should copy from field)");
        assertEquals("7.2.0", d.since());
        assertTrue(!d.forRemoval(), "no removal scheduled — keep in 7.x");
    }

    @Test
    void getMonths_isDeprecatedSince720() throws NoSuchMethodException {
        Method m = Delta.class.getMethod("getMonths");
        Deprecated d = m.getAnnotation(Deprecated.class);
        assertNotNull(d, "getMonths must be @Deprecated (Lombok should copy from field)");
        assertEquals("7.2.0", d.since());
        assertTrue(!d.forRemoval(), "no removal scheduled — keep in 7.x");
    }

    @Test
    void getDays_isNotDeprecated() throws NoSuchMethodException {
        Method m = Delta.class.getMethod("getDays");
        assertTrue(m.getAnnotation(Deprecated.class) == null,
                "getDays must remain non-deprecated — it's the canonical field for FC-originated deltas");
    }

    @Test
    void delta_stillAppliesYearsAndMonths_forBackwardCompatibility() {
        // Manually-constructed Delta instances must continue to apply years/months on their
        // target. Only the fields are deprecated; applyOn is not.
        @SuppressWarnings("deprecation")
        Delta delta = Delta.builder().years(1).months(2).days(3).hours(0).minutes(0).seconds(0).build();
        java.time.LocalDate start = java.time.LocalDate.of(2025, 1, 1);
        java.time.LocalDate result = delta.applyOn(start);
        assertEquals(java.time.LocalDate.of(2026, 3, 4), result);
    }
}
