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

package org.fao.geonet.kernel.harvest.harvester.csw;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.requests.CatalogRequest;
import org.fao.geonet.csw.common.requests.GetRecordByIdRequest;
import org.fao.geonet.csw.common.util.CswServer;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;
import org.fao.geonet.csw.common.Csw.ElementSetName;

//=============================================================================

public class Aligner
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Aligner(Logger log, ServiceContext sc, Dbms dbms, CswServer server, CswParams params)
	{
		this.log        = log;
		this.context    = sc;
		this.dbms       = dbms;
		this.server     = server;
		this.params     = params;

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		dataMan = gc.getDataManager();
		result  = new CswResult();
	}

	//--------------------------------------------------------------------------
	//---
	//--- Alignment method
	//---
	//--------------------------------------------------------------------------

	public CswResult align(Set<RecordInfo> records) throws Exception
	{
		log.info("Start of alignment for : "+ params.name);

		//-----------------------------------------------------------------------
		//--- retrieve all local categories and groups
		//--- retrieve harvested uuids for given harvesting node

		localCateg = new CategoryMapper(dbms);
		localGroups= new GroupMapper(dbms);
		localUuids = new UUIDMapper(dbms, params.uuid);
		dbms.commit();

		//-----------------------------------------------------------------------
		//--- remove old metadata

		for (String uuid : localUuids.getUUIDs())
			if (!exists(records, uuid))
			{
				String id = localUuids.getID(uuid);

				log.debug("  - Removing old metadata with id:"+ id);
				dataMan.deleteMetadata(dbms, id);
				dbms.commit();
				result.locallyRemoved++;
			}

		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for(RecordInfo ri : records)
		{
			result.totalMetadata++;

			log.debug("Obtained remote uuid:"+ ri.uuid +", changeDate:"+ ri.changeDate);

			String id = dataMan.getMetadataId(dbms, ri.uuid);

			if (id == null)	id = addMetadata(ri);
				else				updateMetadata(ri);

			dbms.commit();
			dataMan.indexMetadata(dbms, id);
		}

		log.info("End of alignment for : "+ params.name);

		return result;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : addMetadata
	//---
	//--------------------------------------------------------------------------

	private String addMetadata(RecordInfo ri) throws Exception
	{
		log.debug("  - Adding metadata with remote uuid="+ ri.uuid);

//		int id = dataMan.insertMetadataExt(dbms, schema, md, context.getSerialFactory(),
//													 siteId, createDate, changeDate, remoteUuid, null);

		int id =1;

		dataMan.setTemplate(dbms, id, "n", null);
		dataMan.setHarvestedBit(dbms, id, true);

		result.addedMetadata++;

//		addPrivileges(id, info.getChild("privileges"));
//		addCategories(id, info.getChild("categories"));

		return id +"";
	}

//			if (!dataMan.existsSchema(schema))
//			{
//				log.debug("  - Skipping unsupported schema : "+ schema);
//				result.schemaSkipped++;
//			}
	//--------------------------------------------------------------------------
	//--- Categories
	//--------------------------------------------------------------------------

	private void addCategories(String id, Element categ) throws Exception
	{
		List list = categ.getChildren("category");

		for(Iterator j=list.iterator(); j.hasNext();)
		{
			String catName = ((Element) j.next()).getAttributeValue("name");
			String catId   = localCateg.getID(catName);

			if (catId != null)
			{
				//--- remote category exists locally

				log.debug("    - Setting category : "+ catName);
				dataMan.setCategory(dbms, id, catId);
			}
		}
	}

	//--------------------------------------------------------------------------
	//--- Privileges
	//--------------------------------------------------------------------------

	private void addPrivileges(String id, Element privil) throws Exception
	{
		List list = privil.getChildren("group");

		for (int i=0; i<list.size(); i++)
		{
			Element group   = (Element) list.get(i);
			String  grpName = group.getAttributeValue("name");
			String  grpId   = localGroups.getID(grpName);

			if (grpId != null)
			{
				//--- remote group exists locally

				log.debug("    - Setting privileges for group : "+ grpName);
				addOperations(group, id, grpId);
			}
		}
	}

	//--------------------------------------------------------------------------

	private void addOperations(Element group, String id, String grpId) throws Exception
	{
		List opers = group.getChildren("operation");

		for (int j=0; j<opers.size(); j++)
		{
			Element oper   = (Element) opers.get(j);
			String  opName = oper.getAttributeValue("name");

			int opId = dataMan.getAccessManager().getPrivilegeId(opName);

			log.debug("       --> "+ opName);
			dataMan.setOperation(dbms, id, grpId, opId +"");
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : updateMetadata
	//---
	//--------------------------------------------------------------------------

	private void updateMetadata(RecordInfo ri) throws Exception
	{
		if (localUuids.getID(ri.uuid) == null)
		{
			log.info("  - Warning! The remote uuid '"+ ri.uuid +"' does not belong to harvest node '"+ params.name+"'");
			log.info("     - The site id of this metadata has been changed.");
			log.info("     - The metadata update will be skipped.");

			result.uuidSkipped++;
		}
		else
		{
			updateMetadata("", null, ri);
		}
	}

	//--------------------------------------------------------------------------

	private void updateMetadata(String id, Element md, RecordInfo ri) throws Exception
	{
		String date = localUuids.getChangeDate(ri.uuid);

		if (!updateCondition(date, ri.changeDate))
		{
			log.debug("  - XML not changed to local metadata with id="+ id);
			result.unchangedMetadata++;
		}
		else
		{
			log.debug("  - Updating local metadata with id="+ id);
			dataMan.updateMetadataExt(dbms, id, md, ri.changeDate);

			result.updatedMetadata++;
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	/** Return true if the uuid is present in the remote node */

	private boolean exists(Set<RecordInfo> records, String uuid)
	{
		for(RecordInfo ri : records)
			if (uuid.equals(ri.uuid))
				return true;

		return false;
	}

	//--------------------------------------------------------------------------

	private boolean updateCondition(String localDate, String remoteDate)
	{
		ISODate local = new ISODate(localDate);
		ISODate remote= new ISODate(remoteDate);

		//--- accept if remote date is greater than local date

		return (remote.sub(local) > 0);
	}

	//--------------------------------------------------------------------------

	private Element retrieveMetadata(String uuid) throws OperationAbortedEx
	{
		GetRecordByIdRequest request = new GetRecordByIdRequest();

		request.setElementSetName(ElementSetName.FULL);
		request.addId(uuid);

		CswServer.Operation oper = server.getOperation(CswServer.GET_RECORD_BY_ID);

		if (oper.postUrl != null)
		{
			request.setUrl(oper.postUrl);
			request.setMethod(CatalogRequest.Method.POST);
		}
		else
		{
			request.setUrl(oper.getUrl);
			request.setMethod(CatalogRequest.Method.GET);
		}

		if (params.useAccount)
			request.setCredentials(params.username, params.password);

		try
		{
			log.debug("Getting record from : "+ params.name +" ("+ uuid +")");
			Element response = request.execute();
			log.debug("Record got:\n"+Xml.getString(response));

			return response;
		}
		catch(Exception e)
		{
			log.warning("Raised exception while getting record : "+ e);

			//--- we don't raise any exception here. Just try to go on
			return null;
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private Logger         log;
	private ServiceContext context;
	private Dbms           dbms;
//	private XmlRequest     req;
	private CswParams      params;
	private DataManager    dataMan;
	private CswServer      server;
	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
	private UUIDMapper     localUuids;
	private CswResult      result;
}

//=============================================================================


