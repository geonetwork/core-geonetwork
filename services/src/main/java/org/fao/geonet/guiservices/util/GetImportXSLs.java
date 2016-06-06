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

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

import java.io.File;
import java.nio.file.Path;

//=============================================================================

/**
 * This service returns all stylesheets that can be used during batch import selected tab
 */

public class GetImportXSLs implements Service {
    private Path appPath;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
        this.appPath = appPath;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        String dir = appPath + File.separator + Geonet.Path.IMPORT_STYLESHEETS;

        String sheets[] = new File(dir).list();

        if (sheets == null)
            throw new Exception("Cannot scan directory : " + dir);

        Element elRoot = new Element("a");

        for (String sheet : sheets) {
            if (sheet.endsWith(".xsl")) {
                int pos = sheet.lastIndexOf(".xsl");
                String name = sheet.substring(0, pos);
                String id = sheet;

                Element el = new Element(Jeeves.Elem.RECORD);

                el.addContent(new Element(Geonet.Elem.ID).setText(id));
                el.addContent(new Element(Geonet.Elem.NAME).setText(name));

                elRoot.addContent(el);
            }
        }

        return elRoot;
    }
}

//=============================================================================

