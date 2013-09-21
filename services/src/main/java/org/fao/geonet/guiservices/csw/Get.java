//=============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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
package org.fao.geonet.guiservices.csw;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.CswCapabilitiesInfoFieldRepository;
import org.jdom.Element;

public class Get implements Service {

	public void init(String appPath, ServiceConfig params) throws Exception {}

	public Element exec(Element params, ServiceContext context) throws Exception {
        SettingManager sm = context.getBean(SettingManager.class);

        boolean cswEnabled = sm.getValueAsBool("system/csw/enable");
        boolean cswMetadataPublic = sm.getValueAsBool("system/csw/metadataPublic");
        String cswContactIdValue = sm.getValue("system/csw/contactId");
        if (cswContactIdValue == null) {
            cswContactIdValue = "-1";
        }

        final CswCapabilitiesInfoFieldRepository infoFieldRepository = context.getBean(CswCapabilitiesInfoFieldRepository.class);
        Element cswCapabilitiesConfig = infoFieldRepository.findAllAsXml();

        // Build response
        Element cswEnable = new Element("cswEnable");
        cswEnable.setText(String.valueOf(cswEnabled));

        Element cswPublic = new Element("cswMetadataPublic");
        cswPublic.setText(String.valueOf(cswMetadataPublic));

        Element cswContactId = new Element("cswContactId");
        cswContactId.setText(cswContactIdValue);

        cswCapabilitiesConfig.addContent(cswEnable);
        cswCapabilitiesConfig.addContent(cswPublic);
        cswCapabilitiesConfig.addContent(cswContactId);
        
        return cswCapabilitiesConfig;
	}

}