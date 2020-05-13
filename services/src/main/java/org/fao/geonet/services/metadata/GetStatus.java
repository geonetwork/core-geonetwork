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

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//=============================================================================

/**
 * Given a metadata id returns all associated status records. Called by the metadata.status service
 */
@Deprecated
public class GetStatus implements Service {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getBean(DataManager.class);
        AccessManager am = gc.getBean(AccessManager.class);

        String id = Utils.getIdentifierFromParameters(params, context);

        //-----------------------------------------------------------------------
        //--- check access
        int iLocalId = Integer.parseInt(id);

        if (!dataMan.existsMetadata(iLocalId))
            throw new IllegalArgumentException("Metadata not found --> " + id);

        if (!am.isOwner(context, id))
            throw new IllegalArgumentException("You are not the owner of metadata --> " + id);

        //-----------------------------------------------------------------------
        //--- retrieve metadata status

        MetadataStatus stats = dataMan.getStatus(iLocalId);

        String status = StatusValue.Status.DRAFT;
        String userId = "-1"; // no userId
        if (stats != null) {
            status = String.valueOf(stats.getId().getStatusId());
            userId = String.valueOf(stats.getId().getUserId());
        }

        //-----------------------------------------------------------------------
        //--- retrieve status values

        Element elStatus = gc.getBean(StatusValueRepository.class).findAllAsXml();

        @SuppressWarnings("unchecked")
        List<Element> kids = elStatus.getChildren();

        for (Element kid : kids) {

            kid.setName(Geonet.Elem.STATUS);

            //--- set status value of this metadata to 'on'

            if (kid.getChildText("id").equals(status)) {
                kid.addContent(new Element("on"));

                //--- set the userId of the submitter into the result
                kid.addContent(new Element("userId").setText(userId));
            }
        }

        //-----------------------------------------------------------------------
        //--- get the list of content reviewers for this metadata record

        Set<Integer> ids = new HashSet<Integer>();
        ids.add(Integer.valueOf(id));

        Element cRevs = am.getContentReviewers(context, ids);
        cRevs.setName("contentReviewers");

        //-----------------------------------------------------------------------
        //--- put it all together

        Element elRes = new Element(Jeeves.Elem.RESPONSE)
            .addContent(new Element(Geonet.Elem.ID).setText(id))
            .addContent(new Element("hasEditPermission").setText(am.hasEditPermission(context, id) ? "true" : "false"))
            .addContent(elStatus)
            .addContent(cRevs);

        return elRes;
    }
}

//=============================================================================


