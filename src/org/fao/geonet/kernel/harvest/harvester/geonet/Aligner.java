//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.geonet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jeeves.exceptions.BadParameterEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.XmlRequest;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
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

	public Aligner(Logger log, XmlRequest req, GeonetParams params, DataManager dm,
						Dbms dbms, ServiceContext sc, CategoryMapper cm, GroupMapper gm,
						Element remoteInfo)
	{
		this.log        = log;
		this.req        = req;
		this.params     = params;
		this.dataMan    = dm;
		this.dbms       = dbms;
		this.context    = sc;
		this.localCateg = cm;
		this.localGroups= gm;

		//--- save remote categories and groups into hashmaps for a fast access

		List list = remoteInfo.getChild("categories").getChildren("category");
		setupLocEntity(list, hmRemoteCateg);

		list = remoteInfo.getChild("groups").getChildren("group");
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

	public AlignerResult align(Element result, String siteId) throws Exception
	{
		log.info("Start of alignment for site-id="+ siteId);

		this.result = new AlignerResult();
		this.result.siteId = siteId;

		List mdList = result.getChildren("metadata");

		//-----------------------------------------------------------------------
		//--- retrieve local uuids for given site-id

		localUuids = new UUIDMapper(dbms, siteId);

		//-----------------------------------------------------------------------
		//--- remove old metadata

		for (String uuid : localUuids.getUUIDs())
			if (!exists(mdList, uuid))
			{
				String id = localUuids.getID(uuid);

				log.debug("  - Removing old metadata with id="+ id);
				dataMan.deleteMetadata(dbms, id);
				dbms.commit();
				this.result.locallyRemoved++;
			}

		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for(Iterator i=mdList.iterator(); i.hasNext(); )
		{
			Element info = ((Element) i.next()).getChild("info", Edit.NAMESPACE);

			String remoteId  = info.getChildText("id");
			String remoteUuid= info.getChildText("uuid");
			String schema    = info.getChildText("schema");
			String changeDate= info.getChildText("changeDate");

			this.result.totalMetadata++;

			log.debug("Obtained remote id="+ remoteId +", changeDate="+ changeDate);

			if (!dataMan.existsSchema(schema))
			{
				log.debug("  - Skipping unsupported schema : "+ schema);
				this.result.schemaSkipped++;
			}
			else
			{
				String id = dataMan.getMetadataId(dbms, remoteUuid);

				File mefFile = retrieveMEF(remoteUuid);

				try
				{
					if (id == null)	id = addMetadata(siteId, info, mefFile);
						else				updateMetadata(siteId, info, id, mefFile);
				}
				finally
				{
					mefFile.delete();
				}

				dbms.commit();
				dataMan.indexMetadata(dbms, id);
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

	private String addMetadata(final String siteId, final Element info,
										File mefFile) throws Exception
	{
		final String id[] = { "" };

		//--- import metadata from MEF file

		MEFLib.visit(mefFile, new MEFVisitor()
		{
			public void handleMetadata(Element md) throws Exception
			{
				String remoteId   = info.getChildText("id");
				String remoteUuid = info.getChildText("uuid");
				String schema     = info.getChildText("schema");
				String createDate = info.getChildText("createDate");
				String changeDate = info.getChildText("changeDate");
				String isTemplate = info.getChildText("isTemplate");

				log.debug("  - Adding metadata with remote id="+ remoteId);

				id[0] = dataMan.insertMetadataExt(dbms, schema, md, context.getSerialFactory(),
															 siteId, createDate, changeDate, remoteUuid, null);

				int iId = Integer.parseInt(id[0]);

				dataMan.setTemplate(dbms, iId, isTemplate, null);
				dataMan.setHarvestedBit(dbms, iId, true);

				String pubDir = Lib.resource.getDir(context, "public",  id[0]);
				String priDir = Lib.resource.getDir(context, "private", id[0]);

				new File(pubDir).mkdirs();
				new File(priDir).mkdirs();

				result.addedMetadata++;
			}

			//--------------------------------------------------------------------

			public void handleInfo(Element info) throws Exception
			{
				addCategories(id[0], info.getChild("categories"));
				addPrivileges(id[0], info.getChild("privileges"));
			}

			//--------------------------------------------------------------------

			public void handlePublicFile(String file, String changeDate, InputStream is) throws IOException
			{
				log.debug("    - Adding remote public file with name="+ file);
				String pubDir = Lib.resource.getDir(context, "public", id[0]);

				File outFile = new File(pubDir, file);
				FileOutputStream os = new FileOutputStream(outFile);
				BinaryFile.copy(is, os, false, true);
				outFile.setLastModified(new ISODate(changeDate).getSeconds() * 1000);
			}

			//--------------------------------------------------------------------

			public void handlePrivateFile(String file, String changeDate, InputStream is) {}
		});

		return id[0];
	}

	//--------------------------------------------------------------------------
	//--- Categories
	//--------------------------------------------------------------------------

	private void addCategories(String id, Element categ) throws Exception
	{
		List list = categ.getChildren("category");

		for(Iterator j=list.iterator(); j.hasNext();)
		{
			String catName = ((Element) j.next()).getAttributeValue("name");
			String catId   = localCateg.getID(catName);

			if (catId != null)
			{
				//--- remote category exists locally

				log.debug("    - Setting category : "+ catName);
				dataMan.setCategory(dbms, id, catId);
			}

			else if (params.createCateg)
			{
				//--- the remote category does not exist locally : create it

				log.debug("    - Creating local category : "+ catName);
				catId = createCategory(catName) +"";

				log.debug("    - Setting category : "+ catName);
				dataMan.setCategory(dbms, id, catId);
			}
		}
	}

	//--------------------------------------------------------------------------

	private int createCategory(String name) throws Exception
	{
		Map<String, String> hm = hmRemoteCateg.get(name);

		if (hm == null)
			throw new BadParameterEx("Specified category was not found remotely", name);

		int id = context.getSerialFactory().getSerial(dbms, "Categories");

		dbms.execute("INSERT INTO Categories(id, name) VALUES (?, ?)", id, name);
		Lib.local.insert(dbms, "Categories", id, hm, "<"+name+">");

		localCateg.add(name, id+"");

		return id;
	}

	//--------------------------------------------------------------------------
	//--- Privileges
	//--------------------------------------------------------------------------

	private void addPrivileges(String id, Element privil) throws Exception
	{
		List list = privil.getChildren("group");

		for (int i=0; i<list.size(); i++)
		{
			Element group   = (Element) list.get(i);
			String  grpName = group.getAttributeValue("name");
			String  grpId   = localGroups.getID(grpName);

			if (grpId != null)
			{
				//--- remote group exists locally

				log.debug("    - Setting privileges for group : "+ grpName);
				addOperations(group, id, grpId);
			}

			else if (params.createGroups)
			{
				//--- the remote group does not exist locally : create it

				log.debug("    - Creating local group : "+ grpName);
				grpId = createGroup(grpName) +"";

				log.debug("    - Setting privileges for group : "+ grpName);
				addOperations(group, id, grpId);
			}
		}
	}

	//--------------------------------------------------------------------------

	private void addOperations(Element group, String id, String grpId) throws Exception
	{
		List opers = group.getChildren("operation");

		for (int j=0; j<opers.size(); j++)
		{
			Element oper   = (Element) opers.get(j);
			String  opName = oper.getAttributeValue("name");

			int opId = AccessManager.getPrivilegeId(opName);

			log.debug("       --> "+ opName);
			dataMan.setOperation(dbms, id, grpId, opId +"");
		}
	}

	//--------------------------------------------------------------------------

	private int createGroup(String name) throws Exception
	{
		Map<String, String> hm = hmRemoteGroups.get(name);

		if (hm == null)
			throw new BadParameterEx("Specified group was not found remotely", name);

		int id = context.getSerialFactory().getSerial(dbms, "Groups");

		dbms.execute("INSERT INTO Groups(id, name) VALUES (?, ?)", id, name);
		Lib.local.insert(dbms, "Groups", id, hm, "<"+name+">");

		localGroups.add(name, id +"");

		return id;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : updateMetadata
	//---
	//--------------------------------------------------------------------------

	private void updateMetadata(String siteId, Element info, final String id,
										 File mefFile) throws Exception
	{
		//String remoteId  = info.getChildText("id");
		final String remoteUuid= info.getChildText("uuid");
		final String changeDate= info.getChildText("changeDate");

		final Element thumbs[] = { null };

		if (localUuids.getID(remoteUuid) == null)
		{
			log.error("  - Warning! The remote uuid '"+ remoteUuid +"' does not belong to site '"+ siteId+"'");
			log.error("     - The site id of this metadata has been changed.");
			log.error("     - The metadata update will be skipped.");

			result.uuidSkipped++;
		}
		else
		{
			MEFLib.visit(mefFile, new MEFVisitor()
			{
				public void handleMetadata(Element md) throws Exception
				{
					updateMetadata(id, md, remoteUuid, changeDate);
				}

				//-----------------------------------------------------------------

				public void handleInfo(Element info) throws Exception
				{
					updateCategories(id, info.getChild("categories"));
					updatePrivileges(id, info.getChild("privileges"));
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
	}

	//--------------------------------------------------------------------------

	private void updateMetadata(String id, Element md, String remoteUuid,
										 String changeDate) throws Exception
	{
		String date = localUuids.getChangeDate(remoteUuid);

		if (!updateCondition(date, changeDate))
		{
			log.debug("  - XML not changed to local metadata with id="+ id);
			result.unchangedMetadata++;
		}
		else
		{
			log.debug("  - Updating local metadata with id="+ id);
			dataMan.updateMetadataExt(dbms, id, md, changeDate);

			result.updatedMetadata++;
		}
	}

	//--------------------------------------------------------------------------

	private void updateCategories(String id, Element categs) throws Exception
	{
		List catList = categs.getChildren("category");

		//--- remove old categories

		List locCateg = dataMan.getCategories(dbms, id).getChildren();

		for (int i=0; i<locCateg.size(); i++)
		{
			Element el = (Element) locCateg.get(i);

			String catId   = el.getChildText("id");
			String catName = el.getChildText("name");

			if (!existsCategory(catList, catName))
			{
				log.debug("  - Unsetting category : "+ catName);
				dataMan.unsetCategory(dbms, id, catId);
			}
		}

		//--- add new categories

		for (Iterator j=catList.iterator(); j.hasNext();)
		{
			Element categ   = (Element) j.next();
			String  catName = categ.getAttributeValue("name");
			String  catId   = localCateg.getID(catName);

			if (catId != null)
			{
				//--- it is not necessary to query the db. Anyway...
				if (!dataMan.isCategorySet(dbms, id, catId))
				{
					log.debug("  - Setting category : "+ catName);
					dataMan.setCategory(dbms, id, catId);
				}
			}

			else if (params.createCateg)
			{
				//--- the remote category does not exist locally : create it

				log.debug("  - Creating local category : "+ catName);
				catId = createCategory(catName) +"";

				log.debug("  - Setting category : "+ catName);
				dataMan.setCategory(dbms, id, catId);
			}
		}
	}

	//--------------------------------------------------------------------------

	private boolean existsCategory(List catList, String name)
	{
		for(Iterator i=catList.iterator(); i.hasNext();)
		{
			Element categ   = (Element) i.next();
			String  catName = categ.getAttributeValue("name");

			if (catName.equals(name))
				return true;
		}

		return false;
	}

	//--------------------------------------------------------------------------

	private void updatePrivileges(String id, Element privil) throws Exception
	{
		List grpList = privil.getChildren("group");

		//--- remove old operations

		List locOper = getOperations(dbms, id).getChildren();

		for (int i=0; i<locOper.size(); i++)
		{
			Element oper     = (Element) locOper.get(i);
			String  grpName  = oper.getChildText("grpname");
			String  operName = oper.getChildText("opname");

			int grpId  = Integer.parseInt(oper.getChildText("grpid"));
			int operId = Integer.parseInt(oper.getChildText("opid"));

			if (!existsOperation(grpList, grpName, operName))
			{
				log.debug("  - Unsetting operation : "+ grpName +"/"+ operName);
				dataMan.unsetOperation(dbms, new Integer(id), grpId, operId);
			}
		}

		//--- add new operations

		for (int i=0; i<grpList.size(); i++)
		{
			Element group   = (Element) grpList.get(i);
			String  grpName = group.getAttributeValue("name");
			String  grpId   = localGroups.getID(grpName);

			if (grpId != null)
				updateOperations(group, id, grpId, grpName, locOper);

			else if (params.createGroups)
			{
				//--- the remote group does not exist locally : create it

				log.debug("  - Creating local group : "+ grpName);
				grpId = createGroup(grpName) +"";

				log.debug("  - Setting privileges for group : "+ grpName);
				addOperations(group, id, grpId);
			}
		}
	}

	//--------------------------------------------------------------------------

	private Element getOperations(Dbms dbms, String mdId) throws Exception
	{
		String query = "SELECT G.id as grpId, O.id as opId, G.name AS grpName, O.name as opName "+
							"FROM   Groups G, Operations O, OperationAllowed "+
							"WHERE  G.id=groupId AND O.id=operationId AND metadataId=?";

		return dbms.select(query, new Integer(mdId));
	}

	//--------------------------------------------------------------------------

	private void updateOperations(Element group, String id, String grpId,
											String grpName, List locOper) throws Exception
	{
		List opers = group.getChildren("operation");

		for (int j=0; j<opers.size(); j++)
		{
			Element oper   = (Element) opers.get(j);
			String  opName = oper.getAttributeValue("name");
			String  opId   = AccessManager.getPrivilegeId(opName) +"";

			if (!isOperationSet(locOper, grpId, opId))
			{
				log.debug("  - Setting operation : "+ grpName +"/"+ opName);
				dataMan.setOperation(dbms, id, grpId, opId +"");
			}
		}
	}

	//--------------------------------------------------------------------------

	private boolean existsOperation(List list, String grpName, String operName)
	{
		for (int i=0; i<list.size(); i++)
		{
			Element group = (Element) list.get(i);

			if (grpName.equals(group.getAttributeValue("name")))
			{
				List operList = group.getChildren("operation");

				for (int j=0; j<operList.size(); j++)
				{
					Element oper = (Element) operList.get(j);

					if (operName.equals(oper.getAttributeValue("name")))
						return true;
				}

				//--- there can be only 1 group with a given name
				return false;
			}
		}

		return false;
	}

	//--------------------------------------------------------------------------

	private boolean isOperationSet(List locOpers, String grpId, String operId)
	{
		for (int i=0; i<locOpers.size(); i++)
		{
			Element oper = (Element) locOpers.get(i);

			String gId = oper.getChildText("grpid");
			String oId = oper.getChildText("opid");

			if (grpId.equals(gId) && operId.equals(oId))
				return true;
		}

		return false;
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
			log.debug("  - Adding remote public file with name="+ file);

			FileOutputStream os = new FileOutputStream(locFile);
			BinaryFile.copy(is, os, false, true);
			locFile.setLastModified(remIsoDate.getSeconds() * 1000);
		}
		else
		{
			log.debug("  - Nothing to do to public file with name="+ file);
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	/** Return true if the sourceId is present in the remote site */

	private boolean exists(List mdList, String uuid)
	{
		for(Iterator i=mdList.iterator(); i.hasNext(); )
		{
			Element elInfo = ((Element) i.next()).getChild("info", Edit.NAMESPACE);

			if (uuid.equals(elInfo.getChildText("uuid")))
				return true;
		}

		return false;
	}

	//--------------------------------------------------------------------------

	private File retrieveMEF(String uuid) throws IOException
	{
		req.clearParams();
		req.addParam("uuid",   uuid);
		req.addParam("format", "partial");

		req.setAddress("/"+ params.servlet +"/srv/en/"+ Geonet.Service.MEF_EXPORT);

		File tempFile = File.createTempFile("temp-", ".dat");
		req.executeLarge(tempFile);

		return tempFile;
	}

	//--------------------------------------------------------------------------

	private boolean updateCondition(String localDate, String remoteDate)
	{
		ISODate local = new ISODate(localDate);
		ISODate remote= new ISODate(remoteDate);

		//--- accept if remote date is greater than local date

		return (remote.sub(local) > 0);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private Dbms           dbms;
	private Logger         log;
	private XmlRequest     req;
	private GeonetParams   params;
	private DataManager    dataMan;
	private ServiceContext context;
	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
	private UUIDMapper     localUuids;
	private AlignerResult  result;

	private HashMap<String, HashMap<String, String>> hmRemoteCateg  = new HashMap<String, HashMap<String, String>>();
	private HashMap<String, HashMap<String, String>> hmRemoteGroups = new HashMap<String, HashMap<String, String>>();
}

//=============================================================================

class AlignerResult
{
	public String siteId;

	public int totalMetadata;
	public int addedMetadata;
	public int updatedMetadata;
	public int unchangedMetadata;
	public int locallyRemoved;
	public int schemaSkipped;
	public int uuidSkipped;
}

//=============================================================================

