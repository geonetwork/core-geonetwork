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

import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.Xml;

import org.fao.geonet.arcgis.ArcSDEMetadataAdapter;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//import com.esri.sde.sdk.GeoToolsDummyAPI;
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
	
	public static void init(ServiceContext context) throws Exception {
		ARC_TO_ISO19115_TRANSFORMER_LOCATION = context.getAppPath() + Geonet.Path.STYLESHEETS + "/conversion/import/" + ARC_TO_ISO19115_TRANSFORMER;
		ISO19115_TO_ISO19139_TRANSFORMER_LOCATION = context.getAppPath() + Geonet.Path.STYLESHEETS + "/conversion/import/" + ISO19115_TO_ISO19139_TRANSFORMER;
	}

	@Override
	protected void storeNodeExtra(Dbms dbms, AbstractParams params, String path, String siteId, String optionsId) throws SQLException {
		ArcSDEParams as = (ArcSDEParams) params;
        super.setParams(as);
		settingMan.add(dbms, "id:"+siteId, "icon", as.icon);
		settingMan.add(dbms, "id:"+siteId, "server", as.server);
		settingMan.add(dbms, "id:"+siteId, "port", as.port);
		settingMan.add(dbms, "id:"+siteId, "username", as.username);
		settingMan.add(dbms, "id:"+siteId, "password", as.password);
		settingMan.add(dbms, "id:"+siteId, "database", as.database);
	}
	
	@Override
	protected String doAdd(Dbms dbms, Element node) throws BadInputEx, SQLException {
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
		
			String id = settingMan.add(dbms, "harvesting", "node", getType());
			storeNode(dbms, params, "id:"+id);
			
			Lib.sources.update(dbms, params.uuid, params.name, true);
			Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.uuid);
			
			return id;
	//	}
	}

	@Override
	protected Element getResult() {
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
	protected void doHarvest(Logger l, ResourceManager rm) throws Exception {
	    Log.info(ARCSDE_LOG_MODULE_NAME, "ArcSDE harvest starting");
		ArcSDEMetadataAdapter adapter = new ArcSDEMetadataAdapter(params.server, params.port, params.database, params.username, params.password);
		List<String> metadataList = adapter.retrieveMetadata();
		align(metadataList, rm);
		Log.info(ARCSDE_LOG_MODULE_NAME, "ArcSDE harvest finished");
	}
	
	private void align(List<String> metadataList, ResourceManager rm) throws Exception {
	    Log.info(ARCSDE_LOG_MODULE_NAME, "Start of alignment for : "+ params.name);
		result = new HarvestResult();
		Dbms dbms = (Dbms) rm.open(Geonet.Res.MAIN_DB);
		//----------------------------------------------------------------
		//--- retrieve all local categories and groups
		//--- retrieve harvested uuids for given harvesting node
		CategoryMapper localCateg = new CategoryMapper(dbms);
		GroupMapper localGroups = new GroupMapper(dbms);
		dbms.commit();		
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
				}
				else {

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
					String id = dataMan.getMetadataId(dbms, uuid);
					if (id == null)	{
					    Log.info(ARCSDE_LOG_MODULE_NAME, "adding new metadata");
						id = addMetadata(iso19139, uuid, dbms, schema, localGroups, localCateg);
						result.addedMetadata++;
					}
					else {
					    Log.info(ARCSDE_LOG_MODULE_NAME, "updating existing metadata, id is: " + id);
						updateMetadata(iso19139, id, dbms, localGroups, localCateg);
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
		List<Element> existingMetadata = dataMan.getMetadataByHarvestingSource(dbms, params.uuid);
		for(Element existingId : existingMetadata) {
			String ex$ = existingId.getChildText("id");
			if(!idsForHarvestingResult.contains(ex$)) {
				dataMan.deleteMetadataGroup(context, dbms, ex$);
				result.locallyRemoved++;
			}
		}			
	}

	private void updateMetadata(Element xml, String id, Dbms dbms, GroupMapper localGroups, CategoryMapper localCateg) throws Exception {
	    Log.info(ARCSDE_LOG_MODULE_NAME, "  - Updating metadata with id: "+ id);
        //
        // update metadata
        //
        boolean validate = false;
        boolean ufo = false;
        boolean index = false;
        String language = context.getLanguage();
        dataMan.updateMetadata(context, dbms, id, xml, validate, ufo, index, language, new ISODate().toString(), false);

		dbms.execute("DELETE FROM OperationAllowed WHERE metadataId=?", Integer.parseInt(id));
        aligner.addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, dbms, log);

		dbms.execute("DELETE FROM MetadataCateg WHERE metadataId=?", Integer.parseInt(id));
		aligner.addCategories(id, params.getCategories(), localCateg, dataMan, dbms, context, log, null);

		dbms.commit();
		dataMan.indexMetadata(dbms, id);
	}
	/**
	 * Inserts a metadata into the database. Lucene index is updated after insertion.
	 * @param xml
	 * @param uuid
	 * @param dbms
	 * @param schema
	 * @param localGroups
	 * @param localCateg
	 * @throws Exception
	 */
	private String addMetadata(Element xml, String uuid, Dbms dbms, String schema, GroupMapper localGroups, CategoryMapper localCateg) throws Exception {
	    Log.info(ARCSDE_LOG_MODULE_NAME, "  - Adding metadata with remote uuid: "+ uuid);

        //
        // insert metadata
        //
        String source = params.uuid;
        String createDate = new ISODate().toString();
        String docType = null, title = null, isTemplate = null, group = null, category = null;
        boolean ufo = false, indexImmediate = false;
        String id = dataMan.insertMetadata(context, dbms, schema, xml, context.getSerialFactory().getSerial(dbms, "Metadata"), uuid, Integer.parseInt(params.ownerId), group, source,
                         isTemplate, docType, title, category, createDate, createDate, ufo, indexImmediate);



		int iId = Integer.parseInt(id);
		dataMan.setTemplateExt(dbms, iId, "n", null);
		dataMan.setHarvestedExt(dbms, iId, source);

		aligner.addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, dbms, log);
		aligner.addCategories(id, params.getCategories(), localCateg, dataMan, dbms, context, log, null);

		dbms.commit();
		dataMan.indexMetadata(dbms, id);
		return id;
	}
	
	@Override
	protected void doInit(Element entry) throws BadInputEx {
		params = new ArcSDEParams(dataMan);
        super.setParams(params);
		params.create(entry);
	}

	@Override
	protected void doUpdate(Dbms dbms, String id, Element node) throws BadInputEx, SQLException {
		ArcSDEParams copy = params.copy();

		//--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(dbms, path);

		//--- update database
		storeNode(dbms, copy, path);

		//--- we update a copy first because if there is an exception ArcSDEParams
		//--- could be half updated and so it could be in an inconsistent state

		Lib.sources.update(dbms, copy.uuid, copy.name, true);
		Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.uuid);
		
		params = copy;
        super.setParams(params);
	}

	@Override
	public String getType() {
		return "arcsde";
	}

}