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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.responses.CswConfigurationResponse;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("admin.config.csw")
public class Get {

    @RequestMapping(value = "/{portal}/{lang}/admin.config.csw", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    CswConfigurationResponse exec() throws Exception {
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        SettingManager sm = applicationContext.getBean(SettingManager.class);

        CswConfigurationResponse response = new CswConfigurationResponse();

        String capabilityRecordUuid = sm.getValue(Settings.SYSTEM_CSW_CAPABILITY_RECORD_UUID);
        if (capabilityRecordUuid == null) {
            capabilityRecordUuid = "-1";
        }

        response.setCswEnabled(sm.getValueAsBool(Settings.SYSTEM_CSW_ENABLE));
        response.setCswMetadataPublic(sm.getValueAsBool(Settings.SYSTEM_CSW_METADATA_PUBLIC));
        response.setCapabilityRecordUuid(Integer.parseInt(capabilityRecordUuid));

        return response;
    }
}
