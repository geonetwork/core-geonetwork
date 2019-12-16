//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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

package org.fao.geonet.services.logo;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.FilePathChecker;
import org.jdom.Element;

import java.nio.file.Path;

@Deprecated
public class Delete implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
        throws Exception {

        final Resources resources = context.getBean(Resources.class);
        final Path logoDirectory = resources.locateHarvesterLogosDir(context);

        String file = Util.getParam(params, Params.FNAME);

        FilePathChecker.verify(file);

        if (StringUtils.isEmpty(file)) {
            throw new Exception("Logo name is not defined.");
        }

        Element response = new Element("response");
        resources.deleteImageIfExists(file, logoDirectory);
        response.addContent(new Element("status")
            .setText("Logo removed."));
        return response;
    }
}
