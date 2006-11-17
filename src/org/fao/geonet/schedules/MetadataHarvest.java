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

package org.fao.geonet.schedules;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import jeeves.interfaces.Schedule;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ScheduleContext;
import jeeves.utils.SerialFactory;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

//=============================================================================

public class MetadataHarvest implements Schedule
{
	private class Attr
	{
		private static final String NAME = "name";
	}

	//--------------------------------------------------------------------------

	private class Elem
	{
		private static final String GENERAL         = "general";
		private static final String    BASE_URL     = "baseUrl";
		private static final String FILE            = "file";
		private static final String    URL          = "url";
		private static final String    SCHEMA       = "schema";
		private static final String    SOURCE       = "source";
		private static final String    GROUP        = "group";
		private static final String       PRIVILEGE = "privilege";
		private static final String    CATEGORY     = "category";
	};

	//--------------------------------------------------------------------------

	private String configFile;
	private String baseUrl;

	private HashSet hsUris = new HashSet();

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		configFile = params.getMandatoryValue("configFile");
	}

	//--------------------------------------------------------------------------
	//---
	//--- Schedule
	//---
	//--------------------------------------------------------------------------

	public void exec(ScheduleContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getDataManager();

		Dbms dbms   = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		SerialFactory sf = context.getSerialFactory();

		GroupMapper    groupMapping = new GroupMapper(dbms);
		CategoryMapper categMapping = new CategoryMapper(dbms);

		//--- open file and read configuration

		Element config  = Xml.loadFile(configFile);
		Element general = Util.getChild(config, Elem.GENERAL);

		baseUrl = Util.getParam(general, Elem.BASE_URL);

		//--- loop on all files

		List list = config.getChildren(Elem.FILE);

		context.info("Starting harvesting of "+ list.size() +" metadata.");

		hsUris.clear();

		try
		{
			for(int i=0; i<list.size(); i++)
			{
				Element file = (Element) list.get(i);

				String id = updateMetadata(dbms, dataMan, sf, file);

				updateGroups    (dbms, dataMan, groupMapping, id, file.getChildren(Elem.GROUP));
				updateCategories(dbms, dataMan, categMapping, id, file.getChildren(Elem.CATEGORY));

				dataMan.indexMetadata(dbms, id);
			}

			removeOldMetadata(dbms, dataMan);
		}
		catch (Exception e)
		{
			context.error("Raised exception while harvesting. Aborting.");
			context.error("   Message : "+ e.getMessage());
			context.error("   Class   : "+ e.getClass().getName());
			context.error("   Stack   :\n"+ Util.getStackTrace(e));
			dbms.abort();
		}

		context.info("Harvesting ended.");
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private String updateMetadata(Dbms dbms, DataManager dataMan, SerialFactory sf,
											Element file) throws Exception
	{
		String url    = Util.getParam(file, Elem.URL);
		String schema = Util.getParam(file, Elem.SCHEMA);
		String source = Util.getParam(file, Elem.SOURCE);
		String date   = new ISODate().toString();

		URL fullUrl = new URL(baseUrl + url);

		InputStream is = fullUrl.openStream();
		Element     md = Xml.loadStream(is);
		is.close();

		String  id        = null;
		String  uuid      = null;
		String  sourceUri = fullUrl.toString();
		Element info      = getMetadataInfo(dbms, sourceUri);

		hsUris.add(sourceUri);

		if (info == null)
		{
			uuid = dataMan.extractUUID(schema, md);

			if (uuid == null)
				uuid = UUID.randomUUID().toString();

			id = sf.getSerial(dbms, "Metadata") +"";
			md = dataMan.updateFixedInfo(schema, id, md, uuid, source);

			dataMan.insertMetadataExt(dbms, schema, md, Integer.parseInt(id), source, date, date, uuid, sourceUri);
		}
		else
		{
			id   = info.getChildText("id");
			uuid = info.getChildText("uuid");
			md   = dataMan.updateFixedInfo(schema, id, md, uuid, source);

			dataMan.updateMetadataExt(dbms, id, md, date);
		}

		dbms.commit();

		return id;
	}

	//--------------------------------------------------------------------------

	private Element getMetadataInfo(Dbms dbms, String sourceUri) throws Exception
	{
		String query = "SELECT id, uuid FROM Metadata WHERE sourceUri='"+sourceUri+"'";

		List result = dbms.select(query).getChildren();

		return (result.size() == 0) ? null : (Element) result.get(0);
	}

	//--------------------------------------------------------------------------

	private void updateGroups(Dbms dbms, DataManager dataMan, GroupMapper groupMapping,
									  String id, List groups) throws Exception
	{
		for(int i=0; i<groups.size(); i++)
		{
			Element group = (Element) groups.get(i);
			String  name  = group.getAttributeValue(Attr.NAME);
			String  grpId = groupMapping.getID(name);

			if (grpId == null)
				throw new Exception("Group not found : "+ name);

			dbms.execute("DELETE FROM OperationAllowed WHERE metadataId="+id+" AND groupId="+grpId);

			List privs = group.getChildren(Elem.PRIVILEGE);

			for(int j=0; j<privs.size(); j++)
			{
				Element priv = (Element) privs.get(j);

				int privId = AccessManager.getPrivilegeId(priv.getText());

				if (privId == -1)
					throw new Exception("Privilege not found : "+ priv.getText());

				dataMan.setOperation(dbms, id, grpId, privId +"");
			}
		}

		dbms.commit();
	}

	//--------------------------------------------------------------------------

	private void updateCategories(Dbms dbms, DataManager dataMan, CategoryMapper categMapping,
											String id,List categs) throws Exception
	{
		dbms.execute("DELETE FROM MetadataCateg WHERE metadataId="+id);

		for(int i=0; i<categs.size(); i++)
		{
			Element categ = (Element) categs.get(i);
			String  name  = categ.getText();
			String  catId = categMapping.getID(name);

			if (catId == null)
				throw new Exception("Category not found : "+ name);

			dataMan.setCategory(dbms, id, catId);
		}

		dbms.commit();
	}

	//--------------------------------------------------------------------------

	private void removeOldMetadata(Dbms dbms, DataManager dataMan) throws Exception
	{
		String query = "SELECT id, sourceUri FROM Metadata WHERE sourceUri IS NOT NULL";

		List list = dbms.select(query).getChildren();

		for(int i=0; i<list.size(); i++)
		{
			Element rec = (Element) list.get(i);

			String id = rec.getChildText("id");
			String uri= rec.getChildText("sourceuri");

			if (!hsUris.contains(uri))
				dataMan.deleteMetadata(dbms, id);
		}
	}
}

//=============================================================================


