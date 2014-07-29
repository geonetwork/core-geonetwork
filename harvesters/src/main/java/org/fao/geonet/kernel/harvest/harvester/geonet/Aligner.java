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

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.*;
import org.fao.geonet.kernel.mef.IMEFVisitor;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.mef.MEFVisitor;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.*;

//=============================================================================

public class Aligner extends BaseAligner
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Aligner(Logger log, ServiceContext context, XmlRequest req,
						GeonetParams params, Element remoteInfo)
	{
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
		log.info("Start of alignment for : "+ params.name);

		//-----------------------------------------------------------------------
		//--- retrieve all local categories and groups
		//--- retrieve harvested uuids for given harvesting node

		localCateg = new CategoryMapper(context);
		localGroups= new GroupMapper(context);
		localUuids = new UUIDMapper(context.getBean(MetadataRepository.class), params.uuid);

        dataMan.flush();

        Pair<String, Map<String, String>> filter =
                HarvesterUtil.parseXSLFilter(params.xslfilter, log);
        processName = filter.one();
        processParams = filter.two();
		
		//-----------------------------------------------------------------------
		//--- remove old metadata

		for (String uuid : localUuids.getUUIDs())
			if (!exists(records, uuid))
			{
				String id = localUuids.getID(uuid);

                if(log.isDebugEnabled()) log.debug("  - Removing old metadata with id:"+ id);
				dataMan.deleteMetadata(context, id);

                dataMan.flush();

                result.locallyRemoved++;
			}

		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for(RecordInfo ri : records)
		{
			result.totalMetadata++;

			if (!dataMan.existsSchema(ri.schema))
			{
                if(log.isDebugEnabled())
                    log.debug("  - Metadata skipped due to unknown schema. uuid:"+ ri.uuid
						 	+", schema:"+ ri.schema);
				result.unknownSchema++;
			}
			else
			{
				String id = dataMan.getMetadataId(ri.uuid);

				// look up value of localrating/enable
				GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
				SettingManager settingManager = gc.getBean(SettingManager.class);
				boolean localRating = settingManager.getValueAsBool("system/localrating/enable", false);
				
				if (id == null)	{
					addMetadata(ri, localRating);
				}
				else {
					updateMetadata(ri, id, localRating);
				}
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

	private void addMetadata(final RecordInfo ri, final boolean localRating) throws Exception
	{
		final String  id[] = { null };
		final Element md[] = { null };

		//--- import metadata from MEF file

		File mefFile = retrieveMEF(ri.uuid);

		try
		{
			MEFLib.visit(mefFile, new MEFVisitor(), new IMEFVisitor()
			{
				public void handleMetadata(Element mdata, int index) throws Exception
				{
					md[index] = mdata;
				}

				//--------------------------------------------------------------------
				
				public void handleMetadataFiles(File[] files, Element info, int index) throws Exception {}
				
				//--------------------------------------------------------------------

				public void handleInfo(Element info, int index) throws Exception
				{
					id[index] = addMetadata(ri, md[index], info, localRating);
				}

				//--------------------------------------------------------------------

				public void handlePublicFile(String file, String changeDate, InputStream is, int index) throws IOException
				{
                    if (id[index] == null) return;

                    if(log.isDebugEnabled()) log.debug("    - Adding remote public file with name:"+ file);
					String pubDir = Lib.resource.getDir(context, "public", id[index]);

					File outFile = new File(pubDir, file);
					FileOutputStream os = null;
					try {
                        os = new FileOutputStream(outFile);
    					BinaryFile.copy(is, os);
    					IO.setLastModified(outFile, new ISODate(changeDate).getTimeInSeconds() * 1000, log.getModule());
					} finally {
					    IOUtils.closeQuietly(os);
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
	                    String dir = Lib.resource.getDir(context, "private", id[index]);
	                    File outFile = new File(dir, file);
	                    FileOutputStream os = null;
	                    try {
                            os = new FileOutputStream(outFile);
    	                    BinaryFile.copy(is, os);
    	                    IO.setLastModified(outFile, new ISODate(changeDate).getTimeInSeconds() * 1000, log.getModule());
	                    } finally {
	                        IOUtils.closeQuietly(os);
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
		     if (!mefFile.delete() && mefFile.exists()) {
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

		if ("true".equals(isTemplate))	isTemplate = "y";
			else 									isTemplate = "n";

        if(log.isDebugEnabled()) log.debug("  - Adding metadata with remote uuid:"+ ri.uuid);

        // validate it here if requested
        if (params.validate) {
            if(!dataMan.validate(md))  {
                log.info("Ignoring invalid metadata");
                result.doesNotValidate++;
                return null;
            }
        }


        if (!params.xslfilter.equals("")) {
            md = HarvesterUtil.processMetadata(dataMan.getSchema(ri.schema),
                    md, processName, processParams, log);
        }
        // insert metadata
        String group = null, docType = null, title = null, category = null;
        // If MEF format is full, private file links needs to be updated
        boolean ufo = params.mefFormatFull;
        boolean indexImmediate = false;
        String id = dataMan.insertMetadata(context, ri.schema, md, ri.uuid, Integer.parseInt(params.ownerId), group, siteId,
                         isTemplate, docType, category, createDate, changeDate, ufo, indexImmediate);

		int iId = Integer.parseInt(id);

        dataMan.setTemplateExt(iId, MetadataType.lookup(isTemplate));
        dataMan.setHarvestedExt(iId, params.uuid);


        MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        Metadata metadata = metadataRepository.findOne(iId);

        addCategories(metadata, params.getCategories(), localCateg, context, log, null);

        metadata = metadataRepository.findOne(iId);

		if(!localRating) {
			String rating = general.getChildText("rating");
			if (rating != null) {
                metadata.getDataInfo().setRating(Integer.valueOf(rating));
            }
		}

		if (popularity != null) {
            metadata.getDataInfo().setPopularity(Integer.valueOf(popularity));
        }


		String pubDir = Lib.resource.getDir(context, "public",  id);
		String priDir = Lib.resource.getDir(context, "private", id);

		IO.mkdirs(new File(pubDir), "Geonet Aligner public resources directory for metadata " + id);
		IO.mkdirs(new File(priDir), "Geonet Aligner private resources directory for metadata " + id);

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
        metadataRepository.save(metadata);
//        dataMan.flush();

        dataMan.indexMetadata(id, false);
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
                log.debug("  - Skipped metadata managed by another harvesting node. uuid:"+ ri.uuid +", name:"+ params.name);
        } else {
			File mefFile = retrieveMEF(ri.uuid);

			try
			{
				MEFLib.visit(mefFile, new MEFVisitor(), new IMEFVisitor()
				{
					public void handleMetadata(Element mdata, int index) throws Exception
					{
						md[index] = mdata;
					}

					//-----------------------------------------------------------------
					
					public void handleMetadataFiles(File[] files, Element info, int index) throws Exception
					{
						//md[index] = mdata;
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
	             if (!mefFile.delete() && mefFile.exists()) {
	                 log.warning("Unable to delete mefFile: "+mefFile);
	             }

			}
		}
	}

	//--------------------------------------------------------------------------

	private void updateMetadata(RecordInfo ri, String id, Element md, Element info, boolean localRating) throws Exception
	{
		String date = localUuids.getChangeDate(ri.uuid);

        // validate it here if requested
        if (params.validate) {
            if(!dataMan.validate(md))  {
                log.info("Ignoring invalid metadata");
                result.doesNotValidate++;
                return;
            }
        }
        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        Metadata metadata;
        if (!ri.isMoreRecentThan(date))
		{
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
        addCategories(metadata, params.getCategories(), localCateg, context, log, null);
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

        dataMan.indexMetadata(id, false);
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
		File resourcesDir = new File(Lib.resource.getDir(context, dir, id));

		File files[] = resourcesDir.listFiles();

		if (files == null)
			log.error("  - Cannot scan directory for " + dir + " files : "+ resourcesDir.getAbsolutePath());

		else for (File file : files)
			if (!existsFile(file.getName(), infoFiles))
			{
                if(log.isDebugEnabled()) {
                    log.debug("  - Removing old " + dir + " file with name="+ file.getName());
                }
                if (!file.delete() && file.exists()) {
                    log.warning("Unable to delete file: "+file);
                }
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
		String resourcesDir  = Lib.resource.getDir(context, dir, id);
		File   locFile = new File(resourcesDir, file);

		ISODate locIsoDate = new ISODate(locFile.lastModified(), false);
		ISODate remIsoDate = new ISODate(changeDate);

		if (!locFile.exists() || remIsoDate.timeDifferenceInSeconds(locIsoDate) > 0)
		{
            if(log.isDebugEnabled()){ log.debug("  - Adding remote " + dir + "  file with name:"+ file);}

			FileOutputStream os = null;
			try {
                os = new FileOutputStream(locFile);
    			BinaryFile.copy(is, os);
    			IO.setLastModified(locFile, remIsoDate.getTimeInSeconds() * 1000, log.getModule());
			} finally {
			    IOUtils.closeQuietly(os);
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

	private File retrieveMEF(String uuid) throws IOException
	{
		request.clearParams();
		request.addParam("uuid",   uuid);
		request.addParam("format", (params.mefFormatFull ? "full" : "partial"));

		request.setAddress(params.getServletPath() +"/srv/en/"+ Geonet.Service.MEF_EXPORT);

		File tempFile = File.createTempFile("temp-", ".dat");
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
    private Map<String, String> processParams = new HashMap<String, String>();

    private HashMap<String, HashMap<String, String>> hmRemoteGroups = new HashMap<String, HashMap<String, String>>();
}
