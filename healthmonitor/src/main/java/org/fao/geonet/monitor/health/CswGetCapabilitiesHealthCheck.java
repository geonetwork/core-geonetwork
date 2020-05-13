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
import jeeves.server.context.ServiceContext;
import jeeves.server.local.LocalServiceRequest;
import jeeves.server.sources.ServiceRequest.InputMethod;

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
                    LocalServiceRequest request = LocalServiceRequest.create("local://csw?request=GetCapabilities&service=CSW");
                    request.setDebug(false);
                    request.setLanguage("eng");
                    request.setInputMethod(InputMethod.GET);
                    Element result = context.execute(request);

                    if (result.getChild("ServiceIdentification", Geonet.Namespaces.OWS) == null)
                        return Result.unhealthy("Capabilities did not have a 'ServiceIdentification' element as expected.  Xml: " + Xml.getString(result));
                    return Result.healthy();
                } catch (Throwable e) {
                    return Result.unhealthy(e);
                }
            }
        };
    }
}
