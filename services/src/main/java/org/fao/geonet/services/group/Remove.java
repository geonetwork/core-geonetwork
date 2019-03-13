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

package org.fao.geonet.services.group;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.UserGroupId_;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;


/**
 * Removes a group from the system. Note that the group MUST NOT have operations associated.
 */
@Deprecated
public class Remove extends NotInReadOnlyModeService {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String id = Util.getParam(params, Params.ID);


        OperationAllowedRepository operationAllowedRepo = context.getBean(OperationAllowedRepository.class);
        UserGroupRepository userGroupRepo = context.getBean(UserGroupRepository.class);
        GroupRepository groupRepo = context.getBean(GroupRepository.class);

        Integer iId = Integer.valueOf(id);
        List<Integer> reindex = operationAllowedRepo.findAllIds(OperationAllowedSpecs.hasGroupId(iId), OperationAllowedId_.metadataId);

        operationAllowedRepo.deleteAllByGroupId(iId);
        userGroupRepo.deleteAllByIdAttribute(UserGroupId_.groupId, Arrays.asList(iId));
        groupRepo.delete(iId);
        //--- reindex affected metadata

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);


        dm.indexMetadata(Lists.transform(reindex, Functions.toStringFunction()));

        return new Element(Jeeves.Elem.RESPONSE)
            .addContent(new Element(Jeeves.Elem.OPERATION).setText(Jeeves.Text.REMOVED));
    }
}
