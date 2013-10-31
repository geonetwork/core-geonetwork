package org.fao.geonet.geocat;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import jeeves.config.springutil.JeevesApplicationContext;
import jeeves.guiservices.session.JeevesUser;
import jeeves.interfaces.Schedule;
import jeeves.interfaces.Service;
import jeeves.monitor.MonitorManager;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ScheduleContext;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import jeeves.server.resources.ProviderManager;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.GeonetworkDataDirectory;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Profile;
import org.fao.geonet.kernel.mef.MEFLib;
import org.jdom.Element;

public class ArchiveAllMetadataJob implements Schedule, Service {

	static final String BACKUP_FILENAME = "geocat_backup";
	static final String BACKUP_DIR = "geocat_backups";
	public static final String BACKUP_LOG  = Geonet.GEONETWORK + ".backup";
	private String stylePath;
	private AtomicBoolean backupIsRunning = new AtomicBoolean(false);

	@Override
	public void init(String appPath, ServiceConfig params) throws Exception {
		this.stylePath = appPath + Geonet.Path.SCHEMAS;
	}

	@Override
	public void exec(ScheduleContext context) throws Exception {
		MonitorManager monitorManager = context.getMonitorManager();
		ProviderManager providerManager = context.getProviderManager();
		SerialFactory serialFactory = context.getSerialFactory();
		JeevesApplicationContext appContext = context.getApplicationContext();
		ProfileManager profileManager = context.getProfileManager();
		ServiceContext serviceContext = new ServiceContext("none", appContext , new XmlCacheManager() , monitorManager, providerManager, serialFactory, profileManager , context.allContexts());
		serviceContext.setAppPath(context.getAppPath());
		serviceContext.setBaseUrl(context.getBaseUrl());
		serviceContext.setAsThreadLocal();
		createBackup(serviceContext);
	}

	@Override
	public Element exec(Element params, ServiceContext context)
			throws Exception {
		createBackup(context);
		return new Element("ok");
	}
	

	private void createBackup(ServiceContext serviceContext) throws Exception, SQLException,
			IOException {
	    if(!backupIsRunning.compareAndSet(false, true)) {
	        return;
	    }
	    Dbms dbms = null;
		try {
			loginAsAdmin(serviceContext);
    		Log.info(BACKUP_LOG, "Starting backup of all metadata");
			dbms = (Dbms) serviceContext.getResourceManager().openDirect(Geonet.Res.MAIN_DB);
    		@SuppressWarnings("unchecked")
    		List<Element> uuidQuery = dbms.select("SELECT uuid FROM Metadata where not isharvested='y'").getChildren();

            // don't keep dbms open...
            serviceContext.getResourceManager().close(Geonet.Res.MAIN_DB, dbms);
            dbms = null;

    		Set<String> uuids = new HashSet<String>();
    		for (Element uuidElement : uuidQuery) {
    			uuids.add(uuidElement.getChildText("uuid"));
    		}

    		Log.info(BACKUP_LOG, "Backing up "+uuids.size()+" metadata");
		
    		String format = "full";
    		boolean resolveXlink = true;
    		boolean removeXlinkAttribute = false;
            boolean skipOnError = true;
            File srcFile = new File(MEFLib.doMEF2Export(serviceContext, uuids, format, false, stylePath, resolveXlink , removeXlinkAttribute, skipOnError));
		
    		String datadir = System.getProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY);
    		File backupDir = new File(datadir, BACKUP_DIR);
    		String today = new SimpleDateFormat("-yyyy-MM-dd").format(new Date());
    		File destFile = new File(backupDir, BACKUP_FILENAME+today+".zip");
    		FileUtils.deleteDirectory(backupDir);
    		destFile.getParentFile().mkdirs();
    		FileUtils.moveFile(srcFile, destFile);
    		if(!destFile.exists()) {
    			throw new Exception("Moving backup file failed!");
    		}
    		Log.info(BACKUP_LOG, "Backup finished.  Backup file: "+destFile);
		} catch (Throwable t) {
			Log.error(BACKUP_LOG, "Failed to create a back up of metadata", t);
		} finally {
			if(dbms != null) {
				serviceContext.getResourceManager().close(Geonet.Res.MAIN_DB, dbms);
			}
            serviceContext.getResourceManager().close();
		    backupIsRunning.set(false);
		}
	}

	private void loginAsAdmin(ServiceContext serviceContext) {
		JeevesUser user = new JeevesUser(serviceContext.getProfileManager());
		user.setId("1");
		user.setUsername("Admin");
		user.setProfile(Profile.ADMINISTRATOR);
		UserSession session = new UserSession();
		session.loginAs(user);
		serviceContext.setUserSession(session);
	}

}
