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

import static org.fao.geonet.repository.specification.OperationAllowedSpecs.*;
import static org.springframework.data.jpa.domain.Specifications.*;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specifications;

import java.sql.SQLException;
import java.util.*;

/**
 *
 */
public class Transfer extends NotInReadOnlyModeService {
    /**
     *
     * @param appPath
     * @param params
     * @throws Exception
     */
	public void init(String appPath, ServiceConfig params) throws Exception {}

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
		DataManager   dm = gc.getBean(DataManager.class);

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		//--- transfer privileges (if case)

		Set<String> sourcePriv = retrievePrivileges(context, sourceUsr, sourceGrp);
		Set<String> targetPriv = retrievePrivileges(context, null, targetGrp);

		//--- a commit just to release some resources

		dbms.commit();

		int privCount = 0;

		Set<Integer> metadata = new HashSet<Integer>();

		for (String priv : sourcePriv)
		{
			StringTokenizer st = new StringTokenizer(priv, "|");

			int opId = Integer.parseInt(st.nextToken());
			int mdId = Integer.parseInt(st.nextToken());

			dm.unsetOperation(context, dbms, mdId, sourceGrp, opId);

			if (!targetPriv.contains(priv)) {
			    OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
			    OperationAllowedId id = new OperationAllowedId()
			        .setGroupId(targetGrp)
			        .setMetadataId(mdId)
			        .setOperationId(opId);
                OperationAllowed operationAllowed = new OperationAllowed(id );
                repository.save(operationAllowed);
			}

			dbms.execute("UPDATE Metadata SET owner=?, groupOwner=? WHERE id=?", targetUsr, targetGrp, mdId);

			metadata.add(mdId);
			privCount++;
		}

		dbms.commit();

		//--- reindex metadata
        List<String> list = new ArrayList<String>();
		for (int mdId : metadata) {
            list.add(Integer.toString(mdId));
        }
        
        dm.indexInThreadPool(context,list, dbms);

		//--- return summary
		return new Element("response")
			.addContent(new Element("privileges").setText(privCount      +""))
			.addContent(new Element("metadata")  .setText(metadata.size()+""));
	}

    /**
     *
     * @param context
     * @param userId can be null
     * @param groupId
     * @return
     * @throws java.sql.SQLException
     */
    private Set<String> retrievePrivileges(ServiceContext context, Integer userId, int groupId) throws SQLException {
        OperationAllowedRepository opAllowedRepo = context.getBean(OperationAllowedRepository.class);
        final List<OperationAllowed> opsAllowed;
        if (userId == null) {
            opsAllowed = opAllowedRepo.findById_GroupId(groupId);
        } else {
            Specifications<OperationAllowed> spec = where(hasGroupId(groupId)).and(metadataHasOwnerId(userId));
            opsAllowed = opAllowedRepo.findAll(spec);
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