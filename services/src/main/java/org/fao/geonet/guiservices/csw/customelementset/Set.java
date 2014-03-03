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

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.CustomElementSet;
import org.fao.geonet.repository.CustomElementSetRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

import java.util.List;

/**
 * Save custom element sets.
 *
 */
public class Set implements Service {
    /**
     * No specific initialization.
     *
     * @param appPath
     * @param params
     * @throws Exception
     */
	public void init(String appPath, ServiceConfig params) throws Exception {}

    /**
     * Saves custom element sets.
     *
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
	public Element exec(Element params, ServiceContext context) throws Exception {
        saveCustomElementSets(params, context);
        return new Element(Jeeves.Elem.RESPONSE).setText("ok");
	}

    /**
     *
     * Processes parameters and saves custom element sets in database.
     *
     * @param params
     * @throws Exception
     */
    private void saveCustomElementSets(Element params, ServiceContext context) throws Exception {
        if (Log.isDebugEnabled(Geonet.CUSTOM_ELEMENTSET)) {
            Log.debug(Geonet.CUSTOM_ELEMENTSET, "set customelementset:\n" + Xml.getString(params));
        }

        final CustomElementSetRepository repository = context.getBean(CustomElementSetRepository.class);
        repository.deleteAll();

        @SuppressWarnings("unchecked")
        List<Element> xpaths = params.getChildren("xpath");
        for(Element xpath : xpaths) {
            CustomElementSet customElementSet = new CustomElementSet();
            customElementSet.setXpath(xpath.getText());

            repository.save(customElementSet);
        }
    }

}