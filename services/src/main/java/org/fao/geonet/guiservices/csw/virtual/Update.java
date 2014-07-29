//=============================================================================
//===   Copyright (C) 2001-2013 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.guiservices.csw.virtual;

import jeeves.constants.Jeeves;
import jeeves.server.JeevesEngine;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Service;
import org.fao.geonet.repository.ServiceRepository;
import org.jdom.Element;

import java.util.*;
import java.util.List;

/**
 * Update the virtual CSW server informations
 */

public class Update implements jeeves.interfaces.Service {

    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
            throws Exception {
        String operation = Util.getParam(params, Params.OPERATION);
        String serviceId = params.getChildText(Params.ID);

        String serviceName = Util.getParam(params, Params.SERVICENAME);
        String className = Util.getParam(params, Params.CLASSNAME);
        String serviceDescription = Util.getParam(params,
                Params.SERVICEDESCRIPTION, "");

        HashMap<String, String> filters = new HashMap<String, String>();
        filters.put(Params.FILTER_ANY,
                Util.getParam(params, Params.FILTER_ANY, ""));
        filters.put(Params.FILTER_TITLE,
                Util.getParam(params, Params.FILTER_TITLE, ""));
        filters.put(Params.FILTER_SUBJECT,
                Util.getParam(params, Params.FILTER_SUBJECT, ""));
        filters.put(Params.FILTER_KEYWORD,
                Util.getParam(params, Params.FILTER_KEYWORD, ""));
        filters.put(Params.FILTER_DENOMINATOR,
                Util.getParam(params, Params.FILTER_DENOMINATOR, ""));
        filters.put(Params.FILTER_TYPE,
                Util.getParam(params, Params.FILTER_TYPE, ""));
        filters.put(Params.FILTER_CATALOG,
                Util.getParam(params, Params.FILTER_CATALOG, ""));
        filters.put(Params.FILTER_GROUP,
                Util.getParam(params, Params.FILTER_GROUP, ""));
        filters.put(Params.FILTER_CATEGORY,
                Util.getParam(params, Params.FILTER_CATEGORY, ""));

        final ServiceRepository serviceRepository = context.getBean(ServiceRepository.class);
        if (operation.equals(Params.Operation.NEWSERVICE)) {
            Service service = serviceRepository.findOneByName(serviceName);

            if (service != null) {
                throw new IllegalArgumentException("Service with name "
                        + serviceName + " already exists");
            }

            service = new org.fao.geonet.domain.Service();
            service.setDescription(serviceDescription);
            service.setClassName(className);
            service.setName(serviceName);


            for (Map.Entry<String, String> filter : filters.entrySet()) {
                if (filter.getValue() != null && !filter.getValue().equals("")) {
                    service.getParameters().put(filter.getKey(), filter.getValue());
                }
            }
            serviceRepository.save(service);
            serviceId = String.valueOf(service.getId());
        } else if (operation.equals(Params.Operation.UPDATESERVICE)) {
            final Service service = serviceRepository.findOne(Integer.valueOf(serviceId));
            service.setClassName(className);
            service.setName(serviceName);
            service.setDescription(serviceDescription);

            for (Map.Entry<String, String> filter : filters.entrySet()) {
                service.getParameters().put(filter.getKey(), filter.getValue());
            }

            serviceRepository.save(service);
        }

        // launching the service on the fly
        context.getBean(JeevesEngine.class).loadConfigDB(context.getApplicationContext(), Integer.valueOf(serviceId));

        return new Element(Jeeves.Elem.RESPONSE);
    }
}