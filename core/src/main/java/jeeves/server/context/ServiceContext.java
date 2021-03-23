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

import javax.servlet.http.HttpServletRequest;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.TransactionStatus;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.persistence.EntityManager;

//=============================================================================

/**
 * Contains the context for a service execution.
 *
 * When creating a ServiceContext you are responsible for manging its use on the current thread and any cleanup:
 * <pre><code>
 * try {
 *    context = serviceMan.createServiceContext("AppHandler", appContext);
 *    context.setAsThreadLocal();
 *    ...
 * } finally {
 *    context.clearAsThreadLocal();
 *    context.clear();
 * }</code></pre>
 *
 * @see ServiceManager
 */
public class ServiceContext extends BasicContext {

    /**
     * ServiceContext is managed as a thread locale using setAsThreadLocal, clearAsThreadLocal and clear methods.
     * ThreadLocalPolicy defines the behaviour of these methods double checking that they are being used correctly.
     */
    public static enum ThreadLocalPolicy {
        /** Direct management of thread local with no checking. */
        DIRECT,
        /** Check behavior and log any unexpected use. */
        TRACE,
        /** Raise any {@link IllegalStateException} for unexpected behaviour */
        STRICT };
    /**
     * Use -Djeeves.server.context.service.policy to define policy:
     * <ul>
     *     <li>direct: direct management of thread local with no checking</li>
     *     <li>trace: check behavior and log unusal use</li>
     *     <li>strict: raise illegal state exception for unusual behavior</li>
     * </ul>
     */
    private static final ThreadLocalPolicy POLICY;
    static {
        String property = System.getProperty("jeeves.server.context.policy", "TRACE");
        ThreadLocalPolicy policy;
        try {
            policy = ThreadLocalPolicy.valueOf(property.toUpperCase());
        }
        catch (IllegalArgumentException defaultToDirect) {
            policy = ThreadLocalPolicy.DIRECT;
        }
        POLICY = policy;
    }

    /**
     * Be careful with thread local to avoid leaking resources, set POLICY above to trace allocation.
     */
    private static final InheritableThreadLocal<ServiceContext> THREAD_LOCAL_INSTANCE = new InheritableThreadLocal<ServiceContext>();

    /**
     * Trace allocation via {@link #setAsThreadLocal()}.
     */
    private Throwable allocation = null;

    private UserSession _userSession = new UserSession();
    private InputMethod _input;
    private OutputMethod _output;
    private Map<String, String> _headers;
    private String _language;
    private String _service;
    private String _ipAddress;
    private int _maxUploadSize;
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

    /**
     * Context for service execution.
     *
     * See factory method {@link ServiceManager#createServiceContext(String, String, HttpServletRequest)} and
     * {@link ServiceManager#createServiceContext(String, ConfigurableApplicationContext)}.
     *
     * @param service Service name
     * @param jeevesApplicationContext Application context
     * @param contexts Handler context
     * @param entityManager
     */
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
     *
     * If you call this method you are responsible for thread context management and {@link #clearAsThreadLocal()}.
     * <pre>
     * try {
     *     context.setAsThreadLocal();
     * }
     * finally {
     *     context.clearAsThreadLocal();
     * }
     * </pre>
     */
    public void setAsThreadLocal() {
        ServiceContext check = THREAD_LOCAL_INSTANCE.get();

        if( POLICY == ThreadLocalPolicy.DIRECT || check == null){
            // step one set thread local
            THREAD_LOCAL_INSTANCE.set(this);
            // step two ensure ApplicationContextHolder thread local kept in sync
            ApplicationContextHolder.set(this.getApplicationContext());
            // step three details on allocation
            allocation = new Throwable("ServiceContext allocated to thread");

            return;
        }

        if (this == check) {
            String unexpected = "Service " + _service + " Context: already in use for this thread";
            if( allocation != null ){
                // details on prior allocation
                unexpected += "\n\tContext '"+check._service+"' conflict: " + check.allocation.getStackTrace()[1];
            }
            // step one set thread local
            // (already done)
            // step two ensure ApplicationContextHolder thread local kept in sync
            if( ApplicationContextHolder.get() != null ){
                ApplicationContextHolder.clear();
            }
            ApplicationContextHolder.set(this.getApplicationContext());
            // step three detail on re-allocation
            allocation = new Throwable("ServiceContext allocated to thread");

            unexpected += "\n\tService '"+_service+"' allocate: " + allocation.getStackTrace()[1];
            checkUnexpectedState( unexpected );
            return;
        }

        // thread being recycled or reused for new service context
        //
        THREAD_LOCAL_INSTANCE.remove();

        String unexpected = "Service " + _service + " Context: Clearing prior service context " + check._service;
        if( check.allocation != null ){
            // details on prior allocation
            unexpected += "\n\tContext '"+check._service+"' conflict: " + check.allocation.getStackTrace()[1];
        }

        // step one set thread local
        THREAD_LOCAL_INSTANCE.set(this);
        // step two ensure ApplicationContextHolder thread local kept in sync
        if( ApplicationContextHolder.get() != null ){
            ApplicationContextHolder.clear();
        }
        ApplicationContextHolder.set(this.getApplicationContext());
        // step three detail on present re-allocation
        allocation = new Throwable("ServiceContext allocated to thread");

        unexpected += "\n\tService '"+_service+"' allocate: " + allocation.getStackTrace()[1];
        checkUnexpectedState( unexpected );
    }

    /** Log or raise exception based on {@link POLICY} */
    protected void checkUnexpectedState( String unexpected ){
        if( unexpected == null ){
            return; // nothing unexpected to report
        }
        switch( POLICY ){
            case DIRECT:
                break; // ignore
            case TRACE:
                debug(unexpected);
                break;
            case STRICT:
                throw new IllegalStateException(unexpected);
        }
    }

    /**
     * Called to clear the Service context for this thread and inherited threads.
     *
     * In general code that creates ServiceContext is responsible thread management and any cleanup:
     * <pre>
     * try {
     *     context.setAsThreadLocal();
     * }
     * finally {
     *     context.clearAsThreadLocal();
     * }
     * </pre>
     */
    public void clearAsThreadLocal() {
        ServiceContext check = THREAD_LOCAL_INSTANCE.get();

        // clean up thread local
        if( POLICY == ThreadLocalPolicy.DIRECT){
            if( check != null ){
                check.allocation = null;
                check = null;
            }
            THREAD_LOCAL_INSTANCE.remove();
            allocation = null;
            // ApplicationContextHolder.clear();
            return;
        }
        if( check == null ){
            String unexpected = "ServiceContext "+_service+" clearAsThreadLocal: '"+_service+"' unexpected state, thread local already cleared";
            if( allocation != null ){
                unexpected += "\n\tContext '"+_service+"' allocation: " + allocation.getStackTrace()[1];
            }
            allocation = null;
            // ApplicationContextHolder.clear();
            checkUnexpectedState( unexpected );
            return;
        }
        if (check == this){
            THREAD_LOCAL_INSTANCE.remove();
            allocation = null;
            // ApplicationContextHolder.clear();
            return;
        }

        String unexpected = "ServiceContext clearAsThreadLocal: \"+_service+\" unexpected state, thread local presently used by service context '"+check._service+"'";
        if( check.allocation != null ){
            unexpected += "\n\tContext '"+check._service+"' conflict: " + check.allocation.getStackTrace()[1];
        }
        if( allocation != null ){
            unexpected += "\n\tContext '"+_service+"' allocation: " + allocation.getStackTrace()[1];
        }
        THREAD_LOCAL_INSTANCE.remove();
        allocation = null;
        // ApplicationContextHolder.clear();
        if( ApplicationContextHolder.get() != null && ApplicationContextHolder.get() != getApplicationContext() ){
            unexpected += "\n\tApplicationContext '"+ApplicationContextHolder.get().getApplicationName()+"' conflict detected";
            unexpected += "\n\tApplicationContext '"+getApplicationContext().getApplicationName()+"' expected";
            ApplicationContextHolder.clear();
        }
        checkUnexpectedState( unexpected );
    }

    /**
     * Release any resources tied up by this service context.
     * <p>
     * In general code that creates a ServiceContext is responsible thread management and any cleanup:
     * <pre>
     * ServiceContext context = serviceMan.createServiceContext("AppHandler", appContext);
     * try {
     *     context.setAsThreadLocal();
     * }
     * finally {
     *     context.clearAsThreadLocal();
     *     context.clear();
     * }
     * </pre>
     */
    public void clear(){
        if( this._service != null) {
            this._service =  null;
            this._headers = null;
            this._responseHeaders = null;
            this._servlet = null;
            this._userSession = null;
        }
        else {
            debug("Service context unexpectedly cleared twice");
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    /**
     * Language code, or <code>"?"</code> if undefined.
     * @return language code, or <code>"?"</code> if undefined.
     */
    public String getLanguage() {
        if (_service == null ){
            //throw new NullPointerException("Service context cleared, language not available");
        }
        return _language;
    }
    /**
     * Language code, or <code>"?"</code> if undefined.
     * @param lang language code, or <code>"?"</code> if undefined.
     */
    public void setLanguage(final String lang) {
        _language = lang;
    }

    /**
     * Service name, or null if service context is no longer in use.
     *
     * @return service name, or null if service is no longer in use
     */
    public String getService() {
        return _service;
    }

    public void setService(String service) {
        if( service == null ){
            service = "internal";
        }
        this._service = service;
        logger = Log.createLogger(Log.WEBAPP + "." + service);
    }

    /**
     * IP address of request, or <code>"?"</code> for local loopback request.
     *
     * @return ip address, or <code>"?"</code> for loopback request.
     */
    public String getIpAddress() {
        if (_service == null ){
            throw new NullPointerException("Service context cleared, ip address not available");
        }
        return _ipAddress;
    }

    /**
     * IP address of request, or <code>"?"</code> for local loopback request.
     *
     * @param address ip, address or <code>"?"</code> for loopback request.
     */
    public void setIpAddress(final String address) {
        _ipAddress = address;
    }

    public Path getUploadDir() {
        return getBean(GeonetworkDataDirectory.class).getUploadDir();
    }

    public int getMaxUploadSize() {
        if (_service == null ){
            throw new NullPointerException("Service context cleared, max upload size not available");
        }
        return _maxUploadSize;
    }

    public void setMaxUploadSize(final int size) {
        _maxUploadSize = size;
    }

    /**
     * Warning: this may return a null value if the user is a crawler!!
     *
     * @return the user session stored on httpsession
     */
    public UserSession getUserSession() {
        if (_service == null ){
            throw new NullPointerException("Service context cleared, user session not available");
        }
        return _userSession;
    }

    public void setUserSession(final UserSession session) {
        _userSession = session;
    }

    /**
     * Safely look up user name, or <code>anonymous</code>.
     *
     * This is a quick null safe lookup of user name suitable for use in logging and error messages.
     *
     * @return username, or <code>anonymous</code> if unavailable.
     */
    public String userName(){
        if (_userSession == null || _userSession.getUsername() == null ){
            return "anonymous";
        }
        if( _userSession.getProfile() != null ){
            return _userSession.getUsername() + "/" + _userSession.getProfile();
        }
        return _userSession.getUsername();
    }

    public ProfileManager getProfileManager() {
        return getBean(ProfileManager.class);
    }

    //--------------------------------------------------------------------------

    public InputMethod getInputMethod() {
        if (_service == null ){
            throw new NullPointerException("Service context cleared, input method not available");
        }
        return _input;
    }

    public void setInputMethod(final InputMethod m) {
        _input = m;
    }

    public OutputMethod getOutputMethod() {
        if (_service == null ){
            throw new NullPointerException("Service context cleared, input method not available");
        }
        return _output;
    }

    public void setOutputMethod(final OutputMethod m) {
        _output = m;
    }

    public Map<String, String> getStartupErrors() {
        if (_service == null ){
            throw new NullPointerException("Service context cleared, output method not available");
        }
        return _startupErrors;
    }

    public void setStartupErrors(final Map<String, String> errs) {
        _startupErrors = errs;
        _startupError = true;
    }

    public boolean isStartupError() {
        if (_service == null ){
            throw new NullPointerException("Service context cleared, startup error not available");
        }
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
        if (_service == null ){
            throw new NullPointerException("Service context cleared, headers not available");
        }
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
        if (_service == null ){
            throw new NullPointerException("Service context cleared, servlet not available");
        }
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
                    final ServiceManager serviceManager = getApplicationContext().getBean(ServiceManager.class);
                    final ServiceContext localServiceContext = serviceManager.createServiceContext(request.getService(), getApplicationContext());
                    try {
                        UserSession session = ServiceContext.this._userSession;
                        if (session == null) {
                            session = new UserSession();
                        }
                        localServiceContext.setUserSession(session);

                        serviceManager.dispatch(request, session, localServiceContext);
                    } catch (Exception e) {
                        Log.error(Log.XLINK_PROCESSOR, "Failed to parse result xml" + request.getService());
                        throw new ServiceExecutionFailedException(request.getService(), e);
                    } finally {
                        if( localServiceContext == ServiceContext.get()){
                            // dispatch failed to clear cleanup localServiceContext
                            // restoring  back as thread local
                            ServiceContext.this.setAsThreadLocal();
                        }
                        localServiceContext.clear();
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

