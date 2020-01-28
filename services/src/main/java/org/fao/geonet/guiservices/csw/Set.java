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

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.CswCapabilitiesInfoField;
import org.fao.geonet.domain.Language;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.CswCapabilitiesInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.CswCapabilitiesInfoFieldRepository;
import org.fao.geonet.repository.LanguageRepository;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class Set implements Service {

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        // Save values in settings
        saveCswServerConfig(params, gc.getBean(SettingManager.class));

        // Process parameters and save capabilities information in database
        saveCswCapabilitiesInfo(params, context);

        // Build response
        return new Element(Jeeves.Elem.RESPONSE).setText("ok");
    }

    private void saveCswServerConfig(Element params, SettingManager settingManager)
        throws Exception {

        String cswEnableValue = Util.getParam(params, "csw.enable", "");
        settingManager.setValue(Settings.SYSTEM_CSW_ENABLE, cswEnableValue.equals("on"));


        String cswMetadataPublicValue = Util.getParam(params, "csw.metadataPublic", "");
        settingManager.setValue(Settings.SYSTEM_CSW_METADATA_PUBLIC, cswMetadataPublicValue.equals("on"));

        // Save contact
        String capabilityRecordId = Util.getParam(params, "csw.capabilityRecordId", "-1");
        settingManager.setValue(Settings.SYSTEM_CSW_CAPABILITY_RECORD_UUID, capabilityRecordId);
    }

    @Deprecated
    private void saveCswCapabilitiesInfo(Element params, ServiceContext serviceContext)
        throws Exception {

        List<Language> languages = serviceContext.getBean(LanguageRepository.class).findAll();

        List<CswCapabilitiesInfoField> toSave = new ArrayList<CswCapabilitiesInfoField>();

        final CswCapabilitiesInfoFieldRepository capabilitiesInfoFieldRepository = serviceContext.getBean(CswCapabilitiesInfoFieldRepository.class);
        for (Language language : languages) {
            CswCapabilitiesInfo cswCapInfo = capabilitiesInfoFieldRepository.findCswCapabilitiesInfo(language.getId());

            final String langId = language.getId();
            cswCapInfo.setTitle(getValue(params, "csw.title_" + langId));
            cswCapInfo.setAbstract(getValue(params, "csw.abstract_" + langId));
            cswCapInfo.setFees(getValue(params, "csw.fees_" + langId));
            cswCapInfo.setAccessConstraints(getValue(params, "csw.accessConstraints_" + langId));

            toSave.addAll(cswCapInfo.getFields());
        }

        capabilitiesInfoFieldRepository.save(toSave);
    }

    private String getValue(Element params, String paramId) {
        final Element child = params.getChild(paramId);
        if (child != null) {
            return child.getValue();
        } else {
            return "";
        }
    }

}
