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

package org.fao.geonet.services.metadata;

import java.nio.file.Path;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.exceptions.OperationNotAllowedEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.Utils;
import org.fao.geonet.utils.IO;
import org.jdom.Element;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Removes a metadata from the system.
 */
@Deprecated
public class Delete extends BackupFileService {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getBean(DataManager.class);
        AccessManager accessMan = gc.getBean(AccessManager.class);

        boolean backupFile = Util.getParam(params, Params.BACKUP_FILE, true);
        String id = Utils.getIdentifierFromParameters(params, context);

        // If send a non existing uuid, Utils.getIdentifierFromParameters returns null
        if (id == null)
            throw new IllegalArgumentException("Metadata internal identifier or UUID not found.");

        //-----------------------------------------------------------------------
        //--- check access

        AbstractMetadata metadata = context.getBean(IMetadataUtils.class).findOne(id);

        if (metadata == null)
            throw new IllegalArgumentException("Metadata with identifier " + id + " not found.");

        if (!accessMan.canEdit(context, id))
            throw new OperationNotAllowedEx();

        //-----------------------------------------------------------------------
        //--- backup metadata in 'removed' folder

        if (metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE && backupFile)
            backupFile(context, id, metadata.getUuid(), MEFLib.doExport(context, metadata.getUuid(), "full", false, true, false, false));

        //-----------------------------------------------------------------------
        //--- remove the metadata directory including the public and private directories.
        IO.deleteFileOrDirectory(Lib.resource.getMetadataDir(context.getBean(GeonetworkDataDirectory.class), id));

        //-----------------------------------------------------------------------
        //--- delete metadata and return status

        dataMan.deleteMetadata(context, id);

        Element elResp = new Element(Jeeves.Elem.RESPONSE);
        elResp.addContent(new Element(Geonet.Elem.ID).setText(id));

        return elResp;
    }

}
