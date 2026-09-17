/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package jeeves.monitor;

import com.yammer.metrics.core.HealthCheck;

import jeeves.server.context.ServiceContext;

/**
 * Class for creating HealthCheck objects (http://metrics.codahale.com/) which only require a
 * ServiceContext object for performing the check. The health check object will be created and added
 * after AppHandler is created and started.
 *
 * The HealthCheck will defined in the config.xml in the monitors section
 *
 * That declares what factories should be created.  See config-monitoring.xml for examples and
 * documentation.
 *
 * User: jeichar Date: 3/29/12 Time: 3:29 PM
 */
public interface HealthCheckFactory {
    /**
     * Prefix for the message of a healthy {@link HealthCheck.Result} indicating that the
     * checked component was intentionally disabled via configuration rather than actively
     * verified. Use {@link #disabled(String)} to build such a result and {@link #isDisabled}
     * / {@link #getDisabledMessage} to recognize and unwrap it, rather than matching on this
     * constant directly, so the convention stays in one place.
     */
    String DISABLED_RESULT_PREFIX = "DISABLED: ";

    /**
     * Builds a healthy {@link HealthCheck.Result} indicating that the checked component was
     * intentionally disabled via configuration (e.g. a feature not configured) rather than
     * actively verified. Callers such as {@code GeonetworkHealthCheckServlet} report this as a
     * {@code DISABLED} status instead of {@code OK}, without failing the overall health check.
     *
     * @param message a message describing why the component is disabled
     */
    static HealthCheck.Result disabled(String message) {
        return HealthCheck.Result.healthy(DISABLED_RESULT_PREFIX + message);
    }

    /**
     * Returns {@code true} if the given result was built by {@link #disabled(String)}.
     */
    static boolean isDisabled(HealthCheck.Result result) {
        return result.isHealthy() && result.getMessage() != null
            && result.getMessage().startsWith(DISABLED_RESULT_PREFIX);
    }

    /**
     * Returns the original message passed to {@link #disabled(String)}, with the
     * {@link #DISABLED_RESULT_PREFIX} stripped. Only valid when {@link #isDisabled} is {@code true}.
     */
    static String getDisabledMessage(HealthCheck.Result result) {
        return result.getMessage().substring(DISABLED_RESULT_PREFIX.length());
    }

    /**
     * Create a HealthCheck object of type Type
     */
    HealthCheck create(ServiceContext context);
}
