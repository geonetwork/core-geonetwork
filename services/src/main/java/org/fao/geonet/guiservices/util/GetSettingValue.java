//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.guiservices.util;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Service to retrieve a setting value to be used in internally by Jeeves services.
 *
 * Requires to use a parameter "settingValue" with the setting path to retrieve.
 *
 * Example:
 *
 * <service name="pdf.search">
 *    ...
 *    <output sheet="../xslt/services/pdf/portal-present-fop.xsl" file="true"
 *               contentType="application/pdf">
 *         ...
 *         <call name="documentFileName" class=".guiservices.util.GetSettingValue">
 *           <param name="setting" value="metadata/pdfReport/pdfName"/>
 *         </call>
 *    </output>
 *     ...
 * </service>
 *
 */
public class GetSettingValue implements Service {

    private String setting;

    public void init(Path appPath, ServiceConfig params) throws Exception {
        setting = params.getValue("setting");
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        String settingValue = "";

        if (StringUtils.isNotEmpty(setting)) {
            SettingManager settingManager = context.getBean(SettingManager.class);

            settingValue = settingManager.getValue(setting);

        }

        return new Element("a").setText(settingValue);
    }
}
