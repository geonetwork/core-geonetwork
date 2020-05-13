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
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.CustomElementSet;
import org.fao.geonet.domain.responses.OkResponse;
import org.fao.geonet.repository.CustomElementSetRepository;
import org.fao.geonet.utils.Log;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

/**
 * Save custom element sets.
 */
@Controller("admin.config.csw.customelementset.save")
public class Set {
    /**
     * Saves custom element sets.
     */

    @RequestMapping(value = "/{portal}/{lang}/admin.config.csw.customelementset.save",
        method = {RequestMethod.POST, RequestMethod.PUT},
        produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    OkResponse exec(@RequestParam("xpath") List<String> xpathList) throws Exception {
        saveCustomElementSets(xpathList);
        return new OkResponse();
    }

    /**
     * Processes parameters and saves custom element sets in database.
     */
    private void saveCustomElementSets(List<String> xpathList) throws Exception {
        CustomElementSetRepository customElementSetRepository = ApplicationContextHolder.get().getBean(CustomElementSetRepository.class);
        if (Log.isDebugEnabled(Geonet.CUSTOM_ELEMENTSET)) {
            Log.debug(Geonet.CUSTOM_ELEMENTSET, "set customelementset:\n" + Arrays.toString(xpathList.toArray()));
        }

        customElementSetRepository.deleteAll();

        for (String xpath : xpathList) {
            CustomElementSet customElementSet = new CustomElementSet();
            customElementSet.setXpath(xpath);

            customElementSetRepository.save(customElementSet);
        }
    }

}
