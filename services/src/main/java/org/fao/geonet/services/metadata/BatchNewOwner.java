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

import com.google.common.base.Optional;
import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasGroupId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;
import static org.springframework.data.jpa.domain.Specifications.where;


/**
 * Sets new owner for a set of metadata records.
 */
public class BatchNewOwner extends NotInReadOnlyModeService {
    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        AccessManager accessMan = gc.getBean(AccessManager.class);
        UserSession session = context.getUserSession();


        String targetUsr = Util.getParam(params, Params.USER);
        String targetGrp = Util.getParam(params, Params.GROUP);

        Set<Integer> metadata = new HashSet<Integer>();
        Set<Integer> notFound = new HashSet<Integer>();
        Set<Integer> notOwner = new HashSet<Integer>();

        context.info("Get selected metadata");
        SelectionManager sm = SelectionManager.getManager(session);

        synchronized (sm.getSelection("metadata")) {
            for (Iterator<String> iter = sm.getSelection("metadata").iterator(); iter.hasNext(); ) {
                String uuid = (String) iter.next();
                String id = dm.getMetadataId(uuid);

                context.info("Attempting to set metadata owner on: " + id);

                //--- check existence and access
                Metadata info = context.getBean(MetadataRepository.class).findOne(id);

                if (info == null) {
                    notFound.add(Integer.valueOf(id));
                } else if (!accessMan.isOwner(context, id)) {
                    notOwner.add(Integer.valueOf(id));
                } else {

                    //-- Get existing owner and privileges for that owner - note that
                    //-- owners don't actually have explicit permissions - only their
                    //-- group does which is why we have an ownerGroup (parameter groupid)
                    Integer sourceUsr = info.getSourceInfo().getOwner();
                    Integer sourceGrp = info.getSourceInfo().getGroupOwner();
                    Vector<OperationAllowedId> sourcePriv =
                            retrievePrivileges(context, id, "" + sourceUsr, "" + sourceGrp);

                    // -- Set new privileges for new owner from privileges of the old
                    // -- owner, if none then set defaults
                    if (sourcePriv.size() == 0) {
                        dm.copyDefaultPrivForGroup(context, id, targetGrp, false);
                        context.info("No privileges for user " + sourceUsr + " on metadata " + id + ", so setting default privileges");
                    } else {
                        for (OperationAllowedId priv : sourcePriv) {
                            dm.unsetOperation(context, id,
                                    "" + sourceGrp,
                                    "" + priv.getOperationId());
                            dm.setOperation(context, id, targetGrp,
                                    "" + priv.getOperationId());
                        }
                    }
                    // -- set the new owner into the metadata record
                    dm.updateMetadataOwner(Integer.parseInt(id), targetUsr, targetGrp);

                    metadata.add(Integer.valueOf(id));
                }
            }
        }


        // -- reindex metadata
        context.info("Re-indexing metadata");
        BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dm, metadata);
        r.process();

        // -- for the moment just return the sizes - we could return the ids
        // -- at a later stage for some sort of result display
        return new Element(Jeeves.Elem.RESPONSE)
                .addContent(new Element("done").setText(metadata.size() + ""))
                .addContent(new Element("notOwner").setText(notOwner.size() + ""))
                .addContent(new Element("notFound").setText(notFound.size() + ""));
    }

    //--------------------------------------------------------------------------

    private Vector<OperationAllowedId> retrievePrivileges(ServiceContext context, String id, String userId, String groupId) throws Exception {

        OperationAllowedRepository opAllowRepo = context.getBean(OperationAllowedRepository.class);

        Integer iMetadataId = Integer.valueOf(id);
        Integer iUserId = Integer.valueOf(userId);
        Integer iGroupId = Integer.valueOf(groupId);
        Specification<OperationAllowed> spec =
                where(hasMetadataId(iMetadataId))
                        .and(hasGroupId(iGroupId));

        List<OperationAllowed> operationsAllowed = opAllowRepo.findAllWithOwner(iUserId, Optional.of(spec));

        Vector<OperationAllowedId> result = new Vector<OperationAllowedId>();
        for (OperationAllowed operationAllowed : operationsAllowed) {
            result.add(operationAllowed.getId());
        }

        return result;
    }

}