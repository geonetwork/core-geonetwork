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

package org.fao.geonet.kernel.harvest;

import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;
import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.JeevesException;
import jeeves.exceptions.MissingParameterEx;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.Common.OperResult;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarversterJobListener;
import org.fao.geonet.kernel.harvest.harvester.HarvesterHistoryDao;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;
import org.quartz.SchedulerException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

//=============================================================================

public class HarvestManager
{
	//---------------------------------------------------------------------------
	//---              searchProfiles
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public HarvestManager(ServiceContext context, GeonetContext gc, SettingManager sm, DataManager dm) throws Exception
	{
		this.context = context;
        this.readOnly = gc.isReadOnly();
        System.out.println("HarvesterManager initalizing, READONLYMODE is " + this.readOnly);
		xslPath    = context.getAppPath() + Geonet.Path.STYLESHEETS+ "/xml/harvesting/";
		settingMan = sm;
		dataMan    = dm;

		AbstractHarvester.staticInit(context);
		AbstractHarvester.getScheduler().getListenerManager().addJobListener(new HarversterJobListener(this), jobGroupEquals(AbstractHarvester.HARVESTER_GROUP_NAME));
		
		Element entries = settingMan.get("harvesting", -1).getChild("children");

		if (entries != null)
			for (Object o : entries.getChildren())
			{
				Element node = transform((Element) o);
				String  type = node.getAttributeValue("type");

				AbstractHarvester ah = AbstractHarvester.create(type, context, sm, dm);
				ah.init(node);
				hmHarvesters.put(ah.getID(), ah);
				hmHarvestLookup.put(ah.getParams().uuid, ah);
			}
	}

	//---------------------------------------------------------------------------

	private Element transformSort(Element nodes, String sortField) throws Exception
	{
		Map<String,String> params = new HashMap<String,String>();
		params.put("sortField", sortField);

		return Xml.transform(nodes, xslPath + Geonet.File.SORT_HARVESTERS, params);
	}

	//---------------------------------------------------------------------------

	private Element transform(Element node) throws Exception
	{
		String type = node.getChildText("value");

		node = (Element) node.clone();

		return Xml.transform(node, xslPath + type +".xsl");
	}

	//---------------------------------------------------------------------------

	public void shutdown() 
	{
		for (AbstractHarvester ah : hmHarvesters.values()) {
	        try {
                ah.shutdown();
            } catch (SchedulerException e) {
               Log.error(Geonet.HARVEST_MAN, "Error shutting down"+ah.getID(), e);
            }
		}
        try {
            AbstractHarvester.shutdownScheduler();
        } catch (SchedulerException e) {
           Log.error(Geonet.HARVEST_MAN, "Error shutting down harvester scheduler");
        }
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public Element get(String id, String sort) throws Exception
	{
		Element result = (id == null)
									? settingMan.get("harvesting", -1)
									: settingMan.get("harvesting/id:"+id, -1);

		if (result == null)
			return null;

		if (id != null)
		{
			result = transform(result);
			addInfo(result);
		}

		else
		{
			Element nodes = result.getChild("children");

			result = new Element("nodes");

			if (nodes != null) {
				for (Object o : nodes.getChildren()) {
					Element node = transform((Element) o);
					addInfo(node);
					result.addContent(node);
				}

				// sort according to sort field
				if (sort != null) result = transformSort(result,sort);

			}

		}

		return result;
	}

	//---------------------------------------------------------------------------

	public String add(Dbms dbms, Element node) throws JeevesException, SQLException
	{
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Adding harvesting node : \n"+ Xml.getString(node));

		String type = node.getAttributeValue("type");

		AbstractHarvester ah = AbstractHarvester.create(type, context, settingMan, dataMan);

		ah.add(dbms, node);
		hmHarvesters.put(ah.getID(), ah);
		hmHarvestLookup.put(ah.getParams().uuid, ah);
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Added node with id : \n"+ ah.getID());

		return ah.getID();
	}

	public String add2(Dbms dbms, Element node) throws JeevesException, SQLException
	{
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Adding harvesting node : \n"+ Xml.getString(node));

		String type = node.getAttributeValue("type");

		AbstractHarvester ah = AbstractHarvester.create(type, context, settingMan, dataMan);

		ah.add(dbms, node);
		hmHarvesters.put(ah.getID(), ah);
		hmHarvestLookup.put(ah.getParams().uuid, ah);
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "HarvestManager added node with id: "+ ah.getID() + " and uuid: " + ah.getParams().uuid);
		return ah.getParams().uuid;
	}

	//---------------------------------------------------------------------------

	public synchronized String createClone(Dbms dbms, String id) throws Exception
	{
		// get the specified harvester from the settings table
		Element node = get(id, null);
		if (node == null) return null;

		// remove info from the harvester we will clone
		Element info = node.getChild("info");
		if (info != null) info.removeContent();
		Element site = node.getChild("site");
		if (site != null) {
			Element name = site.getChild("name");
			if (name != null) {
				String nameStr = name.getText();
				name.setText("clone: "+nameStr);
			}
		}

        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Cloning harvesting node : \n"+ Xml.getString(node));

		// now add a new harvester based on the settings in the old
		return add(dbms, node);
	}

	//---------------------------------------------------------------------------

	public synchronized boolean update(Dbms dbms, Element node) throws BadInputEx, SQLException, SchedulerException
	{
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Updating harvesting node : \n"+ Xml.getString(node));

		String id = node.getAttributeValue("id");

		if (id == null)
			throw new MissingParameterEx("attribute:id", node);

		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null)
			return false;

		ah.update(dbms, node);
		return true;
	}

	//---------------------------------------------------------------------------
	/** This method must be synchronized because it cannot run if we are updating some entries */

	public synchronized OperResult remove(Dbms dbms, String id) throws Exception
	{
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Removing harvesting with id : "+ id);

		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null)
			return OperResult.NOT_FOUND;

		hmHarvestLookup.remove(ah.getParams().uuid);
		ah.destroy(dbms);
		hmHarvesters.remove(id);
		settingMan.remove(dbms, "harvesting/id:"+id);

		// set deleted status in harvest history table to 'y'
		HarvesterHistoryDao.setDeleted(dbms, ah.getParams().uuid);

		return OperResult.OK;
	}

	//---------------------------------------------------------------------------

	public OperResult start(Dbms dbms, String id) throws SQLException, SchedulerException
	{
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Starting harvesting with id : "+ id);

		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null)
			return OperResult.NOT_FOUND;

		return ah.start(dbms);
	}

	//---------------------------------------------------------------------------

	public OperResult stop(Dbms dbms, String id) throws SQLException, SchedulerException
	{
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Stopping harvesting with id : "+ id);

		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null)
			return OperResult.NOT_FOUND;

		return ah.stop(dbms);
	}

	//---------------------------------------------------------------------------

	public OperResult run(Dbms dbms, String id) throws SQLException, SchedulerException {
        // READONLYMODE
        if(!this.readOnly) {
            if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
                Log.debug(Geonet.HARVEST_MAN, "Running harvesting with id: "+ id);
            }
            AbstractHarvester ah = hmHarvesters.get(id);

            if (ah == null)
                return OperResult.NOT_FOUND;

            return ah.run(dbms);
        }
        else {
            if(Log.isDebugEnabled(Geonet.HARVEST_MAN)){
                Log.debug(Geonet.HARVEST_MAN, "GeoNetwork is running in read-only mode: skipping run of harvester with id: "+ id);
            }
            return null;
        }
	}

	//---------------------------------------------------------------------------

	public OperResult invoke(ResourceManager resourceManager, String id) {
        // READONLYMODE
        if(!this.readOnly) {
            if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
                Log.debug(Geonet.HARVEST_MAN, "Invoking harvester with id: "+ id);
            }

            AbstractHarvester ah = hmHarvesters.get(id);

            if (ah == null)
                return OperResult.NOT_FOUND;

            return ah.invoke(resourceManager);
        }
        else {
            if(Log.isDebugEnabled(Geonet.HARVEST_MAN)){
                Log.debug(Geonet.HARVEST_MAN, "GeoNetwork is running in read-only mode: skipping invocation of harvester with id: "+ id);
            }
            return null;
        }
	}

	//---------------------------------------------------------------------------

	public Element getHarvestInfo(String harvestUuid, String id, String uuid)
	{
		Element info = new Element(Edit.Info.Elem.HARVEST_INFO);

		AbstractHarvester ah = hmHarvestLookup.get(harvestUuid);

		if (ah != null)
			ah.addHarvestInfo(info, id, uuid);

		return info;
	}

	//---------------------------------------------------------------------------

	public AbstractHarvester getHarvester(String harvestUuid)
	{
		return hmHarvestLookup.get(harvestUuid);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void addInfo(Element node)
	{
		String id = node.getAttributeValue("id");
		hmHarvesters.get(id).addInfo(node);
	}

    public boolean isReadOnly() {
        System.out.println("HarvestManager: readOnly is " + readOnly);
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        System.out.println("HarvestManager setting readOnly to " + readOnly);
        this.readOnly = readOnly;
    }
//---------------------------------------------------------------------------
	//---
	//--- Vars
	//---
	//---------------------------------------------------------------------------

	private String         xslPath;
	private SettingManager settingMan;
	private DataManager    dataMan;
	private ServiceContext context;
    private boolean readOnly;

	private HashMap<String, AbstractHarvester> hmHarvesters   = new HashMap<String, AbstractHarvester>();
	private HashMap<String, AbstractHarvester> hmHarvestLookup= new HashMap<String, AbstractHarvester>();
}