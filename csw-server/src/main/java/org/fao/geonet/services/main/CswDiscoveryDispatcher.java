//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.services.main;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.component.csw.CatalogDispatcher;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Accepts CSW Discovery operations.
 */
public class CswDiscoveryDispatcher implements Service {
    private Logger logger;

    /**
     * The "filter" parameter value is the filter to apply to Lucene query using Lucene query syntax
     * ( http://lucene.apache.org/java/2_4_0/queryparsersyntax.html).
     *
     * Service configuration example with filter" parameter:
     *
     * <service name="csw-custom"> <class name=".services.main.CswDispatcher" > <param name="filter"
     * value="+inspirerelated:on"/> </class> </service>
     */
    private String cswServiceSpecificContraint;

    public void init(Path appPath, ServiceConfig config) throws Exception {
        cswServiceSpecificContraint = config.getValue(Geonet.Elem.FILTER);
    }


    public Element exec(Element params, ServiceContext context) throws Exception {
        logger = context.getLogger();

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager settingMan = gc.getBean(SettingManager.class);
        boolean cswEnable = settingMan.getValueAsBool(Settings.SYSTEM_CSW_ENABLE, false);

        Element response = new Element(Jeeves.Elem.RESPONSE);

        String operation;
        // KVP encoding
        if (params.getName().equals("request")) {
            Map<String, String> hm = CatalogDispatcher.extractParams(params);
            operation = hm.get("request");
            // operation = params.getChildText("request");
            if (operation == null) {
                Element info = new Element("info")
                    .setText("No 'request' parameter found");
                response.addContent(info);
                return response;
            }
        }
        // SOAP encoding
        else if (params.getName().equals("Envelope")) {
            Element soapBody = params.getChild("Body",
                org.jdom.Namespace.getNamespace("http://www.w3.org/2003/05/soap-envelope"));
            @SuppressWarnings("unchecked")
            List<Element> payloadList = soapBody.getChildren();
            Element payload = payloadList.get(0);
            operation = payload.getName();
        }
        // POX
        else {
            operation = params.getName();
        }

        if (!operation.equals("GetCapabilities") && !operation.equals("GetRecords") && !operation.equals("GetRecordById")
            && !operation.equals("DescribeRecord") && !operation.equals("GetDomain")) {
            Element info = new Element("info").setText("Not a CSW Discovery operation: " + operation + ". Did you mean to use the CSW Publication service? Use service name /csw-publication");
            response.addContent(info);
            return response;
        }

        // FIXME : response should be more explicit, an exception should be return ?
        if (!cswEnable) {
            logger.info("CSW is disabled");
            Element info = new Element("info").setText("CSW is disabled");
            response.addContent(info);
        } else {
            response = gc.getBean(CatalogDispatcher.class).dispatch(params, context, cswServiceSpecificContraint);
        }
        return response;
    }
}
