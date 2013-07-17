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

package org.fao.geonet.kernel.harvest.harvester;

import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.JeevesException;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.guiservices.session.JeevesUser;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.QuartzSchedulerUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.Common.OperResult;
import org.fao.geonet.kernel.harvest.Common.Status;
import org.fao.geonet.kernel.harvest.harvester.arcsde.ArcSDEHarvester;
import org.fao.geonet.kernel.harvest.harvester.csw.CswHarvester;
import org.fao.geonet.kernel.harvest.harvester.geoPREST.GeoPRESTHarvester;
import org.fao.geonet.kernel.harvest.harvester.geonet.GeonetHarvester;
import org.fao.geonet.kernel.harvest.harvester.geonet20.Geonet20Harvester;
import org.fao.geonet.kernel.harvest.harvester.localfilesystem.LocalFilesystemHarvester;
import org.fao.geonet.kernel.harvest.harvester.oaipmh.OaiPmhHarvester;
import org.fao.geonet.kernel.harvest.harvester.ogcwxs.OgcWxSHarvester;
import org.fao.geonet.kernel.harvest.harvester.thredds.ThreddsHarvester;
import org.fao.geonet.kernel.harvest.harvester.webdav.WebDavHarvester;
import org.fao.geonet.kernel.harvest.harvester.wfsfeatures.WfsFeaturesHarvester;
import org.fao.geonet.kernel.harvest.harvester.z3950.Z3950Harvester;
import org.fao.geonet.kernel.harvest.harvester.z3950Config.Z3950ConfigHarvester;
import org.fao.geonet.kernel.setting.HarvesterSettingsManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.monitor.harvest.AbstractHarvesterErrorCounter;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.quartz.JobKey.jobKey;

/**
 * TODO javadoc.
 *
 */
public abstract class AbstractHarvester extends BaseAligner {

    private static final String SCHEDULER_ID = "abstractHarvester";
    public static final String HARVESTER_GROUP_NAME = "HARVESTER_GROUP_NAME";

    //---------------------------------------------------------------------------
	//---
	//--- Static API methods
	//---
	//---------------------------------------------------------------------------

    /**
     * TODO Javadoc.
     *
     * @param context
     * @throws Exception
     */
	public static void staticInit(ServiceContext context) throws Exception {
		register(context, GeonetHarvester  .class);
		register(context, Geonet20Harvester.class);
		register(context, GeoPRESTHarvester.class);
		register(context, WebDavHarvester  .class);
		register(context, CswHarvester     .class);
		register(context, Z3950Harvester   .class);
		register(context, Z3950ConfigHarvester   .class);
		register(context, OaiPmhHarvester  .class);
		register(context, OgcWxSHarvester  .class);
		register(context, ThreddsHarvester .class);
		register(context, ArcSDEHarvester  .class);
		register(context, LocalFilesystemHarvester	.class);
		register(context, WfsFeaturesHarvester  .class);
		register(context, LocalFilesystemHarvester      .class);
	}

    /**
     * TODO Javadoc.
     *
     * @param context
     * @param harvester
     * @throws Exception
     */
	private static void register(ServiceContext context, Class<?> harvester) throws Exception {
		try {
			Method initMethod = harvester.getMethod("init", context.getClass());
			initMethod.invoke(null, context);
			AbstractHarvester ah = (AbstractHarvester) harvester.newInstance();
			hsHarvesters.put(ah.getType(), harvester);
		}
		catch(Exception e) {
			throw new Exception("Cannot register harvester : "+harvester, e);
		}
	}

    /**
     * TODO javadoc.
     *
     * @param type
     * @param context
     * @param sm
     * @param dm
     * @return
     * @throws BadParameterEx
     * @throws OperationAbortedEx
     */
	public static AbstractHarvester create(String type, ServiceContext context, HarvesterSettingsManager sm, DataManager dm) throws BadParameterEx, OperationAbortedEx {
		//--- raises an exception if type is null
		if (type == null) {
			throw new BadParameterEx("type", null);
        }

		Class<?> c = hsHarvesters.get(type);

		if (c == null) {
			throw new BadParameterEx("type", type);
        }

		try {
			AbstractHarvester ah = (AbstractHarvester) c.newInstance();

			ah.context    = context;
			ah.settingMan = sm;
			ah.dataMan    = dm;
			return ah;
		}
		catch(Exception e) {
			throw new OperationAbortedEx("Cannot instantiate harvester", e);
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param node
     * @throws BadInputEx
     * @throws SQLException
     */
	public void add(Dbms dbms, Element node) throws BadInputEx, SQLException {
		status   = Status.INACTIVE;
		error    = null;
		id       = doAdd(dbms, node);
	}

	public synchronized void init(Element node) throws BadInputEx, SchedulerException
	{
		id       = node.getAttributeValue("id");
		status   = Status.parse(node.getChild("options").getChildText("status"));
		error    = null;

		//--- init harvester
		doInit(node);

		if (status == Status.ACTIVE) {
		    doSchedule();
		}
	}

    /**
     * TODO Javadoc.
     *
     * @throws SchedulerException
     */
    private void doSchedule() throws SchedulerException {
        Scheduler scheduler = getScheduler();

        JobDetail jobDetail = getParams().getJob();
        Trigger trigger = getParams().getTrigger();
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * TODO Javadoc.
     *
     * @throws SchedulerException
     */
    private void doUnschedule() throws SchedulerException {
        getScheduler().deleteJob(jobKey(getParams().uuid, HARVESTER_GROUP_NAME));
    }

    /**
     * TODO Javadoc.
     *
     * @return
     * @throws SchedulerException
     */
    public static Scheduler getScheduler() throws SchedulerException {
        return QuartzSchedulerUtils.getScheduler(SCHEDULER_ID,true);
    }

    /**
     * Called when the application is shutdown.
     * @throws SchedulerException
     */
	public void shutdown() throws SchedulerException {
       getScheduler().deleteJob(jobKey(getParams().uuid, HARVESTER_GROUP_NAME));
	}

    /**
     * TODO Javadoc.
     *
     * @throws SchedulerException
     */
    public static void shutdownScheduler() throws SchedulerException {
        getScheduler().shutdown(false);
    }

    /**
     * Called when the harvesting entry is removed from the system. It is used to remove harvested metadata.
     *
     * @param dbms
     * @throws Exception
	  */
	public synchronized void destroy(Dbms dbms) throws Exception {
	    doUnschedule();
		//--- remove all harvested metadata
		String getQuery = "SELECT id FROM Metadata WHERE harvestUuid=?";
		for (Object o : dbms.select(getQuery, getParams().uuid).getChildren()) {
			Element el = (Element) o;
			String  id = el.getChildText("id");

			dataMan.deleteMetadata(context, dbms, id);
			dbms.commit();
		}
		doDestroy(dbms);
	}

    /**
     * TODO Javadoc.
     *
     * @param dbms
     * @return
     * @throws SQLException
     * @throws SchedulerException
     */
	public synchronized OperResult start(Dbms dbms) throws SQLException, SchedulerException {
		if (status != Status.INACTIVE){
			return OperResult.ALREADY_ACTIVE;
        }
		settingMan.setValue(dbms, "harvesting/id:"+id+"/options/status", Status.ACTIVE);

		status     = Status.ACTIVE;
		error      = null;
		
		doSchedule();

		return OperResult.OK;
	}

    /**
     * TODO Javadoc.
     *
     * @param dbms
     * @return
     * @throws SQLException
     * @throws SchedulerException
     */
	public synchronized OperResult stop(Dbms dbms) throws SQLException, SchedulerException {
		if (status != Status.ACTIVE) {
			return OperResult.ALREADY_INACTIVE;
        }
		settingMan.setValue(dbms, "harvesting/id:"+id+"/options/status", Status.INACTIVE);
		doUnschedule();
		status   = Status.INACTIVE;
		return OperResult.OK;
	}

    /**
     * TODO Javadoc.
     *
     * @param dbms
     * @return
     * @throws SQLException
     * @throws SchedulerException
     */
    public synchronized OperResult run(Dbms dbms) throws SQLException, SchedulerException {
		if (status == Status.INACTIVE) {
			start(dbms);
        }
		if (running) {
			return OperResult.ALREADY_RUNNING;
        }
        getScheduler().triggerJob(jobKey(getParams().uuid, HARVESTER_GROUP_NAME));
		return OperResult.OK;
	}

    /**
     * TODO Javadoc.
     *
     * @param rm
     * @return
     */
	public synchronized OperResult invoke(ResourceManager rm) {
		// Cannot do invoke if this harvester was started (iei active)
		if (status != Status.INACTIVE){
			return OperResult.ALREADY_ACTIVE;
        }
		String nodeName = getParams().name +" ("+ getClass().getSimpleName() +")";
		OperResult result = OperResult.OK;

		try {
			status = Status.ACTIVE;
			log.info("Started harvesting from node : " + nodeName);
			doHarvest(log, rm);
			log.info("Ended harvesting from node : " + nodeName);
			rm.close();
		}
		catch(Throwable t) {
            context.getMonitorManager().getCounter(AbstractHarvesterErrorCounter.class).inc();
			result = OperResult.ERROR;
			log.warning("Raised exception while harvesting from : " + nodeName);
			log.warning(" (C) Class   : " + t.getClass().getSimpleName());
			log.warning(" (C) Message : " + t.getMessage());
			error = t;
			t.printStackTrace();

			try {
				rm.abort();
			}
			catch (Exception ex) {
				log.warning("CANNOT ABORT EXCEPTION");
				log.warning(" (C) Exc : " + ex);
			}
		}
        finally {
			status = Status.INACTIVE;
		}
		return result;
	}

    /**
     * TODO Javadoc.
     *
     * @param dbms
     * @param node
     * @throws BadInputEx
     * @throws SQLException
     * @throws SchedulerException
     */
	public synchronized void update(Dbms dbms, Element node) throws BadInputEx, SQLException, SchedulerException {
		doUpdate(dbms, id, node);

		if (status == Status.ACTIVE) {
			//--- stop executor
			doUnschedule();
			//--- restart executor
			error      = null;
			doSchedule();
		}
	}

    /**
     * TODO Javadoc.
     *
     * @return
     */
	public String getID() { return id; }

    /**
     * Adds harvesting result information to each harvesting entry.
     *
     * @param node
     */
	public void addInfo(Element node) {
		Element info = node.getChild("info");

		//--- 'running'
		info.addContent(new Element("running").setText(running+""));

		//--- harvester specific info
		doAddInfo(node);

		//--- add error information
		if (error != null){
			node.addContent(JeevesException.toElement(error));
	}
	}

    /**
     * Adds harvesting information to each metadata element. Some sites can generate url for thumbnails.
     *
     * @param info
     * @param id
     * @param uuid
     */
	public void addHarvestInfo(Element info, String id, String uuid) {
		info.addContent(new Element("type").setText(getType()));
	}

	//---------------------------------------------------------------------------
	//---
	//--- Package methods (called by Executor)
	//---
	//---------------------------------------------------------------------------

    /**
     *  Nested class to handle harvesting with fast indexing.
     */
	public class HarvestWithIndexProcessor extends MetadataIndexerProcessor {
		ResourceManager rm;
		Logger logger;

        /**
         *
         * @param dm
         * @param logger
         * @param rm
         */
		public HarvestWithIndexProcessor(DataManager dm, Logger logger, ResourceManager rm) {
			super(dm);
			this.logger = logger;
			this.rm = rm;
		}

        /**
         *
         * @throws Exception
         */
		@Override
		public void process() throws Exception {
			doHarvest(logger, rm);
		}
	}
	
	/**
	 * Create a session for the user who created the harvester. The owner identifier is added when the harvester config
     * is created or updated according to user session.
	 */
	private void login() throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		JeevesUser user = new JeevesUser(this.context.getProfileManager());

        String ownerId = getParams().ownerId;
        if(log.isDebugEnabled()) {
            log.debug("AbstractHarvester login: ownerId = " + ownerId);
        }

        // for harvesters created before owner was added to the harvester code, or harvesters belonging to a user that no longer exists
        if(StringUtils.isEmpty(ownerId) || !this.dataMan.existsUser(dbms, Integer.parseInt(ownerId))) {
            // just pick any Administrator (they can all see all harvesters and groups anyway)
            ownerId = this.dataMan.pickAnyAdministrator(dbms);
            getParams().ownerId = ownerId;
            if(log.isDebugEnabled()) {
                log.debug("AbstractHarvester login: picked Administrator  " + ownerId + " to run this job");
            }
        }

		user.setId(ownerId);

		// lookup owner profile (it may have changed since harvester was created)
        String profile = this.dataMan.getUserProfile(dbms, Integer.parseInt(ownerId));
        user.setProfile(profile);
        // todo reject if < useradmin ?

		UserSession session = new UserSession();
		session.loginAs(user);
		this.context.setUserSession(session);
		
		this.context.setIpAddress(null);
	}

    /**
     *
     */
	void harvest()  {
	    running = true;
	    long startTime = System.currentTimeMillis();
	    try {
            error = null;
		ResourceManager rm = new ResourceManager(context.getMonitorManager(), context.getProviderManager());
		Logger logger = Log.createLogger(Geonet.HARVESTER);
            String lastRun = new DateTime().withZone(DateTimeZone.forID("UTC")).toString();
		String nodeName = getParams().name +" ("+ getClass().getSimpleName() +")";
		    try {
		login();
			Dbms dbms = (Dbms) rm.open(Geonet.Res.MAIN_DB);

			//--- update lastRun
			settingMan.setValue(dbms, "harvesting/id:"+ id +"/info/lastRun", lastRun);

			//--- proper harvesting
			logger.info("Started harvesting from node : "+ nodeName);
			HarvestWithIndexProcessor h = new HarvestWithIndexProcessor(dataMan, logger, rm);
			    // todo check (was: processwithfastindexing)
			h.process();
			logger.info("Ended harvesting from node : "+ nodeName);

			    if (getParams().oneRunOnly){
				stop(dbms);
                }
			rm.close();
		}
            catch(Throwable t) {
			logger.warning("Raised exception while harvesting from : "+ nodeName);
			logger.warning(" (C) Class   : "+ t.getClass().getSimpleName());
			logger.warning(" (C) Message : "+ t.getMessage());
			error = t;
			t.printStackTrace();
                try {
				rm.abort();
			}
                catch (Exception ex) {
				logger.warning("CANNOT ABORT EXCEPTION");
				logger.warning(" (C) Exc : "+ ex);
			}
		}
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;

            // record the results/errors for this harvest in the database
		Dbms dbms = null;
		try {
			dbms = (Dbms) rm.openDirect(Geonet.Res.MAIN_DB);
			Element result = getResult();
			if (error != null) result = JeevesException.toElement(error);
			HarvesterHistoryDao.write(dbms, context.getSerialFactory(), getType(), getParams().name, getParams().uuid, elapsedTime, lastRun, getParams().node, result);
            }
            catch (Exception e) {
			logger.warning("Raised exception while attempting to store harvest history from : "+ nodeName);
			e.printStackTrace();
			logger.warning(" (C) Exc   : "+ e);
            }
            finally {
			try {
                    if (dbms != null) {
                        rm.close(Geonet.Res.MAIN_DB, dbms);
                    }
                }
                catch (Exception dbe) {
				dbe.printStackTrace();
				logger.error("Raised exception while attempting to close dbms connection to harvest history table");
				logger.error(" (C) Exc   : "+ dbe);
			}
		}
	    }
        finally {
	        running  = false;
	    }

	}

	//---------------------------------------------------------------------------
	//---
	//--- Abstract methods that must be overridden
	//---
	//---------------------------------------------------------------------------

    /**
     *
     * @return
     */
	public abstract String getType();

    /**
     *
     * @return
     */
    public AbstractParams getParams() {
        return params;
    }

    /**
     *
     * @param entry
     * @throws BadInputEx
     */
	protected abstract void doInit(Element entry) throws BadInputEx;

    /**
     *
     * @param dbms
     * @throws SQLException
     */
    protected void doDestroy(Dbms dbms) throws SQLException {
        File icon = new File(Resources.locateLogosDir(context), params.uuid +".gif");

        if (!icon.delete() && icon.exists()) {
            Log.warning(Geonet.HARVESTER+"."+getType(), "Unable to delete icon: "+icon);
        }

        Lib.sources.delete(dbms, params.uuid);

        // FIXME: Should also delete the categories we have created for servers
    }

    /**
     *
     * @param dbms
     * @param node
     * @return
     * @throws BadInputEx
     * @throws SQLException
     */
	protected abstract String doAdd(Dbms dbms, Element node) throws BadInputEx, SQLException;

    /**
     *
     * @param dbms
     * @param id
     * @param node
     * @throws BadInputEx
     * @throws SQLException
     */
	protected abstract void doUpdate(Dbms dbms, String id, Element node) throws BadInputEx, SQLException;

    /**
     *
     * @param node
     */
    protected void doAddInfo(Element node) {
        //--- if the harvesting is not started yet, we don't have any info

        if (result == null)
            return;

        //--- ok, add proper info

        Element info = node.getChild("info");
        Element res  = getResult();
        info.addContent(res);
    }

    /**
     *
     * @param l
     * @param rm
     * @throws Exception
     */
	protected abstract void doHarvest(Logger l, ResourceManager rm) throws Exception;

	//---------------------------------------------------------------------------
	//---
	//--- Protected storage methods
	//---
	//---------------------------------------------------------------------------

    /**
     * Invoked from doAdd and doUpdate in sub class implementations.
     *
     * @param dbms
     * @param params
     * @param path
     * @throws SQLException
     */
	protected void storeNode(Dbms dbms, AbstractParams params, String path) throws SQLException {
		String siteId    = settingMan.add(dbms, path, "site",    "");
		String optionsId = settingMan.add(dbms, path, "options", "");
		String infoId    = settingMan.add(dbms, path, "info",    "");
		String contentId = settingMan.add(dbms, path, "content", "");

		//--- setup site node ----------------------------------------

		settingMan.add(dbms, "id:"+siteId, "name",     params.name);
		settingMan.add(dbms, "id:"+siteId, "uuid",     params.uuid);

        /**
         * User who created or updated this node.
         */
        settingMan.add(dbms, "id:"+siteId, "ownerId", params.ownerId);
        /**
         * Group selected by user who created or updated this node.
         */
        settingMan.add(dbms, "id:"+siteId, "ownerGroup", params.ownerIdGroup);

		String useAccId = settingMan.add(dbms, "id:"+siteId, "useAccount", params.useAccount);

		settingMan.add(dbms, "id:"+useAccId, "username", params.username);
		settingMan.add(dbms, "id:"+useAccId, "password", params.password);

		//--- setup options node ---------------------------------------

		settingMan.add(dbms, "id:"+optionsId, "every",      params.every);
		settingMan.add(dbms, "id:"+optionsId, "oneRunOnly", params.oneRunOnly);
		settingMan.add(dbms, "id:"+optionsId, "status",     status);

		//--- setup content node ---------------------------------------

		settingMan.add(dbms, "id:"+contentId, "importxslt", params.importXslt);
		settingMan.add(dbms, "id:"+contentId, "validate",   params.validate);

		//--- setup stats node ----------------------------------------

		settingMan.add(dbms, "id:"+infoId, "lastRun", "");

		//--- store privileges and categories ------------------------

		storePrivileges(dbms, params, path);
		storeCategories(dbms, params, path);

		storeNodeExtra(dbms, params, path, siteId, optionsId);
	}

    /**
     * Override this method with an empty body to avoid privileges storage.
     *
     * @param dbms
     * @param params
     * @param path
     * @throws SQLException
     */
	protected void storePrivileges(Dbms dbms, AbstractParams params, String path) throws SQLException {
		String privId = settingMan.add(dbms, path, "privileges", "");

		for (Privileges p : params.getPrivileges()) {
			String groupId = settingMan.add(dbms, "id:"+ privId, "group", p.getGroupId());
			for (int oper : p.getOperations()) {
				settingMan.add(dbms, "id:"+ groupId, "operation", oper);
		    }
	    }
	}

    /**
     * Override this method with an empty body to avoid categories storage.
     *
     * @param dbms
     * @param params
     * @param path
     * @throws SQLException
     */
	protected void storeCategories(Dbms dbms, AbstractParams params, String path) throws SQLException {
		String categId = settingMan.add(dbms, path, "categories", "");

		for (String id : params.getCategories()) {
			settingMan.add(dbms, "id:"+ categId, "category", id);
	}
	}

    /**
     *  Override this method to store harvesting node's specific settings.
     *
     *
     * @param dbms
     * @param params
     * @param path
     * @param siteId
     * @param optionsId
     * @throws SQLException
     */
	protected void storeNodeExtra(Dbms dbms, AbstractParams params, String path, String siteId, String optionsId) throws SQLException {}

    /**
     *
     * @param values
     * @param path
     * @param el
     * @param name
     */
	protected void setValue(Map<String, Object> values, String path, Element el, String name) {
		if (el == null) {
			return ;
        }

		String value = el.getChildText(name);

		if (value != null) {
			values.put(path, value);
	}
	}

    /**
     *
     * @param el
     * @param name
     * @param value
     */
	protected void add(Element el, String name, int value) {
		el.addContent(new Element(name).setText(Integer.toString(value)));
	}

    public void setParams(AbstractParams params) {
        this.params = params;
    }

    /**
     *
     * @return
     */
    protected Element getResult() {
        Element res  = new Element("result");
        if (result != null) {
            add(res, "added", result.addedMetadata);
            add(res, "atomicDatasetRecords", result.atomicDatasetRecords);
            add(res, "badFormat", result.badFormat);
            add(res, "collectionDatasetRecords", result.collectionDatasetRecords);
            add(res, "datasetUuidExist", result.datasetUuidExist);
            add(res, "doesNotValidate", result.doesNotValidate);
            add(res, "duplicatedResource", result.duplicatedResource);
            add(res, "fragmentsMatched", result.fragmentsMatched);
            add(res, "fragmentsReturned", result.fragmentsReturned);
            add(res, "fragmentsUnknownSchema", result.fragmentsUnknownSchema);
            add(res, "incompatible",  result.incompatibleMetadata);
            add(res, "recordsBuilt", result.recordsBuilt);
            add(res, "recordsUpdated", result.recordsUpdated);
            add(res, "removed", result.locallyRemoved);
            add(res, "serviceRecords", result.serviceRecords);
            add(res, "subtemplatesAdded", result.subtemplatesAdded);
            add(res, "subtemplatesRemoved",	result.subtemplatesRemoved);
            add(res, "subtemplatesUpdated", result.subtemplatesUpdated);
            add(res, "total", result.totalMetadata);
            add(res, "unchanged", result.unchangedMetadata);
            add(res, "unknownSchema",result.unknownSchema);
            add(res, "unretrievable", result.unretrievable);
            add(res, "updated", result.updatedMetadata);
            add(res, "thumbnails", result.thumbnails);
            add(res, "thumbnailsFailed", result.thumbnailsFailed);
        }
        return res;
    }
    //--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private String id;
	private volatile Status status;

	private Throwable error;
    private boolean running = false;

	protected ServiceContext context;
	protected HarvesterSettingsManager settingMan;
	protected DataManager    dataMan;

    protected AbstractParams params;
    protected HarvestResult result;

    protected Logger log = Log.createLogger(Geonet.HARVESTER);

	private static Map<String, Class<?>> hsHarvesters = new HashMap<String, Class<?>>();

}