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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.CustomElementSet;
import org.fao.geonet.domain.responses.CustomElementSetsListResponse;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.CustomElementSetRepository;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieve custom element sets.
 */
@Controller("admin.config.csw.customelementset")
public class Get {
    /**
     * Retrieves custom elementsets.
     *
     * @return a customelementsets element
     * @throws Exception hmmm
     */
    @RequestMapping(value = "/{portal}/{lang}/admin.config.csw.customelementset", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    CustomElementSetsListResponse exec() throws Exception {
        ConfigurableApplicationContext context = ApplicationContextHolder.get();
        CustomElementSetRepository customElementSetRepository = context.getBean(CustomElementSetRepository.class);
        SettingManager sm = context.getBean(SettingManager.class);

        boolean cswEnabled = sm.getValueAsBool(Settings.SYSTEM_CSW_ENABLE);

        CustomElementSetsListResponse response = new CustomElementSetsListResponse();
        List<String> xpaths = new ArrayList<String>();

        if (cswEnabled) {
            List<CustomElementSet> records = customElementSetRepository.findAll();
            for (CustomElementSet record : records) {
                xpaths.add(record.getXpath());
            }
        }

        response.setXpaths(xpaths);
        response.setCswEnabled(cswEnabled);

        //if(Log.isDebugEnabled(Geonet.CUSTOM_ELEMENTSET))
        //    Log.debug(Geonet.CUSTOM_ELEMENTSET, "get customelementset:\n" + Xml.getString(result));

        return response;
    }

}
