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

import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.MissingParameterEx;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.jms.ClusterConfig;
import org.fao.geonet.jms.ClusterException;
import org.fao.geonet.jms.Producer;
import org.fao.geonet.jms.message.harvest.HarvesterMessage;
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

import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

/**
 * TODO javadoc.
 */
public class HarvestManager {
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @param context
     * @param sm
     * @param dm
     * @throws Exception
     */
	public HarvestManager(ServiceContext context, SettingManager sm, DataManager dm) throws Exception {
		this.context = context;

		xslPath    = context.getAppPath() + Geonet.Path.STYLESHEETS+ "/xml/harvesting/";
		settingMan = sm;
		dataMan    = dm;

		AbstractHarvester.staticInit(context);
		AbstractHarvester.getScheduler().getListenerManager().addJobListener(new HarversterJobListener(this), jobGroupEquals(AbstractHarvester.HARVESTER_GROUP_NAME));
        initialize();

	}
    
    public synchronized void initialize() throws Exception {
        Log.debug(Geonet.HARVEST_MAN, "\n\n\n\n\n** initialize\n\n\n\n");
        hmHarvesters.clear();
        hmHarvestLookup.clear();
		
		Element entries = settingMan.get("harvesting", -1).getChild("children");

        if (entries != null) {
			for (Object o : entries.getChildren())
			{
				Element node = transform((Element) o);
				String  type = node.getAttributeValue("type");
                AbstractHarvester ah = AbstractHarvester.create(type, context, settingMan, dataMan);
				ah.init(node);
				hmHarvesters.put(ah.getID(), ah);
				hmHarvestLookup.put(ah.getParams().uuid, ah);
			}
	}
    }


    /**
     * TODO javadoc.
     *
     * @param nodes
     * @param sortField
     * @return
     * @throws Exception
     */
	private Element transformSort(Element nodes, String sortField) throws Exception {
		Map<String,String> params = new HashMap<String,String>();
		params.put("sortField", sortField);

		return Xml.transform(nodes, xslPath + Geonet.File.SORT_HARVESTERS, params);
	}

    /**
     * TODO javadoc.
     *
     * @param node
     * @return
     * @throws Exception
     */
	private Element transform(Element node) throws Exception {
		String type = node.getChildText("value");

		node = (Element) node.clone();

		return Xml.transform(node, xslPath + type +".xsl");
	}


    /**
     * TODO javadoc.
     */
	public synchronized void shutdown() {
		for (AbstractHarvester ah : hmHarvesters.values()) {
	        try {
                ah.shutdown();
                    } 
                    catch (SchedulerException e) {
               Log.error(Geonet.HARVEST_MAN, "Error shutting down"+ah.getID(), e);
            }
		}
        try {
            AbstractHarvester.shutdownScheduler();
                } 
                catch (SchedulerException e) {
           Log.error(Geonet.HARVEST_MAN, "Error shutting down harvester scheduler");
        }
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @param id
     * @param sort
     * @return
     * @throws Exception
     */
	public Element get(String id, String sort) throws Exception {
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

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param node
     * @return
     * @throws Exception
     */
	public synchronized String add(Dbms dbms, Element node)  throws Exception {
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Adding harvesting node : \n"+ Xml.getString(node));

		String type = node.getAttributeValue("type");

		AbstractHarvester ah = AbstractHarvester.create(type, context, settingMan, dataMan);

		ah.add(dbms, node);
		hmHarvesters.put(ah.getID(), ah);
		hmHarvestLookup.put(ah.getParams().uuid, ah);
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Added node with id : \n"+ ah.getID());

        dbms.commit();

        if(ClusterConfig.isEnabled()) {
            try {
                HarvesterMessage message = new HarvesterMessage();
                message.setSenderClientID(ClusterConfig.getClientID());
                Producer harvesterProducer = ClusterConfig.get(Geonet.ClusterMessageTopic.HARVESTER);
                harvesterProducer.produce(message);      
            } 
            catch (ClusterException x) {
                System.err.println(x.getMessage());
                x.printStackTrace();
                throw new Exception(x.getMessage(), x);
            }
        }
		return ah.getID();
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param node
     * @return
     * @throws Exception
     */
	public synchronized String add2(Dbms dbms, Element node) throws Exception {
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Adding harvesting node : \n"+ Xml.getString(node));

		String type = node.getAttributeValue("type");

		AbstractHarvester ah = AbstractHarvester.create(type, context, settingMan, dataMan);

		ah.add(dbms, node);
		hmHarvesters.put(ah.getID(), ah);
		hmHarvestLookup.put(ah.getParams().uuid, ah);
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "HarvestManager added node with id: "+ ah.getID() + " and uuid: " + ah.getParams().uuid);

        dbms.commit();

        if(ClusterConfig.isEnabled()) {
            try {
                HarvesterMessage message = new HarvesterMessage();
                message.setSenderClientID(ClusterConfig.getClientID());
                Producer harvesterProducer = ClusterConfig.get(Geonet.ClusterMessageTopic.HARVESTER);
                harvesterProducer.produce(message);
            }
            catch (ClusterException x) {
                System.err.println(x.getMessage());
                x.printStackTrace();
                throw new Exception(x.getMessage(), x);
            }
        }        
		return ah.getParams().uuid;
	}


    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @return
     * @throws Exception
     */
	public synchronized String createClone(Dbms dbms, String id) throws Exception {
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

    /**
     * TODO javadoc.
     * @param dbms
     * @param node
     * @return
     * @throws BadInputEx
     * @throws SQLException
     */
	public synchronized boolean update(Dbms dbms, Element node) throws BadInputEx, SQLException, Exception {
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Updating harvesting node : \n"+ Xml.getString(node));

		String id = node.getAttributeValue("id");

		if (id == null)
			throw new MissingParameterEx("attribute:id", node);

		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null)
			return false;

		ah.update(dbms, node);

        dbms.commit();

        if(ClusterConfig.isEnabled()) {
            try {
                HarvesterMessage message = new HarvesterMessage();
                message.setSenderClientID(ClusterConfig.getClientID());
                Producer harvesterProducer = ClusterConfig.get(Geonet.ClusterMessageTopic.HARVESTER);
                harvesterProducer.produce(message);
            }
            catch (ClusterException x) {
                System.err.println(x.getMessage());
                x.printStackTrace();
                throw new Exception(x.getMessage(), x);
            }
	}

		return true;
	}

    /**
     * This method must be synchronized because it cannot run if we are updating some entries.
     *
     * @param dbms
     * @param id
     * @return
     * @throws Exception
     */
	public synchronized OperResult remove(Dbms dbms, String id) throws Exception {
		if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "Removing harvesting with id : "+ id);
        }
		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null)
			return OperResult.NOT_FOUND;

		hmHarvestLookup.remove(ah.getParams().uuid);
		ah.destroy(dbms);
		hmHarvesters.remove(id);
		settingMan.remove(dbms, "harvesting/id:"+id);

		// set deleted status in harvest history table to 'y'
		HarvesterHistoryDao.setDeleted(dbms, ah.getParams().uuid);

        dbms.commit();

        if(ClusterConfig.isEnabled()) {
            try {
                HarvesterMessage message = new HarvesterMessage();
                message.setSenderClientID(ClusterConfig.getClientID());
                Producer harvesterProducer = ClusterConfig.get(Geonet.ClusterMessageTopic.HARVESTER);
                harvesterProducer.produce(message);
            }
            catch (ClusterException x) {
                System.err.println(x.getMessage());
                x.printStackTrace();
                throw new Exception(x.getMessage(), x);
            }
	}

		return OperResult.OK;
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @return
     * @throws SQLException
     */
	public synchronized OperResult start(Dbms dbms, String id) throws Exception {
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Starting harvesting with id : "+ id);

		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null)
			return OperResult.NOT_FOUND;

		return ah.start(dbms);
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @return
     * @throws SQLException
     */
	public synchronized OperResult stop(Dbms dbms, String id) throws Exception {
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Stopping harvesting with id : "+ id);

		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null)
			return OperResult.NOT_FOUND;

		return ah.stop(dbms);
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @return
     * @throws SQLException
     */
	public OperResult run(Dbms dbms, String id) throws Exception {
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Running harvesting with id : "+ id);

		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null)  {
            Log.warning(Geonet.HARVEST_MAN, "harvester not found");
			return OperResult.NOT_FOUND;
        }
		return ah.run(dbms);
	}

    /**
     * TODO javadoc.
     *
     * @param resourceManager
     * @param id
     * @return
     */
	public OperResult invoke(ResourceManager resourceManager, String id) {
		if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "Invoking harvester with id : "+ id);
        }

		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null) {
			return OperResult.NOT_FOUND;
        }
		return ah.invoke(resourceManager);
	}

    /**
     * TODO javadoc.
     *
     * @param harvestUuid
     * @param id
     * @param uuid
     * @return
     */
	public Element getHarvestInfo(String harvestUuid, String id, String uuid) {
		Element info = new Element(Edit.Info.Elem.HARVEST_INFO);

		AbstractHarvester ah = hmHarvestLookup.get(harvestUuid);

		if (ah != null)
			ah.addHarvestInfo(info, id, uuid);

		return info;
	}

    /**
     * TODO javadoc.
     *
     * @param harvestUuid
     * @return
     */
	public AbstractHarvester getHarvester(String harvestUuid) {
		return hmHarvestLookup.get(harvestUuid);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @param node
     */
	private void addInfo(Element node) {

		String id = node.getAttributeValue("id");
		hmHarvesters.get(id).addInfo(node);
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

	private HashMap<String, AbstractHarvester> hmHarvesters   = new HashMap<String, AbstractHarvester>();
	private HashMap<String, AbstractHarvester> hmHarvestLookup= new HashMap<String, AbstractHarvester>();
}
