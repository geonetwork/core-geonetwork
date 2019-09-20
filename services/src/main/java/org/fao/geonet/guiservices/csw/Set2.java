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

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.responses.OkResponse;
import org.fao.geonet.repository.CswCapabilitiesInfo;
import org.fao.geonet.repository.CswCapabilitiesInfoFieldRepository;
import org.fao.geonet.repository.LanguageRepository;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedList;
import java.util.List;

/**
 * Copy of Set - only takes care of saving GetCapabilities properties and not settings. In order to
 * save settings use the setting service.
 */
@Controller("admin.config.csw.save")
public class Set2 {

    @RequestMapping(value = "/{portal}/{lang}/admin.config.csw.save",
        method = {RequestMethod.POST, RequestMethod.PUT},
        produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    OkResponse exec(@RequestParam MultiValueMap parameters) throws Exception {
        // Process parameters and save capabilities information in database
        saveCswCapabilitiesInfo(parameters);

        return new OkResponse();
    }

    private void saveCswCapabilitiesInfo(MultiValueMap parameters) throws Exception {
        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        LanguageRepository languageRepository = context.getBean(LanguageRepository.class);
        CswCapabilitiesInfoFieldRepository cswCapabilitiesInfoFieldRepository = context.getBean(CswCapabilitiesInfoFieldRepository.class);
        final List<Language> langs = languageRepository.findAll();

        for (Language lang : langs) {
            CswCapabilitiesInfo cswCapInfo = cswCapabilitiesInfoFieldRepository.findCswCapabilitiesInfo(lang.getId());

            String langId = lang.getId();
            if (parameters.get("csw.title_" + langId) != null) {
                String title = (String) ((LinkedList) parameters.get("csw.title_" + langId)).get(0);
                if (StringUtils.isNotEmpty(title)) {
                    cswCapInfo.setTitle(title);
                }
            }
            if (parameters.get("csw.abstract_" + langId) != null) {
                String abs = (String) ((LinkedList) parameters.get("csw.abstract_" + langId)).get(0);
                if (StringUtils.isNotEmpty(abs)) {
                    cswCapInfo.setAbstract(abs);
                }
            }

            if (parameters.get("csw.fees_" + langId) != null) {
                String fees = (String) ((LinkedList) parameters.get("csw.fees_" + langId)).get(0);
                if (StringUtils.isNotEmpty(fees)) {
                    cswCapInfo.setFees(fees);
                }
            }
            if (parameters.get("csw.accessConstraints_" + langId) != null) {
                String accessConstraints = (String) ((LinkedList) parameters.get("csw.accessConstraints_" + langId)).get(0);
                if (StringUtils.isNotEmpty(accessConstraints)) {
                    cswCapInfo.setAccessConstraints(accessConstraints);
                }
            }


            // Save item
            cswCapabilitiesInfoFieldRepository.save(cswCapInfo);
        }
    }

}
