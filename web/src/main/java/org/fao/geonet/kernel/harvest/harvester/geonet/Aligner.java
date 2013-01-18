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

import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Xml;
import jeeves.utils.XmlRequest;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.mef.IMEFVisitor;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.mef.MEFVisitor;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//=============================================================================

public class Aligner
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Aligner(Logger log, ServiceContext context, Dbms dbms, XmlRequest req,
						GeonetParams params, Element remoteInfo)
	{
		this.log     = log;
		this.context = context;
		this.dbms    = dbms;
		this.request = req;
		this.params  = params;

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		dataMan = gc.getDataManager();
		result  = new GeonetResult();

		//--- save remote categories and groups into hashmaps for a fast access

		List list = remoteInfo.getChild("groups").getChildren("group");
		setupLocEntity(list, hmRemoteGroups);
	}

	//--------------------------------------------------------------------------

	private void setupLocEntity(List list, HashMap<String, HashMap<String, String>> hmEntity)
	{

        for (Object aList : list) {
            Element entity = (Element) aList;
            String name = entity.getChildText("name");

            HashMap<String, String> hm = new HashMap<String, String>();
            hmEntity.put(name, hm);

            List labels = entity.getChild("label").getChildren();

            for (Object label : labels) {
                Element el = (Element) label;
                hm.put(el.getName(), el.getText());
            }
        }
	}

	//--------------------------------------------------------------------------
	//---
	//--- Alignment method
	//---
	//--------------------------------------------------------------------------

	public GeonetResult align(Set<RecordInfo> records) throws Exception
	{
		log.info("Start of alignment for : "+ params.name);

		//-----------------------------------------------------------------------
		//--- retrieve all local categories and groups
		//--- retrieve harvested uuids for given harvesting node

		localCateg = new CategoryMapper(dbms);
		localGroups= new GroupMapper(dbms);
		localUuids = new UUIDMapper(dbms, params.uuid);
		dbms.commit();


		parseXSLFilter();
		
		//-----------------------------------------------------------------------
		//--- remove old metadata

		for (String uuid : localUuids.getUUIDs())
			if (!exists(records, uuid))
			{
				String id = localUuids.getID(uuid);

                if(log.isDebugEnabled()) log.debug("  - Removing old metadata with id:"+ id);
				dataMan.deleteMetadata(context, dbms, id);
				dbms.commit();
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
				String id = dataMan.getMetadataId(dbms, ri.uuid);

				// look up value of localrating/enable
				GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
				SettingManager settingManager = gc.getSettingManager();
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

	private void parseXSLFilter() {
		processName = params.xslfilter;
		
		// Parse complex xslfilter process_name?process_param1=value&process_param2=value...
		if (params.xslfilter.contains("?")) {
			String[] filterInfo = params.xslfilter.split("\\?");
			processName = filterInfo[0];
            if(log.isDebugEnabled()) log.debug("      - XSL Filter name:" + processName);
			if (filterInfo[1] != null) {
				String[] filterKVP = filterInfo[1].split("&");
				for (String kvp : filterKVP) {
					String[] param = kvp.split("=");
					if (param.length == 2) {
                        if(log.isDebugEnabled()) log.debug("        with param:" + param[0] + " = " + param[1]);
						processParams.put(param[0], param[1]);
					} else {
                        if(log.isDebugEnabled()) log.debug("        no value for param: " + param[0]);
					}
				}
			}
		}
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
					FileOutputStream os = new FileOutputStream(outFile);
					BinaryFile.copy(is, os, false, true);
					outFile.setLastModified(new ISODate(changeDate).getSeconds() * 1000);
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
	                    FileOutputStream os = new FileOutputStream(outFile);
	                    BinaryFile.copy(is, os, false, true);
	                    outFile.setLastModified(new ISODate(changeDate).getSeconds() * 1000);
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
			mefFile.delete();
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

        md = processMetadata(ri, md);
        
        // insert metadata
        String group = null, docType = null, title = null, category = null;
        // If MEF format is full, private file links needs to be updated
        boolean ufo = params.mefFormatFull;
        boolean indexImmediate = false;
        String id = dataMan.insertMetadata(context, dbms, ri.schema, md, context.getSerialFactory().getSerial(dbms, "Metadata"), ri.uuid, Integer.parseInt(params.owner), group, siteId,
                         isTemplate, docType, title, category, createDate, changeDate, ufo, indexImmediate);

		int iId = Integer.parseInt(id);

		dataMan.setTemplateExt(dbms, iId, isTemplate, null);
		dataMan.setHarvestedExt(dbms, iId, params.uuid);
		
		if(!localRating) {
			String rating = general.getChildText("rating");
			if (rating != null)
				dbms.execute("UPDATE Metadata SET rating=? WHERE id=?", new Integer(rating), iId);
		}
		
		if (popularity != null)
			dbms.execute("UPDATE Metadata SET popularity=? WHERE id=?", new Integer(popularity), iId);

		String pubDir = Lib.resource.getDir(context, "public",  id);
		String priDir = Lib.resource.getDir(context, "private", id);

		new File(pubDir).mkdirs();
		new File(priDir).mkdirs();

		addCategories(id);
		if (params.createRemoteCategory) {
    		Element categs = info.getChild("categories");
    		if (categs != null) {
    		    Importer.addCategories(context, dataMan, dbms, id, categs);
    		}
		}
		addPrivileges(id, info.getChild("privileges"));

		dbms.commit();
		dataMan.indexMetadata(dbms, id);
		result.addedMetadata++;

		return id;
	}

	//--------------------------------------------------------------------------
	//--- Categories
	//--------------------------------------------------------------------------

	private void addCategories(String id) throws Exception
	{
		for(String catId : params.getCategories())
		{
			String name = localCateg.getName(catId);

			if (name == null)
			{
                if(log.isDebugEnabled()) log.debug("    - Skipping removed category with id:"+ catId);
			}
			else
			{
                if(log.isDebugEnabled()) log.debug("    - Setting category : "+ name);
				dataMan.setCategory(context, dbms, id, catId);
			}
		}
	}

	//--------------------------------------------------------------------------
	//--- Privileges
	//--------------------------------------------------------------------------

	private void addPrivileges(String id, Element privil) throws Exception
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
				dataMan.setOperation(context, dbms, id, groupId, opId +"");
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

		int id = context.getSerialFactory().getSerial(dbms, "Groups");

		dbms.execute("INSERT INTO Groups(id, name) VALUES (?, ?)", id, name);
		Lib.local.insert(dbms, "Groups", id, hm, "<"+name+">");

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
				mefFile.delete();
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

		if (!ri.isMoreRecentThan(date))
		{
            if(log.isDebugEnabled())
                log.debug("  - XML not changed for local metadata with uuid:"+ ri.uuid);
			result.unchangedMetadata++;
		}
		else {
			md = processMetadata(ri, md);
	        
            // update metadata
            if(log.isDebugEnabled())
                log.debug("  - Updating local metadata with id="+ id);

            boolean validate = false;
            boolean ufo = params.mefFormatFull;
            boolean index = false;
            boolean updateDateStamp = true;
            String language = context.getLanguage();
            dataMan.updateMetadata(context, dbms, id, md, validate, ufo, index, language, ri.changeDate, updateDateStamp);

			result.updatedMetadata++;
		}

		Element general = info.getChild("general");

		String popularity = general.getChildText("popularity");

		if(!localRating) {
			String rating = general.getChildText("rating");
			if (rating != null)
				dbms.execute("UPDATE Metadata SET rating=? WHERE id=?", new Integer(rating), new Integer(id));
		}
		
		if (popularity != null)
			dbms.execute("UPDATE Metadata SET popularity=? WHERE id=?", new Integer(popularity), new Integer(id));

		dbms.execute("DELETE FROM MetadataCateg WHERE metadataId=?", Integer.parseInt(id));
		addCategories(id);
		if (params.createRemoteCategory) {
            Element categs = info.getChild("categories");
            if (categs != null) {
                Importer.addCategories(context, dataMan, dbms, id, categs);
            }
        }
		
		dbms.execute("DELETE FROM OperationAllowed WHERE metadataId=?", Integer.parseInt(id));
		addPrivileges(id, info.getChild("privileges"));

		dbms.commit();
		dataMan.indexMetadata(dbms, id);
	}

	/**
	 * Filter the metadata if process parameter is set and
	 * corresponding XSL transformation exists.
	 * @param ri
	 * @param md
	 * @return
	 */
	private Element processMetadata(RecordInfo ri, Element md) {
		// process metadata
		if (!params.xslfilter.equals("")) {
			MetadataSchema metadataSchema = dataMan.getSchema(ri.schema);
			
			String filePath = metadataSchema.getSchemaDir() + "/process/" + processName + ".xsl";
			File xslProcessing = new File(filePath);
			if (!xslProcessing.exists()) {
				log.info("     processing instruction not found for " + ri.schema + " schema. metadata not filtered.");
			} else {
				Element processedMetadata = null;
				try {
					processedMetadata = Xml.transform(md, filePath, processParams);
                    if(log.isDebugEnabled()) log.debug("     metadata filtered.");
					md = processedMetadata;
				} catch (Exception e) {
					log.warning("     processing error (" + params.xslfilter + "): " + e.getMessage());
				}
			}
		}
		return md;
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
                if(log.isDebugEnabled()) log.debug("  - Removing old " + dir + " file with name="+ file.getName());
				file.delete();
			}
	}

	//--------------------------------------------------------------------------

	private boolean existsFile(String fileName, Element files)
	{
		List list = files.getChildren("file");

        for (Object aList : list) {
            Element elem = (Element) aList;
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

		ISODate locIsoDate = new ISODate(locFile.lastModified());
		ISODate remIsoDate = new ISODate(changeDate);

		if (!locFile.exists() || remIsoDate.sub(locIsoDate) > 0)
		{
            if(log.isDebugEnabled()) log.debug("  - Adding remote " + dir + "  file with name:"+ file);

			FileOutputStream os = new FileOutputStream(locFile);
			BinaryFile.copy(is, os, false, true);
			locFile.setLastModified(remIsoDate.getSeconds() * 1000);
		}
		else
		{
            if(log.isDebugEnabled()) log.debug("  - Nothing to do in dir " + dir + " for file with name:"+ file);
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
	private Dbms           dbms;
	private XmlRequest     request;
	private GeonetParams   params;
	private DataManager    dataMan;
	private GeonetResult   result;

	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
	private UUIDMapper     localUuids;
	
	private String processName;
	private HashMap<String, String> processParams = new HashMap<String, String>();
	
	private HashMap<String, HashMap<String, String>> hmRemoteGroups = new HashMap<String, HashMap<String, String>>();
}

//=============================================================================


