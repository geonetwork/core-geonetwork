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

package jeeves.server.context;

import jeeves.component.ProfileManager;
import jeeves.server.UserSession;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.local.LocalServiceRequest;
import jeeves.server.sources.ServiceRequest.InputMethod;
import jeeves.server.sources.ServiceRequest.OutputMethod;
import jeeves.server.sources.http.JeevesServlet;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Logger;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.TransactionStatus;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.CheckForNull;
import jakarta.persistence.EntityManager;

//=============================================================================

/**
 * Contains the context for a service execution.
 */
public class ServiceContext extends BasicContext {

    private static final InheritableThreadLocal<ServiceContext> THREAD_LOCAL_INSTANCE = new InheritableThreadLocal<ServiceContext>();
    private UserSession _userSession = new UserSession();
    private InputMethod _input;
    private OutputMethod _output;
    private Map<String, String> _headers;
    private String _language;
    private String _service;
    private String _ipAddress;
    private long _maxUploadSize;
    private JeevesServlet _servlet;
    private boolean _startupError = false;
    private Map<String, String> _startupErrors;
    /**
     * Property to be able to add custom response headers depending on the code (and not the xml of
     * Jeeves)
     *
     * Be very careful using this, because right now Jeeves doesn't check the headers. Can lead to
     * infinite loops or wrong behaviour.
     *
     * @see #_statusCode
     */
    private Map<String, String> _responseHeaders;
    /**
     * Property to be able to add custom http status code headers depending on the code (and not the
     * xml of Jeeves)
     *
     * Be very careful using this, because right now Jeeves doesn't check the headers. Can lead to
     * infinite loops or wrong behaviour.
     *
     * @see #_responseHeaders
     */
    private Integer _statusCode;
    public ServiceContext(final String service, final ConfigurableApplicationContext jeevesApplicationContext,
                          final Map<String, Object> contexts, final EntityManager entityManager) {
        super(jeevesApplicationContext, contexts, entityManager);

        setService(service);

        setResponseHeaders(new HashMap<String, String>());
    }

    /**
     * ServiceManager sets the service context thread local when dispatch is called.  this method
     * will return null or the service context
     *
     * @return the service context set by service context or null if no in an inherited thread
     */
    @CheckForNull
    public static ServiceContext get() {
        return THREAD_LOCAL_INSTANCE.get();
    }

    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    /**
     * Called to set the Service context for this thread and inherited threads.
     */
    public void setAsThreadLocal() {
        THREAD_LOCAL_INSTANCE.set(this);
        ApplicationContextHolder.set(this.getApplicationContext());
    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    public String getLanguage() {
        return _language;
    }

    public void setLanguage(final String lang) {
        _language = lang;
    }

    public String getService() {
        return _service;
    }

    public void setService(final String service) {
        this._service = service;
        logger = Log.createLogger(Log.WEBAPP + "." + service);
    }

    public String getIpAddress() {
        return _ipAddress;
    }

    public void setIpAddress(final String address) {
        _ipAddress = address;
    }

    public Path getUploadDir() {
        return getBean(GeonetworkDataDirectory.class).getUploadDir();
    }

    public long getMaxUploadSize() {
        return _maxUploadSize;
    }

    public void setMaxUploadSize(final long size) {
        _maxUploadSize = size;
    }

    /**
     * Warning: this may return a null value if the user is a crawler!!
     *
     * @return the user session stored on httpsession
     */
    public UserSession getUserSession() {
        return _userSession;
    }

    public void setUserSession(final UserSession session) {
        _userSession = session;
    }

    public ProfileManager getProfileManager() {
        return getBean(ProfileManager.class);
    }

    //--------------------------------------------------------------------------

    public InputMethod getInputMethod() {
        return _input;
    }

    public void setInputMethod(final InputMethod m) {
        _input = m;
    }

    public OutputMethod getOutputMethod() {
        return _output;
    }

    public void setOutputMethod(final OutputMethod m) {
        _output = m;
    }

    public Map<String, String> getStartupErrors() {
        return _startupErrors;
    }

    public void setStartupErrors(final Map<String, String> errs) {
        _startupErrors = errs;
        _startupError = true;
    }

    public boolean isStartupError() {
        return _startupError;
    }

    //--------------------------------------------------------------------------

    public boolean isServletInitialized() {
        if (_servlet != null) {
            return _servlet.isInitialized();
        } else {
            return true; // Jeeves not running in servlet container eg for testing
        }
    }

    //--------------------------------------------------------------------------

    public void setLogger(final Logger l) {
        logger = l;
    }

    //--------------------------------------------------------------------------

    /**
     * @return The map of headers from the request
     */
    public Map<String, String> getHeaders() {
        return _headers;
    }

    //--------------------------------------------------------------------------

    /**
     * Set the map of headers from the request.
     *
     * @param headers The new headers to be set.
     */
    public void setHeaders(Map<String, String> headers) {
        this._headers = headers;
    }

    public JeevesServlet getServlet() {
        return _servlet;
    }

    public void setServlet(final JeevesServlet serv) {
        _servlet = serv;
    }

    /**
     * Execute a service but _don't_ parse the result.  This is used if in one of two cases.
     *
     * <ol> <li>If the service is side-effect only and the result doesn't matter</li> <li>If the
     * response is not XML, for example if it is JSON or a string</li> </ol>
     */
    public void executeOnly(final LocalServiceRequest request) throws Exception {

        TransactionManager.runInTransaction("ServiceContext#executeOnly: " + request.getAddress(), getApplicationContext(),
            TransactionManager.TransactionRequirement.CREATE_NEW, TransactionManager.CommitBehavior.ALWAYS_COMMIT, false,
            new TransactionTask<Void>() {
                @Override
                public Void doInTransaction(TransactionStatus transaction) throws Throwable {
                    final ServiceContext context = new ServiceContext(request.getService(), getApplicationContext(), htContexts, getEntityManager());
                    UserSession session = ServiceContext.this._userSession;
                    if (session == null) {
                        session = new UserSession();
                    }
                    try {
                        final ServiceManager serviceManager = context.getBean(ServiceManager.class);
                        serviceManager.dispatch(request, session, context);
                    } catch (Exception e) {
                        Log.error(Log.XLINK_PROCESSOR, "Failed to parse result xml" + request.getService());
                        throw new ServiceExecutionFailedException(request.getService(), e);
                    } finally {
                        // set old context back as thread local
                        setAsThreadLocal();
                    }
                    return null;
                }
            });
    }

    /**
     * Call {@link #executeOnly(jeeves.server.local.LocalServiceRequest)} and return the response as
     * XML.
     */
    public Element execute(LocalServiceRequest request) throws Exception {
        executeOnly(request);
        try {
            return request.getResult();
        } catch (Exception e) {
            Log.error(Log.XLINK_PROCESSOR, "Failed to parse result xml from service:" + request.toString() + "\n"
                + request.getResultString());
            throw new ServiceExecutionFailedException(request.getService(), e);
        }
    }

    public Map<String, String> getResponseHeaders() {
        return _responseHeaders;
    }

    private void setResponseHeaders(Map<String, String> responseHeaders) {
        this._responseHeaders = responseHeaders;
    }

    public Integer getStatusCode() {
        return _statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this._statusCode = statusCode;
    }

}

//=============================================================================

