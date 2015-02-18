//=============================================================================
//===	Copyright (C) 2001-2009 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.harvest.harvester.arcsde;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.Logger;
import org.fao.geonet.arcgis.ArcSDEMetadataAdapter;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import com.google.common.collect.Sets;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 
 * Harvester from ArcSDE. Requires the propietary ESRI libraries containing their API. Since those are not
 * committed to our svn, you'll need to replace the dummy library arcsde-dummy.jar with the real ones for this
 * to work.
 * 
 * @author heikki doeleman
 *
 */
public class ArcSDEHarvester extends AbstractHarvester<HarvestResult> {

	private ArcSDEParams params;
    //FIXME use custom class?

	static final String ARCSDE_LOG_MODULE_NAME = Geonet.HARVESTER + ".arcsde";
	private static final String ARC_TO_ISO19115_TRANSFORMER = "ArcCatalog8_to_ISO19115.xsl";
	private static final String ISO19115_TO_ISO19139_TRANSFORMER = "ISO19115-to-ISO19139.xsl";
	private static Path ARC_TO_ISO19115_TRANSFORMER_LOCATION;
	private static Path ISO19115_TO_ISO19139_TRANSFORMER_LOCATION;

    @Override
	protected void storeNodeExtra(AbstractParams params, String path, String siteId, String optionsId) throws SQLException {
		ArcSDEParams as = (ArcSDEParams) params;
        super.setParams(as);
		settingMan.add("id:"+siteId, "icon", as.icon);
		settingMan.add("id:"+siteId, "server", as.server);
		settingMan.add("id:"+siteId, "port", as.port);
		settingMan.add("id:"+siteId, "username", as.getUsername());
		settingMan.add("id:"+siteId, "password", as.getPassword());
		settingMan.add("id:"+siteId, "database", as.database);
	}
	
	@Override
	protected String doAdd(Element node) throws BadInputEx, SQLException {
	/*	try {
			@SuppressWarnings("unused")
			int test = GeoToolsDummyAPI.DUMMY_API_VERSION;
			// if you get here, you're using the dummy API
			System.out.println("ERROR: NO ARCSDE LIBRARIES INSTALLED");
			System.out.println("Replace arcsde-dummy.jar with the real ArcSDE libraries from ESRI");
			System.err.println("ERROR: NO ARCSDE LIBRARIES INSTALLED");
			System.err.println("Replace arcsde-dummy.jar with the real ArcSDE libraries from ESRI");
			return null;
		}
		catch(NoClassDefFoundError n) {
	*/		// using the real ESRI ArcSDE libraries : continue		
			params = new ArcSDEParams(dataMan);
        super.setParams(params);
		
			//--- retrieve/initialize information
			params.create(node);
		
			//--- force the creation of a new uuid
			params.setUuid(UUID.randomUUID().toString());
		
			String id = settingMan.add("harvesting", "node", getType());
			storeNode(params, "id:"+id);

        Source source = new Source(params.getUuid(), params.getName(), params.getTranslations(), true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.getUuid());
			
			return id;
	//	}
	}

	@Override
	public Element getResult() {
		Element res  = new Element("result");

		if (result != null) {
			add(res, "total",          result.totalMetadata);
			add(res, "added",          result.addedMetadata);
			add(res, "updated",        result.updatedMetadata);
			add(res, "unchanged",      result.unchangedMetadata);
			add(res, "unknownSchema",  result.unknownSchema);
			add(res, "removed",        result.locallyRemoved);
			add(res, "unretrievable",  result.unretrievable);
			add(res, "badFormat",      result.badFormat);
			add(res, "doesNotValidate",result.doesNotValidate);
		}

		return res;
	}

	@Override
    public void doHarvest(Logger l) throws Exception {
	    Log.info(ARCSDE_LOG_MODULE_NAME, "ArcSDE harvest starting");
		ArcSDEMetadataAdapter adapter = new ArcSDEMetadataAdapter(params.server, params.port, params.database, params.getUsername(), params.getPassword());
		List<String> metadataList = adapter.retrieveMetadata(cancelMonitor);
		align(metadataList);
		Log.info(ARCSDE_LOG_MODULE_NAME, "ArcSDE harvest finished");
	}
	
	private void align(List<String> metadataList) throws Exception {
	    Log.info(ARCSDE_LOG_MODULE_NAME, "Start of alignment for : "+ params.getName());
		result = new HarvestResult();
		//----------------------------------------------------------------
		//--- retrieve all local categories and groups
		//--- retrieve harvested uuids for given harvesting node
		CategoryMapper localCateg = new CategoryMapper(context);
		GroupMapper localGroups = new GroupMapper(context);

        dataMan.flush();

        List<Integer> idsForHarvestingResult = new ArrayList<Integer>();
		//-----------------------------------------------------------------------
		//--- insert/update metadata		
		for(String metadata : metadataList) {
            if (cancelMonitor.get()) {
                return;
            }
			result.totalMetadata++;
			// create JDOM element from String-XML
			Element metadataElement = Xml.loadString(metadata, false);
			// transform ESRI output to ISO19115
			Element iso19115 = Xml.transform(metadataElement, ARC_TO_ISO19115_TRANSFORMER_LOCATION);
			// transform ISO19115 to ISO19139
			Element iso19139 = Xml.transform(iso19115, ISO19115_TO_ISO19139_TRANSFORMER_LOCATION);
			
			String schema = dataMan.autodetectSchema(iso19139, null);
			if(schema == null) {
				result.unknownSchema++;
			}
			// the xml is recognizable iso19139 format
			else {
				String uuid = dataMan.extractUUID(schema, iso19139);
				if(uuid == null || uuid.equals("")) {
				    Log.info(ARCSDE_LOG_MODULE_NAME, "Skipping metadata due to failure extracting uuid (uuid null or empty).");
					result.badFormat++;
				} else {

                    try {
                        params.getValidate().validate(dataMan, context, iso19139);
                    } catch (Exception e) {
                        Log.info(ARCSDE_LOG_MODULE_NAME, "Ignoring invalid metadata with uuid " + uuid);
                        result.doesNotValidate++;
                        continue;
                    }

                    BaseAligner aligner = new BaseAligner(cancelMonitor){};
					//
					// add / update the metadata from this harvesting result
					//
					String id = dataMan.getMetadataId(uuid);
					if (id == null)	{
					    Log.info(ARCSDE_LOG_MODULE_NAME, "adding new metadata");
						id = addMetadata(iso19139, uuid, schema, localGroups, localCateg, aligner);
						result.addedMetadata++;
					} else {
					    Log.info(ARCSDE_LOG_MODULE_NAME, "updating existing metadata, id is: " + id);
						updateMetadata(iso19139, id, localGroups, localCateg, aligner);
						result.updatedMetadata++;
					}
					idsForHarvestingResult.add(Integer.valueOf(id));
				}
			}
		}
		//
		// delete locally existing metadata from the same source if they were
		// not in this harvesting result
		//
	    Set<Integer> idsResultHs = Sets.newHashSet(idsForHarvestingResult);
        List<Integer> existingMetadata = context.getBean(MetadataRepository.class).findAllIdsBy(MetadataSpecs.hasHarvesterUuid(params.getUuid()));
        for (Integer existingId : existingMetadata) {

            if (cancelMonitor.get()) {
                return;
            }
            if (!idsResultHs.contains(existingId)) {
                log.debug("  Removing: " + existingId);
                dataMan.deleteMetadata(context, existingId.toString());
                result.locallyRemoved++;
            }
        }
	}

	private void updateMetadata(Element xml, String id, GroupMapper localGroups, final CategoryMapper localCateg, BaseAligner aligner) throws Exception {
	    Log.info(ARCSDE_LOG_MODULE_NAME, "  - Updating metadata with id: "+ id);
        //
        // update metadata
        //
        boolean validate = false;
        boolean ufo = false;
        boolean index = false;
        String language = context.getLanguage();

        String changeDate = null;
        try {
            String schema = dataMan.autodetectSchema(xml);
            changeDate = dataMan.extractDateModified(schema, xml);
        } catch (Exception ex) {
            log.error("ArcSDEHarverter - updateMetadata - can't get metadata modified date for metadata id= " + id +
                    ", using current date for modified date");
            changeDate = new ISODate().toString();
        }

        final Metadata metadata = dataMan.updateMetadata(context, id, xml, validate, ufo, index, language, changeDate,
                true);

        OperationAllowedRepository operationAllowedRepository = context.getBean(OperationAllowedRepository.class);
        operationAllowedRepository.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.parseInt(id));
        aligner.addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

        metadata.getCategories().clear();
        aligner.addCategories(metadata, params.getCategories(), localCateg, context, log, null, true);

        dataMan.flush();

        dataMan.indexMetadata(id, true);
	}
	/**
	 * Inserts a metadata into the database. Lucene index is updated after insertion.
	 * @param xml
	 * @param uuid
	 * @param schema
	 * @param localGroups
	 * @param localCateg
	 * @param aligner
     * @throws Exception
	 */
	private String addMetadata(Element xml, String uuid, String schema, GroupMapper localGroups, final CategoryMapper localCateg,
                               BaseAligner aligner) throws Exception {
	    Log.info(ARCSDE_LOG_MODULE_NAME, "  - Adding metadata with remote uuid: "+ uuid);

        //
        // insert metadata
        //
        ISODate createDate = null;
        try {
            createDate = new ISODate(dataMan.extractDateModified(schema, xml));
        } catch (Exception ex) {
            log.error("ArcSDEHarverter - addMetadata - can't get metadata modified date for metadata with uuid= " +
                    uuid + ", using current date for modified date");
            createDate = new ISODate();
        }

        Metadata metadata = new Metadata().setUuid(uuid);
        metadata.getDataInfo().
                setSchemaId(schema).
                setRoot(xml.getQualifiedName()).
                setType(MetadataType.METADATA).
                setCreateDate(createDate).
                setChangeDate(createDate);
        metadata.getSourceInfo().
                setSourceId(params.getUuid()).
                setOwner(Integer.parseInt(params.getOwnerId()));
        metadata.getHarvestInfo().
                setHarvested(true).
                setUuid(params.getUuid());

        aligner.addCategories(metadata, params.getCategories(), localCateg, context, log, null, false);

        metadata = dataMan.insertMetadata(context, metadata, xml, true, false, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        aligner.addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

        dataMan.indexMetadata(id, true);

        return id;
    }
	
	@Override
	protected void doInit(Element entry, ServiceContext context) throws BadInputEx {
        synchronized (ArcSDEHarvester.class) {
            if (ARC_TO_ISO19115_TRANSFORMER_LOCATION == null) {
                ARC_TO_ISO19115_TRANSFORMER_LOCATION = context.getAppPath().resolve(Geonet.Path.STYLESHEETS).resolve("conversion/import").resolve(ARC_TO_ISO19115_TRANSFORMER);
                ISO19115_TO_ISO19139_TRANSFORMER_LOCATION = context.getAppPath().resolve(Geonet.Path.STYLESHEETS).resolve("conversion/import").resolve(ISO19115_TO_ISO19139_TRANSFORMER);
            }
        }
        params = new ArcSDEParams(dataMan);
        super.setParams(params);
		params.create(entry);
	}

	@Override
	protected void doUpdate(String id, Element node) throws BadInputEx, SQLException {
		ArcSDEParams copy = params.copy();

		//--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(path);

		//--- update database
		storeNode(copy, path);

		//--- we update a copy first because if there is an exception ArcSDEParams
		//--- could be half updated and so it could be in an inconsistent state

        Source source = new Source(copy.getUuid(), copy.getName(), copy.getTranslations(), true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.getUuid());
		
		params = copy;
        super.setParams(params);
	}

}
