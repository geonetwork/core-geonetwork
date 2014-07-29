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

import java.util.List;
import java.util.Map;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Language;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.CswCapabilitiesInfo;
import org.fao.geonet.repository.CswCapabilitiesInfoFieldRepository;
import org.fao.geonet.repository.LanguageRepository;
import org.jdom.Element;

/**
 * Copy of Set - only takes care of saving GetCapabilities properties
 * and not settings. In order to save settings use the setting service.
 * 
 */
public class Set2 implements Service {

    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        // Process parameters and save capabilities information in database
        saveCswCapabilitiesInfo(params, context);

        // Build response
        return new Element(Jeeves.Elem.RESPONSE).setText("ok");
    }

    private void saveCswCapabilitiesInfo(Element params, ServiceContext context) throws Exception {

        final List<Language> langs = context.getBean(LanguageRepository.class).findAll();

        final CswCapabilitiesInfoFieldRepository cswInfoFieldRepository = context.getBean(CswCapabilitiesInfoFieldRepository.class);

        for (Language lang : langs) {

            CswCapabilitiesInfo cswCapInfo = cswInfoFieldRepository.findCswCapabilitiesInfo(lang.getId());

            String langId = lang.getId();
            Element title = params.getChild("csw.title_" + langId);
            if (title != null) {
                cswCapInfo.setTitle(title.getValue());
            }
            Element abs = params.getChild("csw.abstract_" + langId);
            if (abs != null) {
                cswCapInfo.setAbstract(abs.getValue());
            }
            Element fees = params.getChild("csw.fees_" + langId);
            if (fees != null) {
                cswCapInfo.setFees(fees.getValue());
            }
            Element accessConstraints = params.getChild("csw.accessConstraints_" + langId);
            if (accessConstraints != null) {
                cswCapInfo.setAccessConstraints(accessConstraints.getValue());
            }
            // Save item
            cswInfoFieldRepository.save(cswCapInfo);
        }
    }

}