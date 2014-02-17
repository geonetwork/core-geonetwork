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

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.*;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.util.XMLExtensionFilenameFilter;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Harvester for local filesystem.
 * 
 * @author heikki doeleman
 *
 */
public class LocalFilesystemHarvester extends AbstractHarvester<HarvestResult> {
	
	//FIXME Put on a different file?
	private BaseAligner aligner = new BaseAligner() {};
	private LocalFilesystemParams params;

	
	@Override
	protected void storeNodeExtra(AbstractParams params, String path, String siteId, String optionsId) throws SQLException {
		LocalFilesystemParams lp = (LocalFilesystemParams) params;
        super.setParams(lp);
        
        settingMan.add( "id:"+siteId, "icon", lp.icon);
		settingMan.add( "id:"+siteId, "recurse", lp.recurse);
		settingMan.add( "id:"+siteId, "directory", lp.directoryname);
		settingMan.add( "id:"+siteId, "nodelete", lp.nodelete);
        settingMan.add( "id:"+siteId, "checkFileLastModifiedForUpdate", lp.checkFileLastModifiedForUpdate);
	}

	@Override
	protected String doAdd(Element node) throws BadInputEx, SQLException {
		params = new LocalFilesystemParams(dataMan);
        super.setParams(params);

        //--- retrieve/initialize information
		params.create(node);
		
		//--- force the creation of a new uuid
		params.uuid = UUID.randomUUID().toString();
		
		String id = settingMan.add( "harvesting", "node", getType());
		storeNode( params, "id:"+id);

        Source source = new Source(params.uuid, params.name, true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.uuid);
        	
		return id;
	}

	/**
	 * Aligns new results from filesystem harvesting. Contrary to practice in e.g. CSW Harvesting,
	 * files removed from the harvesting source are NOT removed from the database. Also, no checks
	 * on modification date are done; the result gets inserted or replaced if the result appears to
	 * be in a supported schema.
	 * @param listOfFiles
	 * @throws Exception
	 */
	private HarvestResult align(List<File> listOfFiles) throws Exception {
		log.debug("Start of alignment for : "+ params.name);
		result = new HarvestResult();

		boolean transformIt = false;
		String thisXslt = context.getAppPath() + Geonet.Path.IMPORT_STYLESHEETS + "/";
		if (!params.importXslt.equals("none")) {
			thisXslt = thisXslt + params.importXslt;
			transformIt = true;
		}

		//----------------------------------------------------------------
		//--- retrieve all local categories and groups
		//--- retrieve harvested uuids for given harvesting node
		CategoryMapper localCateg = new CategoryMapper(context);
		GroupMapper localGroups = new GroupMapper(context);

		List<String> idsForHarvestingResult = new ArrayList<String>();
		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for(File file : listOfFiles) {
			result.totalMetadata++;
			Element xml;
			String filePath = file.getCanonicalPath();
			
			try {
				log.debug("reading file: " + filePath);	
				xml = Xml.loadFile(file);
			} catch (JDOMException e) { // JDOM problem
				log.debug("Error loading XML from file " + filePath +", ignoring");	
				e.printStackTrace();
				result.badFormat++;
				continue; // skip this one
			} catch (Exception e) { // some other error
				log.debug("Error retrieving XML from file " + filePath +", ignoring");	
				e.printStackTrace();
				result.unretrievable++;
				continue; // skip this one
			}

			// validate it here if requested
			if (params.validate) {
				try {
					Xml.validate(xml);
				} catch (Exception e) {
					log.debug("Cannot validate XML from file " + filePath +", ignoring. Error was: "+e.getMessage());
					result.doesNotValidate++;
					continue; // skip this one
				}
			}
			
			// transform using importxslt if not none
			if (transformIt) {
				try {
					xml = Xml.transform(xml, thisXslt);
				} catch (Exception e) {
					log.debug("Cannot transform XML from file " + filePath+", ignoring. Error was: "+e.getMessage());
					result.badFormat++;
					continue; // skip this one
				}
			}

			String schema = dataMan.autodetectSchema(xml, null);
			if(schema == null) {
				result.unknownSchema++;
			}
			else {
				String uuid = dataMan.extractUUID(schema, xml);
				if(uuid == null || uuid.equals("")) {
					result.badFormat++;
				}
				else {
					String id = dataMan.getMetadataId( uuid);
					if (id == null)	{
					    // For new record change date will be the time
					    // the record was harvested
                        String createDate = new ISODate().toString();
                        // or the last modified date of the file
                        if (params.checkFileLastModifiedForUpdate) {
                            createDate = new ISODate(file.lastModified(), false).getDateAndTime();
					    }
                        
                        
						log.debug("adding new metadata");
						id = addMetadata(xml, uuid, schema, localGroups, localCateg, createDate);
						result.addedMetadata++;
					} else {
					    // Check last modified date of the file with the record change date
					    // to check if an update is required
					    if (params.checkFileLastModifiedForUpdate) {
    					    Date fileDate = new Date(file.lastModified());

                            final Metadata metadata = context.getBean(MetadataRepository.class).findOne(id);
                            final ISODate modified = metadata.getDataInfo().getChangeDate();
                            Date recordDate = modified.toDate();
                            
    					    log.debug(" File date is: " + fileDate.toString() + " / record date is: " + modified);
    					    if (recordDate.before(fileDate)) {
    					        log.debug("  Db record is older than file. Updating record with id: " + id);
    					        updateMetadata(xml, id, localGroups, localCateg);
                                result.updatedMetadata ++;
    					    } else {
    					        log.debug("  Db record is not older than last modified date of file. No need for update.");
    					        result.unchangedMetadata ++;
    					    }
					    } else {
    					    log.debug("  updating existing metadata, id is: " + id);
    						updateMetadata(xml, id, localGroups, localCateg);
    						result.updatedMetadata++;
					    }
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
            List<Metadata> existingMetadata = context.getBean(MetadataRepository.class).findAllByHarvestInfo_Uuid(params.uuid);
			for(Metadata existingId : existingMetadata) {
				String ex$ = String.valueOf(existingId.getId());
				if(!idsForHarvestingResult.contains(ex$)) {
				    log.debug("  Removing: " + ex$);
					dataMan.deleteMetadata(context, ex$);
					result.locallyRemoved++;
				}
			}			
		}
		log.debug("End of alignment for : "+ params.name);
		return result;
	}

	private void updateMetadata(Element xml, String id, GroupMapper localGroups, CategoryMapper localCateg) throws Exception {
		log.debug("  - Updating metadata with id: "+ id);

        //
        // update metadata
        //
        boolean validate = false;
        boolean ufo = false;
        boolean index = false;
        String language = context.getLanguage();
        final Metadata metadata = dataMan.updateMetadata(context, id, xml, validate, ufo, index, language, new ISODate().toString(),
                false);

        OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
        repository.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.parseInt(id));
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
	 * @param createDate TODO
	 * @throws Exception
	 */
	private String addMetadata(Element xml, String uuid, String schema, GroupMapper localGroups, CategoryMapper localCateg, String createDate) throws Exception {
		log.debug("  - Adding metadata with remote uuid: "+ uuid);

		String source = params.uuid;
		
        //
        // insert metadata
        //
        String group = null, isTemplate = null, docType = null, title = null, category = null;
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
    public void doHarvest(Logger l) throws Exception {
		log.debug("LocalFilesystem doHarvest: top directory is " + params.directoryname + ", recurse is " + params.recurse);
		File directory = new File(params.directoryname);
		List<File> results = IO.getFilesInDirectory(directory, params.recurse, new XMLExtensionFilenameFilter(XMLExtensionFilenameFilter.ACCEPT_DIRECTORIES));
		log.debug("LocalFilesystem doHarvest: found #" + results.size() + " XML files.");
		this.result = align(results);
	}

	@Override
	protected void doInit(Element entry, ServiceContext context) throws BadInputEx {
		params = new LocalFilesystemParams(dataMan);
        super.setParams(params);
        params.create(entry);
	}

	@Override
	protected void doUpdate(String id, Element node) throws BadInputEx, SQLException {
		LocalFilesystemParams copy = params.copy();

		//--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(path);

		//--- update database
		storeNode(copy, path);

		//--- we update a copy first because if there is an exception LocalFilesystemParams
		//--- could be half updated and so it could be in an inconsistent state

        Source source = new Source(copy.uuid, copy.name, true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + copy.icon, copy.uuid);
		
		params = copy;
        super.setParams(params);

    }

}
