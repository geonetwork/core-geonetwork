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

package org.fao.geonet.services.resources;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.Utils;
import org.fao.geonet.utils.FilePathChecker;
import org.jdom.Element;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Delete an uploaded file from the data directory.
 */
@Deprecated
public class Get extends NotInReadOnlyModeService {

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element serviceSpecificExec(Element params, ServiceContext context)
        throws Exception {
        String id = Utils.getIdentifierFromParameters(params, context);
        String filename = Util.getParam(params, Params.FILENAME);
        String access = Util.getParam(params, Params.ACCESS);

        Lib.resource.checkEditPrivilege(context, id);

        FilePathChecker.verify(filename);

        // delete the file
        FilePathChecker.verify(filename);
        final Store store = context.getBean("resourceStore", Store.class);
        final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        final String uuid = metadataUtils.getMetadataUuid(id);
        store.delResource(context, uuid, MetadataResourceVisibility.parse(access), filename, true);

        return new Element(Jeeves.Elem.RESPONSE)
            .addContent(new Element(Geonet.Elem.ID).setText(id));
    }
}
