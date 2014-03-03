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
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.metadata.StatusActions;
import org.fao.geonet.kernel.metadata.StatusActionsFactory;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Assigns status to metadata.
 */
public class BatchUpdateStatus extends NotInReadOnlyModeService {
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception
	{
		String status = Util.getParam(params, Params.STATUS);
		String changeMessage = Util.getParam(params, Params.CHANGE_MESSAGE);

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dm = gc.getBean(DataManager.class);
		AccessManager accessMan = gc.getBean(AccessManager.class);
		UserSession us = context.getUserSession();

		context.info("Get selected metadata");
		SelectionManager sm = SelectionManager.getManager(us);

		Set<Integer> metadata = new HashSet<Integer>();
		Set<Integer> notFound = new HashSet<Integer>();
		Set<Integer> notOwner = new HashSet<Integer>();

        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        synchronized(sm.getSelection("metadata")) {
		for (Iterator<String> iter = sm.getSelection("metadata").iterator(); iter.hasNext();) {
			String uuid = (String) iter.next();
			String id   = dm.getMetadataId(uuid);


            final Integer iId = Integer.valueOf(id);
            if (metadataRepository.exists(iId)) {
				notFound.add(iId);
			} else if (!accessMan.isOwner(context, id)) {
				notOwner.add(iId);
			} else {
				metadata.add(iId);
			}
		}
		}

        ISODate changeDate = new ISODate();

		//--- use StatusActionsFactory and StatusActions class to 
    //--- change status and carry out behaviours for status changes
    StatusActionsFactory saf = new StatusActionsFactory(gc.getStatusActionsClass());

    StatusActions sa = saf.createStatusActions(context);

    Set<Integer> noChange = sa.statusChange(status, metadata, changeDate, changeMessage);

		//--- reindex metadata
		context.info("Re-indexing metadata");
		BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dm, metadata);
		r.process();

		// -- for the moment just return the sizes - we could return the ids
		// -- at a later stage for some sort of result display
		return new Element(Jeeves.Elem.RESPONSE)
						.addContent(new Element("done")    .setText(metadata.size()+""))
						.addContent(new Element("notOwner").setText(notOwner.size()+""))
						.addContent(new Element("notFound").setText(notFound.size()+""))
						.addContent(new Element("noChange").setText(noChange.size()+""));
	}
}