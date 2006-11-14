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

import java.util.Iterator;
import java.util.List;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.XmlRequest;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
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

	//--------------------------------------------------------------------------
	//---
	//--- Alignment method
	//---
	//--------------------------------------------------------------------------

	public void align(Logger log, DataManager dm, Dbms dbms,
								  Element result, XmlRequest req,
								  CategoryMapper mapCategories, String siteId) throws Exception
	{
		log.info("Start of alignment for site-id : "+ siteId);

		List mdList = result.getChildren("metadata");

		//-----------------------------------------------------------------------
		//--- retrieve local uuids for given site-id

		UUIDMapper mapUuid = new UUIDMapper(dbms, siteId);

		//-----------------------------------------------------------------------
		//--- remove old metadata

		for (Iterator i=mapUuid.getUUIDs(); i.hasNext();)
		{
			String uuid = (String) i.next();

			if (!exists(mdList, uuid))
			{
				String id = mapUuid.getID(uuid);

				dm.deleteMetadata(dbms, id);
				dbms.commit();
				log.debug("  - Removed metadata with id="+ id);
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

			log.debug("Obtained remote id="+ remoteId +", changeDate="+ changeDate);

			if (!dm.existsSchema(schema))
				log.debug("  - Skipping unsupported schema : "+ schema);
			else
			{
				String id = getLocalId(dbms, remoteUuid);

				if (id == null)
				{
					//--- inserting new metadata

//					id = dm.insertMetadataExt(dbms, schema, getRemoteMetadata(req, remoteId),
//													  context.getSerialFactory(), siteId,
//													  createDate, changeDate, remoteUuid, null);

					log.debug("  - Added metadata with id="+ id);

					//--- store privileges for group

//					for(Iterator g=si.groups.iterator(); g.hasNext();)
//					{
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
//					}

					//--- adding categories

					for(Iterator j=catList.iterator(); j.hasNext();)
					{
						String catName = ((Element) j.next()).getText();
						String catId   = mapCategories.getID(catName);

						if (catId != null)
						{
							dm.setCategory(dbms, id, catId);
							log.debug("    - Set category : "+ catName);
						}
					}
				}
				else
				{
					//--- updating an existing metadata

					if (mapUuid.getID(remoteUuid) == null)
					{
						log.error("  - Warning! The remote uuid '"+ remoteUuid +"' does not belong to site '"+ siteId+"'");
						log.error("     - The site id of this metadata has been changed.");
						log.error("     - The metadata update will be skipped.");
					}
					else
					{
						String date = mapUuid.getChangeDate(remoteUuid);

						if (updateCondition(date, changeDate))
						{
							dm.updateMetadataExt(dbms, id, getRemoteMetadata(req, remoteId), changeDate);

							log.debug("  - Updated local metadata with id="+ id);
						}
						else
						{
							log.debug("  - Nothing to do to local metadata with id="+ id);
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
									log.debug("    - Unset category : "+ catName +" for id : "+id);
								}
						}

						//--- checking categories

						for(Iterator j=catList.iterator(); j.hasNext();)
						{
							String catName = ((Element) j.next()).getText();
							String catId   = mapCategories.getID(catName);

							if (catId != null)
								if (!dm.isCategorySet(dbms, id, catId))
								{
									dm.setCategory(dbms, id, catId);
									log.debug("    - Set category : "+ catName +" for id : "+id);
								}
						}
					}
				}

				dbms.commit();
				dm.indexMetadata(dbms, id);
			}
		}

		log.info("End of alignment for site-id : "+ siteId);
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

	private Element getRemoteMetadata(XmlRequest req, String id) throws Exception
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

