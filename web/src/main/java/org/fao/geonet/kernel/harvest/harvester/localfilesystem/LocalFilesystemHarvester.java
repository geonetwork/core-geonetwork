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
package org.fao.geonet.kernel.harvest.harvester.localfilesystem;

import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.util.ISODate;
import org.fao.geonet.util.XMLExtensionFilenameFilter;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Harvester for local filesystem.
 * 
 * @author heikki doeleman
 *
 */
public class LocalFilesystemHarvester extends AbstractHarvester {
	
	private LocalFilesystemParams params;
	private LocalFilesystemResult result;
	
	public static void init(ServiceContext context) throws Exception {
	}
	
	@Override
	protected void storeNodeExtra(Dbms dbms, AbstractParams params, String path, String siteId, String optionsId) throws SQLException {
		LocalFilesystemParams lp = (LocalFilesystemParams) params;
		settingMan.add(dbms, "id:"+siteId, "icon", lp.icon);
		settingMan.add(dbms, "id:"+siteId, "recurse", lp.recurse);
		settingMan.add(dbms, "id:"+siteId, "directory", lp.directoryname);
		settingMan.add(dbms, "id:"+siteId, "nodelete", lp.nodelete);
	}

	@Override
	protected String doAdd(Dbms dbms, Element node) throws BadInputEx, SQLException {
		params = new LocalFilesystemParams(dataMan);
		
		//--- retrieve/initialize information
		params.create(node);
		
		//--- force the creation of a new uuid
		params.uuid = UUID.randomUUID().toString();
		
		String id = settingMan.add(dbms, "harvesting", "node", getType());
		storeNode(dbms, params, "id:"+id);
		
		Lib.sources.update(dbms, params.uuid, params.name, true);
		Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.uuid);
        	
		return id;
	}

	@Override
	protected void doAddInfo(Element node) {
		//--- if the harvesting is not started yet, we don't have any info

		if (result == null)
			return;
		
		//--- ok, add proper info

		Element info = node.getChild("info");
		Element res  = getResult();
		info.addContent(res);		
	}
		
	@Override
	protected Element getResult() {
		Element res  = new Element("result");
		if (result != null) {
			add(res, "total",          result.total);
			add(res, "added",          result.added);
			add(res, "updated",        result.updated);
			add(res, "unchanged",      result.unchanged);
			add(res, "unknownSchema",  result.unknownSchema);
			add(res, "removed",        result.removed);
			add(res, "unretrievable",  result.unretrievable);
			add(res, "badFormat",      result.badFormat);
			add(res, "doesNotValidate",result.doesNotValidate);
		}
		return res;
	}

	@Override
	protected void doDestroy(Dbms dbms) throws SQLException {
        File icon = new File(Resources.locateLogosDir(context), params.uuid +".gif");

		icon.delete();
		Lib.sources.delete(dbms, dataMan.getSiteID());
	}

	/**
	 * Returns a list of all file names in a directory - if recurse is true, 
	 * processes all subdirectories too.
	 * @param directory
	 * @param recurse
	 * @return
	 */
	private List<String> harvestFromDirectory(File directory, boolean recurse) throws IOException {
		System.out.println("LocalFilesystem harvesting: directory " + directory.getAbsolutePath());
		List<String> results = new ArrayList<String>();
		if(! directory.exists()) {
			throw new IOException("directory does not exist: "+ directory.getAbsolutePath());
		}
		if(! directory.canRead()) {
			throw new IOException("cannot read directory: "+ directory.getAbsolutePath());
		}
		if(! directory.isDirectory()) {
			throw new IOException("directory is not a directory: "+ directory.getAbsolutePath());
		}
		for(File file : directory.listFiles(new XMLExtensionFilenameFilter(XMLExtensionFilenameFilter.ACCEPT_DIRECTORIES))) {
			if(file.isDirectory()) {
				if(recurse) { 
					// recurse
					results.addAll(harvestFromDirectory(file, recurse));
				}
			}
			else {
				if(! file.canRead()) {
					throw new IOException("cannot read file "+ file.getAbsolutePath());
				}
				else {
					System.out.println("adding file: " + file.getName());
					results.add(file.getAbsolutePath());
				}
			}
		}		
		return results;
	}
	
	/**
	 * Aligns new results from filesystem harvesting. Contrary to practice in e.g. CSW Harvesting,
	 * files removed from the harvesting source are NOT removed from the database. Also, no checks
	 * on modification date are done; the result gets inserted or replaced if the result appears to
	 * be in a supported schema.
	 * @param results
	 * @param rm
	 * @throws Exception
	 */
	private void align(List<String> results, ResourceManager rm) throws Exception {
		System.out.println("Start of alignment for : "+ params.name);
		this.result = new LocalFilesystemResult();
		Dbms dbms = (Dbms) rm.open(Geonet.Res.MAIN_DB);

		boolean transformIt = false;
		String thisXslt = context.getAppPath() + Geonet.Path.IMPORT_STYLESHEETS + "/";
		if (!params.importXslt.equals("none")) {
			thisXslt = thisXslt + params.importXslt;
			transformIt = true;
		}

		//----------------------------------------------------------------
		//--- retrieve all local categories and groups
		//--- retrieve harvested uuids for given harvesting node
		CategoryMapper localCateg = new CategoryMapper(dbms);
		GroupMapper localGroups = new GroupMapper(dbms);
		dbms.commit();		
		List<String> idsForHarvestingResult = new ArrayList<String>();
		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for(String xmlFile : results) {
			result.total++;
			Element xml;
			try {
				System.out.println("reading file: " + xmlFile);	
				xml = Xml.loadFile(xmlFile);
			} catch (JDOMException e) { // JDOM problem
				System.out.println("Error loading XML from file " + xmlFile +", ignoring");	
				e.printStackTrace();
				result.badFormat++;
				continue; // skip this one
			} catch (Exception e) { // some other error
				System.out.println("Error retrieving XML from file " + xmlFile +", ignoring");	
				e.printStackTrace();
				result.unretrievable++;
				continue; // skip this one
			}

			// validate it here if requested
			if (params.validate) {
				try {
					Xml.validate(xml);
				} catch (Exception e) {
					System.out.println("Cannot validate XML from file " + xmlFile +", ignoring. Error was: "+e.getMessage());
					result.doesNotValidate++;
					continue; // skip this one
				}
			}
			
			// transform using importxslt if not none
			if (transformIt) {
				try {
					xml = Xml.transform(xml, thisXslt);
				} catch (Exception e) {
					System.out.println("Cannot transform XML from file " + xmlFile+", ignoring. Error was: "+e.getMessage());
					result.badFormat++;
					continue; // skip this one
				}
			}

			String schema = dataMan.autodetectSchema(xml);
			if(schema == null) {
				result.unknownSchema++;
			}
			else {
				String uuid = dataMan.extractUUID(schema, xml);
				if(uuid == null || uuid.equals("")) {
					result.badFormat++;
				}
				else {
					String id = dataMan.getMetadataId(dbms, uuid);
					if (id == null)	{
						System.out.println("adding new metadata");
						id = addMetadata(xml, uuid, dbms, schema, localGroups, localCateg);
						result.added++;
					}
					else {
						System.out.println("updating existing metadata, id is: " + id);
						updateMetadata(xml, id, dbms, localGroups, localCateg);
						result.updated++;
					}
					idsForHarvestingResult.add(id);
				}
			}
		}

		if(!params.nodelete) {
			//
			// delete locally existing metadata from the same source if they were
			// not in this harvesting result
			//
			List<Element> existingMetadata = dataMan.getMetadataByHarvestingSource(dbms, params.uuid);
			for(Element existingId : existingMetadata) {
				String ex$ = existingId.getChildText("id");
				if(!idsForHarvestingResult.contains(ex$)) {
					dataMan.deleteMetadata(context, dbms, ex$);
					result.removed++;
				}
			}			
		}
		System.out.println("End of alignment for : "+ params.name);
	}

	private void updateMetadata(Element xml, String id, Dbms dbms, GroupMapper localGroups, CategoryMapper localCateg) throws Exception {
		System.out.println("  - Updating metadata with id: "+ id);

        //
        // update metadata
        //
        boolean validate = false;
        boolean ufo = false;
        boolean index = false;
        String language = context.getLanguage();

        String changeDate;
        try {
            String schema = dataMan.autodetectSchema(xml);
            changeDate = dataMan.extractDateModified(schema, xml);
        } catch (Exception ex) {
            log.error("LocalFilesystemHarvester - updateMetadata - can't get metadata modified date for metadata id= " + id + ", using current date for modified date");
            changeDate = new ISODate().toString();
        }

        dataMan.updateMetadata(context, dbms, id, xml, validate, ufo, index, language, changeDate, true);

        dbms.execute("DELETE FROM OperationAllowed WHERE metadataId=?", Integer.parseInt(id));
        addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, dbms, log);

		dbms.execute("DELETE FROM MetadataCateg WHERE metadataId=?", Integer.parseInt(id));
		addCategories(id, localCateg, dbms);

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
		System.out.println("  - Adding metadata with remote uuid: "+ uuid);

		String source = params.uuid;
		String createDate = null;

        try {
            createDate = dataMan.extractDateModified(schema, xml);
        } catch (Exception ex) {
            log.error("LocalFilesystemHarvester - addMetadata - can't get metadata modified date for metadata uuid= " + uuid + ", using current date for modified date");
            createDate = new ISODate().toString();
        }

        //
        // insert metadata
        //
        String group = null, isTemplate = null, docType = null, title = null, category = null;
        boolean ufo = false, indexImmediate = false;
        String id = dataMan.insertMetadata(context, dbms, schema, xml, context.getSerialFactory().getSerial(dbms, "Metadata"), uuid, Integer.parseInt(params.ownerId), group, source,
                         isTemplate, docType, title, category, createDate, createDate, ufo, indexImmediate);

		int iId = Integer.parseInt(id);
		dataMan.setTemplateExt(dbms, iId, "n", null);
		dataMan.setHarvestedExt(dbms, iId, source);

        addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, dbms, log);
		addCategories(id, localCateg, dbms);

		dbms.commit();
		dataMan.indexMetadata(dbms, id);
		return id;
	}
	
	//--------------------------------------------------------------------------
	//--- Categories
	//--------------------------------------------------------------------------

	private void addCategories(String id, CategoryMapper localCateg, Dbms dbms) throws Exception {
		for(String catId : params.getCategories()) {
			String name = localCateg.getName(catId);

			if (name == null) {
				System.out.println("    - Skipping removed category with id:"+ catId);
			}
			else {
				System.out.println("    - Setting category : "+ name);
				dataMan.setCategory(context, dbms, id, catId);
			}
		}
	}	

	@Override
	protected void doHarvest(Logger l, ResourceManager rm) throws Exception {
		System.out.println("LocalFilesystem doHarvest: top directory is " + params.directoryname + ", recurse is " + params.recurse);
		File directory = new File(params.directoryname);
		List<String> results = harvestFromDirectory(directory, params.recurse);
		System.out.println("LocalFilesystem doHarvest: found #" + results.size() + " results");
		align(results, rm);		
	}

	@Override
	protected void doInit(Element entry) throws BadInputEx {
		params = new LocalFilesystemParams(dataMan);
		params.create(entry);
	}

	@Override
	protected void doUpdate(Dbms dbms, String id, Element node) throws BadInputEx, SQLException {
		LocalFilesystemParams copy = params.copy();

		//--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(dbms, path);

		//--- update database
		storeNode(dbms, copy, path);

		//--- we update a copy first because if there is an exception LocalFilesystemParams
		//--- could be half updated and so it could be in an inconsistent state

		Lib.sources.update(dbms, copy.uuid, copy.name, true);
		Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + copy.icon, copy.uuid);
		
		params = copy;
	}

	@Override
	public AbstractParams getParams() {
		return params;
	}

	@Override
	public String getType() {
		return "filesystem";
	}

	class LocalFilesystemResult {
		public int total;
		public int added;
		public int updated;
		public int unchanged;
		public int removed;
		public int unknownSchema;
		public int unretrievable;
		public int badFormat;
		public int doesNotValidate;		
	}
}
