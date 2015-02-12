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

import jeeves.server.context.ServiceContext;
import org.eclipse.emf.common.command.AbortExecutionException;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.fao.oaipmh.OaiPmh;
import org.fao.oaipmh.exceptions.NoRecordsMatchException;
import org.fao.oaipmh.requests.GetRecordRequest;
import org.fao.oaipmh.requests.ListIdentifiersRequest;
import org.fao.oaipmh.responses.GetRecordResponse;
import org.fao.oaipmh.responses.Header;
import org.fao.oaipmh.responses.ListIdentifiersResponse;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

//=============================================================================

class Harvester extends BaseAligner implements IHarvester<HarvestResult>
{
	private HarvestResult result;
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, OaiPmhParams params)
	{
        super(cancelMonitor);
		this.log    = log;
		this.context= context;
		this.params = params;

		result = new HarvestResult();

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		dataMan = gc.getBean(DataManager.class);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public HarvestResult harvest(Logger log) throws Exception {

	    this.log = log;

		ListIdentifiersRequest req = new ListIdentifiersRequest(context.getBean(GeonetHttpRequestFactory.class));
		req.setSchemaPath(context.getAppPath().resolve(Geonet.SchemaPath.OAI_PMH));

        XmlRequest t = req.getTransport();
		try {
			t.setUrl(new URL(params.url));
        } catch (MalformedURLException e1) {
            HarvestError harvestError = new HarvestError(e1, log);
            harvestError.setDescription(harvestError.getDescription() + " " + params.url);
            errors.add(harvestError);
            throw new AbortExecutionException(e1);
        }

		if (params.isUseAccount()) {
            t.setCredentials(params.getUsername(), params.getPassword());
        }

		//--- set the proxy info if necessary
		Lib.net.setupProxy(context, t);

		//--- perform all searches

		Set<RecordInfo> records = new HashSet<RecordInfo>();

        for (Search s : params.getSearches()) {

            if (cancelMonitor.get()) {
                return this.result;
            }

            try {
                records.addAll(search(req, s));
            } catch (Exception e) {
                log.error("Unknown error trying to harvest");
                log.error(e.getMessage());
                e.printStackTrace();
                errors.add(new HarvestError(e, log));
            } catch (Throwable e) {
                log.fatal("Something unknown and terrible happened while harvesting");
                log.fatal(e.getMessage());
                e.printStackTrace();
                errors.add(new HarvestError(e, log));
            }
        }

        if (params.isSearchEmpty()) {
            try {
                log.debug("Doing an empty search");
                records.addAll(search(req, Search.createEmptySearch()));
            } catch(Exception e) {
                log.error("Unknown error trying to harvest");
                log.error(e.getMessage());
                e.printStackTrace();
                errors.add(new HarvestError(e, log));
            } catch(Throwable e) {
                log.fatal("Something unknown and terrible happened while harvesting");
                log.fatal(e.getMessage());
                e.printStackTrace();
                errors.add(new HarvestError(e, log));
            }
        }

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

        if (s.from.length() != 0) req.setFrom(new ISODate(s.from));
        else req.setFrom(null);

        if (s.until.length() != 0) req.setUntil(new ISODate(s.until));
        else req.setUntil(null);

        if (s.set.length() != 0) req.setSet(s.set);
        else req.setSet(null);

        req.setMetadataPrefix(s.prefix);

        //--- execute request and loop on response

        Set<RecordInfo> records = new HashSet<RecordInfo>();

        log.info("Searching on : " + params.getName());

        try {
            ListIdentifiersResponse response = req.execute();

            while (response.hasNext()) {
                if (cancelMonitor.get()) {
                    return Collections.emptySet();
                }

                Header h = response.next();

                if (!h.isDeleted())
                    records.add(new RecordInfo(h, s.prefix));
            }

            log.info("Records added to result list : " + records.size());

            return records;
        } catch (NoRecordsMatchException e) {
            log.warning("No records were matched: " + e.getMessage());
            this.errors.add(new HarvestError(e, log));
            return records;
        } catch (Exception e) {
            log.warning("Raised exception when searching : " + e);
            log.warning(Util.getStackTrace(e));
            this.errors.add(new HarvestError(e, log));
            throw new OperationAbortedEx("Raised exception when searching", e);
        }
    }

	//---------------------------------------------------------------------------

	private void align(XmlRequest t, Set<RecordInfo> records) throws Exception
	{
		log.info("Start of alignment for : "+ params.getName());

		//-----------------------------------------------------------------------
		//--- retrieve all local categories and groups
		//--- retrieve harvested uuids for given harvesting node

		localCateg = new CategoryMapper(context);
		localGroups= new GroupMapper(context);
		localUuids = new UUIDMapper(context.getBean(MetadataRepository.class), params.getUuid());

        dataMan.flush();

        //-----------------------------------------------------------------------
		//--- remove old metadata

		for (String uuid : localUuids.getUUIDs()) {

            if (cancelMonitor.get()) {
                return;
            }

            if (!exists(records, uuid)) {
                String id = localUuids.getID(uuid);

                if (log.isDebugEnabled()) log.debug("  - Removing old metadata with local id:" + id);
                dataMan.deleteMetadataGroup(context, id);

                dataMan.flush();

                result.locallyRemoved++;
            }
        }
		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for(RecordInfo ri : records) {

            if (cancelMonitor.get()) {
                return ;
            }

            result.totalMetadata++;

			String id = localUuids.getID(ri.id);

			if (id == null)	addMetadata(t, ri);
			else				updateMetadata(t, ri, id);
		}

		log.info("End of alignment for : "+ params.getName());
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

	private void addMetadata(XmlRequest t, RecordInfo ri) throws Exception
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
        Metadata metadata = new Metadata().setUuid(ri.id);
        metadata.getDataInfo().
                setSchemaId(schema).
                setRoot(md.getQualifiedName()).
                setType(MetadataType.METADATA).
                setChangeDate(ri.changeDate).
                setCreateDate(ri.changeDate);
        metadata.getSourceInfo().
                setSourceId(params.getUuid()).
                setOwner(Integer.parseInt(params.getOwnerId()));
        metadata.getHarvestInfo().
                setHarvested(true).
                setUuid(params.getUuid());

        addCategories(metadata, params.getCategories(), localCateg, context, log, null, false);

        metadata = dataMan.insertMetadata(context, metadata, md, true, false, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

        dataMan.flush();

        dataMan.indexMetadata(id, true);
		result.addedMetadata++;
	}

	//--------------------------------------------------------------------------

	private Element retrieveMetadata(XmlRequest transport, RecordInfo ri)
	{
		try
		{
            if(log.isDebugEnabled()) log.debug("  - Getting remote metadata with id : "+ ri.id);

			GetRecordRequest req = new GetRecordRequest(transport);
			req.setSchemaPath(context.getAppPath().resolve(Geonet.SchemaPath.OAI_PMH));

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

			String schema = dataMan.autodetectSchema(md, null);

			if (schema == null)
			{
				log.warning("Skipping metadata with unknown schema. Remote id : "+ ri.id);
				result.unknownSchema++;
			}
			else
			{

                try {
                    params.getValidate().validate(dataMan, context, md);
                    return (Element) md.detach();
                } catch (Exception e) {
                    log.info("Skipping metadata that does not validate. Remote id : "+ ri.id);
                    result.doesNotValidate++;
                }
			}
		}

		catch(JDOMException e)
		{
            HarvestError harvestError = new HarvestError(e, log);
            harvestError.setDescription("Skipping metadata with bad XML format. Remote id : "+ ri.id);
            harvestError.printLog(log);
            this.errors.add(harvestError);
			result.badFormat++;
		}

		catch(Exception e)
		{
            HarvestError harvestError = new HarvestError(e, log);
            harvestError.setDescription("Raised exception while getting metadata file : "+ e);
            this.errors.add(harvestError);
            harvestError.printLog(log);
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
		Path styleSheet = context.getAppPath().resolve("conversion/oai_dc-to-dublin-core/main.xsl");

		try
		{
			return Xml.transform(md, styleSheet);
		}
		catch (Exception e)
		{
            HarvestError harvestError = new HarvestError(e, log);
            harvestError.setDescription("Cannot convert oai_dc to dublin core : "+ e);
            this.errors.add(harvestError);
            harvestError.printLog(log);
			return null;
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : updateMetadata
	//---
	//--------------------------------------------------------------------------

	private void updateMetadata(XmlRequest t, RecordInfo ri, String id) throws Exception
	{
		String date = localUuids.getChangeDate(ri.id);

		if (!ri.isMoreRecentThan(date))
		{
            if(log.isDebugEnabled()) log.debug("  - Metadata XML not changed for remote id : "+ ri.id);
			result.unchangedMetadata++;
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
            final Metadata metadata = dataMan.updateMetadata(context, id, md, validate, ufo, index, language, ri.changeDate.toString(),
                    true);

            //--- the administrator could change privileges and categories using the
			//--- web interface so we have to re-set both

            OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
            repository.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.parseInt(id));
            addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

            metadata.getCategories().clear();
            addCategories(metadata, params.getCategories(), localCateg, context, log, null, true);

            dataMan.flush();
            dataMan.indexMetadata(id, true);
			result.updatedMetadata++;
		}
	}


    public List<HarvestError> getErrors() {
        return errors;
    }

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger         log;
	private ServiceContext context;
	private OaiPmhParams   params;
	private DataManager    dataMan;
	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
	private UUIDMapper     localUuids;
    /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    private List<HarvestError> errors = new LinkedList<HarvestError>();
}

//=============================================================================


