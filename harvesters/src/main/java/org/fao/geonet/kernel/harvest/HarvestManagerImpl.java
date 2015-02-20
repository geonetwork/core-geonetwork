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

import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.HarvestInfoProvider;
import org.fao.geonet.kernel.harvest.Common.OperResult;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarversterJobListener;
import org.fao.geonet.kernel.setting.HarvesterSettingsManager;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TODO Javadoc.
 *
 */
public class HarvestManagerImpl implements HarvestInfoProvider, HarvestManager {

    //---------------------------------------------------------------------------
    //---
    //--- Vars
    //---
    //---------------------------------------------------------------------------
    @Autowired
    private HarvesterSettingsManager settingMan;
    @Autowired
    private DataManager    dataMan;
    private Path xslPath;
    private ServiceContext context;
    private boolean readOnly;

    private Map<String, AbstractHarvester> hmHarvesters   = new HashMap<String, AbstractHarvester>();
    private Map<String, AbstractHarvester> hmHarvestLookup= new HashMap<String, AbstractHarvester>();

	//---------------------------------------------------------------------------
	//---              searchProfiles
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

    /**
     * initialize the manager.
     *
     *
     *
     * @param context service context
     * @throws Exception hmm
     */
	@Override
    public void init(ServiceContext context, boolean isReadOnly) throws Exception {
		this.context = context;
        this.readOnly = isReadOnly;
        Log.debug(Geonet.HARVEST_MAN, "HarvesterManager initializing, READONLYMODE is " + this.readOnly);
		xslPath    = context.getAppPath().resolve(Geonet.Path.STYLESHEETS).resolve("xml/harvesting/");
		AbstractHarvester.getScheduler().getListenerManager().addJobListener(
		        HarversterJobListener.getInstance(this));
		
        final Element harvesting = settingMan.get("harvesting", -1);
        if (harvesting != null) {
            Element entries = harvesting.getChild("children");

            if (entries != null) {
                for (Object o : entries.getChildren()) {
                    Element node = transform((Element) o);
                    String type = node.getAttributeValue("type");

                    AbstractHarvester ah = AbstractHarvester.create(type, context);
                    ah.init(node, context);
                    
                    hmHarvesters.put(ah.getID(), ah);
                    hmHarvestLookup.put(ah.getParams().getUuid(), ah);
                }
            }
        }
    }

    /**
     * TODO Javadoc.
     *
     * @param nodes harvest nodes
     * @param sortField sort field
     * @return sorted harvest nodes
     * @throws Exception hmm
     */
	private Element transformSort(Element nodes, String sortField) throws Exception {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("sortField", sortField);

		return Xml.transform(nodes, xslPath.resolve(Geonet.File.SORT_HARVESTERS), params);
	}

    /**
     * TODO Javadoc.
     *
     * @param node harvest node
     * @return transformed harvest node
     * @throws Exception hmm
     */
	private Element transform(Element node) throws Exception {
		String type = node.getChildText("value");
		node = (Element) node.clone();
		return Xml.transform(node, xslPath.resolve(type +".xsl"));
	}

    /**
     * TODO Javadoc.
     *
     */
	@Override
    public void shutdown() {
		for (AbstractHarvester ah : hmHarvesters.values()) {
	        try {
                ah.shutdown();
            } catch (SchedulerException e) {
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
     * @param id harvester id
     * @param context servicecontext
     * @param sort sort field
     * @return harvest node
     * @throws Exception hmm
     */
	@Override
    public Element get(String id, ServiceContext context, String sort) throws Exception {
		Element result = (id == null)
									? settingMan.get("harvesting", -1)
									: settingMan.get("harvesting/id:"+id, -1);
		if (result == null) {
            return null;
        }

        Profile profile = context.getUserSession().getProfile();
		if (id != null) {
            // you're an Administrator
            if (profile == Profile.Administrator) {
			    result = transform(result);
			    addInfo(result);
		    } else {
                // you're not an Administrator: only return harvest nodes from groups visible to you
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                AccessManager am = gc.getBean(AccessManager.class);
                Set<Integer> groups = am.getVisibleGroups(context.getUserSession().getUserIdAsInt());
                result = transform(result);
                Element nodeGroup =  result.getChild("ownerGroup");
                if ((nodeGroup != null) && (groups.contains(Integer.valueOf(nodeGroup.getValue())))) {
                    addInfo(result);
                } else {
                    return null;
                }
            }
		} else {

            // id is null: return all (visible) nodes
            Element nodes = result.getChild("children");
			result = new Element("nodes");
			if (nodes != null) {
                // you're Administrator: all nodes are visible
                if (profile == Profile.Administrator) {
                    for (Object o : nodes.getChildren()) {
                        Element node = transform((Element) o);
                        addInfo(node);
                        result.addContent(node);
                    }
                } else {
                    // you're not an Adminstrator: only return nodes in groups visible to you
                    GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                    AccessManager am = gc.getBean(AccessManager.class);
                    Set<Integer> groups = am.getVisibleGroups(context.getUserSession().getUserIdAsInt());
				    for (Object o : nodes.getChildren()) {
					    Element node = transform((Element) o);
                        Element nodeGroup =  node.getChild("ownerGroup");
                        if ((nodeGroup != null) && (groups.contains(Integer.valueOf(nodeGroup.getValue())))) {
					        addInfo(node);
					        result.addContent(node);
				        }
                    }
                }
				// sort according to sort field
				if (sort != null) {
                    result = transformSort(result,sort);
                }
			}
		}
		return result;
	}

    /**
     * TODO javadoc.
     *
     * @param node harvester config
     * @param ownerId the id of the user doing this
     * @return id of new harvester
     * @throws JeevesException hmm
     * @throws SQLException hmm
     */
	@Override
    public String addHarvesterReturnId(Element node, String ownerId) throws JeevesException, SQLException {
        if (Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "Adding harvesting node : \n" + Xml.getString(node));
        }
		String type = node.getAttributeValue("type");
		AbstractHarvester ah = AbstractHarvester.create(type, context);

        Element ownerIdE = new Element("ownerId");
        ownerIdE.setText(ownerId);
        node.addContent(ownerIdE);

		ah.add(node);
		hmHarvesters.put(ah.getID(), ah);
		hmHarvestLookup.put(ah.getParams().getUuid(), ah);

        if (Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "Added node with id : \n" + ah.getID());
        }
		return ah.getID();
	}

    /**
     * TODO Javadoc.
     *
     * @param node
     * @return
     * @throws JeevesException
     * @throws SQLException
     */
	@Override
    public String addHarvesterReturnUUID(Element node) throws JeevesException, SQLException {
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "Adding harvesting node : \n" + Xml.getString(node));
        }
		String type = node.getAttributeValue("type");
		AbstractHarvester ah = AbstractHarvester.create(type, context);

		ah.add(node);
		hmHarvesters.put(ah.getID(), ah);
		hmHarvestLookup.put(ah.getParams().getUuid(), ah);

        if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "HarvestManager added node with id: "+ ah.getID() + " and uuid: " + ah.getParams().getUuid());
        }
		return ah.getParams().getUuid();
	}

    /**
     * TODO javadoc.
     *
     * @param id
     * @param ownerId id of the user doing this
     * @return
     * @throws Exception
     */
	@Override
    public synchronized String createClone(String id, String ownerId, ServiceContext context) throws Exception {
		// get the specified harvester from the settings table
		Element node = get(id, context, null);
		if (node == null) return null;

		// remove info from the harvester we will clone
		Element info = node.getChild("info");
		if (info != null) info.removeContent();
		Element site = node.getChild("site");
		if (site != null) {
			Element name = site.getChild("name");
			if (name != null) {
				String nameStr = name.getText();
				// TODO i18n
				name.setText("clone: "+nameStr);
			}
		}

        if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "Cloning harvesting node : \n"+ Xml.getString(node));
        }
        Element ownerIdE = new Element("ownerId");
        ownerIdE.setText(ownerId);
        node.addContent(ownerIdE);

		// now add a new harvester based on the settings in the old
		return addHarvesterReturnId(node, ownerId);
	}
    /**
     * TODO javadoc.
     *
     * @param node
     * @param ownerId id of the user doing this
     * @return
     * @throws BadInputEx
     * @throws SQLException
     * @throws SchedulerException
     */
	@Override
    public synchronized boolean update(Element node, String ownerId) throws BadInputEx, SQLException, SchedulerException {
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "Updating harvesting node : \n"+ Xml.getString(node));
        }
		String id = node.getAttributeValue("id");

		if (id == null) {
			throw new MissingParameterEx("attribute:id", node);
        }
		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null) {
			return false;
        }

        Element ownerIdE = new Element("ownerId");
        ownerIdE.setText(ownerId);
        node.addContent(ownerIdE);

		ah.update(node);
		return true;
	}

    /**
     * This method must be synchronized because it cannot run if we are updating some entries.
     *
     * @param id
     * @return
     * @throws Exception
     */
	@Override
    public synchronized OperResult remove(final String id) throws Exception {
        try {
            if (Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
                Log.debug(Geonet.HARVEST_MAN, "Removing harvesting with id : " + id);
            }
            AbstractHarvester ah = hmHarvesters.get(id);

            if (ah == null) {
                return OperResult.NOT_FOUND;
            }
            ah.destroy();
            settingMan.remove("harvesting/id:" + id);

            final HarvestHistoryRepository historyRepository = context.getBean(HarvestHistoryRepository.class);
            // set deleted status in harvest history table to 'y'
            historyRepository.markAllAsDeleted(ah.getParams().getUuid());
            hmHarvesters.remove(id);
            return OperResult.OK;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}


    /**
     * TODO Javadoc.
     *
     * @param id
     * @return
     * @throws SQLException
     * @throws SchedulerException
     */
	@Override
    public OperResult start(String id) throws SQLException, SchedulerException {
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "Starting harvesting with id : "+ id);
        }
		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null) {
			return OperResult.NOT_FOUND;
        }
		return ah.start();
	}

    /**
     * TODO Javadoc.
     *
     * @param id
     * @param status
     * @return
     * @throws SQLException
     * @throws SchedulerException
     */
	@Override
    public OperResult stop(String id, Common.Status status) throws SQLException, SchedulerException {
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "Stopping harvesting with id : "+ id);
        }
		AbstractHarvester ah = hmHarvesters.get(id);

		if (ah == null){
			return OperResult.NOT_FOUND;
        }
		return ah.stop(status);
	}

    /**
     * TODO Javadoc.
     *
     * @param id
     * @return
     * @throws SQLException
     * @throws SchedulerException
     */
	@Override
    public OperResult run(String id) throws SQLException, SchedulerException {
        // READONLYMODE
        if(!this.readOnly) {
            if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
                Log.debug(Geonet.HARVEST_MAN, "Running harvesting with id: "+ id);
            }
            AbstractHarvester ah = hmHarvesters.get(id);

            if (ah == null) {
                return OperResult.NOT_FOUND;
            }
            return ah.run();
        }
        else {
            if(Log.isDebugEnabled(Geonet.HARVEST_MAN)){
                Log.debug(Geonet.HARVEST_MAN, "GeoNetwork is running in read-only mode: skipping run of harvester with id: "+ id);
            }
            return null;
        }
	}

    /**
     * TODO Javadoc.
     *
     *
     * @param id
     * @return
     */
	@Override
    public OperResult invoke(String id) {
        // READONLYMODE
        if(!this.readOnly) {
            if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
                Log.debug(Geonet.HARVEST_MAN, "Invoking harvester with id: "+ id);
            }

            AbstractHarvester ah = hmHarvesters.get(id);
            if (ah == null) {
                return OperResult.NOT_FOUND;
            }
            return ah.invoke();
        } else {
            if(Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
                Log.debug(Geonet.HARVEST_MAN, "GeoNetwork is running in read-only mode: skipping invocation of harvester with id: "+ id);
            }
            return null;
        }
	}

    /**
     * TODO Javadoc.
     *
     * @param harvestUuid
     * @param id
     * @param uuid
     * @return
     */
	public Element getHarvestInfo(String harvestUuid, String id, String uuid) {
		Element info = new Element(Edit.Info.Elem.HARVEST_INFO);
		AbstractHarvester ah = hmHarvestLookup.get(harvestUuid);

		if (ah != null) {
			ah.addHarvestInfo(info, id, uuid);
        }
		return info;
	}

	@Override
    public AbstractHarvester getHarvester(String harvestUuid) {
	    return hmHarvestLookup.get(harvestUuid);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

    /**
     *
     * @param node
     */
	private void addInfo(Element node) {
		String id = node.getAttributeValue("id");
        if (hmHarvesters.get(id) != null) {
            hmHarvesters.get(id).addInfo(node);
        } else {
            Log.warning(Geonet.HARVEST_MAN, "Trying to add info to a " +
                    "non existing harvester with id : " + id);
        }
	}

    /**
     * Remove harvester information. For example, when records
     * are removed, clean the last status information if any.
     *
     * @param id
     * @param ownerId
     * @throws Exception
     */
    public void removeInfo(String id, String ownerId) throws Exception {
        // get the specified harvester from the settings table
        Element node = get(id, context, null);
        if (node != null) {
            Element info = node.getChild("info");
            if (info != null) {
                info.removeContent();
            }
            update(node, ownerId);
        }
    }
    /**
     *
     * @return
     */
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     *
     * @param readOnly
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public synchronized OperResult clearBatch(String id) throws Exception {
        if(Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, "Clearing harvesting with id : "+ id);

        AbstractHarvester<?> ah = hmHarvesters.get(id);

        if (ah == null) {
            return OperResult.NOT_FOUND;
        }

        long elapsedTime = System.currentTimeMillis();

        String harvesterUUID = ah.getParams().getUuid();

        final Specification<Metadata> specification = MetadataSpecs.hasHarvesterUuid(harvesterUUID);
        int numberOfRecordsRemoved = dataMan.batchDeleteMetadataAndUpdateIndex(specification);
        ah.emptyResult();
        elapsedTime = (System.currentTimeMillis() - elapsedTime) / 1000;

        // clear last run info
        removeInfo(id, context.getUserSession().getUserId());
        ah.emptyResult();

        Element historyEl = new Element("result");
        historyEl.addContent(new Element("cleared").
                setAttribute("recordsRemoved", numberOfRecordsRemoved + ""));
        final String lastRun = new DateTime().withZone(DateTimeZone.forID("UTC")).toString();
        ISODate lastRunDate = new ISODate(lastRun);

        HarvestHistoryRepository historyRepository = context.getBean(HarvestHistoryRepository.class);
        HarvestHistory history = new HarvestHistory();
        history.setDeleted(true);
        history.setElapsedTime((int) elapsedTime);
        history.setHarvestDate(lastRunDate);
        history.setHarvesterName(ah.getParams().getName());
        history.setHarvesterType(ah.getType());
        history.setHarvesterUuid(ah.getParams().getUuid());
        history.setInfo(historyEl);
        history.setParams(ah.getParams().getNodeElement());

        historyRepository.save(history);
        return OperResult.OK;
    }}
