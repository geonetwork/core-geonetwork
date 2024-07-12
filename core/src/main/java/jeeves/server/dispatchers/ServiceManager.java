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
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package jeeves.server.dispatchers;

import com.yammer.metrics.core.TimerContext;
import jeeves.component.ProfileManager;
import jeeves.constants.ConfigFile;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.monitor.MonitorManager;
import jeeves.monitor.timer.ServiceManagerGuiServicesTimer;
import jeeves.monitor.timer.ServiceManagerServicesTimer;
import jeeves.monitor.timer.ServiceManagerXslOutputTransformTimer;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.Call;
import jeeves.server.dispatchers.guiservices.GuiService;
import jeeves.server.dispatchers.guiservices.XmlFile;
import jeeves.server.sources.ServiceRequest;
import jeeves.server.sources.ServiceRequest.InputMethod;
import jeeves.server.sources.ServiceRequest.OutputMethod;
import jeeves.server.sources.http.HttpServiceRequest;
import jeeves.server.sources.http.JeevesServlet;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.io.EofException;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.Util;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.exceptions.NotAllowedEx;
import org.fao.geonet.exceptions.ServiceNotFoundEx;
import org.fao.geonet.exceptions.ServiceNotMatchedEx;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.BLOB;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.SOAPUtil;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.context.ConfigurableApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

//=============================================================================
public class ServiceManager {
    private Map<String, ArrayList<ServiceInfo>> htServices = new HashMap<String, ArrayList<ServiceInfo>>(100);
    private Map<String, Object> htContexts = new HashMap<String, Object>();
    private List<ErrorPage> vErrorPipe = new ArrayList<ErrorPage>();
    private List<GuiService> vDefaultGui = new ArrayList<GuiService>();
    private String baseUrl;
    private int maxUploadSize;
    private String defaultLang;
    private String defaultContType;
    private boolean defaultLocal;
    private JeevesServlet servlet;
    private boolean startupError = false;
    private Map<String, String> startupErrors;


    @PersistenceContext
    private EntityManager entityManager;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    static void info(String message) {
        Log.info(Log.SERVICE, message);
    }

    static void error(String message) {
        Log.error(Log.SERVICE, message);
    }

    public void setDefaultLang(String lang) {
        defaultLang = lang;
    }

    public void setDefaultContType(String type) {
        defaultContType = type;
    }

    public void setMaxUploadSize(int size) {
        maxUploadSize = size;
    }

    public void setDefaultLocal(boolean yesno) {
        defaultLocal = yesno;
    }

    public void setServlet(JeevesServlet serv) {
        servlet = serv;
    }

    //---------------------------------------------------------------------------

    public void setStartupErrors(Map<String, String> errors) {
        startupErrors = errors;
        startupError = true;
    }

    //---------------------------------------------------------------------------

    public boolean isStartupError() {
        return startupError;
    }
    //---------------------------------------------------------------------------

    public void setBaseUrl(String name) {
        baseUrl = name;

        if (!baseUrl.startsWith("/") && baseUrl.length() != 0)
            baseUrl = "/" + baseUrl;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Registering methods (service)
    //---
    //---------------------------------------------------------------------------

    public void registerContext(String name, Object context) {
        htContexts.put(name, context);
    }

    //---------------------------------------------------------------------------

    public void addDefaultGui(Element gui) throws Exception {
        vDefaultGui.add(getGuiService("", gui));
    }


    //---------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public ServiceInfo addService(String pack, Element srv, Path appPath) throws Exception {
        String name = srv.getAttributeValue(ConfigFile.Service.Attr.NAME);
        String match = srv.getAttributeValue(ConfigFile.Service.Attr.MATCH);
        String sheet = srv.getAttributeValue(ConfigFile.Service.Attr.SHEET);
        String cache = srv.getAttributeValue(ConfigFile.Service.Attr.CACHE);

        ServiceInfo si = ApplicationContextHolder.get().getBean(ServiceInfo.class);
        si.setMatch(match);
        si.setSheet(sheet);
        si.setCache(cache);

        ArrayList<ServiceInfo> al = htServices.get(name);

        if (al == null) {
            al = new ArrayList<>();
            htServices.put(name, al);
        } else {
            info("Service " + name + " already exist, re-register it.");
            htServices.remove(name);
            al = new ArrayList<>();
            htServices.put(name, al);
        }

        al.add(si);

        //--- parse classes elements

        List<Element> classes = srv.getChildren(ConfigFile.Service.Child.CLASS);

        for (Element classe : classes) {
            si.addService(buildService(pack, classe, appPath));
        }

        //--- parse output pages

        List<Element> outputs = srv.getChildren(ConfigFile.Service.Child.OUTPUT);

        for (Element output : outputs) {
            si.addOutputPage(buildOutputPage(pack, output));
        }

        //--- parse error pages

        List<Element> errors = srv.getChildren(ConfigFile.Service.Child.ERROR);

        for (Element error : errors) {
            si.addErrorPage(buildErrorPage(error));
        }
        return si;
    }

    //---------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Service buildService(String pack, Element clas, Path appPath) throws Exception {
        //--- get class name

        String name = clas.getAttributeValue(ConfigFile.Class.Attr.NAME);

        if (name == null) {
            throw new IllegalArgumentException("Missing 'name' attrib in 'class' element");
        }

        if (name.startsWith(".")) {
            name = pack + name;
        }

        //--- create instance

        Service service = (Service) Class.forName(name).newInstance();

        service.init(appPath, new ServiceConfig(clas.getChildren(ConfigFile.Class.Child.PARAM)));

        return service;
    }

    //---------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private OutputPage buildOutputPage(String pack, Element output) throws Exception {
        OutputPage outPage = new OutputPage();

        outPage.setStyleSheet(output.getAttributeValue(ConfigFile.Output.Attr.SHEET));
        outPage.setForward(output.getAttributeValue(ConfigFile.Output.Attr.FORWARD));
        outPage.setTestCondition(output.getAttributeValue(ConfigFile.Output.Attr.TEST));
        outPage.setFile(output.getAttributeValue(ConfigFile.Output.Attr.FILE) != null);
        outPage.setBLOB(output.getAttributeValue(ConfigFile.Output.Attr.BLOB) != null);

        //--- set content type

        String contType = output.getAttributeValue(ConfigFile.Output.Attr.CONTENT_TYPE);

        if (contType == null)
            contType = defaultContType;

        outPage.setContentType(contType);

        //--- handle children

        List<Element> guiList = output.getChildren();

        for (Element gui : guiList) {
            outPage.addGuiService(getGuiService(pack, gui));
        }

        return outPage;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Registering methods (error)
    //---
    //---------------------------------------------------------------------------

    private GuiService getGuiService(String pack, Element elem) throws Exception {
        final GeonetworkDataDirectory dataDirectory = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
        if (ConfigFile.Output.Child.XML.equals(elem.getName()))
            return new XmlFile(elem, defaultLang, defaultLocal);

        if (ConfigFile.Output.Child.CALL.equals(elem.getName()))
            return new Call(elem, pack, dataDirectory.getWebappDir());

        throw new IllegalArgumentException("Unknown GUI element : " + Xml.getString(elem));
    }

    //---------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private ErrorPage buildErrorPage(Element err) throws Exception {
        ErrorPage errPage = new ErrorPage();

        errPage.setStyleSheet(err.getAttributeValue(ConfigFile.Error.Attr.SHEET));
        errPage.setTestCondition(err.getAttributeValue(ConfigFile.Error.Attr.ID));

        //--- set content type

        String contType = err.getAttributeValue(ConfigFile.Error.Attr.CONTENT_TYPE);

        if (contType == null)
            contType = defaultContType;

        errPage.setContentType(contType);

        // -- set status code
        int statusCode;
        String strStatusCode = err.getAttributeValue(ConfigFile.Error.Attr.STATUS_CODE);

        try {
            statusCode = Integer.parseInt(strStatusCode);
        } catch (Exception e) {
            // Default value for an error page where status code is not defined.
            statusCode = 500;
        }

        errPage.setStatusCode(statusCode);

        //--- handle children

        List<Element> guiList = err.getChildren();

        for (Element gui : guiList) {
            errPage.addGuiService(getGuiService("?", gui));
        }

        return errPage;
    }

    public void addErrorPage(Element err) throws Exception {
        vErrorPipe.add(buildErrorPage(err));
    }

    public ServiceContext createServiceContext(String name, ConfigurableApplicationContext appContext) {
        ServiceContext context = new ServiceContext(name, appContext, htContexts,
            entityManager);

        context.setBaseUrl(baseUrl);
        context.setLanguage("?");
        context.setUserSession(null);
        context.setIpAddress("?");
        context.setMaxUploadSize(maxUploadSize);
        context.setServlet(servlet);

        return context;
    }

    /**
     * If we do have the optional x-forwarded-for request header then
     * use whatever is in it to record ip address of client
     */
    public static String getRequestIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("x-forwarded-for");
        return StringUtils.isNotEmpty(xForwardedForHeader)
            ? xForwardedForHeader : request.getRemoteAddr();
    }


    public ServiceContext createServiceContext(String name, String lang, HttpServletRequest request) {
        ServiceContext context = new ServiceContext(name, ApplicationContextHolder.get(), htContexts, entityManager);

        context.setBaseUrl(baseUrl);
        context.setLanguage(lang);
        context.setIpAddress(getRequestIpAddress(request));
        context.setMaxUploadSize(maxUploadSize);
        context.setServlet(servlet);

        // Session is created by ApiInterceptor when needed
        // Save the session here in the ServiceContext (not used in the API package).
        final HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            UserSession session = (UserSession) httpSession.getAttribute(Jeeves.Elem.SESSION);
            if (session != null) {

                context.setUserSession(session);
            }
        }

        return context;
    }

    public void dispatch(ServiceRequest req, UserSession session) {
        ServiceContext context = new ServiceContext(req.getService(), ApplicationContextHolder.get(),
            htContexts, entityManager);
        dispatch(req, session, context);
    }

    //---------------------------------------------------------------------------
    //--- Handle error
    //---------------------------------------------------------------------------

    //---------------------------------------------------------------------------
    //---
    //--- Dispatching methods
    //---
    //---------------------------------------------------------------------------
    public void dispatch(ServiceRequest req, UserSession session, ServiceContext context) {
        context.setBaseUrl(baseUrl);
        context.setLanguage(req.getLanguage());
        context.setUserSession(session);
        context.setIpAddress(req.getAddress());
        context.setMaxUploadSize(maxUploadSize);
        context.setInputMethod(req.getInputMethod());
        context.setOutputMethod(req.getOutputMethod());
        context.setHeaders(req.getHeaders());
        context.setServlet(servlet);
        if (startupError) context.setStartupErrors(startupErrors);

        context.setAsThreadLocal();

        //--- invoke service and build result

        Element response = null;
        ServiceInfo srvInfo = null;

        try {
            while (true) {
                String srvName = req.getService();

                info("Dispatching : " + srvName);
                logParameters(req.getParams());

                ArrayList<ServiceInfo> al = htServices.get(srvName);

                if (al == null) {
                    error("Service not found : " + srvName);
                    throw new ServiceNotFoundEx(srvName);
                }

                for (ServiceInfo si : al) {
                    if (si.matches(req.getParams())) {
                        srvInfo = si;
                        break;
                    }
                }

                if (srvInfo == null) {
                    error("Service not matched in list : " + srvName);
                    throw new ServiceNotMatchedEx(srvName);
                }

                //---------------------------------------------------------------------
                //--- check access

                TimerContext timerContext = getMonitorManager().getTimer(ServiceManagerServicesTimer.class).time();
                try {
                    response = srvInfo.execServices(req.getParams(), context);
                } finally {
                    timerContext.stop();
                }

                // Did we change some header on the service?
                for (Entry<String, String> entry : context.getResponseHeaders()
                    .entrySet()) {
                    ((HttpServiceRequest) req).getHttpServletResponse()
                        .setHeader(entry.getKey(), entry.getValue());
                }

                if (context.getStatusCode() != null) {
                    req.setStatusCode(context.getStatusCode());
                }
                //---------------------------------------------------------------------
                //--- handle forward

                OutputPage outPage = srvInfo.findOutputPage(response);
                String forward = dispatchOutput(req, context, response, outPage, srvInfo.isCacheSet());

                if (forward == null) {
                    info(" -> dispatch ended for : " + srvName);
                    return;
                } else {
                    info(" -> forwarding to : " + forward);

                    // Use servlet redirect for user.login and user.logout services.
                    // TODO: Make redirect configurable for services in jeeves
                    if (srvName.equals("metadata.quiet.delete") || srvName.equals("user.login") || srvName.equals("user.logout")) {
                        HttpServiceRequest req2 = (HttpServiceRequest) req;

                        req2.getHttpServletResponse().setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                        req2.getHttpServletResponse().setHeader("Location", baseUrl + "/" + context.getApplicationContext().getBean
                            (NodeInfo.class).getId() + "/" + req.getLanguage() + "/" + forward);

                        return;
                    } else {
                        Element elForward = new Element(Jeeves.Elem.FORWARD);
                        elForward.setAttribute(Jeeves.Attr.SERVICE, srvName);

                        // --- send response to forwarded service request

                        response.setName(Jeeves.Elem.REQUEST);
                        response.addContent(elForward);

                        context.setService(forward);
                        req.setService(forward);
                        req.setParams(response);
                    }
                }
            }
        } catch (Throwable e) {
            if (e instanceof NotAllowedEx) {
                throw (NotAllowedEx) e;
            } else {
                handleError(req, response, context, srvInfo, e);
            }
        }
    }

    //---------------------------------------------------------------------------
    //--- Dispatch output
    //---------------------------------------------------------------------------

    private MonitorManager getMonitorManager() {
        return ApplicationContextHolder.get().getBean(MonitorManager.class);
    }

    //---------------------------------------------------------------------------
    //--- Dispatch error
    //---------------------------------------------------------------------------

    private void handleError(ServiceRequest req, Element response, ServiceContext context,
                             ServiceInfo srvInfo, Throwable e) {
        Element error = getError(req, e, response);
        String id = error.getAttributeValue("id");
        int code = getErrorCode(e);
        boolean cache = (srvInfo != null) && srvInfo.isCacheSet();

        if (isDebug()) debug("Raised exception while executing service\n" + Xml.getString(error));

        try {
            InputMethod input = req.getInputMethod();
            OutputMethod output = req.getOutputMethod();

            if (input == InputMethod.SOAP || output == OutputMethod.SOAP) {
                req.setStatusCode(code);
                req.beginStream("application/soap+xml; charset=UTF-8", cache);

                error.setAttribute("encodingStyle", "http://www.geonetwork.org/encoding/error",
                    SOAPUtil.NAMESPACE_ENV);

                boolean sender = (code < 500);
                String message = error.getChildText("class") + " : " + error.getChildText("message");

                req.write(SOAPUtil.embedExc(error, sender, id, message));
            } else if (input == InputMethod.XML || output == OutputMethod.XML) {
                req.setStatusCode(code);
                req.beginStream("application/xml; charset=UTF-8", cache);
                req.write(error);
            } else {
                //--- try to dispatch to the error output

                ErrorPage errPage = null;
                if (srvInfo != null)
                    errPage = srvInfo.findErrorPage(id);

                if (errPage == null)
                    errPage = findErrorPage(id);

                try {
                    dispatchError(req, context, error, errPage, cache);
                } catch (Exception ex) {
                    //--- ok, if we are here there is no error page
                    //--- so we display plain xml data

                    req.setStatusCode(code);
                    req.beginStream("application/xml; charset=UTF-8", cache);
                    req.write(error);
                }
            }
        } catch (Exception ex) {
            error("Raised exception while writing response to exception");
            error("  Exception : " + ex);
            error("  Message   : " + ex.getMessage());
            error("  Stack     : " + Util.getStackTrace(ex));
        }
    }

    //---------------------------------------------------------------------------

    /**
     * Takes a service's response and builds the output
     */

    private String dispatchOutput(ServiceRequest req, ServiceContext context,
                                  Element response, OutputPage outPage, boolean cache) throws Exception {
        info("   -> dispatching to output for : " + req.getService());

        //------------------------------------------------------------------------
        //--- check if the output page is a foward

        if (outPage != null) {
            String sForward = outPage.getForward();

            if (sForward != null)
                return sForward;
        }

        //------------------------------------------------------------------------
        //--- write result to output page
        final GeonetworkDataDirectory dataDirectory = context.getBean(GeonetworkDataDirectory.class);

        if (outPage == null) {
            //--- if there is no output page we output the xml result (if any)

            if (response == null)
                warning("Response is null and there is no output page for : " + req.getService());
            else {
                info("     -> writing xml for : " + req.getService());

                //--- this logging is usefull for xml services that are called by javascript code
                if (isDebug()) debug("Service xml is :\n" + Xml.getString(response));

                InputMethod in = req.getInputMethod();
                OutputMethod out = req.getOutputMethod();

                if (in == InputMethod.SOAP || out == OutputMethod.SOAP) {

                    // Did we set up a status code for the response?
                    if (context.getStatusCode() != null) {
                        ((ServiceRequest) req).setStatusCode(context
                            .getStatusCode());
                    }

                    req.beginStream("application/soap+xml; charset=UTF-8", cache);

                    if (!SOAPUtil.isEnvelope(response)) {
                        response = SOAPUtil.embed(response);
                    }
                    req.write(response);
                } else {

                    if (req.hasJSONOutput()) {
                        req.beginStream("application/json; charset=UTF-8", cache);
                        req.getOutputStream().write(Xml.getJSON(response).getBytes(Constants.ENCODING));
                        req.endStream();
                    } else {
                        if (response.getAttribute("redirect") != null) {
                            HttpServiceRequest req2 = (HttpServiceRequest) req;
                            req2.getHttpServletResponse().setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                            req2.getHttpServletResponse().setHeader("Location", response.getAttribute("url").getValue());
                            req2.getHttpServletResponse().setHeader("Content-type", response.getAttribute("mime-type").getValue());

                        } else {
                            req.beginStream("application/xml; charset=UTF-8", cache);
                            req.write(response);

                        }
                    }
                }
            }
        }

        //--- FILE output

        else {
            final NodeInfo nodeInfo = context.getBean(NodeInfo.class);
            if (outPage.isFile()) {
                // PDF Output
                if (outPage.getContentType().equals("application/pdf") && !outPage.getStyleSheet().equals("")) {

                    //--- build the xml data for the XSL/FO translation
                    Path styleSheet = IO.toPath(outPage.getStyleSheet());
                    Element guiElem;
                    TimerContext guiServicesTimerContext = context.getMonitorManager().getTimer(ServiceManagerGuiServicesTimer.class)
                        .time();
                    try {
                        guiElem = outPage.invokeGuiServices(context, response, vDefaultGui);
                    } finally {
                        guiServicesTimerContext.stop();
                    }

                    addPrefixes(guiElem, context.getLanguage(), req.getService(), nodeInfo
                        .getId());

                    Element rootElem = new Element(Jeeves.Elem.ROOT)
                        .addContent(guiElem)
                        .addContent(response);

                    Element reqElem = (Element) req.getParams().clone();
                    reqElem.setName(Jeeves.Elem.REQUEST);

                    rootElem.addContent(reqElem);

                    //--- do an XSL transformation

                    styleSheet = dataDirectory.resolveWebResource(Jeeves.Path.XSL).resolve(styleSheet);

                    if (!Files.exists(styleSheet))
                        error(" -> stylesheet not found on disk, aborting : " + styleSheet);
                    else {
                        info(" -> transforming with stylesheet : " + styleSheet);

                        try {
                            TimerContext timerContext = context.getMonitorManager().getTimer(ServiceManagerXslOutputTransformTimer.class)
                                .time();
                            Path file;
                            try {
                                //--- first we do the transformation
                                file = Xml.transformFOP(dataDirectory.getUploadDir(), rootElem, styleSheet.toString());
                            } finally {
                                timerContext.stop();
                            }

                            // Checks for a parameter documentFileName with the document file name,
                            // otherwise uses a default value
                            String documentName = guiElem.getChildText("documentFileName");
                            if (StringUtils.isEmpty(documentName)) {
                                documentName = "document.pdf";
                            } else {
                                if (!documentName.endsWith(".pdf")) {
                                    documentName = documentName + ".pdf";
                                }

                                Calendar c = Calendar.getInstance();

                                documentName = documentName.replace("{year}", c.get(Calendar.YEAR) + "");
                                documentName = documentName.replace("{month}", c.get(Calendar.MONTH) + "");
                                documentName = documentName.replace("{day}", c.get(Calendar.DAY_OF_MONTH) + "");

                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                                SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");

                                documentName = documentName.replace("{date}", dateFormat.format(c.getTime()));
                                documentName = documentName.replace("{datetime}", datetimeFormat.format(c.getTime()));
                            }

                            response = BinaryFile.encode(200, file, documentName, true).getElement();
                        } catch (Exception e) {
                            error(" -> exception during XSL/FO transformation for : " + req.getService());
                            error(" -> (C) stylesheet : " + styleSheet);
                            error(" -> (C) message : " + e.getMessage());
                            error(" -> (C) exception : " + e.getClass().getSimpleName());

                            throw e;
                        }

                        info(" -> end transformation for : " + req.getService());
                    }


                }
                final BinaryFile binaryFile = new BinaryFile(response);
                String contentType = binaryFile.getContentType();

                if (contentType == null)
                    contentType = "application/octet-stream";

                String contentDisposition = binaryFile.getContentDisposition();
                String contentLength = binaryFile.getContentLength();

                long cl = (contentLength == null) ? -1 : Long.parseLong(contentLength);

                // Did we set up a status code for the response?
                if (context.getStatusCode() != null) {
                    ((ServiceRequest) req).setStatusCode(context.getStatusCode());
                }
                req.beginStream(contentType, cl, contentDisposition, cache);
                binaryFile.write(req.getOutputStream());
                req.endStream();
                binaryFile.removeIfTheCase();
            }

            //--- BLOB output

            else if (outPage.isBLOB()) {
                String contentType = BLOB.getContentType(response);

                if (contentType == null)
                    contentType = "application/octet-stream";

                String contentDisposition = BLOB.getContentDisposition(response);
                String contentLength = BLOB.getContentLength(response);

                long cl = (contentLength == null) ? -1 : Long.parseLong(contentLength);

                req.beginStream(contentType, cl, contentDisposition, cache);
                BLOB.write(response, req.getOutputStream());
                req.endStream();
            }

            //--- HTML/XML output

            else {
                //--- build the xml data for the XSL translation

                Path styleSheet = IO.toPath(outPage.getStyleSheet());
                Element guiElem;
                TimerContext guiServicesTimerContext = getMonitorManager().getTimer(ServiceManagerGuiServicesTimer.class).time();
                try {
                    guiElem = outPage.invokeGuiServices(context, response, vDefaultGui);
                } finally {
                    guiServicesTimerContext.stop();
                }

                addPrefixes(guiElem, context.getLanguage(), req.getService(), nodeInfo.getId());

                Element rootElem = new Element(Jeeves.Elem.ROOT)
                    .addContent(guiElem)
                    .addContent(response);

                Element reqElem = (Element) req.getParams().clone();
                reqElem.setName(Jeeves.Elem.REQUEST);

                rootElem.addContent(reqElem);

                //--- do an XSL translation or send xml data to a debug routine

                if (req.hasDebug()) {
                    req.beginStream("application/xml; charset=UTF-8", cache);
                    req.write(rootElem);
                } else {
                    //--- do an XSL transformation

                    styleSheet = dataDirectory.getWebappDir().resolve(Jeeves.Path.XSL).resolve(styleSheet);

                    if (!Files.exists(styleSheet))
                        error("     -> stylesheet not found on disk, aborting : " + styleSheet);
                    else {
                        info("     -> transforming with stylesheet : " + styleSheet);
                        try {
                            //--- then we set the content-type and output the result
                            // If JSON output requested, run the XSLT transformation and the JSON
                            if (req.hasJSONOutput()) {
                                Element xsltResponse = null;
                                TimerContext timerContext = context.getMonitorManager().getTimer(ServiceManagerXslOutputTransformTimer
                                    .class).time();
                                try {
                                    //--- first we do the transformation
                                    xsltResponse = Xml.transform(rootElem, styleSheet);
                                } finally {
                                    timerContext.stop();
                                }
                                req.beginStream("application/json; charset=UTF-8", cache);
                                req.getOutputStream().write(Xml.getJSON(xsltResponse).getBytes(Constants.ENCODING));
                                req.endStream();
                            } else {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                TimerContext timerContext = context.getMonitorManager().getTimer(ServiceManagerXslOutputTransformTimer
                                    .class).time();
                                try {
                                    //--- first we do the transformation
                                    Xml.transform(rootElem, styleSheet, baos);
                                } finally {
                                    timerContext.stop();
                                }

                                if (outPage.getContentType() != null
                                    && outPage.getContentType().startsWith("text/plain")) {
                                    req.beginStream(outPage.getContentType(), -1, "attachment;", cache);
                                } else {
                                    req.beginStream(outPage.getContentType(), cache);
                                }
                                req.getOutputStream().write(baos.toByteArray());
                                req.endStream();
                            }
                        } catch (EofException e) {
                            // ignore this.
                            // it happens because the stream closes by client.
                        } catch (Exception e) {
                            error("   -> exception during transformation for : " + req.getService());
                            error("   ->  (C) stylesheet : " + styleSheet);
                            error("   ->  (C) message    : " + e.getMessage());
                            error("   ->  (C) exception  : " + e.getClass().getSimpleName());

                            throw e;
                        }

                        info("     -> end transformation for : " + req.getService());
                    }
                }
            }
        }

        //------------------------------------------------------------------------
        //--- end data stream

        info("   -> output ended for : " + req.getService());

        return null;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    /**
     * Takes a service's response and builds the output
     */

    private void dispatchError(ServiceRequest req, ServiceContext context,
                               Element response, ErrorPage outPage, boolean cache) throws Exception {
        info("   -> dispatching to error for : " + req.getService());

        //--- build the xml data for the XSL translation

        Path styleSheet = IO.toPath(outPage.getStyleSheet());
        Element guiElem = outPage.invokeGuiServices(context, response, vDefaultGui);

        // Dispatch HTTP status code
        req.setStatusCode(outPage.getStatusCode());

        addPrefixes(guiElem, context.getLanguage(), req.getService(), context.getBean(NodeInfo.class).getId());

        Element rootElem = new Element(Jeeves.Elem.ROOT)
            .addContent(guiElem)
            .addContent(response);

        if (req.hasDebug()) {
            req.beginStream("application/xml; charset=UTF-8", cache);
            req.write(rootElem);
        } else {
            //--- do an XSL transformation

            styleSheet = context.getBean(GeonetworkDataDirectory.class).resolveWebResource(Jeeves.Path.XSL).resolve(styleSheet);

            info("     -> transforming with stylesheet : " + styleSheet);

            try {
                req.beginStream(outPage.getContentType(), cache);
                Xml.transform(rootElem, styleSheet, req.getOutputStream());
                req.endStream();
            } catch (EofException e) {
                // ignore this.
                // it happens because the stream closes by client.
            } catch (Exception e) {
                Log.error(Log.JEEVES, e.getMessage(), e);
            }
        }

        info("     -> end error transformation for : " + req.getService());
        info("   -> error ended for : " + req.getService());
    }

    //---------------------------------------------------------------------------

    private ErrorPage findErrorPage(String id) {
        for (int i = 0; i < vErrorPipe.size(); i++) {
            ErrorPage p = (ErrorPage) vErrorPipe.get(i);

            if (p.matches(id))
                return p;
        }

        error("No default error found for id : " + id);
        throw new NullPointerException("No default error found for id : " + id);
    }

    //---------------------------------------------------------------------------

    private Element getError(ServiceRequest req, Throwable t, Element response) {
        Element params = new Element(Jeeves.Elem.REQUEST)
            .addContent(new Element("language").setText(req.getLanguage()))
            .addContent(new Element("service").setText(req.getService()));

        Element error = JeevesException.toElement(t)
            .addContent(params);

        //--- add response (if any)

        if (response != null)
            error.addContent((Element) response.clone());

        return error;
    }

    //---------------------------------------------------------------------------

    private int getErrorCode(Throwable t) {
        int code = 500;

        if (t instanceof JeevesException)
            code = ((JeevesException) t).getCode();

        return code;
    }

    //---------------------------------------------------------------------------

    private void addPrefixes(Element root, String lang, String service, String nodeId) {
        root.addContent(new Element(Jeeves.Elem.LANGUAGE).setText(lang));
        root.addContent(new Element(Jeeves.Elem.LANGUAGE_2_CHARS).setText(XslUtil.twoCharLangCode(lang)));
        root.addContent(new Element(Jeeves.Elem.REQ_SERVICE).setText(service));
        root.addContent(new Element(Jeeves.Elem.BASE_URL).setText(baseUrl));
        root.addContent(new Element(Jeeves.Elem.LOC_URL).setText(baseUrl + "/loc/" + lang));
        root.addContent(new Element(Jeeves.Elem.BASE_SERVICE).setText(baseUrl + "/" + nodeId));
        root.addContent(new Element(Jeeves.Elem.NODE_ID).setText(nodeId));
        root.addContent(new Element(Jeeves.Elem.LOC_SERVICE).setText(baseUrl + "/" + nodeId + "/" + lang));

        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        root.addContent(new Element("nodeUrl").setText(settingManager.getNodeURL()));
        root.addContent(new Element("baseUrl").setText(settingManager.getBaseURL()));
        root.addContent(new Element("serverUrl").setText(settingManager.getServerURL()));
    }

    @SuppressWarnings("unchecked")
    private void logParameters(Element params) {
        List<Element> paramsList = params.getChildren();

        if (paramsList.size() == 0)
            if (isDebug()) debug(" -> no input parameters");
            else if (isDebug()) debug(" -> parameters are : \n" + Xml.getString(params));
    }

    private boolean isDebug() {
        return Log.isDebugEnabled(Log.SERVICE);
    }

    private void debug(String message) {
        Log.debug(Log.SERVICE, message);
    }

    private void warning(String message) {
        Log.warning(Log.SERVICE, message);
    }

    public ProfileManager getProfileManager() {
        return ApplicationContextHolder.get().getBean(ProfileManager.class);
    }

}

//=============================================================================

