//=============================================================================
//===	Copyright (C) 2001-2022 Food and Agriculture Organization of the
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
import org.jdom.Element;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GetCsvFileName implements Service {
    private String fileName;

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {
        this.fileName = params.getValue("fileName", "metadata_{datetime}.csv");
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {

        String documentName=fileName;

        if (StringUtils.isEmpty(documentName)) {
            documentName = "document.csv";
        } else {
            if (!documentName.endsWith(".csv")) {
                documentName = documentName + ".csv";
            }

            Calendar c = Calendar.getInstance();

            documentName = documentName.replace("{year}", c.get(Calendar.YEAR) + "");
            documentName = documentName.replace("{month}", c.get(Calendar.MONTH) + "");
            documentName = documentName.replace("{day}", c.get(Calendar.DAY_OF_MONTH) + "");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");

            documentName = documentName.replace("{date}", dateFormat.format(c.getTime()));
            documentName = documentName.replace("{datetime}", datetimeFormat.format(c.getTime()));
        }

        return new Element("a").setText(documentName);
    }
}
