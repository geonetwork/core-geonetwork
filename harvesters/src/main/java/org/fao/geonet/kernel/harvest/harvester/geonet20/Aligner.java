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

package org.fao.geonet.kernel.harvest.harvester.geonet20;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;

import java.util.Collection;
import java.util.List;

//=============================================================================

public class Aligner
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Aligner(Logger log, XmlRequest req, GeonetParams params, DataManager dm,
                   ServiceContext sc, CategoryMapper cm)
	{
		this.log        = log;
		this.req        = req;
		this.params     = params;
		this.dataMan    = dm;
		this.context    = sc;
		this.localCateg = cm;
    }

	//--------------------------------------------------------------------------
	//---
	//--- Alignment method
	//---
	//--------------------------------------------------------------------------

	public HarvestResult align(Element result, String siteId) throws Exception
	{
		log.info("Start of alignment for site-id="+ siteId);

		this.result = new HarvestResult();
		this.result.siteId = siteId;

		@SuppressWarnings("unchecked")
        List<Element> mdList = result.getChildren("metadata");

		//-----------------------------------------------------------------------
		//--- retrieve local uuids for given site-id

		localUuids = new UUIDMapper(context.getBean(MetadataRepository.class), siteId);

        //-----------------------------------------------------------------------
        //--- remove old metadata

        for (String uuid : localUuids.getUUIDs())
			if (!exists(mdList, uuid))
			{
                String id = localUuids.getID(uuid);

                if(log.isDebugEnabled()) log.debug("  - Removing old metadata with id="+ id);
                dataMan.deleteMetadata(context, id);

                dataMan.flush();
				this.result.locallyRemoved++;
			}

		//-----------------------------------------------------------------------
		//--- insert/update new metadata

        for (Element aMdList : mdList) {
            Element info = aMdList.getChild("info", Edit.NAMESPACE);

            String remoteId = info.getChildText("id");
            String remoteUuid = info.getChildText("uuid");
            String schema = info.getChildText("schema");
            String changeDate = info.getChildText("changeDate");

            this.result.totalMetadata++;

            if(log.isDebugEnabled()) log.debug("Obtained remote id=" + remoteId + ", changeDate=" + changeDate);

            if (!dataMan.existsSchema(schema)) {
                if(log.isDebugEnabled()) log.debug("  - Skipping unsupported schema : " + schema);
                this.result.schemaSkipped++;
            } else {
                String id = dataMan.getMetadataId(remoteUuid);

                if (id == null) {
                    id = addMetadata(info);
                } else {
                    updateMetadata(siteId, info, id);
                }

                dataMan.flush();



                //--- maybe the metadata was unretrievable

                if (id != null) {
                    dataMan.indexMetadata(id, false);
                }
            }
        }

		log.info("End of alignment for site-id="+ siteId);

		return this.result;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : addMetadata
	//---
	//--------------------------------------------------------------------------

	private String addMetadata(Element info) throws Exception
	{
		String remoteId   = info.getChildText("id");
		String remoteUuid = info.getChildText("uuid");
		String schema     = info.getChildText("schema");
		String createDate = info.getChildText("createDate");
		String changeDate = info.getChildText("changeDate");

        if(log.isDebugEnabled()) log.debug("  - Adding metadata with remote id="+ remoteId);

		Element md = getRemoteMetadata(req, remoteId);

		if (md == null)
		{
			log.warning("  - Cannot get metadata (possibly bad XML) with remote id="+ remoteId);
			return null;
		}

        //
        //  insert metadata
        //
        String group = null, isTemplate = null, docType = null, title = null, category = null;
        boolean ufo = false, indexImmediate = false;
        String id = dataMan.insertMetadata(context, schema, md, params.uuid, Integer.parseInt(params.ownerId), group, remoteUuid,
                         isTemplate, docType, category, createDate, changeDate, ufo, indexImmediate);


		int iId = Integer.parseInt(id);

		dataMan.setTemplate(iId, MetadataType.METADATA, null);
		dataMan.setHarvested(iId, params.uuid);

		result.addedMetadata++;

		@SuppressWarnings("unchecked")
        List<Element> categories = info.getChildren("category");
        addCategories(id, categories);

        addPrivileges(id);

		return id;
	}

	//--------------------------------------------------------------------------
	//--- Categories
	//--------------------------------------------------------------------------

	private void addCategories(String id, List<Element> categ) throws Exception
	{
        for (Element aCateg : categ) {
            String catName = aCateg.getText();
            String catId = localCateg.getID(catName);

            if (catId != null) {
                //--- remote category exists locally

                if(log.isDebugEnabled()) log.debug("    - Setting category : " + catName);
                dataMan.setCategory(context, id, catId);
            }
        }
	}

	//--------------------------------------------------------------------------
	//--- Privileges
	//--------------------------------------------------------------------------

	private void addPrivileges(String id) throws Exception
	{
		//--- set view privilege for both groups 'intranet' and 'all'
		dataMan.setOperation(context, id, "0", "0");
		dataMan.setOperation(context, id, "1", "0");
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : updateMetadata
	//---
	//--------------------------------------------------------------------------

	private void updateMetadata(String siteId, Element info, String id) throws Exception
	{
		String remoteId  = info.getChildText("id");
		String remoteUuid= info.getChildText("uuid");
		String changeDate= info.getChildText("changeDate");

		if (localUuids.getID(remoteUuid) == null)
		{
			log.error("  - Warning! The remote uuid '"+ remoteUuid +"' does not belong to site '"+ siteId+"'");
			log.error("     - The site id of this metadata has been changed.");
			log.error("     - The metadata update will be skipped.");

			result.uuidSkipped++;
		}
		else
		{
			updateMetadata(id, remoteId, remoteUuid, changeDate);
			updateCategories(id, info);
		}
	}

	//--------------------------------------------------------------------------

	private void updateMetadata(String id, String remoteId, String remoteUuid, String changeDate) throws Exception
	{
		String date = localUuids.getChangeDate(remoteUuid);

		if (!updateCondition(date, changeDate)) {
            if(log.isDebugEnabled()) log.debug("  - XML not changed to local metadata with id="+ id);
			result.unchangedMetadata++;
		} else {
            if(log.isDebugEnabled()) log.debug("  - Updating local metadata with id="+ id);

			Element md = getRemoteMetadata(req, remoteId);

			if (md == null) {
				log.warning("  - Cannot get metadata (possibly bad XML) with remote id="+ remoteId);
            } else {
                //
                // update metadata
                //
                boolean validate = false;
                boolean ufo = false;
                boolean index = false;
                String language = context.getLanguage();
                dataMan.updateMetadata(context, id, md, validate, ufo, index, language, changeDate, false);

				result.updatedMetadata++;
			}
		}
	}

	//--------------------------------------------------------------------------

	private void updateCategories(String id, Element info) throws Exception
	{
		@SuppressWarnings("unchecked")
        List<Element> catList = info.getChildren("category");

		//--- remove old categories

		@SuppressWarnings("unchecked")
        Collection<MetadataCategory> locCateg = dataMan.getCategories(id);

        for (MetadataCategory el : locCateg) {
            int catId = el.getId();
            String catName = el.getName();

            if (!existsCategory(catList, catName)) {
                if(log.isDebugEnabled()) {
                    log.debug("  - Unsetting category : " + catName);
                }
                dataMan.unsetCategory(context, id, catId);
            }
        }

		//--- add new categories

        for (Element categ : catList) {
            String catName = categ.getAttributeValue("name");
            String catId = localCateg.getID(catName);

            if (catId != null) {
                //--- it is not necessary to query the db. Anyway...
                if (!dataMan.isCategorySet(id, Integer.valueOf(catId))) {
                    if (log.isDebugEnabled()) {
                        log.debug("  - Setting category : " + catName);
                    }
                    dataMan.setCategory(context, id, catId);
                }
            }
        }
	}

	//--------------------------------------------------------------------------

	private boolean existsCategory(List<Element> catList, String name)
	{
        for (Object aCatList : catList) {
            Element categ = (Element) aCatList;
            String catName = categ.getText();

            if (catName.equals(name)) {
                return true;
            }
        }

		return false;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

    /**
     * Retrieves remote metadata. If validation is requested and the metadata does not validate, returns null.
     *
     * @param req
     * @param id
     * @return
     * @throws Exception
     */
	private Element getRemoteMetadata(XmlRequest req, String id) throws Exception
	{
		req.setAddress(params.getServletPath() +"/srv/en/"+ Geonet.Service.XML_METADATA_GET);
		req.clearParams();
		req.addParam("id", id);

		try
		{
			Element md   = req.execute();
			Element info = md.getChild("info", Edit.NAMESPACE);

			if (info != null)
				info.detach();

            // validate it here if requested
            if (params.validate) {
                if(!dataMan.validate(md))  {
                    log.info("Ignoring invalid metadata");
                    result.doesNotValidate++;
                    return null;
                }
            }
			return md;
		}
		catch(Exception e)
		{
			log.warning("Cannot retrieve remote metadata with id:"+id);
			log.warning(" (C) Error is : "+e.getMessage());

			return null;
		}
	}

	//--------------------------------------------------------------------------
	/** Return true if the sourceId is present in the remote site */

	private boolean exists(List<Element> mdList, String uuid)
	{
        for (Element aMdList : mdList) {
            Element elInfo = aMdList.getChild("info", Edit.NAMESPACE);

            if (uuid.equals(elInfo.getChildText("uuid"))) {
                return true;
            }
        }

		return false;
	}

	//--------------------------------------------------------------------------

	private boolean updateCondition(String localDate, String remoteDate)
	{
		ISODate local = new ISODate(localDate);
		ISODate remote= new ISODate(remoteDate);

		//--- accept if remote date is greater than local date

		return (remote.timeDifferenceInSeconds(local) > 0);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private Logger         log;
	private XmlRequest     req;
	private GeonetParams   params;
	private DataManager    dataMan;
	private ServiceContext context;
	private CategoryMapper localCateg;
    private UUIDMapper     localUuids;
	private HarvestResult result;
}