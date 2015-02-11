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

package org.fao.geonet.kernel.harvest.harvester.geonet;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.HarvesterUtil;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.mef.IMEFVisitor;
import org.fao.geonet.kernel.mef.IVisitor;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.kernel.mef.MEF2Visitor;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.mef.MEFVisitor;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

//=============================================================================

public class Aligner extends BaseAligner
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Aligner(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, XmlRequest req,
                   GeonetParams params, Element remoteInfo)
	{
        super(cancelMonitor);
		this.log     = log;
		this.context = context;
		this.request = req;
		this.params  = params;

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		dataMan = gc.getBean(DataManager.class);
		result  = new HarvestResult();

		//--- save remote categories and groups into hashmaps for a fast access

        // Before 2.11 response contains groups. Now group is used.
        Element groups = remoteInfo.getChild("groups");
        if (groups == null) {
            groups = remoteInfo.getChild("group");
        }
        if (groups != null) {
            @SuppressWarnings("unchecked")
            List<Element> list = groups.getChildren("group");
            setupLocEntity(list, hmRemoteGroups);
        }
	}

	//--------------------------------------------------------------------------

	private void setupLocEntity(List<Element> list, HashMap<String, HashMap<String, String>> hmEntity)
	{

        for (Element entity : list) {
            String name = entity.getChildText("name");

            HashMap<String, String> hm = new HashMap<String, String>();
            hmEntity.put(name, hm);

            @SuppressWarnings("unchecked")
            List<Element> labels = entity.getChild("label").getChildren();

            for (Element el : labels) {
                hm.put(el.getName(), el.getText());
            }
        }
	}

	//--------------------------------------------------------------------------
	//---
	//--- Alignment method
	//---
	//--------------------------------------------------------------------------

	public HarvestResult align(Set<RecordInfo> records, List<HarvestError> errors) throws Exception
	{
		log.info("Start of alignment for : "+ params.getName());

        //-----------------------------------------------------------------------
		//--- retrieve all local categories and groups
		//--- retrieve harvested uuids for given harvesting node

		localCateg = new CategoryMapper(context);
		localGroups= new GroupMapper(context);
		localUuids = new UUIDMapper(context.getBean(MetadataRepository.class), params.getUuid());

        dataMan.flush();

        Pair<String, Map<String, Object>> filter =
                HarvesterUtil.parseXSLFilter(params.xslfilter, log);
        processName = filter.one();
        processParams = filter.two();
		
		//-----------------------------------------------------------------------
		//--- remove old metadata

		for (String uuid : localUuids.getUUIDs()) {
            if (cancelMonitor.get()) {
                return this.result;
            }

            if (!exists(records, uuid)) {
                String id = localUuids.getID(uuid);

                if (log.isDebugEnabled()) log.debug("  - Removing old metadata with id:" + id);
                dataMan.deleteMetadata(context, id);

                dataMan.flush();

                result.locallyRemoved++;
            }
        }
		//-----------------------------------------------------------------------
		//--- insert/update new metadata
// Load preferred schema and set to iso19139 by default
        preferredSchema = context.getBean(ServiceConfig.class).getMandatoryValue("preferredSchema");
        if (preferredSchema == null) {
            preferredSchema = "iso19139";
        }

        for(RecordInfo ri : records) {
            if (cancelMonitor.get()) {
                return this.result;
            }

            result.totalMetadata++;

            // Mef full format provides ISO19139 records in both the profile
            // and ISO19139 so we could be able to import them as far as
            // ISO19139 schema is installed by default.
			if (!dataMan.existsSchema(ri.schema) && !ri.schema.startsWith("iso19139.")) {
                if(log.isDebugEnabled())
                    log.debug("  - Metadata skipped due to unknown schema. uuid:"+ ri.uuid
						 	+", schema:"+ ri.schema);
				result.unknownSchema++;
			} else {
				String id = dataMan.getMetadataId(ri.uuid);

				// look up value of localrating/enable
				GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
				SettingManager settingManager = gc.getBean(SettingManager.class);
				boolean localRating = settingManager.getValueAsBool("system/localrating/enable", false);
				
				if (id == null) {
                    addMetadata(ri, localRating);
                } else {
					updateMetadata(ri, id, localRating);
				}
			}
		}

		log.info("End of alignment for : "+ params.getName());

		return result;
	}


	//--------------------------------------------------------------------------
	//---
	//--- Private methods : addMetadata
	//---
	//--------------------------------------------------------------------------
    private Element extractValidMetadataForImport (DirectoryStream<Path> files, Element info) throws IOException, JDOMException {
        Element metadataValidForImport;
        final String finalPreferredSchema = preferredSchema;

        String infoSchema = "_none_";
        if (info != null && info.getContentSize() != 0) {
            Element general = info.getChild("general");
            if (general != null && general.getContentSize() != 0) {
                if (general.getChildText("schema") != null) {
                    infoSchema = general.getChildText("schema");
                }
            }
        }

        Path lastUnknownMetadataFolderName = null;

        if (Log.isDebugEnabled(Geonet.MEF))
            Log.debug(Geonet.MEF, "Multiple metadata files");

        Map<String, Pair<String, Element>> mdFiles =
                new HashMap<String, Pair<String, Element>>();
        for (Path file : files) {
            if (Files.isRegularFile(file)) {
                Element metadata = Xml.loadFile(file);
                try {
                    String metadataSchema = dataMan.autodetectSchema(metadata, null);
                    // If local node doesn't know metadata
                    // schema try to load next xml file.
                    if (metadataSchema == null) {
                        continue;
                    }

                    String currFile = "Found metadata file " + file.getParent().getParent().relativize(file);
                    mdFiles.put(metadataSchema, Pair.read(currFile, metadata));

                } catch (NoSchemaMatchesException e) {
                    // Important folder name to identify metadata should be ../../
                    lastUnknownMetadataFolderName =  file.getParent().getParent().relativize(file.getParent());
                    log.debug("No schema match for " + lastUnknownMetadataFolderName + file.getFileName() + ".");
                }
            }
        }

        if (mdFiles.size() == 0) {
            log.debug("No valid metadata file found" +
                    ((lastUnknownMetadataFolderName == null) ?
                            "" :
                            (" in " + lastUnknownMetadataFolderName)
                    ) + ".");
            return null;
        }

        // 1st: Select metadata with schema in info file
        Pair<String,Element> mdInform = mdFiles.get(infoSchema);
        if (mdInform != null) {
            log.debug(mdInform.one()
                    + " with info.xml schema (" + infoSchema + ").");
            metadataValidForImport = mdInform.two();
            return metadataValidForImport;
        }
        // 2nd: Select metadata with preferredSchema
        mdInform = mdFiles.get(finalPreferredSchema);
        if (mdInform != null) {
            log.debug(mdInform.one()
                    + " with preferred schema (" + finalPreferredSchema + ").");
            metadataValidForImport = mdInform.two();
            return metadataValidForImport;
        }

        // Lastly: Select the first metadata in the map
        String metadataSchema = (String)mdFiles.keySet().toArray()[0];
        mdInform = mdFiles.get(metadataSchema);
        log.debug(mdInform.one()
                + " with known schema (" + metadataSchema + ").");
        metadataValidForImport = mdInform.two();

        return metadataValidForImport;
    }
	private void addMetadata(final RecordInfo ri, final boolean localRating) throws Exception
	{
		final String  id[] = { null };
		final Element md[] = { null };

		//--- import metadata from MEF file

		Path mefFile = retrieveMEF(ri.uuid);

		try {
            String fileType = "mef";
            MEFLib.Version version = MEFLib.getMEFVersion(mefFile);
            if (version != null && version.equals(MEFLib.Version.V2)) {
                fileType = "mef2";
            }

            IVisitor visitor = fileType.equals("mef2") ? new MEF2Visitor() : new MEFVisitor();

            MEFLib.visit(mefFile, visitor, new IMEFVisitor()
		{
				public void handleMetadata(Element mdata, int index) throws Exception
				{
					md[index] = mdata;
				}

				//--------------------------------------------------------------------
				
                public void handleMetadataFiles(DirectoryStream<Path> files, Element info, int index) throws Exception {
                    // Import valid metadata
                    Element metadataValidForImport = extractValidMetadataForImport(files, info);
				
                    if (metadataValidForImport != null) {
                        handleMetadata(metadataValidForImport, index);
                    }
                }
				
				//--------------------------------------------------------------------

				public void handleInfo(Element info, int index) throws Exception
				{

                    final Element metadata = md[index];
                    String schema = dataMan.autodetectSchema(metadata, null);
                    if (info != null && info.getContentSize() != 0) {
                        Element general = info.getChild("general");
                        if (general != null && general.getContentSize() != 0) {
                            Element schemaInfo = general.getChild("schema");
                            if (schemaInfo != null) {
                                schemaInfo.setText(schema);
                            }
                        }
                    }
					id[index] = addMetadata(ri, md[index], info, localRating);
				}

				//--------------------------------------------------------------------

				public void handlePublicFile(String file, String changeDate, InputStream is, int index) throws IOException
				{
                    if (id[index] == null) return;

                    if(log.isDebugEnabled()) log.debug("    - Adding remote public file with name:"+ file);
					Path pubDir = Lib.resource.getDir(context, "public", id[index]);

					Path outFile = pubDir.resolve(file);
					try (OutputStream os = Files.newOutputStream(outFile)){
    					BinaryFile.copy(is, os);
    					IO.touch(outFile, FileTime.from(new ISODate(changeDate).getTimeInSeconds(), TimeUnit.SECONDS));
					}
				}
				
				public void handleFeatureCat(Element md, int index)
						throws Exception {
					// Feature Catalog not managed for harvesting
				}

				public void handlePrivateFile(String file, String changeDate,
						InputStream is, int index) throws IOException {
				    if (params.mefFormatFull) {
                        if(log.isDebugEnabled())
                            log.debug("    - Adding remote private file with name:" + file + " available for download for user used for harvester.");
	                    Path dir = Lib.resource.getDir(context, "private", id[index]);
	                    Path outFile = dir.resolve(file);
	                    try (OutputStream os = Files.newOutputStream(outFile)){
    	                    BinaryFile.copy(is, os);
    	                    IO.touch(outFile, FileTime.from(new ISODate(changeDate).getTimeInSeconds(), TimeUnit.SECONDS));
	                    }
				    }
				}
			});
		}
		catch(Exception e)
		{
			//--- we ignore the exception here. Maybe the metadata has been removed just now
            if(log.isDebugEnabled())
                log.debug("  - Skipped unretrievable metadata (maybe has been removed) with uuid:"+ ri.uuid);
			result.unretrievable++;
			e.printStackTrace();
		}
		finally
		{
            try {
                Files.deleteIfExists(mefFile);
            } catch (IOException e) {
		         log.warning("Unable to delete mefFile: "+mefFile);
		     }
		}
	}

	//--------------------------------------------------------------------------

	private String addMetadata(RecordInfo ri, Element md, Element info, boolean localRating) throws Exception
	{
		Element general = info.getChild("general");

		String createDate = general.getChildText("createDate");
		String changeDate = general.getChildText("changeDate");
		String isTemplate = general.getChildText("isTemplate");
		String siteId     = general.getChildText("siteId");
		String popularity = general.getChildText("popularity");
        String schema = general.getChildText("schema");

		if ("true".equals(isTemplate))	isTemplate = "y";
			else 									isTemplate = "n";

        if(log.isDebugEnabled()) log.debug("  - Adding metadata with remote uuid:"+ ri.uuid);

        try {
            params.getValidate().validate(dataMan, context, md);
        } catch (Exception e) {
            log.info("Ignoring invalid metadata uuid: " + ri.uuid);
            result.doesNotValidate++;
            return null;
        }

        if (!params.xslfilter.equals("")) {
            md = HarvesterUtil.processMetadata(dataMan.getSchema(ri.schema),
                    md, processName, processParams, log);
        }
        // insert metadata
        // If MEF format is full, private file links needs to be updated
        boolean ufo = params.mefFormatFull;
        Metadata metadata = new Metadata().setUuid(ri.uuid);
        metadata.getDataInfo().
                setSchemaId(schema).
                setRoot(md.getQualifiedName()).
                setType(MetadataType.lookup(isTemplate)).
                setCreateDate(new ISODate(createDate)).
                setChangeDate(new ISODate(changeDate));
        metadata.getSourceInfo().
                setSourceId(siteId).
                setOwner(Integer.parseInt(params.getOwnerId()));
        metadata.getHarvestInfo().
                setHarvested(true).
                setUuid(params.getUuid());

        addCategories(metadata, params.getCategories(), localCateg, context, log, null, false);

        metadata = dataMan.insertMetadata(context, metadata, md, true, false, ufo, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

		if(!localRating) {
			String rating = general.getChildText("rating");
			if (rating != null) {
                metadata.getDataInfo().setRating(Integer.valueOf(rating));
            }
		}

		if (popularity != null) {
            metadata.getDataInfo().setPopularity(Integer.valueOf(popularity));
        }


		Path pubDir = Lib.resource.getDir(context, "public",  id);
        Path priDir = Lib.resource.getDir(context, "private", id);

        Files.createDirectories(pubDir);
        Files.createDirectories(priDir);

        if (params.createRemoteCategory) {
            Element categs = info.getChild("categories");
            if (categs != null) {
                Importer.addCategoriesToMetadata(metadata, categs, context);
            }
        }
        if (((ArrayList<Group>)params.getGroupCopyPolicy()).size() == 0) {
            addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);
        } else {
            addPrivilegesFromGroupPolicy(id, info.getChild("privileges"));
        }
        context.getBean(MetadataRepository.class).save(metadata);

        dataMan.indexMetadata(id, true);
		result.addedMetadata++;

		return id;
	}

	//--------------------------------------------------------------------------
	//--- Privileges
	//--------------------------------------------------------------------------

	private void addPrivilegesFromGroupPolicy(String id, Element privil) throws Exception
	{
		Map<String, Set<String>> groupOper = buildPrivileges(privil);

		for (Group remoteGroup : params.getGroupCopyPolicy())
		{
			//--- get operations allowed to remote group
			Set<String> oper = groupOper.get(remoteGroup.name);

			//--- if we don't find any match, maybe the remote group has been removed

			if (oper == null)
				log.info("    - Remote group has been removed or no privileges exist : "+ remoteGroup.name);
			else
			{
				String localGrpId = localGroups.getID(remoteGroup.name);

				if (localGrpId == null)
				{
					//--- group does not exist locally

					if (remoteGroup.policy == Group.CopyPolicy.CREATE_AND_COPY)
					{
                        if(log.isDebugEnabled()) log.debug("    - Creating local group : "+ remoteGroup.name);
						localGrpId = createGroup(remoteGroup.name);

						if (localGrpId == null)
							log.info("    - Specified group was not found remotely : "+ remoteGroup.name);
						else
						{
                            if(log.isDebugEnabled()) log.debug("    - Setting privileges for group : "+ remoteGroup.name);
							addOperations(id, localGrpId, oper);
						}
					}
				}
				else
				{
					//--- group exists locally

					if (remoteGroup.policy == Group.CopyPolicy.COPY_TO_INTRANET)
					{
                        if(log.isDebugEnabled()) log.debug("    - Setting privileges for 'intranet' group");
						addOperations(id, "0", oper);
					}
					else
					{
                        if(log.isDebugEnabled()) log.debug("    - Setting privileges for group : "+ remoteGroup.name);
						addOperations(id, localGrpId, oper);
					}
				}
			}
		}
	}

	//--------------------------------------------------------------------------

	private Map<String, Set<String>> buildPrivileges(Element privil)
	{
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();

		for (Object o : privil.getChildren("group"))
		{
			Element group = (Element) o;
			String  name  = group.getAttributeValue("name");

			Set<String> set = new HashSet<String>();
			map.put(name, set);

			for (Object op : group.getChildren("operation"))
			{
				Element oper = (Element) op;
				name = oper.getAttributeValue("name");
				set.add(name);
			}
		}

		return map;
	}

	//--------------------------------------------------------------------------

	private void addOperations(String id, String groupId, Set<String> oper) throws Exception
	{
		for (String opName : oper)
		{
			int opId = dataMan.getAccessManager().getPrivilegeId(opName);

			//--- allow only: view, download, dynamic, featured
			if (opId == 0 || opId == 1 || opId == 5 || opId == 6) {
                if(log.isDebugEnabled()) log.debug("       --> "+ opName);
				dataMan.setOperation(context, id, groupId, opId +"");
			} else {
                if(log.isDebugEnabled()) log.debug("       --> "+ opName +" (skipped)");
            }
		}
	}

	//--------------------------------------------------------------------------

	private String createGroup(String name) throws Exception
	{
		Map<String, String> hm = hmRemoteGroups.get(name);

		if (hm == null)
			return null;

        org.fao.geonet.domain.Group group = new org.fao.geonet.domain.Group()
                .setName(name);
        group.getLabelTranslations().putAll(hm);

        group = context.getBean(GroupRepository.class).save(group);

        int id = group.getId();
		localGroups.add(name, id +"");

		return id +"";
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : updateMetadata
	//---
	//--------------------------------------------------------------------------

	private void updateMetadata(final RecordInfo ri, final String id, final boolean localRating) throws Exception
	{
		final Element md[]     = { null };
		final Element publicFiles[] = { null };
		final Element privateFiles[] = { null };

		if (localUuids.getID(ri.uuid) == null) {
            if(log.isDebugEnabled())
                log.debug("  - Skipped metadata managed by another harvesting node. uuid:"+ ri.uuid +", name:"+ params.getName());
        } else {
			Path mefFile = retrieveMEF(ri.uuid);

			try
			{
                String fileType = "mef";
                MEFLib.Version version = MEFLib.getMEFVersion(mefFile);
                if (version != null && version.equals(MEFLib.Version.V2)) {
                    fileType = "mef2";
                }

                IVisitor visitor = fileType.equals("mef2") ? new MEF2Visitor() : new MEFVisitor();

                MEFLib.visit(mefFile, visitor, new IMEFVisitor()
				{
					public void handleMetadata(Element mdata, int index) throws Exception
					{
						md[index] = mdata;
					}

					//-----------------------------------------------------------------
					
					public void handleMetadataFiles(DirectoryStream<Path> files, Element info, int index) throws Exception
					{
                        // Import valid metadata
                        Element metadataValidForImport = extractValidMetadataForImport(files, info);

                        if (metadataValidForImport != null) {
                            handleMetadata(metadataValidForImport, index);
					}
                    }
					
					public void handleInfo(Element info, int index) throws Exception
					{
						updateMetadata(ri, id, md[index], info, localRating);
						publicFiles[index] = info.getChild("public");
						privateFiles[index] = info.getChild("private");
					}

					//-----------------------------------------------------------------

					public void handlePublicFile(String file, String changeDate, InputStream is, int index) throws IOException
					{
						updateFile(id, file, "public", changeDate, is, publicFiles[index]);
					}

					public void handleFeatureCat(Element md, int index)
							throws Exception {
						// Feature Catalog not managed for harvesting
					}

					public void handlePrivateFile(String file,
							String changeDate, InputStream is, int index)
							throws IOException {
	                       updateFile(id, file, "private", changeDate, is, privateFiles[index]);
					}
					
				});
			}
			catch(Exception e)
			{
				//--- we ignore the exception here. Maybe the metadata has been removed just now
				result.unretrievable++;
			}
			finally
			{
                try{
                    Files.deleteIfExists(mefFile);
                } catch (IOException e) {
	                 log.warning("Unable to delete mefFile: "+mefFile);
	             }

			}
		}
	}

	//--------------------------------------------------------------------------

	private void updateMetadata(RecordInfo ri, String id, Element md, Element info, boolean localRating) throws Exception
	{
		String date = localUuids.getChangeDate(ri.uuid);


        try {
            params.getValidate().validate(dataMan, context, md);
        } catch (Exception e) {
            log.info("Ignoring invalid metadata uuid: " + ri.uuid);
            result.doesNotValidate++;
            return;
        }

        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        Metadata metadata;
        if (!ri.isMoreRecentThan(date)) {
            if(log.isDebugEnabled())
                log.debug("  - XML not changed for local metadata with uuid:"+ ri.uuid);
			result.unchangedMetadata++;
            metadata = metadataRepository.findOne(id);
            if (metadata == null) {
                throw new NoSuchElementException("Unable to find a metadata with ID: "+id);
            }
		} else {
            if (!params.xslfilter.equals("")) {
                md = HarvesterUtil.processMetadata(dataMan.getSchema(ri.schema),
                        md, processName, processParams, log);
            }
            // update metadata
            if(log.isDebugEnabled())
                log.debug("  - Updating local metadata with id="+ id);

            boolean validate = false;
            boolean ufo = params.mefFormatFull;
            boolean index = false;
            boolean updateDateStamp = true;
            String language = context.getLanguage();
            dataMan.updateMetadata(context, id, md, validate, ufo, index, language, ri.changeDate,
                    updateDateStamp);
            metadata = metadataRepository.findOne(id);
            result.updatedMetadata++;
		}

        metadata.getCategories().clear();
        addCategories(metadata, params.getCategories(), localCateg, context, log, null, true);
        metadata = metadataRepository.findOne(id);

		Element general = info.getChild("general");

		String popularity = general.getChildText("popularity");

		if(!localRating) {
			String rating = general.getChildText("rating");
			if (rating != null) {
				metadata.getDataInfo().setRating(Integer.valueOf(rating));
            }
		}
		
		if (popularity != null) {
            metadata.getDataInfo().setPopularity(Integer.valueOf(popularity));
        }

		if (params.createRemoteCategory) {
            Element categs = info.getChild("categories");
            if (categs != null) {
                Importer.addCategoriesToMetadata(metadata, categs, context);
            }
        }
		
        OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
        repository.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.parseInt(id));
        if (((ArrayList<Group>)params.getGroupCopyPolicy()).size() == 0) {
            addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);
        } else {
            addPrivilegesFromGroupPolicy(id, info.getChild("privileges"));
        }

        metadataRepository.save(metadata);
//        dataMan.flush();

        dataMan.indexMetadata(id, true);
	}


	//--------------------------------------------------------------------------
	//--- Public file update methods
	//--------------------------------------------------------------------------

	private void updateFile(String id, String file, String dir, String changeDate,
									InputStream is, Element files) throws IOException
	{
		if (files == null)
		{
            if(log.isDebugEnabled()) log.debug("  - No file found in info.xml. Cannot update file:" + file);
		}
		else
		{
			removeOldFile(id, files, dir);
			updateChangedFile(id, file, dir, changeDate, is);
		}
	}

	//--------------------------------------------------------------------------

	private void removeOldFile(String id, Element infoFiles, String dir)
	{
		Path resourcesDir = Lib.resource.getDir(context, dir, id);

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(resourcesDir)) {
            for (Path file : paths) {
                if (!existsFile(file.getFileName().toString(), infoFiles)) {
                    if (log.isDebugEnabled()) {
                        log.debug("  - Removing old " + dir + " file with name=" + file.getFileName());
                    }
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        log.warning("Unable to delete file: " + file);
                    }
                }
            }
        } catch (IOException e) {
            log.error("  - Cannot scan directory for " + dir + " files : "+ resourcesDir.toAbsolutePath().normalize());
        }
	}

	//--------------------------------------------------------------------------

	private boolean existsFile(String fileName, Element files)
	{
		@SuppressWarnings("unchecked")
        List<Element> list = files.getChildren("file");

        for (Element elem : list) {
            String name = elem.getAttributeValue("name");

            if (fileName.equals(name)) {
                return true;
            }
        }

		return false;
	}

	//--------------------------------------------------------------------------

	private void updateChangedFile(String id, String file, String dir,
											 String changeDate, InputStream is) throws IOException
	{
		Path resourcesDir  = Lib.resource.getDir(context, dir, id);
		Path   locFile = resourcesDir.resolve(file);

		ISODate locIsoDate = new ISODate(Files.getLastModifiedTime(locFile).toMillis(), false);
		ISODate remIsoDate = new ISODate(changeDate);

		if (!Files.exists(locFile) || remIsoDate.timeDifferenceInSeconds(locIsoDate) > 0) {
            if(log.isDebugEnabled()){ log.debug("  - Adding remote " + dir + "  file with name:"+ file);}

			try (OutputStream os = Files.newOutputStream(locFile)) {
    			BinaryFile.copy(is, os);
    			IO.touch(locFile, FileTime.from(remIsoDate.getTimeInSeconds(), TimeUnit.SECONDS));
			}
		}
		else
		{
            if(log.isDebugEnabled()){ log.debug("  - Nothing to do in dir " + dir + " for file with name:"+ file);}
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	/** Return true if the uuid is present in the remote node */

	private boolean exists(Set<RecordInfo> records, String uuid)
	{
		for(RecordInfo ri : records)
			if (uuid.equals(ri.uuid))
				return true;

		return false;
	}

	//--------------------------------------------------------------------------

	private Path retrieveMEF(String uuid) throws IOException
	{
		request.clearParams();
		request.addParam("uuid",   uuid);
		request.addParam("format", (params.mefFormatFull ? "full" : "partial"));

        // Request MEF2 format - if remote node is old
        // it will ignore this parameter and return a MEF1 format
        // which will be handle in addMetadata/updateMetadata.
        request.addParam("version", "2");
        request.addParam("relation", "false");
		request.setAddress(params.getServletPath() + "/" + params.getNode()
				+ "/en/" + Geonet.Service.MEF_EXPORT);

		Path tempFile = Files.createTempFile("temp-", ".dat");
		request.executeLarge(tempFile);

		return tempFile;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private Logger         log;
	private ServiceContext context;
	private XmlRequest     request;
	private GeonetParams   params;
	private DataManager    dataMan;
	private HarvestResult   result;

	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
	private UUIDMapper     localUuids;
	
	private String processName;
    private String preferredSchema;
    private Map<String, Object> processParams = new HashMap<String, Object>();

    private HashMap<String, HashMap<String, String>> hmRemoteGroups = new HashMap<String, HashMap<String, String>>();
}
