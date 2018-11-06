package org.vaadin.stefan.fullcalendar;

import java.util.Optional;
import java.util.Set;

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

    /**
     * Adds an resource to this calendar. Removes it from an old calendar instance when existing.
     * Noop if the resource id is already registered.
     *
     * @param resource resource
     * @return true if resource could be added
     * @throws NullPointerException when null is passed
     */
    boolean addResource(Resource resource);

    /**
     * Removes the given resource. Noop if the id is not registered.
     *
     * @param resource resource
     * @throws NullPointerException when null is passed
     */
    void removeResource(Resource resource);

    /**
     * Returns the resource with the given id. Is empty when the id is not registered.
     *
     * @param id id
     * @return resource or empty
     * @throws NullPointerException when null is passed
     */
    Optional<Resource> getResourceById(String id);

    /**
     * Returns all resources registered in this instance. Changes in an resource instance is reflected in the
     * calendar instance on server side, but not client side. Resources can currently not be updated on the client side.
     * <p/>
     * Changes in the list are not reflected to the calendar's list instance. Also please note, that the content
     * of the list is <b>unsorted</b> and may vary with each call. The return of a list is due to presenting
     * a convenient way of using the returned values without the need to encapsulate them yourselves.
     *
     * @return resources resources
     */
    Set<Resource> getResources();

    /**
     * Removes all registered resources from this instance.
     */
    void removeAllResources();

    void setGroupEntriesBy(GroupEntriesBy groupEntriesBy);
}
