package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.server.VaadinSession;

import javax.annotation.Nullable;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class Timezone implements ClientSideValue {
    public static final Timezone NONE = new Timezone("none");
    public static final Timezone LOCAL = new Timezone("local");
    public static final Timezone UTC = new Timezone(ZoneId.of(ZoneOffset.UTC.getId()).normalized());

    private final String clientSideValue;
    private ZoneId zoneId;

    public Timezone(ZoneId zoneId) {
        this(zoneId.getId());
        this.zoneId = zoneId;
    }

    private Timezone(String clientSideValue) {
        this.clientSideValue = clientSideValue;
    }

    @Nullable
    @Override
    public String getClientSideValue() {
        return this.clientSideValue;
    }

    public ZoneId getZoneId() {
        if (this == LOCAL) {
            try {
                // done here since at instantiation time there might be no session yet.
                String timeZoneId = VaadinSession.getCurrent().getBrowser().getTimeZoneId();
                return ZoneId.of(timeZoneId);
            } catch (Exception e) {
                throw new TimezoneNotFoundException(e);
            }
        }

        if (this == NONE) {
            return ZoneId.systemDefault();
        }

        return zoneId;
    }

    /**
     * Thrown when there is no timezone defined or could not be obtained from the client.
     */
    public static class TimezoneNotFoundException extends RuntimeException {

        public TimezoneNotFoundException(String message) {
            super(message);
        }

        public TimezoneNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public TimezoneNotFoundException(Throwable cause) {
            super(cause);
        }
    }
}