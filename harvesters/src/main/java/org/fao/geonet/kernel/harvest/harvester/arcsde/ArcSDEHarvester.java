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
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.*;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
    private BaseAligner aligner = new BaseAligner() {};
	
	static final String ARCSDE_LOG_MODULE_NAME = Geonet.HARVESTER + ".arcsde";
	private static final String ARC_TO_ISO19115_TRANSFORMER = "ArcCatalog8_to_ISO19115.xsl";
	private static final String ISO19115_TO_ISO19139_TRANSFORMER = "ISO19115-to-ISO19139.xsl";
	private static String ARC_TO_ISO19115_TRANSFORMER_LOCATION;
	private static String ISO19115_TO_ISO19139_TRANSFORMER_LOCATION;

    @Override
	protected void storeNodeExtra(AbstractParams params, String path, String siteId, String optionsId) throws SQLException {
		ArcSDEParams as = (ArcSDEParams) params;
        super.setParams(as);
		settingMan.add("id:"+siteId, "icon", as.icon);
		settingMan.add("id:"+siteId, "server", as.server);
		settingMan.add("id:"+siteId, "port", as.port);
		settingMan.add("id:"+siteId, "username", as.username);
		settingMan.add("id:"+siteId, "password", as.password);
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
			params.uuid = UUID.randomUUID().toString();
		
			String id = settingMan.add("harvesting", "node", getType());
			storeNode(params, "id:"+id);

        Source source = new Source(params.uuid, params.name, true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.uuid);
			
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
		ArcSDEMetadataAdapter adapter = new ArcSDEMetadataAdapter(params.server, params.port, params.database, params.username, params.password);
		List<String> metadataList = adapter.retrieveMetadata();
		align(metadataList);
		Log.info(ARCSDE_LOG_MODULE_NAME, "ArcSDE harvest finished");
	}
	
	private void align(List<String> metadataList) throws Exception {
	    Log.info(ARCSDE_LOG_MODULE_NAME, "Start of alignment for : "+ params.name);
		result = new HarvestResult();
		//----------------------------------------------------------------
		//--- retrieve all local categories and groups
		//--- retrieve harvested uuids for given harvesting node
		CategoryMapper localCateg = new CategoryMapper(context);
		GroupMapper localGroups = new GroupMapper(context);

        dataMan.flush();

        List<String> idsForHarvestingResult = new ArrayList<String>();
		//-----------------------------------------------------------------------
		//--- insert/update metadata		
		for(String metadata : metadataList) {
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

                    // validate it here if requested
                    if (params.validate) {
                        if(!dataMan.validate(iso19139))  {
                            Log.info(ARCSDE_LOG_MODULE_NAME, "Ignoring invalid metadata with uuid " + uuid);
                            result.doesNotValidate++;
                            continue;
                        }
                    }

					//
					// add / update the metadata from this harvesting result
					//
					String id = dataMan.getMetadataId(uuid);
					if (id == null)	{
					    Log.info(ARCSDE_LOG_MODULE_NAME, "adding new metadata");
						id = addMetadata(iso19139, uuid, schema, localGroups, localCateg);
						result.addedMetadata++;
					} else {
					    Log.info(ARCSDE_LOG_MODULE_NAME, "updating existing metadata, id is: " + id);
						updateMetadata(iso19139, id, localGroups, localCateg);
						result.updatedMetadata++;
					}
					idsForHarvestingResult.add(id);
				}
			}
		}
		//
		// delete locally existing metadata from the same source if they were
		// not in this harvesting result
		//	
        List<Metadata> existingMetadata = context.getBean(MetadataRepository.class).findAllByHarvestInfo_Uuid(params.uuid);
        for(Metadata existingId : existingMetadata) {
            String ex$ = String.valueOf(existingId.getId());
			if(!idsForHarvestingResult.contains(ex$)) {
				dataMan.deleteMetadataGroup(context, ex$);
				result.locallyRemoved++;
			}
		}			
	}

	private void updateMetadata(Element xml, String id, GroupMapper localGroups, CategoryMapper localCateg) throws Exception {
	    Log.info(ARCSDE_LOG_MODULE_NAME, "  - Updating metadata with id: "+ id);
        //
        // update metadata
        //
        boolean validate = false;
        boolean ufo = false;
        boolean index = false;
        String language = context.getLanguage();
        final Metadata metadata = dataMan.updateMetadata(context, id, xml, validate, ufo, index, language, new ISODate().toString(),
                false);

        OperationAllowedRepository operationAllowedRepository = context.getBean(OperationAllowedRepository.class);
        operationAllowedRepository.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.parseInt(id));
        aligner.addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

        metadata.getCategories().clear();

        context.getBean(MetadataRepository.class).save(metadata);
        aligner.addCategories(id, params.getCategories(), localCateg, dataMan, context, log, null);

        dataMan.flush();

        dataMan.indexMetadata(id, false);
	}
	/**
	 * Inserts a metadata into the database. Lucene index is updated after insertion.
	 * @param xml
	 * @param uuid
	 * @param schema
	 * @param localGroups
	 * @param localCateg
	 * @throws Exception
	 */
	private String addMetadata(Element xml, String uuid, String schema, GroupMapper localGroups, CategoryMapper localCateg) throws Exception {
	    Log.info(ARCSDE_LOG_MODULE_NAME, "  - Adding metadata with remote uuid: "+ uuid);

        //
        // insert metadata
        //
        String source = params.uuid;
        String createDate = new ISODate().toString();
        String docType = null, title = null, isTemplate = null, group = null, category = null;
        boolean ufo = false, indexImmediate = false;
        String id = dataMan.insertMetadata(context, schema, xml, uuid, Integer.parseInt(params.ownerId), group, source,
                         isTemplate, docType, category, createDate, createDate, ufo, indexImmediate);



		int iId = Integer.parseInt(id);
		dataMan.setTemplateExt(iId, MetadataType.METADATA);
		dataMan.setHarvestedExt(iId, source);

        aligner.addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);
        aligner.addCategories(id, params.getCategories(), localCateg, dataMan, context, log, null);

        dataMan.flush();

        dataMan.indexMetadata(id, false);
		return id;
	}
	
	@Override
	protected void doInit(Element entry, ServiceContext context) throws BadInputEx {
        synchronized (ArcSDEHarvester.class) {
            if (ARC_TO_ISO19115_TRANSFORMER_LOCATION == null) {
                ARC_TO_ISO19115_TRANSFORMER_LOCATION = context.getAppPath() + Geonet.Path.STYLESHEETS + "/conversion/import/" + ARC_TO_ISO19115_TRANSFORMER;
                ISO19115_TO_ISO19139_TRANSFORMER_LOCATION = context.getAppPath() + Geonet.Path.STYLESHEETS + "/conversion/import/" + ISO19115_TO_ISO19139_TRANSFORMER;
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

        Source source = new Source(copy.uuid, copy.name, true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.uuid);
		
		params = copy;
        super.setParams(params);
	}

}
