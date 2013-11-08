//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
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
package org.fao.geonet.guiservices.csw.customelementset;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.CustomElementSet;
import org.fao.geonet.repository.CustomElementSetRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

import java.util.List;

/**
 * Retrieve custom element sets.
 *
 */
public class Get implements Service {
    /**
     * No specific initialization.
     *
     * @param appPath
     * @param params
     * @throws Exception
     */
	public void init(String appPath, ServiceConfig params) throws Exception {}

    /**
     * Retrieves custom elementsets.
     *
     * @param params parameters to this request
     * @param context Jeeves servicecontext
     * @return a customelementsets element
     * @throws Exception hmmm
     */
	public Element exec(Element params, ServiceContext context) throws Exception {
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        boolean cswEnabled = sm.getValueAsBool("system/csw/enable");

        Element result = new Element("customelementsets");
        if(cswEnabled) {
            List<CustomElementSet> records = context.getBean(CustomElementSetRepository.class).findAll();
            for(CustomElementSet record : records) {
                String xpath = record.getXpath();
                Element xpathElement = new Element("xpath");
                xpathElement.setText(xpath);
                result.addContent(xpathElement);
            }
        }

        Element cswEnabledElement = new Element("cswEnabled");
        cswEnabledElement.setText(String.valueOf(cswEnabled));

        result.addContent(cswEnabledElement);

        if(Log.isDebugEnabled(Geonet.CUSTOM_ELEMENTSET))
            Log.debug(Geonet.CUSTOM_ELEMENTSET, "get customelementset:\n" + Xml.getString(result));

        return result;
	}

}