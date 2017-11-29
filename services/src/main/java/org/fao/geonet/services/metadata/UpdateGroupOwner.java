//=============================================================================
//===	Copyright (C) 2001-2014 Food and Agriculture Organization of the
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
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.exceptions.OperationNotAllowedEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Update the metadata group owner.
 *
 * For batch update see BatchNewOwner.
 */
@Deprecated
public class UpdateGroupOwner extends NotInReadOnlyModeService {

    /**
     * @param appPath
     * @param params
     * @throws Exception
     */
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    /**
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getBean(DataManager.class);
        AccessManager accessMan = gc.getBean(AccessManager.class);

        String id = Utils.getIdentifierFromParameters(params, context);
        String groupOwner = Util.getParam(params, "groupid");

        int iLocalId = Integer.parseInt(id);
        if (!dataMan.existsMetadata(iLocalId)) {
            throw new IllegalArgumentException("Metadata with identifier '" + id + "' not found.");
        }

        if (!accessMan.canEdit(context, id)) {
            throw new OperationNotAllowedEx();
        }

        int iGroupOwner = Integer.parseInt(groupOwner);
        Group group = context.getBean(GroupRepository.class).findOne(iGroupOwner);
        if (group == null) {
            throw new IllegalArgumentException("Group with identifier '" + groupOwner + "' not found.");
        }

        //--- Update groupOwner
        IMetadataManager metadataRepository = context.getBean(IMetadataManager.class);
        IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        AbstractMetadata metadata = metadataUtils.findOne(iLocalId);
        metadata.getSourceInfo().setGroupOwner(iGroupOwner);
        metadataRepository.save(metadata);

        //--- index metadata
        dataMan.indexMetadata(id, true, null);

        //--- return id for showing
        return new Element(Jeeves.Elem.RESPONSE).
            addContent(new Element(Geonet.Elem.ID).
                setText(id));
    }
}
