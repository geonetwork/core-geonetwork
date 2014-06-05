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

import jeeves.server.ServiceConfig;
import org.fao.geonet.domain.CswCapabilitiesInfoField;
import org.fao.geonet.domain.responses.CswConfiguration;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.CswCapabilitiesInfoFieldRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("admin.config.csw")
public class Get {
    @Autowired
    private ConfigurableApplicationContext jeevesApplicationContext;
    @Autowired
    private CswCapabilitiesInfoFieldRepository infoFieldRepository;
    /*+	@Autowired
    +	private UserGroupRepository userGroupRepo;
    +	@Autowired
    +	private UserRepository userRepository;
    +
    */
	public void init(String appPath, ServiceConfig params) throws Exception {}

    @RequestMapping(value = "/{lang}/admin.config.csw", produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    CswConfiguration exec() throws Exception {
        SettingManager sm = jeevesApplicationContext.getBean(SettingManager.class);

        CswConfiguration cswConfiguration = new CswConfiguration();

        String cswContactIdValue = sm.getValue("system/csw/contactId");
        if (cswContactIdValue == null) {
            cswContactIdValue = "-1";
        }

        java.util.List<CswCapabilitiesInfoField> capabilitiesInfoFields = infoFieldRepository.findAll(); //AsXml();

        cswConfiguration.setCswEnabled(sm.getValueAsBool("system/csw/enable"));
        cswConfiguration.setCswMetadataPublic(sm.getValueAsBool("system/csw/metadataPublic"));
        cswConfiguration.setCswContactId(Integer.parseInt(cswContactIdValue));
        cswConfiguration.setCapabilitiesInfoFields(capabilitiesInfoFields);

        return cswConfiguration;
    }
}