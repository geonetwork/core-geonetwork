//=============================================================================
//===	Copyright (C) 2001-2013 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.guiservices.csw.virtual;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.repository.ServiceRepository;
import org.jdom.Element;

import java.util.Map;

/**
 * Retrieves a particular service
 */
public class Get implements Service {

    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
            throws Exception {

        String id = params.getChildText(Params.ID);

        final ServiceRepository serviceRepository = context.getBean(ServiceRepository.class);
        final org.fao.geonet.domain.Service service = serviceRepository.findOne(Integer.valueOf(id));

        final Map<String,String> parameters = service.getParameters();

        Element elParameters = new Element("serviceParameters");
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            elParameters.addContent(new Element("parameter")
                    .setAttribute("name", parameter.getKey())
                    .setText(parameter.getValue()));
        }
        return new Element("service")
               .addContent(new Element("id").setText(String.valueOf(service.getId())))
               .addContent(new Element("name").setText(service.getName()))
               .addContent(new Element("description").setText(service.getDescription()))
               .addContent(elParameters);
    }
}
