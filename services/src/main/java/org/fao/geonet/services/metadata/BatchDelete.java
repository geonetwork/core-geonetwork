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

import com.google.common.collect.Sets;
import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.IO;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Removes a metadata from the system.
 */
public class BatchDelete extends BackupFileService {
	public void init(Path appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dataMan   = gc.getBean(DataManager.class);
		AccessManager accessMan = gc.getBean(AccessManager.class);
		UserSession   session   = context.getUserSession();

		Set<String> metadata = new HashSet<>();
		Set<String> notFound = new HashSet<>();
		Set<String> notOwner = new HashSet<>();
        boolean backupFile = Util.getParam(params, Params.BACKUP_FILE, true);

        if(context.isDebugEnabled()) {
            context.debug("Get selected metadata");
        }
		SelectionManager sm = SelectionManager.getManager(session);
        final Set<String> selection;
        synchronized(sm.getSelection(SelectionManager.SELECTION_METADATA)) {
            selection = Sets.newHashSet(sm.getSelection(SelectionManager.SELECTION_METADATA));

            // Clear the selection after delete
            Element clearSelectionParams = params.addContent(new Element("selected").setText(SelectionManager.REMOVE_ALL_SELECTED));
            SelectionManager.updateSelection("metadata", session, clearSelectionParams, context);
        }

        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        for (String uuid : selection) {
            if (context.isDebugEnabled()) {
                context.debug("Deleting metadata with uuid:" + uuid);
            }

            Metadata info = metadataRepository.findOneByUuid(uuid);
            if (info == null) {
                notFound.add(uuid);
            } else if (!accessMan.isOwner(context, String.valueOf(info.getId()))) {
                notOwner.add(uuid);
            } else {
                String idString = String.valueOf(info.getId());

                //--- backup metadata in 'removed' folder
                if (backupFile && info.getDataInfo().getType() != MetadataType.SUB_TEMPLATE) {
                    backupFile(context, idString, info.getUuid(), MEFLib.doExport(context, info.getUuid(), "full", false, true, false));
                }

                //--- remove the metadata directory
                Path pb = Lib.resource.getMetadataDir(context.getBean(GeonetworkDataDirectory.class), idString);
                IO.deleteFileOrDirectory(pb);

                //--- delete metadata and return status
                dataMan.deleteMetadata(context, idString);
                if (context.isDebugEnabled())
                    context.debug("  Metadata with id " + idString + " deleted.");
                metadata.add(uuid);
            }
        }

		// -- for the moment just return the sizes - we could return the ids
		// -- at a later stage for some sort of result display
		return new Element(Jeeves.Elem.RESPONSE)
			.addContent(new Element("done")    .setText(metadata.size()+""))
			.addContent(new Element("notOwner").setText(notOwner.size()+""))
			.addContent(new Element("notFound").setText(notFound.size()+""));
	}
}