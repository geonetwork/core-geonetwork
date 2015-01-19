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

package jeeves.server.context;

import jeeves.component.ProfileManager;
import jeeves.server.UserSession;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.local.LocalServiceRequest;
import jeeves.server.sources.ServiceRequest.InputMethod;
import jeeves.server.sources.ServiceRequest.OutputMethod;
import jeeves.server.sources.http.JeevesServlet;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Logger;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.persistence.EntityManager;

//=============================================================================

/**
 * Contains the context for a service execution.
 */
public class ServiceContext extends BasicContext {

    private static final InheritableThreadLocal<ServiceContext> THREAD_LOCAL_INSTANCE = new InheritableThreadLocal<ServiceContext>();

    /**
     * ServiceManager sets the service context thread local when dispatch is called.  this method will
     * return null or the service context
     *
     * @return the service context set by service context or null if no in an inherited thread
     */
    @CheckForNull
    public static ServiceContext get() {
        return THREAD_LOCAL_INSTANCE.get();
    }

    /**
     * Called to set the Service context for this thread and inherited threads.
     */
    public void setAsThreadLocal() {
        THREAD_LOCAL_INSTANCE.set(this);
        ApplicationContextHolder.set(this.getApplicationContext());
    }


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
     * Property to be able to add custom response headers depending on the code
     * (and not the xml of Jeeves)
     * 
     * Be very careful using this, because right now Jeeves doesn't check the
     * headers. Can lead to infinite loops or wrong behaviour.
     * 
     * @see #_statusCode
     * 
     */
    private Map<String, String> _responseHeaders;
    /**
     * Property to be able to add custom http status code headers depending on
     * the code (and not the xml of Jeeves)
     * 
     * Be very careful using this, because right now Jeeves doesn't check the
     * headers. Can lead to infinite loops or wrong behaviour.
     * 
     * @see #_responseHeaders
     * 
     */
    private Integer _statusCode;

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public ServiceContext(final String service, final ConfigurableApplicationContext jeevesApplicationContext,
                          final Map<String, Object> contexts, final EntityManager entityManager)
	{
		super(jeevesApplicationContext, contexts, entityManager);

		setService(service);

        setResponseHeaders(new HashMap<String, String>());
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public String getLanguage()  { return _language;  }
	public String getService()   { return _service;   }
	public String getIpAddress() { return _ipAddress; }
	public Path getUploadDir() { return getBean(GeonetworkDataDirectory.class).getUploadDir(); }
    public int getMaxUploadSize() { return _maxUploadSize; }

	public UserSession    getUserSession()    { return _userSession; }
	public ProfileManager getProfileManager() { return getBean(ProfileManager.class);   }

	public InputMethod  getInputMethod()  { return _input;  }
	public OutputMethod getOutputMethod() { return _output; }
	public Map<String, String> getStartupErrors() { return _startupErrors; }
	public boolean isStartupError() { return _startupError; }
	public boolean isServletInitialized() { 
		if (_servlet != null) {
            return _servlet.isInitialized();
        } else {
            return true; // Jeeves not running in servlet container eg for testing
        }
	}

	//--------------------------------------------------------------------------

	public void setLanguage(final String lang)    { _language = lang;    }
	public void setServlet(final JeevesServlet serv)    { _servlet = serv;    }
	public void setIpAddress(final String address) { _ipAddress = address; }
    public void setMaxUploadSize(final int size)   { _maxUploadSize = size; }
    public void setStartupErrors(final Map<String,String> errs)   { _startupErrors = errs; _startupError = true; }

	public void setInputMethod(final InputMethod m)  { _input = m; }
	public void setOutputMethod(final OutputMethod m) { _output = m; }

	//--------------------------------------------------------------------------

	public void setService(final String service) {
		this._service = service;
		logger       = Log.createLogger(Log.WEBAPP +"."+ service);
	}

	//--------------------------------------------------------------------------

	public void setUserSession(final UserSession session) {
		_userSession = session;
	}

	//--------------------------------------------------------------------------

	public void setLogger(final Logger l)
	{
		logger = l;
	}

	//--------------------------------------------------------------------------

	/**
	 * @return The map of headers from the request
	 */
	public Map<String, String> getHeaders()
	{
		return _headers;
	}

	/**
	 * Set the map of headers from the request.
	 * @param headers The new headers to be set.
	 */
	public void setHeaders(Map<String, String> headers)
	{
		this._headers = headers;
	}

	public JeevesServlet getServlet() {
        return _servlet;
    }

    /**
     * Execute a service but _don't_ parse the result.  This is used if in one of two cases.
     *
     * <ol>
     *     <li>If the service is side-effect only and the result doesn't matter</li>
     *     <li>If the response is not XML, for example if it is JSON or a string</li>
     * </ol>
     */
    public void executeOnly(LocalServiceRequest request) throws Exception {
        ServiceContext context = new ServiceContext(request.getService(), getApplicationContext(), htContexts, getEntityManager());
        UserSession session = this._userSession;
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
    }

    /**
     * Call {@link #executeOnly(jeeves.server.local.LocalServiceRequest)} and return the response as XML.
     */
    public Element execute(LocalServiceRequest request) throws Exception {
        executeOnly(request);
        try {
            return request.getResult();
        } catch (Exception e) {
            Log.error(Log.XLINK_PROCESSOR, "Failed to parse result xml from service:" + request.getService() + "\n"
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

    public void setStatusCode(Integer statusCode) {
        this._statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return _statusCode;
    }

}

//=============================================================================

