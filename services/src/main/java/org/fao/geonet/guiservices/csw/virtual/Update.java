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

import java.util.HashMap;
import java.util.Map;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.jdom.Element;

/**
 * Update the virtual CSW server informations
 */

public class Update implements Service {

    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
            throws Exception {
        String operation = Util.getParam(params, Params.OPERATION);
        String serviceId = params.getChildText(Params.ID);
        String paramId = params.getChildText(Params.ID);

        String servicename = Util.getParam(params, Params.SERVICENAME);
        String classname = Util.getParam(params, Params.CLASSNAME);
        String servicedescription = Util.getParam(params,
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

        Dbms dbms = (Dbms) context.getResourceManager()
                .open(Geonet.Res.MAIN_DB);

        if (operation.equals(Params.Operation.NEWSERVICE)) {

            String query = "SELECT * FROM Services WHERE name=?";
            Element servicesTest = dbms.select(query, servicename);

            if (servicesTest.getChildren().size() != 0) {
                throw new IllegalArgumentException("Service with name "
                        + servicename + " already exists");
            }

            serviceId = String.format("%s",
                    context.getSerialFactory().getSerial(dbms, "Services"));
            query = "INSERT INTO services (id, name, class, description) VALUES (?, ?, ?, ?)";
            dbms.execute(query, Integer.valueOf(serviceId), servicename, classname,
                    servicedescription);

            for (Map.Entry<String, String> filter : filters.entrySet()) {
                if (filter.getValue() != null && !filter.getValue().equals("")) {
                    paramId = String.format("%s", context.getSerialFactory()
                            .getSerial(dbms, "ServiceParameters"));
                    query = "INSERT INTO serviceParameters (id, service, name, value) VALUES (?, ?, ?, ?)";
                    dbms.execute(query, Integer.valueOf(paramId), Integer.valueOf(serviceId),
                            filter.getKey(), filter.getValue());
                }
            }
        }

        else if (operation.equals(Params.Operation.UPDATESERVICE)) {

            for (Map.Entry<String, String> filter : filters.entrySet()) {

                String query = "SELECT * FROM ServiceParameters WHERE service=? AND name=?";
                Element testParams = dbms.select(query, Integer.valueOf(serviceId),
                        filter.getKey());

                if (testParams.getChildren().size() != 0) {
                    query = "UPDATE serviceParameters SET value=? WHERE service=? AND name=?";
                    dbms.execute(query, filter.getValue(), Integer.valueOf(serviceId),
                            filter.getKey());
                } else {
                    paramId = String.format("%s", context.getSerialFactory()
                            .getSerial(dbms, "ServiceParameters"));
                    query = "INSERT INTO serviceParameters (id, service, name, value) VALUES (?, ?, ?, ?)";
                    dbms.execute(query, Integer.valueOf(paramId), Integer.valueOf(serviceId),
                            filter.getKey(), filter.getValue());
                }

                query = "UPDATE services SET description=?, name=? WHERE id=?";
                dbms.execute(query, servicedescription, servicename, Integer.valueOf(serviceId));
            }
        }

        // launching the service on the fly
        context.getServlet().getEngine().loadConfigDB(dbms, Integer.valueOf(serviceId));

        return new Element(Jeeves.Elem.RESPONSE);
    }
}