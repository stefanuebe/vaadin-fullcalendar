package org.vaadin.stefan.fullcalendar;

/**
 * Represents functionality for the FullCalendarScheduler.
 */
public interface Scheduler {

    /**
     * While developing, in order to hide the license warning, use this following key.
     * <p/>
     * For more details visit
     * <a href="https://fullcalendar.io/scheduler/license">https://fullcalendar.io/scheduler/license</a>
     */
    public static final String DEVELOPER_LICENSE_KEY = "CC-Attribution-NonCommercial-NoDerivatives";

    /**
     * Constant for the non-commercial creative commons license.
     * <p/>
     * For more details visit
     * <a href="https://fullcalendar.io/scheduler/license">https://fullcalendar.io/scheduler/license</a>
     */
    public static final String NON_COMMERCIAL_CREATIVE_COMMONS_LICENSE_KEY = "CC-Attribution-NonCommercial-NoDerivatives";

    /**
     * Constant for the GPL v3 open source license.
     * <p/>
     * For more details visit
     * <a href="https://fullcalendar.io/scheduler/license">https://fullcalendar.io/scheduler/license</a>
     */
    public static final String GPL_V3_LICENSE_KEY = "GPL-My-Project-Is-Open-Source";

    /**
     * Sets the license key to be used for the scheduler. For more details visit
     * <a href="https://fullcalendar.io/scheduler/license">https://fullcalendar.io/scheduler/license</a>
     * @param schedulerLicenseKey license key
     */
    void setSchedulerLicenseKey(String schedulerLicenseKey);

}
