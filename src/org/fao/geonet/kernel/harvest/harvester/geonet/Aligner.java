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

import static org.fao.geonet.kernel.harvest.harvester.geonet.GeonetConsts.*;

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

	public void align(Element result, String siteId) throws Exception
	{
		log.info("Start of alignment for site-id : "+ siteId);

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

				log.debug("  - Removing metadata with id="+ id);
				dataMan.deleteMetadata(dbms, id);
				dbms.commit();
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

			log.debug("Obtained remote id="+ remoteId +", changeDate="+ changeDate);

			if (!dataMan.existsSchema(schema))
				log.debug("  - Skipping unsupported schema : "+ schema);
			else
			{
				String id = dataMan.getMetadataId(dbms, remoteUuid);

				if (id == null)	addMetadata(siteId, info);
					else				updateMetadata(siteId, info, id);

				dbms.commit();
				dataMan.indexMetadata(dbms, id);
			}
		}

		log.info("End of alignment for site-id : "+ siteId);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : addMetadata
	//---
	//--------------------------------------------------------------------------

	private void addMetadata(final String siteId, final Element info) throws Exception
	{
		String remoteUuid = info.getChildText("uuid");
		File   mefFile    = retrieveMEF(remoteUuid);

		final String id[] = { "" };

		MEFLib.visit(mefFile, new MEFVisitor()
		{
			public void handleMetadata(Element md) throws Exception
			{
				String remoteUuid = info.getChildText("uuid");
				String schema     = info.getChildText("schema");
				String createDate = info.getChildText("createDate");
				String changeDate = info.getChildText("changeDate");

				boolean isTemplate = "y".equals(info.getChildText("isTemplate"));

				log.debug("  - Adding remote metadata with uuid="+ remoteUuid);

				id[0] = dataMan.insertMetadataExt(dbms, schema, md, context.getSerialFactory(),
															 siteId, createDate, changeDate, remoteUuid, null);

				dataMan.setTemplateBit (dbms, id[0], isTemplate);
				dataMan.setHarvestedBit(dbms, id[0], true);

				String pubDir = Lib.resource.getDir(context, "public",  id[0]);
				String priDir = Lib.resource.getDir(context, "private", id[0]);

				new File(pubDir).mkdirs();
				new File(priDir).mkdirs();
			}

			//--------------------------------------------------------------------

			public void handleInfo(Element md) throws Exception
			{
				addCategories(id[0], md.getChild("categories"));
				addPrivileges(id[0], md.getChild("privileges"));
			}

			//--------------------------------------------------------------------

			public void handleThumbnail(String file, InputStream is) throws IOException
			{
				log.debug("    - Adding remote thumbnail with name="+ file);
				String pubDir = Lib.resource.getDir(context, "public", id[0]);

				FileOutputStream os = new FileOutputStream(new File(pubDir, file));
				BinaryFile.copy(is, os, false, true);
			}

			//--------------------------------------------------------------------

			public void handleData(String file, InputStream is) throws IOException {}
		});
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

		return id;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : updateMetadata
	//---
	//--------------------------------------------------------------------------

	private void updateMetadata(String siteId, Element info, String id) throws Exception
	{
		String remoteId  = info.getChildText("id");
		String remoteUuid= info.getChildText("uuid");
		String changeDate= info.getChildText("changeDate");

		List catList = info.getChildren("category");

		if (localUuids.getID(remoteUuid) == null)
		{
			log.error("  - Warning! The remote uuid '"+ remoteUuid +"' does not belong to site '"+ siteId+"'");
			log.error("     - The site id of this metadata has been changed.");
			log.error("     - The metadata update will be skipped.");
		}
		else
		{
			String date = localUuids.getChangeDate(remoteUuid);

			if (updateCondition(date, changeDate))
			{
//				dataMan.updateMetadataExt(dbms, id, getRemoteMetadata(req, remoteId), changeDate);

				log.debug("  - Updated local metadata with id="+ id);
			}
			else
			{
				log.debug("  - Nothing to do to local metadata with id="+ id);
			}

			//--- removing categories

			List locCateg = dataMan.getCategories(dbms, id).getChildren();

			for(int c=0; c<locCateg.size(); c++)
			{
				Element el = (Element) locCateg.get(c);

				String catId   = el.getChildText("id");
				String catName = el.getChildText("name").toLowerCase();

				if (!existsCategory(catList, catName))
					if (dataMan.isCategorySet(dbms, id, catId))
					{
						dataMan.unsetCategory(dbms, id, catId);
						log.debug("    - Unset category : "+ catName +" for id : "+id);
					}
			}

			//--- checking categories

			for(Iterator j=catList.iterator(); j.hasNext();)
			{
				String catName = ((Element) j.next()).getText();
				String catId   = localCateg.getID(catName);

				if (catId != null)
					if (!dataMan.isCategorySet(dbms, id, catId))
					{
						dataMan.setCategory(dbms, id, catId);
						log.debug("    - Set category : "+ catName +" for id : "+id);
					}
			}
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

//	private Element getRemoteMetadata(XmlRequest req, String id) throws Exception
//	{
//		req.clearParams();
//		req.addParam("id", id);
//
//		Element elMetadata = req.execute();//si.get);
//
//		Element elInfo = elMetadata.getChild("info", Edit.NAMESPACE);
//
//		if (elInfo != null)
//			elInfo.detach();
//
//		return elMetadata;
//
//	}

	private File retrieveMEF(String uuid) throws IOException
	{
		req.clearParams();
		req.addParam("uuid",   uuid);
		req.addParam("format", "partial");

		req.setAddress("/"+ params.servlet +"/srv/en/"+ SERVICE_MEF_EXPORT);

		return req.executeLarge();
	}

	//--------------------------------------------------------------------------

	private boolean existsCategory(List catList, String name)
	{
		for(Iterator i=catList.iterator(); i.hasNext();)
		{
			String catName = ((Element) i.next()).getText();

			if (catName.equals(name))
				return true;
		}

		return false;
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

	private HashMap<String, HashMap<String, String>> hmRemoteCateg  = new HashMap<String, HashMap<String, String>>();
	private HashMap<String, HashMap<String, String>> hmRemoteGroups = new HashMap<String, HashMap<String, String>>();
}

//=============================================================================

