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
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.services.ReadWriteController;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlRootElement;

import static org.fao.geonet.kernel.SelectionManager.SELECTION_METADATA;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasGroupId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;
import static org.springframework.data.jpa.domain.Specifications.where;


/**
 * Sets new owner for a set of metadata records.
 */
@ReadWriteController
@Controller
public class BatchNewOwner {

    @RequestMapping(value = "/{lang}/metadata.batch.newowner", produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public NewOwnerResult exec(
            @PathVariable String lang,
            @RequestParam("userId") String targetUsr,
            @RequestParam("groupId") String targetGrp,
            HttpServletRequest request) throws Exception {

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        DataManager dataManager = appContext.getBean(DataManager.class);
        ServiceManager serviceManager = appContext.getBean(ServiceManager.class);

        ServiceContext context = serviceManager.createServiceContext("metadata.batch.newowner", lang, request);
        UserSession session = context.getUserSession();

        context.info("Get selected metadata");
        SelectionManager sm = SelectionManager.getManager(session);
        Collection<String> selection = new ArrayList<>();
        synchronized (sm.getSelection(SELECTION_METADATA)) {
            selection.addAll(sm.getSelection(SELECTION_METADATA));
        }

        Set<Integer> modified = new HashSet<Integer>();
        NewOwnerResult result = setNewOwner(context, targetUsr, targetGrp, selection, modified);


        // -- reindex metadata
        context.info("Re-indexing metadata");
        BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataManager, modified);
        r.process();

        return result;
    }

    private NewOwnerResult setNewOwner(ServiceContext context, String targetUsr, String targetGrp,
                                       Collection<String> selection, Set <Integer> modified) throws Exception {

        AccessManager accessManager = context.getBean(AccessManager.class);
        DataManager dataManager = context.getBean(DataManager.class);

        Set<Integer> notFound = new HashSet<Integer>();
        Set<Integer> notOwner = new HashSet<Integer>();
        for (String uuid : selection) {
            String id = dataManager.getMetadataId(uuid);

            context.info("Attempting to set metadata owner on: " + id);

            //--- check existence and access
            Metadata info = null;
            if (id != null) {
                info = context.getBean(MetadataRepository.class).findOne(id);
            }
            if (info == null) {
                if (id != null) {
                    notFound.add(Integer.valueOf(id));
                } else {
                    notFound.add(null);
                }
            } else if (!accessManager.isOwner(context, id)) {
                notOwner.add(Integer.valueOf(id));
            } else {

                //-- Get existing owner and privileges for that owner - note that
                //-- owners don't actually have explicit permissions - only their
                //-- group does which is why we have an ownerGroup (parameter groupid)
                Integer sourceUsr = info.getSourceInfo().getOwner();
                Integer sourceGrp = info.getSourceInfo().getGroupOwner();
                Vector<OperationAllowedId> sourcePriv =
                        retrievePrivileges(context, id, sourceUsr,  sourceGrp);

                // -- Set new privileges for new owner from privileges of the old
                // -- owner, if none then set defaults
                if (sourcePriv.size() == 0) {
                    dataManager.copyDefaultPrivForGroup(context, id, targetGrp, false);
                    context.info("No privileges for user " + sourceUsr + " on metadata " + id + ", so setting default privileges");
                } else {
                    for (OperationAllowedId priv : sourcePriv) {
                        if (sourceGrp != null) {
                            dataManager.unsetOperation(context, id,
                                    "" + sourceGrp,
                                    "" + priv.getOperationId());
                        }
                        dataManager.setOperation(context, id, targetGrp,
                                "" + priv.getOperationId());
                    }
                }
                // -- set the new owner into the metadata record
                dataManager.updateMetadataOwner(Integer.parseInt(id), targetUsr, targetGrp);

                modified.add(Integer.valueOf(id));
            }
        }

        return new NewOwnerResult(modified.size(), notOwner.size(), notFound.size());
    }

    //--------------------------------------------------------------------------

    private Vector<OperationAllowedId> retrievePrivileges(ServiceContext context, String id, Integer userId, Integer groupId) throws Exception {

        OperationAllowedRepository opAllowRepo = context.getBean(OperationAllowedRepository.class);

        int iMetadataId = Integer.parseInt(id);
        Specifications<OperationAllowed> spec =
                where(hasMetadataId(iMetadataId));
        if (groupId != null) {
            spec = spec.and(hasGroupId(groupId));
        }

        List<OperationAllowed> operationsAllowed = opAllowRepo.findAllWithOwner(userId, Optional.of((Specification<OperationAllowed>) spec));

        Vector<OperationAllowedId> result = new Vector<OperationAllowedId>();
        for (OperationAllowed operationAllowed : operationsAllowed) {
            result.add(operationAllowed.getId());
        }

        return result;
    }


    @XmlRootElement(name = "response")
    public static class NewOwnerResult {
        private final int done;
        private final int notOwner;
        private final int notFound;

        public NewOwnerResult(int total, int notOwner, int notFound) {
            this.done = total;
            this.notOwner = notOwner;
            this.notFound = notFound;
        }

        public int getDone() {
            return done;
        }

        public int getNotOwner() {
            return notOwner;
        }

        public int getNotFound() {
            return notFound;
        }
    }


}