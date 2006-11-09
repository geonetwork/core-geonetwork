//=============================================================================

package org.fao.geonet.schedules;
import org.fao.geonet.kernel.harvest.harvester.*;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Schedule;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ScheduleContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.utils.XmlRequest;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

//=============================================================================

public class MetadataSync
{
	//--------------------------------------------------------------------------
	//---
	//--- Alignment method
	//---
	//--------------------------------------------------------------------------

	private void alignSite(ScheduleContext context, DataManager dm, Dbms dbms,
								  Element result, SiteInfo si, XmlRequest req,
								  CategMapping mapCategories, String siteId) throws Exception
	{
		context.info("Start of alignment for site-id : "+ siteId);

		List mdList = result.getChildren("metadata");

		//-----------------------------------------------------------------------
		//--- retrieve local uuids for given site-id

		UuidMapping mapUuid = new UuidMapping(dbms, siteId);

		//-----------------------------------------------------------------------
		//--- remove old metadata

		for (Iterator i=mapUuid.getUuids(); i.hasNext();)
		{
			String uuid = (String) i.next();

			if (!exists(mdList, uuid))
			{
				String id = mapUuid.getId(uuid);

				dm.deleteMetadata(dbms, id);
				dbms.commit();
				context.debug("  - Removed metadata with id="+ id);
			}
		}

		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for(Iterator i=mdList.iterator(); i.hasNext(); )
		{
			Element elInfo = ((Element) i.next()).getChild("info", Edit.NAMESPACE);

			String remoteId  = elInfo.getChildText("id");
			String remoteUuid= elInfo.getChildText("uuid");
			String schema    = elInfo.getChildText("schema");
			String createDate= elInfo.getChildText("createDate");
			String changeDate= elInfo.getChildText("changeDate");

			List catList = elInfo.getChildren("category");

			context.debug("Obtained remote id="+ remoteId +", changeDate="+ changeDate);

			if (!dm.existsSchema(schema))
				context.debug("  - Skipping unsupported schema : "+ schema);
			else
			{
				String id = getLocalId(dbms, remoteUuid);

				if (id == null)
				{
					//--- inserting new metadata

					id = dm.insertMetadataExt(dbms, schema, getRemoteMetadata(req, remoteId, si),
													  context.getSerialFactory(), siteId,
													  createDate, changeDate, remoteUuid, null);

					context.debug("  - Added metadata with id="+ id);

					//--- store privileges for group

					for(Iterator g=si.groups.iterator(); g.hasNext();)
					{
//						Group group = (Group) g.next();
//
//						context.debug("    - Setting privileges for group : "+group.groupId);
//
//						for(Iterator p=group.privileges.iterator(); p.hasNext(); )
//						{
//							int priv = ((Integer) p.next()).intValue();
//
//							dm.setOperation(dbms, id, group.groupId, priv +"");
//							context.debug("       --> "+ AccessManager.getPrivilegeName(priv));
//						}
					}

					//--- adding categories

					for(Iterator j=catList.iterator(); j.hasNext();)
					{
						String catName = ((Element) j.next()).getText();
						String catId   = mapCategories.getId(catName);

						if (catId != null)
						{
							dm.setCategory(dbms, id, catId);
							context.debug("    - Set category : "+ catName);
						}
					}
				}
				else
				{
					//--- updating an existing metadata

					if (mapUuid.getId(remoteUuid) == null)
					{
						context.error("  - Warning! The remote uuid '"+ remoteUuid +"' does not belong to site '"+ siteId+"'");
						context.error("     - The site id of this metadata has been changed.");
						context.error("     - The metadata update will be skipped.");
					}
					else
					{
						String date = mapUuid.getChangeDate(remoteUuid);

						if (updateCondition(date, changeDate))
						{
							dm.updateMetadataExt(dbms, id, getRemoteMetadata(req, remoteId, si), changeDate);

							context.debug("  - Updated local metadata with id="+ id);
						}
						else
						{
							context.debug("  - Nothing to do to local metadata with id="+ id);
						}

						//--- removing categories

						List locCateg = dm.getCategories(dbms, id).getChildren();

						for(int c=0; c<locCateg.size(); c++)
						{
							Element el = (Element) locCateg.get(c);

							String catId   = el.getChildText("id");
							String catName = el.getChildText("name").toLowerCase();

							if (!existsCategory(catList, catName))
								if (dm.isCategorySet(dbms, id, catId))
								{
									dm.unsetCategory(dbms, id, catId);
									context.debug("    - Unset category : "+ catName +" for id : "+id);
								}
						}

						//--- checking categories

						for(Iterator j=catList.iterator(); j.hasNext();)
						{
							String catName = ((Element) j.next()).getText();
							String catId   = mapCategories.getId(catName);

							if (catId != null)
								if (!dm.isCategorySet(dbms, id, catId))
								{
									dm.setCategory(dbms, id, catId);
									context.debug("    - Set category : "+ catName +" for id : "+id);
								}
						}
					}
				}

				dbms.commit();
				dm.indexMetadata(dbms, id);
			}
		}

		context.info("End of alignment for site-id : "+ siteId);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Return true if the sourceId is present in the remote site
	//---
	//--------------------------------------------------------------------------

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

	private Element getRemoteMetadata(XmlRequest req, String id, SiteInfo si) throws Exception
	{
		req.clearParams();
		req.addParam("id", id);

		Element elMetadata = req.execute();//si.get);

		Element elInfo = elMetadata.getChild("info", Edit.NAMESPACE);

		if (elInfo != null)
			elInfo.detach();

		return elMetadata;

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

	private String getLocalId(Dbms dbms, String uuid) throws Exception
	{
		String query = "SELECT id "+
							"FROM   Metadata "+
							"WHERE  uuid='"+uuid+"'";

		List idsList = dbms.select(query).getChildren();

		if (idsList.size() == 0)
			return null;
		else
		{
			Element record = (Element) idsList.get(0);

			return record.getChildText("id");
		}
	}

	//--------------------------------------------------------------------------

	private boolean updateCondition(String localDate, String remoteDate)
	{
		ISODate local = new ISODate(localDate);
		ISODate remote= new ISODate(remoteDate);

		//--- accept if remote date is greater than local date

		return (remote.sub(local) > 0);
	}
}

//=============================================================================
//===
//=== Store site information
//===
//=============================================================================

class SiteInfo
{
	//--- general

	public String name;
	public String host;
	public int    port;
	public String language;
	public String username;
	public String password;

	//--- services

	public String login;
	public String logout;
	public String search;
	public String get;

	//--- search

	public Vector queries = new Vector();

	//--- group

	public Vector groups = new Vector();
}

//=============================================================================
//===
//=== Create a mapping remote ID -> local ID / change date
//===
//=============================================================================

class UuidMapping
{
	private Hashtable htUuidDate = new Hashtable();
	private Hashtable htUuidId   = new Hashtable();

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public UuidMapping(Dbms dbms, String siteId) throws Exception
	{
		String query = "SELECT id, uuid, changeDate "+
							"FROM   Metadata "+
							"WHERE  source='"+siteId+"'";

		List idsList = dbms.select(query).getChildren();

		for (int i=0; i<idsList.size(); i++)
		{
			Element record = (Element) idsList.get(i);

			String id   = record.getChildText("id");
			String uuid = record.getChildText("uuid");
			String date = record.getChildText("changedate");

			htUuidDate.put(uuid, date);
			htUuidId  .put(uuid, id);
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public String getChangeDate(String uuid) { return (String) htUuidDate.get(uuid); }

	//--------------------------------------------------------------------------

	public String getId(String uuid) { return (String) htUuidId.get(uuid); }

	//--------------------------------------------------------------------------

	public Iterator getUuids() { return htUuidDate.keySet().iterator(); }
}

//=============================================================================

