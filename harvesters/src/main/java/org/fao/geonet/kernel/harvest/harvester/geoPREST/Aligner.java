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

import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.*;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;

import java.net.URL;
import java.util.List;
import java.util.Set;

//=============================================================================

public class Aligner extends BaseAligner
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Aligner(Logger log, ServiceContext sc, GeoPRESTParams params) throws Exception {
		this.log        = log;
		this.context    = sc;
		this.params     = params;

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		dataMan = gc.getBean(DataManager.class);
		result  = new HarvestResult();

		//--- setup REST operation rest/document?id={uuid}

		request = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(new URL(params.baseUrl+"/rest/document"));

	}

	//--------------------------------------------------------------------------
	//---
	//--- Alignment method
	//---
	//--------------------------------------------------------------------------


	public HarvestResult align(Set<RecordInfo> records, List<HarvestError> errors) throws Exception {		log.info("Start of alignment for : "+ params.name);

		//-----------------------------------------------------------------------
		//--- retrieve all local categories and groups
		//--- retrieve harvested uuids for given harvesting node

		localCateg = new CategoryMapper(context);
		localGroups= new GroupMapper(context);
		localUuids = new UUIDMapper(context.getBean(MetadataRepository.class), params.uuid);

        dataMan.flush();

        //-----------------------------------------------------------------------
		//--- remove old metadata

		for (String uuid : localUuids.getUUIDs()) {
			if (!exists(records, uuid)) {
				String id = localUuids.getID(uuid);

				if(log.isDebugEnabled())
					log.debug("  - Removing old metadata with local id:"+ id);
				dataMan.deleteMetadata(context, id);

                dataMan.flush();

                result.locallyRemoved++;
			}
		}

		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for (RecordInfo ri : records) {
		    try {
    			String id = dataMan.getMetadataId(ri.uuid);
    
    			if (id == null)	addMetadata(ri);
    			else				updateMetadata(ri, id);
                result.totalMetadata++;
                
		    }catch (Throwable t) {
                errors.add(new HarvestError(t, log));
                log.error("Unable to process record from csw (" + this.params.name + ")");
                log.error("   Record failed: " + ri.uuid); 
		    }
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

		String schema = dataMan.autodetectSchema(md, null);

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
		String id = dataMan.insertMetadata(context, schema, md, ri.uuid, userid, group, params.uuid, isTemplate, docType, category, ri.changeDate, ri.changeDate, ufo, indexImmediate);

		int iId = Integer.parseInt(id);

		dataMan.setTemplateExt(iId, MetadataType.METADATA);
		dataMan.setHarvestedExt(iId, params.uuid);

        addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);
        addCategories(id, params.getCategories(), localCateg, dataMan, context, log, null);

        dataMan.flush();

        dataMan.indexMetadata(id, false);
		result.addedMetadata++;
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
                final Metadata metadata = dataMan.updateMetadata(context, id, md, validate, ufo, index, language, ri.changeDate, false);

                OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
                repository.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.parseInt(id));
                addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

                metadata.getCategories().clear();
                context.getBean(MetadataRepository.class).save(metadata);
                addCategories(id, params.getCategories(), localCateg, dataMan, context, log, null);

                dataMan.flush();

                dataMan.indexMetadata(id, false);
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
			if (log.isDebugEnabled()) {
                log.debug("Getting record from : " + request.getHost() + " (uuid:" + uuid + ")");
            }
			Element response = null;
			try {
				response = request.execute();
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Getting record from GeoPortal REST raised exception: "+e.getMessage());
				log.error("Sent request "+request.getSentData());
				throw new Exception(e);
			}

			if(log.isDebugEnabled()) {
                log.debug("Record got:\n" + Xml.getString(response));
            }

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
	private XmlRequest		 request;
	private GeoPRESTParams params;
	private DataManager    dataMan;
	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
	private UUIDMapper     localUuids;
	private HarvestResult result;
}

//=============================================================================


