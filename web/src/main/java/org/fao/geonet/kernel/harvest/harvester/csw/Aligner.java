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

import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.CswOperation;
import org.fao.geonet.csw.common.CswServer;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.requests.CatalogRequest;
import org.fao.geonet.csw.common.requests.GetRecordByIdRequest;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.Privileges;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.util.IDFactory;
import org.jdom.Element;

import java.util.List;
import java.util.Set;

//=============================================================================

public class Aligner
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Aligner(Logger log, ServiceContext sc, Dbms dbms, CswServer server, CswParams params) throws OperationAbortedEx
	{
		this.log        = log;
		this.context    = sc;
		this.dbms       = dbms;
		this.server     = server;
		this.params     = params;

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		dataMan = gc.getDataManager();
		result  = new CswResult();

		//--- setup get-record-by-id request

		request = new GetRecordByIdRequest(sc);
		request.setElementSetName(ElementSetName.FULL);

		CswOperation oper = server.getOperation(CswServer.GET_RECORD_BY_ID);

		// Use the preferred HTTP method and check one exist.
		if (oper.getUrl != null && Harvester.PREFERRED_HTTP_METHOD.equals("GET")) {
			request.setUrl(oper.getUrl);
			request.setMethod(CatalogRequest.Method.GET);
		} else if (oper.postUrl != null && Harvester.PREFERRED_HTTP_METHOD.equals("POST")) {
			request.setUrl(oper.postUrl);
			request.setMethod(CatalogRequest.Method.POST);
		} else {
			if (oper.getUrl != null) {
				request.setUrl(oper.getUrl);
				request.setMethod(CatalogRequest.Method.GET);
			} else if (oper.getUrl != null) {
				request.setUrl(oper.postUrl);
				request.setMethod(CatalogRequest.Method.POST);
			} else {
				throw new OperationAbortedEx("No GET or POST DCP available in this service.");
			}
		}

		if(oper.preferredOutputSchema != null) {
			request.setOutputSchema(oper.preferredOutputSchema);
		}

        if(oper.preferredServerVersion != null) {
			request.setServerVersion(oper.preferredServerVersion);
		}

		if (params.useAccount) {
			request.setCredentials(params.username, params.password);
		}	
		
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

                if(log.isDebugEnabled())
                    log.debug("  - Removing old metadata with local id:"+ id);
				dataMan.deleteMetadata(context, dbms, id);
				dbms.commit();
				result.locallyRemoved++;
			}

		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for(RecordInfo ri : records)
		{
			result.totalMetadata++;

			String id = dataMan.getMetadataId(dbms, ri.uuid);

			if (id == null)	addMetadata(ri);
			else				updateMetadata(ri, id);
		}

		log.info("End of alignment for : "+ params.name);

		return result;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : addMetadata
	//---
	//--------------------------------------------------------------------------

	private void addMetadata(RecordInfo ri) throws Exception
	{
		Element md = retrieveMetadata(ri.uuid);

		if (md == null)
			return;

		String schema = dataMan.autodetectSchema(md);

		if (schema == null)
		{
            if(log.isDebugEnabled())
                log.debug("  - Metadata skipped due to unknown schema. uuid:"+ ri.uuid);
			result.unknownSchema++;

			return;
		}

        if(log.isDebugEnabled())
            log.debug("  - Adding metadata with remote uuid:"+ ri.uuid + " schema:" + schema);

        //
        // insert metadata
        //
        String userid = "1";
        String group = null, isTemplate = null, docType = null, title = null, category = null;
        boolean ufo = false, indexImmediate = false;
        String id = IDFactory.newID();
        dataMan.insertMetadata(context, dbms, schema, md, id, ri.uuid, userid, group, params.uuid, isTemplate, docType,
                title, category, ri.changeDate, ri.changeDate, ufo, indexImmediate);

		dataMan.setTemplateExt(dbms, id, "n", null);
		dataMan.setHarvestedExt(dbms, id, params.uuid);

		addPrivileges(id);
		addCategories(id);

		dbms.commit();
        boolean workspace = false;
        dataMan.indexMetadataGroup(dbms, id, workspace, true);
		result.addedMetadata++;
	}

	//--------------------------------------------------------------------------
	//--- Categories
	//--------------------------------------------------------------------------

	private void addCategories(String id) throws Exception
	{
		for(String catId : params.getCategories())
		{
			String name = localCateg.getName(catId);

			if (name == null)
                if(log.isDebugEnabled())
                    log.debug("    - Skipping removed category with id:"+ catId);
			else
			{
                if(log.isDebugEnabled())
                    log.debug("    - Setting category : "+ name);
				dataMan.setCategory(context, dbms, id, catId);
			}
		}
	}

	//--------------------------------------------------------------------------
	//--- Privileges
	//--------------------------------------------------------------------------

	private void addPrivileges(String id) throws Exception
	{
		for (Privileges priv : params.getPrivileges())
		{
			String name = localGroups.getName(priv.getGroupId());

			if (name == null)
                if(log.isDebugEnabled())
                    log.debug("    - Skipping removed group with id:"+ priv.getGroupId());
			else
			{
                if(log.isDebugEnabled())
                    log.debug("    - Setting privileges for group : "+ name);

				for (String opId: priv.getOperations()) {
					name = dataMan.getAccessManager().getPrivilegeName(opId);

					//--- allow only: view, dynamic, featured
					if (opId.equals("0") || opId.equals("5") || opId.equals("6"))
					{
                        if(log.isDebugEnabled())
                            log.debug("       --> "+ name);
						dataMan.setOperation(context, dbms, id, priv.getGroupId(), opId +"");
					}
					else
                    if(log.isDebugEnabled())
                        log.debug("       --> "+ name +" (skipped)");
				}
			}
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : updateMetadata
	//---
	//--------------------------------------------------------------------------

	private void updateMetadata(RecordInfo ri, String id) throws Exception
	{
		String date = localUuids.getChangeDate(ri.uuid);

		if (date == null)
            if(log.isDebugEnabled())
                log.debug("  - Skipped metadata managed by another harvesting node. uuid:"+ ri.uuid +", name:"+ params.name);
		else
		{
			if (!ri.isMoreRecentThan(date))
			{
                if(log.isDebugEnabled())
                    log.debug("  - Metadata XML not changed for uuid:"+ ri.uuid);
				result.unchangedMetadata++;
			}
			else
			{
                if(log.isDebugEnabled())
                    log.debug("  - Updating local metadata for uuid:"+ ri.uuid);

				Element md = retrieveMetadata(ri.uuid);

				if (md == null)
					return;

                //
                // update metadata
                //
                boolean validate = false;
                boolean ufo = false;
                boolean index = false;
                String language = context.getLanguage();
				dataMan.updateMetadata(context, dbms, id, md, validate, ufo, index, language, ri.changeDate, false);

				dbms.execute("DELETE FROM OperationAllowed WHERE metadataId=?", id);
				addPrivileges(id);

				dbms.execute("DELETE FROM MetadataCateg WHERE metadataId=?", id);
				addCategories(id);

				dbms.commit();
                boolean workspace = false;
                dataMan.indexMetadataGroup(dbms, id, workspace, true);
				result.updatedMetadata++;
			}
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

    /**
     *  Returns true if the uuid is present in the remote node.
     * 
     * @param records
     * @param uuid
     * @return
     */
	private boolean exists(Set<RecordInfo> records, String uuid)
	{
		for(RecordInfo ri : records)
			if (uuid.equals(ri.uuid))
				return true;

		return false;
	}

	//--------------------------------------------------------------------------

	/**
	 * Does CSW GetRecordById request. If validation is requested and the metadata
     * does not validate, null is returned.
     *
     * @param uuid uuid of metadata to request
     * @return metadata the metadata
	 */
	private Element retrieveMetadata(String uuid)
	{
		request.clearIds();
		request.addId(uuid);

		try
		{
            if(log.isDebugEnabled())
                log.debug("Getting record from : "+ request.getHost() +" (uuid:"+ uuid +")");
			Element response = request.execute();
            if(log.isDebugEnabled())
                log.debug("Record got:\n"+Xml.getString(response));

			List list = response.getChildren();

			//--- maybe the metadata has been removed

			if (list.size() == 0)
				return null;

			response = (Element) list.get(0);
			response = (Element) response.detach();

            // validate it here if requested
            if (params.validate) {
                if(!dataMan.validate(response))  {
                    log.info("Ignoring invalid metadata with uuid " + uuid);
                    result.doesNotValidate++;
                    return null;
                }
            }
            return response;
		}
		catch(Exception e)
		{
			log.warning("Raised exception while getting record : "+ e);
			e.printStackTrace();
			result.unretrievable++;

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
	private CswParams      params;
	private DataManager    dataMan;
	private CswServer      server;
	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
	private UUIDMapper     localUuids;
	private CswResult      result;
	private GetRecordByIdRequest request;
}

//=============================================================================


