package org.vaadin.stefan;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import org.vaadin.stefan.fullcalendar.ClientSideValue;
import org.vaadin.stefan.fullcalendar.FullCalendarScheduler;

import javax.annotation.Nullable;
import java.time.ZoneId;

@Tag("my-full-calendar")
@HtmlImport("frontend://my-full-calendar.html")
public class MyFullCalendar extends FullCalendarScheduler {
    MyFullCalendar(int entryLimit) {
        super(entryLimit);
    }

    public void setTimezone(Timezone tzb) {
        setOption("timezone", tzb);
    }

    public enum Timezone implements ClientSideValue {
        NONE("false"),
        LOCAL("local"),
        UTC("utc"),
        ZONE_ID("utc");

        private final String clientSideValue;

        Timezone(String clientSideValue) {
            this.clientSideValue = clientSideValue;
        }

        @Nullable
        @Override
        public String getClientSideValue() {
            return this.clientSideValue;
        }
    }



}
