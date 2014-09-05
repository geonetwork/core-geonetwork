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

package org.fao.geonet.kernel.harvest.harvester.oaipmh;

import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.lib.Lib;
import org.fao.oaipmh.OaiPmh;
import org.fao.oaipmh.exceptions.NoRecordsMatchException;
import org.fao.oaipmh.requests.GetRecordRequest;
import org.fao.oaipmh.requests.ListIdentifiersRequest;
import org.fao.oaipmh.requests.Transport;
import org.fao.oaipmh.responses.GetRecordResponse;
import org.fao.oaipmh.responses.Header;
import org.fao.oaipmh.responses.ListIdentifiersResponse;
import org.fao.oaipmh.util.ISODate;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
class Harvester extends BaseAligner {
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Harvester(Logger log, ServiceContext context, Dbms dbms, OaiPmhParams params)
	{
		this.log    = log;
		this.context= context;
		this.dbms   = dbms;
		this.params = params;

		result = new OaiPmhResult();

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		dataMan = gc.getDataManager();
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public OaiPmhResult harvest() throws Exception
	{
		ListIdentifiersRequest req = new ListIdentifiersRequest();
		req.setSchemaPath(new File(context.getAppPath() + Geonet.SchemaPath.OAI_PMH));

		Transport t = req.getTransport();
		t.setUrl(new URL(params.url));

		if (params.useAccount)
			t.setCredentials(params.username, params.password);

		//--- set the proxy info if necessary
		Lib.net.setupProxy(context, t);

		//--- perform all searches

		Set<RecordInfo> records = new HashSet<RecordInfo>();

		for(Search s : params.getSearches())
			records.addAll(search(req, s));

		if (params.isSearchEmpty())
			records.addAll(search(req, Search.createEmptySearch()));

		log.info("Total records processed in all searches :"+ records.size());

		//--- align local node

		if (records.size() != 0)
			align(t, records);

		return result;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private Set<RecordInfo> search(ListIdentifiersRequest req, Search s) throws OperationAbortedEx
	{
		//--- setup search parameters

		if (s.from.length() != 0)	req.setFrom(new ISODate(s.from));
			else							req.setFrom(null);

		if (s.until.length() != 0)	req.setUntil(new ISODate(s.until));
			else 							req.setUntil(null);

		if (s.set.length() != 0) 	req.setSet(s.set);
			else 							req.setSet(null);

		req.setMetadataPrefix(s.prefix);

		//--- execute request and loop on response

		Set<RecordInfo> records = new HashSet<RecordInfo>();

		log.info("Searching on : "+ params.name);

		try
		{
			ListIdentifiersResponse response = req.execute();

			while (response.hasNext())
			{
				Header h = response.next();

				if (!h.isDeleted())
					records.add(new RecordInfo(h, s.prefix));
			}

			log.info("Records added to result list : "+ records.size());

			return records;
		}
		catch(NoRecordsMatchException e)
		{
			//--- return gracefully
			return records;
		}

		catch(Exception e)
		{
			log.warning("Raised exception when searching : "+ e);
			log.warning(Util.getStackTrace(e));
			throw new OperationAbortedEx("Raised exception when searching", e);
		}
	}

	//---------------------------------------------------------------------------

	private void align(Transport t, Set<RecordInfo> records) throws Exception
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

                if(log.isDebugEnabled()) log.debug("  - Removing old metadata with local id:"+ id);
				dataMan.deleteMetadataGroup(context, dbms, id);
				dbms.commit();
				result.locallyRemoved++;
			}

		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for(RecordInfo ri : records)
		{
			result.total++;

			String id = localUuids.getID(ri.id);

			if (id == null)	addMetadata(t, ri);
			else				updateMetadata(t, ri, id);
		}

		log.info("End of alignment for : "+ params.name);
	}

	//--------------------------------------------------------------------------
	/** Return true if the uuid is present in the remote records */

	private boolean exists(Set<RecordInfo> records, String uuid)
	{
		for(RecordInfo ri : records)
			if (uuid.equals(ri.id))
				return true;

		return false;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : addMetadata
	//---
	//--------------------------------------------------------------------------

	private void addMetadata(Transport t, RecordInfo ri) throws Exception
	{
		Element md = retrieveMetadata(t, ri);

		if (md == null)
			return;

		//--- schema handled check already done

		String schema = dataMan.autodetectSchema(md);

        if(log.isDebugEnabled()) log.debug("  - Adding metadata with remote id : "+ ri.id);

        //
        // insert metadata
        //
        String group = null, isTemplate = null, docType = null, title = null, category = null;
        boolean ufo = false, indexImmediate = false;
        String id = dataMan.insertMetadata(context, dbms, schema, md, context.getSerialFactory().getSerial(dbms, "Metadata"), ri.id, Integer.parseInt(params.ownerId), group, params.uuid,
                         isTemplate, docType, title, category, ri.changeDate.toString(), ri.changeDate.toString(), ufo, indexImmediate);

		int iId = Integer.parseInt(id);

		dataMan.setTemplateExt(dbms, iId, "n", null);
		dataMan.setHarvestedExt(dbms, iId, params.uuid);

        addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, dbms, log);
		addCategories(id);

		dbms.commit();
		dataMan.indexMetadata(dbms, id);
		result.added++;
	}

	//--------------------------------------------------------------------------

	private Element retrieveMetadata(Transport t, RecordInfo ri)
	{
		try
		{
            if(log.isDebugEnabled()) log.debug("  - Getting remote metadata with id : "+ ri.id);

			GetRecordRequest req = new GetRecordRequest();
			req.setSchemaPath(new File(context.getAppPath() + Geonet.SchemaPath.OAI_PMH));
			req.setTransport(t);
			req.setIdentifier(ri.id);
			req.setMetadataPrefix(ri.prefix);

			GetRecordResponse res = req.execute();

			Element md = res.getRecord().getMetadata();

            if(log.isDebugEnabled()) log.debug("    - Record got:\n"+ Xml.getString(md));

			if (isOaiDc(md))
			{
                if(log.isDebugEnabled()) log.debug("    - Converting oai_dc to dublin core");
				md = toDublinCore(md);

				if (md == null)
					return null;
			}

			String schema = dataMan.autodetectSchema(md);

			if (schema == null)
			{
				log.warning("Skipping metadata with unknown schema. Remote id : "+ ri.id);
				result.unknownSchema++;
			}
			else
			{
				if (!params.validate || validates(schema, md))
					return (Element) md.detach();

				log.warning("Skipping metadata that does not validate. Remote id : "+ ri.id);
				result.doesNotValidate++;
			}
		}

		catch(JDOMException e)
		{
			log.warning("Skipping metadata with bad XML format. Remote id : "+ ri.id);
			result.badFormat++;
		}

		catch(Exception e)
		{
			log.warning("Raised exception while getting metadata file : "+ e);
			log.warning(Util.getStackTrace(e));
			result.unretrievable++;
		}

		//--- we don't raise any exception here. Just try to go on
		return null;
	}

	//--------------------------------------------------------------------------

	private boolean isOaiDc(Element md)
	{
		return (md.getName().equals("dc")) && (md.getNamespace().equals(OaiPmh.Namespaces.OAI_DC));
	}

	//--------------------------------------------------------------------------

	private Element toDublinCore(Element md)
	{
		String styleSheet = context.getAppPath() +"conversion/oai_dc-to-dublin-core/main.xsl";

		try
		{
			return Xml.transform(md, styleSheet);
		}
		catch (Exception e)
		{
			log.warning("Cannot convert oai_dc to dublin core : "+ e);
			return null;
		}
	}

	//--------------------------------------------------------------------------

	private boolean validates(String schema, Element md)
	{
		try
		{
			dataMan.validate(schema, md);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
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
			{
                if(log.isDebugEnabled()) log.debug("    - Skipping removed category with id:"+ catId);
			}
			else
			{
                if(log.isDebugEnabled()) log.debug("    - Setting category : "+ name);
				dataMan.setCategory(context, dbms, id, catId);
			}
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : updateMetadata
	//---
	//--------------------------------------------------------------------------

	private void updateMetadata(Transport t, RecordInfo ri, String id) throws Exception
	{
		String date = localUuids.getChangeDate(ri.id);

		if (!ri.isMoreRecentThan(date))
		{
            if(log.isDebugEnabled()) log.debug("  - Metadata XML not changed for remote id : "+ ri.id);
			result.unchanged++;
		}
		else
		{
            if(log.isDebugEnabled()) log.debug("  - Updating local metadata for remote id : "+ ri.id);

			Element md = retrieveMetadata(t, ri);

			if (md == null)
				return;

            //
            // update metadata
            //
            boolean validate = false;
            boolean ufo = false;
            boolean index = false;
            String language = context.getLanguage();
            dataMan.updateMetadata(context, dbms, id, md, validate, ufo, index, language, ri.changeDate.toString(), true);

			//--- the administrator could change privileges and categories using the
			//--- web interface so we have to re-set both

			dbms.execute("DELETE FROM OperationAllowed WHERE metadataId=?", Integer.parseInt(id));
            addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, dbms, log);

			dbms.execute("DELETE FROM MetadataCateg WHERE metadataId=?", Integer.parseInt(id));
			addCategories(id);

			dbms.commit();
			dataMan.indexMetadata(dbms, id);
			result.updated++;
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger         log;
	private ServiceContext context;
	private Dbms           dbms;
	private OaiPmhParams   params;
	private DataManager    dataMan;
	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
	private UUIDMapper     localUuids;
	private OaiPmhResult   result;
}

//=============================================================================


