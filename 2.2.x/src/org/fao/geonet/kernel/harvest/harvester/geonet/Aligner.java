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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.XmlRequest;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.mef.MEFVisitor;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

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

		for (int i=0; i<list.size(); i++)
		{
			Element entity= (Element) list.get(i);
			String  name  = entity.getChildText("name");

			HashMap<String, String> hm = new HashMap<String, String>();
			hmEntity.put(name, hm);

			List labels = entity.getChild("label").getChildren();

			for (int j=0; j<labels.size(); j++)
			{
				Element el = (Element) labels.get(j);
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

		//-----------------------------------------------------------------------
		//--- remove old metadata

		for (String uuid : localUuids.getUUIDs())
			if (!exists(records, uuid))
			{
				String id = localUuids.getID(uuid);

				log.debug("  - Removing old metadata with id:"+ id);
				dataMan.deleteMetadata(dbms, id);
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
				log.debug("  - Metadata skipped due to unknown schema. uuid:"+ ri.uuid
							 +", schema:"+ ri.schema);
				result.unknownSchema++;
			}
			else
			{
				String id = dataMan.getMetadataId(dbms, ri.uuid);

				if (id == null)	addMetadata(ri);
					else				updateMetadata(ri, id);
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

	private void addMetadata(final RecordInfo ri) throws Exception
	{
		final String  id[] = { null };
		final Element md[] = { null };

		//--- import metadata from MEF file

		File mefFile = retrieveMEF(ri.uuid);

		try
		{
			MEFLib.visit(mefFile, new MEFVisitor()
			{
				public void handleMetadata(Element mdata) throws Exception
				{
					md[0] = mdata;
				}

				//--------------------------------------------------------------------

				public void handleInfo(Element info) throws Exception
				{
					id[0] = addMetadata(ri, md[0], info);
				}

				//--------------------------------------------------------------------

				public void handlePublicFile(String file, String changeDate, InputStream is) throws IOException
				{
					log.debug("    - Adding remote public file with name:"+ file);
					String pubDir = Lib.resource.getDir(context, "public", id[0]);

					File outFile = new File(pubDir, file);
					FileOutputStream os = new FileOutputStream(outFile);
					BinaryFile.copy(is, os, false, true);
					outFile.setLastModified(new ISODate(changeDate).getSeconds() * 1000);
				}

				//--------------------------------------------------------------------

				public void handlePrivateFile(String file, String changeDate, InputStream is) {}
			});
		}
		catch(Exception e)
		{
			//--- we ignore the exception here. Maybe the metadata has been removed just now
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

	private String addMetadata(RecordInfo ri, Element md, Element info) throws Exception
	{
		Element general = info.getChild("general");

		String createDate = general.getChildText("createDate");
		String changeDate = general.getChildText("changeDate");
		String isTemplate = general.getChildText("isTemplate");
		String siteId     = general.getChildText("siteId");
		String rating     = general.getChildText("rating");
		String popularity = general.getChildText("popularity");

		if ("true".equals(isTemplate))	isTemplate = "y";
			else 									isTemplate = "n";

		log.debug("  - Adding metadata with remote uuid:"+ ri.uuid);

		String id = dataMan.insertMetadataExt(dbms, ri.schema, md, context.getSerialFactory(),
													 siteId, createDate, changeDate, ri.uuid, 1, null);

		int iId = Integer.parseInt(id);

		dataMan.setTemplate(dbms, iId, isTemplate, null);
		dataMan.setHarvested(dbms, iId, params.uuid);

		if (rating != null)
			dbms.execute("UPDATE Metadata SET rating=? WHERE id=?", new Integer(rating), iId);

		if (popularity != null)
			dbms.execute("UPDATE Metadata SET popularity=? WHERE id=?", new Integer(popularity), iId);

		String pubDir = Lib.resource.getDir(context, "public",  id);
		String priDir = Lib.resource.getDir(context, "private", id);

		new File(pubDir).mkdirs();
		new File(priDir).mkdirs();

		addCategories(id);
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
				log.debug("    - Skipping removed category with id:"+ catId);
			else
			{
				log.debug("    - Setting category : "+ name);
				dataMan.setCategory(dbms, id, catId);
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
						log.debug("    - Creating local group : "+ remoteGroup.name);
						localGrpId = createGroup(remoteGroup.name);

						if (localGrpId == null)
							log.info("    - Specified group was not found remotely : "+ remoteGroup.name);
						else
						{
							log.debug("    - Setting privileges for group : "+ remoteGroup.name);
							addOperations(id, localGrpId, oper);
						}
					}
				}
				else
				{
					//--- group exists locally

					if (remoteGroup.policy == Group.CopyPolicy.COPY_TO_INTRANET)
					{
						log.debug("    - Setting privileges for 'intranet' group");
						addOperations(id, "0", oper);
					}
					else
					{
						log.debug("    - Setting privileges for group : "+ remoteGroup.name);
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

			//--- allow only: view, dynamic, featured
			if (opId == 0 || opId == 5 || opId == 6)
			{
				log.debug("       --> "+ opName);
				dataMan.setOperation(dbms, id, groupId, opId +"");
			}
			else
				log.debug("       --> "+ opName +" (skipped)");
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

	private void updateMetadata(final RecordInfo ri, final String id) throws Exception
	{
		final Element md[]     = { null };
		final Element thumbs[] = { null };

		if (localUuids.getID(ri.uuid) == null)
			log.debug("  - Skipped metadata managed by another harvesting node. uuid:"+ ri.uuid +", name:"+ params.name);
		else
		{
			File mefFile = retrieveMEF(ri.uuid);

			try
			{
				MEFLib.visit(mefFile, new MEFVisitor()
				{
					public void handleMetadata(Element mdata) throws Exception
					{
						md[0] = mdata;
					}

					//-----------------------------------------------------------------

					public void handleInfo(Element info) throws Exception
					{
						updateMetadata(ri, id, md[0], info);
						thumbs[0] = info.getChild("thumbnails");
					}

					//-----------------------------------------------------------------

					public void handlePublicFile(String file, String changeDate, InputStream is) throws IOException
					{
						updateFile(id, file, changeDate, is, thumbs[0]);
					}

					//-----------------------------------------------------------------

					public void handlePrivateFile(String file, String changeDate, InputStream is) {}
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

	private void updateMetadata(RecordInfo ri, String id, Element md, Element info) throws Exception
	{
		String date = localUuids.getChangeDate(ri.uuid);

		if (!ri.isMoreRecentThan(date))
		{
			log.debug("  - XML not changed for local metadata with uuid:"+ ri.uuid);
			result.unchangedMetadata++;
		}
		else
		{
			log.debug("  - Updating local metadata with id="+ id);
			dataMan.updateMetadataExt(dbms, id, md, ri.changeDate);
			result.updatedMetadata++;
		}

		Element general = info.getChild("general");

		String rating     = general.getChildText("rating");
		String popularity = general.getChildText("popularity");

		if (rating != null)
			dbms.execute("UPDATE Metadata SET rating=? WHERE id=?", new Integer(rating), new Integer(id));

		if (popularity != null)
			dbms.execute("UPDATE Metadata SET popularity=? WHERE id=?", new Integer(popularity), new Integer(id));

		dbms.execute("DELETE FROM MetadataCateg WHERE metadataId=?", Integer.parseInt(id));
		addCategories(id);

		dbms.execute("DELETE FROM OperationAllowed WHERE metadataId=?", Integer.parseInt(id));
		addPrivileges(id, info.getChild("privileges"));

		dbms.commit();
		dataMan.indexMetadata(dbms, id);
	}

	//--------------------------------------------------------------------------
	//--- Public file update methods
	//--------------------------------------------------------------------------

	private void updateFile(String id, String file, String changeDate, InputStream is,
									Element files) throws IOException
	{
		if (files == null)
			log.debug("  - No 'public' element in info.xml. Cannot update public file :"+ file);
		else
		{
			removeOldFile(id, files);
			updateChangedFile(id, file, changeDate, is);
		}
	}

	//--------------------------------------------------------------------------

	private void removeOldFile(String id, Element infoFiles)
	{
		File pubDir = new File(Lib.resource.getDir(context, "public", id));

		File files[] = pubDir.listFiles();

		if (files == null)
			log.error("  - Cannot scan directory for public files : "+ pubDir.getAbsolutePath());

		else for (File file : files)
			if (!existsFile(file.getName(), infoFiles))
			{
				log.debug("  - Removing old public file with name="+ file.getName());
				file.delete();
			}
	}

	//--------------------------------------------------------------------------

	private boolean existsFile(String fileName, Element files)
	{
		List list = files.getChildren("file");

		for (int i=0; i<list.size(); i++)
		{
			Element elem = (Element) list.get(i);
			String  name = elem.getAttributeValue("name");

			if (fileName.equals(name))
				return true;
		}

		return false;
	}

	//--------------------------------------------------------------------------

	private void updateChangedFile(String id, String file, String changeDate,
											 InputStream is) throws IOException
	{
		String pubDir  = Lib.resource.getDir(context, "public", id);
		File   locFile = new File(pubDir, file);

		ISODate locIsoDate = new ISODate(locFile.lastModified());
		ISODate remIsoDate = new ISODate(changeDate);

		if (!locFile.exists() || remIsoDate.sub(locIsoDate) > 0)
		{
			log.debug("  - Adding remote public file with name:"+ file);

			FileOutputStream os = new FileOutputStream(locFile);
			BinaryFile.copy(is, os, false, true);
			locFile.setLastModified(remIsoDate.getSeconds() * 1000);
		}
		else
		{
			log.debug("  - Nothing to do to public file with name:"+ file);
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
		request.addParam("format", "partial");

		request.setAddress("/"+ params.servlet +"/srv/en/"+ Geonet.Service.MEF_EXPORT);

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

	private HashMap<String, HashMap<String, String>> hmRemoteGroups = new HashMap<String, HashMap<String, String>>();
}

//=============================================================================


