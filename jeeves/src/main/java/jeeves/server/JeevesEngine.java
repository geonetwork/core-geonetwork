//==============================================================================
//===
//===   JeevesEngine
//===
//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.xml.transform.TransformerConfigurationException;

import jeeves.config.springutil.JeevesApplicationContext;
import jeeves.constants.ConfigFile;
import jeeves.constants.Jeeves;
import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Activator;
import jeeves.interfaces.ApplicationHandler;
import jeeves.interfaces.Logger;
import jeeves.monitor.MonitorManager;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import jeeves.server.overrides.ConfigurationOverrides;
import jeeves.server.resources.ProviderManager;
import jeeves.server.resources.ResourceManager;
import jeeves.server.sources.ServiceRequest;
import jeeves.server.sources.http.JeevesServlet;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;
import jeeves.utils.TransformerFactoryFactory;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.springframework.web.context.support.WebApplicationContextUtils;

//=============================================================================

/** This is the main class. It handles http connections and inits the system
  */

public class JeevesEngine
{
	private static final String TRANSFORMER_PATH = "/WEB-INF/classes/META-INF/services/javax.xml.transform.TransformerFactory";
	private String  defaultSrv;
	private String	startupErrorSrv;
	private String  profilesFile;
	private String  defaultLang;
	private String  defaultContType;
	private String  uploadDir;
	private int     maxUploadSize;
	private String  appPath;
	private boolean defaultLocal;
	private boolean debugFlag;

		private String dbResourceProviderName = "main-db";
    private boolean dbLoaded;
    
	/** true if the 'general' part has been loaded */
	private boolean generalLoaded;

	private ServiceManager  serviceMan  = new ServiceManager();
	private ProviderManager providerMan = new ProviderManager();
	private ScheduleManager scheduleMan = new ScheduleManager();
	private SerialFactory   serialFact  = new SerialFactory();

	private Logger appHandLogger = Log.createLogger(Log.APPHAND);
	private List<Element> appHandList = new ArrayList<Element>();
	private Vector<ApplicationHandler> vAppHandlers = new Vector<ApplicationHandler>();
	private Vector<Activator> vActivators = new Vector<Activator>();
	private XmlCacheManager xmlCacheManager = new XmlCacheManager();
    private MonitorManager monitorManager;
    
    private List<Element> dbservices = new ArrayList<Element>();
    


    //---------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//---------------------------------------------------------------------------

	/** Inits the engine, loading all needed data
	  */

	public void init(String appPath, String configPath, String baseUrl, JeevesServlet servlet) throws ServletException
	{
		try
		{
			
			PropertyConfigurator.configure(configPath +"log4j.cfg");

            ServletContext servletContext = null;
            if(servlet != null) servletContext= servlet.getServletContext();

            ConfigurationOverrides.DEFAULT.updateLoggingAsAccordingToOverrides(servletContext, appPath);

            monitorManager = new MonitorManager(servletContext, baseUrl);
			this.appPath = appPath;

			long start   = System.currentTimeMillis();

			long maxMem  = Runtime.getRuntime().maxMemory()   / 1024;
			long totMem  = Runtime.getRuntime().totalMemory() / 1024;
			long freeMem = Runtime.getRuntime().freeMemory()  / 1024;

			long usedMem      = totMem - freeMem;
			long startFreeMem = maxMem - usedMem;

			// System.setProperty("javax.xml.transform.TransformerFactory",
			//						 "net.sf.saxon.TransformerFactoryImpl");
			// Do this using library meta-inf to avoid affecting other servlets
			// in the same container

			info("=== Starting system ========================================");

			//---------------------------------------------------------------------
			//--- init system
			info("Engine : "+ this.getClass().getName());
			info("Java version : "+ System.getProperty("java.vm.version"));
			info("Java vendor  : "+ System.getProperty("java.vm.vendor"));

            setupXSLTTransformerFactory(servlet);

			info("Path    : "+ appPath);
			info("BaseURL : "+ baseUrl);

			// obtain application context so we can configure the serviceManager with it but we will configure it a bit later
            JeevesApplicationContext jeevesAppContext = (JeevesApplicationContext) WebApplicationContextUtils.getWebApplicationContext(servletContext);
            
			serviceMan.setAppPath(appPath);
			serviceMan.setProviderMan(providerMan);
			serviceMan.setMonitorMan(monitorManager);
			serviceMan.setXmlCacheManager(xmlCacheManager );
			serviceMan.setApplicationContext(jeevesAppContext);
			serviceMan.setSerialFactory(serialFact);
			serviceMan.setBaseUrl(baseUrl);
			serviceMan.setServlet(servlet);

			scheduleMan.setAppPath(appPath);
			scheduleMan.setProviderMan(providerMan);
			scheduleMan.setMonitorManager(monitorManager);
			scheduleMan.setApplicationContext(jeevesAppContext);
			scheduleMan.setSerialFactory(serialFact);
			scheduleMan.setBaseUrl(baseUrl);

			dbLoaded = false;
			
			loadConfigFile(servletContext, configPath, Jeeves.CONFIG_FILE, serviceMan);

            info("Initializing profiles...");
            ProfileManager profileManager = serviceMan.loadProfiles(servletContext, profilesFile);

            // Add ResourceManager as a bean to the spring application context so that GeonetworkAuthentication can access it
            jeevesAppContext.getBeanFactory().registerSingleton("resourceManager", new ResourceManager(this.monitorManager, this.providerMan));
            jeevesAppContext.getBeanFactory().registerSingleton("profileManager", profileManager);
            jeevesAppContext.getBeanFactory().registerSingleton("serialFactory", serialFact);

			//--- handlers must be started here because they may need the context
			//--- with the ProfileManager already loaded

			for(int i=0; i<appHandList.size(); i++)
				initAppHandler((Element) appHandList.get(i), servlet, jeevesAppContext);

			info("Starting schedule manager...");
			scheduleMan.start();

			//---------------------------------------------------------------------

			long end      = System.currentTimeMillis();
			long duration = (end - start) / 1000;

			freeMem = Runtime.getRuntime().freeMemory()  / 1024;
			totMem  = Runtime.getRuntime().totalMemory() / 1024;
			usedMem = totMem - freeMem;

			long endFreeMem = maxMem - usedMem;
			long dataMem    = startFreeMem - endFreeMem;

			info("Memory used is  : " + dataMem  + " Kb");
			info("Total memory is : " + maxMem   + " Kb");
			info("Startup time is : " + duration + " (secs)");

			info("=== System working =========================================");
		}
		catch (Exception e)
		{
			fatal("Raised exception during init");
			fatal("   Exception : " +e);
			fatal("   Message   : " +e.getMessage());
			fatal("   Stack     : " +Util.getStackTrace(e));

			throw new ServletException("Exception raised", e);
		}
	}

    /**
     * Looks up the implementation of XSLT factory defined in META-INF/services/javax.xml.transform.TransformerFactory and instantiates
     * that implementation. This way, a conflicting setting in System Properties is overridden for this application only.
     *
     * @throws IOException
     * @throws TransformerConfigurationException
     */
    private void setupXSLTTransformerFactory(JeevesServlet servlet) throws IOException, TransformerConfigurationException {
    	
    	InputStream in = null;
    	BufferedReader br = null;
    	// In debug mode, Jeeves may load a different file
    	// Load javax.xml.transform.TransformerFactory from application path instead
    	if(servlet != null) {
    		in = servlet.getServletContext().getResourceAsStream(TRANSFORMER_PATH);
    	}
    	if(in == null){
    		File f = new File(appPath + TRANSFORMER_PATH);
    		in = new FileInputStream(f);
    	}        
        try {
            
            if(in != null) {
                br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String line;
                while ((line = br.readLine()) != null)   {
                    if(line.length() == 0) {
                        warning("Malformed definition of XSLT transformer (in: META-INF/services/javax.xml.transform.TransformerFactory).");
                    }
                    TransformerFactoryFactory.init(line);
                    break;
                }
            }
        }
        catch(IOException x) {
        	String msg = "Definition of XSLT transformer not found (tried: " + new File(appPath + TRANSFORMER_PATH).getCanonicalPath() + ")";
        	if(servlet != null) {
        		msg += " and servlet.getServletContext().getResourceAsStream("+TRANSFORMER_PATH+")";
        	}
        	warning(msg);
            error(x.getMessage());
            x.printStackTrace();
        }
        finally {
            IOUtils.closeQuietly(in);
            if(br != null) {
                IOUtils.closeQuietly(br);
            }
        }
    }


	//---------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	private void loadConfigFile(ServletContext servletContext, String path, String file, ServiceManager serviceMan) throws Exception
	{
		file = path + file;

		info("Loading : " + file);

		Element configRoot = Xml.loadFile(file);

        ConfigurationOverrides.DEFAULT.updateWithOverrides(file, servletContext, appPath, configRoot);

		Element elGeneral = configRoot.getChild(ConfigFile.Child.GENERAL);
		Element elDefault = configRoot.getChild(ConfigFile.Child.DEFAULT);

		if (!generalLoaded)
		{
			if (elGeneral == null)
				throw new NullPointerException("Missing 'general' element in config file :" +file);

			if (elDefault == null)
				throw new NullPointerException("Missing 'default' element in config file :" +file);

			generalLoaded = true;

			initGeneral(elGeneral, serviceMan);
			initDefault(elDefault, serviceMan);
		}
		else
		{
			if (elGeneral != null)
				throw new IllegalArgumentException("Illegal 'general' element in secondary include");

			if (elDefault != null)
				throw new IllegalArgumentException("Illegal 'default' element in secondary include");
		}

		//--- init resources

		List<Element> resList = configRoot.getChildren(ConfigFile.Child.RESOURCES);
		
		for(int i=0; i<resList.size(); i++)
			initResources(resList.get(i), file);

		
		//--- init app-handlers

		appHandList.addAll(configRoot.getChildren(ConfigFile.Child.APP_HANDLER));

		//--- init services

		List<Element> srvList = configRoot.getChildren(ConfigFile.Child.SERVICES);
		
		for(int i=0; i<srvList.size(); i++)
			initServices(srvList.get(i));

		if (!dbLoaded) {
			setDBServicesElement(dbResourceProviderName);
			for(int i=0; i<dbservices.size(); i++){
				initServices(dbservices.get(i));
			}
			dbLoaded = true;
		}
		
		

		
		//--- init schedules

		List<Element> schedList = configRoot.getChildren(ConfigFile.Child.SCHEDULES);

		for(int i=0; i<schedList.size(); i++)
			initSchedules(schedList.get(i));

        //--- init monitoring

        List<Element> monitorList = configRoot.getChildren(ConfigFile.Child.MONITORS);

        for(int i=0; i<monitorList.size(); i++)
            monitorManager.initMonitors(monitorList.get(i));

		//--- recurse on includes

		List<Element> includes = configRoot.getChildren(ConfigFile.Child.INCLUDE);

		for(int i=0; i<includes.size(); i++)
		{
			Element include = includes.get(i);

			loadConfigFile(servletContext, path, include.getText(), serviceMan);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- 'general' element
	//---
	//---------------------------------------------------------------------------

	/** Setup parameters from config tag (config.xml)
	  */

	private void initGeneral(Element general, ServiceManager serviceMan) throws BadInputEx
	{
		info("Initializing general configuration...");

		profilesFile = Util.getParam(general, ConfigFile.General.Child.PROFILES);
		uploadDir    = Util.getParam(general, ConfigFile.General.Child.UPLOAD_DIR);
		try {
		    maxUploadSize = Integer.parseInt(Util.getParam(general, ConfigFile.General.Child.MAX_UPLOAD_SIZE));
		} 
		catch(Exception e){
		    maxUploadSize = 50;
		    error("Maximum upload size not properly configured in config.xml. Using default size of 50MB");
            error("   Exception : " +e);
            error("   Message   : " +e.getMessage());
            error("   Stack     : " +Util.getStackTrace(e));
	    }

        if (!new File(uploadDir).isAbsolute())
			uploadDir = appPath + uploadDir;

        if (!uploadDir.endsWith("/"))
            uploadDir += "/";

		File uploadDirFile = new File(uploadDir);
		if( !uploadDirFile.mkdirs() && !uploadDirFile.exists()) {
		    throw new RuntimeException("Unable to make upload directory: "+uploadDirFile);
		} else {
		    Log.info(Log.JEEVES, "Upload directory is: "+uploadDir);
		}

		debugFlag = "true".equals(general.getChildText(ConfigFile.General.Child.DEBUG));

		serviceMan.setUploadDir(uploadDir);
		serviceMan.setMaxUploadSize(maxUploadSize);
	}

	//---------------------------------------------------------------------------
	//---
	//--- 'general' element
	//---
	//---------------------------------------------------------------------------

	/** Setup parameters from config tag (config.xml)
	  */

	@SuppressWarnings("unchecked")
	private void initDefault(Element defaults, ServiceManager serviceMan) throws Exception
	{
		info("Initializing defaults...");

		defaultSrv     = Util.getParam(defaults, ConfigFile.Default.Child.SERVICE);

		// -- Don't break behaviour before gn 2.7 - if the startupErrorService 
		// -- doesn't exist then ignore this parameter
		startupErrorSrv = Util.getParam(defaults, ConfigFile.Default.Child.STARTUPERRORSERVICE, "");
		defaultLang    = Util.getParam(defaults, ConfigFile.Default.Child.LANGUAGE);
		defaultContType= Util.getParam(defaults, ConfigFile.Default.Child.CONTENT_TYPE);

		defaultLocal = "true".equals(defaults.getChildText(ConfigFile.Default.Child.LOCALIZED));

		info("   Default local is :" +defaultLocal);

		serviceMan.setDefaultLang(defaultLang);
		serviceMan.setDefaultLocal(defaultLocal);
		serviceMan.setDefaultContType(defaultContType);

		List<Element> errorPages = defaults.getChildren(ConfigFile.Default.Child.ERROR);

		for(int i=0; i<errorPages.size(); i++)
			serviceMan.addErrorPage(errorPages.get(i));

		Element gui = defaults.getChild(ConfigFile.Default.Child.GUI);

		if (gui != null)
		{
			List<Element> guiElems = gui.getChildren();

			for(int i=0; i<guiElems.size(); i++)
				serviceMan.addDefaultGui(guiElems.get(i));
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- 'resources' element
	//---
	//---------------------------------------------------------------------------

	/** Setup resources from the resource element (config.xml)
	  */

	@SuppressWarnings("unchecked")
	private void initResources(Element resources, String file)
	{
		boolean resourceFound = false;
		info("Initializing resources...");

		List<Element> resList = resources.getChildren(ConfigFile.Resources.Child.RESOURCE);

		for(int i=0; i<resList.size(); i++)
		{
			Element res = resList.get(i);

			String  name      = res.getChildText(ConfigFile.Resource.Child.NAME);
			String  provider  = res.getChildText(ConfigFile.Resource.Child.PROVIDER);
			Element config    = res.getChild    (ConfigFile.Resource.Child.CONFIG);
			Element activator = res.getChild    (ConfigFile.Resource.Child.ACTIVATOR);

			String enabled = res.getAttributeValue(ConfigFile.Resource.Attr.ENABLED);

			if ((enabled == null) || enabled.equals("true"))
			{
				info("   Adding resource : " + name);

				resourceFound = true;
				try
				{
					if (activator != null)
					{
						String clas = activator.getAttributeValue(ConfigFile.Activator.Attr.CLASS);

						info("      Loading activator  : "+ clas);
						Activator activ = (Activator) Class.forName(clas).newInstance();

						info("      Starting activator : "+ clas);
						activ.startup(appPath, activator);

						vActivators.add(activ);
					}

					providerMan.register(provider, name, config);

					// Try and open a resource from the provider
					providerMan.getProvider(name).open();
					
					if (name.equals(dbResourceProviderName)){
						Dbms dbms = null;
						try {
							dbms = (Dbms) providerMan.getProvider(name).open();
						} finally {
							if (dbms != null) providerMan.getProvider(name).close(dbms);
						}
					}
				}
				catch(Exception e)
				{
					Map<String,String> errors = new HashMap<String,String>();
					String eS = "Raised exception while initializing resource "+name+" in "+file+". Skipped.";
					error(eS); 
					errors.put("Error", eS); 
					error("   Resource  : " +name);
					errors.put("Resource", name); 
					error("   Provider  : " +provider);
					errors.put("Provider", provider);
					error("   Exception : " +e);
					errors.put("Exception",e.toString());
					error("   Message   : " +e.getMessage());
					errors.put("Message", e.getMessage());
					error("   Stack     : " +Util.getStackTrace(e));
					errors.put("Stack", Util.getStackTrace(e));
					error(errors.toString());
					serviceMan.setStartupErrors(errors);
				}
			}
		}

		if (!resourceFound) {
			Map<String,String> errors = new HashMap<String,String>();
			errors.put("Error", "No database resources found to initialize - check "+file); 
			error(errors.toString());
			serviceMan.setStartupErrors(errors);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- 'appHandler' element
	//---
	//---------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	private void initAppHandler(Element handler, JeevesServlet servlet, JeevesApplicationContext jeevesApplicationContext) throws Exception
	{
		if (handler == null)
			info("Handler not found");
		else
		{
			String className = handler.getAttributeValue(ConfigFile.AppHandler.Attr.CLASS);

			if (className == null)
				throw new IllegalArgumentException("Missing '"        +ConfigFile.AppHandler.Attr.CLASS+
															  "' attribute in '" +ConfigFile.Child.APP_HANDLER+
															  "' element");

			info("Found handler : " +className);

			Class c = Class.forName(className);

			ApplicationHandler h = (ApplicationHandler) c.newInstance();

			ServiceContext srvContext = serviceMan.createServiceContext("AppHandler", jeevesApplicationContext);
			srvContext.setLanguage(defaultLang);
			srvContext.setLogger(appHandLogger);
			srvContext.setServlet(servlet);
			srvContext.setAsThreadLocal();

			try
			{
				info("--- Starting handler --------------------------------------");

				Object context = h.start(handler, srvContext);

				srvContext.getResourceManager().close();
				vAppHandlers.add(h);
				serviceMan .registerContext(h.getContextName(), context);
				scheduleMan.registerContext(h.getContextName(), context);
                monitorManager.initMonitorsForApp(srvContext);

				info("--- Handler started ---------------------------------------");
			}
			catch (Exception e)
			{
				Map<String,String> errors = new HashMap<String,String>();
				String eS = "Raised exception while starting appl handler. Skipped.";
				error(eS);
				errors.put("Error", eS);
				error("   Handler   : " +className);
				errors.put("Handler", className);
				error("   Exception : " +e);
				errors.put("Exception",e.toString());
				error("   Message   : " +e.getMessage());
				errors.put("Message",e.getMessage());
				error("   Stack     : " +Util.getStackTrace(e));
				errors.put("Stack",Util.getStackTrace(e));
				error(errors.toString());
				// only set the error if we don't already have one
				if (!serviceMan.isStartupError()) serviceMan.setStartupErrors(errors);
				srvContext.getResourceManager().abort();
			}
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- 'services' element
	//---
	//---------------------------------------------------------------------------

	/** Setup services found in the services tag (config.xml)
	  */

	@SuppressWarnings("unchecked")
	public void initServices(Element services) throws Exception
	{
		info("Initializing services...");

		//--- get services root package
		String pack = services.getAttributeValue(ConfigFile.Services.Attr.PACKAGE);

		// --- scan services elements
		for (Element service : (List<Element>) services
				.getChildren(ConfigFile.Services.Child.SERVICE)) {
			String name = service
					.getAttributeValue(ConfigFile.Service.Attr.NAME);

			info("   Adding service : " + name);
			
			try {
				serviceMan.addService(pack, service);
			} catch (Exception e) {
				warning("Raised exception while registering service. Skipped.");
				warning("   Service   : " + name);
				warning("   Package   : " + pack);
				warning("   Exception : " + e);
				warning("   Message   : " + e.getMessage());
				warning("   Stack     : " + Util.getStackTrace(e));
			}
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- 'schedules' element
	//---
	//---------------------------------------------------------------------------

	/** Setup schedules found in the 'schedules' element (config.xml)
	  */

	@SuppressWarnings("unchecked")
	private void initSchedules(Element schedules) throws Exception
	{
		info("Initializing schedules...");

		//--- get schedules root package
		String pack = schedules.getAttributeValue(ConfigFile.Schedules.Attr.PACKAGE);

		// --- scan schedules elements
		for (Element schedule : (List<Element>) schedules
				.getChildren(ConfigFile.Schedules.Child.SCHEDULE)) {
			String name = schedule
					.getAttributeValue(ConfigFile.Schedule.Attr.NAME);

			info("   Adding schedule : " + name);

			try {
				scheduleMan.addSchedule(pack, schedule);
			} catch (Exception e) {
				error("Raised exception while registering schedule. Skipped.");
				error("   Schedule  : " + name);
				error("   Package   : " + pack);
				error("   Exception : " + e);
				error("   Message   : " + e.getMessage());
				error("   Stack     : " + Util.getStackTrace(e));
			}
		}
	}

    //---------------------------------------------------------------------------
    //---
    //--- 'schedules' element
    //---
    //---------------------------------------------------------------------------

    /** Setup schedules found in the 'schedules' element (config.xml)
     */
	//---------------------------------------------------------------------------
	//---
	//--- Destroy
	//---
	//---------------------------------------------------------------------------

	public void destroy()
	{
		try
		{
			info("=== Stopping system ========================================");

			info("Shutting down monitor manager...");
			monitorManager.shutdown();

			info("Stopping schedule manager...");
			scheduleMan.exit();

			info("Stopping handlers...");
			stopHandlers();

			info("Stopping resources...");
			stopResources();

			info("=== System stopped ========================================");
		}
		catch (Exception e)
		{
			error("Raised exception during destroy");
			error("  Exception : " +e);
			error("  Message   : " +e.getMessage());
			error("  Stack     : " +Util.getStackTrace(e));
		}
	}

	//---------------------------------------------------------------------------
	/** Stop handlers
	  */

	private void stopHandlers() throws Exception {
		for (ApplicationHandler h : vAppHandlers) {
			h.stop();
		}
	}

	//---------------------------------------------------------------------------
	/** Stop resources
	  */

	private void stopResources()
	{
		providerMan.end();
		for (Activator a : vActivators) {
			info("   Stopping activator : " + a.getClass().getName());
			a.shutdown();
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getUploadDir() { return uploadDir; }

	//---------------------------------------------------------------------------

  public int getMaxUploadSize() { return maxUploadSize; }

  //---------------------------------------------------------------------------

	public void dispatch(ServiceRequest srvReq, UserSession session)
	{
		if (srvReq.getService() == null || srvReq.getService().length() == 0)
			srvReq.setService(defaultSrv);

		if (srvReq.getLanguage() == null || srvReq.getLanguage().length() == 0)
			srvReq.setLanguage(defaultLang);

		srvReq.setDebug(srvReq.hasDebug() && debugFlag);

		//--- if we have a startup error (ie. exception during startup) then
		//--- override with the startupErrorSrv service (if defined)
		if (serviceMan.isStartupError() && !startupErrorSrv.equals("") 
				&& !srvReq.getService().contains(startupErrorSrv))
			srvReq.setService(startupErrorSrv);

		//--- normal dispatch pipeline

		serviceMan.dispatch(srvReq, session);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Other private methods
	//---
	//---------------------------------------------------------------------------

	private void info   (String message) { Log.info   (Log.ENGINE, message); }
	private void warning(String message) { Log.warning(Log.ENGINE, message); }
	private void error  (String message) { Log.error  (Log.ENGINE, message); }
	private void fatal  (String message) { Log.fatal  (Log.ENGINE, message); }

	public ServiceManager getServiceManager() {
		return serviceMan;
	}

	public ProfileManager getProfileManager() { return serviceMan.getProfileManager(); }
	
    /**
     * Create Jeeves services from a configuration stored in the Services table
     * of the DBMS resource.
		 *
		 * @param name resource provider name to open dbms from (usually main-db)
     */
    private void setDBServicesElement(String name) {
				Dbms dbms = null;
        try {
						dbms = (Dbms) providerMan.getProvider(name).open();
            Element eltServices = new Element("services");
            eltServices.setAttribute("package", "org.fao.geonet");
            java.util.List serviceList = dbms.select("SELECT * FROM Services")
                    .getChildren();

            if (!dbLoaded) {
                for (int j = 0; j < serviceList.size(); j++) {

                    Element eltService = (Element) serviceList.get(j);
                    Element srv = new Element("service");
                    Element cls = new Element("class");
                    java.util.List paramList = dbms
                            .select("SELECT name, value FROM ServiceParameters WHERE service =?",
                                    Integer.valueOf(eltService
                                            .getChildText("id"))).getChildren();

                    for (int k = 0; k < paramList.size(); k++) {
                        Element eltParam = (Element) paramList.get(k);
                        String paramId = eltParam.getChildText("id");
                        if (eltParam.getChildText("value") != null
                                && !eltParam.getChildText("value").equals("")) {
                            cls.addContent(new Element("param").setAttribute(
                                    "name", "filter").setAttribute(
                                    "value",
                                    "+" + eltParam.getChildText("name") + ":"
                                            + eltParam.getChildText("value")));
                        }
                    }

                    srv.setAttribute("name", eltService.getChildText("name"))
                            .addContent(
                                    cls.setAttribute("name",
                                            eltService.getChildText("class")));
                    eltServices.addContent(srv);
                }
            }

            dbservices.add(eltServices);
        } catch (Exception e) {
            warning("Jeeves DBMS service configuration lookup failed (database may not be available yet). Message is: "
                    + e.getMessage());
        } finally {
					try {
						if (dbms != null) providerMan.getProvider(name).close(dbms);
					} catch (Exception e) {
						warning("Unable to close jeeves dbms - may not matter?");
						e.printStackTrace();
					}
				}

    }
}
