package org.vaadin.stefan.fullcalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TimeslotsSelectedEventTest {

	@Mock
	private FullCalendar calendar;
	
	private LocalDateTime startUTC;
	private LocalDateTime endUTC;
	private LocalDateTime startWithOffset;
	private LocalDateTime endWithOffset;
	
	// client dates as UTC
	private String clientStartDateStr = "2000-01-01T16:00:00Z";
	private String clientEndDateStr = "2000-01-01T17:00:00Z";
	
	private TimeslotsSelectedEvent event;
	
	private Timezone berlinTimezone = new Timezone(ZoneId.of("Europe/Berlin"));
	
	@BeforeEach
	void setup()
	{
		// client side of full-calendar runs in Europe/Berlin
		when(calendar.getTimezone()).thenReturn(berlinTimezone);
		
		// UTC
		startUTC = LocalDateTime.of(2000, 1, 1, 16, 0);
		endUTC = LocalDateTime.of(2000, 1, 1, 17, 0);
		
		// Europe/Berlin
		startWithOffset = berlinTimezone.applyTimezoneOffset(startUTC);
		endWithOffset = berlinTimezone.applyTimezoneOffset(endUTC);
		
		// event object under test
		event = new TimeslotsSelectedEvent(calendar, true, clientStartDateStr, clientEndDateStr, true);
	}
	
	/**
	 * Tests that the {@link TimeslotsSelectedEvent#getStart()} returns the {@link LocalDateTime} as UTC.
	 */
    @Test
    void getStart() {
       
        assertEquals(startUTC, event.getStart());
    }
    
	/**
	 * Tests that the {@link TimeslotsSelectedEvent#getEnd()} returns the {@link LocalDateTime} as UTC.
	 */
    @Test
    void getEnd() {
       
        assertEquals(endUTC, event.getEnd());
    }
    
	/**
	 * Tests that the {@link TimeslotsSelectedEvent#getStartWithOffset()} returns the {@link LocalDateTime} as Europe/Berlin.
	 */
    @Test
    void getStartWithOffset() {
    	
        assertEquals(startWithOffset, event.getStartWithOffset());
    }
    
	/**
	 * Tests that the {@link TimeslotsSelectedEvent#getEndWithOffset()} returns the {@link LocalDateTime} as Europe/Berlin.
	 */
    @Test
    void getEndWithOffset() {
       
        assertEquals(endWithOffset, event.getEndWithOffset());
    }
    
	/**
	 * Tests that the {@link TimeslotsSelectedEvent#getStartAsInstant()} returns the {@link Instant} as UTC.
	 */
    @Test
    void getStartAsInstant() {
    	
		assertEquals(startUTC.toInstant(ZoneOffset.UTC), event.getStartAsInstant());
    }
    
	/**
	 * Tests that the {@link TimeslotsSelectedEvent#getEndAsInstant()} returns the {@link Instant} as UTC.
	 */
    @Test
    void getEndAsInstant() {
       
		assertEquals(endUTC.toInstant(ZoneOffset.UTC), event.getEndAsInstant());
    }
    
}
