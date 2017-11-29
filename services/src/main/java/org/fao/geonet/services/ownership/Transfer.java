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

package org.fao.geonet.services.ownership;

import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasGroupId;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import com.google.common.base.Optional;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 *
 */
@Deprecated
public class Transfer extends NotInReadOnlyModeService {
    /**
     *
     * @param appPath
     * @param params
     * @throws Exception
     */
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    /**
     *
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        int sourceUsr = Util.getParamAsInt(params, "sourceUser");
        int sourceGrp = Util.getParamAsInt(params, "sourceGroup");
        int targetUsr = Util.getParamAsInt(params, "targetUser");
        int targetGrp = Util.getParamAsInt(params, "targetGroup");

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        final IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);
        final IMetadataManager metadataManager = context.getBean(IMetadataManager.class);

        //--- transfer privileges (if case)

        Set<String> sourcePriv = retrievePrivileges(context, sourceUsr, sourceGrp);
        Set<String> targetPriv = retrievePrivileges(context, null, targetGrp);

        //--- a commit just to release some resources

        dm.flush();

        int privCount = 0;

        Set<Integer> metadata = new HashSet<Integer>();

        if (sourcePriv.size() > 0) {
            for (String priv : sourcePriv) {
                StringTokenizer st = new StringTokenizer(priv, "|");

                int opId = Integer.parseInt(st.nextToken());
                int mdId = Integer.parseInt(st.nextToken());

                // 2 cases could happen, 1) only the owner change
                // in that case sourceGrp = targetGrp and operations
                // allowed does not need to be modified.
                if (sourceGrp != targetGrp) {
                    // 2) the sourceGrp != targetGrp and in that
                    // case, all operations need to be transfered to
                    // the new group if not already defined.
                    dm.unsetOperation(context, mdId, sourceGrp, opId);

                    if (!targetPriv.contains(priv)) {
                        OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
                        OperationAllowedId id = new OperationAllowedId()
                            .setGroupId(targetGrp)
                            .setMetadataId(mdId)
                            .setOperationId(opId);
                        OperationAllowed operationAllowed = new OperationAllowed(id);
                        repository.save(operationAllowed);
                    }
                }

                // Collect all metadata ids
                metadata.add(mdId);
                privCount++;
            }
        }
        // If no privileges defined for the target group
        // assign the new owner and ownerGroup for the source
        // user records.
        final List<Integer> sourceUserRecords =
            metadataRepository.findAllIdsBy(MetadataSpecs.hasOwner(sourceUsr));
        metadata.addAll(sourceUserRecords);

        // Set owner for all records to be modified.
        for (Integer i : metadata) {
            final AbstractMetadata metadata1 = metadataRepository.findOne(i);
            metadata1.getSourceInfo().setGroupOwner(targetGrp).setOwner(targetUsr);
            metadataManager.save(metadata1);
        }

        dm.flush();

        //--- reindex metadata
        List<String> list = new ArrayList<String>();
        for (int mdId : metadata) {
            list.add(Integer.toString(mdId));
        }
        dm.indexMetadata(list);

        //--- return summary
        return new Element("response")
            .addContent(new Element("privileges").setText(privCount + ""))
            .addContent(new Element("metadata").setText(metadata.size() + ""));
    }

    /**
     * @param userId can be null
     */
    private Set<String> retrievePrivileges(ServiceContext context, Integer userId, int groupId) throws SQLException {
        OperationAllowedRepository opAllowedRepo = context.getBean(OperationAllowedRepository.class);
        final List<OperationAllowed> opsAllowed;
        if (userId == null) {
            opsAllowed = opAllowedRepo.findAllById_GroupId(groupId);
        } else {
            opsAllowed = opAllowedRepo.findAllWithOwner(userId, Optional.of(hasGroupId(groupId)));
        }

        Set<String> result = new HashSet<String>();
        for (OperationAllowed elem : opsAllowed) {
            int opId = elem.getId().getOperationId();
            int mdId = elem.getId().getMetadataId();
            result.add(opId + "|" + mdId);
        }
        return result;
    }
}
