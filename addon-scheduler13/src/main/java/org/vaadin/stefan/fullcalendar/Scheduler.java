/*
 * Copyright 2018, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar;

import java.util.Optional;
import java.util.Set;

/**
 * Represents functionality for the FullCalendarScheduler.
 */
public interface Scheduler {

    /**
     * While developing, in order to hide the license warning, use this following key.
     * <br><br>
     * For more details visit
     * <a href="https://fullcalendar.io/scheduler/license">https://fullcalendar.io/scheduler/license</a>
     */
    public static final String DEVELOPER_LICENSE_KEY = "CC-Attribution-NonCommercial-NoDerivatives";

    /**
     * Constant for the non-commercial creative commons license.
     * <br><br>
     * For more details visit
     * <a href="https://fullcalendar.io/scheduler/license">https://fullcalendar.io/scheduler/license</a>
     */
    public static final String NON_COMMERCIAL_CREATIVE_COMMONS_LICENSE_KEY = "CC-Attribution-NonCommercial-NoDerivatives";

    /**
     * Constant for the GPL v3 open source license.
     * <br><br>
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
     * Adds an resource to this calendar. Noop if the resource id is already registered.
     *
     * @param resource resource
     * @throws NullPointerException when null is passed
     */
    void addResource(Resource resource);

    /**
     * Adds resources to this calendar. Noop already registered resources.
     *
     * @param resources resources to add
     * @throws NullPointerException when null is passed
     */
    void addResources(Resource... resources);

    /**
     * Adds resources to this calendar. Noop already registered resources.
     *
     * @param resources resources to add
     * @throws NullPointerException when null is passed
     */
    void addResources(Iterable<Resource> resources);

    /**
     * Removes the given resource. Noop if the id is not registered.
     *
     * @param resource resource
     * @throws NullPointerException when null is passed
     */
    void removeResource(Resource resource);

    /**
     * Removes the given resources. Noop not registered resources.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    void removeResources(Resource... resources);
 /**
     * Removes the given resources. Noop not registered resources.
     *
     * @param resources resources
     * @throws NullPointerException when null is passed
     */
    void removeResources(Iterable<Resource> resources);

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
     * <br><br>
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
