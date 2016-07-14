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

package org.fao.geonet.services.relations;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataRelationId;
import org.fao.geonet.repository.MetadataRelationRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Removes a user from the system. It removes the relationship to a group too.
 */
@Deprecated
public class Remove extends NotInReadOnlyModeService {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service to delete a related resource
    //---
    //--------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        int parentId = Integer.parseInt(Utils.getIdentifierFromParameters(
            params, context, Params.PARENT_UUID, Params.PARENT_ID));
        int childId = Integer.parseInt(Utils.getIdentifierFromParameters(
            params, context, Params.CHILD_UUID, Params.CHILD_ID));

        final MetadataRelationRepository relationRepository = context.getBean(MetadataRelationRepository.class);
        relationRepository.delete(new MetadataRelationId(parentId, childId));

        return new Element(Jeeves.Elem.RESPONSE);
    }
}
