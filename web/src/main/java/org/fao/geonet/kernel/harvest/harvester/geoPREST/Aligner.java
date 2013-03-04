//=============================================================================
//===	Copyright (C) 2001-2013 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.geoPREST;

import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import jeeves.utils.XmlRequest;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.Privileges;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;

import org.jdom.Element;

import java.net.URL;
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

	public Aligner(Logger log, ServiceContext sc, Dbms dbms, GeoPRESTParams params) throws Exception {
		this.log        = log;
		this.context    = sc;
		this.dbms       = dbms;
		this.params     = params;

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		dataMan = gc.getDataManager();
		result  = new GeoPRESTResult();

		//--- setup REST operation rest/document?id={uuid}

		request = new XmlRequest(new URL(params.baseUrl+"/rest/document"));

	}

	//--------------------------------------------------------------------------
	//---
	//--- Alignment method
	//---
	//--------------------------------------------------------------------------

	public GeoPRESTResult align(Set<RecordInfo> records) throws Exception {
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

		for (String uuid : localUuids.getUUIDs()) {
			if (!exists(records, uuid)) {
				String id = localUuids.getID(uuid);

				if(log.isDebugEnabled())
					log.debug("  - Removing old metadata with local id:"+ id);
				dataMan.deleteMetadata(context, dbms, id);
				dbms.commit();
				result.locallyRemoved++;
			}
		}

		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for (RecordInfo ri : records) {
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

	private void addMetadata(RecordInfo ri) throws Exception {
		Element md = retrieveMetadata(ri.uuid);

		if (md == null) return;

		String schema = dataMan.autodetectSchema(md);

		if (schema == null) {
			if (log.isDebugEnabled()) {
				log.debug("  - Metadata skipped due to unknown schema. uuid:"+ ri.uuid);
			}
			result.unknownSchema++;
			return;
		}

		if (log.isDebugEnabled())
			log.debug("  - Adding metadata with remote uuid:"+ ri.uuid + " schema:" + schema);

		//
		// insert metadata
		//
		int userid = 1;
		String group = null, isTemplate = null, docType = null, title = null, category = null;
		boolean ufo = false, indexImmediate = false;
		String id = dataMan.insertMetadata(context, dbms, schema, md, context.getSerialFactory().getSerial(dbms, "Metadata"), ri.uuid, userid, group, params.uuid, isTemplate, docType, title, category, ri.changeDate, ri.changeDate, ufo, indexImmediate);

		int iId = Integer.parseInt(id);

		dataMan.setTemplateExt(dbms, iId, "n", null);
		dataMan.setHarvestedExt(dbms, iId, params.uuid);

		addPrivileges(id);
		addCategories(id);

		dbms.commit();
		dataMan.indexMetadata(dbms, id);
		result.addedMetadata++;
	}

	//--------------------------------------------------------------------------
	//--- Categories
	//--------------------------------------------------------------------------

	private void addCategories(String id) throws Exception {
		for(String catId : params.getCategories()) {
			String name = localCateg.getName(catId);

			if (name == null) {
				if (log.isDebugEnabled()) {
					log.debug("    - Skipping removed category with id:"+ catId);
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("    - Setting category : "+ name);
				}
				dataMan.setCategory(context, dbms, id, catId);
			}
		}
	}

	//--------------------------------------------------------------------------
	//--- Privileges
	//--------------------------------------------------------------------------

	private void addPrivileges(String id) throws Exception {
		for (Privileges priv : params.getPrivileges()) {
			String name = localGroups.getName(priv.getGroupId());
			
			if (name == null) {
				if (log.isDebugEnabled()) {
					log.debug("    - Skipping removed group with id:"+ priv.getGroupId());
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("    - Setting privileges for group : "+ name);
				}
                
				for (int opId: priv.getOperations()) {
					name = dataMan.getAccessManager().getPrivilegeName(opId);

					//--- allow only: view, dynamic, featured
					if (opId == 0 || opId == 5 || opId == 6) {
						if (log.isDebugEnabled()) log.debug("       --> "+ name);
						dataMan.setOperation(context, dbms, id, priv.getGroupId(), opId +"");
					} else {
						if (log.isDebugEnabled()) log.debug("       --> "+ name +" (skipped)");
					}
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

		if (date == null) {
			if (log.isDebugEnabled()) {
				log.debug("  - Skipped metadata managed by another harvesting node. uuid:"+ ri.uuid +", name:"+ params.name);
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("  - Comparing date "+date+" with harvested date "+ri.changeDate+" Comparison: "+ri.isMoreRecentThan(date));
			}
			if (!ri.isMoreRecentThan(date)) {
 				if (log.isDebugEnabled()) {
					log.debug("  - Metadata XML not changed for uuid:"+ ri.uuid);
				}
				result.unchangedMetadata++;
			} else {
				if (log.isDebugEnabled()) {
					log.debug("  - Updating local metadata for uuid:"+ ri.uuid);
				}
				Element md = retrieveMetadata(ri.uuid);

				if (md == null) return;
				
				//
				// update metadata
				//
				boolean validate = false;
				boolean ufo = false;
				boolean index = false;
				String language = context.getLanguage();
				dataMan.updateMetadata(context, dbms, id, md, validate, ufo, index, language, ri.changeDate, false);

				dbms.execute("DELETE FROM OperationAllowed WHERE metadataId=?", Integer.parseInt(id));
				addPrivileges(id);

				dbms.execute("DELETE FROM MetadataCateg WHERE metadataId=?", Integer.parseInt(id));
				addCategories(id);

				dbms.commit();
				dataMan.indexMetadata(dbms, id);
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
		for(RecordInfo ri : records) {
			if (uuid.equals(ri.uuid)) return true;
		}

		return false;
	}

	//--------------------------------------------------------------------------

	/**
	 * Does REST document request. If validation is requested and the metadata
   * does not validate, null is returned. If transformation is requested then
	 * metadata is transformed.
   *
   * @param uuid uuid of metadata to request
   * @return metadata the metadata
	 */
	private Element retrieveMetadata(String uuid)
	{
		request.clearParams();
		//request.addParam("id","{"+uuid+"}");
		request.addParam("id",uuid);

		try
		{
			if (log.isDebugEnabled())
                log.debug("Getting record from : "+ request.getHost() +" (uuid:"+ uuid +")");
			Element response = null;
			try {
				response = request.execute();
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Getting record from GeoPortal REST raised exception: "+e.getMessage());
				log.error("Sent request "+request.getSentData());
				if (response != null) log.error("Received:\n"+Xml.getString(response));
				throw new Exception(e);
			}

			if(log.isDebugEnabled()) log.debug("Record got:\n"+Xml.getString(response));

			// validate it here if requested
			if (params.validate) {
				if(!dataMan.validate(response))  {
					log.info("Ignoring invalid metadata with uuid " + uuid);
					result.doesNotValidate++;
					return null;
				}
			}

			// transform it here if requested
			if (!params.importXslt.equals("none")) {
				String thisXslt = context.getAppPath() + Geonet.Path.IMPORT_STYLESHEETS + "/";
				thisXslt = thisXslt + params.importXslt;
				try {
					response = Xml.transform(response, thisXslt);
				} catch (Exception e) {
					log.info("Cannot transform XML " +Xml.getString(response)+", ignoring. Error was: "+e.getMessage());
					result.badFormat++;
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
	private XmlRequest		 request;
	private GeoPRESTParams params;
	private DataManager    dataMan;
	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
	private UUIDMapper     localUuids;
	private GeoPRESTResult      result;
}

//=============================================================================


