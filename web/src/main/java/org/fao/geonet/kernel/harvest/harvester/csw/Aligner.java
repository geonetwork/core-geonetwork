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
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class Aligner extends BaseAligner {
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
			} else if (oper.postUrl != null) {
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
        String group = null, isTemplate = null, docType = null, title = null, category = null;
        boolean ufo = false, indexImmediate = false;
        String id = dataMan.insertMetadata(context, dbms, schema, md, context.getSerialFactory().getSerial(dbms, "Metadata"), ri.uuid, Integer.parseInt(params.ownerId), group, params.uuid,
                         isTemplate, docType, title, category, ri.changeDate, ri.changeDate, ufo, indexImmediate);

		int iId = Integer.parseInt(id);

		dataMan.setTemplateExt(dbms, iId, "n", null);
		dataMan.setHarvestedExt(dbms, iId, params.uuid);

		addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, dbms, log);
		addCategories(id);

		dbms.commit();
		dataMan.indexMetadata(dbms, id);
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

			if (name == null) {
                if(log.isDebugEnabled()) {
                    log.debug("    - Skipping removed category with id:"+ catId);
                }
			} else {
                if(log.isDebugEnabled()) {
                    log.debug("    - Setting category : "+ name);
                }
				dataMan.setCategory(context, dbms, id, catId);
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
            if(log.isDebugEnabled()) {
                log.debug("  - Skipped metadata managed by another harvesting node. uuid:"+ ri.uuid +", name:"+ params.name);
            }
		} else {
			if (!ri.isMoreRecentThan(date)) {
                if(log.isDebugEnabled()) {
                    log.debug("  - Metadata XML not changed for uuid:"+ ri.uuid);
                }
				result.unchangedMetadata++;
			} else {
                if(log.isDebugEnabled()) {
                    log.debug("  - Updating local metadata for uuid:"+ ri.uuid);
                }
				Element md = retrieveMetadata(ri.uuid);

				if (md == null) {
					return;
				}
				
                //
                // update metadata
                //
                boolean validate = false;
                boolean ufo = false;
                boolean index = false;
                String language = context.getLanguage();
				dataMan.updateMetadata(context, dbms, id, md, validate, ufo, index, language, ri.changeDate, true);

				dbms.execute("DELETE FROM OperationAllowed WHERE metadataId=?", Integer.parseInt(id));
                addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, dbms, log);

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
            
            if(params.rejectDuplicateResource) {
                if (foundDuplicateForResource(uuid, response)) {
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

    /**
     * Check for metadata in the catalog having the same resource identifier as the
     * harvested record.
     * 
     * If one dataset (same MD_metadata/../identificationInfo/../identifier/../code) 
     * (eg. a NMA layer for roads) is described in 2 or more catalogs with different 
     * metadata uuids. The metadata may be slightly different depending on the author,
     * but the resource is the same. When harvesting, some users would like to have 
     * the capability to exclude "duplicate" description of the same dataset.
     * 
     * The check is made searching the identifier field in the index using 
     * {@link LuceneSearcher#getAllMetadataFromIndexFor(String, String, String, Set, boolean)}
     * 
     * @param uuid the metadata unique identifier
     * @param response  the XML document to check
     * @return true if a record with same resource identifier is found. false otherwise.
     */
    private boolean foundDuplicateForResource(String uuid, Element response) {
        String schema = dataMan.autodetectSchema(response);
        
        if(schema.startsWith("iso19139")) {
            String resourceIdentifierXPath = "gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:identifier/*/gmd:code/gco:CharacterString";
            String resourceIdentifierLuceneIndexField = "identifier";
            String defaultLanguage = "eng";
            
            try {
                // Extract resource identifier
                XPath xp = XPath.newInstance (resourceIdentifierXPath);
                xp.addNamespace("gmd", "http://www.isotc211.org/2005/gmd");
                xp.addNamespace("gco", "http://www.isotc211.org/2005/gco");
                List<Element> resourceIdentifiers = xp.selectNodes(response);
                if (resourceIdentifiers.size() > 0) {
                    // Check if the metadata to import has a resource identifier
                    // existing in current catalog for a record with a different UUID
                    
                    log.debug("  - Resource identifiers found : " + resourceIdentifiers.size());
                    
                    for (Element identifierNode : resourceIdentifiers) {
                        String identifier = identifierNode.getTextTrim();
                        log.debug("    - Searching for duplicates for resource identifier: " + identifier);
                        
                        Map<String, Map<String,String>> values = LuceneSearcher.getAllMetadataFromIndexFor(defaultLanguage, resourceIdentifierLuceneIndexField, 
                                identifier, Collections.singleton("_uuid"), true);
                        log.debug("    - Number of resources with same identifier: " + values.size());
                        for (String key : values.keySet()) {
                            Map<String, String> recordFieldValues = values.get(key);
                            String indexRecordUuid = recordFieldValues.get("_uuid");
                            if (!indexRecordUuid.equals(uuid)) {
                                log.debug("      - UUID " + indexRecordUuid + " in index does not match harvested record UUID " + uuid);
                                log.warning("      - Duplicates found. Skipping record with UUID " + uuid + " and resource identifier " + identifier);
                                
                                result.duplicatedResource ++;
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warning("      - Error when searching for resource duplicate " + uuid + ". Error is: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
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


