/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.monitor.health;

import jeeves.monitor.HealthCheckFactory;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.sources.ServiceRequest.InputMethod;
import org.fao.geonet.component.csw.CatalogDispatcher;

import org.fao.geonet.utils.Xml;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

import com.yammer.metrics.core.HealthCheck;

/**
 * Checks to ensure that the CSW subsystem is accessible and functioning
 * <p/>
 * User: jeichar Date: 3/26/12 Time: 9:01 AM
 */
public class CswGetCapabilitiesHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck(this.getClass().getSimpleName()) {
            @Override
            protected Result check() throws Exception {
                try {
                    Element result = CswGetCapabilitiesHealthCheck.dispatch(context,
                        "request", "GetCapabilities", "service", "CSW");

                    if (result.getChild("ServiceIdentification", Geonet.Namespaces.OWS) == null)
                        return Result.unhealthy("Capabilities did not have a 'ServiceIdentification' element as expected.  Xml: " + Xml.getString(result));
                    return Result.healthy();
                } catch (Throwable e) {
                    return Result.unhealthy(e);
                }
            }
        };
    }

    /**
     * Runs a CSW KVP request as an anonymous user, the way a client would.
     */
    static Element dispatch(ServiceContext context, String... namesAndValues) {
        Element request = new Element("request");
        for (int i = 0; i < namesAndValues.length; i += 2) {
            request.addContent(new Element(namesAndValues[i]).setText(namesAndValues[i + 1]));
        }
        ServiceContext cswContext = context.getBean(ServiceManager.class)
            .createServiceContext("csw", context.getApplicationContext());
        cswContext.setLanguage("eng");
        cswContext.setUserSession(new UserSession());
        cswContext.setInputMethod(InputMethod.GET);
        cswContext.setAsThreadLocal();
        try {
            return context.getBean(CatalogDispatcher.class).dispatch(request, cswContext);
        } finally {
            context.setAsThreadLocal();
        }
    }
}
