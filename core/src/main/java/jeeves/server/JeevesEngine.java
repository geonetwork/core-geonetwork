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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.PropertyConfigurator;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.Logger;
import org.fao.geonet.Util;
import org.fao.geonet.domain.Service;
import org.fao.geonet.domain.ServiceParam;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.repository.ServiceRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import jeeves.component.ProfileManager;
import jeeves.constants.ConfigFile;
import jeeves.constants.Jeeves;
import jeeves.interfaces.ApplicationHandler;
import jeeves.monitor.MonitorManager;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.overrides.ConfigurationOverrides;
import jeeves.server.sources.ServiceRequest;
import jeeves.server.sources.http.JeevesServlet;

//=============================================================================

/**
 * This is the main class. It handles http connections and inits the system
 */
public class JeevesEngine {
    private static final int DEFAULT_MAX_UPLOAD_SIZE_MD = 50;
    private static final int BYTES_PER_KB = 1024;

    private String _defaultSrv;
    private String _startupErrorSrv;
    private String _defaultLang;
    private boolean _debugFlag;

    /* true if the 'general' part has been loaded */
    private boolean _generalLoaded;


    private Logger _appHandLogger = Log.createLogger(Log.APPHAND);
    private List<Element> _appHandList = new ArrayList<Element>();
    private Vector<ApplicationHandler> _appHandlers = new Vector<ApplicationHandler>();
    private List<Element> _dbServices = new ArrayList<Element>();
    private Path _appPath;
    private int _maxUploadSize;


    //---------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //---------------------------------------------------------------------------

    public static void handleStartupError(Throwable e) {
        Log.fatal(Log.ENGINE, "Raised exception during init");
        Log.fatal(Log.ENGINE, "   Exception : " + e);
        Log.fatal(Log.ENGINE, "   Message   : " + e.getMessage());
        Log.fatal(Log.ENGINE, "   Stack     : " + Util.getStackTrace(e));

        if (Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty(Jeeves.SHUTDOWN_ON_STARTUP_ERROR))) {
            System.err.println("\n\n\tERROR STARTING UP GEONETWORK.  System property " + Jeeves.SHUTDOWN_ON_STARTUP_ERROR + " == " + System.getProperty(Jeeves.SHUTDOWN_ON_STARTUP_ERROR));
            System.err.println("\n\n\t>> HARD SHUTDOWN INITIATED <<");
            System.exit(1);
        }

        throw new RuntimeException("Exception raised", e);
    }

    /**
     * Inits the engine, loading all needed data.
     */
    public void init(final Path appPath, final Path configPath, final String baseUrl, final JeevesServlet servlet) throws ServletException {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        ServiceManager _serviceMan = appContext.getBean(ServiceManager.class);
        MonitorManager _monitorManager = appContext.getBean(MonitorManager.class);

        ServletContext servletContext = null;
        if (servlet != null) {
            servletContext = servlet.getServletContext();
        }

        try {
            Path log4jConfig = configPath.resolve("log4j.cfg");
            if (Files.exists(log4jConfig)) {
                PropertyConfigurator.configure(log4jConfig.toUri().toURL());
            }

            ConfigurationOverrides.DEFAULT.updateLoggingAsAccordingToOverrides(servletContext, appPath);


            _monitorManager.init(servletContext, baseUrl);
            JeevesEngine.this._appPath = appPath;

            long start = System.currentTimeMillis();

            long maxMem = Runtime.getRuntime().maxMemory() / BYTES_PER_KB;
            long totMem = Runtime.getRuntime().totalMemory() / BYTES_PER_KB;
            long freeMem = Runtime.getRuntime().freeMemory() / BYTES_PER_KB;

            long usedMem = totMem - freeMem;
            long startFreeMem = maxMem - usedMem;

            // System.setProperty("javax.xml.transform.TransformerFactory",
            //						 "net.sf.saxon.TransformerFactoryImpl");
            // Do this using library meta-inf to avoid affecting other servlets
            // in the same container

            info("=== Starting system ========================================");

            //---------------------------------------------------------------------
            //--- init system
            info("Engine : " + this.getClass().getName());
            info("Java version : " + System.getProperty("java.vm.version"));
            info("Java vendor  : " + System.getProperty("java.vm.vendor"));

            setupXSLTTransformerFactory(servlet);

            info("Path    : " + appPath);
            info("BaseURL : " + baseUrl);

            _serviceMan.setBaseUrl(baseUrl);
            _serviceMan.setServlet(servlet);


            loadConfigFile(servletContext, configPath, Jeeves.CONFIG_FILE, _serviceMan);
            loadConfigDB(appContext, -1);

            //--- handlers must be started here because they may need the context
            //--- with the ProfileManager already loaded

            for (int i = 0; i < _appHandList.size(); i++)
                initAppHandler(_appHandList.get(i), servlet);

            //---------------------------------------------------------------------

            long end = System.currentTimeMillis();
            long duration = TimeUnit.MILLISECONDS.toSeconds(end - start);

            freeMem = Runtime.getRuntime().freeMemory() / BYTES_PER_KB;
            totMem = Runtime.getRuntime().totalMemory() / BYTES_PER_KB;
            usedMem = totMem - freeMem;

            long endFreeMem = maxMem - usedMem;
            long dataMem = startFreeMem - endFreeMem;

            info("Memory used is  : " + dataMem + " Kb");
            info("Total memory is : " + maxMem + " Kb");
            info("Startup time is : " + duration + " (secs)");

            info("=== System working =========================================");
        } catch (Throwable e) {
            handleStartupError(e);
        }

    }

    /**
     * Looks up the implementation of XSLT factory defined in META-INF/services/javax.xml.transform.TransformerFactory
     * and instantiates that implementation. This way, a conflicting setting in System Properties is
     * overridden for this application only.
     */
    private void setupXSLTTransformerFactory(JeevesServlet servlet) throws IOException, TransformerConfigurationException {

        InputStream in = null;
        BufferedReader br = null;
        // In debug mode, Jeeves may load a different file
        // Load javax.xml.transform.TransformerFactory from application path instead
        if (servlet != null) {
            in = servlet.getServletContext().getResourceAsStream(TransformerFactoryFactory.TRANSFORMER_PATH);
        }
        if (in == null) {
            File f = new File(_appPath + TransformerFactoryFactory.TRANSFORMER_PATH);
            in = new FileInputStream(f);
        }
        try {

            if (in != null) {
                br = new BufferedReader(new InputStreamReader(in, Constants.ENCODING));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.length() == 0) {
                        warning("Malformed definition of XSLT transformer (in: META-INF/services/javax.xml.transform.TransformerFactory).");
                    }
                    TransformerFactoryFactory.init(line);
                    break;
                }
            }
        } catch (IOException x) {
            String msg = "Definition of XSLT transformer not found (tried: " + new File(_appPath +
                TransformerFactoryFactory.TRANSFORMER_PATH).getCanonicalPath() + ")";
            if (servlet != null) {
                msg += " and servlet.getServletContext().getResourceAsStream(" + TransformerFactoryFactory.TRANSFORMER_PATH + ")";
            }
            warning(msg);
            error(x.getMessage());
            error("  Stack     : " + Util.getStackTrace(x));
        } finally {
            IOUtils.closeQuietly(in);
            if (br != null) {
                IOUtils.closeQuietly(br);
            }
        }
    }


    //---------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void loadConfigFile(ServletContext servletContext, Path path, String fileName, ServiceManager serviceMan) throws Exception {
        if (fileName.charAt(0) == '/' || fileName.charAt(0) == '\\') {
            fileName = fileName.substring(1);
        }
        Path file = path.resolve(fileName);

        info("Loading : " + file);

        Element configRoot = Xml.loadFile(file);

        ConfigurationOverrides.DEFAULT.updateWithOverrides(file.toString(), servletContext, _appPath, configRoot);

        Element elGeneral = configRoot.getChild(ConfigFile.Child.GENERAL);
        Element elDefault = configRoot.getChild(ConfigFile.Child.DEFAULT);

        if (!_generalLoaded) {
            if (elGeneral == null) {
                throw new NullPointerException("Missing 'general' element in config file :" + file);
            }

            if (elDefault == null) {
                throw new NullPointerException("Missing 'default' element in config file :" + file);
            }

            _generalLoaded = true;

            initGeneral(elGeneral, serviceMan);
            initDefault(elDefault, serviceMan);
        } else {
            if (elGeneral != null) {
                throw new IllegalArgumentException("Illegal 'general' element in secondary include");
            }

            if (elDefault != null) {
                throw new IllegalArgumentException("Illegal 'default' element in secondary include");
            }
        }

        //--- init app-handlers

        _appHandList.addAll(configRoot.getChildren(ConfigFile.Child.APP_HANDLER));

        //--- init services

        List<Element> srvList = configRoot.getChildren(ConfigFile.Child.SERVICES);

        for (Element aSrvList : srvList) {
            initServices(aSrvList);
        }
        //--- init monitoring

        List<Element> monitorList = configRoot.getChildren(ConfigFile.Child.MONITORS);

        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        for (Element aMonitorList : monitorList) {
            context.getBean(MonitorManager.class).initMonitors(aMonitorList);
        }

        //--- recurse on includes

        List<Element> includes = configRoot.getChildren(ConfigFile.Child.INCLUDE);

        for (Element include : includes) {
            loadConfigFile(servletContext, path, include.getText(), serviceMan);
        }

    }

    //---------------------------------------------------------------------------
    //---
    //--- 'general' element
    //---
    //---------------------------------------------------------------------------

    /**
     * Setup parameters from config tag. (config.xml)
     */

    private void initGeneral(final Element general, final ServiceManager serviceMan) throws BadInputEx {
        info("Initializing general configuration...");

        try {
            _maxUploadSize = Integer.parseInt(Util.getParam(general, ConfigFile.General.Child.MAX_UPLOAD_SIZE));
        } catch (Exception e) {
            _maxUploadSize = DEFAULT_MAX_UPLOAD_SIZE_MD;
            error("Maximum upload size not properly configured in config.xml. Using default size of 50MB");
            error("   Exception : " + e);
            error("   Message   : " + e.getMessage());
            error("   Stack     : " + Util.getStackTrace(e));
        }

        _debugFlag = "true".equals(general.getChildText(ConfigFile.General.Child.DEBUG));

        serviceMan.setMaxUploadSize(_maxUploadSize);
    }

    //---------------------------------------------------------------------------
    //---
    //--- 'general' element
    //---
    //---------------------------------------------------------------------------

    /**
     * Setup parameters from config tag. (config.xml)
     */

    @SuppressWarnings("unchecked")
    private void initDefault(Element defaults, ServiceManager serviceMan) throws Exception {
        info("Initializing defaults...");

        _defaultSrv = Util.getParam(defaults, ConfigFile.Default.Child.SERVICE);

        // -- Don't break behaviour before gn 2.7 - if the startupErrorService
        // -- doesn't exist then ignore this parameter
        _startupErrorSrv = Util.getParam(defaults, ConfigFile.Default.Child.STARTUPERRORSERVICE, "");
        _defaultLang = Util.getParam(defaults, ConfigFile.Default.Child.LANGUAGE);
        String defaultContType = Util.getParam(defaults, ConfigFile.Default.Child.CONTENT_TYPE);

        boolean defaultLocal = "true".equals(defaults.getChildText(ConfigFile.Default.Child.LOCALIZED));

        info("   Default local is :" + defaultLocal);

        serviceMan.setDefaultLang(_defaultLang);
        serviceMan.setDefaultLocal(defaultLocal);
        serviceMan.setDefaultContType(defaultContType);

        List<Element> errorPages = defaults.getChildren(ConfigFile.Default.Child.ERROR);

        for (Element errorPage : errorPages) {
            serviceMan.addErrorPage(errorPage);
        }

        Element gui = defaults.getChild(ConfigFile.Default.Child.GUI);

        if (gui != null) {
            List<Element> guiElems = gui.getChildren();

            for (Element guiElem : guiElems) {
                serviceMan.addDefaultGui(guiElem);
            }
        }
    }

    //---------------------------------------------------------------------------
    //---
    //--- 'appHandler' element
    //---
    //---------------------------------------------------------------------------

    private void initAppHandler(Element handler, JeevesServlet servlet) throws Exception {
        if (handler == null) {
            info("Handler not found");
        } else {
            String className = handler.getAttributeValue(ConfigFile.AppHandler.Attr.CLASS);

            if (className == null) {
                throw new IllegalArgumentException("Missing '" + ConfigFile.AppHandler.Attr.CLASS
                    + "' attribute in '" + ConfigFile.Child.APP_HANDLER
                    + "' element");
            }

            ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
            ServiceManager serviceMan = appContext.getBean(ServiceManager.class);
            MonitorManager monitorManager = appContext.getBean(MonitorManager.class);

            info("Found handler : " + className);

            Class<?> c = Class.forName(className);

            ApplicationHandler h = (ApplicationHandler) c.newInstance();

            ServiceContext srvContext = serviceMan.createServiceContext("AppHandler", appContext);
            srvContext.setLanguage(_defaultLang);
            srvContext.setLogger(_appHandLogger);
            srvContext.setServlet(servlet);
            srvContext.setAsThreadLocal();

            try {
                info("--- Starting handler --------------------------------------");

                Object context = h.start(handler, srvContext);

                _appHandlers.add(h);
                serviceMan.registerContext(h.getContextName(), context);
                monitorManager.initMonitorsForApp(srvContext);

                info("--- Handler started ---------------------------------------");
            } catch (Exception e) {
                Map<String, String> errors = new HashMap<String, String>();
                String eS = "Raised exception while starting the application. " +
                    "Fix the error and restart.";
                error(eS);
                errors.put("Error", eS);
                error("   Handler   : " + className);
                errors.put("Handler", className);
                error("   Exception : " + e);
                errors.put("Exception", e.toString());
                error("   Message   : " + e.getMessage());
                errors.put("Message", e.getMessage());
                final String stackTrace = Util.getStackTrace(e);
                error("   Stack     : " + stackTrace);
                errors.put("Stack", stackTrace);
                error(errors.toString());
                // only set the error if we don't already have one
                if (!serviceMan.isStartupError()) {
                    serviceMan.setStartupErrors(errors);
                }
            }
        }
    }

    //---------------------------------------------------------------------------
    //---
    //--- 'services' element
    //---
    //---------------------------------------------------------------------------

    /**
     * Setup services found in the services tag (config.xml)
     */

    @SuppressWarnings("unchecked")
    public void initServices(Element services) throws Exception {
        info("Initializing services...");

        //--- get services root package
        String pack = services.getAttributeValue(ConfigFile.Services.Attr.PACKAGE);

        ServiceManager serviceManager = ApplicationContextHolder.get().getBean(ServiceManager.class);

        // --- scan services elements
        for (Element service : (List<Element>) services
            .getChildren(ConfigFile.Services.Child.SERVICE)) {
            String name = service
                .getAttributeValue(ConfigFile.Service.Attr.NAME);

            info("   Adding service : " + name);

            try {
                serviceManager.addService(pack, service, this._appPath);
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
    //--- Destroy
    //---
    //---------------------------------------------------------------------------
    @PreDestroy
    public void destroy() {
        try {
            info("=== Stopping system ========================================");

            // The monitor manager will be stopped by the ApplicationContextHolder when it is closed

            info("Stopping handlers...");
            stopHandlers();

            info("=== System stopped ========================================");
        } catch (Exception e) {
            error("Raised exception during destroy");
            error("  Exception : " + e);
            error("  Message   : " + e.getMessage());
            error("  Stack     : " + Util.getStackTrace(e));
        }
    }

    //---------------------------------------------------------------------------

    /**
     * Stop handlers.
     */

    private void stopHandlers() throws Exception {
        for (ApplicationHandler h : _appHandlers) {
            h.stop();
        }
    }

    //---------------------------------------------------------------------------

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public Path getUploadDir() {
        return ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class).getUploadDir();
    }

    //---------------------------------------------------------------------------

    public int getMaxUploadSize() {
        return _maxUploadSize;
    }

    //---------------------------------------------------------------------------

    public void dispatch(ServiceRequest srvReq, UserSession session) {
        ServiceManager serviceManager = ApplicationContextHolder.get().getBean(ServiceManager.class);

        if (srvReq.getService() == null || srvReq.getService().length() == 0)
            srvReq.setService(_defaultSrv);

        if (srvReq.getLanguage() == null || srvReq.getLanguage().length() == 0)
            srvReq.setLanguage(_defaultLang);

        srvReq.setDebug(srvReq.hasDebug() && _debugFlag);

        //--- if we have a startup error (ie. exception during startup) then
        //--- override with the startupErrorSrv service (if defined)
        if (serviceManager.isStartupError() && !_startupErrorSrv.equals("")
            && !srvReq.getService().contains(_startupErrorSrv))
            srvReq.setService(_startupErrorSrv);

        //--- normal dispatch pipeline

        serviceManager.dispatch(srvReq, session);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Other private methods
    //---
    //---------------------------------------------------------------------------

    private void info(String message) {
        Log.info(Log.ENGINE, message);
    }

    private void warning(String message) {
        Log.warning(Log.ENGINE, message);
    }

    private void error(String message) {
        Log.error(Log.ENGINE, message);
    }

    private void fatal(String message) {
        Log.fatal(Log.ENGINE, message);
    }

    public ServiceManager getServiceManager() {
        return ApplicationContextHolder.get().getBean(ServiceManager.class);
    }

    public ProfileManager getProfileManager() {
        return getServiceManager().getProfileManager();
    }

    /**
     * Create or reload Jeeves services from a configuration stored in the Services table of the
     * DBMS resource.
     *
     * @param serviceIdentifierToLoad -1 for all or the service identifier
     */
    public void loadConfigDB(ApplicationContext context, int serviceIdentifierToLoad) {
        try {
            Element eltServices = new Element("services");
            eltServices.setAttribute("package", "org.fao.geonet");

            java.util.List<Service> serviceList = null;
            ServiceRepository serviceRepo = context.getBean(ServiceRepository.class);
            if (serviceIdentifierToLoad == -1) {
                serviceList = serviceRepo.findAll();
            } else {
                serviceList = Collections.singletonList(serviceRepo.findOne(serviceIdentifierToLoad));
            }

            for (Service service : serviceList) {
                if (service != null) {
                    Element srv = new Element("service");
                    Element cls = new Element("class");

                    List<ServiceParam> paramList = service.getParameters();
                    StringBuilder filter = new StringBuilder();
                    if (!service.getExplicitQuery().isEmpty()) {
                        filter.append(service.getExplicitQuery());
                    }
                    for (ServiceParam serviceParam : paramList) {
                        if (serviceParam.getValue() != null && !serviceParam.getValue().trim().isEmpty()) {
                            filter.append(" ");
                            if (serviceParam.getOccur() == null) {
                                filter.append("+");
                            } else {
                                filter.append(serviceParam.getOccur());
                            }
                            filter.append(serviceParam.getName()).append(":").append(serviceParam.getValue());
                        }
                    }
                    cls.addContent(new Element("param").
                        setAttribute("name", "filter").
                        setAttribute("value", filter.toString().trim()));

                    srv.setAttribute("name", service.getName())
                        .addContent(
                            cls.setAttribute("name",
                                service.getClassName()));
                    eltServices.addContent(srv);
                }
            }

            _dbServices.add(eltServices);

            for (int i = 0; i < _dbServices.size(); i++) {
                initServices(_dbServices.get(i));
            }
        } catch (Exception e) {
            warning("Jeeves DBMS service configuration lookup failed (database may not be available yet). Message is: "
                + e.getMessage());
        }

    }
}
